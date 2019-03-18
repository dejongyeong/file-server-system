package com.dejong.client;

import com.dejong.utils.ClientServerDatagramSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * A subclass of DatagramSocket which contains methods for sending and
 * receiving messages.
 *
 * @author  M. L. Liu
 *
 * Note: class reused from Distributed Computing lab.
 *
 **/

public class MyClientDatagramSocket extends ClientServerDatagramSocket {

    MyClientDatagramSocket() throws SocketException {
        super();
    }

    MyClientDatagramSocket(int portNo) throws SocketException {
        super(portNo);
    }

    public void sendMessage(InetAddress receiverHost, int receiverPort, String message) throws IOException {
        super.sendMessage(receiverHost, receiverPort, message);
    }

    public String receiveMessage() throws IOException {
        return super.receiveMessage();
    }

}
