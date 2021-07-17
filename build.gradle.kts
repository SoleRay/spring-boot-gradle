plugins {
    id("org.springframework.boot") version "2.4.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    java
}

group = "com.sole.ray"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations {
    "implementation" {
        exclude("org.springframework.boot","spring-boot-starter-logging")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("com.thoughtworks.xstream:xstream:1.4.17")
    implementation("org.apache.httpcomponents:httpclient:4.5.3")
    implementation("mysql:mysql-connector-java:5.1.40")
    implementation("com.alibaba:druid:1.2.5")
    implementation("com.alibaba:fastjson:1.2.38")
    implementation("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    implementation("tk.mybatis:mapper-spring-boot-starter:2.1.5")
    implementation("org.mybatis:mybatis-typehandlers-jsr310:1.0.2")
    implementation("commons-codec:commons-codec:1.15")
    implementation("commons-io:commons-io:2.11.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

