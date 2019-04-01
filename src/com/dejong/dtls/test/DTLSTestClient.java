package com.dejong.dtls.test;

import com.dejong.dtls.client.MyDTLSClientDatagramSocket;

public class DTLSTestClient {

    public static void main(String args[]) {

        try {

            MyDTLSClientDatagramSocket client = new MyDTLSClientDatagramSocket();
            client.sendMessage("I'm client", "localhost", 7);

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    } //end main

}
