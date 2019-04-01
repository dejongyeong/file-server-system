package com.dejong.dtls;

/**
 * Note to invigilator:
 * Implementations on Datagram Secure Socket Layer were discussed with classmates, thus,
 * code implementation with classmates will be similar.
 *
 * reference: https://www.programcreek.com/java-api-examples/index.php?source_dir=usc-master/usc-channel-impl/src/main/java/org/opendaylight/usc/crypto/dtls/DtlsClient.java
 *
 */

import com.dejong.server.DatagramMessage;

import javax.net.ssl.SSLEngine;
import java.net.*;
import java.nio.ByteBuffer;

public class DTLSClient {

    private static int port = 7;

    private SSLEngine engine;
    private DatagramSocket mySocket;

    public DTLSClient() throws SocketException {
        this.mySocket = new DatagramSocket(port);
    }

    public byte[] send(ByteBuffer message, String hostName, int portNum) {
        try {
            engine = DTLSEngine.createSSLEngine(true);

            InetSocketAddress serverSocketAddr = new InetSocketAddress(InetAddress.getByName(hostName), portNum);
            DTLSEngine.handshake(engine, mySocket, serverSocketAddr, false);

            DTLSEngine.sendAppData(engine, mySocket, message.duplicate(), serverSocketAddr, "Client");

            System.out.println(engine.getSession().isValid());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    public DatagramMessage receive(String hostName, int portNum) {
        try {
            engine = DTLSEngine.createSSLEngine(true);

            InetSocketAddress serverSocketAddr = new InetSocketAddress(InetAddress.getByName(hostName), portNum);

            // handshaking
            DTLSEngine.handshake(engine, mySocket, serverSocketAddr, false);

            DatagramMessage receivedData = DTLSEngine.receiveAppData(engine, mySocket, "Client");

            if (receivedData == null) {
                System.out.println("No Application data received on client side.");
            } else {
                System.out.println("Received Message");
                System.out.println(receivedData.getMessage());
                return receivedData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } //end catch

        return null;
    }

    public DatagramMessage sendAndReceive(ByteBuffer message, String hostName, int portNum) {
        try {
            engine = DTLSEngine.createSSLEngine(true);

            InetSocketAddress serverSocketAddr = new InetSocketAddress(InetAddress.getByName(hostName), portNum);

            DTLSEngine.handshake(engine, mySocket, serverSocketAddr, false);

            DTLSEngine.sendAppData(engine, mySocket, message.duplicate(), serverSocketAddr, "Client");

            System.out.println("Data Sent");

            DatagramMessage receivedData = DTLSEngine.receiveAppData(engine, mySocket, "Client");

            if (receivedData == null) {
                System.out.println("No data received on client side.");
            } else {
                System.out.println("Received Message");
                System.out.println(receivedData.getMessage());
                return receivedData;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
