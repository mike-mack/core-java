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

package org.spine3.server.command;

import com.google.protobuf.Message;
import org.spine3.base.CommandContext;
import org.spine3.base.Identifiers;
import org.spine3.server.entity.Entity;
import org.spine3.server.entity.GetIdByFieldIndex;

/**
 * Obtains a command target {@link Entity} ID based on a command {@link Message} and context.
 *
 * <p>An entity ID must be the first field in command messages (in Protobuf definition).
 * Its name must end with the {@link Identifiers#ID_PROPERTY_SUFFIX}.
 *
 * @param <I> the type of target entity IDs
 * @param <M> the type of command messages to get IDs from
 * @author Alexander Litus
 */
public class GetTargetIdFromCommand<I, M extends Message> extends GetIdByFieldIndex<I, M, CommandContext> {

    public static final int ID_FIELD_INDEX = 0;

    private GetTargetIdFromCommand() {
        super(ID_FIELD_INDEX);
    }

    /**
     * Creates a new ID function instance.
     */
    public static<I, M extends Message> GetTargetIdFromCommand<I, M> newInstance() {
        return new GetTargetIdFromCommand<>();
    }
}