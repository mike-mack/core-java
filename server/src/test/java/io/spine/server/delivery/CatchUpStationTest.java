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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Timestamp;
import io.spine.test.delivery.Calc;
import io.spine.test.delivery.DCounter;
import io.spine.type.TypeUrl;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Streams.stream;
import static com.google.common.truth.Truth.assertThat;
import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Timestamps.compare;
import static com.google.protobuf.util.Timestamps.subtract;
import static io.spine.base.Time.currentTime;
import static io.spine.server.delivery.CatchUpStatus.COMPLETED;
import static io.spine.server.delivery.CatchUpStatus.FINALIZING;
import static io.spine.server.delivery.CatchUpStatus.STARTED;
import static io.spine.server.delivery.InboxMessageStatus.DELIVERED;
import static io.spine.server.delivery.InboxMessageStatus.TO_CATCH_UP;
import static io.spine.server.delivery.InboxMessageStatus.TO_DELIVER;
import static io.spine.server.delivery.given.TestCatchUpJobs.catchUpJob;
import static io.spine.server.delivery.given.TestInboxMessages.catchingUp;
import static io.spine.server.delivery.given.TestInboxMessages.copyWithNewId;
import static io.spine.server.delivery.given.TestInboxMessages.copyWithStatus;
import static io.spine.server.delivery.given.TestInboxMessages.delivered;
import static io.spine.server.delivery.given.TestInboxMessages.toDeliver;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@DisplayName("`CatchUpStation` should")
class CatchUpStationTest {

    private static final String targetOne = "first-catching-up-target-ID";
    private static final String targetTwo = "catching-up-target-ID";
    private static final TypeUrl type = TypeUrl.of(DCounter.class);
    private static final TypeUrl anotherType = TypeUrl.of(Calc.class);

    @Test
    @DisplayName("do nothing if an empty conveyor is passed")
    void doNothingIfNoJobMatches() {
        MemoizingAction action = new MemoizingAction();
        CatchUpStation station = new CatchUpStation(action, new ArrayList<>());
        Conveyor emptyConveyor = new Conveyor(new ArrayList<>(), new DeliveredMessages());

        Station.Result result = station.process(emptyConveyor);
        assertDeliveredCount(result, 0);

        assertThat(action.passedMessages()).isNull();
    }

    @Test
    @DisplayName("remove live messages which correspond to a started `CatchUpJob`")
    void removeLiveMessagesIfCatchUpStarted() {
        InboxMessage toDeliver = toDeliver(targetOne, type);
        InboxMessage anotherToDeliver = copyWithNewId(toDeliver);
        InboxMessage delivered = delivered(targetOne, type);
        InboxMessage differentTarget = delivered(targetTwo, type);
        Conveyor conveyor = new Conveyor(
                ImmutableList.of(toDeliver, anotherToDeliver, delivered, differentTarget),
                new DeliveredMessages()
        );

        CatchUp job = catchUpJob(type, STARTED, currentTime(), ImmutableList.of(targetOne));
        CatchUpStation station = new CatchUpStation(MemoizingAction.empty(), ImmutableList.of(job));
        Station.Result result = station.process(conveyor);

        assertDeliveredCount(result, 0);

        assertContainsExactly(conveyor.removals(),
                              // expected:
                              toDeliver, anotherToDeliver);

        assertContainsExactly(conveyor.iterator(),
                              // expected:
                              delivered, differentTarget);
    }

    @Test
    @DisplayName("deliver the messages in `CATCH_UP` status " +
            "which correspond to a started `CatchUpJob` and remove duplicates")
    void matchAndRunDeliveryAction() {
        InboxMessage toCatchUp = catchingUp(targetOne, type);
        InboxMessage duplicateCopy = copyWithNewId(toCatchUp);
        InboxMessage anotherToCatchUp = catchingUp(targetOne, type);
        InboxMessage alreadyDelivered = delivered(targetOne, type);
        InboxMessage differentTarget = catchingUp(targetTwo, type);

        ImmutableList<InboxMessage> initialContents =
                ImmutableList.of(toCatchUp, anotherToCatchUp, duplicateCopy,
                                 alreadyDelivered, differentTarget);
        Conveyor conveyor = new Conveyor(initialContents, new DeliveredMessages());

        CatchUp job = catchUpJob(type, STARTED, currentTime(), ImmutableList.of(targetOne));
        CatchUpStation station = new CatchUpStation(MemoizingAction.empty(), ImmutableList.of(job));
        Station.Result result = station.process(conveyor);

        assertDeliveredCount(result, 2);

        assertContainsExactly(conveyor.delivered(),
                              // expected:
                              copyWithStatus(toCatchUp, DELIVERED),
                              copyWithStatus(anotherToCatchUp, DELIVERED),
                              alreadyDelivered);

        assertContainsExactly(conveyor.removals(), duplicateCopy);

        assertContainsExactly(conveyor.iterator(),
                              // expected:
                              copyWithStatus(toCatchUp, DELIVERED),
                              copyWithStatus(anotherToCatchUp, DELIVERED),
                              alreadyDelivered,
                              differentTarget);
    }

    @Test
    @DisplayName("mark as `CATCH_UP` those live messages " +
            "which correspond to a finalizing `CatchUpJob`")
    void markLiveMessagesCatchUpIfJobFinalizing() {
        InboxMessage toDeliver = toDeliver(targetOne, type);
        InboxMessage anotherToDeliver = copyWithNewId(toDeliver);
        InboxMessage delivered = delivered(targetOne, type);
        InboxMessage differentTarget = delivered(targetTwo, type);
        Conveyor conveyor = new Conveyor(
                ImmutableList.of(toDeliver, anotherToDeliver, delivered, differentTarget),
                new DeliveredMessages()
        );

        CatchUp job = catchUpJob(type, FINALIZING, currentTime(), ImmutableList.of(targetOne));
        CatchUpStation station = new CatchUpStation(MemoizingAction.empty(), ImmutableList.of(job));
        Station.Result result = station.process(conveyor);
        assertDeliveredCount(result, 0);

        assertContainsExactly(conveyor.iterator(),
                              // expected:
                              copyWithStatus(toDeliver, TO_CATCH_UP),
                              copyWithStatus(anotherToDeliver, TO_CATCH_UP),
                              delivered,
                              differentTarget);
    }

    @Test
    @DisplayName("de-duplicate and deliver all matching `CATCH_UP` messages " +
            "if the respective `CatchUpJob` is completed, " +
            "keeping the `CATCH_UP` messages in their storage for a bit longer")
    void deduplicateAndDeliverWhenJobCompleted() {

        InboxMessage toCatchUp = catchingUp(targetOne, type);
        InboxMessage moreToCatchUp = catchingUp(targetOne, type);
        InboxMessage toDeliver = toDeliver(targetOne, type);
        InboxMessage duplicateToCatchUp = copyWithNewId(toCatchUp);
        InboxMessage duplicateToDeliver = copyWithStatus(copyWithNewId(toCatchUp), TO_DELIVER);
        Conveyor conveyor = new Conveyor(
                ImmutableList.of(toCatchUp, moreToCatchUp, toDeliver,
                                 duplicateToCatchUp, duplicateToDeliver),
                new DeliveredMessages()
        );

        CatchUp job = catchUpJob(type, COMPLETED, currentTime(), ImmutableList.of(targetOne));
        MemoizingAction action = MemoizingAction.empty();
        CatchUpStation station = new CatchUpStation(action, ImmutableList.of(job));
        Station.Result result = station.process(conveyor);
        assertDeliveredCount(result, 2);

        Collection<InboxMessage> deliveredMessages = checkNotNull(action.passedMessages());
        assertContainsExactly(deliveredMessages,
                              // expected:
                              toCatchUp,
                              moreToCatchUp
        );

        Map<InboxMessageId, InboxMessage> remaindersById =
                stream(conveyor.iterator()).collect(toMap(InboxMessage::getId, message -> message));
        assertThat(remaindersById).hasSize(3);

        assertKeptForLonger(toCatchUp.getId(), remaindersById);
        assertKeptForLonger(moreToCatchUp.getId(), remaindersById);
        assertThat(remaindersById.containsKey(toDeliver.getId())).isTrue();
    }

    private static void assertKeptForLonger(InboxMessageId id,
                                            Map<InboxMessageId, InboxMessage> remaindersById) {
        InboxMessage modifiedToCatchUp = remaindersById.get(id);
        assertThat(
                compare(modifiedToCatchUp.getKeepUntil(), modifiedToCatchUp.getWhenReceived())
        ).isGreaterThan(0);
    }

    @Test
    @DisplayName("run the delivery action " +
            "for the messages in `CATCH_UP` status sorting them beforehand")
    void sortCatchUpMessagesBeforeCallingToAction() {

        Timestamp now = currentTime();
        Timestamp secondBefore = subtract(now, fromSeconds(1));
        Timestamp twoSecondsBefore = subtract(now, fromSeconds(2));
        Timestamp threeSecondsBefore = subtract(now, fromSeconds(3));

        InboxMessage toCatchUp1 = catchingUp(targetOne, type, threeSecondsBefore);
        InboxMessage toCatchUp2 = catchingUp(targetOne, type, twoSecondsBefore);
        InboxMessage toCatchUp3 = catchingUp(targetOne, type, secondBefore);
        InboxMessage toCatchUp4 = catchingUp(targetOne, type, now);
        Conveyor conveyor = new Conveyor(
                ImmutableList.of(toCatchUp3, toCatchUp2, toCatchUp4, toCatchUp1),
                new DeliveredMessages()
        );

        CatchUp job = catchUpJob(type, STARTED, currentTime(), ImmutableList.of(targetOne));
        MemoizingAction action = MemoizingAction.empty();
        CatchUpStation station = new CatchUpStation(action, ImmutableList.of(job));
        Station.Result result = station.process(conveyor);
        assertDeliveredCount(result, 4);

        List<InboxMessage> deliveredMessages = checkNotNull(action.passedMessages());
        assertThat(deliveredMessages).containsExactlyElementsIn(
                ImmutableList.of(toCatchUp1, toCatchUp2, toCatchUp3, toCatchUp4)
        );
    }

    @Nested
    @DisplayName("match the `InboxMessage` to a `CatchUpJob`")
    class MatchCatchUpJob {

        @Test
        @DisplayName("by a particular target ID of the `CatchUpJob`")
        void byId() {
            ImmutableSet<InboxMessage> messages = messagesToTargetOneOf(type);
            CatchUp job = catchUpJob(type, STARTED, currentTime(), ImmutableList.of(targetOne));
            assertMatchesEvery(messages, job);
        }

        @Test
        @DisplayName("when the `CatchUpJob` matches all the targets of type " +
                "to which `InboxMessage` is dispatched")
        void allByType() {
            ImmutableSet<InboxMessage> messages = messagesToTargetOneOf(type);
            CatchUp job = catchUpJob(type, STARTED, currentTime(), null);
            assertMatchesEvery(messages, job);
        }

        private void assertMatchesEvery(ImmutableSet<InboxMessage> messages, CatchUp job) {
            for (InboxMessage message : messages) {
                assertThat(CatchUpStation.matches(job, message))
                        .isTrue();

            }
        }
    }

    @Nested
    @DisplayName("not match the `InboxMessage` to a `CatchUpJob`")
    class NotMatchCatchUpJob {

        @Test
        @DisplayName("when the target type of the `InboxMessage` and the `CatchUpJob` differs")
        void whenTargetTypeDiffers() {
            ImmutableSet<InboxMessage> messages = messagesToTargetOneOf(anotherType);
            CatchUp job = catchUpJob(type, STARTED, currentTime(), null);
            assertMatchesNone(messages, job);
        }

        @Test
        @DisplayName("when the target ID of the `InboxMessage` " +
                "does not match the IDs enumerated in the `CatchUpJob`")
        void whenIdDoesNotMatch() {
            ImmutableSet<InboxMessage> messages = messagesToTargetOneOf(type);
            CatchUp job = catchUpJob(type, STARTED, currentTime(), ImmutableSet.of(targetTwo));
            assertMatchesNone(messages, job);
        }

        private void assertMatchesNone(ImmutableSet<InboxMessage> messages, CatchUp job) {
            for (InboxMessage message : messages) {
                assertThat(CatchUpStation.matches(job, message))
                        .isFalse();

            }
        }
    }

    private static ImmutableSet<InboxMessage> messagesToTargetOneOf(TypeUrl targetType) {
        return ImmutableSet.of(toDeliver(targetOne, targetType),
                               delivered(targetOne, targetType),
                               catchingUp(targetOne, targetType));
    }

    private static void assertContainsExactly(Iterator<InboxMessage> actual,
                                              InboxMessage... expected) {
        assertThat(ImmutableSet.copyOf(actual))
                .containsExactlyElementsIn(ImmutableSet.copyOf(expected));
    }

    private static void assertContainsExactly(Collection<InboxMessage> actual,
                                              InboxMessage... expected) {
        assertThat(ImmutableSet.copyOf(actual))
                .containsExactlyElementsIn(ImmutableSet.copyOf(expected));
    }

    private static void assertContainsExactly(Stream<InboxMessage> actual,
                                              InboxMessage... expected) {
        assertThat(actual.collect(toSet()))
                .containsExactlyElementsIn(ImmutableSet.copyOf(expected));
    }

    private static void assertDeliveredCount(Station.Result result, int howMany) {
        assertThat(result.deliveredCount()).isEqualTo(howMany);
        assertThat(result.errors()
                         .hasErrors()).isFalse();
    }

    private static final class MemoizingAction implements DeliveryAction {

        private @Nullable List<InboxMessage> passedMessages;

        private static MemoizingAction empty() {
            return new MemoizingAction();
        }

        @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")   // intentionally.
        @Override
        public DeliveryErrors executeFor(List<InboxMessage> messages) {
            passedMessages = messages;
            return DeliveryErrors.newBuilder()
                                 .build();
        }

        private @Nullable List<InboxMessage> passedMessages() {
            return passedMessages;
        }
    }

}
