package com.example.mycli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyCliTest {

    @Test
    void runsGreetSubcommand() {
        int exitCode = MyCli.execute(new String[] {"greet", "--name", "Alice"});
        assertEquals(0, exitCode);
    }

    @Test
    void printsUsageWhenNoSubcommandGiven() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            MyCli.execute(new String[] {});
        } finally {
            System.setOut(originalOut);
        }
        assertTrue(out.toString().contains("Usage:"));
    }
}
