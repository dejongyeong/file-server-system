package com.dejong.dtls.server;

import com.dejong.dtls.DTLSEngine;
import com.dejong.server.DatagramMessage;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class MyDTLSServerDatagramSocket extends DatagramSocket {

    private static final int MAX_LEN = 100;
    private SSLEngine engine;

    //constructor
    public MyDTLSServerDatagramSocket(int port) throws SocketException {
        super(port);
    }

    //send message
    public void sendMessage(SSLEngine engine, InetAddress receiverHost, int receiverPort, String message) {
        try {
            //client socket address
            InetSocketAddress clientSocket = new InetSocketAddress(receiverHost, receiverPort);

            //engine wrap data
            DTLSEngine.sendAppData(engine, this, ByteBuffer.wrap(message.getBytes()), clientSocket, "Server");
            System.out.println("Data sent to Client");

        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch
    }

    //receive message
    public String receiveMessage() {
        try {
            //create sslengine for server
            engine = DTLSEngine.createSSLEngine(false);

            //datagram receive packet
            DatagramMessage receivedData = DTLSEngine.receiveAppData(engine, this, "Server");

            //client socket address
            InetSocketAddress clientSocket = new InetSocketAddress(receivedData.getAddress(), receivedData.getPort());

            //handshaking
            DatagramMessage appData = DTLSEngine.handshake(engine, this, clientSocket, true);

            //send message
            if(appData == null) {
                System.out.println("No application data received on server side.");
            } else {
                System.out.println("Message received");
                System.out.println(appData.getMessage());
                return appData.getMessage();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch

        return null;
    }

}
