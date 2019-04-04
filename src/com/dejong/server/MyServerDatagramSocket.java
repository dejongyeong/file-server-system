package com.dejong.server;

import com.dejong.dtls.DTLSEngine;
import com.dejong.utils.SSLClientServerDatagramSocket;

import javax.net.ssl.SSLEngine;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * A subclass of DatagramSocket which contains methods for sending
 * and receiving messages.
 *
 * @author M. L. Liu
 *
 * Note: class reused from Distributed Computing lab.
 *
 **/

@SuppressWarnings("Duplicates")
public class MyServerDatagramSocket extends SSLClientServerDatagramSocket {

    private SSLEngine engine;
    private DatagramSocket socket;
    private static int SERVER_PORT = 7;

    //initialize server datagram socket
    public MyServerDatagramSocket() throws SocketException {
        this.socket = new DatagramSocket(SERVER_PORT);
    }

    //send message
    protected void sendMessage(InetAddress receiverHost, int receiverPort, String message) {
        try {
            //client socket address
            InetSocketAddress client = new InetSocketAddress(receiverHost, receiverPort);

            //engine send wrap data
            DTLSEngine.sendAppData(this.engine, socket, ByteBuffer.wrap(message.getBytes()), client, "Server");
            System.out.println("Data sent to client");

        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch
    }

    @Override
    public DatagramMessage receiveMessage(InetAddress hostname, int port) {
        try {
            //create ssl engine for server
            engine = DTLSEngine.createSSLEngine(false);

            //client socket address
            InetSocketAddress clientSocket = new InetSocketAddress(hostname, port);

            //handshaking
            DatagramMessage appData = DTLSEngine.handshake(engine, socket, clientSocket, true);

            //send message
            if(appData == null) {
                System.out.println("No data received on server side.");
            } else {
                System.out.println("Message received");
                return appData;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch

        return null;
    }
}


//public class MyServerDatagramSocket extends ClientServerDatagramSocket {
//
//    static final int MAX_LEN = 100;
//
//    MyServerDatagramSocket() throws SocketException {
//        super();
//    }
//
//    MyServerDatagramSocket(int port) throws SocketException {
//        super(port);
//    }
//
//    public void sendMessage(InetAddress receiverHost, int receiverPort, String message) throws IOException {
//        super.sendMessage(receiverHost, receiverPort, message);
//    } //end sendMessage
//
//    public String receiveMessage() throws IOException {
//        return super.receiveMessage();
//    } //end receiveMessage
//
//    public DatagramMessage receiveMessageAndSender() throws IOException {
//        byte[] receiveBuffer = new byte[MAX_LEN];
//        DatagramPacket datagram = new DatagramPacket(receiveBuffer, MAX_LEN);
//        this.receive(datagram);
//
//        //create a DatagramMessage object to contain message received and sender's address.
//        DatagramMessage returnVal = new DatagramMessage();
//        returnVal.putVal(new String(receiveBuffer), datagram.getAddress(), datagram.getPort());
//        return returnVal;
//    }
//} //end class
