/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jackrabbit.oak.index;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.oak.plugins.index.lucene.ExtractedTextCache;
import org.apache.jackrabbit.oak.plugins.index.lucene.IndexCopier;
import org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexEditorProvider;
import org.apache.jackrabbit.oak.plugins.index.lucene.directory.DirectoryFactory;
import org.apache.jackrabbit.oak.spi.blob.GarbageCollectableBlobStore;

class LuceneIndexHelper implements Closeable {
    private final IndexHelper indexHelper;
    private IndexCopier indexCopier;
    //TODO Set pre extracted text provider
    private final ExtractedTextCache textCache =
            new ExtractedTextCache(FileUtils.ONE_MB * 5, TimeUnit.HOURS.toSeconds(5));
    private DirectoryFactory directoryFactory;

    LuceneIndexHelper(IndexHelper indexHelper) {
        this.indexHelper = indexHelper;
    }

    public LuceneIndexEditorProvider createEditorProvider() throws IOException {
        LuceneIndexEditorProvider editor =  new LuceneIndexEditorProvider(
                getIndexCopier(),
                textCache,
                null,
                indexHelper.getMountInfoProvider()
        );

        if (indexHelper.getBlobStore() instanceof GarbageCollectableBlobStore) {
            editor.setBlobStore((GarbageCollectableBlobStore) indexHelper.getBlobStore());
        }

        if (directoryFactory != null) {
            editor.setDirectoryFactory(directoryFactory);
        }

        return editor;
    }

    public void setDirectoryFactory(DirectoryFactory directoryFactory) {
        this.directoryFactory = directoryFactory;
    }

    private IndexCopier getIndexCopier() throws IOException {
        if (indexCopier == null) {
            File indexWorkDir = new File(indexHelper.getWorkDir(), "indexWorkDir");
            indexCopier = new IndexCopier(indexHelper.getExecutor(), indexWorkDir, true);
        }
        return indexCopier;
    }

    @Override
    public void close() throws IOException {
        if (indexCopier != null) {
            indexCopier.close();
        }
    }
}
