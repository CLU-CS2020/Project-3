/*
 * Copyright OrangeDog LLC.
 * All rights reserved.
 */
package project3;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author graham
 */
public class Network
{
	private final Random random;
	private final double lossRate;

	public Network(Random random, double failureRate)
	{
		this.random = random;
		this.lossRate = failureRate;
	}

	private void actualSend(DatagramSocket socket, String hostName, int destPort, Segment segment) throws IOException
	{
		System.out.println("Sending : " + segment);
		ByteArrayOutputStream b_stream = new ByteArrayOutputStream();
		ObjectOutputStream o_stream = new ObjectOutputStream(b_stream);
		o_stream.writeObject(segment);
		byte[] data = b_stream.toByteArray();
		DatagramPacket msg = new DatagramPacket(data, data.length, InetAddress.getByName(hostName), destPort);
		socket.send(msg);
	}

	public void send(DatagramSocket socket, String hostName, int destPort, Segment segment) throws Exception
	{
		//	allow syn's through
		if (segment.isSyn() || random.nextDouble() > lossRate)
		{
			this.actualSend(socket, hostName, destPort, segment);
		}
		else
		{
			System.out.println("Dropping : " + segment);
		}
	}

	public Segment receive(DatagramSocket socket) throws Exception
	{
		byte[] recvBuf = new byte[ 5_000 ];
		DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
		try
		{
			socket.receive(packet);
		}
		catch (SocketTimeoutException ex)
		{
			return null;
		}
		ByteArrayInputStream byteStream = new ByteArrayInputStream(recvBuf);
		try (ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream)))
		{
			return (Segment) is.readObject();
		}
	}

	public void send(DatagramSocket socket, String hostName, int destPort, Segment... segments) throws Exception
	{
		List<Segment> shuffled = new ArrayList<>(segments.length);
		shuffled.addAll(Arrays.asList(segments));
		Collections.shuffle(shuffled);
		for (Segment segment : shuffled)
		{
			send(socket, hostName, destPort, segment);
		}
	}
}
