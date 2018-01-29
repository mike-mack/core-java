/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.server.tuple;

import com.google.common.base.Optional;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Message;
import io.spine.test.tuple.Bear;
import io.spine.test.tuple.Donkey;
import io.spine.test.tuple.Goat;
import io.spine.test.tuple.Instrument;
import io.spine.test.tuple.Monkey;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static org.junit.Assert.assertEquals;

/**
 * @author Alexander Yevsyukov
 */
public class QuartetShould {

    private final Monkey monkey = Monkey.newBuilder()
                                        .setMessage("Show must go on!")
                                        .setInstrument(Instrument.VIOLIN)
                                        .build();

    private final Donkey donkey = Donkey.newBuilder()
                                        .setMessage("Let's play!")
                                        .setInstrument(Instrument.ALTO_VIOLIN)
                                        .build();

    private final Goat goat = Goat.newBuilder()
                                  .setMessage("We will rock you!")
                                  .setInstrument(Instrument.VIOLIN)
                                  .build();

    private final Bear bear = Bear.newBuilder()
                                  .setMessage("Why not?")
                                  .setInstrument(Instrument.BASS)
                                  .build();

    private Quartet<Monkey, Donkey, Goat, Bear> quartet;

    @Before
    public void setUp() {
        quartet = Quartet.of(monkey, donkey, goat, bear);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().setDefault(Message.class, goat)
                               .setDefault(Optional.class, Optional.of(goat))
                               .testAllPublicStaticMethods(Quartet.class);
    }

    @Test
    public void serialize() {
        reserializeAndAssert(Quartet.of(monkey, donkey, goat, bear));
        reserializeAndAssert(Quartet.withNullable(monkey, null, goat, bear));
        reserializeAndAssert(Quartet.withNullable(monkey, donkey, null, bear));
        reserializeAndAssert(Quartet.withNullable(monkey, donkey, goat, null));
        reserializeAndAssert(Quartet.withNullable(monkey, null, null, null));
    }

    @Test
    public void return_elements() {
        assertEquals(monkey, quartet.getA());
        assertEquals(donkey, quartet.getB());
        assertEquals(goat, quartet.getC());
        assertEquals(bear, quartet.getD());
    }
}
