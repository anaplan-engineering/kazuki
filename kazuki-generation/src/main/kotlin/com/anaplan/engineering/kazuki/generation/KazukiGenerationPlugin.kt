package com.anaplan.engineering.kazuki.generation

import com.squareup.kotlinpoet.ClassName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import java.io.File

open class KazukiGenerationPluginExtension @javax.inject.Inject constructor(@Suppress("unused") objectFactory: ObjectFactory) {
    var generatedSrc: String = "generatedSrc"
}


internal const val RootPackageName = "com.anaplan.engineering.kazuki.core"
internal const val InternalPackageName = "$RootPackageName.internal"
internal val PrettyPrintableInterfaceName = ClassName(RootPackageName, "PrettyPrintable")
internal val TupleInterfaceName = ClassName(RootPackageName, "Tuple")
internal val KazukiLogName = ClassName(RootPackageName, "Kazuki.Log")
internal val CacheKeyName = ClassName(InternalPackageName, "_CacheKey")
internal val EvaluationCacheName = ClassName(InternalPackageName, "_EvaluationCache")
internal val EvaluationProfilerName = ClassName(InternalPackageName, "_EvaluationProfiler")

internal val PrettyFunctionName = "pretty"
internal val PrettyOrDefaultFunctionName = "_prettyOrDefault"

class KazukiGenerationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // TODO -- get this working!?
        project.afterEvaluate {
            val sourceSets = it.extensions.getByType(JavaPluginExtension::class.java).sourceSets
            sourceSets.getByName("main").java.srcDirs.add(project.layout.buildDirectory.dir("generated/kazuki/main/kotlin").get().asFile)
            sourceSets.getByName("test").java.srcDirs.add(project.layout.buildDirectory.dir("generated/kazuki/test/kotlin").get().asFile)
        }
        project.extensions.create("kazuki", KazukiGenerationPluginExtension::class.java, project.objects)
        project.createKazukiTask("generateFunctions", FunctionGeneratorTask::class.java)
        project.createKazukiTask("generateTuples", TupleGeneratorTask::class.java)
        project.createKazukiTask("generateRecordTests", RecordTestGeneratorTask::class.java)
    }
}

internal const val kazukiTaskGroup = "kazuki"

internal fun Project.createKazukiTask(name: String, type: Class<out Task>) =
    tasks.register(name, type).configure { group = kazukiTaskGroup }

internal fun Project.generationSrcDir() = layout.buildDirectory.dir("generated/kazuki/main/kotlin").get().asFile
internal fun Project.generationTestSrcDir() = layout.buildDirectory.dir("generated/kazuki/test/kotlin").get().asFile
