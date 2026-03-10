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
package scripts.instances;

import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.HellboundManager;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.Party;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.QuestGuardInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.holders.SkillHolder;
import gameserver.model.skills.Skill;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;
import gameserver.utils.Util;

/**
 * Rework by LordWinter 02.10.2020
 */
public class HellboundTown extends AbstractReflection
{
	private static final NpcStringId[] NPCSTRING_ID =
	{
	        NpcStringId.INVADER, NpcStringId.YOU_HAVE_DONE_WELL_IN_FINDING_ME_BUT_I_CANNOT_JUST_HAND_YOU_THE_KEY
	};

	private static final NpcStringId[] NATIVES_NPCSTRING_ID =
	{
	        NpcStringId.THANK_YOU_FOR_SAVING_ME, NpcStringId.GUARDS_ARE_COMING_RUN, NpcStringId.NOW_I_CAN_ESCAPE_ON_MY_OWN
	};

	public HellboundTown()
	{
		super(2);

		addFirstTalkId(32358);
		addStartNpc(32346, 32358);
		addTalkId(32346, 32358);
		addAttackId(22359, 22361);
		addAggroRangeEnterId(22359);
		addKillId(22449, 32358, 22359, 22360, 22361);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 2))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				final var amaskari = addSpawn(22449, new Location(19424, 253360, -2032, 16860), false, 0, false, r);
				r.setParam("amaskari", amaskari);
				r.setParam("amaskariDead", false);
			}
		}
	}
	
	@Override
	protected void onTeleportEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			r.addAllowed(player);
			player.getAI().setIntention(CtrlIntention.IDLE);
			final var teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.IDLE);
			final var teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
	}

	@Override
	public final String onFirstTalk(Npc npc, Player player)
	{
		if (npc.getFirstEffect(4616) == null)
		{
			return "32358-02.htm";
		}
		return "32358-01.htm";
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = null;
		if (npc.getId() == 32346)
		{
			htmltext = checkConditions(player);
			if (htmltext == null)
			{
				enterInstance(player, npc);
			}
		}
		else if (npc.getId() == 32343)
		{
			final var r = npc.getReflection();
			if (isInReflection(r))
			{
				final var party = player.getParty();
				if (party == null || party.getMemberCount() < 2)
				{
					htmltext = "32343-02.htm";
				}
				else if (npc.isBusy())
				{
					htmltext = "32343-02c.htm";
				}
				else if (player.getInventory().getInventoryItemCount(9714, -1, false) >= 1)
				{
					for (final var partyMember : party.getMembers())
					{
						if (!Util.checkIfInRange(300, npc, partyMember, true))
						{
							return "32343-02b.htm";
						}
					}

					if (player.destroyItemByItemId("Quest", 9714, 1, npc, true))
					{
						npc.setBusy(true);
						r.setDuration(5 * 60000);
						r.setEmptyDestroyTime(0);
						r.addTimer("EXIT_REFLECTION", ThreadPoolManager.getInstance().schedule(() -> exitReflection(party, r), 285000));
						htmltext = "32343-02d.htm";
					}
				}
				else
				{
					htmltext = "32343-02a.htm";
				}
			}
		}
		return htmltext;
	}

	@Override
	public final String onAdvEvent(String event, Npc npc, Player player)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (npc.getId() == 32358)
			{
				final var amaskari = r.getParams().getObject("amaskari", Npc.class);
				final var amaskariDead = r.getParams().getBool("amaskariDead", false);
				if (event.equalsIgnoreCase("rebuff") && !amaskariDead)
				{
					new SkillHolder(4616, 1).getSkill().getEffects(npc, npc, false, true);
				}
				else if (event.equalsIgnoreCase("break_chains"))
				{
					if ((npc.getFirstEffect(4611) == null) || amaskariDead)
					{
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NATIVES_NPCSTRING_ID[0]));
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NATIVES_NPCSTRING_ID[2]));
					}
					else
					{
						cancelQuestTimer("rebuff", npc, null);
						for (final var e : npc.getAllEffects())
						{
							if (e.getSkill() == new SkillHolder(4616, 1).getSkill())
							{
								e.exit(true);
							}
						}
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NATIVES_NPCSTRING_ID[0]));
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NATIVES_NPCSTRING_ID[1]));
						HellboundManager.getInstance().updateTrust(10, true);
						npc.scheduleDespawn(3000);

						if ((amaskari != null) && !amaskari.isDead() && (getRandom(1000) < 25) && Util.checkIfInRange(5000, npc, amaskari, false))
						{
							r.addTimer("CALL_AMASKARI", ThreadPoolManager.getInstance().schedule(() -> callAmaskari(npc), 25000));
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public final String onSpawn(Npc npc)
	{
		if (npc.getId() == 32358)
		{
			((QuestGuardInstance) npc).setPassive(true);
			npc.setAutoAttackable(false);
			new SkillHolder(4616, 1).getSkill().getEffects(npc, npc, false, true);
			startQuestTimer("rebuff", 357000, npc, null);
		}
		else if ((npc.getId() == 22359) || (npc.getId() == 22361))
		{
			npc.setBusy(false);
			npc.setBusyMessage("");
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (!npc.isBusy())
			{
				npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NPCSTRING_ID[0]));
				npc.setBusy(true);

				final var amaskari = r.getParams().getObject("amaskari", Npc.class);
				if ((amaskari != null) && !amaskari.isDead() && (getRandom(1000) < 25) && Util.checkIfInRange(1000, npc, amaskari, false))
				{
					r.addTimer("CALL_AMASKARI", ThreadPoolManager.getInstance().schedule(() -> callAmaskari(npc), 25000));
				}
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final var amaskariDead = r.getParams().getBool("amaskariDead", false);
			if (!amaskariDead && !(npc.getBusyMessage().equalsIgnoreCase("atk") || npc.isBusy()))
			{
				int msgId;
				int range;
				switch (npc.getId())
				{
					case 22359 :
						msgId = 0;
						range = 1000;
						break;
					case 22361 :
						msgId = 1;
						range = 5000;
						break;
					default :
						msgId = -1;
						range = 0;
				}
				if (msgId >= 0)
				{
					npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NPCSTRING_ID[msgId]));
				}
				npc.setBusy(true);
				npc.setBusyMessage("atk");

				final var amaskari = r.getParams().getObject("amaskari", Npc.class);
				if ((amaskari != null) && !amaskari.isDead() && (getRandom(1000) < 25) && Util.checkIfInRange(range, npc, amaskari, false))
				{
					r.addTimer("CALL_AMASKARI", ThreadPoolManager.getInstance().schedule(() -> callAmaskari(npc), 25000));
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			r.setParam("amaskariDead", true);
		}
		return super.onKill(npc, killer, isSummon);
	}

	private String checkConditions(Player player)
	{
		if (HellboundManager.getInstance().getLevel() < 10)
		{
			return "32346-lvl.htm";
		}
		return null;
	}

	private void callAmaskari(Npc npc)
	{
		if ((npc != null) && !npc.isDead())
		{
			final var r = npc.getReflection();
			if (isInReflection(r))
			{
				final var amaskari = r.getParams().getObject("amaskari", Npc.class);
				if ((amaskari != null) && !amaskari.isDead())
				{
					amaskari.teleToLocation(npc.getX(), npc.getY(), npc.getZ(), true, r);
					amaskari.broadcastPacketToOthers(2000, new NpcSay(amaskari.getObjectId(), Say2.NPC_ALL, amaskari.getId(), NpcStringId.ILL_MAKE_YOU_FEEL_SUFFERING_LIKE_A_FLAME_THAT_IS_NEVER_EXTINGUISHED));
				}
			}
		}
	}

	private void exitReflection(Party party, Reflection r)
	{
		if ((party != null) && (r != null))
		{
			for (final var partyMember : party.getMembers())
			{
				if ((partyMember != null) && !partyMember.isDead())
				{
					r.removeAllowed(partyMember);
					teleportPlayer(partyMember, new Location(16262, 283651, -9700), ReflectionManager.DEFAULT);
				}
			}
		}
	}

	void main()
	{
		new HellboundTown();
	}
}
