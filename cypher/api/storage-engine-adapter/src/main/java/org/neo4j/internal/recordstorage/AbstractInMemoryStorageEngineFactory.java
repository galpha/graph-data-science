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
package org.neo4j.internal.recordstorage;

import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.context.CursorContext;
import org.neo4j.kernel.impl.store.MetaDataStore;
import org.neo4j.storageengine.api.StorageEngineFactory;
import org.neo4j.storageengine.api.StorageFilesState;
import org.neo4j.storageengine.migration.SchemaRuleMigrationAccess;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.neo4j.io.layout.recordstorage.RecordDatabaseLayout.convert;

public abstract class AbstractInMemoryStorageEngineFactory implements StorageEngineFactory {

    @Override
    public List<Path> listStorageFiles(
        FileSystemAbstraction fileSystem, DatabaseLayout databaseLayout
    ) {
        return Collections.emptyList();
    }

    @Override
    public boolean storageExists(
        FileSystemAbstraction fileSystem, DatabaseLayout databaseLayout, PageCache pageCache
    ) {
        return false;
    }

    protected abstract AbstractInMemoryMetaDataProvider metadataProvider();

    protected abstract SchemaRuleMigrationAccess schemaRuleMigrationAccess();

    @Override
    public Optional<UUID> databaseIdUuid(
        FileSystemAbstraction fs, DatabaseLayout databaseLayout, PageCache pageCache, CursorContext cursorContext
    ) {
        var fieldAccess = MetaDataStore.getFieldAccess(
            pageCache,
            convert(databaseLayout).metadataStore(),
            databaseLayout.getDatabaseName(),
            cursorContext
        );

        try {
            return fieldAccess.readDatabaseUUID();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public StorageFilesState checkStoreFileState(
        FileSystemAbstraction fs, DatabaseLayout databaseLayout, PageCache pageCache
    ) {
        return StorageFilesState.recoveredState();
    }
}
