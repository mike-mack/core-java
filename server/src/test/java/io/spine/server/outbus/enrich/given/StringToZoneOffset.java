/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.server.outbus.enrich.given;

import com.google.common.base.Function;
import io.spine.time.ZoneId;
import io.spine.time.ZoneOffset;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Naïve implementation of string to ZoneOffset conversion for the enrichment tests purposes.
 *
 * <p>Production code should perform validation.
 *
 * @author Alexander Yevsyukov
 */
public class StringToZoneOffset implements Function<String, ZoneOffset> {
    @Override
    public @Nullable ZoneOffset apply(@Nullable String input) {
        return input == null
               ? ZoneOffset.getDefaultInstance()
               : ZoneOffset.newBuilder()
                           .setId(ZoneId.newBuilder()
                                        .setValue(input))
                           .build();
    }
}