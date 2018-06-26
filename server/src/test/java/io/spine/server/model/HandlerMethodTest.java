/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.server.model;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.spine.core.EventClass;
import io.spine.core.EventContext;
import io.spine.server.model.given.HandlerMethodTestEnv.OneParamMethod;
import io.spine.server.model.given.HandlerMethodTestEnv.StubHandler;
import io.spine.server.model.given.HandlerMethodTestEnv.TwoParamMethod;
import io.spine.test.Tests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alexander Litus
 * @author Alexander Yevsyukov
 */
@SuppressWarnings("DuplicateStringLiteralInspection") // Common test display names.
@DisplayName("HandlerMethod should")
class HandlerMethodTest {

    private final HandlerMethod.Factory<OneParamMethod> factory = OneParamMethod.factory();

    private HandlerMethod<EventClass, EventContext> twoParamMethod;
    private HandlerMethod<EventClass, Empty> oneParamMethod;
    private Object target;

    @BeforeEach
    void setUp() {
        target = new StubHandler();
        twoParamMethod = new TwoParamMethod(StubHandler.getTwoParameterMethod());
        oneParamMethod = new OneParamMethod(StubHandler.getOneParameterMethod());
    }

    @Test
    @DisplayName("not accept null method")
    void notAcceptNullMethod() {
        assertThrows(NullPointerException.class, () -> new TwoParamMethod(Tests.nullRef()));
    }

    @Test
    @DisplayName("return method")
    void returnMethod() {
        assertEquals(StubHandler.getTwoParameterMethod(), twoParamMethod.getMethod());
    }

    @Nested
    @DisplayName("check if method access is")
    class CheckAccess {

        @Test
        @DisplayName("public")
        void isPublic() {
            assertTrue(twoParamMethod.isPublic());
        }

        @Test
        @DisplayName("private")
        void isPrivate() {
            assertTrue(oneParamMethod.isPrivate());
        }
    }

    @Test
    @DisplayName("obtain first parameter type of method")
    void returnFirstParamType() {
        assertEquals(BoolValue.class, HandlerMethod.getFirstParamType(oneParamMethod.getMethod()));
    }

    @Nested
    @DisplayName("invoke method")
    class InvokeMethod {

        @SuppressWarnings("CheckReturnValue") // can ignore the result in this test
        @Test
        @DisplayName("with one parameter")
        void withOneParam() {
            oneParamMethod.invoke(target,
                                  BoolValue.getDefaultInstance(),
                                  Empty.getDefaultInstance());

            assertTrue(((StubHandler) target).wasHandleInvoked());
        }

        @SuppressWarnings("CheckReturnValue") // can ignore the result in this test
        @Test
        @DisplayName("with two parameters")
        void withTwoParams() {
            twoParamMethod.invoke(target,
                                  StringValue.getDefaultInstance(),
                                  EventContext.getDefaultInstance());

            assertTrue(((StubHandler) target).wasOnInvoked());
        }
    }

    @Test
    @DisplayName("return full name in `toString`")
    void provideToString() {
        assertEquals(twoParamMethod.getFullName(), twoParamMethod.toString());
    }

    @Nested
    @DisplayName("provide `equals` method such that")
    class ProvideEqualsSuchThat {

        @Test
        @DisplayName("instance equals to itself")
        void equalsToItself() {
            assertEquals(twoParamMethod, twoParamMethod);
        }

        @Test
        @DisplayName("instance is not equal to null")
        void notEqualsToNull() {
            assertNotEquals(null, oneParamMethod);
        }

        @Test
        @DisplayName("instance is not equal to another class")
        void notEqualsToOtherClass() {
            assertNotEquals(twoParamMethod, oneParamMethod);
        }

        @Test
        @DisplayName("all fields are compared")
        void allFieldsAreCompared() {
            HandlerMethod<EventClass, EventContext> anotherMethod =
                    new TwoParamMethod(StubHandler.getTwoParameterMethod());

            assertEquals(twoParamMethod, anotherMethod);
        }
    }

    @Test
    @DisplayName("have `hashCode`")
    void haveHashCode() {
        assertNotEquals(System.identityHashCode(twoParamMethod), twoParamMethod.hashCode());
    }

    @Test
    @DisplayName("not be created from method throwing checked exception")
    void rejectMethodThrowingChecked() {
        assertThrows(IllegalStateException.class,
                     () -> factory.create(StubHandler.getMethodWithCheckedException()));
    }

    @Test
    @DisplayName("be normally created from method throwing runtime exception")
    void acceptMethodThrowingRuntime() {
        OneParamMethod method = factory.create(StubHandler.getMethodWithRuntimeException());
        assertEquals(StubHandler.getMethodWithRuntimeException(), method.getMethod());
    }
}
