plugins {
    `java-library`
}

dependencies {
    api(project(":libs:backend:common"))
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
