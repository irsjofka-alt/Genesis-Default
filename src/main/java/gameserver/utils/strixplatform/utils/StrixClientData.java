package gameserver.utils.strixplatform.utils;

public class StrixClientData
{
	private String HWID;
	private String VMPKey;
	private String clientAccount;
	private long sessionId;
	private long HWIDChecksum;
	private long filesChecksum;
	private DetectionResponse detectionResponse;
	private LaunchStateResponse launchStateResponse;
	private ServerResponse serverResponse;
	private int clientSideVersion;
	private int activeWindowCount;
	
	public StrixClientData()
	{
	}
	
	public void setVMPKey(final String vmpKey)
	{
		VMPKey = vmpKey;
	}
	
	public String getVMPKey()
	{
		return VMPKey;
	}
	
	public void setDetectionResponse(final long detectionInfo)
	{
		detectionResponse = DetectionResponse.valueOf((int) detectionInfo);
	}
	
	public DetectionResponse getDetectionResponse()
	{
		return detectionResponse;
	}
	
	public void setLaunchStateResponse(final long launchState)
	{
		launchStateResponse = LaunchStateResponse.valueOf((int) launchState);
	}
	
	public LaunchStateResponse getLaunchStateResponse()
	{
		return launchStateResponse;
	}
	
	public void setSessionId(final long sessionId)
	{
		this.sessionId = sessionId;
	}
	
	public long getSessionId()
	{
		return sessionId;
	}
	
	public void setFilesChecksum(final long filesChecksum)
	{
		this.filesChecksum = filesChecksum;
	}
	
	public long getFilesChecksum()
	{
		return filesChecksum;
	}
	
	public void setHWIDChecksum(final long HWIDChecksum)
	{
		this.HWIDChecksum = HWIDChecksum;
	}
	
	public long getHWIDChecksum()
	{
		return HWIDChecksum;
	}
	
	public void setClientHWID(final String HWID)
	{
		this.HWID = HWID;
	}
	
	public String getClientHWID()
	{
		return HWID;
	}
	
	public void setServerResponse(final ServerResponse response)
	{
		serverResponse = response;
	}
	
	public ServerResponse getServerResponse()
	{
		return serverResponse;
	}
	
	public void setClientAccount(final String clientAccount)
	{
		this.clientAccount = clientAccount;
	}
	
	public String getClientAccount()
	{
		return clientAccount;
	}
	
	public void setClientSideVersion(final int clientSideVersion)
	{
		this.clientSideVersion = clientSideVersion;
	}
	
	public int getClientSideVersion()
	{
		return clientSideVersion;
	}
	
	public void setActiveWindowCount(final int activeWindowCount)
	{
		this.activeWindowCount = activeWindowCount;
	}
	
	public int getActiveWindowCount()
	{
		return activeWindowCount;
	}
	
	@Override
	public String toString()
	{
		final String toString = "ClientAccount: [" + (clientAccount != null ? clientAccount : "UNKNOW") + "] HWID: [" + HWID + "] VMPKey: [" + VMPKey + "] DetectionResponse: [" + (detectionResponse != null ? detectionResponse.getDescription() : "NULL") + "] SessionID: [" + sessionId + "] FilesChecksum: [" + filesChecksum + "] ServerResponse: [" + (serverResponse != null ? serverResponse.toString() : "NULL") + "] LaunchStateResponse: [" + (launchStateResponse != null ? launchStateResponse.getDescription() : "NULL") + "]";
		return toString;
	}
}