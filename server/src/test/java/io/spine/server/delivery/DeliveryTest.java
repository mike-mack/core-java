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

package io.spine.server.delivery;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.truth.Truth8;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import io.spine.base.Identifier;
import io.spine.base.Time;
import io.spine.core.Event;
import io.spine.core.EventContext;
import io.spine.core.TenantId;
import io.spine.core.UserId;
import io.spine.grpc.MemoizingObserver;
import io.spine.protobuf.Messages;
import io.spine.server.DefaultRepository;
import io.spine.server.ServerEnvironment;
import io.spine.server.delivery.given.CounterView;
import io.spine.server.delivery.given.DeliveryTestEnv.RawMessageMemoizer;
import io.spine.server.delivery.given.DeliveryTestEnv.ShardIndexMemoizer;
import io.spine.server.delivery.given.FixedShardStrategy;
import io.spine.server.delivery.given.MemoizingDeliveryMonitor;
import io.spine.server.delivery.given.TaskAggregate;
import io.spine.server.delivery.given.TaskAssignment;
import io.spine.server.delivery.given.TaskView;
import io.spine.server.delivery.memory.InMemoryShardedWorkRegistry;
import io.spine.server.event.EventFilter;
import io.spine.server.event.EventStreamQuery;
import io.spine.server.tenant.TenantAwareRunner;
import io.spine.test.delivery.DCreateTask;
import io.spine.test.delivery.DTaskView;
import io.spine.test.delivery.NumberAdded;
import io.spine.testing.SlowTest;
import io.spine.testing.core.given.GivenTenantId;
import io.spine.testing.server.blackbox.BlackBoxBoundedContext;
import io.spine.testing.server.blackbox.SingleTenantBlackBoxContext;
import io.spine.testing.server.entity.EntitySubject;
import io.spine.type.TypeName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.spine.server.delivery.given.DeliveryTestEnv.manyTargets;
import static io.spine.server.delivery.given.DeliveryTestEnv.singleTarget;
import static io.spine.testing.Tests.nullRef;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.toList;

/**
 * Integration tests on message delivery that use different settings of sharding configuration and
 * post {@code Command}s and {@code Events} via multiple threads.
 *
 * @implNote Some of the test methods in this test class use underscores to improve the
 *         readability and allow to distinguish one test from another by their names faster.
 */
@SlowTest
@DisplayName("Delivery of messages to entities should deliver those via")
@SuppressWarnings("WeakerAccess")   // Exposed for libraries, wishing to run these tests.
public class DeliveryTest {

    private Delivery originalDelivery;

    @BeforeEach
    public void setUp() {
        this.originalDelivery = ServerEnvironment.instance()
                                                 .delivery();
    }

    @AfterEach
    public void tearDown() {
        ServerEnvironment.instance()
                         .configureDelivery(originalDelivery);
    }

    @Test
    @DisplayName("a single shard to a single target in a multi-threaded env")
    public void singleTarget_singleShard_manyThreads() {
        changeShardCountTo(1);
        ImmutableSet<String> aTarget = singleTarget();
        new NastyClient(42).runWith(aTarget);
    }

    @Test
    @DisplayName("a single shard to multiple targets in a multi-threaded env")
    public void manyTargets_singleShard_manyThreads() {
        changeShardCountTo(1);
        ImmutableSet<String> targets = manyTargets(7);
        new NastyClient(10).runWith(targets);
    }

    @Test
    @DisplayName("multiple shards to a single target in a multi-threaded env")
    public void singleTarget_manyShards_manyThreads() {
        changeShardCountTo(1986);
        ImmutableSet<String> targets = singleTarget();
        new NastyClient(15).runWith(targets);
    }

    @Test
    @DisplayName("multiple shards to multiple targets in a multi-threaded env")
    public void manyTargets_manyShards_manyThreads() {
        changeShardCountTo(2004);
        ImmutableSet<String> targets = manyTargets(13);
        new NastyClient(19).runWith(targets);
    }

    @Test
    @DisplayName("multiple shards to a single target in a single-threaded env")
    public void singleTarget_manyShards_singleThread() {
        changeShardCountTo(12);
        ImmutableSet<String> aTarget = singleTarget();
        new NastyClient(1).runWith(aTarget);
    }

    @Test
    @DisplayName("a single shard to a single target in a single-threaded env")
    public void singleTarget_singleShard_singleThread() {
        changeShardCountTo(1);
        ImmutableSet<String> aTarget = singleTarget();
        new NastyClient(1).runWith(aTarget);
    }

    @Test
    @DisplayName("a single shard to mutiple targets in a single-threaded env")
    public void manyTargets_singleShard_singleThread() {
        changeShardCountTo(1);
        ImmutableSet<String> targets = manyTargets(11);
        new NastyClient(1).runWith(targets);
    }

    @Test
    @DisplayName("multiple shards to multiple targets in a single-threaded env")
    public void manyTargets_manyShards_singleThread() {
        changeShardCountTo(2019);
        ImmutableSet<String> targets = manyTargets(13);
        new NastyClient(1).runWith(targets);
    }

    @Test
    @DisplayName("multiple shards to multiple targets " +
            "in a multi-threaded env with the custom strategy")
    public void withCustomStrategy() {
        FixedShardStrategy strategy = new FixedShardStrategy(13);
        Delivery newDelivery = Delivery.localWithStrategyAndWindow(strategy, Durations.ZERO);
        ShardIndexMemoizer memoizer = new ShardIndexMemoizer();
        newDelivery.subscribe(memoizer);
        ServerEnvironment.instance()
                         .configureDelivery(newDelivery);

        ImmutableSet<String> targets = manyTargets(7);
        new NastyClient(5, false).runWith(targets);

        ImmutableSet<ShardIndex> shards = memoizer.shards();
        assertThat(shards.size()).isEqualTo(1);
        assertThat(shards.iterator()
                         .next())
                .isEqualTo(strategy.nonEmptyShard());
    }

    @Test
    @DisplayName("multiple shards to multiple targets in a single-threaded env " +
            "and calculate the statistics properly")
    public void calculateStats() {
        Delivery delivery = Delivery.newBuilder()
                                    .setStrategy(UniformAcrossAllShards.forNumber(7))
                                    .build();
        ServerEnvironment.instance()
                         .configureDelivery(delivery);
        List<DeliveryStats> deliveryStats = synchronizedList(new ArrayList<>());
        delivery.subscribe(msg -> {
            Optional<DeliveryStats> stats = delivery.deliverMessagesFrom(msg.getShardIndex());
            stats.ifPresent(deliveryStats::add);
        });

        RawMessageMemoizer rawMessageMemoizer = new RawMessageMemoizer();
        delivery.subscribe(rawMessageMemoizer);

        ImmutableSet<String> targets = manyTargets(7);
        new NastyClient(1).runWith(targets);
        int totalMsgsInStats = deliveryStats.stream()
                                            .mapToInt(DeliveryStats::deliveredCount)
                                            .sum();
        assertThat(totalMsgsInStats).isEqualTo(rawMessageMemoizer.messages()
                                                                 .size());
    }

    @Test
    @DisplayName("single shard and return stats when picked up the shard " +
            "and `Optional.empty()` if shard was already picked")
    public void returnOptionalEmptyIfPicked() {
        int shardCount = 11;
        ShardedWorkRegistry registry = new InMemoryShardedWorkRegistry();
        FixedShardStrategy strategy = new FixedShardStrategy(shardCount);
        Delivery delivery = Delivery.newBuilder()
                                    .setStrategy(strategy)
                                    .setWorkRegistry(registry)
                                    .build();
        ServerEnvironment env = ServerEnvironment.instance();
        env.configureDelivery(delivery);

        ShardIndex index = strategy.nonEmptyShard();
        TenantId tenantId = GivenTenantId.generate();
        TenantAwareRunner.with(tenantId)
                         .run(() -> assertStatsMatch(delivery, index));

        Optional<ShardProcessingSession> session = registry.pickUp(index, env.nodeId());
        Truth8.assertThat(session)
              .isPresent();

        TenantAwareRunner.with(tenantId)
                         .run(() -> assertStatsEmpty(delivery, index));
    }

    @Test
    @DisplayName("single shard and notify the monitor once the delivery is completed")
    public void notifyDeliveryMonitorOfDeliveryCompletion() {
        MonitorUnderTest monitor = new MonitorUnderTest();
        int shardCount = 1;
        FixedShardStrategy strategy = new FixedShardStrategy(shardCount);
        ShardIndex theOnlyIndex = strategy.nonEmptyShard();
        Delivery delivery = Delivery.newBuilder()
                                    .setStrategy(strategy)
                                    .setMonitor(monitor)
                                    .build();
        RawMessageMemoizer rawMessageMemoizer = new RawMessageMemoizer();
        delivery.subscribe(rawMessageMemoizer);
        delivery.subscribe(new LocalDispatchingObserver());
        ServerEnvironment.instance()
                         .configureDelivery(delivery);

        ImmutableSet<String> aTarget = singleTarget();
        assertThat(monitor.stats()).isEmpty();
        new NastyClient(1).runWith(aTarget);

        for (DeliveryStats singleRunStats : monitor.stats()) {
            assertThat(singleRunStats.shardIndex()).isEqualTo(theOnlyIndex);
        }
        int totalFromStats = monitor.stats()
                                    .stream()
                                    .mapToInt(DeliveryStats::deliveredCount)
                                    .sum();

        int observedMsgCount = rawMessageMemoizer.messages()
                                                 .size();
        assertThat(totalFromStats).isEqualTo(observedMsgCount);
    }

    private static void assertStatsEmpty(Delivery delivery, ShardIndex index) {
        Optional<DeliveryStats> emptyStats = delivery.deliverMessagesFrom(index);
        Truth8.assertThat(emptyStats)
              .isEmpty();
    }

    @Test
    @DisplayName("multiple shards and " +
            "keep them as `TO_DELIVER` right after they are written to `InboxStorage`, " +
            "and mark every as `DELIVERED` after they are actually delivered.")
    @SuppressWarnings("MethodWithMultipleLoops")
    // Traversing over the storage.
    public void markDelivered() {

        FixedShardStrategy strategy = new FixedShardStrategy(3);

        // Set a very long window to keep the messages non-deleted from the `InboxStorage`.
        Delivery newDelivery = Delivery.localWithStrategyAndWindow(strategy, Durations.fromDays(1));
        RawMessageMemoizer memoizer = new RawMessageMemoizer();
        newDelivery.subscribe(memoizer);
        ServerEnvironment.instance()
                         .configureDelivery(newDelivery);

        ImmutableSet<String> targets = manyTargets(6);
        new NastyClient(3, false).runWith(targets);

        // Check that each message was in `TO_DELIVER` status upon writing to the storage.
        ImmutableList<InboxMessage> rawMessages = memoizer.messages();
        for (InboxMessage message : rawMessages) {
            assertThat(message.getStatus()).isEqualTo(InboxMessageStatus.TO_DELIVER);
        }

        ImmutableMap<ShardIndex, Page<InboxMessage>> contents = InboxContents.get();
        for (Page<InboxMessage> page : contents.values()) {
            ImmutableList<InboxMessage> messages = page.contents();
            for (InboxMessage message : messages) {
                assertThat(message.getStatus()).isEqualTo(InboxMessageStatus.DELIVERED);
            }
        }
    }

    @Test
    @DisplayName("a single shard to a single target in a multi-threaded env in batches")
    public void deliverInBatch() {
        FixedShardStrategy strategy = new FixedShardStrategy(1);
        MemoizingDeliveryMonitor monitor = new MemoizingDeliveryMonitor();
        int pageSize = 20;
        Delivery delivery = Delivery.newBuilder()
                                    .setStrategy(strategy)
                                    .setIdempotenceWindow(Durations.ZERO)
                                    .setMonitor(monitor)
                                    .setPageSize(pageSize)
                                    .build();
        deliverAfterPause(delivery);

        ServerEnvironment.instance()
                         .configureDelivery(delivery);
        ImmutableSet<String> targets = singleTarget();
        NastyClient simulator = new NastyClient(7, false);
        simulator.runWith(targets);

        String theTarget = targets.iterator()
                                  .next();
        int signalsDispatched = simulator.signalsPerTarget()
                                         .get(theTarget)
                                         .size();
        assertThat(simulator.callsToRepoLoadOrCreate(theTarget)).isLessThan(signalsDispatched);
        assertThat(simulator.callsToRepoStore(theTarget)).isLessThan(signalsDispatched);

        assertStages(monitor, pageSize);
    }

    @Test
    @DisplayName("via multiple shards in multiple threads in an order of message emission")
    public void deliverMessagesInOrderOfEmission() throws InterruptedException {
        changeShardCountTo(20);

        SingleTenantBlackBoxContext context = BlackBoxBoundedContext.singleTenant()
                                                                    .with(DefaultRepository.of(
                                                                            TaskAggregate.class))
                                                                    .with(new TaskAssignment.Repository())
                                                                    .with(new TaskView.Repository());
        List<DCreateTask> commands = generateCommands(200);
        ExecutorService service = newFixedThreadPool(20);
        service.invokeAll(commands.stream()
                                  .map(c -> (Callable<Object>) () -> context.receivesCommand(c))
                                  .collect(toList()));
        List<Runnable> leftovers = service.shutdownNow();
        assertThat(leftovers).isEmpty();

        for (DCreateTask command : commands) {
            String taskId = command.getId();
            EntitySubject subject = context.assertEntity(TaskView.class, taskId);
            subject.exists();

            TaskView actualView = (TaskView) subject.actual();
            DTaskView state = actualView.state();
            UserId actualAssignee = state.getAssignee();

            assertThat(state.getId()).isEqualTo(taskId);
            assertThat(Messages.isDefault(actualAssignee)).isFalse();
        }
    }

    @Test
    public void catchUp() throws InterruptedException {
        changeShardCountTo(2);
        CounterView.Repository repo = new CounterView.Repository();
        SingleTenantBlackBoxContext ctx = BlackBoxBoundedContext.singleTenant()
                                                                .with(repo);

        String[] ids = {"first", "second", "third", "fourth"};
        List<NumberAdded> events = generateEvents(200, ids);

        // Round 1. Fight!

        int initialWeight = 1;
        CounterView.changeWeightTo(initialWeight);
        dispatchInParallel(ctx, events, 20);

        List<Integer> initialTotals = readTotals(repo, ids);
        int sumInRound = events.size() / ids.length * initialWeight;
        IntStream sums = IntStream.iterate(sumInRound, i -> i)
                                  .limit(ids.length);
        assertThat(initialTotals).isEqualTo(sums.boxed()
                                                .collect(toList()));

        // Round 2. Catch up the first one and fight!

        int newWeight = 100;
        CounterView.changeWeightTo(newWeight);
        Timestamp aWhileAgo = Timestamps.subtract(Time.currentTime(), Durations.fromHours(1));

        ExecutorService service = newFixedThreadPool(20);
        List<Callable<Object>> jobs = new ArrayList<>();

        // Do the same, but add the catch-up for ID #0 as the first job.
        String firstId = ids[0];
        Callable<Object> catchUpCallable = () -> {
            repo.catchUp(ImmutableSet.of(firstId), aWhileAgo);
            return nullRef();
        };
        jobs.add(catchUpCallable);
        jobs.addAll(asPostJobs(ctx, events));
        service.invokeAll(jobs);
        List<Runnable> leftovers = service.shutdownNow();
        assertThat(leftovers).isEmpty();

        EventFilter numberAdded = EventFilter
                .newBuilder()
                .setEventType(TypeName.of(NumberAdded.class)
                                      .value())
                .build();
        MemoizingObserver<Event> allEvents = new MemoizingObserver<>();
        ctx.eventBus()
           .eventStore()
           .read(EventStreamQuery.newBuilder()
                                 .addFilter(numberAdded)
                                 .vBuild(), allEvents);
        System.out.println("---- ALL events: ---");
        for (Event event : allEvents.responses()) {
            EventContext context = event.getContext();
            Timestamp timestamp = context.getTimestamp();
            System.out.println(timestamp.getSeconds()
                                       + "." + timestamp.getNanos() + " -> "
                                       + context.getOrder());
        }

        List<Integer> totalsAfterCatchUp = readTotals(repo, ids);
        List<Integer> expectedTotals = new ArrayList<>();
        expectedTotals.add(sumInRound * newWeight / initialWeight * 2);
        IntStream.range(1, ids.length)
                 .forEach((i) -> expectedTotals.add(
                         sumInRound + sumInRound * newWeight / initialWeight));
        assertThat(totalsAfterCatchUp).isEqualTo(expectedTotals);

    }

    private static List<Callable<Object>> asPostJobs(SingleTenantBlackBoxContext ctx,
                                                     List<NumberAdded> events) {
        return events.stream()
                     .map(e -> (Callable<Object>) () -> ctx.receivesEvent(e))
                     .collect(toList());
    }

    private static List<Integer> readTotals(CounterView.Repository repo, String[] ids) {
        return Arrays.stream(ids)
                     .map((id) -> findView(repo, id).state()
                                                    .getTotal())
                     .collect(toList());
    }

    private static List<NumberAdded> generateEvents(int howMany, String[] targets) {
        Iterator<String> idIterator = Iterators.cycle(targets);
        List<NumberAdded> events = new ArrayList<>(howMany);
        for (int i = 0; i < howMany; i++) {
            events.add(NumberAdded.newBuilder()
                                  .setCalculatorId(idIterator.next())
                                  .setValue(0)
                                  .vBuild());
        }
        return events;
    }

    private static CounterView findView(CounterView.Repository repo, String id) {
        Optional<CounterView> firstView = repo.find(id);
        if(!firstView.isPresent()) {
            System.out.println("`CounterView`#" + id + " not found.");
        }
        Truth8.assertThat(firstView)
              .isPresent();
        return firstView.get();
    }

    /*
     * Test environment.
     *
     * <p>Accesses the {@linkplain Delivery Delivery API} which has been made
     * package-private and marked as visible for testing. Therefore the test environment routines
     * aren't moved to a separate {@code ...TestEnv} class. Otherwise the test-only API
     * of {@code Delivery} must have been made {@code public}, which wouldn't be
     * a good API design move.
     ******************************************************************************/

    private static void changeShardCountTo(int shards) {
        Delivery newDelivery = Delivery.localWithShardsAndWindow(shards, Durations.ZERO);
        ServerEnvironment.instance()
                         .configureDelivery(newDelivery);
    }

    private static void assertStatsMatch(Delivery delivery, ShardIndex index) {
        Optional<DeliveryStats> stats = delivery.deliverMessagesFrom(index);
        Truth8.assertThat(stats)
              .isPresent();
        assertThat(stats.get()
                        .shardIndex()).isEqualTo(index);
    }

    private static List<DCreateTask> generateCommands(int howMany) {
        List<DCreateTask> commands = new ArrayList<>();
        for (int taskIndex = 0; taskIndex < howMany; taskIndex++) {
            String taskId = Identifier.newUuid();
            commands.add(DCreateTask.newBuilder()
                                    .setId(taskIndex + "--" + taskId)
                                    .vBuild());
        }
        return commands;
    }

    private static void assertStages(MemoizingDeliveryMonitor monitor, int pageSize) {
        ImmutableList<DeliveryStage> totalStages = monitor.getStages();
        List<Integer> actualSizePerPage = totalStages.stream()
                                                     .map(DeliveryStage::getMessagesDelivered)
                                                     .collect(toList());
        for (Integer actualSize : actualSizePerPage) {
            assertThat(actualSize).isAtMost(pageSize);
        }
    }

    private static void deliverAfterPause(Delivery delivery) {
        CountDownLatch latch = new CountDownLatch(20);
        // Sleep for some time to accumulate messages in shards before starting to process them.
        delivery.subscribe(update -> {
            if (latch.getCount() > 0) {
                sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
                latch.countDown();
            } else {
                delivery.deliverMessagesFrom(update.getShardIndex());
            }
        });
    }

    private static void dispatchInParallel(SingleTenantBlackBoxContext ctx,
                                           List<NumberAdded> events,
                                           int threads) throws InterruptedException {
        ExecutorService service = newFixedThreadPool(threads);
        service.invokeAll(asPostJobs(ctx, events));
        List<Runnable> leftovers = service.shutdownNow();
        assertThat(leftovers).isEmpty();
    }

    private static final class MonitorUnderTest extends DeliveryMonitor {

        private final List<DeliveryStats> allStats = new ArrayList<>();

        @Override
        public void onDeliveryCompleted(DeliveryStats stats) {
            allStats.add(stats);
        }

        private ImmutableList<DeliveryStats> stats() {
            return ImmutableList.copyOf(allStats);
        }
    }
}
