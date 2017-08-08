/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.server.rejection.given;

import com.google.protobuf.Empty;
import io.spine.core.CommandContext;
import io.spine.core.React;
import io.spine.core.Rejection;
import io.spine.test.rejection.ProjectRejections;

import static org.junit.Assert.fail;

/**
 * The reactor which throws exception from the reactor method.
 *
 * @author Alexander Yevsyukov
 **/
public class FaultyReactor extends VerifiableReactor {

    @SuppressWarnings("unused") // It's fine for a faulty reactor.
    @React
    public Empty on(ProjectRejections.InvalidProjectName rejection, CommandContext context) {
        triggerCall();
        throw new UnsupportedOperationException(
                "Faulty reactor should have failed: " +
                        FaultyReactor.class.getSimpleName());
    }

    @Override
    public void verifyGot(Rejection ignored) {
        fail("FaultyReactor");
    }
}
