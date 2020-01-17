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

package io.spine.client;

import com.google.common.collect.ImmutableList;
import io.spine.base.EventMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects event types and allows to query them later for the purpose of testing
 * calls made to event consumers.
 */
final class ConsumerCallCounter {

    private final List<Class<? extends EventMessage>> eventTypes = new ArrayList<>();

    void clear() {
        eventTypes.clear();
    }

    void add(EventMessage e) {
        add(e.getClass());
    }

    void add(Class<? extends EventMessage> eventType) {
        eventTypes.add(eventType);
    }

    boolean contains(Class<? extends EventMessage> type) {
        return eventTypes.contains(type);
    }

    @SafeVarargs
    final boolean containsAll(Class<? extends EventMessage>... types) {
        ImmutableList<Class<? extends EventMessage>> asList = ImmutableList.copyOf(types);
        return eventTypes.containsAll(asList);
    }
}
