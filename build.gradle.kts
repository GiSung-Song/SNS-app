plugins {
	java
	id("org.springframework.boot") version "3.3.12"
	id("io.spring.dependency-management") version "1.1.7"
}

val querydslVersion = "5.0.0"
val generatedDir = "src/main/generated"

group = "com.outsta"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
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
	// Batch
	implementation("org.springframework.boot:spring-boot-starter-batch")

	// JPA
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// Web
	implementation("org.springframework.boot:spring-boot-starter-web")

	// Validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// AOP
	implementation("org.springframework.boot:spring-boot-starter-aop")

	// Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// logstash Logback Encoder
	implementation("net.logstash.logback:logstash-logback-encoder:7.4")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// Jackson for Java Time (LocalDateTime 등)
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	// java mail sender
	implementation("org.springframework.boot:spring-boot-starter-mail")

	// QueryDSL
	implementation("com.querydsl:querydsl-jpa:${querydslVersion}:jakarta")
	annotationProcessor("com.querydsl:querydsl-apt:${querydslVersion}:jakarta")
	annotationProcessor("jakarta.annotation:jakarta.annotation-api")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")

	testImplementation("org.springframework.boot:spring-boot-testcontainers")

	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql:1.17.6")

	testImplementation("org.springframework.boot:spring-boot-starter-data-redis")

	testImplementation("org.springframework.security:spring-security-test")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.mysql:mysql-connector-j")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.batch:spring-batch-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
	options.generatedSourceOutputDirectory.set(file(generatedDir))
	options.compilerArgs.add("-parameters")
}

sourceSets {
	named("main") {
		java.srcDir(generatedDir)
	}
}

tasks.named<Delete>("clean") {
	delete(file(generatedDir))
}

tasks.withType<Test> {
	useJUnitPlatform()
}
