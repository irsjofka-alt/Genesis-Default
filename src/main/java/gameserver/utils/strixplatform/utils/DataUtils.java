package gameserver.utils.strixplatform.utils;

import gameserver.utils.strixplatform.configs.MainConfig;

public class DataUtils
{
	public static void getDecodedDataFromKey(byte[] dataArray, final int dataXoredKey)
	{
		final String dataXoredKeyString = String.valueOf(dataXoredKey);
		final char xorKey[] = dataXoredKeyString.toCharArray();
		for(int i = 0; i < dataArray.length; i++)
		{
			dataArray[i] ^= xorKey[i % xorKey.length]; 
		}
	}

	public static int getDataChecksum(byte[] byteArray, final boolean isAdler32)
	{
		int a = 1, b = 0;
		for(int i = 0; i < byteArray.length; ++i)
		{
			a = (a + (byteArray[i] & 0xff)) % (isAdler32 ? 65521 : MainConfig.STRIX_PLATFORM_SECOND_KEY);
			b = (b + a) % (isAdler32 ? 65521 : MainConfig.STRIX_PLATFORM_SECOND_KEY);
		}
		return (b << 16) | a;
	}

	public static int getRealDataChecksum(final int dataChecksum)
	{
		return dataChecksum - MainConfig.STRIX_PLATFORM_SECOND_KEY;
	}
}