plugins {
    id("java")
}

group = "au.hardy.nicholas"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.google.code.gson:gson:+")
}

tasks.test {
    useJUnitPlatform()
}