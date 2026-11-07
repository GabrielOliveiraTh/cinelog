import com.android.build.api.dsl.CommonExtension
import com.gabriel.cinelog.convention.library
import com.gabriel.cinelog.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            // Works regardless of whether this plugin is applied before or after the Android one.
            listOf("com.android.application", "com.android.library").forEach { androidPlugin ->
                pluginManager.withPlugin(androidPlugin) {
                    extensions.getByType<CommonExtension>().buildFeatures.compose = true
                }
            }

            dependencies {
                val bom = platform(libs.library("androidx-compose-bom"))
                add("implementation", bom)
                add("androidTestImplementation", bom)

                add("implementation", libs.library("androidx-compose-ui"))
                add("implementation", libs.library("androidx-compose-ui-tooling-preview"))
                add("implementation", libs.library("androidx-compose-material3"))
                add("debugImplementation", libs.library("androidx-compose-ui-tooling"))
            }
        }
    }
}
