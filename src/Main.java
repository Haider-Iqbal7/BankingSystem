import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // Check for command line arg
        if (args.length > 0 && args[0].equalsIgnoreCase("console")) {
            new ConsoleUI().start();
            return;
        }

        // Ask user: Console or GUI?
        String[] options = {"GUI Mode (Swing)", "Console Mode", "Exit"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Welcome to SmartBank Management System!\n\nPlease select the interface:",
                "SmartBank - Mode Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        switch (choice) {
            case 0 -> BankGui.launch();
            case 1 -> new ConsoleUI().start();
            default -> System.exit(0);
        }
    }
}

