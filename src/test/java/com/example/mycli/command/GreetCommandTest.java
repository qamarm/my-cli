package com.example.mycli.command;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreetCommandTest {

    @Test
    void buildGreetingUsesGivenName() {
        assertEquals("Hello, Alice!", GreetCommand.buildGreeting("Alice"));
    }

    @Test
    void defaultsToWorldWhenNoNameOptionGiven() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            new CommandLine(new GreetCommand()).execute();
        } finally {
            System.setOut(originalOut);
        }
        assertTrue(out.toString().contains("Hello, World!"));
    }

    @Test
    void printsGreetingForNameOption() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            new CommandLine(new GreetCommand()).execute("--name", "Bob");
        } finally {
            System.setOut(originalOut);
        }
        assertTrue(out.toString().contains("Hello, Bob!"));
    }
}
