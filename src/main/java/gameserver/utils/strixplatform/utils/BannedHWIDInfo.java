package gameserver.utils.strixplatform.utils;

public class BannedHWIDInfo
{
	private final String HWID;
	private final long timeExpire;
	private final String reason;
	private final String gmName;

	public BannedHWIDInfo(final String HWID, final long timeExpire, final String reason, final String gmName)
	{
		this.HWID = HWID;
		this.timeExpire = timeExpire;
		this.reason = reason;
		this.gmName = gmName;
	}

	public String getHWID()
	{
		return HWID;
	}

	public long getTimeExpire()
	{
		return timeExpire;
	}

	public String getReason()
	{
		return reason;
	}

	public String getGmName()
	{
		return gmName;
	}

	@Override
	public String toString()
	{
		return "Banned info: HWID=[" + HWID + "] TimeExpire=[" + timeExpire + "] Reason=[" + reason + "] GMName=[" + gmName + "]";
	}
}