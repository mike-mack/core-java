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

package org.spine3.server.event.storage;

import org.spine3.SPI;
import org.spine3.base.Event;
import org.spine3.protobuf.TypeUrl;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.event.EventStorage;
import org.spine3.server.storage.StorageField;

/**
 * A container for the storage fields specific for the {@link EventStorage} and its implementations.
 *
 * @author Dmytro Dashenkov
 * @see StorageField
 */
@SPI
public enum EventField implements StorageField {

    /**
     * A field representing the string representation of
     * the {@linkplain EventStorageRecord#getProducerId() producer ID} of an event.
     *
     * <p>Typically, it's an ID of the {@linkplain Aggregate} that produced the {@linkplain Event}.
     */
    producer_id,

    /**
     * A field representing the {@linkplain EventStorageRecord#getEventId() event ID}.
     *
     * <p>An event ID is a protobuf-message type, though the {@link EventStorageRecord#getEventId()} returns its string
     * representation.
     *
     * <p>The string representation is what is stored in this field indeed.
     */
    event_id,

    /**
     * A field representing the type of an {@linkplain Event} message e.g. as a {@link TypeUrl}.
     *
     * <p>This field may be used for serialization.
     */
    event_type

}