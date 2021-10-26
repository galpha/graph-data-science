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
package org.neo4j.gds.core;

import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.gds.api.GraphLoaderContext;
import org.neo4j.gds.core.loading.IdMapBuilder;
import org.neo4j.gds.core.loading.InternalHugeIdMappingBuilder;
import org.neo4j.gds.core.loading.InternalIdMappingBuilderFactory;
import org.neo4j.gds.core.loading.NodeMappingBuilder;

@ServiceProvider
public class OpenGdsIdMapBehavior implements IdMapBehavior {

    @Override
    public InternalIdMappingBuilderFactory<InternalHugeIdMappingBuilder, InternalHugeIdMappingBuilder.BulkAdder> idMappingBuilderFactory(GraphLoaderContext loadingContext) {
        return dimensions -> InternalHugeIdMappingBuilder.of(dimensions.nodeCount(), loadingContext.allocationTracker());
    }

    @Override
    public NodeMappingBuilder<InternalHugeIdMappingBuilder> nodeMappingBuilder() {
        return (idMapBuilder, labelInformationBuilder, highestNeoId, concurrency, checkDuplicateIds, allocationTracker) -> {
            if (checkDuplicateIds) {
                return IdMapBuilder.buildChecked(
                    idMapBuilder,
                    labelInformationBuilder,
                    highestNeoId,
                    concurrency,
                    allocationTracker
                );
            } else {
                return IdMapBuilder.build(
                    idMapBuilder,
                    labelInformationBuilder,
                    highestNeoId,
                    concurrency,
                    allocationTracker
                );
            }
        };
    }

    public int priority() {
        return 0;
    }
}
