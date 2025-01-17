/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.server.log;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.LogSite;
import io.spine.server.model.Receptor;

import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A log for receptor methods.
 *
 * <p>The log is set up with a {@link Receptor} from which it should be accessed. By default,
 * the log will include the given method as the logging {@linkplain LogSite site}.
 */
public final class ReceptorLog {

    private final FluentLogger logger;
    private final LogSite logSite;

    public ReceptorLog(FluentLogger logger, Receptor<?, ?, ?, ?> method) {
        this.logger = checkNotNull(logger);
        checkNotNull(method);
        this.logSite = new ReceptorSite(method);
    }

    /**
     * Returns a fluent logging API appropriate for the specified log level.
     *
     * <p>By default, the log produced by this API will include the name of the handler method, as
     * well as its parameter types.
     */
    public FluentLogger.Api at(Level level) {
        return logger.at(level)
                     .withInjectedLogSite(logSite);
    }
}
