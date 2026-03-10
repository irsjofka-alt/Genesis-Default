package gameserver.utils.strixplatform.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLog
{
	protected String timePattern = "HH:mm:ss";

	private final SimpleDateFormat timeFormat = new SimpleDateFormat(timePattern);
	private final Date now = new Date();
	protected String fileName;
	protected Writer fw;

	public FileLog(final String fileName) throws IOException
	{
		setFile(fileName);
	}

	public synchronized void setFile(final String fileName) throws IOException
	{
		this.fileName = fileName;

		fw = null;
		try
		{
			fw = new BufferedWriter(new FileWriter(fileName, true));
		}
		catch(final FileNotFoundException ex)
		{
			final String parentName = new File(fileName).getParent();
			if(parentName != null)
			{
				final File parentDir = new File(parentName);
				if((!parentDir.exists()) && (parentDir.mkdirs()))
				{
					fw = new BufferedWriter(new FileWriter(fileName, true));
				}
				else
				{
					throw ex;
				}
			}
			else
			{
				throw ex;
			}
		}
	}

	protected void closeFile()
	{
		if(fw != null)
		{
			try
			{
				fw.close();
			}
			catch(final IOException e) {}
		}
	}

	public synchronized void log(final String msg)
	{
		if(msg == null)
		{
			return;
		}
		now.setTime(System.currentTimeMillis());

		if(fw == null)
		{
			return;
		}
		try
		{
			fw.append('[');
			fw.append(timeFormat.format(now));
			fw.append(']');
			fw.append(' ');
			fw.append("[strixplatform]");
			fw.append(' ');
			fw.append(msg);
			fw.append('\n');
			fw.flush();
		}
		catch(final IOException e)
		{
			e.printStackTrace();
		}
	}
}