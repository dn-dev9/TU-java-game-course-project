package io;

import engine.Session;
import java.util.Scanner;

/**
 * Reads player input from the input stream and forwards each line to the
 * Session for processing
 */
public class CommandParser {

    private final Session session;
    private final Scanner scanner;

    public CommandParser(Session session) {
        this.session = session;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the  game loop
     */
    public void run() {
        System.out.println("*******************************");
        System.out.println("*     DUNGEONS & DRAGONS      *");
        System.out.println("*******************************");
        System.out.println("Type 'help' for a list of commands.");
        System.out.println("Type 'open <file>' to begin.");
        System.out.println();

        while (session.isRunning()) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            String response = session.process(line);
            if (response != null && !response.isEmpty()) {
                System.out.println(response);
            }
            System.out.println();
        }

        scanner.close();
    }
}
