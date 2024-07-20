plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jib)
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation(project(":stub"))

    //mail notifications
    implementation("org.simplejavamail:simple-java-mail:8.11.2")

    runtimeOnly(libs.grpc.netty)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.grpc.testing)
}

tasks.register<JavaExec>("MailServer") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.grpc.examples.mail_server.MailServerKt")
}


val mailServerStartScripts =
    tasks.register<CreateStartScripts>("mailServerStartScripts") {
        mainClass.set("io.grpc.examples.mail_server.MailServerKt")
        applicationName = "mail-server"
        outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
        classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
    }

tasks.named("startScripts") {
    dependsOn(mailServerStartScripts)
}

tasks.withType<Test> {
    useJUnit()

    testLogging {
        events =
            setOf(
                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

jib {
    container {
        mainClass = "io.grpc.examples.mail_server.MailServerKt"
    }
}

// Required by the 'shadowJar' task
project.setProperty("mainClassName", "io.grpc.examples.mail_server.MailServerKt")

tasks.jar {
    manifest.attributes["Main-Class"] = "io.grpc.examples.mail_server.MailServerKt"
}
