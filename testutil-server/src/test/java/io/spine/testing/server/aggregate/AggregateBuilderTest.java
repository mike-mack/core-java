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

package io.spine.testing.server.aggregate;

import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.server.aggregate.Aggregate;
import io.spine.time.testing.TimeTests;
import io.spine.testing.server.aggregate.given.AggregateBuilderTestEnv.TestAggregate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.server.aggregate.given.AggregateBuilderTestEnv.givenAggregate;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Yevsyukov
 */
@DisplayName("AggregateBuilder should")
class AggregateBuilderTest {

    @Test
    @DisplayName("create aggregate")
    void createAggregate() {
        int id = 2048;
        int version = 2017;
        Timestamp whenModified = Time.getCurrentTime();
        Timestamp state = TimeTests.Past.minutesAgo(60);

        Aggregate aggregate = givenAggregate()
                .withId(id)
                .withVersion(version)
                .withState(state)
                .modifiedOn(whenModified)
                .build();

        assertEquals(TestAggregate.class, aggregate.getClass());
        assertEquals(id, aggregate.getId());
        assertEquals(state, aggregate.getState());
        assertEquals(version, aggregate.getVersion().getNumber());
        assertEquals(whenModified, aggregate.whenModified());
    }
}