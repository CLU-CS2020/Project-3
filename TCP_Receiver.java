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


    private static Segment listen(DatagramSocket s) throws IOException, ClassNotFoundException {
        Segment incomingSegment;

        // incoming SYN segment
        int bufsize = 512;
        DatagramPacket incomingMSG = new DatagramPacket(new byte[bufsize], bufsize);
        incomingMSG.setLength(bufsize);  // max received packet size
        s.receive(incomingMSG);          // the actual receive operation
        System.err.println("message from <" + incomingMSG.getAddress().getHostAddress()
                + "," + incomingMSG.getPort() + ">");
        byte[] data = incomingMSG.getData();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        incomingSegment = (Segment) is.readObject();
        System.out.println("**DEBUG** Incoming Segment: " + incomingSegment.toString());
        return incomingSegment;

    }

    private static void sendSYN_ACK(DatagramSocket s) throws SocketException { // sends guaranteed message
        Random random = new Random(0);
        Network network = new Network(random, 0);
        System.out.println("SYN Received");
        try {
            Segment sendACK = new Segment(true, true, 0, 1, 1);
            network.sendGuaranteed(s, SenderIP, destport, sendACK);
            System.out.println("ACK sent!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendACK(int totalSegments, int seqNo, int ackNo, int length, DatagramSocket s) throws Exception {// sends guaranteed message
        Random random = new Random(0);
        Network network = new Network(random, 0);
        System.out.println("Received " + totalSegments + " Segment(s) from sender.");
        String senderIP = "10.100.39.163";
        try {
            network.sendGuaranteed(s, senderIP, destport, new Segment(false, true, seqNo, ackNo, length));
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
            Segment incomingSegment = listen(s);
            if (incomingSegment.isSyn()) {
                ackNo += incomingSegment.getLength();
                seqNo = incomingSegment.getAckNo();
                sendSYN_ACK(s);
            } else {
                System.out.println("NO SYN Message received.");
            }
            try {
                s.setSoTimeout(timeout);       // set timeout in milliseconds
            } catch (SocketException se) {System.err.println("socket exception: timeout not set!");}

            // add received ACK code,
            if (incomingSegment.isAck()) {
                System.out.println("ACK Received. \n" + incomingSegment.toString());
            }

            if (!incomingSegment.isSyn() && !incomingSegment.isAck()) {                         /* add else code for this statement */
                if (!SegmentMap.containsKey(incomingSegment.getSeqNo())) {                      // if incoming segment doesn't have a key that matches Map,
                    SegmentMap.put(incomingSegment.getSeqNo(), incomingSegment.getLength());    // add it to map
                    totalSegments++;                                                            // increment # of segments received.
                }
                for (int i = 0; i <= totalSegments; i++) {
                    if (SegmentMap.containsKey(ackNo)) {
                        ackNo += SegmentMap.get(incomingSegment.getSeqNo());                    //Returns the value to which the specified key is mapped
                        i++;
                    }if ((i == totalSegments) && (SegmentMap.size() == 9)) {
                        sendACK(totalSegments, seqNo, ackNo, length, s);                           /* if we have everything, send ack*/
                    }if (SegmentMap.size() != 9 && !SegmentMap.containsKey(ackNo)) {
                        sendACK(totalSegments, seqNo, ackNo, length, s);                           /* if we don't have all the messages, and no key = ackNo,send ack for what we have */
                    }
                }
            }else{
                /* add else code here */
                System.out.println("Unknown parameters sent. user error?");
            }

        } catch (Exception e) {
            e.printStackTrace();
            e.getStackTrace();
        }
    }
}
