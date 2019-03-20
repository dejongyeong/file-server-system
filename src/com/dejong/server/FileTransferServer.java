package com.dejong.server;

import com.dejong.utils.SeedUsers;
import com.dejong.utils.ServerUtilities;
import com.dejong.utils.TrackLoginUsers;
import com.dejong.utils.Users;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

/**
 * This module contains application logic of file transfer server which uses connectionless datagram socket of IPC.
 * A command-line argument is required to specify server port.
 *
 * @author M. L. Liu
 *
 *
 * Response Code:
 * Login: 301 successful login; 302 unsuccessful login; 303 user already logged in; 900 system error.
 *
 *
 */

public class FileTransferServer {

    // variables
    static int serverPort = 3000; // default port
    static MyServerDatagramSocket socket;

    // main method to run server.
    public static void main(String[] args) {

        //variables
        String code;
        String username;
        String password;
        String response;

        try {
            socket = new MyServerDatagramSocket(serverPort);
            System.out.println("File Management Server ready.");

            while(true) { //loop forever
                //send and receive data
                DatagramMessage request = socket.receiveMessageAndSender();
                System.out.println("request received");
                String message = request.getMessage();
                System.out.println("message received: " + message);

                //decode message format: "300, username, password" documented in documentation and remove whitespace.
                //references: https://www.mkyong.com/java/java-how-to-split-a-string/
                String[] messages = message.split(",");
                code = messages[0].trim();
                username = messages[1].trim();
                password = messages[2].trim();

                //invoke methods based on message types
                //300 login; 500 register; 400 logout;
                switch (code) {
                    case "300":
                        System.out.println("Server: Log In");
                        response = ServerUtilities.login(username, password);
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                        break;
                    case "500":
                        System.out.println("Server: Register");
                        response = ServerUtilities.register(username, password);
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                        break;
                    default:
                        System.out.println("System error occurred!");
                        response = "900: System error. Please try again!";
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                } //end switch

                //list of all logged in users
                ServerUtilities.listOfLoggedInUsers();
            } //end while
        } catch (Exception ex) {
            ex.printStackTrace();
        } //end catch

    } //end main
} //end class
