# Remote Compose document server

이 디렉터리는 독립 Gradle build다. Android app project를 구성하거나 빌드하지 않고 Ktor server만 빌드·테스트·실행할 수 있다.

```bash
./gradlew build
./gradlew run
```

실행 distribution이 필요하면 다음 명령을 사용한다.

```bash
./gradlew installDist
./build/install/server/bin/server
```

상위 `remote-state-lab`은 이 프로젝트를 Gradle composite build로 포함하므로 기존 명령도 유지된다.

```bash
# remote-state-lab 디렉터리에서
./gradlew :server:build
./gradlew :server:run
```

Remote Compose, Kotlin, Ktor version은 독립 실행을 위해 [`gradle/libs.versions.toml`](gradle/libs.versions.toml)에 선언한다. 상위 build의 version을 변경할 때 이 catalog도 함께 갱신해야 한다.
