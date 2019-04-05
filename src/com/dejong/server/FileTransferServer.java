package com.dejong.server;

import com.dejong.utils.SeedUsers;
import com.dejong.utils.ServerUtilities;
import com.dejong.utils.Users;

import java.net.InetAddress;
import java.util.List;

/**
 * This module contains application logic of file transfer server which uses connectionless datagram socket of IPC.
 * A command-line argument is required to specify server port.
 *
 * @author M. L. Liu
 *
 *
 * Response Code:
 * 300 Login: 301 successful login; 302 unsuccessful login; 303 user already logged in; 900 system error.
 * 400 Logout: 401 successful logout; 402 unsuccessful logout; 900 system error.
 * 500 Register: 501 succesful register; 502 unsuccessful register.
 * 600 Upload: 601 successful upload; 602 unsuccessful upload.
 * 700 Download: 701 successful download; 702 unsuccessful download.
 *
 */

@SuppressWarnings("Duplicates")
public class FileTransferServer {

    //variables
    static int clientPort = 8;
    static String hostname = "localhost";

    //main method to run server.
    public static void main(String[] args) {
        //variables
        String code;
        String username;
        String password;
        String response;
        String filename;

        try {
            //initialize socket
            MyServerDatagramSocket socket = new MyServerDatagramSocket();

            System.out.println("\n------File Management Server ready------");

            //display list of users in server
            displayListOfUsers();

            while(true) { //loop forever
                //send and receive data
                DatagramMessage request = socket.receiveMessage(InetAddress.getByName(hostname), clientPort);
                System.out.println("request received");
                String message = request.getMessage();
                System.out.println("message received: " + message);

                //decode message format: "300, username, password" documented in documentation and remove whitespace.
                //handle different message format: "400, username"
                //references: https://www.mkyong.com/java/java-how-to-split-a-string/
                String[] messages = message.split(" ");
                code = messages[0].trim();
                username = messages[1].trim();

                //invoke methods based on message types
                //300 login; 500 register; 400 logout; 600 upload; 700 download;
                switch (code) {
                    case "300":
                        System.out.println("Server: Log In");
                        password = messages[2].trim();
                        response = ServerUtilities.login(username, password);
                        System.out.println(response); //print server response out
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                        break;
                    case "400":
                        System.out.println("Server: Log Out");
                        response = ServerUtilities.logout(username);
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                        break;
                    case "500":
                        System.out.println("Server: Register");
                        password = messages[2].trim();
                        response = ServerUtilities.register(username, password);
                        System.out.println(response); //print server response out
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                        break;
                    case "600":
                        System.out.println("Server: Upload");
                        filename = messages[2].trim();
                        response = ServerUtilities.upload(username, filename);
                        System.out.println(response); //print server response out
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                        break;
                    case "700":
                        System.out.println("Server: Download");
                        filename = messages[2].trim();
                        response = ServerUtilities.download(username, filename);
                        System.out.println(response); //print server response out
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                        break;
                    default:
                        System.out.println("System error occurred!");
                        response = "900: System error. Please try again!";
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                } //end switch
            } //end while
        } catch (Exception ex) {
            ex.printStackTrace();
        } //end catch
    } //end main

    private static void displayListOfUsers() {
        List<Users> users = SeedUsers.open();
        System.out.println("List of Users:");
        for(Users u: users) {
            System.out.println(u.getUsername() + " " + u.getPassword());
        }
        System.out.println();
    }
} //end class