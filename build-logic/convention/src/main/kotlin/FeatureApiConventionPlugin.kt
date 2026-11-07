import com.gabriel.cinelog.convention.library
import com.gabriel.cinelog.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class FeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("cinelog.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                add("api", libs.library("androidx-navigation3-runtime"))
                add("implementation", libs.library("kotlinx-serialization-core"))
            }
        }
    }
}
