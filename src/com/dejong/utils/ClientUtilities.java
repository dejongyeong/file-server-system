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
    private static String port = "3000";

    /**
     * Client login utility class.
     * @param username username of client.
     * @param password password of client.
     * @return server response in string.
     * @throws IOException
     */
    public static String login(String username, String password) throws IOException {
        ClientHelper helper = new ClientHelper(hostname, port);
        String message = "300" + " " + username + " " + password;
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
        String message = "500" + " " + username + " " + password;
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
        String message = "400" + " " + username;
        String echo = helper.send(message);
        return echo;
    }

}
