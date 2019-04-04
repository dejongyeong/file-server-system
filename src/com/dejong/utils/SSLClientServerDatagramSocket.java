package com.dejong.utils;

import com.dejong.server.DatagramMessage;

import java.net.InetAddress;

public abstract class SSLClientServerDatagramSocket {

    //receive message
    public abstract DatagramMessage receiveMessage(InetAddress hostname, int port);
}
