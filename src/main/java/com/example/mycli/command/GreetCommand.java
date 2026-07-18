package com.example.mycli.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
        name = "greet",
        mixinStandardHelpOptions = true,
        description = "Prints a greeting."
)
public class GreetCommand implements Callable<Integer> {

    @Option(names = {"-n", "--name"}, description = "Name to greet.", defaultValue = "World")
    private String name;

    @Override
    public Integer call() {
        System.out.println(buildGreeting(name));
        return 0;
    }

    public static String buildGreeting(String name) {
        return "Hello, " + name + "!";
    }
}
