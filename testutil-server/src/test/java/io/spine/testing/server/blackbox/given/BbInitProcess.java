/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.testing.server.blackbox.given;

import io.spine.server.command.Assign;
import io.spine.server.model.Nothing;
import io.spine.server.procman.ProcessManager;
import io.spine.testing.server.blackbox.BbInit;
import io.spine.testing.server.blackbox.BbInitVBuilder;
import io.spine.testing.server.blackbox.BbProjectId;
import io.spine.testing.server.blackbox.command.BbInitProject;

/**
 * Test environment process manager for testing
 * {@link io.spine.testing.server.entity.EntitySubject}.
 */
public class BbInitProcess extends ProcessManager<BbProjectId, BbInit, BbInitVBuilder> {

    protected BbInitProcess(BbProjectId id) {
        super(id);
    }

    @Assign
    Nothing on(BbInitProject cmd) {
        builder().setId(cmd.getProjectId())
                 .setInitialized(true);
        setDeleted(true);
        return nothing();
    }
}
