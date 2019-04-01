package com.dejong.dtls.utils;

// reference: http://cr.openjdk.java.net/~asmotrak/8159416/webrev.08/test/javax/net/ssl/DTLS/DTLSOverDatagram.java.html

/* Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8043758
 * @summary Datagram Transport Layer Security (DTLS)
 * @modules java.base/sun.security.util
 * @run main/othervm DTLSOverDatagram
 */

import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;
import java.security.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.net.ssl.*;

import sun.security.util.HexDumpEncoder;

/**
 * An example to show the way to use SSLEngine in datagram connections.
 */
@SuppressWarnings("Duplicates")
public class DTLSOverDatagram {

    /*static {
        // set a custom DatagramSocketImplFactory which can drop packets
        try {
            DatagramSocket.setDatagramSocketImplFactory(
                    new CustomDatagramSocketImplFactory());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    private static final int MAX_HANDSHAKE_LOOPS = 200;
    private static final int MAX_APP_READ_LOOPS = 60;
    private static final int SOCKET_TIMEOUT = Integer.getInteger("socket.timeout", 3 * 1000); // in millis
    private static final int BUFFER_SIZE = 1024;
    private static final int MAXIMUM_PACKET_SIZE = 1024;
    private static final boolean IS_SERVER = true;
    private static final boolean IS_CLIENT = false;

    /*
     * The following is to set up the keystores.
     */
    //private static final String pathToStores = "C:\\";
    private static final String keyStoreFile = "fms.jks";
    private static final String trustStoreFile = "public.jks";
    private static final String passwd = "ittralee";

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

    // these data are supposed to be read-only
    private static final ByteBuffer serverApp =
            ByteBuffer.wrap("Hi Client, I'm Server".getBytes());
    private static final ByteBuffer clientApp =
            ByteBuffer.wrap("Hi Server, I'm Client".getBytes());

    /*
     * =============================================================
     * The test case
     */
    public static void main(String[] args) throws Exception {
        DTLSOverDatagram testCase = new DTLSOverDatagram();
        testCase.runTest(testCase);
    }

    /*
     * Define the server side of the test.
     */
    void doServerSide(DatagramSocket socket, InetSocketAddress clientSocketAddr) throws Exception {

        // create SSLEngine
        SSLEngine engine = createSSLEngine(false);

        // handshaking
        ByteBuffer appData = handshake(
                engine, socket, clientSocketAddr, IS_SERVER);

        // write server application data
        sendAppData(engine, socket, serverApp.duplicate(),
                clientSocketAddr, "Server");

        if (appData == null) {
            throw new Exception("No application data received on server side");
        }

        printHex("Server received application data", appData);
        printHex("Server expected application data", clientApp);
        if (!appData.equals(clientApp)) {
            throw new Exception("Unexpected application data on client side");
        }
    }

    /*
     * Define the client side of the test.
     */
    void doClientSide(DatagramSocket socket, InetSocketAddress serverSocketAddr)
            throws Exception {

        // create SSLEngine
        SSLEngine engine = createSSLEngine(true);

        // handshaking
        handshake(engine, socket, serverSocketAddr, IS_CLIENT);

        // write client application data
        sendAppData(engine, socket, clientApp.duplicate(),
                serverSocketAddr, "Client");

        // read server application data
        ByteBuffer appData = receiveAppData(engine, socket, "Client");

        if (appData == null) {
            throw new Exception("No application data received on client side");
        }

        log("Client", new String(appData.array()));

        /*printHex("Client received application data", appData);
        printHex("Client expected application data", serverApp);*/
        if (!appData.equals(serverApp)) {
            throw new Exception("Unexpected application data on client side");
        }
    }

    /*
     * =============================================================
     * The remainder is support stuff for DTLS operations.
     */
    SSLEngine createSSLEngine(boolean isClient) throws Exception {
        SSLContext context = getDTLSContext();
        SSLEngine engine = context.createSSLEngine();

        SSLParameters paras = engine.getSSLParameters();
        paras.setMaximumPacketSize(MAXIMUM_PACKET_SIZE);

        engine.setUseClientMode(isClient);
        engine.setSSLParameters(paras);

        return engine;
    }

    // handshake
    ByteBuffer handshake(SSLEngine engine, DatagramSocket socket,
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

                log(side, "Need DTLS records, handshake status is " + hs);

                ByteBuffer iNet; //
                ByteBuffer iApp; //
                if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                    byte[] buf = new byte[BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        log(side, "handshake(): wait for a packet");
                        socket.receive(packet);
                        log(side, "handshake(): received a packet, length = "
                                + packet.getLength());
                    } catch (SocketTimeoutException ste) {
                        log(side, "Warning: " + ste);

                        packets.clear();
                        boolean finished = onReceiveTimeout(
                                engine, peerAddr, side, packets);

                        log(side, "handshake(): receive timeout: re-send "
                                + packets.size() + " packets");
                        for (DatagramPacket p : packets) {
                            socket.send(p);
                        }

                        if (finished) {
                            log(side, "Handshake status is FINISHED "
                                    + "after calling onReceiveTimeout(), "
                                    + "finish the loop");
                            endLoops = true;
                        }

                        log(side, "New handshake status is "
                                + engine.getHandshakeStatus());

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
                    log(side, "BUFFER_OVERFLOW, handshake status is " + hs);

                    // the client maximum fragment size config does not work?
                    throw new Exception("Buffer overflow: " +
                            "incorrect client maximum fragment size");
                } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    log(side, "BUFFER_UNDERFLOW, handshake status is " + hs);

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
                    log(side, "Handshake status is FINISHED, finish the loop");
                    endLoops = true;
                }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                packets.clear();
                boolean finished = produceHandshakePackets(
                        engine, peerAddr, side, packets);

                log(side, "handshake(): need wrap: send " + packets.size()
                        + " packets");
                for (DatagramPacket p : packets) {
                    socket.send(p);
                }

                if (finished) {
                    log(side, "Handshake status is FINISHED "
                            + "after producing handshake packets, "
                            + "finish the loop");
                    endLoops = true;
                }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                log(side, "NEED TASK");
                runDelegatedTasks(engine);
            } else if (hs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                log(side, "Handshake status is NOT_HANDSHAKING,"
                        + " finish the loop");
                endLoops = true;
            } else if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
                throw new Exception(
                        "Unexpected status, SSLEngine.getHandshakeStatus() "
                                + "shouldn't return FINISHED");
            } else {
                throw new Exception("Can't reach here, handshake status is " + hs);
            }
        }

        // in case of server, try to receive first application data
        //
        // if a client keeps sending handshake data it may mean
        // that some packets were lost (for example, FINISHED message),
        // and the server needs to re-send last handshake messages
        ByteBuffer appBuffer = null;
        if (isServer) {
            appBuffer = receiveAppData(engine, socket, side, (Callable) () -> {
                // looks like client didn't finish handshaking
                // because it keeps sending handshake messages
                //
                // re-send final handshake packets

                SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
                log(side, "receiveAppData(): handshake status is " + hs);

                packets.clear();
                produceHandshakePackets(engine, peerAddr, side, packets);

                log(side, "receiveAppData(): re-send " + packets.size()
                        + " final packets");
                for (DatagramPacket p : packets) {
                    socket.send(p);
                }

                return null;
            });
        }

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        log(side, "Handshake finished, status is " + hs);

        if (engine.getHandshakeSession() != null) {
            throw new Exception(
                    "Handshake finished, but handshake session is not null");
        }

        SSLSession session = engine.getSession();
        if (session == null) {
            throw new Exception("Handshake finished, but session is null");
        }
        log(side, "Negotiated protocol is " + session.getProtocol());
        log(side, "Negotiated cipher suite is " + session.getCipherSuite());

        // handshake status should be NOT_HANDSHAKING
        //
        // according to the spec,
        // SSLEngine.getHandshakeStatus() can't return FINISHED
        if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            throw new Exception("Unexpected handshake status " + hs);
        }

        return appBuffer;
    }

    // deliver application data
    void sendAppData(SSLEngine engine, DatagramSocket socket,
                     ByteBuffer appData, SocketAddress peerAddr, String side)
            throws Exception {

        List<DatagramPacket> packets =
                produceApplicationPackets(engine, appData, peerAddr);
        appData.flip();
        log(side, "sendAppData(): send " + packets.size() + " packets");
        for (DatagramPacket p : packets) {
            socket.send(p);
        }
    }

    ByteBuffer receiveAppData(SSLEngine engine, DatagramSocket socket,
                              String side) throws Exception {
        return receiveAppData(engine, socket, side, null);
    }

    // receive application data
    // the method returns not-null if data received
    ByteBuffer receiveAppData(SSLEngine engine, DatagramSocket socket,
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
            log(side, "receiveAppData(): wait for a packet");
            try {
                socket.receive(packet);
                log(side, "receiveAppData(): received a packet");
            } catch (SocketTimeoutException e) {
                log(side, "Warning: " + e);
                continue;
            }
            ByteBuffer netBuffer = ByteBuffer.wrap(buf, 0, packet.getLength());
            ByteBuffer recBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            SSLEngineResult rs = engine.unwrap(netBuffer, recBuffer);
            log(side, "receiveAppData(): engine status is " + rs);
            recBuffer.flip();
            if (recBuffer.remaining() != 0) {
                return recBuffer;
            }

            if (onHandshakeMessage != null) {
                onHandshakeMessage.call();
            }
        }

        return null;
    }

    // produce handshake packets
    boolean produceHandshakePackets(SSLEngine engine, SocketAddress socketAddr,
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
                log(side, "produceHandshakePackets(): BUFFER_UNDERFLOW");
                log(side, "produceHandshakePackets(): handshake status: " + hs);
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
                log(side, "produceHandshakePackets(): "
                        + "Handshake status is FINISHED, finish the loop");
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

    DatagramPacket createHandshakePacket(byte[] ba, SocketAddress socketAddr) {
        return new DatagramPacket(ba, ba.length, socketAddr);
    }

    // produce application packets
    List<DatagramPacket> produceApplicationPackets(
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

    // run delegated tasks
    void runDelegatedTasks(SSLEngine engine) throws Exception {
        Runnable runnable;
        while ((runnable = engine.getDelegatedTask()) != null) {
            runnable.run();
        }

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            throw new Exception("handshake shouldn't need additional tasks");
        }
    }

    // retransmission if timeout
    boolean onReceiveTimeout(SSLEngine engine, SocketAddress socketAddr,
                             String side, List<DatagramPacket> packets) throws Exception {

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        if (hs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            return false;
        } else {
            // retransmission of handshake messages
            return produceHandshakePackets(engine, socketAddr, side, packets);
        }
    }

    // get DTSL context
    SSLContext getDTLSContext() throws Exception {
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

        SSLContext sslCtx = SSLContext.getInstance("DTLS");

        sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslCtx;
    }


    /*
     * =============================================================
     * The remainder is support stuff to kickstart the testing.
     */

    // Will the handshaking and application data exchange succeed?
    public boolean isGoodJob() {
        return true;
    }

    public final void runTest(DTLSOverDatagram testCase) throws Exception {
        try (DatagramSocket serverSocket = new DatagramSocket();
             DatagramSocket clientSocket = new DatagramSocket()) {

            serverSocket.setSoTimeout(SOCKET_TIMEOUT);
            clientSocket.setSoTimeout(SOCKET_TIMEOUT);

            InetSocketAddress serverSocketAddr = new InetSocketAddress(
                    InetAddress.getLocalHost(), serverSocket.getLocalPort());

            InetSocketAddress clientSocketAddr = new InetSocketAddress(
                    InetAddress.getLocalHost(), clientSocket.getLocalPort());

            ExecutorService pool = Executors.newFixedThreadPool(2);
            Future<String> server, client;

            try {
                server = pool.submit(new ServerCallable(
                        testCase, serverSocket, clientSocketAddr));
                client = pool.submit(new ClientCallable(
                        testCase, clientSocket, serverSocketAddr));
            } finally {
                pool.shutdown();
            }

            boolean failed = false;

            // wait for client to finish
            try {
                System.out.println("Client finished: " + client.get());
            } catch (CancellationException | InterruptedException
                    | ExecutionException e) {
                System.out.println("Exception on client side: ");
                e.printStackTrace(System.out);
                failed = true;
            }

            // wait for server to finish
            try {
                System.out.println("Client finished: " + server.get());
            } catch (CancellationException | InterruptedException
                    | ExecutionException e) {
                System.out.println("Exception on server side: ");
                e.printStackTrace(System.out);
                failed = true;
            }

            if (failed) {
                throw new RuntimeException("Test failed");
            }
        }
    }

    final static class ServerCallable implements Callable<String> {

        private final DTLSOverDatagram testCase;
        private final DatagramSocket socket;
        private final InetSocketAddress clientSocketAddr;

        ServerCallable(DTLSOverDatagram testCase, DatagramSocket socket,
                       InetSocketAddress clientSocketAddr) {

            this.testCase = testCase;
            this.socket = socket;
            this.clientSocketAddr = clientSocketAddr;
        }

        @Override
        public String call() throws Exception {
            Thread.currentThread().setName("Server thread");
            try {
                testCase.doServerSide(socket, clientSocketAddr);
            } catch (Exception e) {
                System.out.println("Exception in  ServerCallable.call():");
                e.printStackTrace(System.out);
                serverException = e;

                if (testCase.isGoodJob()) {
                    throw e;
                } else {
                    return "Well done, server!";
                }
            }

            if (testCase.isGoodJob()) {
                return "Well done, server!";
            } else {
                throw new Exception("No expected exception");
            }
        }
    }

    final static class ClientCallable implements Callable<String> {

        private final DTLSOverDatagram testCase;
        private final DatagramSocket socket;
        private final InetSocketAddress serverSocketAddr;

        ClientCallable(DTLSOverDatagram testCase, DatagramSocket socket,
                       InetSocketAddress serverSocketAddr) {

            this.testCase = testCase;
            this.socket = socket;
            this.serverSocketAddr = serverSocketAddr;
        }

        @Override
        public String call() throws Exception {
            Thread.currentThread().setName("Client thread");
            try {
                testCase.doClientSide(socket, serverSocketAddr);
            } catch (Exception e) {
                System.out.println("Exception in ClientCallable.call():");
                e.printStackTrace(System.out);
                clientException = e;

                if (testCase.isGoodJob()) {
                    throw e;
                } else {
                    return "Well done, client!";
                }
            }

            if (testCase.isGoodJob()) {
                return "Well done, client!";
            } else {
                throw new Exception("No expected exception");
            }
        }
    }

    final static void printHex(String prefix, ByteBuffer bb) {
        HexDumpEncoder  dump = new HexDumpEncoder();

        synchronized (System.out) {
            System.out.println(prefix);
            try {
                dump.encodeBuffer(bb.slice(), System.out);
            } catch (Exception e) {
                System.out.println("Unexpected exception while printing data");
                e.printStackTrace(System.out);
            }
            System.out.flush();
        }
    }

    final static void printHex(String prefix,
                               byte[] bytes, int offset, int length) {

        printHex(prefix, ByteBuffer.wrap(bytes, offset, length));
    }

    static void log(String side, String message) {
        System.out.println(side + ": " + message);
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // this factory creates DatagramSocketImpl instances
    // which emulate UDP connections and packet loss
    static class CustomDatagramSocketImplFactory
            implements DatagramSocketImplFactory {

        static enum DropMode {

            // drop packets randomly with specified loss rate
            RANDOMLY,

            // drop only specified packets
            SPECIFIC
        }

        static DropMode mode = DropMode.RANDOMLY;
        static final Random random;
        static final double DEFAULT_PACKET_LOSS_RATE = 0.05;
        static {
            long seed = Long.getLong("seed", new Random().nextLong());
            System.out.println("CustomDatagramSocketImplFactory: seed: "
                    + seed);
            random = new Random(seed);
        }

        // maps a port number to a socket
        private static final Map<Integer, CustomDatagramSocketImpl> sockets =
                new HashMap<>();

        // next available port number
        private static int port = 0;

        // if true, it's going to drop some packets
        private static boolean dropPackets = true;

        // it counts sent packets to be able to drop them by a number
        private static int packetCounter = 0;

        // numbers of packets which should be dropped
        private static final Set<Integer> packetsToDrop = new HashSet<>();

        @Override
        public synchronized DatagramSocketImpl createDatagramSocketImpl() {
            return new CustomDatagramSocketImpl();
        }

        // returns a total number of packets that were sent
        static synchronized int getTotalPackets() {
            return packetCounter;
        }

        static synchronized void resetPacketCounter() {
            packetCounter = 0;
        }

        // specifies numbers of packets that should be dropped
        static synchronized void setPacketsToDrop(Set<Integer> packetsToDrop) {
            mode = DropMode.SPECIFIC;
            CustomDatagramSocketImplFactory.packetsToDrop.clear();
            CustomDatagramSocketImplFactory.packetsToDrop.addAll(packetsToDrop);
        }

        // checks if a packet should be dropped,
        // and returns true if a packet should be dropped
        static synchronized boolean losePacket() {
            packetCounter++;

            if (!dropPackets) {
                return false;
            }

            if (mode == DropMode.RANDOMLY
                    && random.nextDouble() < DEFAULT_PACKET_LOSS_RATE) {
                System.out.println(
                        "CustomDatagramSocketImplFactory: "
                                + "randomly lose a packet ("
                                + packetCounter
                                + ")");
                return true;
            } else if (mode == DropMode.SPECIFIC
                    && packetsToDrop.contains(packetCounter)) {
                System.out.println(
                        "CustomDatagramSocketImplFactory: "
                                + "lose specified packet ("
                                + packetCounter
                                + ")");
                return true;
            }

            return false;
        }

        // returns next available port number
        static synchronized int nextPort() {
            return ++port;
        }

        static synchronized int addSocket(CustomDatagramSocketImpl socket) {
            int nextPort = nextPort();
            sockets.put(nextPort, socket);
            return nextPort;
        }

        static synchronized CustomDatagramSocketImpl findSocket(int port) {
            return sockets.get(port);
        }

        static synchronized void removeSocket(int port) {
            sockets.remove(port);
        }

        // disable dropping packets
        static synchronized void disablePacketDrop() {
            dropPackets = false;
        }

        // enable dropping packets
        static synchronized void enablePacketDrop() {
            dropPackets = true;
        }
    }

    // DatagramSocketImpl implementation
    // which emulates UDP connections with packet loss
    static class CustomDatagramSocketImpl extends DatagramSocketImpl {

        /*
         * See https://tools.ietf.org/html/rfc5246#appendix-A.1 for details
         *
         * enum {
         *     change_cipher_spec(20), alert(21), handshake(22),
         *     application_data(23), (255)
         * } ContentType;
         *
         */
        private static final byte DTLS_APPLICATION_DATA_TYPE = 23;

        // socket timeout (set by SO_TIMEOUT socket option)
        private int timeout = 0;

        // packets queue
        private final LinkedList<byte[]> packets = new LinkedList<>();

        private void addPacket(byte[] data) {
            // check if a packet should be dropped
            // don't drop application data
            if (data[0] != DTLS_APPLICATION_DATA_TYPE
                    && CustomDatagramSocketImplFactory.losePacket()) {
                return;
            }

            // put a packet to queue,
            // and notify other sockets which wait in receive() method
            synchronized (packets) {
                packets.add(data.clone());
                packets.notifyAll();
            }
        }

        @Override
        protected void create() throws SocketException {
            System.out.println("CustomDatagramSocketImpl: create() called");
        }

        @Override
        protected void bind(int port, InetAddress addr) throws SocketException {
            System.out.println("CustomDatagramSocketImpl: bind() called: " +
                    ", port = " + port);
            if (port != 0) {
                throw new BindException("Couldn't bind to " + port
                        + " port (not supported)");
            }
            localPort = CustomDatagramSocketImplFactory.addSocket(this);
        }

        @Override
        protected void send(DatagramPacket p) throws IOException {
            System.out.println("CustomDatagramSocketImpl: send() called, "
                    + "length = " + p.getLength());
            int port = 0;
            if (p.getPort() > 0) {
                port = p.getPort();
            } else if (p.getSocketAddress() instanceof InetSocketAddress) {
                port = ((InetSocketAddress) p.getSocketAddress()).getPort();
            } else {
                throw new IOException(
                        "Couldn't send a packet (undefined port)");
            }
            CustomDatagramSocketImpl dstSocket =
                    CustomDatagramSocketImplFactory.findSocket(port);
            if (dstSocket == null) {
                throw new PortUnreachableException("port is " + port);
            }
            dstSocket.addPacket(p.getData());
        }

        @Override
        protected int peek(InetAddress i) throws IOException {
            System.out.println("CustomDatagramSocketImpl: peek() called");
            throw new UnsupportedOperationException();
        }

        @Override
        protected int peekData(DatagramPacket p) throws IOException {
            System.out.println("CustomDatagramSocketImpl: peekData() called");
            throw new UnsupportedOperationException();
        }

        @Override
        protected void receive(DatagramPacket dst) throws IOException {
            System.out.println("CustomDatagramSocketImpl: receive() called");
            long time = System.currentTimeMillis();
            synchronized (packets) {
                while (packets.isEmpty()) {
                    try {
                        packets.wait(timeout);
                    } catch (InterruptedException e) {
                        throw new IOException(e);
                    }
                    if (System.currentTimeMillis() - time >= timeout) {
                        throw new SocketTimeoutException();
                    }
                }
                byte[] data = packets.poll();
                dst.setLength(data.length);
                System.arraycopy(data, 0, dst.getData(), 0, dst.getLength());
            }
        }

        @Override
        protected void setTTL(byte ttl) throws IOException {
            System.out.println("CustomDatagramSocketImpl: setTTL() called");
        }

        @Override
        protected byte getTTL() throws IOException {
            System.out.println("CustomDatagramSocketImpl: getTTL() called");
            throw new UnsupportedOperationException();
        }

        @Override
        protected void setTimeToLive(int ttl) throws IOException {
            System.out.println("CustomDatagramSocketImpl: "
                    + "setTimeToLive() called");
        }

        @Override
        protected int getTimeToLive() throws IOException {
            System.out.println("CustomDatagramSocketImpl: "
                    + "getTimeToLive() called");
            throw new UnsupportedOperationException();
        }

        @Override
        protected void join(InetAddress inetaddr) throws IOException {
            System.out.println("CustomDatagramSocketImpl: join() called");
        }

        @Override
        protected void leave(InetAddress inetaddr) throws IOException {
            System.out.println("CustomDatagramSocketImpl: leave() called");
        }

        @Override
        protected void joinGroup(SocketAddress addr, NetworkInterface intf)
                throws IOException {
            System.out.println("CustomDatagramSocketImpl: joinGroup() called");
        }

        @Override
        protected void leaveGroup(SocketAddress addr, NetworkInterface intf)
                throws IOException {
            System.out.println("CustomDatagramSocketImpl: leaveGroup() called");
        }

        @Override
        protected void close() {
            System.out.println("CustomDatagramSocketImpl: called");
            CustomDatagramSocketImplFactory.removeSocket(localPort);
        }

        @Override
        public void setOption(int optID, Object value) throws SocketException {
            System.out.println("CustomDatagramSocketImpl: setOption() called");
            if (optID == SocketOptions.SO_TIMEOUT) {
                if (value instanceof Integer) {
                    timeout = (Integer) value;
                    System.out.println("CustomDatagramSocketImpl: "
                            + "setOption(): set timeout to " + timeout);
                }
            }
        }

        @Override
        public Object getOption(int optID) throws SocketException {
            System.out.println("CustomDatagramSocketImpl: getOption() called");
            throw new UnsupportedOperationException();
        }

    }
}