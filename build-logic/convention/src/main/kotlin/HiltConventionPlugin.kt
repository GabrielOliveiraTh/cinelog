import com.gabriel.cinelog.convention.library
import com.gabriel.cinelog.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")

            dependencies {
                add("implementation", libs.library("hilt-android"))
                add("ksp", libs.library("hilt-compiler"))
            }

            // The Hilt Gradle plugin is only needed where the component graph is generated.
            pluginManager.withPlugin("com.android.application") {
                pluginManager.apply("com.google.dagger.hilt.android")
            }
        }
    }
}
