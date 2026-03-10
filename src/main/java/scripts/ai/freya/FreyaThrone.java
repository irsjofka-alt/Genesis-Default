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
package scripts.ai.freya;


import java.util.ArrayList;
import java.util.List;

import l2e.commons.util.Rnd;
import gameserver.ai.model.CtrlIntention;
import gameserver.ai.npc.Fighter;
import gameserver.data.parser.SkillsParser;
import gameserver.instancemanager.ReflectionManager;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.zone.ZoneType;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.ExShowScreenMessage;

public class FreyaThrone extends Fighter
{
	private final ZoneType _hall = ZoneManager.getInstance().getZoneById(20503);
	private final ZoneType _zone = ZoneManager.getInstance().getZoneById(90578);
	
	private static final int ETERNAL_BLIZZARD = 6274;
	private static final int ICE_BALL = 6278;
	private static final int SUMMON_ELEMENTAL = 6277;
	private static final int SELF_NOVA = 6279;
	private static final int DEATH_SENTENCE = 6280;
	private static final int ANGER = 6285;

	private long _blizzardReuseTimer = 0;
	private long _iceballReuseTimer = 0;
	private long _summonReuseTimer = 0;
	private long _selfnovaReuseTimer = 0;
	private long _deathsentenceReuseTimer = 0;
	private long _angerReuseTimer = 0;

	private final int _blizzardReuseDelay = 60;
	private final int _iceballReuseDelay = 20;
	private final int _summonReuseDelay = 60;
	private final int _selfnovaReuseDelay = 70;
	private final int _deathsentenceReuseDelay = 50;
	private final int _angerReuseDelay = 50;

	private final int _summonChance = 70;
	private final int _iceballChance = 60;
	private final int _deathsentenceChance = 60;
	private final int _angerChance = 60;

	public FreyaThrone(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void thinkAttack()
	{
		final Attackable actor = getActiveChar();
		final Creature mostHated = actor.getAggroList().getMostHated();

		if (!actor.isCastingNow() && (_blizzardReuseTimer < System.currentTimeMillis()))
		{
			actor.doCast(SkillsParser.getInstance().getInfo(ETERNAL_BLIZZARD, 1));
			final var r = actor.getReflection();
			if (!r.isDefault())
			{
				r.broadcastPacket(new ExShowScreenMessage(NpcStringId.STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE, 2, 3000));
			}
			else
			{
				final var players = _hall.getPlayersInside(ReflectionManager.DEFAULT);
				if (!players.isEmpty())
				{
					final var packet = new ExShowScreenMessage(NpcStringId.STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE, 2, 3000);
					for (final var activeChar : players)
					{
						activeChar.sendPacket(packet);
					}
				}
			}
			_blizzardReuseTimer = System.currentTimeMillis() + (_blizzardReuseDelay * 1000L);
		}

		if (!actor.isCastingNow() && !actor.isMoving() && (_iceballReuseTimer < System.currentTimeMillis()) && Rnd.chance(_iceballChance))
		{
			if ((mostHated != null) && !mostHated.isDead() && mostHated.isInRange(actor, 1000))
			{
				actor.setTarget(mostHated);
				actor.doCast(SkillsParser.getInstance().getInfo(ICE_BALL, 1));
				_iceballReuseTimer = System.currentTimeMillis() + (_iceballReuseDelay * 1000L);
			}
		}

		if (!actor.isCastingNow() && (_summonReuseTimer < System.currentTimeMillis()) && Rnd.chance(_summonChance))
		{
			actor.doCast(SkillsParser.getInstance().getInfo(SUMMON_ELEMENTAL, 1));
			for (final Npc npc : World.getAroundNpc(actor, 800, 200))
			{
				if (npc != null && npc.isMonster() && !npc.isDead())
				{
					npc.makeTriggerCast(SkillsParser.getInstance().getInfo(SUMMON_ELEMENTAL, 1), npc);
				}
			}
			_summonReuseTimer = System.currentTimeMillis() + (_summonReuseDelay * 1000L);
		}

		if (!actor.isCastingNow() && (_selfnovaReuseTimer < System.currentTimeMillis()))
		{
			actor.doCast(SkillsParser.getInstance().getInfo(SELF_NOVA, 1));
			_selfnovaReuseTimer = System.currentTimeMillis() + (_selfnovaReuseDelay * 1000L);
		}

		if (!actor.isCastingNow() && !actor.isMoving() && (_deathsentenceReuseTimer < System.currentTimeMillis()) && Rnd.chance(_deathsentenceChance))
		{
			if ((mostHated != null) && !mostHated.isDead() && mostHated.isInRange(actor, 1000))
			{
				actor.setTarget(mostHated);
				actor.doCast(SkillsParser.getInstance().getInfo(DEATH_SENTENCE, 1));
				_deathsentenceReuseTimer = System.currentTimeMillis() + (_deathsentenceReuseDelay * 1000L);
			}
		}

		if (!actor.isCastingNow() && !actor.isMoving() && (_angerReuseTimer < System.currentTimeMillis()) && Rnd.chance(_angerChance))
		{
			actor.setTarget(actor);
			actor.doCast(SkillsParser.getInstance().getInfo(ANGER, 1));
			_angerReuseTimer = System.currentTimeMillis() + (_angerReuseDelay * 1000L);
		}
		super.thinkAttack();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		final long generalReuse = System.currentTimeMillis() + 40000L;
		_blizzardReuseTimer += generalReuse + (Rnd.get(1, 20) * 1000L);
		_iceballReuseTimer += generalReuse + (Rnd.get(1, 20) * 1000L);
		_summonReuseTimer += generalReuse + (Rnd.get(1, 20) * 1000L);
		_selfnovaReuseTimer += generalReuse + (Rnd.get(1, 20) * 1000L);
		_deathsentenceReuseTimer += generalReuse + (Rnd.get(1, 20) * 1000L);
		_angerReuseTimer += generalReuse + (Rnd.get(1, 20) * 1000L);

		aggroPlayers(false);
	}

	@Override
	protected boolean thinkActive()
	{
		aggroPlayers(true);
		return super.thinkActive();
	}
	
	private void aggroPlayers(boolean searchTarget)
	{
		final var actor = getActiveChar();
		if (actor.isDead())
		{
			return;
		}
		
		final var r = actor.getReflection();
		if (!r.isDefault())
		{
			final List<Player> activeList = new ArrayList<>();
			for (final var activeChar : r.getReflectionPlayers())
			{
				if (_hall != null && _hall.isInsideZone(activeChar) || activeChar.isDead())
				{
					continue;
				}
					
				actor.addDamageHate(activeChar, 0, 2);
				if (searchTarget)
				{
					activeList.add(activeChar);
				}
			}
			
			if (!activeList.isEmpty())
			{
				final var attacked = activeList.get(Rnd.get(activeList.size()));
				if (attacked != null)
				{
					actor.setTarget(attacked);
					actor.getAI().setIntention(CtrlIntention.ATTACK, attacked);
				}
			}
		}
		else
		{
			final List<Player> activeList = new ArrayList<>();
			final var players = _zone.getPlayersInside(ReflectionManager.DEFAULT);
			if (!players.isEmpty())
			{
				for (final var activeChar : players)
				{
					if (activeChar != null && !activeChar.isDead())
					{
						actor.addDamageHate(activeChar, 0, 2);
						if (searchTarget)
						{
							activeList.add(activeChar);
						}
					}
				}
			}
			
			if (!activeList.isEmpty())
			{
				final var attacked = activeList.get(Rnd.get(activeList.size()));
				if (attacked != null)
				{
					actor.setTarget(attacked);
					actor.getAI().setIntention(CtrlIntention.ATTACK, attacked);
				}
			}
		}
	}
}
