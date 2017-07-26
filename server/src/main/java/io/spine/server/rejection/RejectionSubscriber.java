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
package io.spine.server.rejection;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import io.spine.core.Command;
import io.spine.core.CommandContext;
import io.spine.core.RejectionClass;
import io.spine.core.RejectionEnvelope;
import io.spine.server.reflect.RejectionSubscriberMethod;
import io.spine.server.tenant.CommandOperation;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Abstract base for objects receiving rejections from {@link RejectionBus}.
 *
 * @author Alex Tymchenko
 * @author Alexander Yevsyukov
 * @see RejectionBus#register(io.spine.server.bus.MessageDispatcher)
 */
public class RejectionSubscriber implements RejectionDispatcher<String> {

    /**
     * Cached set of the rejection classes this subscriber is subscribed to.
     */
    @Nullable
    private Set<RejectionClass> rejectionClasses;

    @Override
    public Set<String> dispatch(final RejectionEnvelope envelope) {
        final Command originCommand = envelope.getOuterObject()
                                              .getContext()
                                              .getCommand();
        final CommandOperation op = new CommandOperation(originCommand) {

            @Override
            public void run() {
                handle(envelope.getMessage(),
                       envelope.getCommandMessage(),
                       envelope.getCommandContext());
            }
        };
        op.execute();
        return Identity.of(this);
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // as we return an immutable collection.
    public Set<RejectionClass> getMessageClasses() {
        if (rejectionClasses == null) {
            rejectionClasses = ImmutableSet.copyOf(
                    RejectionSubscriberMethod.getRejectionClasses(getClass()));
        }
        return rejectionClasses;
    }

    public void handle(Message rejectionMessage, Message commandMessage, CommandContext context) {
        RejectionSubscriberMethod.invokeFor(this, rejectionMessage, commandMessage, context);
    }
}