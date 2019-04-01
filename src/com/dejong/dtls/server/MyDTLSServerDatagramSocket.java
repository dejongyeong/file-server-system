package com.dejong.dtls.server;

import com.dejong.dtls.utils.DTLSEngine;
import com.dejong.server.DatagramMessage;

import javax.net.ssl.SSLEngine;
import java.net.*;
import java.nio.ByteBuffer;

public class MyDTLSServerDatagramSocket {

    private SSLEngine engine;
    private DatagramSocket mySocket;
    private static int DEFAULT_PORT = 7;

    //constructor
    public MyDTLSServerDatagramSocket() {
        try {
            this.mySocket = new DatagramSocket(DEFAULT_PORT);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    //return ssl engine
    public SSLEngine getEngine() {
        return this.engine;
    }

    //send message
    public void sendMessage(SSLEngine engine, InetAddress receiverHost, int receiverPort, String message) {
        try {
            //client socket address
            InetSocketAddress clientSocket = new InetSocketAddress(receiverHost, receiverPort);

            //engine wrap data
            DTLSEngine.sendAppData(engine, mySocket, ByteBuffer.wrap(message.getBytes()), clientSocket, "Server");
            System.out.println("Data sent to Client");

        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch
    }

    //receive message
    public DatagramMessage receiveMessage(String hostname, int port) {
        try {
            //create ssl engine for server
            engine = DTLSEngine.createSSLEngine(false);

            System.out.println(engine.getSession().isValid() + "---");

            //client socket address
            InetSocketAddress clientSocket = new InetSocketAddress(InetAddress.getByName(hostname), port);

            //handshaking
            DatagramMessage appData = DTLSEngine.handshake(engine, mySocket, clientSocket, true);

            //send message
            if(appData == null) {
                System.out.println("No data received on server side.");
            } else {
                System.out.println("Message received");
                //System.out.println(appData.getMessage());
                return appData;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch

        return null;
    }

}
