/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.server.migration.mirror.given;

import io.spine.base.EntityState;
import io.spine.server.migration.mirror.MirrorStorage;
import io.spine.system.server.Mirror;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MirrorMigrationTestEnv {

    private MirrorMigrationTestEnv() {
    }

    /**
     * Fills a storage with {@linkplain Mirror} projections.
     *
     * <p>The returned storage is pre-filled with three types of entities'. For each type, number
     * of records to generate is specified.
     */
    public static void fill(MirrorStorage mirrorStorage,
                            Supplier<EntityState<?>> stateSupplier,
                            int numberOfStates) {

        var mirrors = IntStream.rangeClosed(1, numberOfStates)
                               .mapToObj(i -> stateSupplier)
                               .map(MirrorMigrationTestEnv::mirror)
                               .collect(Collectors.toList());
        mirrorStorage.writeBatch(mirrors);
    }

    private static Mirror mirror(Supplier<EntityState<?>> stateSupplier) {
        var state = stateSupplier.get();
        var mirror = MirrorMappingTestEnv.mirror(state);
        return mirror;
    }
}
