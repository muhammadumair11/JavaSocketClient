package org.javasocketclient;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        try {
            Client user = new Client("localhost", 8082);
            user.sendMessage("whatever");


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}