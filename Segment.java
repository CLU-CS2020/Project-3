/*
 * Copyright OrangeDog LLC.
 * All rights reserved.
 */
package project3;

import java.io.Serializable;

/**
 *
 * @author graham
 */
public class Segment implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final boolean syn;
	private final boolean ack;
	private final int seqNo;
	private final int ackNo;
	private final int length;

	public Segment(boolean syn, boolean ack, int seqNo, int ackNo, int length)
	{
		this.syn = syn;
		this.ack = ack;
		this.seqNo = seqNo;
		this.ackNo = ackNo;
		this.length = length;
	}

	public int getSeqNo()
	{
		return seqNo;
	}

	public int getAckNo()
	{
		return ackNo;
	}

	public int getLength()
	{
		return length;
	}

	public boolean isSyn()
	{
		return syn;
	}

	public boolean isAck()
	{
		return ack;
	}

	@Override
	public String toString()
	{
		return "Segment{" + "syn=" + syn + ", ack=" + ack + ", seqNo=" + seqNo + ", ackNo=" + ackNo + ", length=" + length + '}';
	}
}
