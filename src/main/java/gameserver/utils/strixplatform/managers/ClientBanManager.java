package gameserver.utils.strixplatform.managers;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import gameserver.utils.strixplatform.configs.MainConfig;
import gameserver.utils.strixplatform.database.impl.BanDAO;
import gameserver.utils.strixplatform.logging.Log;
import gameserver.utils.strixplatform.utils.BannedHWIDInfo;
import gameserver.utils.strixplatform.utils.DetectionResponse;
import gameserver.utils.strixplatform.utils.FailedCheckResolve;
import gameserver.utils.strixplatform.utils.ServerResponse;
import gameserver.utils.strixplatform.utils.StrixClientData;

public class ClientBanManager
{
	private final Map<String, BannedHWIDInfo> bannedHWIDInfo;
	private final Queue<String> bannedHWIDBlockInfo;
	
	public static ClientBanManager getInstance()
	{
		return LazyHolder.INSTANCE;
	}
	
	public ClientBanManager()
	{
		bannedHWIDInfo = BanDAO.getInstance().loadAllBannedHWID();
		bannedHWIDBlockInfo = new ConcurrentLinkedQueue<>();
		Log.info("Loaded [" + bannedHWIDInfo.size() + "] banned client HWID data");
		if (bannedHWIDInfo.size() > 0)
		{
			for (final String fullHwid : bannedHWIDInfo.keySet())
			{
				addHWIDBlock(fullHwid);
			}
			Log.info("Loaded [" + bannedHWIDBlockInfo.size() + "] banned HWID block");
		}
	}
	
	private void addHWIDBlock(final String fullHwid)
	{
		try
		{
			final String firstBlock = fullHwid.substring(0, 8);
			final String secondBlock = fullHwid.substring(8, 16);
			final String thirdBlock = fullHwid.substring(16, 24);
			final String fourthBlock = fullHwid.substring(24, 32);
			
			if (!bannedHWIDBlockInfo.contains(firstBlock))
			{
				bannedHWIDBlockInfo.add(firstBlock);
			}
			if (!bannedHWIDBlockInfo.contains(secondBlock))
			{
				bannedHWIDBlockInfo.add(secondBlock);
			}
			if (!bannedHWIDBlockInfo.contains(thirdBlock))
			{
				bannedHWIDBlockInfo.add(thirdBlock);
			}
			if (!bannedHWIDBlockInfo.contains(fourthBlock))
			{
				bannedHWIDBlockInfo.add(fourthBlock);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void removeHWIDBlock(final String fullHwid)
	{
		try
		{
			final String firstBlock = fullHwid.substring(0, 8);
			final String secondBlock = fullHwid.substring(8, 16);
			final String thirdBlock = fullHwid.substring(16, 24);
			final String fourthBlock = fullHwid.substring(24, 32);
			
			if (bannedHWIDBlockInfo.contains(firstBlock))
			{
				bannedHWIDBlockInfo.remove(firstBlock);
			}
			if (bannedHWIDBlockInfo.contains(secondBlock))
			{
				bannedHWIDBlockInfo.remove(secondBlock);
			}
			if (bannedHWIDBlockInfo.contains(thirdBlock))
			{
				bannedHWIDBlockInfo.remove(thirdBlock);
			}
			if (bannedHWIDBlockInfo.contains(fourthBlock))
			{
				bannedHWIDBlockInfo.remove(fourthBlock);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void acceptResolve(final FailedCheckResolve failedCheckResolve, final StrixClientData clientData)
	{
		if (failedCheckResolve == FailedCheckResolve.BAN)
		{
			Log.audit("Server resolve [" + failedCheckResolve.toString() + "] for failed check [" + clientData.getServerResponse().toString() + "]");
			tryToStoreBan(clientData);
		}
	}
	
	public boolean checkEnterFullHWIDBanned(final StrixClientData clientData)
	{
		final String clientHWID = clientData.getClientHWID();
		if (clientHWID.length() != 32)
		{
			Log.error("Client HWID=[" + clientHWID + "] not correct for size! Please send this message to Strix-Platform support!");
			return true;
		}
		
		if (bannedHWIDInfo.containsKey(clientHWID))
		{
			final BannedHWIDInfo bhi = bannedHWIDInfo.get(clientHWID);
			if (bhi.getTimeExpire() <= System.currentTimeMillis())
			{
				tryToDeleteBan(clientData);
				Log.audit("Client HWID=[" + clientHWID + "] ban expired and deleted from database and cache");
			}
			else
			{
				Log.audit("Client HWID=[" + clientHWID + "] attemp to enter from full banned HWID");
				return true;
			}
		}
		
		return false;
	}
	
	public boolean checkEnterBlockHWIDBanned(final StrixClientData clientData)
	{
		final String clientHWID = clientData.getClientHWID();
		if (clientHWID.length() != 32)
		{
			Log.error("Client HWID=[" + clientHWID + "] not correct for size! Please send this message to Strix-Platform support!");
			return true;
		}
		
		final String firstBlock = clientHWID.substring(0, 8);
		final String secondBlock = clientHWID.substring(8, 16);
		final String thirdBlock = clientHWID.substring(16, 24);
		final String fourthBlock = clientHWID.substring(24, 32);
		
		int blockFindedInBan = 0;
		if (bannedHWIDBlockInfo.contains(firstBlock))
		{
			blockFindedInBan++;
		}
		if (bannedHWIDBlockInfo.contains(secondBlock))
		{
			blockFindedInBan++;
		}
		if (bannedHWIDBlockInfo.contains(thirdBlock))
		{
			blockFindedInBan++;
		}
		if (bannedHWIDBlockInfo.contains(fourthBlock))
		{
			blockFindedInBan++;
		}
		
		if (blockFindedInBan > 0 && blockFindedInBan >= MainConfig.STRIX_PLATFORM_HWID_BLOCK_TO_LOCK)
		{
			Log.audit("Client HWID=[" + clientHWID + "] attemp to enter from banned count=[" + blockFindedInBan + "] block");
			clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_HWID_BLOCK_BLOCKED);
			return true;
		}
		
		return false;
	}
	
	public void tryToStoreBan(final StrixClientData clientData)
	{
		if (bannedHWIDInfo == null || bannedHWIDBlockInfo == null)
		{
			return;
		}
		
		final String clientHWID = clientData.getClientHWID();
		if (!bannedHWIDInfo.containsKey(clientData.getClientHWID()))
		{
			final BannedHWIDInfo bhi = new BannedHWIDInfo(clientData.getClientHWID(), System.currentTimeMillis() + (MainConfig.STRIX_PLATFORM_AUTOMATICAL_BAN_TIME * 60 * 1000), ((clientData.getDetectionResponse() != null && clientData.getDetectionResponse() != DetectionResponse.RESPONSE_OK) ? clientData.getDetectionResponse().getDescription() : clientData.getServerResponse().toString()), "AUTOMATICAL_BAN");
			
			if (BanDAO.getInstance().insert(bhi))
			{
				bannedHWIDInfo.put(clientHWID, bhi);
				addHWIDBlock(clientHWID);
				Log.audit("Client HWID=[" + clientHWID + "] added in cache and stored to database");
			}
		}
		else
		{
			Log.audit("Client HWID=[" + clientHWID + "] finded in cache and not store in database");
		}
	}
	
	public void tryToStoreBan(final BannedHWIDInfo bhi)
	{
		if (bannedHWIDInfo == null || bannedHWIDBlockInfo == null)
		{
			return;
		}
		
		if (!bannedHWIDInfo.containsKey(bhi.getHWID()))
		{
			if (BanDAO.getInstance().insert(bhi))
			{
				bannedHWIDInfo.put(bhi.getHWID(), bhi);
				addHWIDBlock(bhi.getHWID());
				Log.audit("Client HWID=[" + bhi.getHWID() + "] added in cache and stored to database");
			}
		}
		else
		{
			Log.audit("Client HWID=[" + bhi.getHWID() + "] finded in cache and not store in database");
		}
	}
	
	public void tryToDeleteBan(final StrixClientData clientData)
	{
		if (bannedHWIDInfo == null || bannedHWIDBlockInfo == null)
		{
			return;
		}
		
		final String clientHWID = clientData.getClientHWID();
		if (BanDAO.getInstance().delete(clientHWID))
		{
			bannedHWIDInfo.remove(clientHWID);
			removeHWIDBlock(clientHWID);
		}
	}
	
	public void tryToDeleteBan(final String clientHWID)
	{
		if (bannedHWIDInfo == null || bannedHWIDBlockInfo == null)
		{
			return;
		}
		
		if (BanDAO.getInstance().delete(clientHWID))
		{
			bannedHWIDInfo.remove(clientHWID);
			removeHWIDBlock(clientHWID);
		}
	}
	
	private static class LazyHolder
	{
		private static final ClientBanManager INSTANCE = new ClientBanManager();
	}
}