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

import io.spine.internal.dependency.AutoService
import io.spine.internal.dependency.Grpc
import io.spine.internal.dependency.Kotlin
import io.spine.internal.dependency.Spine

plugins {
    `java-test-fixtures`
    `detekt-code-analysis`
}

dependencies {
    api(Kotlin.reflect)
    api(Grpc.protobuf)
    api(Grpc.core)
    api(Grpc.stub)
    api(project(":client"))

    val spine = Spine(project)
    /*
     * Expose Validation API at the server side. E.g. for tuning custom validation for
     * anti-corruption layer.
     */
    api(spine.validation.runtime)

    with(AutoService) {
        testAnnotationProcessor(processor)
        testCompileOnly(annotations)
    }
    testImplementation(Grpc.nettyShaded)

    testImplementation(spine.testlib)
    testImplementation(spine.baseTypes)

    testFixturesImplementation(spine.testlib)

    testImplementation(project(path = ":core", configuration = "testArtifacts"))
    testImplementation(project(path = ":client", configuration = "testArtifacts"))
    testImplementation(project(":testutil-server"))
}

// Copies the documentation files to the Javadoc output folder.
// Inspired by https://discuss.gradle.org/t/do-doc-files-work-with-gradle-javadoc/4673
tasks.javadoc {
    doLast {
        copy {
            from("src/main/docs")
            into("$buildDir/docs/javadoc")
        }
    }
}
