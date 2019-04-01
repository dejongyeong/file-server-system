package com.dejong.dtls.test;

import com.dejong.dtls.server.MyDTLSServerDatagramSocket;

public class DTLSTestServer {

    public static void main(String args[]) {

        try {

            MyDTLSServerDatagramSocket server = new MyDTLSServerDatagramSocket();

            while(true) {
                server.receiveMessage("localhost", 8);
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

}
