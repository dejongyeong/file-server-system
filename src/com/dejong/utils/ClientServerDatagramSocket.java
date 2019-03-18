package com.dejong.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientServerDatagramSocket extends DatagramSocket {

    private static final int MAX_LEN = 100;

    public ClientServerDatagramSocket() throws SocketException {
        super();
    }

    protected ClientServerDatagramSocket(int port) throws SocketException {
        super(port);
    }

    protected void sendMessage(InetAddress receiverHost, int receiverPort, String message) throws IOException {
        byte[] sendBuffer = message.getBytes();
        DatagramPacket datagram = new DatagramPacket(sendBuffer, sendBuffer.length, receiverHost, receiverPort);
        this.send(datagram);
    } //end sendMessage

    protected String receiveMessage() throws IOException {
        byte[] receiveBuffer = new byte[MAX_LEN];
        DatagramPacket datagram = new DatagramPacket(receiveBuffer, MAX_LEN);
        this.receive(datagram);

        String message = new String(receiveBuffer);
        return message;
    } //end receiveMessage

}
