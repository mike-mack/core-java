import com.google.common.truth.Truth.assertThat
import io.spine.base.Identifier.newUuid
import io.spine.core.UserId
import io.spine.core.userId
import io.spine.testing.client.TestActorRequestFactory
import io.spine.time.ZoneId
import io.spine.time.ZoneIds
import io.spine.time.ZoneIds.systemDefault
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

@DisplayName("`TestActorRequestFactory` should")
class TestActorRequestFactorySpec {

    private lateinit var userId: UserId
    private lateinit var zoneId: ZoneId

    @BeforeEach
    fun generateIds() {
        userId = userId{ value = newUuid() }
        zoneId = systemDefault()
    }

    @Test
    fun `accept 'null' for tenant ID in single-tenant context`() {
        val factory = TestActorRequestFactory(null, userId, zoneId)
        assertThat(factory.tenantId()).isNull()
    }

    @Test
    fun `assume 'systemDefault()' time zone if created using class name`() {
        val factory = TestActorRequestFactory(javaClass)
        assertThat(factory.zoneId()).isEqualTo(systemDefault())
    }
}
