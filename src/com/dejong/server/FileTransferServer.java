package com.dejong.server;

import com.dejong.utils.ServerUtilities;
import com.dejong.utils.TrackLoginUsers;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.DatagramSocket;
import java.security.KeyStore;

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

    //variables
    static String hostname = "localhost";
    static int serverPort = 7; // default port
    static MyServerDatagramSocket socket;
    static String keystoreFile = "fms.jks";
    static String keyStorePwd = "ittralee";

    //main method to run server.
    public static void main(String[] args) {
        //ssl communication
        System.setProperty("javax.net.ssl.keyStore", keystoreFile);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePwd);
        System.setProperty("javax.net.debug", "all");

        //variables
        String code;
        String username;
        String password = "";
        String response;

        try {
            //ssl secure communication
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keystoreFile), keyStorePwd.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"); // Type of Certificate
            kmf.init(ks, keyStorePwd.toCharArray());

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);

            socket = new MyServerDatagramSocket(serverPort);
            System.out.println("\n\n------File Management Server ready------");

            while(true) { //loop forever
                //send and receive data
                DatagramMessage request = socket.receiveMessageAndSender();
                System.out.println("request received");
                String message = request.getMessage();
                System.out.println("message received: " + message);

                //decode message format: "300, username, password" documented in documentation and remove whitespace.
                //handle different message format: "400, username"
                //references: https://www.mkyong.com/java/java-how-to-split-a-string/
                String[] messages = message.split(" ");
                code = messages[0].trim();
                username = messages[1].trim();
                if(messages.length == 3) {
                    password = messages[2].trim();
                } //end if

                //invoke methods based on message types
                //300 login; 500 register; 400 logout;
                switch (code) {
                    case "300":
                        System.out.println("Server: Log In");
                        response = ServerUtilities.login(username, password);
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                        break;
                    case "400":
                        System.out.println("Server: Log Out");
                        response = ServerUtilities.logout(username);
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
            } //end while
        } catch (Exception ex) {
            ex.printStackTrace();
        } //end catch
    } //end main
} //end class
