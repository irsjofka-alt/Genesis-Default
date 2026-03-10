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
package gameserver.model.actor.templates;

/**
 * Created by LordWinter
 */
public class TimeSkillTemplate
{
	private final int _objId;
	private final int _skillId;
	private final int _skillLevel;
	private final boolean _isClanSkill;
	private long _time = 0;
	
	public TimeSkillTemplate(int objId, int skillId, int skillLevel, boolean isClanSkill)
	{
		_objId = objId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_isClanSkill = isClanSkill;
	}
	
	public int getId()
	{
		return _objId;
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public int getSkillLevel()
	{
		return _skillLevel;
	}
	
	public boolean isClanSkill()
	{
		return _isClanSkill;
	}

	public void setTime(long expireTime)
	{
		_time = expireTime;
	}
	
	public long getTime()
	{
		return _time;
	}
}