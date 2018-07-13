/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author jake
 */
//Send a SYN to the receiver containing the server's initial sequence number.
//
//Wait for a SYN-ACK from the receiver containing the receiver's initial sequence number (do not send an ACK back acknowledging the SYN-ACK).
//
//Simultaneously send the receiver 10 segments, each segment of some random length.
//
//Implement the TCP acknowledgements with re-transmission protocol to make sure all 10 segments are received.
public class TCPSender {

    static public int destport = 5432;
    static public int bufsize = 512;
    static public final int timeout = 15000;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {

        Random sendRandom = new Random(0);
        Network sendNetwork = new Network(sendRandom, 0);

        DatagramSocket s;
        try {
            s = new DatagramSocket(destport);
        } catch (SocketException se) {
            System.err.println("cannot create socket with port " + destport);
            return;
        }

        String receiverIP = "10.100.39.161";

        ArrayList<Segment> segmentsToSend = new ArrayList<>();
        Segment[] segments = {
            new Segment(false, false, 1, 0, 10),
            new Segment(false, false, 11, 0, 6),
            new Segment(false, false, 17, 0, 4),
            new Segment(false, false, 21, 0, 13),
            new Segment(false, false, 34, 0, 12),
            new Segment(false, false, 49, 0, 15),
            new Segment(false, false, 64, 0, 1),
            new Segment(false, false, 65, 0, 3),
            new Segment(false, false, 69, 0, 4),
            new Segment(false, false, 73, 0, 2)};

        Segment synSegment = new Segment(true, false, 0, 0, 1);

        sendNetwork.sendGuaranteed(s, receiverIP, destport, synSegment);

        DatagramPacket incomingMSG = new DatagramPacket(new byte[bufsize], bufsize);
        incomingMSG.setLength(bufsize);  // max received packet size
        s.receive(incomingMSG);          // the actual receive operation
        System.err.println("message from <" + incomingMSG.getAddress().getHostAddress() + "," + incomingMSG.getPort() + ">");
        byte[] data = incomingMSG.getData();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        System.out.println("Sending SYN to " + receiverIP);
        Segment synAckSegment = (Segment) is.readObject();

        
        
        if (synAckSegment.isAck()) {

            try {
                s.setSoTimeout(timeout);       // set timeout in milliseconds
            } catch (SocketException se) {
                System.err.println("socket exception: timeout not set!");
            }

            sendNetwork.send(s, receiverIP, destport, segments);

            System.out.println("SYN-ACK Received from " + incomingMSG.getAddress());
            
//            while (true) {
//
//                incomingMSG = new DatagramPacket(new byte[bufsize], bufsize);
//                incomingMSG.setLength(bufsize);  // max received packet size
//                s.receive(incomingMSG);          // the actual receive operation
//                System.err.println("message from <" + incomingMSG.getAddress().getHostAddress() + "," + incomingMSG.getPort() + ">");
//                data = incomingMSG.getData();
//                in = new ByteArrayInputStream(data);
//                is = new ObjectInputStream(in);
//                Segment ackSegment = (Segment) is.readObject();
//
//            }

            // TODO code application logic here
        } else {
            System.out.println("No SYN-ACK Received from SYN");
        }
    }
}
