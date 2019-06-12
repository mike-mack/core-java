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

package io.spine.system.server;

import io.grpc.stub.StreamObserver;
import io.spine.core.Ack;
import io.spine.core.SignalId;
import io.spine.core.Signal;
import io.spine.core.Status;
import io.spine.logging.Logging;
import io.spine.type.TypeName;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A {@link StreamObserver} for acknowledgement of system messages.
 *
 * <p>A single observer observes handling of a single message. Thus, the observer expects the stream
 * to be unary.
 *
 * <p>Logs the message handling results:
 * <ul>
 *     <li>successful handling - on {@code debug} level;
 *     <li>rejections - on {@code warn} level;
 *     <li>errors - on {@code error} level.
 * </ul>
 */
final class SystemAckObserver implements StreamObserver<Ack> {

    private final TypeName messageType;
    private final SignalId signalId;
    private final Logger logger;

    private SystemAckObserver(TypeName type, SignalId id, Logger logger) {
        this.messageType = checkNotNull(type);
        this.signalId = checkNotNull(id);
        this.logger = checkNotNull(logger);
    }

    /**
     * Creates a new instance of {@code SystemAckObserver} which observes acknowledgement of the
     * given system message.
     */
    static SystemAckObserver ofResultsOf(Signal<?, ?, ?> message) {
        SignalId id = message.id();
        TypeName name = message.typeUrl()
                               .toTypeName();
        return new SystemAckObserver(name, id, Logging.get(SystemWriteSide.class));
    }

    @SuppressWarnings("EnumSwitchStatementWhichMissesCases")
        // Intentionally covered by the "default" branch.
    @Override
    public void onNext(Ack ack) {
        Status status = ack.getStatus();
        Status.StatusCase statusCase = status.getStatusCase();
        switch (statusCase) {
            case OK:
                logger.debug("Message {} acknowledged.", messageInfo());
                break;
            case ERROR:
                logger.error("Message {} handled with an error:{}{}",
                             messageInfo(),
                             System.lineSeparator(),
                             status.getError());
                break;
            case REJECTION:
                logger.warn("Message {} rejected:{}{}",
                            messageInfo(),
                            System.lineSeparator(),
                            status.getRejection().enclosedMessage());
                break;
            default:
                logger.error("Message {} handled with an unexpected status:{}{}",
                             messageInfo(),
                             System.lineSeparator(),
                             ack);
                break;
        }
    }

    @Override
    public void onError(Throwable t) {
        logger.error(format("Error while posting a system message %s.", messageInfo()), t);
    }

    @Override
    public void onCompleted() {
        logger.debug("System message {} posted successfully.", messageInfo());
    }

    private String messageInfo() {
        return format("%s[%s: %s]",
                      messageType,
                      signalId.getClass().getSimpleName(),
                      signalId.value());
    }
}