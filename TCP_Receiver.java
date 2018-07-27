package project3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Random;

public class TCP_Receiver {
    private static final String SenderIP = "10.100.39.163"; // ensure CORRECT IP address.
    private static int destport = 5432;
    private static final int timeout = 15000; // time in milliseconds

    static Random random = new Random();
    static Network network = new Network(random, 0.9);
    private static void sendSYN_ACK(DatagramSocket s) throws SocketException { // sends guaranteed message
        System.out.println("SYN Received");
        try {
            Segment sendACK = new Segment(true, true, 0, 1, 1);
            network.send(s, SenderIP, destport, sendACK);
            System.out.println("ACK sent!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendACK(int totalSegments, int seqNo, int ackNo, int length, DatagramSocket s) throws Exception {// sends guaranteed message
        System.out.println("Received " + totalSegments + " Segment(s) from sender.");
        String senderIP = "10.100.39.163";
        try {
            network.send(s, senderIP, destport, new Segment(false, true, seqNo, ackNo, length));
            System.out.println("**DEBUG** Outoing ackNo: " + ackNo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("InfiniteLoopStatement")
    static public void main(String args[]) throws Exception {
        HashMap<Integer, Integer> SegmentMap = new HashMap<>();
        DatagramSocket s = new DatagramSocket(destport);
        int totalSegments = 0;                                                                   // total # of overall segments received.
        int seqNo = 0;
        int ackNo = 0;
        int length = 0;

        while (true) try {
            Segment incomingSegment = network.receive(s);//)listen(s);
            if (incomingSegment.isSyn()) {
                ackNo += incomingSegment.getLength();
                sendSYN_ACK(s);
                seqNo = 1;
                continue;
            }

            // add received ACK code,
            if (incomingSegment.isAck()) {
                System.out.println("ACK Received. \n" + incomingSegment.toString());
                continue;
            }

            if (!incomingSegment.isSyn() && !incomingSegment.isAck()) {                         /* add else code for this statement */
                if (!SegmentMap.containsKey(incomingSegment.getSeqNo())) {                      // if incoming
                    // segment doesn't have a key that matches Map,
                    SegmentMap.put(incomingSegment.getSeqNo(), incomingSegment.getLength());    // add it to map
                } else {
                    if (incomingSegment.getSeqNo() < ackNo) {
                        sendACK(totalSegments, seqNo, ackNo, 0, s);
                    }
                }
                System.out.println("Printing segments in map: " + SegmentMap);
                int tempAckNo = ackNo;
                for (int i = 0; i < SegmentMap.size(); i++) {
                    if (SegmentMap.containsKey(ackNo)) {
                        System.out.println(SegmentMap.get(ackNo));
                        ackNo += SegmentMap.get(ackNo);                    //Returns the value
                        // to which the specified key is mapped

                    }
                }
                if (ackNo != tempAckNo) {
                    sendACK(totalSegments, seqNo, ackNo, 0, s);

                    for (int i = 0; i < ackNo; i++) {

                        if (SegmentMap.containsKey(i)) {
                            SegmentMap.remove(i);
                        }

                    }System.out.println("Printing segments in map after removal: " + SegmentMap.keySet());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.getStackTrace();
        }
    }

}
