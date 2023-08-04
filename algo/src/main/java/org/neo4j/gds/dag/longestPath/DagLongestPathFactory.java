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
package org.neo4j.gds.dag.longestPath;

import org.neo4j.gds.GraphAlgorithmFactory;
import org.neo4j.gds.api.Graph;
import org.neo4j.gds.core.CypherMapWrapper;
import org.neo4j.gds.core.utils.progress.tasks.ProgressTracker;
import org.neo4j.gds.core.utils.progress.tasks.Task;
import org.neo4j.gds.core.utils.progress.tasks.Tasks;
import org.neo4j.gds.dag.topologicalsort.TopologicalSortFactory;
import org.neo4j.gds.dag.topologicalsort.TopologicalSortStreamConfig;

import java.util.List;

public class DagLongestPathFactory<CONFIG extends DagLongestPathBaseConfig> extends GraphAlgorithmFactory<DagLongestPath, CONFIG> {
    @Override
    public DagLongestPath build(Graph graph, DagLongestPathBaseConfig configuration, ProgressTracker progressTracker) {
        var topologicalSortConfigMap =
            CypherMapWrapper
                .create(configuration.toMap())
                .withBoolean("computeMaxDistanceFromSource", true);

        var topologicalSort = new TopologicalSortFactory().build(
            graph,
            TopologicalSortStreamConfig.of(topologicalSortConfigMap),
            progressTracker
        );

        return new DagLongestPath(
            progressTracker,
            topologicalSort
        );
    }

    @Override
    public String taskName() {
        // todo: longest path uses topological sort tasks until we have better abstraction
        return "TopologicalSort";
    }

    @Override
    public Task progressTask(Graph graph, CONFIG config) {
        var initializationTask = Tasks.leaf("Initialization", graph.nodeCount());
        var traversalTask = Tasks.leaf("Traversal", graph.nodeCount());

        return Tasks.task("TopologicalSort", List.of(initializationTask, traversalTask));
    }
}
