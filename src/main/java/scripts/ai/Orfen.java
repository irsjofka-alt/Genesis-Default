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
package scripts.ai;

import l2e.commons.util.Rnd;
import gameserver.ai.npc.Fighter;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;
import gameserver.model.zone.type.BossZone;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter 05.12.2024
 */
public class Orfen extends Fighter
{
	private static final Location[] _pos =
	{
	        new Location(43728, 17220, -4342), new Location(55024, 17368, -5412), new Location(53504, 21248, -5486), new Location(53248, 24576, -5262)
	};
	
	public static final NpcStringId[] _text =
	{
	        NpcStringId.S1_STOP_KIDDING_YOURSELF_ABOUT_YOUR_OWN_POWERLESSNESS, NpcStringId.S1_ILL_MAKE_YOU_FEEL_WHAT_TRUE_FEAR_IS, NpcStringId.YOURE_REALLY_STUPID_TO_HAVE_CHALLENGED_ME_S1_GET_READY, NpcStringId.S1_DO_YOU_THINK_THATS_GOING_TO_WORK
	};
	
	private static BossZone _zone = (BossZone) ZoneManager.getInstance().getZoneById(12013);
	private long _reuseTimer = 0;
	
	public Orfen(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		final var actor = getActiveChar();
		if (actor == null || actor.isDead())
		{
			return false;
		}

		if (actor.isScriptValue(1))
		{
			if (actor.getCurrentHp() > (actor.getMaxHp() * 0.95))
			{
				actor.setScriptValue(0);
				setSpawnPoint(actor, Rnd.get(1, 3));
			}
			else if (!_zone.isInsideZone(actor))
			{
				setSpawnPoint(actor, 0);
			}
		}
		return true;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		super.onEvtAttacked(attacker, damage);
		final var actor = getActiveChar();
		if (actor == null || actor.isCastingNow() || actor.isDead())
		{
			return;
		}
		
		if (actor.isScriptValue(0) && (actor.getCurrentHp() < (actor.getMaxHp() / 2)))
		{
			actor.setScriptValue(1);
			setSpawnPoint(actor, 0);
			return;
		}
		
		final double distance = actor.getDistance(attacker);
		if (distance > 300 && distance < 1000 && _damSkills.length > 0 && Rnd.chance(10) && _reuseTimer < System.currentTimeMillis())
		{
			_reuseTimer = System.currentTimeMillis() + 120000L;
			final var packet = new NpcSay(actor.getObjectId(), Say2.NPC_ALL, actor.getId(), _text[Rnd.get(0, 3)]);
			packet.addStringParameter(attacker.getName(null).toString());
			actor.broadcastPacketToOthers(2000, packet);
			attacker.teleToLocation(Location.findFrontPosition(actor, attacker, 0, 50), true, attacker.getReflection());
			final var sk = _damSkills[Rnd.get(_damSkills.length)];
			if (sk != null && canUseSkill(sk, attacker, -1))
			{
				actor.setTarget(attacker);
				actor.doCast(sk);
			}
		}
		else if (_debuffSkills.length > 0 && Rnd.chance(1))
		{
			final var sk = _debuffSkills[Rnd.get(_debuffSkills.length)];
			if (sk != null && canUseSkill(sk, attacker, -1))
			{
				actor.setTarget(attacker);
				actor.doCast(sk);
			}
		}
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		super.onEvtSeeSpell(skill, caster);
		
		final var actor = getActiveChar();
		if(actor.isCastingNow())
		{
			return;
		}

		final double distance = actor.getDistance(caster);
		if (_damSkills.length > 0 && skill.getAggroPoints() > 0 && distance < 1000 && Rnd.chance(20) && _reuseTimer < System.currentTimeMillis())
		{
			_reuseTimer = System.currentTimeMillis() + 120000L;
			final var packet = new NpcSay(actor.getObjectId(), Say2.NPC_ALL, actor.getId(), _text[Rnd.get(4)]);
			packet.addStringParameter(caster.getName(null).toString());
			actor.broadcastPacketToOthers(2000, packet);
			caster.teleToLocation(Location.findFrontPosition(actor, caster, 0, 50), true, caster.getReflection());
			final var sk = _damSkills[Rnd.get(_damSkills.length)];
			if (sk != null && canUseSkill(sk, caster, -1))
			{
				actor.setTarget(caster);
				actor.doCast(sk);
			}
		}
	}
	
	private void setSpawnPoint(Attackable npc, int index)
	{
		npc.getSpawn().setLocation(_pos[index]);
		npc.teleToLocation(_pos[index], true, npc.getReflection());
		npc.clearAggroList(true);
	}
}