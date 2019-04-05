package com.dejong.dtls;

// reference: http://cr.openjdk.java.net/~amjiang/8145849/webrev.02/test/javax/net/ssl/ALPN/SSLEngineAlpnTest.java.html
// changes made: renamed file

/*
 * Copyright (c) 2003, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

// SunJSSE does not support dynamic system properties, no way to re-use
// system properties in samevm/agentvm mode.

/*
 * @test
 * @bug 8051498
 * @summary JEP 244: TLS Application-Layer Protocol Negotiation Extension
 * @run main/othervm SSLEngineAlpnTest h2          h2          h2
 * @run main/othervm SSLEngineAlpnTest h2          h2,http/1.1 h2
 * @run main/othervm SSLEngineAlpnTest h2,http/1.1 h2,http/1.1 h2
 * @run main/othervm SSLEngineAlpnTest http/1.1,h2 h2,http/1.1 http/1.1
 * @run main/othervm SSLEngineAlpnTest h4,h3,h2    h1,h2       h2
 * @run main/othervm SSLEngineAlpnTest EMPTY       h2,http/1.1 NONE
 * @run main/othervm SSLEngineAlpnTest h2          EMPTY       NONE
 * @run main/othervm SSLEngineAlpnTest H2          h2          ERROR
 * @run main/othervm SSLEngineAlpnTest h2          http/1.1    ERROR
 */

/**
 * A SSLEngine usage example which simplifies the presentation
 * by removing the I/O and multi-threading concerns.
 *
 * The demo creates two SSLEngines, simulating a client and server.
 * The "transport" layer consists two ByteBuffers:  think of them
 * as directly connected pipes.
 *
 * Note, this is a *very* simple example: real code will be much more
 * involved.  For example, different threading and I/O models could be
 * used, transport mechanisms could close unexpectedly, and so on.
 *
 * When this application runs, notice that several messages
 * (wrap/unwrap) pass before any application data is consumed or
 * produced.  (For more information, please see the SSL/TLS
 * specifications.)  There may several steps for a successful handshake,
 * so it's typical to see the following series of operations:
 *
 *      client          server          message
 *      ======          ======          =======
 *      wrap()          ...             ClientHello
 *      ...             unwrap()        ClientHello
 *      ...             wrap()          ServerHello/Certificate
 *      unwrap()        ...             ServerHello/Certificate
 *      wrap()          ...             ClientKeyExchange
 *      wrap()          ...             ChangeCipherSpec
 *      wrap()          ...             Finished
 *      ...             unwrap()        ClientKeyExchange
 *      ...             unwrap()        ChangeCipherSpec
 *      ...             unwrap()        Finished
 *      ...             wrap()          ChangeCipherSpec
 *      ...             wrap()          Finished
 *      unwrap()        ...             ChangeCipherSpec
 *      unwrap()        ...             Finished
 */

import com.dejong.server.DatagramMessage;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings("Duplicates")
public class DTLSEngine {

    /*
     * The following is to set up the keystores.
     */
    //private static final String pathToStores = "C:\\";
    private static final String keyStoreFile = "fms.jks";
    private static final String trustStoreFile = "public.jks";
    private static final String passwd = "ittralee";

    private static final int MAX_HANDSHAKE_LOOPS = 200;
    private static final int MAX_APP_READ_LOOPS = 60;
    private static final int SOCKET_TIMEOUT = Integer.getInteger("socket.timeout", 3 * 1000); // in millis
    private static final int BUFFER_SIZE = 1024;
    private static final int MAXIMUM_PACKET_SIZE = 1024;
    private static final boolean IS_SERVER = true;
    private static final boolean IS_CLIENT = false;

    private static final String keyFilename =
            //System.getProperty("test.src", ".") + "/" +
            //pathToStores + "\\" +
            keyStoreFile;
    private static final String trustFilename =
            //System.getProperty("test.src", ".") + "/" +
            //pathToStores + "\\" +
            trustStoreFile;
    private static Exception clientException = null;
    private static Exception serverException = null;

    //retrieve dtls context
    private static SSLContext getDTLSContext() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");

        char[] passphrase = passwd.toCharArray();

        try (FileInputStream fis = new FileInputStream(keyFilename)) {
            ks.load(fis, passphrase);
        }

        try (FileInputStream fis = new FileInputStream(trustFilename)) {
            ts.load(fis, passphrase);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        //retrieve instance of SSLContext with correspond to DTLS
        SSLContext sslCtx = SSLContext.getInstance("DTLS");

        //additional security information
        sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslCtx;
    }

    //create ssl engine
    public static SSLEngine createSSLEngine(boolean isClient) throws Exception {
        SSLContext context = getDTLSContext();
        SSLEngine engine = context.createSSLEngine();

        SSLParameters paras = engine.getSSLParameters();
        paras.setMaximumPacketSize(MAXIMUM_PACKET_SIZE);

        engine.setUseClientMode(isClient);
        engine.setSSLParameters(paras);

        return engine;
    }

    // handshake
    public static DatagramMessage handshake(SSLEngine engine, DatagramSocket socket,
                                            SocketAddress peerAddr, boolean isServer) throws Exception {

        boolean endLoops = false;
        int loops = MAX_HANDSHAKE_LOOPS;
        List<DatagramPacket> packets = new ArrayList<>();
        String side = isServer ? "Server" : "Client";
        engine.beginHandshake();
        while (!endLoops &&
                (serverException == null) && (clientException == null)) {

            if (--loops < 0) {
                throw new RuntimeException(
                        "Too much loops to produce handshake packets");
            }

            // Handshake statuses
            // NEED_WRAP: must send data to the remote side before handshaking can continue
            // NEED_UNWRAP: needs to receive data from the remote side before handshaking can continue.
            // NEED_UNWRAP_AGAIN: needs to receive data from the remote side before handshaking can continue again.
            SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
            if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP ||
                    hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN) {

//                log(side, "Need DTLS records, handshake status is " + hs);

                ByteBuffer iNet;
                ByteBuffer iApp;
                if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                    byte[] buf = new byte[BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
//                        log(side, "handshake(): wait for a packet");
                        socket.receive(packet);
//                        log(side, "handshake(): received a packet, length = "
//                                + packet.getLength());
                    } catch (SocketTimeoutException ste) {
//                        log(side, "Warning: " + ste);

                        packets.clear();
                        boolean finished = onReceiveTimeout(
                                engine, peerAddr, side, packets);

//                        log(side, "handshake(): receive timeout: re-send "
//                                + packets.size() + " packets");
                        for (DatagramPacket p : packets) {
                            socket.send(p);
                        }

                        if (finished) {
//                            log(side, "Handshake status is FINISHED "
//                                    + "after calling onReceiveTimeout(), "
//                                    + "finish the loop");
                            endLoops = true;
                        }

//                        log(side, "New handshake status is "
//                                + engine.getHandshakeStatus());

                        continue;
                    }

                    iNet = ByteBuffer.wrap(buf, 0, packet.getLength());
                    iApp = ByteBuffer.allocate(BUFFER_SIZE);
                } else {
                    iNet = ByteBuffer.allocate(0);
                    iApp = ByteBuffer.allocate(BUFFER_SIZE);
                }

                SSLEngineResult r = engine.unwrap(iNet, iApp);
                SSLEngineResult.Status rs = r.getStatus();
                hs = r.getHandshakeStatus();
                if (rs == SSLEngineResult.Status.OK) {
                    // OK
                } else if (rs == SSLEngineResult.Status.BUFFER_OVERFLOW) {
//                    log(side, "BUFFER_OVERFLOW, handshake status is " + hs);

                    // the client maximum fragment size config does not work?
                    throw new Exception("Buffer overflow: " +
                            "incorrect client maximum fragment size");
                } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
//                    log(side, "BUFFER_UNDERFLOW, handshake status is " + hs);

                    // bad packet, or the client maximum fragment size
                    // config does not work?
                    if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                        throw new Exception("Buffer underflow: " +
                                "incorrect client maximum fragment size");
                    } // otherwise, ignore this packet
                } else if (rs == SSLEngineResult.Status.CLOSED) {
                    throw new Exception(
                            "SSL engine closed, handshake status is " + hs);
                } else {
                    throw new Exception("Can't reach here, result is " + rs);
                }

                if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
//                    log(side, "Handshake status is FINISHED, finish the loop");
                    endLoops = true;
                }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                packets.clear();
                boolean finished = produceHandshakePackets(
                        engine, peerAddr, side, packets);

//                log(side, "handshake(): need wrap: send " + packets.size()
//                        + " packets");
                for (DatagramPacket p : packets) {
                    socket.send(p);
                }

                if (finished) {
//                    log(side, "Handshake status is FINISHED "
//                            + "after producing handshake packets, "
//                            + "finish the loop");
                    endLoops = true;
                }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                runDelegatedTasks(engine);
            } else if (hs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
//                log(side, "Handshake status is NOT_HANDSHAKING,"
//                        + " finish the loop");
                endLoops = true;
            } else if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
                throw new Exception(
                        "Unexpected status, SSLEngine.getHandshakeStatus() "
                                + "shouldn't return FINISHED");
            } else {
                throw new Exception("Can't reach here, handshake status is "
                        + hs);
            }
        }

        // in case of server, try to receive first application data
        //
        // if a client keeps sending handshake data it may mean
        // that some packets were lost (for example, FINISHED message),
        // and the server needs to re-send last handshake messages
        DatagramMessage appBuffer = null;
        if (isServer) {
            appBuffer = receiveAppData(engine, socket, side, (Callable) () -> {
                // looks like client didn't finish handshaking
                // because it keeps sending handshake messages
                //
                // re-send final handshake packets

                SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
//                log(side, "receiveAppData(): handshake status is " + hs);

                packets.clear();
                produceHandshakePackets(engine, peerAddr, side, packets);

//                log(side, "receiveAppData(): re-send " + packets.size()
//                        + " final packets");
                for (DatagramPacket p : packets) {
                    socket.send(p);
                }

                return null;
            });
        }

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
//        log(side, "Handshake finished, status is " + hs);

        if (engine.getHandshakeSession() != null) {
            throw new Exception(
                    "Handshake finished, but handshake session is not null");
        }

        SSLSession session = engine.getSession();
        if (session == null) {
            throw new Exception("Handshake finished, but session is null");
        }
//        log(side, "Negotiated protocol is " + session.getProtocol());
//        log(side, "Negotiated cipher suite is " + session.getCipherSuite());

        // handshake status should be NOT_HANDSHAKING
        //
        // according to the spec,
        // SSLEngine.getHandshakeStatus() can't return FINISHED
        if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            throw new Exception("Unexpected handshake status " + hs);
        }

        return appBuffer;
    }

    // produce handshake packets
    private static boolean produceHandshakePackets(SSLEngine engine, SocketAddress socketAddr,
                                           String side, List<DatagramPacket> packets) throws Exception {

        boolean endLoops = false;
        int loops = MAX_HANDSHAKE_LOOPS;
        while (!endLoops &&
                (serverException == null) && (clientException == null)) {

            if (--loops < 0) {
                throw new RuntimeException(
                        "Too much loops to produce handshake packets");
            }

            ByteBuffer oNet = ByteBuffer.allocate(32768);
            ByteBuffer oApp = ByteBuffer.allocate(0);
            SSLEngineResult r = engine.wrap(oApp, oNet);
            oNet.flip();

            SSLEngineResult.Status rs = r.getStatus();
            SSLEngineResult.HandshakeStatus hs = r.getHandshakeStatus();
            if (rs == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                // the client maximum fragment size config does not work?
                throw new Exception("Buffer overflow: " +
                        "incorrect server maximum fragment size");
            } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
//                log(side, "produceHandshakePackets(): BUFFER_UNDERFLOW");
//                log(side, "produceHandshakePackets(): handshake status: " + hs);
                // bad packet, or the client maximum fragment size
                // config does not work?
                if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                    throw new Exception("Buffer underflow: " +
                            "incorrect server maximum fragment size");
                } // otherwise, ignore this packet
            } else if (rs == SSLEngineResult.Status.CLOSED) {
                throw new Exception("SSLEngine has closed");
            } else if (rs == SSLEngineResult.Status.OK) {
                // OK
            } else {
                throw new Exception("Can't reach here, result is " + rs);
            }

            // SSLEngineResult.Status.OK:
            if (oNet.hasRemaining()) {
                byte[] ba = new byte[oNet.remaining()];
                oNet.get(ba);
                DatagramPacket packet = createHandshakePacket(ba, socketAddr);
                packets.add(packet);
            }

            if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
//                log(side, "produceHandshakePackets(): "
//                        + "Handshake status is FINISHED, finish the loop");
                return true;
            }

            boolean endInnerLoop = false;
            SSLEngineResult.HandshakeStatus nhs = hs;
            while (!endInnerLoop) {
                if (nhs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    runDelegatedTasks(engine);
                } else if (nhs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP ||
                        nhs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN ||
                        nhs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {

                    endInnerLoop = true;
                    endLoops = true;
                } else if (nhs == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                    endInnerLoop = true;
                } else if (nhs == SSLEngineResult.HandshakeStatus.FINISHED) {
                    throw new Exception(
                            "Unexpected status, SSLEngine.getHandshakeStatus() "
                                    + "shouldn't return FINISHED");
                } else {
                    throw new Exception("Can't reach here, handshake status is "
                            + nhs);
                }
                nhs = engine.getHandshakeStatus();
            }
        }

        return false;
    }

    // deliver application data
    public static void sendAppData(SSLEngine engine, DatagramSocket socket,
                                   ByteBuffer appData, SocketAddress peerAddr, String side)
            throws Exception {

        List<DatagramPacket> packets =
                produceApplicationPackets(engine, appData, peerAddr);
        appData.flip();
//        log(side, "sendAppData(): send " + packets.size() + " packets");
        for (DatagramPacket p : packets) {
            socket.send(p);
        }
    }

    private static DatagramPacket createHandshakePacket(byte[] ba, SocketAddress socketAddr) {
        return new DatagramPacket(ba, ba.length, socketAddr);
    }

    // produce application packets
    private static List<DatagramPacket> produceApplicationPackets(
            SSLEngine engine, ByteBuffer source,
            SocketAddress socketAddr) throws Exception {

        List<DatagramPacket> packets = new ArrayList<>();
        ByteBuffer appNet = ByteBuffer.allocate(32768);
        SSLEngineResult r = engine.wrap(source, appNet);
        appNet.flip();

        SSLEngineResult.Status rs = r.getStatus();
        if (rs == SSLEngineResult.Status.BUFFER_OVERFLOW) {
            // the client maximum fragment size config does not work?
            throw new Exception("Buffer overflow: " +
                    "incorrect server maximum fragment size");
        } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
            // unlikely
            throw new Exception("Buffer underflow during wraping");
        } else if (rs == SSLEngineResult.Status.CLOSED) {
            throw new Exception("SSLEngine has closed");
        } else if (rs == SSLEngineResult.Status.OK) {
            // OK
        } else {
            throw new Exception("Can't reach here, result is " + rs);
        }

        // SSLEngineResult.Status.OK:
        if (appNet.hasRemaining()) {
            byte[] ba = new byte[appNet.remaining()];
            appNet.get(ba);
            DatagramPacket packet =
                    new DatagramPacket(ba, ba.length, socketAddr);
            packets.add(packet);
        }

        return packets;
    }

    // retransmission if timeout
    private static boolean onReceiveTimeout(SSLEngine engine, SocketAddress socketAddr,
                                    String side, List<DatagramPacket> packets) throws Exception {

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        if (hs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            return false;
        } else {
            // retransmission of handshake messages
            return produceHandshakePackets(engine, socketAddr, side, packets);
        }
    }

    public static DatagramMessage receiveAppData(SSLEngine engine, DatagramSocket socket,
                                                 String side) throws Exception {
        return receiveAppData(engine, socket, side, null);
    }

    // receive application data
    // the method returns not-null if data received
    private static DatagramMessage receiveAppData(SSLEngine engine, DatagramSocket socket,
                                                 String side, Callable onHandshakeMessage)
            throws Exception {

        int loops = MAX_APP_READ_LOOPS;
        while ((serverException == null) && (clientException == null)) {
            if (--loops < 0) {
                throw new RuntimeException(
                        "Too much loops to receive application data");
            }

            byte[] buf = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
//            log(side, "receiveAppData(): wait for a packet");
            try {
                socket.receive(packet);
//                log(side, "receiveAppData(): received a packet");
            } catch (SocketTimeoutException e) {
                log(side, "Warning: " + e);
                continue;
            }



            ByteBuffer netBuffer = ByteBuffer.wrap(buf, 0, packet.getLength());
            ByteBuffer recBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            SSLEngineResult rs = engine.unwrap(netBuffer, recBuffer);
//            log(side, "receiveAppData(): engine status is " + rs);
            recBuffer.flip();
            if (recBuffer.remaining() != 0) {
                //return recBuffer;
                //System.out.println(new String(recBuffer.array()));
                DatagramMessage returnVal = new DatagramMessage( );
                returnVal.putVal(new String(recBuffer.array()),
                        packet.getAddress( ),
                        packet.getPort( ));
                return returnVal;
            }

            if (onHandshakeMessage != null) {
                onHandshakeMessage.call();
            }
        }

        return null;
    }

    // run delegated tasks
    static void runDelegatedTasks(SSLEngine engine) throws Exception {
        Runnable runnable;
        while ((runnable = engine.getDelegatedTask()) != null) {
            runnable.run();
        }

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            throw new Exception("handshake shouldn't need additional tasks");
        }
    }

    static void log(String side, String message) {
        System.out.println(side + ": " + message);
    }
}