plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus") version "1.12.1.Final"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":common"))
    implementation(project(":html-client"))

    implementation(platform("io.quarkus:quarkus-bom:1.12.1.Final"))
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    runtimeOnly("io.quarkus:quarkus-container-image-jib")

    implementation("io.quarkus:quarkus-reactive-pg-client")

    // todo: testImpl
    implementation("org.testcontainers:postgresql:1.15.1")
    runtimeOnly("org.postgresql:postgresql:42.2.6") // why is this transitive for micronaut-server?
}


/*
tasks.withType<io.quarkus.gradle.tasks.QuarkusDev> {

}
 */

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        javaParameters = true
    }
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
}

tasks.create<io.quarkus.gradle.tasks.QuarkusBuild>("dockerBuild") {
    doFirst {
        System.setProperty("quarkus.container-image.build", "true")
        System.setProperty("quarkus.container-image.group", "")
        System.setProperty("quarkus.container-image.tag", "latest")
    }
}

tasks.create<io.quarkus.gradle.tasks.QuarkusBuild>("nativeDockerBuild") {
    doFirst {
        System.setProperty("quarkus.package.type", "native")
        System.setProperty("quarkus.native.container-build", "true")
        //System.setProperty("quarkus.native.resources.includes", "assets/**,webjars/**")
        System.setProperty("quarkus.native.additional-build-args", "-H:ReflectionConfigurationFiles=reflection-config.json,-H:ResourceConfigurationFiles=resources-config.json")
        System.setProperty("quarkus.container-image.build", "true")
        System.setProperty("quarkus.container-image.name", "quarkus-server-native")
        System.setProperty("quarkus.container-image.group", "")
        System.setProperty("quarkus.container-image.tag", "latest")
    }
}