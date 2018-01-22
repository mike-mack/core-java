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

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.spine.test.TestValues;
import io.spine.test.Tests;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("LocalVariableNamingConvention") // OK for tuple entry values
public class PairShould {

    @Test(expected = NullPointerException.class)
    public void prohibit_null_input() {
        Pair.of(TestValues.newUuidValue(), Tests.<BoolValue>nullRef());
    }

    @Test
    public void support_equality() {
        StringValue v1 = TestValues.newUuidValue();
        StringValue v2 = TestValues.newUuidValue();

        assertEquals(Pair.of(v1, v2), Pair.of(v1, v2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void prohibit_default_values() {
        Pair.of(BoolValue.of(true), StringValue.getDefaultInstance());
    }

    @Test
    public void allow_empty_value() {
        Pair.of(Empty.getDefaultInstance(), BoolValue.of(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void prohibit_all_Empty_instances() {
        Pair.of(Empty.getDefaultInstance(), Empty.getDefaultInstance());
    }

    @Test
    public void return_values() {
        StringValue a = TestValues.newUuidValue();
        BoolValue b = BoolValue.of(true);

        Pair<StringValue, BoolValue> pair = Pair.of(a, b);

        assertEquals(a, pair.getA());
        assertEquals(b, pair.getB());
    }
}