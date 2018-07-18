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
    private static final String SenderIP = "10.100.28.60";
    private static int destport = 5432;
    private static final int timeout = 15000; // time in milliseconds
    
    private static Segment listen() throws IOException, ClassNotFoundException {
        DatagramSocket s = new DatagramSocket(destport);
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
        return incomingSegment;

    }
    private static void sendSYN_ACK() throws SocketException {
        Random random = new Random(0);
        Network network = new Network(random, 0);
        DatagramSocket s = new DatagramSocket(destport);
        System.out.println("SYN Received");
        try {
            Segment sendACK = new Segment(true, true, 0, 1, 1);
            network.sendGuaranteed(s, SenderIP, destport, sendACK);
            System.out.print("ACK sent!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void sendACK(int totalSegments, int seqNo, int ackNo, int length) throws Exception {
        Random random = new Random(0);
        Network network = new Network(random, 0);
        DatagramSocket s = new DatagramSocket(destport);
        System.out.println("Received " + totalSegments + " Segment(s) from sender.");
        String senderIP = "10.100.28.60";
        try {
            network.sendGuaranteed(s, senderIP, destport, new Segment(false, true, seqNo, ackNo, length));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    static public void main(String args[]) throws Exception {
        HashMap<Integer, Integer> SegmentMap = new HashMap<>();
        DatagramSocket s = new DatagramSocket(destport);
        int totalSegments = 0; // total # of overall segments received.
        int seqNo = 0;
        int ackNo = 0;
        int length = 0;
        
            while (true) try {
                Segment incomingSegment = listen();
                if (incomingSegment.isSyn()) {
                    ackNo += incomingSegment.getSeqNo();
                    seqNo = incomingSegment.getAckNo();
                    sendSYN_ACK();
                }else {
                    System.out.print("NO SYN Message received.");
                }
                    try {
                        s.setSoTimeout(timeout);       // set timeout in milliseconds
                    } catch (SocketException se) {
                        System.err.println("socket exception: timeout not set!");
                    }
                if (!incomingSegment.isSyn() && !incomingSegment.isAck()) {
                    if (!SegmentMap.containsKey(incomingSegment.getSeqNo())) {
                        // if incoming segment doesn't have a key that matches Map,
                        SegmentMap.put(incomingSegment.getSeqNo(),
                                incomingSegment.getLength());
                        // add it to map
                        totalSegments++;
                    }
                    for (int i = 0; i <= totalSegments; i++) {
                        if (SegmentMap.containsKey(ackNo)) {
                            ackNo += SegmentMap.get(incomingSegment.getSeqNo());
                            i++;
                        }
                        else if ((i == totalSegments) && (SegmentMap.size() == 9)) {
                            sendACK(totalSegments, seqNo, ackNo, length);
                        }else if (SegmentMap.size() != 9 && !SegmentMap.containsKey(ackNo) ) {
                            sendACK(totalSegments, seqNo, ackNo, length);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                e.getStackTrace();
            }
        }
    }
