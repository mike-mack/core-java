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

/**
 * This package contains of high-level {@link io.spine.client.Client} API. For the tests of
 * low-level client API based on {@link io.spine.client.ActorRequestFactory} please see the same
 * package in the {@link ":client"} module.
 *
 * <p>High-level Client API tests are placed under the {@code ":server"} module because they
 * are based on client-server communications that in turn require backend classes
 * like {@link io.spine.server.Server} or {@link io.spine.server.BoundedContextBuilder}.
 * The {@code ":server"} module itself also depends on {@code ":client"}. Placing these client
 * tests here avoids dependency from {@code "testImplementation"} of {@code ":client"}
 * to {@code ":server"}, which is not circular but in some cases is not handled by Gradle.
 */
@CheckReturnValue
@ParametersAreNonnullByDefault
package io.spine.client;

import com.google.errorprone.annotations.CheckReturnValue;

import javax.annotation.ParametersAreNonnullByDefault;