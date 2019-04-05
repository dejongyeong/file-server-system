package com.dejong.client;

import com.dejong.dtls.DTLSEngine;

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

    private MyClientDatagramSocket socket;
    private InetAddress serverHost;
    private int serverPort;

    //constructor
    public ClientHelper(String hostname, String portNum) throws IOException {
        this.serverHost = InetAddress.getByName(hostname);
        this.serverPort = Integer.parseInt(portNum);

        // instantiates datagram socket for both sending and receiving data
        this.socket = new MyClientDatagramSocket();
    }

    //send and receive
    public String sendAndReceive(String message) {
        try {
            //set ssl engine
            socket.setSSLEngine(DTLSEngine.createSSLEngine(true));

            //send message
            socket.sendMessage(serverHost, serverPort, message);

            // receive echo
            return socket.receiveMessage(serverHost, serverPort).getMessage();

        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch

        return null;
    } // end receive echo/message

    //disconnect from server
    public void disconnect() {
        socket.disconnect();
    }
}
