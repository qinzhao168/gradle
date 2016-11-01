/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests

import groovy.transform.NotYetImplemented
import org.gradle.api.CircularReferenceException
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.CrossVersionIntegrationSpec
import org.gradle.integtests.fixtures.executer.DefaultGradleDistribution
import org.gradle.integtests.fixtures.executer.GradleContextualExecuter
import org.gradle.integtests.fixtures.executer.GradleDistribution
import org.gradle.integtests.fixtures.executer.GradleExecuter
import org.gradle.integtests.fixtures.executer.ReleasedGradleDistribution
import org.gradle.integtests.fixtures.executer.UnderDevelopmentGradleDistribution
import org.gradle.integtests.fixtures.versions.ReleasedVersionDistributions
import org.gradle.test.fixtures.file.TestFile
import org.gradle.util.GradleVersion
import spock.lang.Ignore
import spock.lang.Issue

import static org.hamcrest.Matchers.startsWith

public class TaskExecutionCrossVersionIntegrationTest extends CrossVersionIntegrationSpec {

    @NotYetImplemented
    @Issue("gradle/gradle#783")
    def "current Gradle honours task order with finalizer of Gradle 3.2-rc-1"() {
        GradleExecuter executerRc1 = version(new ReleasedGradleDistribution("3.2-rc-1", temporaryFolder.createDir(".gradle-3.2-rc-1")))
        GradleExecuter executerLatest = version(new UnderDevelopmentGradleDistribution())

        buildFile << """
            project("c") {
                task jar
            }

            project("b") {
                task jar {
                  dependsOn "compileJava"
                }
                task compileJava {
                  dependsOn ":c:jar"
                  finalizedBy "copyMainKotlin"
                }
                task copyMainKotlin
            }

            task build {
              dependsOn ":b:jar"
              dependsOn ":c:jar"
            }
        """
        settingsFile << "include 'b', 'c'"

        when:
        def resultRc1 = executerRc1.withTasks("build").run()
        def resultLatest = executerLatest.withTasks("build").run()

        then:
        resultRc1.executedTasks == resultLatest.executedTasks
    }
}
