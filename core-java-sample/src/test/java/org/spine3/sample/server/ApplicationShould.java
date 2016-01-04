/*
 * Copyright 2015, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine3.sample.server;

import org.junit.Test;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.filesystem.FileSystemStorageFactory;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import java.io.IOException;

@SuppressWarnings("InstanceMethodNamingConvention")
public class ApplicationShould {

    private static final StorageFactory FS_STORAGE_FACTORY = FileSystemStorageFactory.newInstance(ApplicationShould.class);

    @Test
    public void execute_on_in_memory_storage() {
        final Application app = new Application(InMemoryStorageFactory.getInstance());
        app.execute();
    }

    @Test
    public void execute_on_file_system_storage() throws IOException {
        try (final Application app = new Application(FS_STORAGE_FACTORY))  {
            app.execute();
        }
    }
}