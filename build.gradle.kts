plugins {
    id("java")
}

group = "com.coloryr"
version = "1.0"


repositories {
    mavenCentral()
}

dependencies {
    compileOnly(fileTree("libs") { include("*.jar") })
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "coloryr.optifinewrapper.Main"
    }
}