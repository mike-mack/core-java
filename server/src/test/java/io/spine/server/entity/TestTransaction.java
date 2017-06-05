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
package io.spine.server.entity;

import com.google.protobuf.Message;
import io.spine.base.EventContext;
import io.spine.base.Version;

import java.lang.reflect.InvocationTargetException;

/**
 * A utility class providing various test-only methods, which in production mode are allowed
 * with the transactions only.
 *
 * @author Alex Tymchenko
 */
public class TestTransaction {

    private TestTransaction() {
        // Prevent instantiation.
    }

    /**
     * A hack allowing to inject state and version into an {@code EventPlayingEntity} instance
     * by creating and committing a fake transaction.
     *
     * <p>To be used in tests only.
     */
    public static void injectState(EventPlayingEntity entity, Message state, Version version) {

        @SuppressWarnings("unchecked")
        final Transaction tx = new Transaction(entity, state, version) {

            @Override
            protected void dispatch(EventPlayingEntity entity,
                                    Message eventMessage,
                                    EventContext context) throws InvocationTargetException {
                // do nothing.
            }
        };

        tx.commit();
    }
}