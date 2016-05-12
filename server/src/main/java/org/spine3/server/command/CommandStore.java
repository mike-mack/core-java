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

package org.spine3.server.command;

import org.spine3.base.Command;
import org.spine3.base.CommandId;
import org.spine3.base.CommandStatus;
import org.spine3.base.Error;
import org.spine3.base.Errors;
import org.spine3.base.Failure;
import org.spine3.server.storage.CommandStorage;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkState;

/**
 * Manages commands received by the system.
 *
 * @author Mikhail Mikhaylov
 */
public class CommandStore implements AutoCloseable {

    private final CommandStorage storage;

    /**
     * Creates a new instance.
     *
     * @param storage an underlying storage
     */
    public CommandStore(CommandStorage storage) {
        this.storage = storage;
    }

    /**
     * Stores the command.
     *
     * <p>The underlying storage must be opened.
     *
     * @param command the command to store
     * @throws IllegalStateException if the storage is closed
     */
    public void store(Command command) {
        checkIsOpened();
        storage.store(command);
    }

    /**
     * Stores a command with the error status.
     *
     * @param command a command to store
     * @param error an error occurred
     * @throws IllegalStateException if the storage is closed
     */
    public void store(Command command, Error error) {
        checkIsOpened();
        storage.store(command, error);
    }

    /**
     * Stores the command with the error status.
     *
     * @param command the command to store
     * @param exception an exception occurred to convert to {@link org.spine3.base.Error}
     * @throws IllegalStateException if the storage is closed
     */
    public void store(Command command, Exception exception) {
        checkIsOpened();
        storage.store(command, Errors.fromException(exception));
    }

    /**
     * Stores a command with the given status.
     *
     * @param command a command to store
     * @param status a command status
     * @throws IllegalStateException if the storage is closed
     */
    public void store(Command command, CommandStatus status) {
        checkIsOpened();
        storage.store(command, status);
    }

    /**
     * Loads all commands with the given status.
     *
     * @param status a command status to search by
     * @return commands with the given status
     * @throws IllegalStateException if the storage is closed
     */
    public Iterator<Command> load(CommandStatus status) {
        checkIsOpened();
        final Iterator<Command> commands = storage.load(status);
        return commands;
    }

    /**
     * Sets the status of the command to {@link CommandStatus#OK}
     *
     * @param commandId an ID of the command
     * @throws IllegalStateException if the storage is closed
     */
    public void setCommandStatusOk(CommandId commandId) {
        checkIsOpened();
        storage.setOkStatus(commandId);
    }

    /**
     * Updates the status of the command processing with the exception.
     *
     * @param commandId the ID of the command
     * @param exception the exception occurred during command processing
     * @throws IllegalStateException if the storage is closed
     */
    public void updateStatus(CommandId commandId, Exception exception) {
        checkIsOpened();
        storage.updateStatus(commandId, Errors.fromException(exception));
    }

    /**
     * Updates the status of the command with the passed error.
     *
     * @param commandId the ID of the command
     * @param error the error, which occurred during command processing
     * @throws IllegalStateException if the storage is closed
     */
    public void updateStatus(CommandId commandId, org.spine3.base.Error error) {
        checkIsOpened();
        storage.updateStatus(commandId, error);
    }

    /**
     * Updates the status of the command with the business failure.
     *
     * @param commandId the ID of the command
     * @param failure the business failure occurred during command processing
     * @throws IllegalStateException if the storage is closed
     */
    public void updateStatus(CommandId commandId, Failure failure) {
        checkIsOpened();
        storage.updateStatus(commandId, failure);
    }

    @Override
    public void close() throws Exception {
        storage.close();
    }

    /**
     * @return true if the store is open, false otherwise
     */
    public boolean isOpen() {
        final boolean isOpened = storage.isOpen();
        return isOpened;
    }

    /**
     * @return true if the store is closed, false otherwise
     */
    public boolean isClosed() {
        final boolean isClosed = !isOpen();
        return isClosed;
    }

    private void checkIsOpened() {
        checkState(isOpen(), "The command store is closed.");
    }
}
