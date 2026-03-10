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
package gameserver.model.actor.instance;

import gameserver.ai.character.CharacterAI;
import gameserver.ai.npc.EffectPointAI;
import gameserver.data.parser.SkillsParser;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.skills.Skill;
import gameserver.model.skills.l2skills.SkillSignet;
import gameserver.model.skills.l2skills.SkillSignetCasttime;
import gameserver.model.zone.ZoneId;

public class EffectPointInstance extends Npc
{
	private final Player _owner;
	private Skill _skill;
	private Skill _mainSkill;
	private final boolean _srcInArena;
	private boolean _isCastTime = false;
	
	public EffectPointInstance(int objectId, NpcTemplate template, Creature owner, Skill skill)
	{
		super(objectId, template);
		setInstanceType(InstanceType.EffectPointInstance);
		_ai = new EffectPointAI(this);
		setIsInvul(false);
		_owner = owner == null ? null : owner.getActingPlayer();
		_srcInArena = _owner != null ? _owner.isInsideZone(ZoneId.PVP) && !_owner.isInsideZone(ZoneId.SIEGE) : false;
		if (owner != null)
		{
			setReflection(owner.getReflection());
		}
		
		if (skill != null && (skill instanceof SkillSignet || skill instanceof SkillSignetCasttime))
		{
			_skill = SkillsParser.getInstance().getInfo(skill.getEffectId(), skill.getLevel());
			_mainSkill = skill;
			_isCastTime = skill instanceof SkillSignetCasttime;
		}
	}

	@Override
	public Player getActingPlayer()
	{
		return _owner;
	}

	@Override
	public void onAction(Player player, boolean interact, boolean shift)
	{
		player.sendActionFailed();
	}

	@Override
	public void onActionShift(Player player)
	{
		if (player == null)
		{
			return;
		}

		player.sendActionFailed();
	}
	
	@Override
	public CharacterAI getAI()
	{
		return _ai;
	}
	
	public boolean isInSrcInArena()
	{
		return _srcInArena;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	public Skill getMainSkill()
	{
		return _mainSkill;
	}
	
	public boolean isCastTime()
	{
		return _isCastTime;
	}
	
	@Override
	public boolean isEffectPoint()
	{
		return true;
	}
}