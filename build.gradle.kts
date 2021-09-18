import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    id("maven-publish")
}

group = "bot.zerotwo"
version = "0.1.0"


configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val kotlinX = "1.5.2-native-mt" // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
dependencies {
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.21")

    // Coroutine utils
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinX")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinX")

    // Serializer
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    // Amqp
    implementation("com.rabbitmq:amqp-client:5.9.0")

    // Kord
    //implementation("dev.kord:kord-core:0.8.0-M5")
    implementation("dev.kord:kord-core:undefined")
}

tasks.test {
    useJUnitPlatform()
}


val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks {
    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }
    withType(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}


publishing {
    repositories {
        mavenLocal()
        /*
        maven {
            url = uri("https://nexus.zerotwo.bot/repository/maven-releases/")
            credentials {
                username = property("publishUsername").toString()
                password = property("publishPassword").toString()
            }
        }
         */
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = groupId
            artifactId = artifactId
            version = version

            from(components["kotlin"])
        }
    }
}