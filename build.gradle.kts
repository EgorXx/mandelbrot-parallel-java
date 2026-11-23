plugins {
    java
}

group = "mandelbrot"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.register<JavaExec>("benchmark") {
    group = "application"
    description = "Measure all renderers and write benchmark results to CSV."
    dependsOn(tasks.test)
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("mandelbrot.benchmark.BenchmarkRunner")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })
    maxHeapSize = "1g"
}
