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
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
    static public final int timeout = 10;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {

        Random sendRandom = new Random(0);
        Network sendNetwork = new Network(sendRandom, .8);

        DatagramSocket s;
        try {
            s = new DatagramSocket(destport);
        } catch (SocketException se) {
            System.err.println("cannot create socket with port " + destport);
            return;
        }

        String receiverIP = "10.100.47.251";
        Segment[] segments = {
            new Segment(false, false, 1, 0, 10),
            new Segment(false, false, 11, 0, 6),
            new Segment(false, false, 17, 0, 4),
            new Segment(false, false, 21, 0, 13),
            new Segment(false, false, 34, 0, 12),
            new Segment(false, false, 46, 0, 15),
            new Segment(false, false, 61, 0, 1),
            new Segment(false, false, 62, 0, 3),
            new Segment(false, false, 65, 0, 4),
            new Segment(false, false, 69, 0, 2)};

        Segment synSegment = new Segment(true, false, 0, 0, 1);
        System.out.println("Sending SYN to " + receiverIP);
        sendNetwork.send(s, receiverIP, destport, synSegment);

        DatagramPacket incomingMSG = new DatagramPacket(new byte[bufsize], bufsize);
        incomingMSG.setLength(bufsize);  // max received packet size
        s.receive(incomingMSG);          // the actual receive operation
        System.err.println("message from <" + incomingMSG.getAddress().getHostAddress() + "," + incomingMSG.getPort() + ">");
        byte[] data = incomingMSG.getData();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        Segment synAckSegment = (Segment) is.readObject();

        if (synAckSegment.isAck()) {

            try {
                s.setSoTimeout(timeout);       // set timeout in milliseconds
            } catch (SocketException se) {
                System.err.println("socket exception: timeout not set!");
            }

            System.out.println("SYN-ACK Received from " + incomingMSG.getAddress());
            System.out.println("Sending all data segments");

            Long currentTime = new Long(0);

            HashMap<Segment, Long> segmentTimes = new HashMap<Segment, Long>() {
                {
                    put(segments[0], new Long(15));
                    put(segments[1], new Long(15));
                    put(segments[2], new Long(15));
                    put(segments[3], new Long(15));
                    put(segments[4], new Long(15));
                    put(segments[5], new Long(15));
                    put(segments[6], new Long(15));
                    put(segments[7], new Long(15));
                    put(segments[8], new Long(15));
                    put(segments[9], new Long(15));
                }
            };
            sendNetwork.send(s, receiverIP, destport, segments);

            while (true) {
                try {
                    Segment ackSegment = sendNetwork.receive(s);

                    System.out.println("***************************************");
                    System.out.println("ACK received: " + ackSegment.toString());
                    int lastAck = ackSegment.getAckNo();
                    System.out.println(incomingMSG.getAddress() + " has acknowledged up to byte " + lastAck + ".");
                    System.out.println("***************************************");

                    Iterator<Segment> it = segmentTimes.keySet().iterator();
                    while(it.hasNext())
                    {
                        Segment segment = it.next();
                        if (segment.getSeqNo() < lastAck) {
                            System.out.println("Segment with sequence number " + segment.getSeqNo() + " has been acknowldged.");
                            it.remove();
                        }
                    }

                    boolean allSegmentsAck = segmentTimes.isEmpty();

                    if (allSegmentsAck) {
                        System.out.println("All segments have been acknowledged. Ending connection.");
                        break;
                    }

                } catch (SocketTimeoutException ste) {    // receive() timed out
                    System.out.println("Response timed out. Sending un-acknowledged segments again!" + segmentTimes.size());
                    System.out.println("----------------------------------");
                    currentTime += 10;
                    System.out.println("Current running time is " + currentTime + " ms.");
                    for (Map.Entry<Segment, Long> entry : segmentTimes.entrySet()) {
                        Segment segment = entry.getKey();
                        Long segmentTime = entry.getValue();
                        if (segmentTime > 60000) {
                            System.out.println("Timeout of 60000 MS exceeded. Closing connection.");
                            System.exit(1);
                        } else if (segmentTime < currentTime) {
                            System.out.println("Resending the segment with sequence number " + segment.getSeqNo() + ".");
                            segmentTimes.put(segment, (segmentTime * 2));
                            sendNetwork.send(s, receiverIP, destport, segment);
                        }
                    }
                    System.out.println("----------------------------------");

                } catch (Exception ioe) {                // should never happen!
                    System.err.println("General exception. Printing stack trace!");
                    ioe.printStackTrace();
                }
            }

            // TODO code application logic here
        } else {
            System.out.println("No SYN-ACK Received from SYN");
        }
    }
}
