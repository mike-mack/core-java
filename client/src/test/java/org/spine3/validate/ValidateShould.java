/*
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
 */

package org.spine3.validate;

import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.TimeUtil;
import org.junit.Test;
import org.spine3.base.CommandId;
import org.spine3.base.Commands;
import org.spine3.base.EventId;
import org.spine3.base.Events;

import static org.junit.Assert.*;

@SuppressWarnings("InstanceMethodNamingConvention")
public class ValidateShould {

    @Test
    public void verify_that_message_is_not_in_default_state() {
        final Message msg = StringValue.newBuilder().setValue("check_if_message_is_not_in_default_state").build();

        assertTrue(Validate.isNotDefault(msg));
        assertFalse(Validate.isNotDefault(StringValue.getDefaultInstance()));
    }

    @Test
    public void verify_that_message_is_in_default_state() {
        final Message nonDefault = StringValue.newBuilder().setValue("check_if_message_is_in_default_state").build();

        assertTrue(Validate.isDefault(StringValue.getDefaultInstance()));
        assertFalse(Validate.isDefault(nonDefault));
    }

    @Test(expected = IllegalStateException.class)
    public void check_if_message_is_in_default_state_throwing_exception_if_not() {
        final StringValue nonDefault = StringValue.newBuilder()
                                                  .setValue("check_if_message_is_in_default_state_throwing_exception_if_not")
                                                  .build();
        Validate.checkDefault(nonDefault);
    }

    @Test
    public void return_default_value_on_check() {
        final Message defaultValue = StringValue.getDefaultInstance();
        assertEquals(defaultValue, Validate.checkDefault(defaultValue));
        assertEquals(defaultValue, Validate.checkDefault(defaultValue, "error message"));
    }

    @Test(expected = IllegalStateException.class)
    public void check_if_message_is_in_not_in_default_state_throwing_exception_if_not() {
        Validate.checkNotDefault(StringValue.getDefaultInstance());
    }

    @Test
    public void return_non_default_value_on_check() {
        final StringValue nonDefault = StringValue.newBuilder()
                                                  .setValue("return_non_default_value_on_check")
                                                  .build();
        assertEquals(nonDefault, Validate.checkNotDefault(nonDefault));
        assertEquals(nonDefault, Validate.checkNotDefault(nonDefault, "with error message"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_timestamp_seconds_are_not_positive() {
        Validate.checkTimestamp(Timestamp.getDefaultInstance(), "");
    }

    @Test
    public void do_not_throw_exception_if_timestamp_seconds_are_positive() {
        Validate.checkTimestamp(TimeUtil.getCurrentTime(), "");
    }

    @Test(expected = NullPointerException.class)
    public void throw_exception_if_checked_string_is_null() {
        //noinspection ConstantConditions
        Validate.checkNotEmptyOrBlank(null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_checked_string_is_empty() {
        Validate.checkNotEmptyOrBlank("", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_checked_string_is_blank() {
        Validate.checkNotEmptyOrBlank("   ", "");
    }

    @Test
    public void do_not_throw_exception_if_checked_string_is_valid() {
        Validate.checkNotEmptyOrBlank("valid_string", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_checked_command_id_is_empty() {
        Validate.checkValid(CommandId.getDefaultInstance());
    }

    @Test
    public void not_throw_exception_if_checked_command_id_is_valid() {
        Validate.checkValid(Commands.generateId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_checked_event_id_is_empty() {
        Validate.checkValid(EventId.getDefaultInstance());
    }

    @Test
    public void not_throw_exception_if_checked_event_id_is_valid() {
        Validate.checkValid(Events.generateId());
    }
}