package gameserver.utils.strixplatform.utils;

import java.util.NoSuchElementException;

public enum LaunchStateResponse
{
	RESPONSE_LAUNCHED_NORMAL(2100, "Client launched from system binary"), RESPONSE_LAUNCHED_FROM_LAUNCHER(2101, "Client launched from launcher(updater)"), RESPONSE_LAUNCHED_ON_VIRTUAL_MACHINE(2102, "Client launched on virtual machine"), RESPONSE_LAUNCHED_ON_VIRTUAL_MACHIME_AND_FROM_LAUNCHER(2103, "Client launched on virtual machine from updater");
	
	private final int response;
	private final String description;
	
	LaunchStateResponse(final int response, final String description)
	{
		this.response = response;
		this.description = description;
	}
	
	public int getResponse()
	{
		return response;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public static LaunchStateResponse valueOf(final int id)
	{
		for (final LaunchStateResponse detectionResponse : values())
		{
			if (detectionResponse.getResponse() == id)
			{
				return detectionResponse;
			}
		}
		throw new NoSuchElementException("Not find LaunchStateResponse by id: " + id);
	}
}