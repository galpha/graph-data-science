/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.gds.procedures.integration;

import org.neo4j.gds.ProcedureCallContextReturnColumns;
import org.neo4j.gds.algorithms.AlgorithmMemoryValidationService;
import org.neo4j.gds.algorithms.RequestScopedDependencies;
import org.neo4j.gds.algorithms.community.CommunityAlgorithmsEstimateBusinessFacade;
import org.neo4j.gds.algorithms.community.CommunityAlgorithmsFacade;
import org.neo4j.gds.algorithms.community.CommunityAlgorithmsMutateBusinessFacade;
import org.neo4j.gds.algorithms.community.CommunityAlgorithmsStatsBusinessFacade;
import org.neo4j.gds.algorithms.community.CommunityAlgorithmsStreamBusinessFacade;
import org.neo4j.gds.algorithms.community.CommunityAlgorithmsWriteBusinessFacade;
import org.neo4j.gds.algorithms.estimation.AlgorithmEstimator;
import org.neo4j.gds.algorithms.metrics.AlgorithmMetricsService;
import org.neo4j.gds.algorithms.mutateservices.MutateNodePropertyService;
import org.neo4j.gds.algorithms.runner.AlgorithmRunner;
import org.neo4j.gds.algorithms.writeservices.WriteNodePropertyService;
import org.neo4j.gds.configuration.DefaultsConfiguration;
import org.neo4j.gds.configuration.LimitsConfiguration;
import org.neo4j.gds.core.loading.GraphStoreCatalogService;
import org.neo4j.gds.core.write.ExporterContext;
import org.neo4j.gds.logging.Log;
import org.neo4j.gds.memest.DatabaseGraphStoreEstimationService;
import org.neo4j.gds.memest.FictitiousGraphStoreEstimationService;
import org.neo4j.gds.procedures.KernelTransactionAccessor;
import org.neo4j.gds.procedures.TaskRegistryFactoryService;
import org.neo4j.gds.procedures.TerminationFlagService;
import org.neo4j.gds.procedures.community.CommunityProcedureFacade;
import org.neo4j.gds.procedures.configparser.ConfigurationParser;
import org.neo4j.gds.services.DatabaseIdAccessor;
import org.neo4j.gds.services.UserAccessor;
import org.neo4j.gds.services.UserLogServices;
import org.neo4j.internal.kernel.api.exceptions.ProcedureException;
import org.neo4j.kernel.api.procedure.Context;

/**
 * We call it a provider because it is used as a sub-provider to the {@link org.neo4j.gds.procedures.GraphDataScience} provider.
 */
public class CommunityProcedureProvider {
    // Global state and services
    private final Log log;
    private final GraphStoreCatalogService graphStoreCatalogService;
    private final boolean useMaxMemoryEstimation;

    // Request scoped state and services
    private final AlgorithmMetaDataSetterService algorithmMetaDataSetterService;
    private final DatabaseIdAccessor databaseIdAccessor;
    private final ExporterBuildersProviderService exporterBuildersProviderService;
    private final KernelTransactionAccessor kernelTransactionAccessor;
    private final TaskRegistryFactoryService taskRegistryFactoryService;
    private final AlgorithmMetricsService algorithmMetricsService;
    private final TerminationFlagService terminationFlagService;
    private final UserLogServices userLogServices;
    private final UserAccessor userAccessor;

    public CommunityProcedureProvider(
        Log log,
        GraphStoreCatalogService graphStoreCatalogService,
        boolean useMaxMemoryEstimation,
        AlgorithmMetaDataSetterService algorithmMetaDataSetterService,
        DatabaseIdAccessor databaseIdAccessor,
        KernelTransactionAccessor kernelTransactionAccessor,
        ExporterBuildersProviderService exporterBuildersProviderService,
        TaskRegistryFactoryService taskRegistryFactoryService,
        AlgorithmMetricsService algorithmMetricsService,
        TerminationFlagService terminationFlagService,
        UserLogServices userLogServices,
        UserAccessor userAccessor
    ) {
        this.log = log;
        this.graphStoreCatalogService = graphStoreCatalogService;
        this.useMaxMemoryEstimation = useMaxMemoryEstimation;

        this.algorithmMetaDataSetterService = algorithmMetaDataSetterService;
        this.databaseIdAccessor = databaseIdAccessor;
        this.kernelTransactionAccessor = kernelTransactionAccessor;
        this.exporterBuildersProviderService = exporterBuildersProviderService;
        this.taskRegistryFactoryService = taskRegistryFactoryService;
        this.algorithmMetricsService = algorithmMetricsService;
        this.terminationFlagService = terminationFlagService;
        this.userLogServices = userLogServices;
        this.userAccessor = userAccessor;
    }

    public CommunityProcedureFacade createCommunityProcedureFacade(Context context) throws ProcedureException {
        // Neo4j's services
        var graphDatabaseService = context.graphDatabaseAPI();
        var kernelTransaction = kernelTransactionAccessor.getKernelTransaction(context);

        // GDS derived services - request scoped things
        var algorithmMetaDataSetter = algorithmMetaDataSetterService.getAlgorithmMetaDataSetter(kernelTransaction);
        var algorithmMemoryValidationService = new AlgorithmMemoryValidationService(log, useMaxMemoryEstimation);
        var databaseId = databaseIdAccessor.getDatabaseId(context.graphDatabaseAPI());
        var returnColumns = new ProcedureCallContextReturnColumns(context.procedureCallContext());
        var terminationFlag = terminationFlagService.createTerminationFlag(kernelTransaction);
        var user = userAccessor.getUser(context.securityContext());
        var taskRegistryFactory = taskRegistryFactoryService.getTaskRegistryFactory(databaseId, user);
        var userLogRegistryFactory = userLogServices.getUserLogRegistryFactory(databaseId, user);
        var exportBuildersProvider = exporterBuildersProviderService.identifyExportBuildersProvider(graphDatabaseService);
        var requestScopedDependencies = RequestScopedDependencies.builder()
            .with(databaseId)
            .with(terminationFlag)
            .with(user)
            .build();
        var algorithmRunner = new AlgorithmRunner(
            log,
            graphStoreCatalogService,
            algorithmMemoryValidationService,
            taskRegistryFactory,
            userLogRegistryFactory,
            algorithmMetricsService,
            requestScopedDependencies
        );

        // algorithm facade
        var communityAlgorithmsFacade = new CommunityAlgorithmsFacade(algorithmRunner);

        // moar services
        var fictitiousGraphStoreEstimationService = new FictitiousGraphStoreEstimationService();
        var graphLoaderContext = GraphLoaderContextProvider.buildGraphLoaderContext(
            context,
            databaseId,
            taskRegistryFactory,
            terminationFlag,
            userLogRegistryFactory,
            log
        );
        var databaseGraphStoreEstimationService = new DatabaseGraphStoreEstimationService(
            user,
            graphLoaderContext
        );

        var exporterContext = new ExporterContext.ProcedureContextWrapper(context);


        // business facades
        var estimateBusinessFacade = new CommunityAlgorithmsEstimateBusinessFacade(
            new AlgorithmEstimator(
                graphStoreCatalogService,
                fictitiousGraphStoreEstimationService,
                databaseGraphStoreEstimationService,
                databaseId,
                user
            )
        );
        var statsBusinessFacade = new CommunityAlgorithmsStatsBusinessFacade(communityAlgorithmsFacade);
        var streamBusinessFacade = new CommunityAlgorithmsStreamBusinessFacade(communityAlgorithmsFacade);
        var mutateBusinessFacade = new CommunityAlgorithmsMutateBusinessFacade(
            communityAlgorithmsFacade,
            new MutateNodePropertyService(log)
        );
        CommunityAlgorithmsWriteBusinessFacade writeBusinessFacade = new CommunityAlgorithmsWriteBusinessFacade(
            communityAlgorithmsFacade,
            new WriteNodePropertyService(
                exportBuildersProvider.nodePropertyExporterBuilder(exporterContext),
                log,
                taskRegistryFactory,
                terminationFlag
            )
        );
        var configurationParser = new ConfigurationParser(
            DefaultsConfiguration.Instance,
            LimitsConfiguration.Instance
        );

        // procedure facade
        return new CommunityProcedureFacade(
            configurationParser,
            algorithmMetaDataSetter,
            user,
            returnColumns,
            estimateBusinessFacade,
            mutateBusinessFacade,
            statsBusinessFacade,
            streamBusinessFacade,
            writeBusinessFacade
        );
    }
}
