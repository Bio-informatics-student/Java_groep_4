plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()
}


dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Source: https://mvnrepository.com/artifact/info.picocli/picocli
    implementation("info.picocli:picocli:4.7.7")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "Main"
    }
}