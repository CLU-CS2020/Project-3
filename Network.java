/*
 * Copyright OrangeDog LLC.
 * All rights reserved.
 */
package zzz;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

	public void send(DatagramSocket socket, String hostName, int destPort, Segment segment) throws Exception
	{
		if (random.nextDouble() > lossRate)
		{
			ByteArrayOutputStream b_stream = new ByteArrayOutputStream();
			ObjectOutputStream o_stream = new ObjectOutputStream(b_stream);
			o_stream.writeObject(segment);
			byte[] data = b_stream.toByteArray();
			DatagramPacket msg = new DatagramPacket(data, data.length, InetAddress.getByName(hostName), destPort);
			socket.send(msg);
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
