package com.example.mycli;

import com.example.mycli.command.GreetCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "my-cli",
        mixinStandardHelpOptions = true,
        version = "my-cli 1.0.0",
        description = "An example Java CLI built with picocli.",
        subcommands = {
                GreetCommand.class
        }
)
public class MyCli implements Runnable {

    @Override
    public void run() {
        // No subcommand given: show usage.
        new CommandLine(this).usage(System.out);
    }

    static int execute(String[] args) {
        return new CommandLine(new MyCli()).execute(args);
    }

    @Generated
    public static void main(String[] args) {
        System.exit(execute(args));
    }
}
