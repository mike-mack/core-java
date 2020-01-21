/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.server.delivery;

import io.spine.server.delivery.CatchUpProcess.DispatchCatchingUp;
import io.spine.server.projection.ProjectionRepository;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A builder for {@link CatchUpProcess}.
 */
public final class CatchUpProcessBuilder<I> {

    private final ProjectionRepository<I, ?, ?> repository;
    private @Nullable CatchUpStorage storage;
    private @Nullable DispatchCatchingUp<I> dispatchOp;

    CatchUpProcessBuilder(ProjectionRepository<I, ?, ?> repository) {
        this.repository = repository;
    }

    public ProjectionRepository<I, ?, ?> repository() {
        return repository;
    }

    Optional<CatchUpStorage> getStorage() {
        return Optional.ofNullable(storage);
    }

    CatchUpStorage catchUpStorage() {
        return checkNotNull(storage);
    }

    CatchUpProcessBuilder<I> withStorage(CatchUpStorage storage) {
        this.storage = checkNotNull(storage);
        return this;
    }

    public Optional<DispatchCatchingUp<I>> getDispatchOp() {
        return Optional.ofNullable(dispatchOp);
    }

    public DispatchCatchingUp<I> dispatchOp() {
        return checkNotNull(dispatchOp);
    }

    public CatchUpProcessBuilder<I> withDispatchOp(DispatchCatchingUp<I> operation) {
        this.dispatchOp = checkNotNull(operation);
        return this;
    }

    public CatchUpProcess<I> build() {
        checkNotNull(storage);
        checkNotNull(dispatchOp);
        return new CatchUpProcess<>(this);
    }
}
