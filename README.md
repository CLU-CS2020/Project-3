# Project-3 Group Project 1
In this assignment you will implement a simplified TCP stack.
The simplifications will be:
• Instead of having TCP over IP, we will have TCP over UDP.

• Instead of having both ends of a TCP connection being able to send, we will have one end send
bytes, and the other end receive bytes.

• Instead of having multiple TCP connection, we will simply have one connection.

• Our TCP segments will have lengths, but won't contain any data.
The sender should do the following:

• Send a SYN to the receiver containing the server's initial sequence number.

• Wait for a SYN-ACK from the receiver containing the receiver's initial sequence number (do
not send an ACK back acknowledging the SYN-ACK).

• Simultaneously send the receiver 10 segments, each segment of some random length.

• Implement the TCP acknowledgements with re-transmission protocol to make sure all 10
segments are received.

Conversely the receiver should do the following:

• Wait for a SYN from the server containing the sender's initial sequence number.

• Send a SYN-ACK to the sender containing the receiver's initial sequence number (do not wait
for an ACK back acknowledging the SYN-ACK).

• Correctly acknowledge bytes received from the sender.

To simulate a network with failures you should use the Network class on blackboard. So both the
server and receiver should initially start by making a network object over which they send messages
(the LOSS_RATE parameter specifies the probability, between 0 and 1, of the network dropping a
segment):

Random r = new Random(0);
final Network network = new Network(r, LOSS_RATE);
The two critical methods in the Network class are:
public void send(DatagramSocket socket, String hostName,
int destPort, Segment segment) throws Exception
public void send(DatagramSocket socket, String hostName, int destPort,
Segment... segments) throws Exception

The first method sends a single TCP segment over the network (as a UDP packet). The latter sends an
array of segments simultaneously.

Put print statements in both the receiver and sender to show what is going on – what messages are
being sent, when there are timeouts, etc. I should be able to read your output and understand exactly
how things are working.

Comment your code so that both you and I can read the comments later and know what is going on.
