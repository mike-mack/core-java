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
package org.spine3.client;

import com.google.protobuf.Any;
import com.google.protobuf.FieldMask;
import org.junit.Test;
import org.spine3.protobuf.AnyPacker;
import org.spine3.test.client.TestEntity;
import org.spine3.test.client.TestEntityId;
import org.spine3.type.TypeName;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Alex Tymchenko
 */
public class TopicFactoryShould
        extends ActorRequestFactoryShould {

    // See {@code client_requests.proto} for declaration.
    private static final Class<TestEntity> TARGET_ENTITY_CLASS = TestEntity.class;
    private static final TypeName TARGET_ENTITY_TYPE_NAME = TypeName.of(TARGET_ENTITY_CLASS);

    @Test
    public void create_topic_for_all_entities_of_kind() {
        final Topic topic = factory().topic().allOf(TARGET_ENTITY_CLASS);

        verifyTargetAndContext(topic);

        assertEquals(0, topic.getTarget()
                             .getFilters()
                             .getIdFilter()
                             .getIdsCount());
    }

    @Test
    public void create_topic_for_some_entities_of_kind() {

        final Set<TestEntityId> ids = newHashSet(entityId(1), entityId(2),
                                                 entityId(3));
        final Topic topic = factory().topic().someOf(TARGET_ENTITY_CLASS, ids);

        verifyTargetAndContext(topic);

        final List<EntityId> actualIds = topic.getTarget()
                                              .getFilters()
                                              .getIdFilter()
                                              .getIdsList();
        assertEquals(ids.size(), actualIds.size());
        for (EntityId actualId : actualIds) {
            final Any rawId = actualId.getId();
            final TestEntityId unpackedId = AnyPacker.unpack(rawId);
            assertTrue(ids.contains(unpackedId));
        }
    }

    @Test
    public void create_topic_for_given_target() {
        final Target givenTarget = Targets.allOf(TARGET_ENTITY_CLASS);
        final Topic topic = factory().topic().forTarget(givenTarget);

        verifyTargetAndContext(topic);
    }

    private void verifyTargetAndContext(Topic topic) {
        assertNotNull(topic);
        assertNotNull(topic.getId());

        assertEquals(TARGET_ENTITY_TYPE_NAME.value(), topic.getTarget()
                                                           .getType());
        assertEquals(FieldMask.getDefaultInstance(), topic.getFieldMask());
        assertEquals(actorContext(), topic.getContext());
    }

    private static TestEntityId entityId(int idValue) {
        return TestEntityId.newBuilder()
                           .setValue(idValue)
                           .build();
    }
}
