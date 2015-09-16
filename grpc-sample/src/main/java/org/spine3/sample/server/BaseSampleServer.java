/*
 * Copyright 2015, TeamDev Ltd. All rights reserved.
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
package org.spine3.sample.server;

import io.grpc.ServerImpl;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.spine3.base.CommandRequest;
import org.spine3.base.CommandResult;
import org.spine3.base.CommandServiceGrpc;
import org.spine3.base.EventRecord;
import org.spine3.eventbus.EventBus;
import org.spine3.sample.EventLogger;
import org.spine3.sample.order.OrderRootRepository;
import org.spine3.server.CommandStore;
import org.spine3.server.Engine;
import org.spine3.server.EventStore;
import org.spine3.server.MessageJournal;
import org.spine3.server.aggregate.AggregateRootEventStorage;
import org.spine3.server.aggregate.SnapshotStorage;

/**
 * Sample gRPC server implementation.
 *
 * @author Mikhail Melnik
 */
public abstract class BaseSampleServer {

    public void registerEventSubscribers() {
        EventBus.getInstance().register(new EventLogger());
    }

    public void prepareEngine() {
        final EventStore eventStore = new EventStore(provideEventStoreStorage());
        final CommandStore commandStore = new CommandStore(provideCommandStoreStorage());

        final OrderRootRepository orderRootRepository = getOrderRootRepository();

        Engine.configure(commandStore, eventStore);
        final Engine engine = Engine.getInstance();
        engine.getCommandDispatcher().register(orderRootRepository);
    }

    private OrderRootRepository getOrderRootRepository() {

        final AggregateRootEventStorage eventStore = new AggregateRootEventStorage(
                provideEventStoreStorage()
        );

        final OrderRootRepository repository = new OrderRootRepository(eventStore, provideSnapshotStorage());
        return repository;
    }

    /* The port on which the server should run */
    private int port = 50051;
    private ServerImpl server;

    protected void start() throws Exception {

        prepareEngine();
        registerEventSubscribers();

        server = NettyServerBuilder.forPort(port)
                .addService(CommandServiceGrpc.bindService(new CommandServiceImpl()))
                .build()
                .start();

        getLog().info("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                BaseSampleServer.this.stop();
                System.err.println("*** server shut down");
            }
        });

    }

    protected void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private static class CommandServiceImpl implements CommandServiceGrpc.CommandService {
        @Override
        public void handle(CommandRequest req, StreamObserver<CommandResult> responseObserver) {
            CommandResult reply = Engine.getInstance().process(req);

            responseObserver.onValue(reply);
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<CommandRequest> handleStream(StreamObserver<CommandResult> responseObserver) {
            StreamObserver<CommandRequest> o = null;
            //TODO:2015-06-25:mikhail.melnik: implement
            return o;
        }
    }

    protected abstract Logger getLog();

    protected abstract MessageJournal<EventRecord> provideEventStoreStorage();

    protected abstract MessageJournal<CommandRequest> provideCommandStoreStorage();

    protected abstract SnapshotStorage provideSnapshotStorage();
}
