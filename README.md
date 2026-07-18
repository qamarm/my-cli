# my-cli

An example Java CLI built with [picocli](https://picocli.info/), packaged as an executable fat jar.

## Requirements

- Java 17 (managed via [mise](https://mise.jdx.dev/) — see `mise.toml`)
- Maven (also managed via mise)

If you have mise installed, just run `mise install` in this directory to get the pinned Java and Maven versions.

## Build

```bash
mvn clean package
```

This produces `target/my-cli.jar`, an executable fat jar with all dependencies bundled.

## Run

```bash
java -jar target/my-cli.jar --help
java -jar target/my-cli.jar --version
java -jar target/my-cli.jar greet --name World
```

## Test

```bash
mvn test
```

## Project structure

```
src/main/java/com/example/mycli/MyCli.java              # top-level command, entry point
src/main/java/com/example/mycli/command/GreetCommand.java  # example "greet" subcommand
src/test/java/com/example/mycli/command/GreetCommandTest.java
pom.xml                                                  # Maven build, picocli dep, shade plugin for fat jar
```

## Adding a new subcommand

1. Create a new class in `com.example.mycli.command` implementing `Callable<Integer>` (or `Runnable`), annotated with `@Command`.
2. Register it in the `subcommands` array of `@Command` on `MyCli`.
