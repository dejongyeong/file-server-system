package com.dejong.utils;

import com.dejong.client.ClientHelper;

import java.io.IOException;

/**
 * This module contains all client features: login, logout, upload, download, which separates from
 * Client presentation logic.
 *
 * @author De Jong on 20 March 2019
 */

public class ClientUtilities {

    private static String hostname = "localhost";
    private static String port = "7";

    /**
     * Client login utility class.
     * @param username username of client.
     * @param password password of client.
     * @return server response in string.
     * @throws IOException
     */
    public static String login(String username, String password) throws IOException {
        ClientHelper helper = new ClientHelper(hostname, port);
        String message = "300" + " " + username + " " + password; //client message to server
        String echo = helper.send(message);
        return echo;
    }

    /**
     * Client logout from server.
     * @param username username of client.
     * @return server response in string.
     * @throws IOException
     */
    public static String logout(String username) throws IOException {
        ClientHelper helper = new ClientHelper(hostname, port);
        String message = "400" + " " + username; //client message to server
        String echo = helper.send(message);
        return echo;
    }

    /**
     * Client register utility class.
     * @param username username of client.
     * @param password password of client.
     * @return server response in string.
     * @throws IOException
     */
    public static String register(String username, String password) throws IOException {
        ClientHelper helper = new ClientHelper(hostname, port);
        String message = "500" + " " + username + " " + password; //client message to server
        String echo = helper.send(message);
        return echo;
    }

    /**
     * Client upload to his/her unique folder
     * @param username username of client.
     * @param filename filename to be uploaded
     * @return server response in string.
     * @throws IOException
     */
    public static String upload(String username, String filename) throws IOException {
        ClientHelper helper = new ClientHelper(hostname, port);
        String message = "600" + " " + username + " " + filename;
        String echo = helper.send(message);
        return echo;
    }

}
