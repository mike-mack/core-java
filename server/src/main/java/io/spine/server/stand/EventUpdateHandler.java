/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.server.stand;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.base.Identifier;
import io.spine.client.EventUpdates;
import io.spine.client.Subscription;
import io.spine.client.SubscriptionUpdate;
import io.spine.client.TargetFilters;
import io.spine.core.Event;
import io.spine.core.EventId;
import io.spine.core.Responses;
import io.spine.server.type.EventEnvelope;
import io.spine.type.TypeUrl;

import java.util.Optional;

/**
 * The update handler of {@code Subscription}s for {@code Event}s.
 */
class EventUpdateHandler extends UpdateHandler {

    EventUpdateHandler(Subscription subscription) {
        super(subscription);
    }

    @Override
    Optional<SubscriptionUpdate> detectUpdate(EventEnvelope event) {
        boolean matches = isTypeMatching(event) && (includeAll() || matchByFilters(event));
        if (!matches) {
            return Optional.empty();
        }
        SubscriptionUpdate update = createSubscriptionUpdate(event);
        return Optional.of(update);
    }

    @Override
    protected Any extractId(EventEnvelope event) {
        EventId eventId = event.id();
        Any result = Identifier.pack(eventId);
        return result;
    }

    @Override
    boolean isTypeMatching(EventEnvelope event) {
        String expectedTypeUrl = target().getType();
        String actualTypeUrl = TypeUrl.of(event.message())
                                      .value();
        return expectedTypeUrl.equals(actualTypeUrl);
    }

    /**
     * Matches an event to the subscription filters.
     */
    private boolean matchByFilters(EventEnvelope event) {
        return isIdMatching(event) && checkEventMessageMatches(event);
    }

    /**
     * Creates a subscription update with a single {@link Event} obtained from the envelope.
     */
    private SubscriptionUpdate createSubscriptionUpdate(EventEnvelope event) {
        EventUpdates updates = extractEventUpdates(event);
        SubscriptionUpdate result = SubscriptionUpdate
                .newBuilder()
                .setSubscription(subscription())
                .setResponse(Responses.ok())
                .setEventUpdates(updates)
                .build();
        return result;
    }

    private static EventUpdates extractEventUpdates(EventEnvelope event) {
        Event eventObject = event.outerObject();
        EventUpdates result = EventUpdates
                .newBuilder()
                .addEvent(eventObject)
                .build();
        return result;
    }

    /**
     * Checks if the event message matches the subscription filters.
     */
    private boolean checkEventMessageMatches(EventEnvelope event) {
        Message message = event.message();
        TargetFilters filters = target().getFilters();
        boolean result = filters
                .getFilterList()
                .stream()
                .allMatch(f -> checkPasses(message, f));
        return result;
    }
}
