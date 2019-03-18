package com.dejong.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This class is a nodule which provides the application logic for an
 * Echo client using connectionless datagram socket.
 *
 * @author: M. L. Liu
 *
 * Note: class reused from Distributed Computing lab.
 *
 **/

public class ClientHelper {

    private MyClientDatagramSocket mySocket;
    private InetAddress serverHost;
    private int serverPort;

    ClientHelper(String hostname, String portNum) throws SocketException, UnknownHostException {
        this.serverHost = InetAddress.getByName(hostname);
        this.serverPort = Integer.parseInt(portNum);

        // instantiates datagram socket for both sending and receiving data
        this.mySocket = new MyClientDatagramSocket();
    }

    public String getEcho(String message) throws SocketException, IOException {
        String echo = "";
        mySocket.sendMessage(serverHost, serverPort, message);

        // receive echo
        echo = mySocket.receiveMessage();
        return echo;
    } // end receive echo/message

    public void done() throws SocketException {
        mySocket.close();
    }
}
