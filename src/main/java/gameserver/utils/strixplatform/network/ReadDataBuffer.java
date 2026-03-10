package gameserver.utils.strixplatform.network;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ReadDataBuffer
{
	private final byte[] dataArray;
	private final int dataLenght;
	private int dataBufferPosition;
	
	public ReadDataBuffer(byte[] dataArray)
	{
		this.dataArray = dataArray;
		dataLenght = dataArray.length;
		dataBufferPosition = 0;
	}
	
	public char ReadC()
	{
		if ((dataBufferPosition + 1) > dataLenght)
		{
			return 0;
		}
		final ByteBuffer bb = ByteBuffer.wrap(dataArray, dataBufferPosition, 1);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		final char value = bb.getChar();
		dataBufferPosition++;
		return value;
	}
	
	public short ReadH()
	{
		if ((dataBufferPosition + 2) > dataLenght)
		{
			return 0;
		}
		final ByteBuffer bb = ByteBuffer.wrap(dataArray, dataBufferPosition, 2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		final short value = bb.getShort();
		dataBufferPosition += 2;
		return value;
	}
	
	public int ReadD()
	{
		if ((dataBufferPosition + 4) > dataLenght)
		{
			return 0;
		}
		final ByteBuffer bb = ByteBuffer.wrap(dataArray, dataBufferPosition, 4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		final int value = bb.getInt();
		dataBufferPosition += 4;
		return value;
	}
	
	public long ReadQ()
	{
		if ((dataBufferPosition + 8) > dataLenght)
		{
			return 0;
		}
		final ByteBuffer bb = ByteBuffer.wrap(dataArray, dataBufferPosition, 8);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		final long value = bb.getLong();
		dataBufferPosition += 8;
		return value;
	}
	
	public String ReadS()
	{
		final StringBuilder str = new StringBuilder();
		while ((char) (dataArray[dataBufferPosition]) != '\0')
		{
			str.append((char) dataArray[dataBufferPosition]);
			dataBufferPosition += 2;
		}
		dataBufferPosition += 2;
		return str.toString();
	}
}