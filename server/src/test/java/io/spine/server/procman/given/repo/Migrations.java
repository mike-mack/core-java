/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.server.procman.given.repo;

import io.spine.base.EntityState;
import io.spine.protobuf.ValidatingBuilder;
import io.spine.server.procman.ProcessManager;
import io.spine.server.procman.ProcessManagerMigration;

public final class Migrations {

    private Migrations() {
    }

    public static final class MarkArchived<I,
                                           P extends ProcessManager<I, S, B>,
                                           S extends EntityState,
                                           B extends ValidatingBuilder<S>>
            extends ProcessManagerMigration<I, S, B, P> {

        @Override
        public S apply(S s) {
            markArchived();
            return s;
        }
    }

    public static final class MarkDeleted<I,
                                          P extends ProcessManager<I, S, B>,
                                          S extends EntityState,
                                          B extends ValidatingBuilder<S>>
            extends ProcessManagerMigration<I, S, B, P> {

        @Override
        public S apply(S s) {
            markDeleted();
            return s;
        }
    }

    public static final class RemoveFromStorage<I,
                                                P extends ProcessManager<I, S, B>,
                                                S extends EntityState,
                                                B extends ValidatingBuilder<S>>
            extends ProcessManagerMigration<I, S, B, P> {

        @Override
        public S apply(S s) {
            removeFromStorage();
            return s;
        }
    }
}
