/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://l2jeternity.com/>.
 */
package gameserver.model.service.buffer;

public class SingleBuff extends SchemeBuff
{
	private final String _buffType;
	private final int _buffTime;
	private final int _premiumBuffTime;
	private final int[][] _requestItems;
	private final boolean _needAllItems;
	private final boolean _isBuffForItems;
	private final boolean _removeItems;
	private final boolean _allowSave;
	
	public SingleBuff(String buffType, int buffId, int buffLevel, int premiumBuffLevel, int buffTime, int premiumBuffTime, boolean isDanceSlot, int[][] requestItems, boolean needAllItems, boolean removeItems, boolean allowSave)
	{
		super(buffId, buffLevel, premiumBuffLevel, isDanceSlot);
		_buffType = buffType;
		_buffTime = buffTime;
		_premiumBuffTime = premiumBuffTime;
		_requestItems = requestItems;
		_needAllItems = needAllItems;
		_removeItems = removeItems;
		_isBuffForItems = _requestItems != null;
		_allowSave = allowSave;
	}
	
	public String getBuffType()
	{
		return _buffType;
	}
	
	public int getBuffTime()
	{
		return _buffTime;
	}
	
	public int getPremiumBuffTime()
	{
		return _premiumBuffTime;
	}
	
	public int[][] getRequestItems()
	{
		return _requestItems;
	}
	
	public boolean needAllItems()
	{
		return _needAllItems;
	}
	
	public boolean isBuffForItems()
	{
		return _isBuffForItems;
	}
	
	public boolean isRemoveItems()
	{
		return _removeItems;
	}
	
	public boolean isAllowSave()
	{
		return _allowSave;
	}
}