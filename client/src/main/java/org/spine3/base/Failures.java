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

package org.spine3.base;

import com.google.protobuf.Message;
import org.spine3.protobuf.AnyPacker;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for working with failures.
 *
 * @author Alexander Yevsyukov
 */
public class Failures {

    private Failures() {
        // Prevent instantiation of this utility class.
    }

    /** Generates a new random UUID-based {@code FailureId}. */
    public static FailureId generateId() {
        final String value = Identifiers.newUuid();
        return FailureId.newBuilder()
                        .setUuid(value)
                        .build();
    }

    /**
     * Extracts the message from the passed {@code Failure} instance.
     *
     * @param failure a failure to extract a message from
     * @param <M>     a type of the failure message
     * @return an unpacked message
     */
    public static <M extends Message> M getMessage(Failure failure) {
        checkNotNull(failure);
        final M result = AnyPacker.unpack(failure.getMessage());
        return result;
    }
}
