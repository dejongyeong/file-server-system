package com.dejong.dtls.test;

import com.dejong.client.MyClientDatagramSocket;
import com.dejong.dtls.DTLSEngine;

public class DTLSTestClient {

    public static void main(String args[]) {

        try {

            MyClientDatagramSocket client = new MyClientDatagramSocket();
            client.setSSLEngine(DTLSEngine.createSSLEngine(true));

            //testing purpose
            //send message
            client.sendMessage("localhost", 7, "I'm Client");

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    } //end main

}
