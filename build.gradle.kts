plugins {
    java
    application
}

group = "onesv.fun"
version = "1.43.7"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("Game/src"))
            exclude("**/test/**")
        }
    }
    test {
        java {
            setSrcDirs(listOf("Game/src/test/java"))
        }
        resources {
            setSrcDirs(listOf("Game/src/test/resources"))
        }
    }
}

dependencies {
    implementation("com.mysql:mysql-connector-j:9.0.0")
    implementation("joda-time:joda-time:2.12.7")
    implementation("org.scijava:jep:2.4.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("estaticos.MainServidor")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "estaticos.MainServidor"
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.withType<JavaCompile> {
    // UTF-8: fuentes en español para comunidad latina (ñ, á, é, í, ó, ú correctos)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:-options", "-Xlint:-deprecation"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runGame") {
    group = "application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("estaticos.MainServidor")
    jvmArgs = listOf("-Xms512m", "-Xmx1024m")
}
