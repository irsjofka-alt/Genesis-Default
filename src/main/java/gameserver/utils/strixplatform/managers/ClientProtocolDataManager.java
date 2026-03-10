package gameserver.utils.strixplatform.managers;

import gameserver.utils.strixplatform.configs.MainConfig;
import gameserver.utils.strixplatform.logging.Log;
import gameserver.utils.strixplatform.network.ReadDataBuffer;
import gameserver.utils.strixplatform.utils.DataUtils;
import gameserver.utils.strixplatform.utils.ServerResponse;
import gameserver.utils.strixplatform.utils.StrixClientData;

public class ClientProtocolDataManager
{
	public static ClientProtocolDataManager getInstance()
	{
		return LazyHolder.INSTANCE;
	}
	
	public StrixClientData getDecodedData(byte[] dataArray, final int clientDataChecksum)
	{
		try
		{
			if (dataArray == null || dataArray.length < MainConfig.CLIENT_DATA_SIZE)
			{
				Log.error("Received client data nulled or not use Strix-Platform modules(Clear pacth or Strix-Platform not loaded)");
				return null;
			}
			
			final StrixClientData clientData = new StrixClientData();
			
			DataUtils.getDecodedDataFromKey(dataArray, DataUtils.getRealDataChecksum(clientDataChecksum));
			final int decodedDataChecksum = DataUtils.getDataChecksum(dataArray, false);
			if (decodedDataChecksum != DataUtils.getRealDataChecksum(clientDataChecksum))
			{
				Log.error("Received client data not valide. Client checksum: " + DataUtils.getRealDataChecksum(clientDataChecksum) + " Decoded checksum: " + decodedDataChecksum);
				clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_CLIENT_DATA_CHECKSUM_CHECK);
				return clientData;
			}
			
			if (MainConfig.STRIX_PLATFORM_DEBUG_ENABLED)
			{
				String data = "";
				for (int i = 0; i < 192; i++)
				{
					data += (char) dataArray[i];
				}
				Log.debug("ClientProtocolDataManager: first 192 byte " + data);
			}
			
			final ReadDataBuffer dataBuffer = new ReadDataBuffer(dataArray);
			clientData.setClientHWID(dataBuffer.ReadS());
			clientData.setVMPKey(dataBuffer.ReadS());
			clientData.setHWIDChecksum(dataBuffer.ReadQ());
			clientData.setDetectionResponse(dataBuffer.ReadQ());
			clientData.setLaunchStateResponse(dataBuffer.ReadQ());
			clientData.setSessionId(dataBuffer.ReadQ());
			clientData.setFilesChecksum(dataBuffer.ReadQ());
			clientData.setClientSideVersion(dataBuffer.ReadH());
			clientData.setActiveWindowCount(dataBuffer.ReadH());
			
			ClientGameSessionManager.getInstance().checkClientData(clientData);
			
			return clientData;
		}
		catch (final Exception e)
		{
			Log.error("Cannot decode Strix data from client. Please send this error and all needed info to Strix-Platform support! Exception: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	private static class LazyHolder
	{
		private static final ClientProtocolDataManager INSTANCE = new ClientProtocolDataManager();
	}
}