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
package org.neo4j.gds.leiden;

import org.neo4j.gds.NullComputationResultConsumer;
import org.neo4j.gds.executor.AlgorithmSpec;
import org.neo4j.gds.executor.ComputationResultConsumer;
import org.neo4j.gds.executor.ExecutionContext;
import org.neo4j.gds.executor.ExecutionMode;
import org.neo4j.gds.executor.GdsCallable;
import org.neo4j.gds.procedures.algorithms.community.LeidenMutateResult;
import org.neo4j.gds.procedures.algorithms.configuration.NewConfigFunction;

import java.util.stream.Stream;

import static org.neo4j.gds.leiden.Constants.LEIDEN_DESCRIPTION;


@GdsCallable(name = "gds.leiden.mutate", aliases = {"gds.beta.leiden.mutate"}, description = LEIDEN_DESCRIPTION, executionMode = ExecutionMode.MUTATE_NODE_PROPERTY)
public class LeidenMutateSpec implements AlgorithmSpec<Leiden, LeidenResult, LeidenMutateConfig, Stream<LeidenMutateResult>, LeidenAlgorithmFactory<LeidenMutateConfig>> {
    @Override
    public String name() {
        return "LeidenMutate";
    }

    @Override
    public LeidenAlgorithmFactory<LeidenMutateConfig> algorithmFactory(ExecutionContext executionContext) {
        return new LeidenAlgorithmFactory<>();
    }

    @Override
    public NewConfigFunction<LeidenMutateConfig> newConfigFunction() {
        return (__, config) -> LeidenMutateConfig.of(config);
    }

    @Override
    public ComputationResultConsumer<Leiden, LeidenResult, LeidenMutateConfig, Stream<LeidenMutateResult>> computationResultConsumer() {
        return new NullComputationResultConsumer<>();
    }
}
