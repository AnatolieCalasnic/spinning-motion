plugins {
    java
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    id ("org.sonarqube") version "5.1.0.4882"
    id ("jacoco")
}
val javaVersion = 21

val flywayVersion = "9.22.3"
val mysqlConnectorVersion = "8.2.0"

val springSecurityTestVersion = "6.3.1"
val jjwtVersion = "0.11.5"

val stripeVersion = "24.0.0"
val springDotenvVersion = "4.0.0"
val sockjsVersion = "1.5.1"
val stompWebSocketVersion = "2.3.4"

val lombokVersion = "1.18.32"

val h2DatabaseVersion = "2.2.224"
val junitPlatformVersion = "1.10.2"
group = "org.myexample"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Data and Web
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database Migration
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-mysql:$flywayVersion")

    // Database Connectivity
    implementation("com.mysql:mysql-connector-j:$mysqlConnectorVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // Payment Integration
    implementation("com.stripe:stripe-java:$stripeVersion")
    //
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    // Environment Configuration
    implementation("me.paulschwarz:spring-dotenv:$springDotenvVersion")

    // WebSocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.webjars:sockjs-client:$sockjsVersion")
    implementation("org.webjars:stomp-websocket:$stompWebSocketVersion")

    //Email
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf:thymeleaf-spring6")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test:$springSecurityTestVersion")
    testRuntimeOnly("com.h2database:h2:$h2DatabaseVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
}

sonar {
    properties {
        property("sonar.projectKey", "spinning-motion")
        property("sonar.projectName", "spinning-motion")
        property("sonar.host.url", project.findProperty("sonarHostUrl")?.toString() ?: "")
        property("sonar.token", project.findProperty("sonarToken")?.toString() ?: "")
        property("sonar.qualitygate.wait", "true")
        property("sonar.exclusions", listOf(
            "**/domain/**",
            "**/controller/**",
            "**/persistence/**",
            "**/configuration/**",
            "**/SpinningMotionApplication.*"
        ))

        // Excluding domain layer and include only business layer
        property("sonar.coverage.exclusions", listOf(
            "**/domain/**",
            "**/configuration/**",
            "**/controller/**",
            "**/persistence/**",
            "**/SpinningMotionApplication.*"
        ))

        property("sonar.coverage.inclusions", listOf(
            "**/business/**"
        ))
        property("sonar.coverage.jacoco.xmlReportPaths", "$rootDir/build/reports/jacoco/test/jacocoTestReport.xml")
    }
}
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    systemProperties(System.getProperties().entries.associate { it.key.toString() to it.value })
    environment(mapOf(
        "STRIPE_SECRET_KEY" to (project.findProperty("STRIPE_SECRET_KEY")?.toString() ?: ""),
        "STRIPE_PUBLISHABLE_KEY" to (project.findProperty("STRIPE_PUBLISHABLE_KEY")?.toString() ?: ""),
        "STRIPE_WEBHOOK_SECRET" to (project.findProperty("STRIPE_WEBHOOK_SECRET")?.toString() ?: "")
    ))
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // Tests are required before generating report
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
    classDirectories.setFrom(files(classDirectories.files.map { file ->
        fileTree(file) {
            exclude(
                "**/domain/**",
                "**/configuration/**",
                "**/controller/**",
                "**/persistence/**",
                "**/SpinningMotionApplication.*"
            )
            include(
                "**/business/**"
            )
        }
    }))
}
tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
tasks.sonar {
    dependsOn(tasks.jacocoTestReport)
}