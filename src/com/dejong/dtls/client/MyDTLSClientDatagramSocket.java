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

public class MyDTLSClientDatagramSocket extends DatagramSocket {

    private static final int MAX_LEN = 100;
    private static int port = 7;
    private static String hostname = "localhost";

    private SSLEngine engine;

    public MyDTLSClientDatagramSocket(int port) throws SocketException {
        super(port);
    }

    //send message
    public void sendMessage(String message, InetAddress receiverHost, int receiverPort) {
        try {

            engine = DTLSEngine.createSSLEngine(true);

            InetSocketAddress serverSocket = new InetSocketAddress(receiverHost, receiverPort);
            DTLSEngine.handshake(engine, this, serverSocket, false);

            //convert string to bytebuffer
            DTLSEngine.sendAppData(engine, this, ByteBuffer.wrap(message.getBytes()), serverSocket, "Client");

            //check if session is valid
            System.out.println(engine.getSession().isValid());

        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch
    } //end send message

    //receive message
    public String receiveMessage() {
        try {
            //receive sslengine for client
            engine = DTLSEngine.createSSLEngine(true);

            //receive message
            DatagramMessage receivedData = DTLSEngine.receiveAppData(engine, this, "Client");

            InetSocketAddress serverSocket = new InetSocketAddress(receivedData.getAddress(), receivedData.getPort());

            //handshaking
            DTLSEngine.handshake(engine, this, serverSocket, false);

            if(receivedData == null) {
                System.out.println("No data received on client side");
            } else {
                System.out.println("Received message");
                System.out.println(receivedData.getMessage());
                return receivedData.getMessage();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch

        return null;
    } //end receive message
}
