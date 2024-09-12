package org.javasocketclient;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket client;
    private PrintWriter messageSender;
    private BufferedReader messageReader;

    private InputStream dataComingIn;
    private boolean running;

    private Thread receiverThread;
    private Thread senderThread;


    public Client(String url, int Port) throws IOException {
        client = new Socket(url, Port);
        dataComingIn = client.getInputStream();

        messageSender = new PrintWriter(client.getOutputStream(), true);
        messageReader = new BufferedReader(new InputStreamReader(dataComingIn));

        receiverThread = new Thread(this::messageReceiver);
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
            receiverThread.join();
            senderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }

    }

    public void messageSendingService() throws IOException {
        Scanner readConsole = new Scanner(System.in);
        System.out.println("To Stop Service Write 'exit' in console");

        while(running) {
            if(readConsole.nextLine().equals("exit")) {
                closeConnection();
                running = false;
                readConsole.close();
                return;
            }

            sendMessage(readConsole.nextLine());
        }
    }

    private void sendMessage(String message) throws IOException {
        messageSender.println(message);
        messageSender.flush();
    }

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

    private void closeConnection() {
        try {
            if(messageReader != null) messageReader.close();
            if(messageSender != null) messageSender.close();
            if(client != null) client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
