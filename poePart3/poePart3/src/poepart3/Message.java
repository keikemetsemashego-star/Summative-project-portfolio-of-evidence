
package poepart3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Creates, validates, sends, stores and manages messages.
 */
public final class Message {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int CAPACITY = 100;

    private static int count = 0;
    private static int totalMessages = 0;

    private static final String[] ids = new String[CAPACITY];
    private static final String[] senders = new String[CAPACITY];
    private static final String[] recipients = new String[CAPACITY];
    private static final String[] messages = new String[CAPACITY];
    private static final String[] flags = new String[CAPACITY];
    private static final String[] messageHashes = new String[CAPACITY];

    private static final List<String> SENT_MESSAGES = new ArrayList<>();
    private static final List<String> DISREGARDED_MESSAGES = new ArrayList<>();
    private static final List<String> STORED_MESSAGES = new ArrayList<>();

    private final String messageId;
    private final int messageNumber;
    private final String recipient;
    private final String text;
    private final String messageHash;

    private String actionStatus = "";

    // CONSTRUCTORS

    public Message(String recipient, String text) {
        this(recipient, text, count + 1);
    }

    public Message(String recipient, String text, int sequenceNumber) {

        this.messageId = generateMessageId();
        this.messageNumber = sequenceNumber;
        this.recipient = recipient;
        this.text = text;
        this.messageHash = createMessageHash();
    }

    // GENERATE UNIQUE MESSAGE ID

    private static String generateMessageId() {

        String id;

        do {
            id = String.format(
                    "%010d",
                    RANDOM.nextInt(1_000_000_000));
        } while (idExists(id));

        return id;
    }

    private static boolean idExists(String id) {

        for (int i = 0; i < count; i++) {
            if (id.equals(ids[i])) {
                return true;
            }
        }

        return false;
    }

    // GETTERS

    public String getMessageId() {
        return messageId;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getText() {
        return text;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public static int getNextMessageNumber() {
        return count + 1;
    }

    // CHECK MESSAGE ID

    public boolean checkMessageID() {

        return messageId != null
                && messageId.matches("[0-9]{10}");
    }

    // CHECK RECIPIENT CELL NUMBER

    public String checkRecipientCell() {

        if (recipient != null
                && recipient.matches("^\\+27[0-9]{9}$")) {

            return "Cell phone number successfully captured.";
        }

        return "Cell phone number is incorrectly formatted or "
                + "does not contain an international code.";
    }

    // CREATE MESSAGE HASH

    public String createMessageHash() {

        if (text == null || text.isBlank()) {
            return "";
        }

        String[] words = text.trim().split("\\s+");

        String first = words[0].replaceAll(
                "[^\\p{L}\\p{N}]", "");

        String last = words[words.length - 1].replaceAll(
                "[^\\p{L}\\p{N}]", "");

        return (messageId.substring(0, 2)
                + ":" + messageNumber
                + ":" + first + last).toUpperCase();
    }

    // VALIDATE MESSAGE LENGTH

    public String validateMessageLength() {

        if (text == null || text.length() > 250) {

            int length = text == null ? 0 : text.length();

            return "Message exceeds 250 characters by "
                    + Math.max(0, length - 250)
                    + "; please reduce the size.";
        }

        return "Message ready to send.";
    }

    // DISPLAY MESSAGE DETAILS

    public String details() {

        return "Message ID: " + messageId
                + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient
                + "\nMessage: " + text;
    }

    // ADD MESSAGE TO ARRAYS

    private static boolean addMessage(
            Message message, String flag) {

        if (count >= CAPACITY) {
            System.out.println("Message storage is full.");
            return false;
        }

        ids[count] = message.messageId;
        senders[count] = "User";
        recipients[count] = message.recipient;
        messages[count] = message.text;
        flags[count] = flag;
        messageHashes[count] = message.messageHash;

        if (flag.equalsIgnoreCase("Sent")) {

            SENT_MESSAGES.add(message.details());
            totalMessages++;

        } else if (flag.equalsIgnoreCase("Stored")) {

            STORED_MESSAGES.add(message.details());

        } else if (flag.equalsIgnoreCase("Disregard")) {

            DISREGARDED_MESSAGES.add(message.details());
        }

        count++;

        return true;
    }

    // ADDITIONAL METHOD FOR TEST DATA

    public static void add(
            String sender,
            String recipient,
            String message,
            String flag) {

        if (count >= CAPACITY) {
            System.out.println("Message storage is full.");
            return;
        }

        String id = generateMessageId();

        String[] words = message.trim().split("\\s+");

        String first = words[0].replaceAll(
                "[^\\p{L}\\p{N}]", "");

        String last = words[words.length - 1].replaceAll(
                "[^\\p{L}\\p{N}]", "");

        String hash = (id.substring(0, 2)
                + ":" + (count + 1)
                + ":" + first + last).toUpperCase();

        ids[count] = id;
        senders[count] = sender;
        recipients[count] = recipient;
        messages[count] = message;
        flags[count] = flag;
        messageHashes[count] = hash;

        if (flag.equalsIgnoreCase("Sent")) {
            SENT_MESSAGES.add(message);
            totalMessages++;

        } else if (flag.equalsIgnoreCase("Stored")) {
            STORED_MESSAGES.add(message);

        } else if (flag.equalsIgnoreCase("Disregard")) {
            DISREGARDED_MESSAGES.add(message);
        }

        count++;
    }

    // SEND, DISREGARD OR STORE MESSAGE

    public String SentMessage(Scanner input) {

        System.out.println("\n1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");

        System.out.print("Choose an option: ");

        String choice = input.nextLine().trim();

        switch (choice) {

            case "1":

                if (!checkRecipientCell().contains("successfully")) {
                    actionStatus = checkRecipientCell();
                    break;
                }

                if (!validateMessageLength().equals(
                        "Message ready to send.")) {

                    actionStatus = validateMessageLength();
                    break;
                }

                if (addMessage(this, "Sent")) {
                    actionStatus = "Message successfully sent.";
                }

                break;

            case "2":

                if (addMessage(this, "Disregard")) {
                    actionStatus = "Message disregarded.";
                }

                break;

            case "3":

                if (!checkRecipientCell().contains("successfully")) {
                    actionStatus = checkRecipientCell();
                    break;
                }

                if (!validateMessageLength().equals(
                        "Message ready to send.")) {

                    actionStatus = validateMessageLength();
                    break;
                }

                if (count >= CAPACITY) {
                    actionStatus = "Message storage is full.";
                    break;
                }

                try {

                    storeMessage(Paths.get("messages.json"));

                    if (addMessage(this, "Stored")) {
                        actionStatus = "Message successfully stored.";
                    }

                } catch (IOException exception) {

                    actionStatus = "Message could not be stored: "
                            + exception.getMessage();
                }

                break;

            default:

                actionStatus = "Invalid option.";
        }

        return actionStatus;
    }

    // DISPLAY RECENTLY SENT MESSAGES

    public static void showRecentlySentMessages() {

        System.out.println("\n--- Recently Sent Messages ---");

        if (SENT_MESSAGES.isEmpty()) {
            System.out.println("No sent messages available.");
            return;
        }

        for (String message : SENT_MESSAGES) {
            System.out.println(message);
            System.out.println("----------------------------");
        }
    }

    // STORED MESSAGE MANAGEMENT MENU

    public static void showStoredMessagesMenu(Scanner input) {

        String choice;

        do {

            System.out.println("\n--- Stored Messages ---");
            System.out.println("1. Display sender and recipient");
            System.out.println("2. Display longest sent message");
            System.out.println("3. Search for a message ID");
            System.out.println("4. Search messages by recipient");
            System.out.println("5. Delete a message using its hash");
            System.out.println("6. Display full message report");
            System.out.println("0. Return to main menu");

            System.out.print("Select an option: ");

            choice = input.nextLine().trim();

            switch (choice) {

                case "1":
                    displaySendersAndRecipients();
                    break;

                case "2":
                    displayLongestSentMessage();
                    break;

                case "3":
                    System.out.print("Enter message ID: ");
                    searchById(input.nextLine().trim());
                    break;

                case "4":
                    System.out.print("Enter recipient: ");
                    searchByRecipient(input.nextLine().trim());
                    break;

                case "5":
                    System.out.print("Enter message hash: ");
                    deleteByHash(input.nextLine().trim());
                    break;

                case "6":
                    displayReport();
                    break;

                case "0":
                    System.out.println("Returning to main menu.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }

        } while (!choice.equals("0"));
    }

    // DISPLAY SENDER AND RECIPIENT

    private static void displaySendersAndRecipients() {

        boolean found = false;

        for (int i = 0; i < count; i++) {

            if (flags[i].equalsIgnoreCase("Stored")) {

                System.out.println("Sender: " + senders[i]);
                System.out.println("Recipient: " + recipients[i]);

                found = true;
            }
        }

        if (!found) {
            System.out.println("No stored messages found.");
        }
    }

    // DISPLAY LONGEST SENT MESSAGE

    private static void displayLongestSentMessage() {

        int longest = -1;

        for (int i = 0; i < count; i++) {

            if (flags[i].equalsIgnoreCase("Sent")) {

                if (longest == -1
                        || messages[i].length()
                        > messages[longest].length()) {

                    longest = i;
                }
            }
        }

        if (longest == -1) {

            System.out.println("No sent messages found.");

        } else {

            System.out.println("Longest sent message:");
            System.out.println(messages[longest]);
        }
    }

    // SEARCH BY MESSAGE ID

    private static void searchById(String id) {

        for (int i = 0; i < count; i++) {

            if (ids[i].equalsIgnoreCase(id)) {

                System.out.println("Sender: " + senders[i]);
                System.out.println("Recipient: " + recipients[i]);
                System.out.println("Message: " + messages[i]);
                System.out.println("Hash: " + messageHashes[i]);

                return;
            }
        }

        System.out.println("Message ID not found.");
    }

    // SEARCH BY RECIPIENT

    private static void searchByRecipient(String recipient) {

        boolean found = false;

        for (int i = 0; i < count; i++) {

            if (recipients[i].equalsIgnoreCase(recipient)
                    && (flags[i].equalsIgnoreCase("Sent")
                    || flags[i].equalsIgnoreCase("Stored"))) {

                System.out.println("Message: " + messages[i]);
                System.out.println("Status: " + flags[i]);

                found = true;
            }
        }

        if (!found) {
            System.out.println(
                    "No sent or stored messages for this recipient.");
        }
    }

    // DELETE MESSAGE USING HASH

    private static void deleteByHash(String hash) {

        for (int i = 0; i < count; i++) {

            if (messageHashes[i].equalsIgnoreCase(hash)
                    && flags[i].equalsIgnoreCase("Stored")) {

                System.out.println(
                        "Message: \"" + messages[i]
                        + "\" successfully deleted.");

                for (int j = i; j < count - 1; j++) {
                    copy(j + 1, j);
                }

                clear(count - 1);
                count--;

                rebuildStoredMessages();

                try {
                    rewriteStoredMessages(
                            Paths.get("messages.json"));
                } catch (IOException exception) {
                    System.out.println(
                            "Could not update JSON file: "
                            + exception.getMessage());
                }

                return;
            }
        }

        System.out.println("Stored message hash not found.");
    }

    // REBUILD STORED MESSAGE LIST

    private static void rebuildStoredMessages() {

        STORED_MESSAGES.clear();

        for (int i = 0; i < count; i++) {

            if (flags[i].equalsIgnoreCase("Stored")) {
                STORED_MESSAGES.add(
                        "Message ID: " + ids[i]
                        + "\nMessage Hash: " + messageHashes[i]
                        + "\nRecipient: " + recipients[i]
                        + "\nMessage: " + messages[i]);
            }
        }
    }

    // DISPLAY FULL MESSAGE REPORT

    public static void displayReport() {

        System.out.println(
                "\nID | Hash | Sender | Recipient | Flag | Message");

        if (count == 0) {
            System.out.println("No messages available.");
            return;
        }

        for (int i = 0; i < count; i++) {

            System.out.printf(
                    "%s | %s | %s | %s | %s | %s%n",
                    ids[i],
                    messageHashes[i],
                    senders[i],
                    recipients[i],
                    flags[i],
                    messages[i]);
        }
    }

    // COPY ARRAY DETAILS

    private static void copy(int from, int to) {

        ids[to] = ids[from];
        senders[to] = senders[from];
        recipients[to] = recipients[from];
        messages[to] = messages[from];
        flags[to] = flags[from];
        messageHashes[to] = messageHashes[from];
    }

    // CLEAR ARRAY POSITION

    private static void clear(int i) {

        ids[i] = null;
        senders[i] = null;
        recipients[i] = null;
        messages[i] = null;
        flags[i] = null;
        messageHashes[i] = null;
    }

    // PRINT MESSAGES

    public String printMessages() {

        return String.join("\n", SENT_MESSAGES);
    }

    public int returnTotalMessages() {

        return totalMessages;
    }

    public static int getTotalMessages() {

        return totalMessages;
    }

    // STORE MESSAGE IN JSON FILE

    public void storeMessage(Path path) throws IOException {

        String object = createJsonObject(
                messageId,
                messageHash,
                recipient,
                text);

        String existing = Files.exists(path)
                ? Files.readString(
                        path, StandardCharsets.UTF_8).trim()
                : "";

        String content;

        if (existing.isEmpty() || existing.equals("[]")) {

            content = "[" + object + "]";

        } else if (existing.startsWith("[")
                && existing.endsWith("]")) {

            content = existing.substring(
                    0, existing.length() - 1)
                    + "," + object + "]";

        } else {

            throw new IOException(
                    "Existing JSON file is not a valid JSON array.");
        }

        Files.writeString(
                path,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    // REWRITE JSON FILE AFTER DELETION

    private static void rewriteStoredMessages(Path path)
            throws IOException {

        StringBuilder content = new StringBuilder("[");

        boolean first = true;

        for (int i = 0; i < count; i++) {

            if (flags[i].equalsIgnoreCase("Stored")) {

                if (!first) {
                    content.append(",");
                }

                content.append(
                        createJsonObject(
                                ids[i],
                                messageHashes[i],
                                recipients[i],
                                messages[i]));

                first = false;
            }
        }

        content.append("]");

        Files.writeString(
                path,
                content.toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    // CREATE JSON OBJECT

    private static String createJsonObject(
            String id,
            String hash,
            String recipient,
            String message) {

        return "{\"messageId\":\"" + escape(id)
                + "\",\"messageHash\":\"" + escape(hash)
                + "\",\"recipient\":\"" + escape(recipient)
                + "\",\"message\":\"" + escape(message) + "\"}";
    }

    // ESCAPE JSON CHARACTERS

    private static String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
