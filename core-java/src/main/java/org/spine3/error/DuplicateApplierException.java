/*
 * Copyright 2015, TeamDev Ltd. All rights reserved.
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
package org.spine3.error;

import org.spine3.EventClass;
import org.spine3.MessageSubscriber;

/**
 * Exception that is thrown when more than one applier
 * of the same event class is found in a declaring class.
 *
 * @author Mikhail Melnik
 * @author Alexander Yevsyukov
 */
public class DuplicateApplierException extends RuntimeException {

    /**
     * Creates new exception.
     *
     * @param eventClass           a class of the event
     * @param currentSubscriber    a method currently registered
     * @param discoveredSubscriber another applier method for the same event class
     */
    public DuplicateApplierException(
            EventClass eventClass,
            MessageSubscriber currentSubscriber,
            MessageSubscriber discoveredSubscriber) {

        super(String.format("The class %s defines more than one applier method for the event class %s. " +
                            "Applier methods encountered: %s, %s.",
                currentSubscriber.getTargetClass(),
                eventClass,
                currentSubscriber,
                discoveredSubscriber));
    }

    private static final long serialVersionUID = 0L;
}