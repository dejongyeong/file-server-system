package com.dejong.dtls.test;

import com.dejong.server.MyServerDatagramSocket;

import java.net.InetAddress;

public class DTLSTestServer {

    public static void main(String args[]) {

        try {

            MyServerDatagramSocket server = new MyServerDatagramSocket();

            while(true) {
                server.receiveMessage(InetAddress.getByName("localhost"), 8);
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

}
