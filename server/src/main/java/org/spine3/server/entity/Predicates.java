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

package org.spine3.server.entity;

import com.google.common.base.Predicate;
import org.spine3.server.entity.status.EntityStatus;
import org.spine3.server.storage.EntityStorageRecord;

import javax.annotation.Nullable;

/**
 * Collection of predicates for filtering aggregates by their {@code EntityStatus}.
 *
 * @author Alexander Yevsyukov
 */
public class Predicates {

    private static final Predicate<EntityStatus> isEntityVisible = new Predicate<EntityStatus>() {
        @Override
        public boolean apply(@Nullable EntityStatus input) {
            return input == null ||
                    !(input.getArchived() || input.getDeleted());
        }
    };
    private static final Predicate<EntityStorageRecord> isRecordVisible = new Predicate<EntityStorageRecord>() {
        @Override
        public boolean apply(@Nullable EntityStorageRecord input) {
            if (input == null) {
                return true;
            }
            final EntityStatus entityStatus = input.getEntityStatus();
            return !(entityStatus.getArchived() || entityStatus.getDeleted());
        }
    };

    private Predicates() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Obtains the predicate for checking if an aggregate is visible to regular queries.
     *
     * <p>An aggregate may be marked as archived or deleted. If so, it becomes “invisible”
     * to regular queries.
     *
     * @return the predicate that filters “invisible” {@code AggregateStatus}es
     * @see EntityStatus#getArchived()
     * @see EntityStatus#getDeleted()
     */
    public static Predicate<EntityStatus> isEntityVisible() {
        return isEntityVisible;
    }

    /**
     * Obtains the predicate for checking if an entity is visible to regular queries.
     *
     * <p>An entity may be marked as archived or deleted. If so, it becomes “invisible”
     * to regular queries.
     *
     * @return the predicate that filters “invisible” {@code EntityStorageRecord}s
     * @see EntityStorageRecord#getEntityStatus()
     */
    public static Predicate<EntityStorageRecord> isRecordVisible() {
        return isRecordVisible;
    }
}