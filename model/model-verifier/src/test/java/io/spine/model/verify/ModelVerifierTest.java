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

package io.spine.model.verify;

import com.google.common.io.Files;
import io.spine.model.CommandHandlers;
import io.spine.model.verify.ModelVerifier.GetDestinationDir;
import io.spine.model.verify.given.DuplicateCommandHandler;
import io.spine.model.verify.given.EditAggregate;
import io.spine.model.verify.given.RenameProcMan;
import io.spine.model.verify.given.UploadCommandHandler;
import io.spine.server.model.DuplicateCommandHandlerError;
import io.spine.testing.logging.MuteLogging;
import org.gradle.api.Project;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.internal.impldep.com.google.common.collect.Iterators;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;

import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ModelVerifier should")
class ModelVerifierTest {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private Project project = null;

    static Project actualProject() {
        Project result = ProjectBuilder.builder().build();
        result.getPluginManager().apply("java");
        return result;
    }

    @SuppressWarnings("unchecked") // OK for test mocks.
    @BeforeEach
    void setUp() {
        project = mock(Project.class);
        ScriptHandler buildScript = mock(ScriptHandler.class);
        when(buildScript.getClassLoader()).thenReturn(ModelVerifierTest.class.getClassLoader());
        when(project.getSubprojects()).thenReturn(emptySet());
        when(project.getRootProject()).thenReturn(project);
        when(project.getBuildscript()).thenReturn(buildScript);

        TaskContainer tasks = mock(TaskContainer.class);
        TaskCollection emptyTaskCollection = mock(TaskCollection.class);
        when(emptyTaskCollection.iterator()).thenReturn(Iterators.emptyIterator());
        when(emptyTaskCollection.toArray()).thenReturn(EMPTY_ARRAY);
        when(tasks.withType(any(Class.class))).thenReturn(emptyTaskCollection);
        when(project.getTasks()).thenReturn(tasks);
    }

    @Test
    @DisplayName("verify model from classpath")
    void verifyModel() {
        ModelVerifier verifier = new ModelVerifier(project);

        verify(project).getSubprojects();

        String commandHandlerTypeName = UploadCommandHandler.class.getName();
        String aggregateTypeName = EditAggregate.class.getName();
        String procManTypeName = RenameProcMan.class.getName();
        CommandHandlers spineModel = CommandHandlers
                .newBuilder()
                .addCommandHandlingTypes(commandHandlerTypeName)
                .addCommandHandlingTypes(aggregateTypeName)
                .addCommandHandlingTypes(procManTypeName)
                .build();
        verifier.verify(spineModel);
    }

    @Test
    @DisplayName("fail on duplicate command handlers")
    void failOnDuplicateHandlers() {
        ModelVerifier verifier = new ModelVerifier(project);
        String firstType = UploadCommandHandler.class.getName();
        String secondType = DuplicateCommandHandler.class.getName();

        CommandHandlers spineModel = CommandHandlers
                .newBuilder()
                .addCommandHandlingTypes(firstType)
                .addCommandHandlingTypes(secondType)
                .build();
        assertThrows(DuplicateCommandHandlerError.class, () -> verifier.verify(spineModel));
    }

    @Test
    @MuteLogging
    @DisplayName("ignore invalid class names")
    void ignoreInvalidClassNames() {
        String invalidClassname = "non.existing.class.Name";
        CommandHandlers spineModel = CommandHandlers
                .newBuilder()
                .addCommandHandlingTypes(invalidClassname)
                .build();
        new ModelVerifier(project).verify(spineModel);
    }

    @Test
    @DisplayName("not accept non-CommandHandler types")
    void rejectNonHandlerTypes() {
        String invalidClassname = ModelVerifierTest.class.getName();
        CommandHandlers spineModel = CommandHandlers
                .newBuilder()
                .addCommandHandlingTypes(invalidClassname)
                .build();
        assertThrows(IllegalArgumentException.class,
                     () -> new ModelVerifier(project).verify(spineModel));
    }

    @Test
    @DisplayName("retrieve compilation destination directory from task")
    void getCompilationDestDir() throws MalformedURLException {
        JavaCompile compileTask = actualProject().getTasks()
                                                 .withType(JavaCompile.class)
                                                 .getByName(COMPILE_JAVA.getValue());
        File dest = Files.createTempDir();
        compileTask.setDestinationDir(dest);
        Function<JavaCompile, URL> func = GetDestinationDir.FUNCTION;
        URL destUrl = dest.toURI().toURL();
        assertEquals(destUrl, func.apply(compileTask));
    }

    @Test
    @DisplayName("retrieve null if destination directory is null")
    void getNullDestDir() {
        JavaCompile compileTask = mock(JavaCompile.class);
        Function<JavaCompile, URL> func = GetDestinationDir.FUNCTION;
        assertNull(func.apply(compileTask));
    }
}
