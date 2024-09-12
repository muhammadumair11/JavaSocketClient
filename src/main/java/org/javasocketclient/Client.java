package org.javasocketclient;

import java.io.*;
import java.net.Socket;

public class Client  {
    Socket client;
    PrintWriter messageSender;
    BufferedReader messageReader;

    InputStream dataComingIn;

    public Client(String url, int Port) throws IOException {
        client = new Socket(url, Port);
        dataComingIn = client.getInputStream();

        messageSender = new PrintWriter(client.getOutputStream(), true);
        messageReader = new BufferedReader(new InputStreamReader(dataComingIn));

        sendMessage("just a test message sending ");

        String receivedMessage;
        while ((receivedMessage = messageReader.readLine()) != null) {
            System.out.println("Received: " + receivedMessage);
        }

    }

    public void sendMessage(String message) throws IOException {
        if(message.equals("exit")) {
            closeConnection();
            return;
        }
        messageSender.println("just a test message ");
    }

    private void closeConnection() throws IOException {
        messageReader.close();
        messageSender.close();
        client.close();
    }


}
