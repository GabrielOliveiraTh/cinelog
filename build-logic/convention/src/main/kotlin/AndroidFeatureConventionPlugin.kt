import com.android.build.api.dsl.LibraryExtension
import com.gabriel.cinelog.convention.library
import com.gabriel.cinelog.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("cinelog.android.library")
            pluginManager.apply("cinelog.android.compose")
            pluginManager.apply("cinelog.hilt")

            extensions.configure<LibraryExtension> {
                defaultConfig.testInstrumentationRunner = "com.gabriel.cinelog.core.testing.HiltTestRunner"
            }

            dependencies {
                add("implementation", project(":core:data"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:ui"))

                add("implementation", libs.library("androidx-activity-compose"))
                add("implementation", libs.library("androidx-lifecycle-runtime-compose"))
                add("implementation", libs.library("androidx-lifecycle-runtime-ktx"))
                add("implementation", libs.library("androidx-lifecycle-viewmodel-compose"))
                add("implementation", libs.library("androidx-hilt-lifecycle-viewmodel-compose"))
                add("implementation", libs.library("androidx-navigation3-runtime"))

                add("testImplementation", project(":core:testing"))
                add("testImplementation", libs.library("junit"))
                add("testImplementation", libs.library("kotlinx-coroutines-test"))
                add("testImplementation", libs.library("hilt-android-testing"))
                add("kspTest", libs.library("hilt-compiler"))

                add("androidTestImplementation", project(":core:testing"))
                add("androidTestImplementation", libs.library("androidx-test-ext-junit"))
                add("androidTestImplementation", libs.library("androidx-test-runner"))
                add("androidTestImplementation", libs.library("androidx-compose-ui-test-junit4"))
                add("androidTestImplementation", libs.library("hilt-android-testing"))
                add("kspAndroidTest", libs.library("hilt-compiler"))
                add("debugImplementation", libs.library("androidx-compose-ui-test-manifest"))
            }
        }
    }
}
