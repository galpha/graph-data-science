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
package org.neo4j.gds;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.gds.api.GraphStore;
import org.neo4j.gds.api.PropertyState;
import org.neo4j.gds.api.nodeproperties.LongNodeProperties;
import org.neo4j.gds.core.utils.mem.AllocationTracker;
import org.neo4j.gds.extension.GdlExtension;
import org.neo4j.gds.extension.GdlGraph;
import org.neo4j.gds.extension.Inject;
import org.neo4j.gds.test.ImmutableTestMutateConfig;
import org.neo4j.gds.test.TestAlgoResultBuilder;
import org.neo4j.gds.test.TestAlgorithm;
import org.neo4j.gds.test.TestMutateConfig;
import org.neo4j.gds.test.TestProc;

import static org.assertj.core.api.Assertions.assertThat;

@GdlExtension
class MutatePropertyComputationResultConsumerTest {

    @GdlGraph
    static final String DB_CYPHER = "(a), (b)";

    @Inject
    GraphStore graphStore;

    MutatePropertyComputationResultConsumer mutateResultConsumer;

    @BeforeEach
    void setup() {
        mutateResultConsumer = new MutatePropertyComputationResultConsumer<TestAlgorithm, TestAlgorithm, TestMutateConfig, TestProc.TestResult>(
            (computationResult, resultProperty, allocationTracker) -> new TestNodeProperties(),
            computationResult -> new TestAlgoResultBuilder(),
            new TestLog(),
            AllocationTracker.empty()
        );
    }

    @Test
    void shouldMutateNodeProperties() {
        var computationResult = ImmutableComputationResult
            .builder()
            .algorithm(null)
            .config(ImmutableTestMutateConfig.builder().mutateProperty("mutated").build())
            .result(null)
            .graph(graphStore.getUnion())
            .graphStore(graphStore)
            .createMillis(0)
            .computeMillis(0)
            .build();
        mutateResultConsumer.consume(computationResult);

        assertThat(graphStore.hasNodeProperty(graphStore.nodeLabels(), "mutated")).isTrue();
        var mutatedNodeProperty = graphStore.nodeProperty("mutated");

        assertThat(mutatedNodeProperty.propertyState()).isEqualTo(PropertyState.TRANSIENT);

        var mutatedNodePropertyValues = mutatedNodeProperty.values();
        assertThat(mutatedNodePropertyValues.longValue(0)).isEqualTo(0);
        assertThat(mutatedNodePropertyValues.longValue(1)).isEqualTo(1);
    }

    class TestNodeProperties implements LongNodeProperties {

        @Override
        public long size() {
            return graphStore.nodeCount();
        }

        @Override
        public long longValue(long nodeId) {
            return nodeId;
        }
    }

}