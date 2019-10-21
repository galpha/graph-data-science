/*
 * Copyright (c) 2017-2019 "Neo4j,"
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

package org.neo4j.graphalgo.impl.jaccard;

import java.util.Objects;

public class SimilarityResult {
    long node1;
    long node2;
    double similarity;

    SimilarityResult(long node1, long node2, double similarity) {
        this.node1 = node1;
        this.node2 = node2;
        this.similarity = similarity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimilarityResult that = (SimilarityResult) o;
        return node1 == that.node1 &&
               node2 == that.node2 &&
               Double.compare(that.similarity, similarity) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1, node2, similarity);
    }

    @Override
    public String toString() {
        return "SimilarityResult{" +
               "node1=" + node1 +
               ", node2=" + node2 +
               ", similarity=" + similarity +
               '}';
    }
}
