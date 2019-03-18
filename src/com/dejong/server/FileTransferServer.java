package com.dejong.server;

import java.io.*;
import java.nio.file.Files;

public class FileTransferServer {

    static int serverPort = 3000; // default port
    static MyServerDatagramSocket socket;

    public static void main(String[] args) {

        if(args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }

        try {
            socket = new MyServerDatagramSocket(serverPort);
            System.out.println("server ready.");

            while(true) { // loop forever
                DatagramMessage request = socket.receiveMessageAndSender();
                System.out.println("request received");
                String message = request.getMessage();
                System.out.println("message received: " + message);

                socket.sendMessage(request.getAddress(), request.getPort(), message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } // end catch

    }
}
