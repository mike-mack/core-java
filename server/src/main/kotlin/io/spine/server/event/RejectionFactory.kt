/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.server.event

import com.google.common.base.Throwables
import io.spine.base.RejectionThrowable
import io.spine.core.Command
import io.spine.core.Event
import io.spine.core.RejectionEventContext
import io.spine.server.type.RejectionEnvelope.PRODUCER_UNKNOWN

/**
 * Creates a rejection event for the passed command and the throwable.
 *
 * The cause of the throwable must implement [RejectionThrowable].
 *
 * @throws IllegalArgumentException
 *          if the cause of the passed `throwable` does not implement `RejectionThrowable`
 */
fun reject(command: Command, throwable: Throwable): Event {
    val rt = unwrap(throwable)
    val factory = RejectionFactory(command, rt)
    return factory.createRejection()
}

/**
 * Extracts a `RejectionThrowable` from the passed instance.
 *
 * @throws IllegalArgumentException
 *          if the cause does not implement `RejectionThrowable`
 */
private fun unwrap(throwable: Throwable): RejectionThrowable {
    if (throwable is RejectionThrowable) {
        return throwable
    }
    val cause = Throwables.getRootCause(throwable)
    if (cause !is RejectionThrowable) {
        throw IllegalArgumentException(
            "The cause of `${throwable}` has the type `${cause.javaClass}`." +
                    " Expected: `${RejectionThrowable::class}`."
        )
    }
    return cause
}

/**
 * A factory for producing rejection events.
 */
private class RejectionFactory(val command: Command, val throwable: RejectionThrowable) :
    EventFactoryBase(
        EventOrigin.from(command.asMessageOrigin()),
        throwable.producerId().orElse(PRODUCER_UNKNOWN)
    ) {

    /**
     * Creates a factory instance for creating a rejection for the passed command and
     * the throwable.
     *
     * The cause of the throwable must implement [RejectionThrowable].
     *
     * @throws IllegalArgumentException
     *          if the cause of the passed `throwable` does not implement `RejectionThrowable`
     */
    private constructor(command: Command, throwable: Throwable) : this(command, unwrap(throwable))

    /**
     * Creates a rejection event which does not have version information.
     */
    fun createRejection(): Event {
        val msg = throwable.messageThrown()
        val context = rejectionContext()
        val outerContext = newContext(null)
            .setRejection(context)
            .vBuild()
        return assemble(msg, outerContext)
    }

    /**
     * Constructs a new [RejectionEventContext].
     */
    private fun rejectionContext(): RejectionEventContext {
        val st = Throwables.getStackTraceAsString(throwable)
        return RejectionEventContext.newBuilder()
            .setCommand(command)
            .setStacktrace(st)
            .vBuild()
    }
}
