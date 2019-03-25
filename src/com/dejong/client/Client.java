package com.dejong.client;

import com.dejong.utils.ClientUtilities;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;

/**
 * This module contains presentation logic of a client
 * @author M. L. Liu
 */

public class Client {

    //keystore details
    static String keystoreFile = "public.jks";
    static String keyStorePwd = "ittralee";

    public static void main(String args[]) {
        //ssl communication
        System.setProperty("javax.net.ssl.keyStore", keystoreFile);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePwd);

        //uncomment to check for debug
        //System.setProperty("javax.net.debug", "all");

        //variables
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean done = false;
        String serverResponse;
        String username;
        String password;
        String filename;
        String filetype;
        String file;

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

            SSLEngine engine = sc.createSSLEngine("localhost", 7);
            engine.setUseClientMode(true);

            System.out.println("\n------Welcome to File Management System------");

            //program loop
            while(!done) {
                System.out.println("\n---------- Enter option -----------\n" +
                        "1. Login\n" + "2. Register\n" + "3. Upload\n" + "4. Download\n" +
                        "5. Logout\n" + "6. Quit/Disconnect");
                String option = br.readLine();
                switch(option) {
                    case "1":
                        System.out.println("Prepare to log in");
                        System.out.println("Enter username: ");
                        username = br.readLine();
                        System.out.println("Enter password: ");
                        password = br.readLine();
                        if(username.trim().length() == 0 || password.trim().length() == 0) {
                            throw new RuntimeException("Username and Password must not be empty.");
                        }
                        serverResponse = ClientUtilities.login(username, password);
                        System.out.println(serverResponse);
                        break;
                    case "2":
                        System.out.println("Prepare to register");
                        System.out.println("Enter username");
                        username = br.readLine();
                        System.out.println("Enter password");
                        password = br.readLine();
                        if(username.trim().length() == 0 || password.trim().length() == 0) {
                            throw new RuntimeException("Username and Password must not be empty.");
                        }
                        serverResponse = ClientUtilities.register(username, password);
                        System.out.println(serverResponse);
                        break;
                    case "3":
                        System.out.println("Prepare to upload - Ensure file is in C:\\DC\\");
                        System.out.println("Enter username");
                        username = br.readLine();
                        System.out.println("Enter filename");
                        filename = br.readLine();
                        filetype = filetype(); //prompt user for file format
                        if(username.trim().length() == 0 || filename.trim().length() == 0 || filetype.trim().length() == 0) {
                            throw new RuntimeException("Username, filename and file format must not be empty.");
                        }
                        file = filename + filetype;
                        System.out.println("File to upload: " + file);
                        serverResponse = ClientUtilities.upload(username, file);
                        System.out.println(serverResponse);
                        break;
                    case "4":
                        System.out.println("Prepare to download - Ensure file is in C:\\DC\\username");
                        System.out.println("Enter username");
                        username = br.readLine();
                        System.out.println("Enter filename");
                        filename = br.readLine();
                        filetype = filetype();
                        if(username.trim().length() == 0 || filename.trim().length() == 0 || filetype.trim().length() == 0) {
                            throw new RuntimeException("Username, filename and file format must not be empty.");
                        }
                        file = filename + filetype;
                        System.out.println("File to download: " + file);
                        serverResponse = ClientUtilities.download(username, file);
                        System.out.println(serverResponse);
                        break;
                    case "5":
                        System.out.println("Prepare to logout");
                        System.out.println("Enter username");
                        username = br.readLine();
                        if(username.trim().length() == 0) {
                            throw new RuntimeException("Username and Password must not be empty.");
                        }
                        serverResponse = ClientUtilities.logout(username);
                        System.out.println(serverResponse);
                        break;
                    case "6":
                        System.out.println("Quitting");
                        ClientUtilities.shutdown();  //close socket
                        done = true;  //break loop
                        break;
                    default:
                        System.out.println("Invalid option! Please try again.");
                        break;
                } //end switch
            } //end while
        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch
    } //end main

    //prompt user for file type
    private static String filetype() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String filetype = "";
        System.out.println("File Format Option:\n1. .txt\n2. .docx");
        String option = br.readLine();
        switch(option) {
            case "1":
                filetype = ".txt";
                break;
            case "2":
                filetype = ".docx";
                break;
            default:
                System.out.println("Invalid option! Please try again.");
                break;
        } //end switch
        return filetype;
    }
} //end class
