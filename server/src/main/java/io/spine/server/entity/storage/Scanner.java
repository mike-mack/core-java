/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.server.entity.storage;

import io.spine.base.EntityState;
import io.spine.client.ArchivedColumn;
import io.spine.client.DeletedColumn;
import io.spine.client.VersionColumn;
import io.spine.query.Column;
import io.spine.query.EntityColumn;
import io.spine.server.entity.Entity;
import io.spine.server.entity.model.EntityClass;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Scans and extracts the definitions of {@link Column}s to be stored
 * for a particular {@code Entity}.
 *
 * <p>The resulting columns include both the entity state-based columns declared with
 * {@link io.spine.option.OptionsProto#column (column)} Proto option and the columns
 * storing lifecycle and version attributes of an {@code Entity}.
 *
 * @param <E>
 *         the type of {@code Entity} to scan
 * @param <S>
 *         the type of the state for the scanned {@code Entity}
 * @implNote Client-side API includes generic definitions of lifecycle and version columns
 *         (such as {@link ArchivedColumn}). However, their code cannot depend on the {@code Entity}
 *         type directly, as the {@code client} module has no dependency on {@code server} module.
 *         Therefore, this column scanning process wires those generic column definitions with an
 *         actual {@code Entity} type, instances of which serve as a data source for each column.
 *         Also, instead of scanning the {@code (column)} options from an entity state
 *         {@code Message} directly, this scanner uses a Spine compiler-generated shortcut method
 *         called {@code definitions()} which returns the set of {@link EntityColumn}s.
 *         Such an approach improves the scanning performance and preserve the types of generic
 *         parameters code-generated for each {@code EntityColumn}.
 */
final class Scanner<S extends EntityState<?>, E extends Entity<?, S>> {

    /**
     * The name of the nested class generated by the Spine compiler as a container of
     * the entity column definitions.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")   // coincidental duplication
    private static final String COLS_NESTED_CLASSNAME = "Column";

    /**
     * The name of the method inside the column container class generated by the Spine compiler.
     *
     * <p>The method returns all the definitions of the columns for this state class.
     */
    private static final String COL_DEFS_METHOD_NAME = "definitions";

    /**
     * The target entity class to scan.
     */
    private final EntityClass<E> entityClass;

    /**
     * Creates an instance of scanner for a given type of {@code Entity}.
     */
    Scanner(EntityClass<E> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Returns all columns for the scanned {@code Entity}.
     *
     * <p>The result includes both lifecycle columns and the columns declared
     * in the {@code Entity} state.
     */
    EntityColumns<E> columns() {
        Set<Column<E, ?>> accumulator = new HashSet<>();

        var stateColumns = stateColumns();
        for (var stateCol : stateColumns) {
            Column<E, ?> wrapped = wrap(stateCol, (entity) -> stateCol.valueIn(entity.state()));
            accumulator.add(wrapped);
        }

        accumulator.add(wrap(ArchivedColumn.instance(), Entity::isArchived));
        accumulator.add(wrap(DeletedColumn.instance(), Entity::isDeleted));
        accumulator.add(wrap(VersionColumn.instance(), Entity::version));

        var columns = new EntityColumns<>(accumulator);
        return columns;
    }

    private AsEntityColumn<E> wrap(Column<?, ?> origin, Function<E, Object> getter) {
        return new AsEntityColumn<>(origin, getter);
    }

    /**
     * Obtains the {@linkplain EntityColumn entity-state-based} columns of the class.
     */
    @SuppressWarnings("OverlyBroadCatchBlock")   /* Treating all exceptions equally. */
    StateColumns<S> stateColumns() {
        var stateClass = entityClass.stateClass();
        var columnClass = findColumnsClass(stateClass);
        if (columnClass == null) {
            return StateColumns.none();
        }
        try {
            var getDefinitions = columnClass.getDeclaredMethod(COL_DEFS_METHOD_NAME);
            @SuppressWarnings("unchecked")  // ensured by the Spine code generation.
            var columns = (Set<EntityColumn<S, ?>>) getDefinitions.invoke(null);
            return new StateColumns<>(columns);
        } catch (Exception e) {
            throw newIllegalStateException(
                    e,
                    "Error fetching the declared columns by invoking the `%s.%s()` method" +
                            " of the entity state type `%s`.",
                    COLS_NESTED_CLASSNAME, COL_DEFS_METHOD_NAME, stateClass.getName());
        }
    }

    /**
     * Finds the {@code Column} class which is generated the messages representing the entity
     * state type.
     *
     * <p>If an entity has no such class generated, it does not declare any columns. In this
     * case, this method returns {@code null}.
     *
     * @param stateClass
     *         the class of the entity state to look for the method in
     * @return the class declaring the entity columns,
     *         or {@code null} if the entity declares no columns
     */
    private static @Nullable Class<?> findColumnsClass(Class<? extends EntityState<?>> stateClass) {
        var innerClasses = stateClass.getDeclaredClasses();
        Class<?> columnClass = null;
        for (var aClass : innerClasses) {
            if (COLS_NESTED_CLASSNAME.equals(aClass.getSimpleName())) {
                columnClass = aClass;
            }
        }
        return columnClass;
    }
}
