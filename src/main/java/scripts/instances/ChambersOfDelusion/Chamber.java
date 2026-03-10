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
package scripts.instances.ChambersOfDelusion;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.ReflectionParser;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.GameObject;
import gameserver.model.Location;
import gameserver.model.Party;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.actor.templates.reflection.ReflectionTemplate.ReflectionEntryType;
import gameserver.model.entity.Reflection;
import gameserver.model.holders.SkillHolder;
import gameserver.model.skills.Skill;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.EarthQuake;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.network.serverpackets.NpcSay;
import scripts.instances.AbstractReflection;

/**
 * Rework by LordWinter 14.11.2020
 */
public abstract class Chamber extends AbstractReflection
{
	private final int _enterNpc;
	private final int _gkFirst;
	private final int _gkLast;
	private final int _raid;
	private final int _box;
	private final int _reflectionId;
	private final String _boxGroup;
	
	protected Location[] _coords;

	protected Chamber(int reflectionId, int enterNpc, int gkFirst, int gkLast, int raid, int box, String boxGroup)
	{
		super(127, 128, 129, 130, 131, 132);

		_reflectionId = reflectionId;
		_enterNpc = enterNpc;
		_gkFirst = gkFirst;
		_gkLast = gkLast;
		_raid = raid;
		_box = box;
		_boxGroup = boxGroup;
		
		addStartNpc(_enterNpc);
		addTalkId(_enterNpc);
		
		for (int i = _gkFirst; i <= _gkLast; i++)
		{
			addStartNpc(i);
			addTalkId(i);
		}
		
		addAttackId(_box);
		addSpellFinishedId(_box);
		addEventReceivedId(_box);
		addKillId(_raid);
	}

	private boolean isBigChamber()
	{
		return ((_reflectionId == 131) || (_reflectionId == 132));
	}

	private boolean isBossRoom(Reflection r)
	{
		return r.getParams().getInteger("CURRENT_ROOM", 0) == (_coords.length - 1);
	}

	protected void changeRoom(Reflection r)
	{
		if (r == null)
		{
			return;
		}
		
		final var party = r.getParams().getObject("PARTY", Party.class);
		final var isSoloEnter = r.getParams().getInteger("SOLO_ENTER", 0) == 1;
		if ((party == null || party.getMemberCount() < 2) && !isSoloEnter)
		{
			return;
		}
		
		final int bossRoomChance = r.getParams().getInteger("bossRoomChance");
		int newRoom = r.getParams().getInteger("CURRENT_ROOM", 0);

		if (isBigChamber() && isBossRoom(r))
		{
			return;
		}
		else if (isBigChamber() && ((r.getInstanceEndTime() - System.currentTimeMillis()) < 600000))
		{
			newRoom = _coords.length - 1;
		}
		else if (!isBossRoom(r) && (bossRoomChance > 0 && Rnd.chance(bossRoomChance)))
		{
			newRoom = _coords.length - 1;
		}
		else
		{
			while (newRoom == r.getParams().getInteger("CURRENT_ROOM", 0))
			{
				newRoom = getRandom(_coords.length - 1);
			}
		}

		if (!isSoloEnter)
		{
			for (final var partyMember : party.getMembers())
			{
				if (r.getId() == partyMember.getReflectionId())
				{
					partyMember.getAI().setIntention(CtrlIntention.IDLE);
					teleportPlayer(partyMember, _coords[newRoom], r);
				}
			}
		}
		else
		{
			final var players = r.getReflectionPlayers();
			if (!players.isEmpty())
			{
				for (final var player : players)
				{
					player.getAI().setIntention(CtrlIntention.IDLE);
					teleportPlayer(player, _coords[newRoom], r);
				}
			}
		}
		r.setParam("CURRENT_ROOM", newRoom);
		
		if (isBigChamber() && isBossRoom(r))
		{
			r.setDuration((int) ((r.getInstanceEndTime() - System.currentTimeMillis()) + 1200000));
			for (final var npc : r.getNpcs())
			{
				if (npc.getId() == _gkLast)
				{
					npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.N21_MINUTES_ARE_ADDED_TO_THE_REMAINING_TIME_IN_THE_INSTANT_ZONE));
				}
			}
		}
		else
		{
			scheduleRoomChange(r, false);
		}
	}
	
	protected synchronized void enterInstance(Player player, Npc npc, int templateId)
	{
		final var party = player.isInParty() ? player.getParty() : null;
		if (enterReflection(player, npc, templateId))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.setParam("CURRENT_ROOM", 0);
				final var template = ReflectionParser.getInstance().getReflectionId(templateId);
				if (template != null)
				{
					if ((party == null || party.getMemberCount() < 2) && (template.getEntryType() == ReflectionEntryType.SOLO || template.getEntryType() == ReflectionEntryType.SOLO_PARTY))
					{
						r.setParam("SOLO_ENTER", 1);
					}
					
					if (r.getParams().getInteger("SOLO_ENTER", 0) == 0 && party != null)
					{
						r.setParam("PARTY", party);
						for (final var member : party.getMembers())
						{
							if (member != null)
							{
								if (hasQuestItems(member, 15311))
								{
									takeItems(member, 15311, -1);
								}
								
								if (party.isLeader(member))
								{
									giveItems(member, 15311, 1);
								}
							}
						}
						r.addTimer("BANISH", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> banishTask(r), 60000, 60000));
					}
				}
				changeRoom(r);
			}
		}
	}
	
	@Override
	protected void onTeleportEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			r.addAllowed(player);
			player.setReflection(r);
			if (player.hasSummon())
			{
				player.getSummon().setReflection(r);
			}
		}
		else
		{
			final var loc = _coords[r.getParams().getInteger("CURRENT_ROOM", 0)];
			teleportPlayer(player, loc, r);
			if (player.hasSummon())
			{
				player.getSummon().teleToLocation(loc, true, r);
			}
		}
	}

	protected void exitInstance(Reflection r, Player player)
	{
		if (player == null || !player.isOnline() || player.getReflectionId() == 0)
		{
			return;
		}
		r.removeAllowed(player);
		teleportPlayer(player, r.getReturnLoc(), ReflectionManager.DEFAULT);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final var html = new NpcHtmlMessage(npc.getObjectId());
		final var r = npc.getReflection();
		if (isInReflection(r) && (npc.getId() >= _gkFirst) && (npc.getId() <= _gkLast))
		{
			final var isSoloEnter = r.getParams().getInteger("SOLO_ENTER", 0) == 1;
			
			var st = player.getQuestState(getName());
			if (st == null)
			{
				st = newQuestState(player);
			}
			else if (event.equals("next_room"))
			{
				if (!isSoloEnter)
				{
					if (player.getParty() == null)
					{
						html.setFile(player, player.getLang(), "data/html/scripts/instances/ChambersOfDelusion/no_party.htm");
						player.sendPacket(html);
						return null;
					}
					else if (player.getParty().getLeaderObjectId() != player.getObjectId())
					{
						html.setFile(player, player.getLang(), "data/html/scripts/instances/ChambersOfDelusion/no_leader.htm");
						player.sendPacket(html);
						return null;
					}
				}
				
				if (hasQuestItems(player, 15311))
				{
					st.takeItems(15311, 1);
					r.removeTimer("CHANGE_ROOM");
					changeRoom(r);
					return null;
				}
				else
				{
					html.setFile(player, player.getLang(), "data/html/scripts/instances/ChambersOfDelusion/no_item.htm");
					player.sendPacket(html);
					return null;
				}
			}
			else if (event.equals("go_out"))
			{
				if (!isSoloEnter)
				{
					if (player.getParty() == null)
					{
						html.setFile(player, player.getLang(), "data/html/scripts/instances/ChambersOfDelusion/no_party.htm");
						player.sendPacket(html);
						return null;
					}
					else if (player.getParty().getLeaderObjectId() != player.getObjectId())
					{
						html.setFile(player, player.getLang(), "data/html/scripts/instances/ChambersOfDelusion/no_leader.htm");
						player.sendPacket(html);
						return null;
					}
				}
				
				r.stopAllTimers();
				if (!isSoloEnter)
				{
					for (final var partyMember : player.getParty().getMembers())
					{
						exitInstance(r, partyMember);
					}
				}
				else
				{
					final var players = r.getReflectionPlayers();
					if (players.isEmpty())
					{
						for (final var pl : players)
						{
							exitInstance(r, pl);
						}
					}
				}
				r.setEmptyDestroyTime(0);
				return null;
			}
			else if (event.equals("look_party"))
			{
				if (player.getParty() != null && !isSoloEnter)
				{
					teleportPlayer(player, _coords[r.getParams().getInteger("CURRENT_ROOM", 0)], r);
				}
				return null;
			}
		}
		return null;
	}

	@Override
	public String onAttack(final Npc npc, final Player attacker, final int damage, final boolean isPet, final Skill skill)
	{
		if (!npc.isBusy() && (npc.getCurrentHp() < (npc.getMaxHp() / 10)))
		{
			npc.setBusy(true);
			final MonsterInstance box = (MonsterInstance) npc;
			if (getRandom(100) < 25)
			{
				if (getRandom(100) < 33)
				{
					box.dropSingleItem(attacker, 4042, (int) (3 * Config.RATE_DROP_ITEMS));
				}
				if (getRandom(100) < 50)
				{
					box.dropSingleItem(attacker, 4044, (int) (4 * Config.RATE_DROP_ITEMS));
				}
				if (getRandom(100) < 50)
				{
					box.dropSingleItem(attacker, 4043, (int) (4 * Config.RATE_DROP_ITEMS));
				}
				if (getRandom(100) < 16)
				{
					box.dropSingleItem(attacker, 9628, (int) (2 * Config.RATE_DROP_ITEMS));
				}
				box.broadcastEvent("SCE_LUCKY", 2000, null);
				box.doCast(new SkillHolder(5758, 1).getSkill());
			}
			else
			{
				box.broadcastEvent("SCE_DREAM_FIRE_IN_THE_HOLE", 2000, null);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onEventReceived(String eventName, Npc sender, Npc receiver, GameObject reference)
	{
		switch (eventName)
		{
			case "SCE_LUCKY" :
				receiver.setBusy(true);
				receiver.doCast(new SkillHolder(5758, 1).getSkill());
				break;
			case "SCE_DREAM_FIRE_IN_THE_HOLE" :
				receiver.setBusy(true);
				receiver.doCast(new SkillHolder(5376, 4).getSkill());
				break;
		}
		return null;
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			r.spawnByGroup(_boxGroup);
			if (isBigChamber())
			{
				finishInstance(r, true);
			}
			else
			{
				r.removeTimer("CHANGE_ROOM");
				scheduleRoomChange(r, true);
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	private void banishTask(Reflection r)
	{
		if (r != null)
		{
			if ((r.getInstanceEndTime() - System.currentTimeMillis()) < 60000)
			{
				r.removeTimer("BANISH");
			}
			else
			{
				final var isSoloEnter = r.getParams().getInteger("SOLO_ENTER", 0) == 1;
				for (final var pl : r.getReflectionPlayers())
				{
					if (!isSoloEnter && !pl.isInParty())
					{
						exitInstance(r, pl);
					}
				}
			}
		}
	}
	
	private void changeRoomTask(Reflection r)
	{
		if (r != null)
		{
			for (final var player : r.getReflectionPlayers())
			{
				player.sendPacket(new EarthQuake(player.getX(), player.getY(), player.getZ(), 20, 10));
			}
			
			try
			{
				Thread.sleep(5000);
			}
			catch (final InterruptedException _)
			{
			}
			changeRoom(r);
		}
	}
	
	private void scheduleRoomChange(Reflection r, boolean bossRoom)
	{
		final long nextInterval = bossRoom ? 60000L : (480 + getRandom(120)) * 1000L;
		if ((r.getInstanceEndTime() - System.currentTimeMillis()) > nextInterval)
		{
			r.addTimer("CHANGE_ROOM", ThreadPoolManager.getInstance().schedule(() -> changeRoomTask(r), nextInterval - 5000));
		}
	}

	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		if ((npc.getId() == _box) && ((skill.getId() == 5376) || (skill.getId() == 5758)) && !npc.isDead())
		{
			npc.doDie(player);
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		if (npc.getId() == _enterNpc)
		{
			enterInstance(player, npc, _reflectionId);
		}
		return null;
	}

	public static void main(String[] args)
	{
	}
}
