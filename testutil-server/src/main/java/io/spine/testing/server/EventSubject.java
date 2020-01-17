/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.testing.server;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import io.spine.base.EventMessage;
import io.spine.core.Event;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import static com.google.common.truth.Truth.assertAbout;

/**
 * Checks for events generated by a Bounded Context under the test.
 */
public final class EventSubject extends EmittedMessageSubject<EventSubject, Event, EventMessage> {

    private EventSubject(FailureMetadata metadata, @NullableDecl Iterable<Event> actual) {
        super(metadata, actual);
    }

    /** Provides the factory for creating {@code EventSubject}. */
    public static Subject.Factory<EventSubject, Iterable<Event>> events() {
        return EventSubject::new;
    }

    @Override
    protected Factory<EventSubject, Iterable<Event>> factory() {
        return events();
    }

    /** Creates the subject for asserting passed events. */
    public static EventSubject assertThat(@NullableDecl Iterable<Event> actual) {
        return assertAbout(events()).that(actual);
    }
}
