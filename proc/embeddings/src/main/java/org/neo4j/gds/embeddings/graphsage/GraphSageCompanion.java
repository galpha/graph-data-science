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
package org.neo4j.gds.embeddings.graphsage;

import org.neo4j.gds.api.properties.nodes.DoubleArrayNodePropertyValues;
import org.neo4j.gds.api.properties.nodes.EmptyDoubleArrayNodePropertyValues;
import org.neo4j.gds.core.model.ModelCatalog;
import org.neo4j.gds.embeddings.graphsage.algo.GraphSageBaseConfig;
import org.neo4j.gds.embeddings.graphsage.algo.GraphSageResult;
import org.neo4j.gds.executor.validation.ValidationConfiguration;

import java.util.Optional;

public final class GraphSageCompanion {

    static final String GRAPHSAGE_DESCRIPTION = "The GraphSage algorithm inductively computes embeddings for nodes based on a their features and neighborhoods.";

    private GraphSageCompanion() {}

    static DoubleArrayNodePropertyValues nodePropertyValues(Optional<GraphSageResult> graphSageResult) {
        return graphSageResult
            .map(GraphSageResult::embeddings)
            .map(embeddings -> (DoubleArrayNodePropertyValues) new EmbeddingNodePropertyValues(embeddings))
            .orElse(EmptyDoubleArrayNodePropertyValues.INSTANCE);
    }

    static <CONFIG extends GraphSageBaseConfig> ValidationConfiguration<CONFIG> getValidationConfig(ModelCatalog catalog) {
        return new GraphSageConfigurationValidation<>(catalog);
    }

}
