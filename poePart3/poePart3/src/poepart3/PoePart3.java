
package poepart3;

import java.util.Scanner;

/**
 * Main application for ChatApp Part 3.
 */
public class PoePart3 {

    public static void main(String[] args) {

        try (Scanner scan = new Scanner(System.in)) {

            System.out.println("=================================");
            System.out.println("       CHATAPP REGISTRATION");
            System.out.println("=================================");

            System.out.print("Please enter your first name: ");
            String firstName = scan.nextLine();
            System.out.println("First name successfully captured.");

            System.out.print("Please enter your last name: ");
            String lastName = scan.nextLine();
            System.out.println("Last name successfully captured.");

            System.out.print("Please enter the username: ");
            String username = scan.nextLine();
            System.out.println("Username successfully captured.");

            System.out.print("Please enter the password: ");
            String password = scan.nextLine();
            System.out.println("Password successfully captured.");

            System.out.print("Please enter your South African cell number (+27...): ");
            String phone = scan.nextLine();
            System.out.println("Cell number successfully captured.");

            Registration registration = new Registration(
                    username, password, phone, firstName, lastName);

            System.out.println(registration.usernameMessage());
            System.out.println(registration.passwordMessage());
            System.out.println(registration.cellPhoneMessage());

            if (!registration.checkUserName()
                    || !registration.checkPasswordComplexity()
                    || !registration.checkCellPhoneNumber()) {

                System.out.println(
                        "Registration unsuccessful. Please correct the information above.");
                return;
            }

            System.out.println("User has been registered successfully.");

            // LOGIN
            Login login = new Login(username, password);

            System.out.print("\nEnter your username: ");
            String loginUsername = scan.nextLine();

            System.out.print("Enter your password: ");
            String loginPassword = scan.nextLine();

            boolean loggedIn = login.loginUser(
                    loginUsername, loginPassword);

            System.out.println(
                    login.returnLoginStatus(firstName, lastName));

            if (!loggedIn) {
                return;
            }

            System.out.println("\nWelcome to QuickChat!");

            System.out.print(
                    "How many messages would you like to process? ");

            int requested;

            try {
                requested = Integer.parseInt(scan.nextLine().trim());

                if (requested < 0) {
                    System.out.println(
                            "Please enter zero or a positive number.");
                    return;
                }

            } catch (NumberFormatException exception) {
                System.out.println("Please enter a whole number.");
                return;
            }

            int completed = 0;

            // MAIN MENU
            while (true) {

                System.out.println("\n========== QUICKCHAT MENU ==========");
                System.out.println("1) Send Messages");
                System.out.println("2) Show recently sent messages");
                System.out.println("3) Stored message management");
                System.out.println("4) Quit");
                System.out.print("Select an option: ");

                String option = scan.nextLine().trim();

                switch (option) {

                    case "1":

                        if (completed >= requested) {
                            System.out.println(
                                    "You have processed the requested number of messages.");
                            break;
                        }

                        while (completed < requested) {

                            System.out.println("\n--- Create Message ---");

                            System.out.print(
                                    "Enter recipient cell number (+27 followed by 9 digits): ");
                            String recipient = scan.nextLine().trim();

                            System.out.print(
                                    "Enter message (maximum 250 characters): ");
                            String messageText = scan.nextLine();

                            Message message = new Message(
                                    recipient,
                                    messageText,
                                    Message.getNextMessageNumber());

                            System.out.println("\n" + message.details());

                            System.out.println(
                                    "\nMessage ID valid: "
                                    + message.checkMessageID());

                            System.out.println(
                                    message.validateMessageLength());

                            System.out.println(
                                    message.checkRecipientCell());

                            if (!message.checkRecipientCell().contains(
                                    "successfully")
                                    || !message.validateMessageLength().equals(
                                            "Message ready to send.")) {

                                System.out.println(
                                        "Message was not processed. Please try again.");
                                continue;
                            }

                            String status = message.SentMessage(scan);

                            System.out.println(status);

                            if (status.equals("Message successfully sent.")
                                    || status.equals("Message successfully stored.")
                                    || status.equals("Message disregarded.")) {

                                completed++;
                            }

                            System.out.println(
                                    "Messages processed: "
                                    + completed + "/" + requested);
                        }

                        break;

                    case "2":

                        Message.showRecentlySentMessages();
                        break;

                    case "3":

                        Message.showStoredMessagesMenu(scan);
                        break;

                    case "4":

                        System.out.println("\nThank you for using QuickChat.");

                        System.out.println(
                                "Total messages successfully sent: "
                                + Message.getTotalMessages());

                        return;

                    default:

                        System.out.println(
                                "Invalid option. Please select 1, 2, 3, or 4.");
                }
            }
        }
    }
}
