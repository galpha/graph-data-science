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
package org.neo4j.gds.storageengine;

import org.neo4j.graphalgo.api.GraphStore;
import org.neo4j.graphalgo.api.nodeproperties.ValueType;
import org.neo4j.token.TokenHolders;
import org.neo4j.token.api.NamedToken;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.ValueGroup;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

class InMemoryNodePropertyCursor extends InMemoryPropertyCursor.DelegatePropertyCursor {

    private String currentNodePropertyKey = null;

    private final Set<Integer> seenNodeReferences;

    public InMemoryNodePropertyCursor(GraphStore graphStore, TokenHolders tokenHolders) {
        super(NO_ID, graphStore, tokenHolders);
        this.seenNodeReferences = new HashSet<>();
    }

    @Override
    public void initNodeProperties(long reference) {
        setId(reference);
    }

    @Override
    public void initNodeProperties(long reference, long ownerReference) {

    }

    @Override
    public void initRelationshipProperties(long reference, long ownerReference) {

    }

    @Override
    public void initRelationshipProperties(long reference) {

    }

    @Override
    public int propertyKey() {
        return tokenHolders.propertyKeyTokens().getIdByName(currentNodePropertyKey);
    }

    @Override
    public ValueGroup propertyType() {
        // TODO: this assumes we are always a node property cursor
        ValueType valueType = graphStore
            .schema()
            .nodeSchema()
            .filter(graphStore.nodeLabels())
            .properties()
            .values()
            .stream()
            .map(map -> map.get(currentNodePropertyKey))
            .findFirst()
            .get()
            .valueType();

        return ValueGroup.valueOf(valueType.cypherName());
    }

    @Override
    public Value propertyValue() {
        if (currentNodePropertyKey != null) {
            return graphStore.nodePropertyValues(currentNodePropertyKey).value(getId());
        } else {
            throw new IllegalStateException("Property cursor is initialized as node and relationship cursor, maybe you forgot to `reset()`?");
        }
    }

    @Override
    public boolean next() {
        if (getId() != NO_ID) {
            Optional<NamedToken> maybeNextEntry = StreamSupport.stream(tokenHolders
                .propertyKeyTokens()
                .getAllTokens()
                .spliterator(), false)
                .filter(tokenHolder -> !seenNodeReferences.contains(tokenHolder.id()))
                .findFirst();

            if (maybeNextEntry.isPresent()) {
                currentNodePropertyKey = maybeNextEntry.get().name();
                seenNodeReferences.add(maybeNextEntry.get().id());
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public void reset() {
        clear();
        this.setId(NO_ID);
        this.currentNodePropertyKey = null;
        this.seenNodeReferences.clear();
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public void setForceLoad() {

    }

    @Override
    public void close() {

    }
}
