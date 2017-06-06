/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.server.storage.memory;

import io.grpc.ManagedChannel;
import io.spine.base.Event;
import io.spine.base.EventId;
import io.spine.server.entity.EntityRecord;
import io.spine.server.event.EventStoreIO;
import io.spine.server.event.EventStreamQuery;
import io.spine.server.storage.EventRecordStorage;
import io.spine.server.storage.RecordStorage;
import io.spine.server.storage.RecordStorageIO;
import io.spine.server.storage.memory.grpc.EventStreamRequest;
import io.spine.server.storage.memory.grpc.EventStreamServiceGrpc;
import io.spine.server.storage.memory.grpc.EventStreamServiceGrpc.EventStreamServiceBlockingStub;
import io.spine.server.storage.memory.grpc.InMemoryGrpcServer;
import io.spine.users.TenantId;

import java.util.Iterator;
import java.util.Map;

/**
 * {@inheritDoc}
 */
class InMemoryEventRecordStorage extends EventRecordStorage {

    private final String boundedContextName;

    InMemoryEventRecordStorage(String boundedContextName, RecordStorage<EventId> storage) {
        super(storage);
        this.boundedContextName = boundedContextName;
    }

    @Override
    protected Map<EventId, EntityRecord> readRecords(EventStreamQuery query) {
        final Map<EventId, EntityRecord> allRecords = readAll();
        return allRecords;
    }

    /*
     * Beam support
     *****************/

    @Override
    public RecordStorageIO<EventId> getIO(Class<EventId> idClass) {
        return getDelegateStorage().getIO(idClass);
    }

    @Override
    public EventStoreIO.QueryFn queryFn(TenantId tenantId) {
        return new InMemQueryFn(boundedContextName, tenantId);
    }

    private static class InMemQueryFn extends EventStoreIO.QueryFn {

        private static final long serialVersionUID = 0L;
        private final String boundedContextName;
        private transient ManagedChannel channel;
        private transient EventStreamServiceBlockingStub blockingStub;

        private InMemQueryFn(String boundedContextName, TenantId tenantId) {
            super(tenantId);
            this.boundedContextName = boundedContextName;
        }

        @StartBundle
        public void startBundle() {
            channel = InMemoryGrpcServer.createChannel(boundedContextName);
            blockingStub = EventStreamServiceGrpc.newBlockingStub(channel);
        }

        @FinishBundle
        public void finishBundle() {
            channel.shutdownNow();
            blockingStub = null;
        }

        @Override
        protected Iterator<Event> read(TenantId tenantId, EventStreamQuery query) {
            final EventStreamRequest request = EventStreamRequest.newBuilder()
                                                                 .setTenantId(tenantId)
                                                                 .setQuery(query)
                                                                 .build();
            final Iterator<Event> result = blockingStub.query(request);
            return result;
        }
    }
}
