package com.dejong.server;

import com.dejong.utils.ClientServerDatagramSocket;

import java.net.*;
import java.io.*;

/**
 * A subclass of DatagramSocket which contains methods for sending
 * and receiving messages.
 *
 * @author M. L. Liu
 *
 * Note: class reused from Distributed Computing lab.
 *
 **/

public class MyServerDatagramSocket extends ClientServerDatagramSocket {

    static final int MAX_LEN = 100;

    MyServerDatagramSocket() throws SocketException {
        super();
    }

    MyServerDatagramSocket(int port) throws SocketException {
        super(port);
    }

    public void sendMessage(InetAddress receiverHost, int receiverPort, String message) throws IOException {
        super.sendMessage(receiverHost, receiverPort, message);
    } //end sendMessage

    public String receiveMessage() throws IOException {
        return super.receiveMessage();
    } //end receiveMessage

    public DatagramMessage receiveMessageAndSender() throws IOException {
        byte[] receiveBuffer = new byte[MAX_LEN];
        DatagramPacket datagram = new DatagramPacket(receiveBuffer, MAX_LEN);
        this.receive(datagram);

        //create a DatagramMessage object to contain message received and sender's address.
        DatagramMessage returnVal = new DatagramMessage();
        returnVal.putVal(new String(receiveBuffer), datagram.getAddress(), datagram.getPort());
        return returnVal;
    }
} //end class
