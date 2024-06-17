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
package org.neo4j.gds.applications.algorithms.community;

import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.gds.api.GraphName;
import org.neo4j.gds.api.properties.nodes.NodePropertyValues;
import org.neo4j.gds.applications.algorithms.machinery.AlgorithmProcessingTemplate;
import org.neo4j.gds.applications.algorithms.machinery.MutateNodeProperty;
import org.neo4j.gds.applications.algorithms.machinery.ResultBuilder;
import org.neo4j.gds.applications.algorithms.metadata.NodePropertiesWritten;
import org.neo4j.gds.approxmaxkcut.ApproxMaxKCutResult;
import org.neo4j.gds.approxmaxkcut.config.ApproxMaxKCutMutateConfig;
import org.neo4j.gds.core.utils.paged.dss.DisjointSetStruct;
import org.neo4j.gds.k1coloring.K1ColoringMutateConfig;
import org.neo4j.gds.k1coloring.K1ColoringResult;
import org.neo4j.gds.kcore.KCoreDecompositionMutateConfig;
import org.neo4j.gds.kcore.KCoreDecompositionResult;
import org.neo4j.gds.kmeans.KmeansMutateConfig;
import org.neo4j.gds.kmeans.KmeansResult;
import org.neo4j.gds.labelpropagation.LabelPropagationMutateConfig;
import org.neo4j.gds.labelpropagation.LabelPropagationResult;
import org.neo4j.gds.leiden.LeidenMutateConfig;
import org.neo4j.gds.leiden.LeidenResult;
import org.neo4j.gds.wcc.WccMutateConfig;

import java.util.Optional;

import static org.neo4j.gds.applications.algorithms.metadata.LabelForProgressTracking.ApproximateMaximumKCut;
import static org.neo4j.gds.applications.algorithms.metadata.LabelForProgressTracking.K1Coloring;
import static org.neo4j.gds.applications.algorithms.metadata.LabelForProgressTracking.KCore;
import static org.neo4j.gds.applications.algorithms.metadata.LabelForProgressTracking.KMeans;
import static org.neo4j.gds.applications.algorithms.metadata.LabelForProgressTracking.LabelPropagation;
import static org.neo4j.gds.applications.algorithms.metadata.LabelForProgressTracking.Leiden;
import static org.neo4j.gds.applications.algorithms.metadata.LabelForProgressTracking.WCC;

public class CommunityAlgorithmsMutateModeBusinessFacade {
    private final CommunityAlgorithmsEstimationModeBusinessFacade estimation;
    private final CommunityAlgorithms algorithms;
    private final AlgorithmProcessingTemplate template;
    private final MutateNodeProperty mutateNodeProperty;

    public CommunityAlgorithmsMutateModeBusinessFacade(
        CommunityAlgorithmsEstimationModeBusinessFacade estimation,
        CommunityAlgorithms algorithms,
        AlgorithmProcessingTemplate template,
        MutateNodeProperty mutateNodeProperty
    ) {
        this.estimation = estimation;
        this.algorithms = algorithms;
        this.template = template;
        this.mutateNodeProperty = mutateNodeProperty;
    }

    public <RESULT> RESULT approximateMaximumKCut(
        GraphName graphName,
        ApproxMaxKCutMutateConfig configuration,
        ResultBuilder<ApproxMaxKCutMutateConfig, ApproxMaxKCutResult, RESULT, NodePropertiesWritten> resultBuilder
    ) {
        var mutateStep = new ApproxMaxKCutMutateStep(mutateNodeProperty, configuration);

        return template.processAlgorithm(
            graphName,
            configuration,
            ApproximateMaximumKCut,
            () -> estimation.approximateMaximumKCut(configuration),
            graph -> algorithms.approximateMaximumKCut(graph, configuration),
            Optional.of(mutateStep),
            resultBuilder
        );
    }

    public <RESULT> RESULT k1Coloring(
        GraphName graphName,
        K1ColoringMutateConfig configuration,
        ResultBuilder<K1ColoringMutateConfig, K1ColoringResult, RESULT, Void> resultBuilder
    ) {
        var mutateStep = new K1ColoringMutateStep(mutateNodeProperty, configuration);

        return template.processAlgorithm(
            graphName,
            configuration,
            K1Coloring,
            estimation::k1Coloring,
            graph -> algorithms.k1Coloring(graph, configuration),
            Optional.of(mutateStep),
            resultBuilder
        );
    }

    public <RESULT> RESULT kCore(
        GraphName graphName,
        KCoreDecompositionMutateConfig configuration,
        ResultBuilder<KCoreDecompositionMutateConfig, KCoreDecompositionResult, RESULT, NodePropertiesWritten> resultBuilder
    ) {
        var mutateStep = new KCoreMutateStep(mutateNodeProperty, configuration);

        return template.processAlgorithm(
            graphName,
            configuration,
            KCore,
            estimation::kCore,
            graph -> algorithms.kCore(graph, configuration),
            Optional.of(mutateStep),
            resultBuilder
        );
    }

    public <RESULT> RESULT kMeans(
        GraphName graphName,
        KmeansMutateConfig configuration,
        ResultBuilder<KmeansMutateConfig, KmeansResult, RESULT, NodePropertiesWritten> resultBuilder
    ) {
        var mutateStep = new KMeansMutateStep(mutateNodeProperty, configuration);

        return template.processAlgorithm(
            graphName,
            configuration,
            KMeans,
            () -> estimation.kMeans(configuration),
            graph -> algorithms.kMeans(graph, configuration),
            Optional.of(mutateStep),
            resultBuilder
        );
    }

    public <RESULT> RESULT labelPropagation(
        GraphName graphName,
        LabelPropagationMutateConfig configuration,
        ResultBuilder<LabelPropagationMutateConfig, LabelPropagationResult, RESULT, NodePropertiesWritten> resultBuilder
    ) {
        var mutateStep = new LabelPropagationMutateStep(mutateNodeProperty, configuration);

        return template.processAlgorithm(
            graphName,
            configuration,
            LabelPropagation,
            estimation::labelPropagation,
            graph -> algorithms.labelPropagation(graph, configuration),
            Optional.of(mutateStep),
            resultBuilder
        );
    }

    public <RESULT> RESULT leiden(
        GraphName graphName,
        LeidenMutateConfig configuration,
        ResultBuilder<LeidenMutateConfig, LeidenResult, RESULT, Pair<NodePropertiesWritten, NodePropertyValues>> resultBuilder
    ) {
        var mutateStep = new LeidenMutateStep(mutateNodeProperty, configuration);

        return template.processAlgorithm(
            graphName,
            configuration,
            Leiden,
            () -> estimation.leiden(configuration),
            graph -> algorithms.leiden(graph, configuration),
            Optional.of(mutateStep),
            resultBuilder
        );
    }

    public <RESULT> RESULT wcc(
        GraphName graphName,
        WccMutateConfig configuration,
        ResultBuilder<WccMutateConfig, DisjointSetStruct, RESULT, NodePropertiesWritten> resultBuilder
    ) {
        var mutateStep = new WccMutateStep(mutateNodeProperty, configuration);

        return template.processAlgorithm(
            graphName,
            configuration,
            WCC,
            () -> estimation.wcc(configuration),
            graph -> algorithms.wcc(graph, configuration),
            Optional.of(mutateStep),
            resultBuilder
        );
    }
}
