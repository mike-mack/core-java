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

package io.spine.server.delivery;

import com.google.common.testing.EqualsTester;
import com.google.protobuf.Timestamp;
import io.spine.base.Identifier;
import io.spine.base.Time;
import io.spine.client.EntityId;
import io.spine.core.Command;
import io.spine.test.delivery.DCreateTask;
import io.spine.test.delivery.DTask;
import io.spine.testing.client.TestActorRequestFactory;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Timestamps.subtract;
import static io.spine.server.delivery.given.TestInboxMessages.toDeliver;

@DisplayName("`DispatchingId` should")
class DispatchingIdTest {

    private final TestActorRequestFactory factory =
            new TestActorRequestFactory(DispatchingIdTest.class);

    @Test
    @DisplayName("support equality")
    void supportEquality() {
        Timestamp now = Time.currentTime();
        Timestamp inPast = subtract(now, fromSeconds(1));
        Command commandA = command("Target A");
        Command commandB = command("Target B");


        DispatchingId messageOne = new DispatchingId(toDeliver(commandA, now));
        DispatchingId anotherMessageOne = new DispatchingId(toDeliver(commandA, now));
        DispatchingId messageOneFromPast = new DispatchingId(toDeliver(commandA, inPast));

        DispatchingId messageTwo = new DispatchingId(toDeliver(commandB, now));
        DispatchingId messageTwoFromPast = new DispatchingId(toDeliver(commandB, inPast));

        new EqualsTester().addEqualityGroup(messageOne, anotherMessageOne, messageOneFromPast)
                          .addEqualityGroup(messageTwo, messageTwoFromPast)
                          .testEquals();
    }

    private Command command(String taskId) {
        return factory.createCommand(DCreateTask.newBuilder()
                                                .setId(taskId)
                                                .vBuild());
    }
}
