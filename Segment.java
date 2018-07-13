/*
 * Copyright OrangeDog LLC.
 * All rights reserved.
 */
package zzz;

import java.io.Serializable;

/**
 *
 * @author graham
 */
public class Segment implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final int seqNo;
	private final int ackNo;
	private final int length;

	public Segment(int seqNo, int ackNo, int length)
	{
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
}
