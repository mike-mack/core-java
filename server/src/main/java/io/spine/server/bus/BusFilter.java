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

package io.spine.server.bus;

import com.google.common.base.Optional;
import io.spine.base.IsSent;
import io.spine.envelope.MessageEnvelope;

/**
 * @author Dmytro Dashenkov
 */
public interface BusFilter<E extends MessageEnvelope<?>> {

    /**
     * Accepts or rejects a passed command.
     *
     * <p>A filter can:
     * <ul>
     *     <li>Accept the message (by returning {@code Optional.absent()};
     *     <li>Reject the message with {@link io.spine.base.Error} status e.g. if it fails to pass
     *         the validation;
     *     <li>Reject the message with {@code OK} status. For example, a scheduled command may not
     *         pass a filter.
     * </ul>
     *
     * @param envelope      the envelope with the message to filter
     * @return {@code Optional.absent()} if the message passes the filter,
     *         {@linkplain IsSent posting result} with either status otherwise
     */
    Optional<IsSent> accept(E envelope);
}
