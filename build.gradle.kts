import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val avroVersion = "1.8.2"
val confluentVersion = "5.0.0"
val coroutinesVersion = "1.0.0"
val cxfVersion = "3.2.5"
val jacksonVersion = "2.9.6"
val kafkaVersion = "2.0.0"
val kafkaEmbeddedVersion = "2.0.2"
val kluentVersion = "1.48"
val ktorVersion = "1.1.2"
val logstashEncoderVersion = "5.2"
val logbackVersion = "1.2.3"
val mockitoKotlinVersion = "2.0.0-RC1"
val prometheusVersion = "0.5.0"
val spekVersion = "2.0.0"

plugins {
    //id("org.jmailen.kotlinter") version "1.14.0"
    id("org.jetbrains.kotlin.jvm") version "1.3.20"
    id("com.diffplug.gradle.spotless") version "3.18.0"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

group = "no.nav.syfo"
version = "1.0-SNAPSHOT"

tasks.withType<Jar> {
    manifest.attributes["MainClass"] = "no.nav.syfo.BootstrapKt"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
    maven { url = uri("http://packages.confluent.io/maven/") }
    maven { url = uri("https://repo.adeo.no/repository/maven-releases/") }
    maven { url = uri("https://dl.bintray.com/spekframework/spek-dev") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("spek2")
    }
    testLogging {
        showStandardStreams = true
    }
}

tasks.create("printVersion") {
    doLast {
        println(version)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(project(":syfooppgave-schemas"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")

    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-auth-basic:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")

    implementation("io.confluent:kafka-avro-serializer:$confluentVersion")
    implementation("io.confluent:kafka-streams-avro-serde:$confluentVersion")
    implementation("org.apache.avro:avro:$avroVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    implementation("org.apache.kafka:kafka_2.12:$kafkaVersion")
    implementation("org.apache.kafka:kafka-streams:$kafkaVersion")

    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("no.nav:kafka-embedded-env:$kafkaEmbeddedVersion")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion")

    testRuntimeOnly("org.spekframework.spek2:spek-runtime-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
}