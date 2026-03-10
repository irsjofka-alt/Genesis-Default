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
import gameserver.data.parser.SkillsParser;
import gameserver.model.GameObject;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.skills.Skill;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.NpcSay;
import scripts.quests._196_SevenSignSealOfTheEmperor;

/**
 * Rework by LordWinter 16.11.2020
 */
public class SealOfTheEmperor extends AbstractReflection
{
	private static final NpcStringId[] ANAKIM_TEXT =
	{
	        NpcStringId.FOR_THE_ETERNITY_OF_EINHASAD, NpcStringId.DEAR_SHILLIENS_OFFSPRINGS_YOU_ARE_NOT_CAPABLE_OF_CONFRONTING_US, NpcStringId.ILL_SHOW_YOU_THE_REAL_POWER_OF_EINHASAD, NpcStringId.DEAR_MILITARY_FORCE_OF_LIGHT_GO_DESTROY_THE_OFFSPRINGS_OF_SHILLIEN
	};

	private static final NpcStringId[] LILITH_TEXT =
	{
	        NpcStringId.YOU_SUCH_A_FOOL_THE_VICTORY_OVER_THIS_WAR_BELONGS_TO_SHILIEN, NpcStringId.HOW_DARE_YOU_TRY_TO_CONTEND_AGAINST_ME_IN_STRENGTH_RIDICULOUS, NpcStringId.ANAKIM_IN_THE_NAME_OF_GREAT_SHILIEN_I_WILL_CUT_YOUR_THROAT, NpcStringId.YOU_CANNOT_BE_THE_MATCH_OF_LILITH_ILL_TEACH_YOU_A_LESSON
	};

	public SealOfTheEmperor()
	{
		super(112);

		addStartNpc(32585, 32657);
		addTalkId(32585, 32657);
		addSkillSeeId(27384);
		addSpawnId(32718, 32715, 27384);
		addAggroRangeEnterId(27371, 27372, 27373, 27377, 27378, 27379);
		addAttackId(27384, 32715, 32716, 32717, 32718, 32719, 32720, 32721);
		addKillId(27371, 27372, 27373, 27374, 27375, 27377, 27378, 27379, 27384);
	}

	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		final var r = player.getReflection();
		if (isInReflection(r))
		{
			npc.abortAttack();
			npc.setTarget(player);
			npc.setIsRunning(true);
			npc.getAI().setIntention(CtrlIntention.ATTACK, player);
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}

	private void runStartRoom(Reflection r)
	{
		r.setStatus(0);
		addSpawn(32586, -89456, 216184, -7504, 40960, false, 0, false, r);
		addSpawn(32587, -89400, 216125, -7504, 40960, false, 0, false, r);
		addSpawn(32657, -84385, 216117, -7497, 0, false, 0, false, r);
		addSpawn(32598, -84945, 220643, -7495, 0, false, 0, false, r);
		addSpawn(32598, -89563, 220647, -7491, 0, false, 0, false, r);
	}

	private void runFirstRoom(Reflection r)
	{
		final var npc = addSpawn(27371, -89049, 217979, -7495, 0, false, 0, false, r);
		r.setParam("PROGRESS", npc);
		addSpawn(27372, -89049, 217979, -7495, 0, false, 0, false, r);
		addSpawn(27373, -89049, 217979, -7495, 0, false, 0, false, r);
		addSpawn(27374, -89049, 217979, -7495, 0, false, 0, false, r);
		r.setStatus(1);
	}

	private void runSecondRoom(Reflection r)
	{
		final var npc = addSpawn(27371, -88599, 220071, -7495, 0, false, 0, false, r);
		r.setParam("PROGRESS", npc);
		addSpawn(27371, -88599, 220071, -7495, 0, false, 0, false, r);
		addSpawn(27372, -88599, 220071, -7495, 0, false, 0, false, r);
		addSpawn(27373, -88599, 220071, -7495, 0, false, 0, false, r);
		addSpawn(27373, -88599, 220071, -7495, 0, false, 0, false, r);
		addSpawn(27374, -88599, 220071, -7495, 0, false, 0, false, r);
		r.setStatus(2);
	}

	private void runThirdRoom(Reflection r)
	{
		final var npc = addSpawn(27371, -86846, 220639, -7495, 0, false, 0, false, r);
		r.setParam("PROGRESS", npc);
		addSpawn(27371, -86846, 220639, -7495, 0, false, 0, false, r);
		addSpawn(27372, -86846, 220639, -7495, 0, false, 0, false, r);
		addSpawn(27372, -86846, 220639, -7495, 0, false, 0, false, r);
		addSpawn(27373, -86846, 220639, -7495, 0, false, 0, false, r);
		addSpawn(27373, -86846, 220639, -7495, 0, false, 0, false, r);
		addSpawn(27374, -86846, 220639, -7495, 0, false, 0, false, r);
		addSpawn(27374, -86846, 220639, -7495, 0, false, 0, false, r);
		r.setStatus(3);
	}

	private void runForthRoom(Reflection r)
	{
		final var npc = addSpawn(27371, -85463, 219227, -7495, 0, false, 0, false, r);
		r.setParam("PROGRESS", npc);
		addSpawn(27372, -85463, 219227, -7495, 0, false, 0, false, r);
		addSpawn(27373, -85463, 219227, -7495, 0, false, 0, false, r);
		addSpawn(27374, -85463, 219227, -7495, 0, false, 0, false, r);
		addSpawn(27375, -85463, 219227, -7495, 0, false, 0, false, r);
		addSpawn(27377, -85463, 219227, -7495, 0, false, 0, false, r);
		addSpawn(27378, -85463, 219227, -7495, 0, false, 0, false, r);
		addSpawn(27379, -85463, 219227, -7495, 0, false, 0, false, r);
		r.setStatus(4);
	}

	private void runFifthRoom(Reflection r)
	{
		final var npc = addSpawn(27371, -87441, 217623, -7495, 0, false, 0, false, r);
		r.setParam("PROGRESS", npc);
		addSpawn(27372, -87441, 217623, -7495, 0, false, 0, false, r);
		addSpawn(27373, -87441, 217623, -7495, 0, false, 0, false, r);
		addSpawn(27374, -87441, 217623, -7495, 0, false, 0, false, r);
		addSpawn(27375, -87441, 217623, -7495, 0, false, 0, false, r);
		addSpawn(27375, -87441, 217623, -7495, 0, false, 0, false, r);
		addSpawn(27377, -87441, 217623, -7495, 0, false, 0, false, r);
		addSpawn(27377, -87441, 217623, -7495, 0, false, 0, false, r);
		addSpawn(27378, -87441, 217623, -7495, 0, false, 0, false, r);
		addSpawn(27378, -87441, 217623, -7495, 0, false, 0, false, r);
		addSpawn(27379, -87441, 217623, -7495, 0, false, 0, false, r);
		addSpawn(27379, -87441, 217623, -7495, 0, false, 0, false, r);
		r.setStatus(5);
	}

	private void runBossRoom(Reflection r)
	{
		if (r != null)
		{
			final var lilith = addSpawn(32715, -83175, 217021, -7504, 49151, false, 0, false, r);
			r.setParam("lilith", lilith);
			addSpawn(32716, -83222, 217055, -7504, 49151, false, 0, false, r);
			addSpawn(32717, -83127, 217056, -7504, 49151, false, 0, false, r);
			final var anakim = (Attackable) addSpawn(32718, -83179, 216479, -7504, 16384, false, 0, false, r);
			r.setParam("anakim", anakim);
			addSpawn(32719, -83227, 216443, -7504, 16384, false, 0, false, r);
			addSpawn(32720, -83179, 216432, -7504, 16384, false, 0, false, r);
			addSpawn(32721, -83134, 216443, -7504, 16384, false, 0, false, r);
			
			for (final var n : r.getAliveNpcs(32716, 32717, 32719, 32720, 32721))
			{
				n.setIsImmobilized(true);
			}
			
			addSpawn(27384, -83177, 217353, -7520, 32768, false, 0, false, r);
			addSpawn(27384, -83177, 216137, -7520, 32768, false, 0, false, r);
			addSpawn(27384, -82588, 216754, -7520, 32768, false, 0, false, r);
			addSpawn(27384, -83804, 216754, -7520, 32768, false, 0, false, r);
			addSpawn(32592, -83176, 216753, -7497, 0, false, 0, false, r);
			r.setStatus(6);
		}
	}

	private void runSDRoom(Reflection r)
	{
		final var npc1 = addSpawn(27384, -83177, 217353, -7520, 32768, false, 0, false, r);
		npc1.setIsNoRndWalk(true);
		npc1.setRHandId(15281);
		final var npc2 = addSpawn(27384, -83177, 216137, -7520, 32768, false, 0, false, r);
		npc2.setIsNoRndWalk(true);
		npc2.setRHandId(15281);
		final var npc3 = addSpawn(27384, -82588, 216754, -7520, 32768, false, 0, false, r);
		npc3.setIsNoRndWalk(true);
		npc3.setRHandId(15281);
		final var npc4 = addSpawn(27384, -83804, 216754, -7520, 32768, false, 0, false, r);
		npc4.setIsNoRndWalk(true);
		npc4.setRHandId(15281);
	}

	private boolean checkKillProgress(Reflection r, Npc npc)
	{
		if (r != null)
		{
			final var n = r.getParams().getObject("PROGRESS", Npc.class);
			return n != null && n.getObjectId() == npc.getObjectId();
		}
		return false;
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
	
	private synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 112))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				runStartRoom(r);
				runFirstRoom(r);
			}
		}
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final int npcId = npc.getId();
		if ((npcId == 32715) || (npcId == 32716) || (npcId == 32717))
		{
			npc.setCurrentHp(npc.getCurrentHp() + damage);
			((Attackable) npc).getAggroList().stopHating(attacker);
		}

		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (r.isStatus(6) && (npc.getId() == 27384))
			{
				npc.doCast(SkillsParser.getInstance().getInfo(5980, 3));
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}

	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if ((skill.getId() == 8357) && (r.isStatus(6)) && (npc.getId() == 27384))
			{
				npc.doCast(SkillsParser.getInstance().getInfo(5980, 3));
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final var r = player.getReflection();
		if (isInReflection(r))
		{
			if (event.equalsIgnoreCase("DOORS"))
			{
				r.openDoor(17240111);
				for (final var pl : r.getReflectionPlayers())
				{
					pl.showQuestMovie(12);
				}
				r.addTimer("SPAWN_RAID", ThreadPoolManager.getInstance().schedule(() -> runBossRoom(r), 22000));
				startQuestTimer("lilith_text", 26000, npc, player);
				startQuestTimer("anakim_text", 26000, npc, player);
				startQuestTimer("go_fight", 25000, npc, player);
			}
			else if (event.equalsIgnoreCase("anakim_text"))
			{
				final var anakim = r.getParams().getObject("anakim", Npc.class);
				if (anakim != null)
				{
					final var ns = new NpcSay(anakim.getObjectId(), 0, anakim.getId(), ANAKIM_TEXT[getRandom(ANAKIM_TEXT.length)]);
					player.sendPacket(ns);
					startQuestTimer("anakim_text", 20000, npc, player);
				}
			}
			else if (event.equalsIgnoreCase("lilith_text"))
			{
				final var lilith = r.getParams().getObject("lilith", Npc.class);
				if (lilith != null)
				{
					final var ns = new NpcSay(lilith.getObjectId(), 0, lilith.getId(), LILITH_TEXT[getRandom(LILITH_TEXT.length)]);
					player.sendPacket(ns);
					startQuestTimer("lilith_text", 22000, npc, player);
				}
			}
			else if (event.equalsIgnoreCase("go_fight"))
			{
				for (final var n : r.getAliveNpcs(32716, 32717, 32719, 32720, 32721))
				{
					n.setIsImmobilized(false);
				}
			}
			else if (event.equalsIgnoreCase("Delete"))
			{
				for (final var n : r.getAliveNpcs(32715, 32716, 32717, 32718, 32719, 32720, 32721))
				{
					n.deleteMe();
				}
				r.setParam("lilith", null);
				r.setParam("anakim", null);
			}
			else if (event.equalsIgnoreCase("Tele"))
			{
				player.teleToLocation(-89528, 216056, -7516, true, player.getReflection());
			}
		}
		return null;
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		final String htmltext = getNoQuestMsg(player);
		final var st = player.getQuestState(_196_SevenSignSealOfTheEmperor.class.getSimpleName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (npc.getId())
		{
			case 32585 :
				if (st.isCond(3) || st.isCond(4))
				{
					enterInstance(player, npc);
					return null;
				}
				break;
		}
		return htmltext;
	}

	@Override
	public final String onSpawn(Npc npc)
	{
		if (npc.getId() == 32718 || npc.getId() == 32715 || npc.getId() == 27384)
		{
			npc.setIsNoRndWalk(true);
			npc.setIsImmobilized(true);
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var st = player.getQuestState(_196_SevenSignSealOfTheEmperor.class.getSimpleName());
		if (st == null)
		{
			return null;
		}
		
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (r.isStatus(1))
			{
				if (checkKillProgress(r, npc))
				{
					runSecondRoom(r);
					r.openDoor(17240102);
				}
			}
			else if (r.isStatus(2))
			{
				if (checkKillProgress(r, npc))
				{
					runThirdRoom(r);
					r.openDoor(17240104);
				}
			}
			else if (r.isStatus(3))
			{
				if (checkKillProgress(r, npc))
				{
					runForthRoom(r);
					r.openDoor(17240106);
				}
			}
			else if (r.isStatus(4))
			{
				if (checkKillProgress(r, npc))
				{
					runFifthRoom(r);
					r.openDoor(17240108);
				}
			}
			else if (r.isStatus(5))
			{
				if (checkKillProgress(r, npc))
				{
					r.openDoor(17240110);
				}
			}
			else if (r.isStatus(6))
			{
				if (npc.getId() == 27384)
				{
					if (st.getQuestItemsCount(13846) < 3)
					{
						npc.setRHandId(15281);
						st.playSound("ItemSound.quest_itemget");
						st.giveItems(13846, 1);
					}
					else
					{
						npc.setRHandId(15281);
						giveItems(player, 13846, 1);
						st.playSound("ItemSound.quest_middle");
						runSDRoom(r);
						player.showQuestMovie(13);
						startQuestTimer("Tele", 26000, null, player);
						startQuestTimer("Delete", 26000, null, player);
					}
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new SealOfTheEmperor();
	}
}