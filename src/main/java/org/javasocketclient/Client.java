package org.javasocketclient;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;
import java.util.Stack;

public class Client {
    private Socket client; // Client socket for connecting to the server
    private PrintWriter messageSender; // To send messages to the server
    private BufferedReader messageReader; // To read messages from the server

    private InputStream dataComingIn; // InputStream for receiving incoming data
    private boolean running; // Controls the client's running state

    private Thread receiverThread; // Thread to handle incoming messages from the server
    private Thread senderThread; // Thread to handle outgoing messages to the server

    Stack<String> messageStack = new Stack<>(); // Stack to store messages temporarily

    /**
     * Initializes the client and connects it to the server.
     * Sets up communication streams and starts the sender and receiver threads.
     */
    public Client(String url, int Port) throws IOException {
        client = new Socket(url, Port);
        dataComingIn = client.getInputStream();

        messageSender = new PrintWriter(client.getOutputStream(), true);
        messageReader = new BufferedReader(new InputStreamReader(dataComingIn));

        receiverThread = new Thread(this::messageReceiver); // Thread to handle incoming messages

        // Thread to handle outgoing messages, wrapped to manage potential I/O exceptions
        senderThread = new Thread(() -> {
            try {
                messageSendingService();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        receiverThread.start();
        senderThread.start();

        try {
            receiverThread.join(); // Ensure both threads complete before exiting
            senderThread.join();

            senderThread.setPriority(Thread.MAX_PRIORITY); // Prioritize the sender thread
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Handle interrupt properly
            e.printStackTrace();
        } finally {
            closeConnection(); // Ensure resources are closed when finished
        }
    }

    /**
     * Handles sending messages from the client to the server.
     * Continuously reads from the console and sends messages until "exit" is entered.
     */
    public void messageSendingService() throws IOException {
        Scanner readConsole = new Scanner(System.in);
        String consoleData = "";
        System.out.println("Type exit to stop service");

        while (running) {
            consoleData = readConsole.nextLine();
            messageStack.push(consoleData);
            if (Objects.equals(consoleData, "exit")) {
                closeConnection();
                running = false;
                readConsole.close();
                return;
            }

            sendMessage(consoleData); // Send message to the server
            consoleData = "";
        }
    }

    /**
     * Sends a message to the server.
     * Ensures that the message is immediately flushed.
     */
    private void sendMessage(String message) throws IOException {
        messageSender.println(message);
        messageSender.flush();
    }

    /**
     * Continuously receives messages from the server and prints them.
     * Terminates if the client stops running or the connection is lost.
     */
    public void messageReceiver() {
        running = true;

        try {
            String receivedMessage;
            while (running && (receivedMessage = messageReader.readLine()) != null) {
                System.out.println("Received: " + receivedMessage);
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    /**
     * Closes all open resources associated with the client communication.
     */
    private void closeConnection() {
        try {
            if (messageReader != null) messageReader.close();
            if (messageSender != null) messageSender.close();
            if (client != null) client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
