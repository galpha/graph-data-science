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
package org.neo4j.gds.compat._5x;

import org.neo4j.gds.compat.StoreScan;
import org.neo4j.internal.kernel.api.Cursor;
import org.neo4j.internal.kernel.api.PartitionedScan;
import org.neo4j.kernel.api.KernelTransaction;

public final class PartitionedStoreScan<C extends Cursor> implements StoreScan<C> {
    private final PartitionedScan<C> scan;

    public PartitionedStoreScan(PartitionedScan<C> scan) {
        this.scan = scan;
    }

    public static int getNumberOfPartitions(long nodeCount, int batchSize) {
        int numberOfPartitions;
        if (nodeCount > 0) {
            // ceil div to try to get enough partitions so a single one does
            // not include more nodes than batchSize
            long partitions = ((nodeCount - 1) / batchSize) + 1;

            // value must be positive
            if (partitions < 1) {
                partitions = 1;
            }

            numberOfPartitions = (int) Long.min(Integer.MAX_VALUE, partitions);
        } else {
            // we have no partitions to scan, but the value must still  be positive
            numberOfPartitions = 1;
        }
        return numberOfPartitions;
    }

    @Override
    public boolean reserveBatch(C cursor, KernelTransaction ktx) {
        //noinspection deprecation, we are doing our own thread management and know that this is a thread-safe usage
        return scan.reservePartition(cursor, ktx.cursorContext(), ktx.securityContext().mode());
    }
}
