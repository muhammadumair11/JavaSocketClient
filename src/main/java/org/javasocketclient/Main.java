package org.javasocketclient;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Client user = new Client("192.168.100.6", 8082);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}