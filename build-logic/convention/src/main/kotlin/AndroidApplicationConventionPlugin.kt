import com.android.build.api.dsl.ApplicationExtension
import com.gabriel.cinelog.convention.AndroidConfig
import com.gabriel.cinelog.convention.configureDetekt
import com.gabriel.cinelog.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            configureDetekt()

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = AndroidConfig.TARGET_SDK
            }
        }
    }
}
