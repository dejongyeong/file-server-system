package com.dejong.dtls.client;

import com.dejong.dtls.DTLSEngine;
import com.dejong.server.DatagramMessage;

import javax.net.ssl.SSLEngine;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Note to invigilator:
 * Implementations on Datagram Secure Socket Layer were discussed with classmates, thus,
 * code implementation with classmates will be similar.
 *
 */

public class MyDTLSClientDatagramSocket {

    private static int DEFAULT_PORT = 8;

    private SSLEngine engine;
    private DatagramSocket mySocket;

    public MyDTLSClientDatagramSocket(){
        try {
            this.mySocket = new DatagramSocket(DEFAULT_PORT);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    //send message
    public byte[] sendMessage(String message, String receiverHost, int receiverPort) {
        try {

            engine = DTLSEngine.createSSLEngine(true);

            InetSocketAddress serverSocket = new InetSocketAddress(receiverHost, receiverPort);
            DTLSEngine.handshake(this.engine, mySocket, serverSocket, false);

            //convert string to byte buffer
            DTLSEngine.sendAppData(this.engine, mySocket, ByteBuffer.wrap(message.getBytes()).duplicate(),
                    serverSocket, "Client");

            //check if session is valid
            System.out.println(engine.getSession().isValid());

            return null;

        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch

        return new byte[0];
    } //end send message

    //receive message
    public DatagramMessage receiveMessage(String hostname, int port) {
        try {
            //receive ssl engine for client
            engine = DTLSEngine.createSSLEngine(true);

            //server socket
            //InetSocketAddress serverSocket = new InetSocketAddress(InetAddress.getByName(hostname), port);

            //handshaking
            //DTLSEngine.handshake(this.engine, mySocket, serverSocket, false);

            DatagramMessage receivedData = DTLSEngine.receiveAppData(this.engine, mySocket, "Client");

            if(receivedData == null) {
                System.out.println("No data received on client side");
            } else {
                System.out.println("Received message");
                //System.out.println(receivedData.getMessage());
                return receivedData;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch

        return null;
    } //end receive message
}
