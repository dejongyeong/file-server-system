package com.dejong.client;

import java.io.IOException;
import java.net.InetAddress;

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

    public ClientHelper(String hostname, String portNum) throws IOException {
        this.serverHost = InetAddress.getByName(hostname);
        this.serverPort = Integer.parseInt(portNum);

        // instantiates datagram socket for both sending and receiving data
        this.mySocket = new MyClientDatagramSocket();
    }

    public String sendAndReceive(String message) throws IOException {
        mySocket.sendMessage(serverHost, serverPort, message);

        // receive echo
        return mySocket.receiveMessage();
    } // end receive echo/message

    public void done() {
        mySocket.close();
    }
}
