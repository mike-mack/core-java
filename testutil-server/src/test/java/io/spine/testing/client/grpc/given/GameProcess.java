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

package io.spine.testing.client.grpc.given;

import io.spine.server.command.Assign;
import io.spine.server.procman.ProcessManager;
import io.spine.testing.client.grpc.Table;
import io.spine.testing.client.grpc.TableSide;
import io.spine.testing.client.grpc.TableVBuilder;
import io.spine.testing.client.grpc.command.Ping;
import io.spine.testing.client.grpc.event.Pong;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.testing.client.grpc.TableSide.LEFT;
import static io.spine.testing.client.grpc.TableSide.RIGHT;
import static io.spine.testing.client.grpc.TableSide.SIDE_UNDEFINED;

/**
 * Process manager that handles the {@link io.spine.testing.client.grpc.command.Ping Ping}
 * command generating the {@link io.spine.testing.client.grpc.event.Pong Pong} event.
 */
final class GameProcess extends ProcessManager<Integer, Table, TableVBuilder> {

    private GameProcess(Integer id) {
        super(id);
    }

    @Assign
    Pong handle(Ping command) {
        getBuilder().setNumber(command.getTable());
        return Pong.newBuilder()
                   .setTable(command.getTable())
                   .setSide(opposite(command.getSide()))
                   .build();
    }

    private static TableSide opposite(TableSide side) {
        checkArgument(side != SIDE_UNDEFINED);
        return (side == LEFT) ? RIGHT : LEFT;
    }
}