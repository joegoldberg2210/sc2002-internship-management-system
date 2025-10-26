package ui;

import java.util.Scanner;

public final class ConsoleUI {
    public static final int WIDTH = 50; // change once, all screens follow

    private ConsoleUI() {}

    public static void loginBox(String title) {
        System.out.println();
        int width = 40; 
        int padding = (width - 2 - title.length()) / 2;

        System.out.println("+" + "-".repeat(width - 2) + "+");
        System.out.println("|" + " ".repeat(padding) + title + " ".repeat(width - 2 - title.length() - padding) + "|");
        System.out.println("+" + "-".repeat(width - 2) + "+");
    }

    public static void bigBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                                      ║");
        System.out.println("║                          ██╗██████╗ ███╗   ███╗███████╗                              ║");
        System.out.println("║                          ██║██╔══██╗████╗ ████║██╔════╝                              ║");
        System.out.println("║                          ██║██████╔╝██╔████╔██║███████╗                              ║");
        System.out.println("║                          ██║██╔═══╝ ██║╚██╔╝██║╚════██║                              ║");
        System.out.println("║                          ██║██║     ██║ ╚═╝ ██║███████║                              ║");
        System.out.println("║                          ╚═╝╚═╝     ╚═╝     ╚═╝╚══════╝                              ║");
        System.out.println("║                                                                                      ║");
        System.out.println("║             Welcome to Internship Placement Management System (IPMS)!                ║");
        System.out.println("║                                                                                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    public static void sectionHeader(String title) {
        int width = 50;
        int paddingLeft = 2;
        int innerWidth = width - 2;

        String top = "╔" + "═".repeat(innerWidth) + "╗";
        String bottom = "╚" + "═".repeat(innerWidth) + "╝";

        String line = "║" + " ".repeat(paddingLeft) + title;
        if (line.length() > width - 1)
            line = line.substring(0, width - 1);
        line += " ".repeat(width - 1 - line.length()) + "║";
        
        System.out.println();
        System.out.println(top);
        System.out.println(line);
        System.out.println(bottom);
    }

    public static void repeat(char ch, int n) {
        for (int i = 0; i < n; i++) System.out.print(ch);
    }
}