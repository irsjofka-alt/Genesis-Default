package gameserver.utils.strixplatform.network;

import gameserver.utils.strixplatform.utils.StrixClientData;

public abstract interface IStrixClientData
{
	public abstract void setStrixClientData(final StrixClientData clientData);

	public abstract StrixClientData getStrixClientData();
}