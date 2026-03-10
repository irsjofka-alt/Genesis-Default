package gameserver.utils.strixplatform.managers;

import java.util.HashSet;
import java.util.Set;

import gameserver.utils.strixplatform.StrixPlatform;
import gameserver.utils.strixplatform.configs.MainConfig;
import gameserver.utils.strixplatform.logging.Log;
import gameserver.utils.strixplatform.utils.DataUtils;
import gameserver.utils.strixplatform.utils.DetectionResponse;
import gameserver.utils.strixplatform.utils.FailedCheckResolve;
import gameserver.utils.strixplatform.utils.ServerResponse;
import gameserver.utils.strixplatform.utils.StrixClientData;

public class ClientGameSessionManager
{
	private final Set<Long> SESSION_ID_LIST = new HashSet<>();
	
	public static ClientGameSessionManager getInstance()
	{
		return LazyHolder.INSTANCE;
	}
	
	public boolean addSessionId(final long sessionId)
	{
		if (MainConfig.STRIX_PLATFORM_GAME_SESSION_CHECK_ENABLED)
		{
			final Long longValue = Long.valueOf(sessionId);
			if (SESSION_ID_LIST.contains(longValue))
			{
				return false;
			}
			SESSION_ID_LIST.add(longValue);
		}
		return true;
	}
	
	public boolean checkKeyInfo(final StrixClientData clientData)
	{
		if (clientData == null || clientData.getVMPKey().length() < 64)
		{
			return false;
		}
		
		if (!clientData.getVMPKey().equalsIgnoreCase(MainConfig.STRIX_PLATFORM_KEY))
		{
			return false;
		}
		return true;
	}
	
	public boolean checkHWIDChecksum(final StrixClientData clientData)
	{
		if (clientData == null)
		{
			return false;
		}
		
		final String clientHWID = clientData.getClientHWID();
		if (clientHWID.length() != 32 || DataUtils.getDataChecksum(clientHWID.getBytes(), true) != clientData.getHWIDChecksum())
		{
			return false;
		}
		return true;
	}
	
	public boolean checkFilesChecksum(final StrixClientData clientData)
	{
		for (int i = 0; i < MainConfig.STRIX_PLATFORM_FILES_CHECKSUM.length; i++)
		{
			if (MainConfig.STRIX_PLATFORM_FILES_CHECKSUM[i] != 0)
			{
				if (clientData == null || clientData.getFilesChecksum() != MainConfig.STRIX_PLATFORM_FILES_CHECKSUM[i])
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean checkDetectionInfo(final StrixClientData clientData)
	{
		if (clientData == null || clientData.getDetectionResponse() != DetectionResponse.RESPONSE_OK)
		{
			return false;
		}
		return true;
	}
	
	public boolean checkLaunchState(final StrixClientData clientData)
	{
		if (clientData == null)
		{
			return false;
		}
		
		switch (clientData.getLaunchStateResponse())
		{
			case RESPONSE_LAUNCHED_ON_VIRTUAL_MACHINE :
				if (MainConfig.STRIX_PLATFORM_VIRTUAL_MACHINE_CHECK_ENABLED)
				{
					return false;
				}
				break;
			case RESPONSE_LAUNCHED_ON_VIRTUAL_MACHIME_AND_FROM_LAUNCHER :
				if (MainConfig.STRIX_PLATFORM_VIRTUAL_MACHINE_CHECK_ENABLED)
				{
					return false;
				}
				break;
			case RESPONSE_LAUNCHED_FROM_LAUNCHER :
				break;
			case RESPONSE_LAUNCHED_NORMAL :
				if (MainConfig.STRIX_PLATFORM_ONLY_LAUNCHER_CHECK_ENABLED)
				{
					return false;
				}
				break;
		}
		return true;
	}
	
	public boolean checkClientSideVersion(final StrixClientData clientData)
	{
		if (clientData == null)
		{
			return false;
		}
		
		if (MainConfig.STRIX_PLATFORM_CHECK_CLIENT_SIDE_VERSION)
		{
			if (MainConfig.STRIX_PLATFORM_MANUAL_CLIENT_SIDE_VERSION < 0)
			{
				if (MainConfig.CLIENT_SIDE_VERSION_STORED != clientData.getClientSideVersion())
				{
					StrixPlatform.getInstance().checkClientSideVersion();
					if (MainConfig.CLIENT_SIDE_VERSION_STORED != clientData.getClientSideVersion())
					{
						return false;
					}
				}
			}
			else
			{
				if (MainConfig.STRIX_PLATFORM_MANUAL_CLIENT_SIDE_VERSION != clientData.getClientSideVersion())
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean checkActiveWindowCount(final StrixClientData clientData)
	{
		if (clientData == null)
		{
			return false;
		}
		
		if (MainConfig.STRIX_PLATFORM_ACTIVE_WINDOW_COUNT > 0)
		{
			if (clientData.getActiveWindowCount() > MainConfig.STRIX_PLATFORM_ACTIVE_WINDOW_COUNT)
			{
				return false;
			}
		}
		return true;
	}
	
	public void checkClientData(final StrixClientData clientData)
	{
		if (!checkKeyInfo(clientData))
		{
			Log.audit("Client [HWID:" + clientData.getClientHWID() + "] incorrect license key");
			clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_LICENSE_KEY_INFO_CHECK);
			return;
		}
		if (!checkHWIDChecksum(clientData))
		{
			Log.audit("Client [HWID:" + clientData.getClientHWID() + "] incorrect HWID checksum]");
			clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_CLIENT_HWID_CHECKSUM_CHECK);
			return;
		}
		if (!addSessionId(clientData.getSessionId()))
		{
			Log.audit("Client [HWID:" + clientData.getClientHWID() + "] game session [SessionID:" + clientData.getSessionId() + "] dublicated");
			clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_GAME_SESSION_CHECK);
			return;
		}
		if (!checkFilesChecksum(clientData))
		{
			Log.audit("Client [HWID:" + clientData.getClientHWID() + "] incorrect files checksum [ClientFilesChecksum:" + clientData.getFilesChecksum() + " != ServerFilesChecksum:" + MainConfig.STRIX_PLATFORM_FILES_CHECKSUM + "]");
			clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_CHECKSUM_CHECK);
			return;
		}
		if (!checkDetectionInfo(clientData))
		{
			Log.audit("Client [HWID:" + clientData.getClientHWID() + "] detected soft [DetectionResponse:" + clientData.getDetectionResponse().toString() + "]");
			clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_DETECTION_INFO_CHECK);
			return;
		}
		if (!checkLaunchState(clientData))
		{
			Log.audit("Client [HWID:" + clientData.getClientHWID() + "] bad launched state [LaunchStateResponse:" + clientData.getLaunchStateResponse().toString() + "]");
			clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_LAUNCHED_STATE_CHECK);
			return;
		}
		if (!checkClientSideVersion(clientData))
		{
			Log.audit("Client [HWID:" + clientData.getClientHWID() + "] used old or incorrect version [ClientSideVersion:" + clientData.getClientSideVersion() + "|ServerSideVersion:" + (MainConfig.CLIENT_SIDE_VERSION_STORED > 0 ? MainConfig.CLIENT_SIDE_VERSION_STORED : MainConfig.STRIX_PLATFORM_MANUAL_CLIENT_SIDE_VERSION) + "]");
			clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_CLIENT_SIDE_VERSION_CHECK);
			return;
		}
		if (!checkActiveWindowCount(clientData))
		{
			Log.audit("Client [HWID:" + clientData.getClientHWID() + "] loaded many window [ActiveWindowCount:" + clientData.getActiveWindowCount() + "]");
			clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_ACTIVE_WINDOW_COUNT);
			return;
		}
		clientData.setServerResponse(ServerResponse.RESPONSE_OK);
	}
	
	public boolean checkServerResponse(final StrixClientData clientData)
	{
		if (clientData == null || clientData.getServerResponse() == null)
		{
			return false;
		}
		
		switch (clientData.getServerResponse())
		{
			case RESPONSE_FAILED_CLIENT_DATA_CHECKSUM_CHECK :
			case RESPONSE_FAILED_CLIENT_HWID_CHECKSUM_CHECK :
				return false;
			case RESPONSE_FAILED_CHECKSUM_CHECK :
				if (MainConfig.FAILED_CHECK_FILES_CHECKSUM != FailedCheckResolve.NONE)
				{
					ClientBanManager.getInstance().acceptResolve(MainConfig.FAILED_CHECK_FILES_CHECKSUM, clientData);
					return false;
				}
				break;
			case RESPONSE_FAILED_DETECTION_INFO_CHECK :
				if (MainConfig.FAILED_CHECK_DETECTION_INFO != FailedCheckResolve.NONE)
				{
					ClientBanManager.getInstance().acceptResolve(MainConfig.FAILED_CHECK_DETECTION_INFO, clientData);
					return false;
				}
				break;
			case RESPONSE_FAILED_LICENSE_KEY_INFO_CHECK :
				if (MainConfig.FAILED_CHECK_LICENSE_KEY != FailedCheckResolve.NONE)
				{
					ClientBanManager.getInstance().acceptResolve(MainConfig.FAILED_CHECK_LICENSE_KEY, clientData);
					return false;
				}
				break;
			case RESPONSE_FAILED_GAME_SESSION_CHECK :
				if (MainConfig.FAILED_CHECK_GAME_SESSION != FailedCheckResolve.NONE)
				{
					ClientBanManager.getInstance().acceptResolve(MainConfig.FAILED_CHECK_GAME_SESSION, clientData);
					return false;
				}
				break;
			case RESPONSE_FAILED_LAUNCHED_STATE_CHECK :
				if (MainConfig.FAILED_CHECK_LAUNCH_STATE != FailedCheckResolve.NONE)
				{
					ClientBanManager.getInstance().acceptResolve(MainConfig.FAILED_CHECK_LAUNCH_STATE, clientData);
					return false;
				}
				break;
			case RESPONSE_FAILED_CLIENT_SIDE_VERSION_CHECK :
				if (MainConfig.FAILED_CHECK_CLIENT_SIDE_VERSION != FailedCheckResolve.NONE)
				{
					ClientBanManager.getInstance().acceptResolve(MainConfig.FAILED_CHECK_CLIENT_SIDE_VERSION, clientData);
					return false;
				}
				break;
			case RESPONSE_FAILED_ACTIVE_WINDOW_COUNT :
				if (MainConfig.FAILED_CHECK_ACTIVE_WINDOW != FailedCheckResolve.NONE)
				{
					ClientBanManager.getInstance().acceptResolve(MainConfig.FAILED_CHECK_ACTIVE_WINDOW, clientData);
					return false;
				}
				break;
			default :
				break;
		}
		
		if (ClientBanManager.getInstance().checkEnterFullHWIDBanned(clientData) || ClientBanManager.getInstance().checkEnterBlockHWIDBanned(clientData))
		{
			return false;
		}
		return true;
	}
	
	private static class LazyHolder
	{
		private static final ClientGameSessionManager INSTANCE = new ClientGameSessionManager();
	}
}