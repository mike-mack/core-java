/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.test;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.base.UserId;
import org.spine3.client.CommandFactory;
import org.spine3.time.ZoneOffset;

/**
 * The command factory, which allows generating commands as if the were
 * created at the specified moments in time.
 *
 * @author Alexaner Yevsyukov
 */
public class TestCommandFactory extends CommandFactory {

    public static TestCommandFactory newInstance(String actor, ZoneOffset zoneOffset) {
        return new TestCommandFactory(UserId.newBuilder().setValue(actor).build(), zoneOffset);
    }

    protected TestCommandFactory(UserId actor, ZoneOffset zoneOffset) {
        super(actor, zoneOffset);
    }

    /**
     * Creates new command with the passed timestamp.
     */
    public Command create(Message message, Timestamp timestamp) {
        final Command.Builder command = create(message).toBuilder();
        final CommandContext.Builder context = command.getContext().toBuilder();

        return command.setContext(context.setTimestamp(timestamp).build()).build();
    }
}