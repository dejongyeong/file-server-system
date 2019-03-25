package com.dejong.server;

import com.dejong.utils.ServerUtilities;

import javax.net.ssl.*;
import java.io.FileInputStream;
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
    static int serverPort = 7; // default port
    static MyServerDatagramSocket socket;
    static String keystoreFile = "fms.jks";
    static String keyStorePwd = "ittralee";

    //main method to run server.
    public static void main(String[] args) {
        //ssl communication
        System.setProperty("javax.net.ssl.keyStore", keystoreFile);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePwd);

        //uncomment to check for debug.
        //System.setProperty("javax.net.debug", "all");

        //variables
        String code;
        String username;
        String password;
        String response;
        String filename;

        try {
            //ssl secure communication
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keystoreFile), keyStorePwd.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"); // Type of Certificate
            kmf.init(ks, keyStorePwd.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"); //truststore
            tmf.init(ks);

            SSLContext sc = SSLContext.getInstance("DTLS");
            sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            SSLEngine engine = sc.createSSLEngine();
            engine.setUseClientMode(false);

            socket = new MyServerDatagramSocket(serverPort);
            System.out.println("\n------File Management Server ready------");

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

                //invoke methods based on message types
                //300 login; 500 register; 400 logout; 600 upload; 700 download;
                switch (code) {
                    case "300":
                        System.out.println("Server: Log In");
                        password = messages[2].trim();
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
                        password = messages[2].trim();
                        response = ServerUtilities.register(username, password);
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                        break;
                    case "600":
                        System.out.println("Server: Upload");
                        filename = messages[2].trim();
                        response = ServerUtilities.upload(username, filename);
                        socket.sendMessage(request.getAddress(), request.getPort(), response);
                        break;
                    case "700":
                        System.out.println("Server: Download");
                        filename = messages[2].trim();
                        response = ServerUtilities.download(username, filename);
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