/*
 *
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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
 *
 */
package org.spine3.server.stand;

import com.google.protobuf.Any;
import org.junit.Assert;
import org.junit.Test;
import org.spine3.testdata.TestStandFactory;

import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Alex Tymchenko
 */
public class StandFunnelShould {

    // **** Positive scenarios (unit) ****

    /**
     * - Initialize properly with various Builder options;
     * - deliver mock updates to the stand (invoke proper methods with particular arguments) - test the delivery only.
     */

    @Test
    public void initialize_properly_with_stand_only() {
        final Stand stand = TestStandFactory.create();
        final StandFunnel.Builder builder = StandFunnel.newBuilder()
                                                       .setStand(stand);
        final StandFunnel standFunnel = builder.build();
        Assert.assertNotNull(standFunnel);
    }


    @Test
    public void use_executor_from_builder() {
        final Stand stand = spy(TestStandFactory.create());
        final Executor executor = spy(new Executor() {
            @Override
            public void execute(Runnable command) {

            }
        });
        final StandFunnel.Builder builder = StandFunnel.newBuilder()
                                                       .setStand(stand)
                                                       .setExecutor(executor);

        final StandFunnel standFunnel = builder.build();
        Assert.assertNotNull(standFunnel);

        final Any someState = Any.getDefaultInstance();
        final Object someId = new Object();
        standFunnel.post(someId, someState);

        verify(executor).execute(any(Runnable.class));
    }


    // **** Negative scenarios (unit) ****

    /**
     * - Fail to initialise with improper stand.
     */

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(expected = IllegalStateException.class)
    public void fail_to_initialize_from_empty_builder() {
        final StandFunnel.Builder builder = StandFunnel.newBuilder();
        builder.build();
    }

    // **** Integration scenarios (<source> -> StandFunnel -> Mock Stand) ****

    /**
     * - Deliver updates from projection repo on update;
     * - deliver updates from aggregate repo on update;
     * - deliver the updates from several projection and aggregate repositories.
     */


}
