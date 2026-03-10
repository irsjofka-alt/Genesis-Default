package gameserver.utils.strixplatform.utils;

import java.util.NoSuchElementException;

public enum DetectionResponse
{
	RESPONSE_OK(1000, "Clear client"), RESPONSE_L2TOWER(1001, "L2Tower detected automatical"), RESPONSE_L2WALKER(1002, "L2Walker detected automatical"), RESPONSE_ADRENALIN(1003, "Adrenalin detected automatical"), RESPONSE_CHEAT_ENGINE_SPEED_HACK(1004, "Cheat engine speed hack detected automatical"), RESPONSE_L2HLAPEX(1005, "L2Hlapex detected automatical"), RESPONSE_BASE_FUNCTION_HOOKED(1006, "Base function hooked detected automatical"), RESPONSE_CHANGED_PACKET_DATA(1007, "Change packet data detected automatical");
	
	private final int response;
	private final String description;
	
	DetectionResponse(final int response, final String description)
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
	
	public static DetectionResponse valueOf(final int id)
	{
		for (final DetectionResponse detectionResponse : values())
		{
			if (detectionResponse.getResponse() == id)
			{
				return detectionResponse;
			}
		}
		throw new NoSuchElementException("Not find DetectionResponse by id: " + id);
	}
}