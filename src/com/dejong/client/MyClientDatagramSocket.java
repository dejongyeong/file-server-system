package com.dejong.client;

import com.dejong.dtls.DTLSEngine;
import com.dejong.server.DatagramMessage;
import com.dejong.utils.SSLClientServerDatagramSocket;

import javax.net.ssl.SSLEngine;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * A subclass of DatagramSocket which contains methods for sending and
 * receiving messages.
 *
 * @author  M. L. Liu
 *
 * Note: class reused from Distributed Computing lab.
 *
 **/

@SuppressWarnings("Duplicates")
public class MyClientDatagramSocket extends SSLClientServerDatagramSocket {

    private static int CLIENT_PORT = 8;
    private SSLEngine engine;
    private DatagramSocket socket;

    //constructor
    public MyClientDatagramSocket() throws SocketException {
        this.socket = new DatagramSocket(CLIENT_PORT);
    }

    public void setSSLEngine(SSLEngine engine) {
        this.engine = engine;
    }

    //client send message
    public void sendMessage(InetAddress receiverHost, int receiverPort, String message) {
        try {
            //server socket address
            InetSocketAddress server = new InetSocketAddress(receiverHost, receiverPort);
            DTLSEngine.handshake(engine, socket, server, false); //handshake

            //wrap and send data
            DTLSEngine.sendAppData(engine, socket, ByteBuffer.wrap(message.getBytes()).duplicate(), server, "Client");

        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch
    }

    //client receive message
    @Override
    public DatagramMessage receiveMessage(InetAddress hostname, int port) {
        try {
            //receive data
            DatagramMessage receivedData = DTLSEngine.receiveAppData(engine, socket, "Client");

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
    }

    //disconnect
    public void disconnect() {
        socket.close();
    }

    //testing purpose
    //send message
    public byte[] sendMessage(String receiverHost, int receiverPort, String message) {
        try {
            //server socket
            InetSocketAddress serverSocket = new InetSocketAddress(receiverHost, receiverPort);
            DTLSEngine.handshake(engine, socket, serverSocket, false);

            //convert string to byte buffer
            DTLSEngine.sendAppData(engine, socket, ByteBuffer.wrap(message.getBytes()).duplicate(),
                    serverSocket, "Client");

            return null;

        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch

        return new byte[0];
    } //end send message

}

//public class MyClientDatagramSocket extends ClientServerDatagramSocket {
//
//    MyClientDatagramSocket() throws SocketException {
//        super();
//    }
//
//    MyClientDatagramSocket(int portNo) throws SocketException {
//        super(portNo);
//    }
//
//    public void sendMessage(InetAddress receiverHost, int receiverPort, String message) throws IOException {
//        super.sendMessage(receiverHost, receiverPort, message);
//    }
//
//    public String receiveMessage() throws IOException {
//        return super.receiveMessage();
//    }
//}
