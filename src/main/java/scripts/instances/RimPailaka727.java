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

import java.util.HashMap;
import java.util.Map;

import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.FortManager;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.holders.SkillHolder;
import gameserver.network.NpcStringId;
import gameserver.network.SystemMessageId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter 17.10.2020
 */
public final class RimPailaka727 extends AbstractReflection
{
	private final Map<Integer, Integer> _castleReflections = new HashMap<>(9);
	
	private RimPailaka727()
	{
		super(100, 101, 102, 103, 104, 105, 106, 107, 108, 100);

		_castleReflections.put(36403, 101);
		_castleReflections.put(36404, 102);
		_castleReflections.put(36405, 103);
		_castleReflections.put(36406, 104);
		_castleReflections.put(36407, 105);
		_castleReflections.put(36408, 106);
		_castleReflections.put(36409, 107);
		_castleReflections.put(36410, 108);
		_castleReflections.put(36411, 109);
		
		for (final int i : _castleReflections.keySet())
		{
			addStartNpc(i);
			addTalkId(i);
		}
		
		addStartNpc(36562, 36563, 36564, 36565);
		addTalkId(36562, 36563, 36564, 36565);
		addFirstTalkId(36562, 36563, 36564, 36565);
		
		addSpawnId(25653, 25654, 25655, 36562, 36563, 36564, 36565);
		addKillId(25653, 25654, 25655, 25656, 25657, 25658, 36562, 36563, 36564, 36565);
		addAttackId(25653, 25654, 25655, 25656, 25657, 25658, 36562, 36563, 36564, 36565);
	}
	
	private synchronized void enterInstance(Player player, Npc npc, int reflectionId)
	{
		if (enterReflection(player, npc, reflectionId))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				final long delay = r.getParams().getLong("firstWaveDelay");
				r.setParam("underAttack", false);
				r.addTimer("FIRST_STAGE", ThreadPoolManager.getInstance().schedule(() -> firstStage(r), delay));
			}
		}
	}
	
	@Override
	protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var castle = npc.getCastle();
		final boolean checkConds = template.getParams().getBool("checkContractConditions");
		final var html = new NpcHtmlMessage(npc.getObjectId());
		if ((player == null) || (castle == null))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/CastleWarden-03.htm");
			player.sendPacket(html);
			return false;
		}
		
		if (checkConds)
		{
			boolean haveContract = false;
			for (final var fort : FortManager.getInstance().getForts())
			{
				if (fort.getContractedCastleId() == castle.getId())
				{
					haveContract = true;
					break;
				}
			}
			
			if (!haveContract)
			{
				html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/CastleWarden-11.htm");
				player.sendPacket(html);
				return false;
			}
		}
		
		final var st = player.getQuestState("_727_HopeWithinTheDarkness");
		if ((st == null) || (st.getCond() < 1))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/CastleWarden-07.htm");
			player.sendPacket(html);
			return false;
		}
		
		if ((player.getClan() == null) || (player.getClan().getCastleId() != castle.getId()))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/CastleWarden-08.htm");
			player.sendPacket(html);
			return false;
		}
		return super.checkSoloType(player, npc, template);
	}
	
	@Override
	protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var castle = npc.getCastle();
		final boolean checkConds = template.getParams().getBool("checkContractConditions");
		final var html = new NpcHtmlMessage(npc.getObjectId());
		
		if ((player == null) || (castle == null))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/CastleWarden-03.htm");
			player.sendPacket(html);
			return false;
		}
		
		final var party = player.getParty();
		if (party == null || party.getMemberCount() < 2)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		
		if (checkConds)
		{
			boolean haveContract = false;
			for (final var fort : FortManager.getInstance().getForts())
			{
				if (fort.getContractedCastleId() == castle.getId())
				{
					haveContract = true;
					break;
				}
			}
			
			if (!haveContract)
			{
				html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/CastleWarden-11.htm");
				player.sendPacket(html);
				return false;
			}
		}
		
		for (final var partyMember : party.getMembers())
		{
			if (partyMember != null)
			{
				final var st = partyMember.getQuestState("_727_HopeWithinTheDarkness");
				if ((st == null) || (st.getCond() < 1))
				{
					html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/CastleWarden-07.htm");
					player.sendPacket(html);
					return false;
				}
				
				if ((partyMember.getClan() == null) || (partyMember.getClan().getCastleId() != castle.getId()))
				{
					html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/CastleWarden-08.htm");
					player.sendPacket(html);
					return false;
				}
			}
		}
		return super.checkPartyType(player, npc, template);
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
	public String onFirstTalk(Npc npc, Player player)
	{
		final var r = player.getReflection();
		if (isInReflection(r))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			final var underAttack = r.getParams().getBool("underAttack", false);
			if (underAttack)
			{
				html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/Victim-02.htm");
			}
			else if (r.isStatus(4))
			{
				html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/Victim-03.htm");
			}
			else
			{
				html.setFile(player, player.getLang(), "data/html/scripts/quests/_727_HopeWithinTheDarkness/Victim-01.htm");
			}
			player.sendPacket(html);
		}
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player, npc, _castleReflections.get(npc.getId()));
		}
		else if (event.equalsIgnoreCase("leave"))
		{
			if ((npc.getId() >= 36562) && (npc.getId() <= 36565))
			{
				final var r = player.getReflection();
				if (isInReflection(r))
				{
					r.removeAllowed(player);
					player.teleToLocation(r.getReturnLoc(), true, ReflectionManager.DEFAULT);
					if (r.getPlayers().isEmpty())
					{
						r.collapse();
					}
					return null;
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player player, int damage, boolean isSummon)
	{
		if ((npc.getId() >= 36562) && (npc.getId() <= 36565))
		{
			if (npc.getCurrentHp() <= (npc.getMaxHp() * 0.1))
			{
				npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.YOUR_MIND_IS_GOING_BLANK));
			}
			else if (npc.getCurrentHp() <= (npc.getMaxHp() * 0.4))
			{
				npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.YOUR_MIND_IS_GOING_BLANK));
			}
			return null;
		}
		
		if (player != null)
		{
			final var attacker = (isSummon ? player.getSummon() : player);
			if ((attacker.getLevel() - npc.getLevel()) >= 9)
			{
				if ((attacker.getBuffCount() > 0) || (attacker.getDanceCount() > 0))
				{
					npc.setTarget(attacker);
					npc.doSimultaneousCast(new SkillHolder(5456, 1).getSkill());
				}
				else if (player.getParty() != null)
				{
					for (final Player pmember : player.getParty().getMembers())
					{
						if ((pmember.getBuffCount() > 0) || (pmember.getDanceCount() > 0))
						{
							npc.setTarget(pmember);
							npc.doSimultaneousCast(new SkillHolder(5456, 1).getSkill());
						}
					}
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public final String onSpawn(Npc npc)
	{
		if (npc.getId() == 36562 || npc.getId() == 36563 || npc.getId() == 36564 || npc.getId() == 36565)
		{
			npc.setIsHasNoChatWindow(false);
			if (npc.getId() == 36562)
			{
				npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.WARRIORS_HAVE_YOU_COME_TO_HELP_THOSE_WHO_ARE_IMPRISONED_HERE));
			}
		}
		else if (npc.getId() == 25653)
		{
			npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.ILL_RIP_THE_FLESH_FROM_YOUR_BONES));
		}
		else if (npc.getId() == 25654)
		{
			npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.ILL_RIP_THE_FLESH_FROM_YOUR_BONES));
		}
		else if (npc.getId() == 25655)
		{
			npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.YOULL_FLOUNDER_IN_DELUSION_FOR_THE_REST_OF_YOUR_LIFE));
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (npc.getId() == 25653 || npc.getId() == 25654 || npc.getId() == 25655)
			{
				npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.HOW_DARE_YOU));
			}
	
			switch (npc.getId())
			{
				case 36562 :
				case 36563 :
				case 36564 :
				case 36565 :
					r.setStatus(5);
					r.stopAllTimers();
					if (npc.getId() == 36562)
					{
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.I_CANT_STAND_IT_ANYMORE_AAH));
					}
					else if (npc.getId() == 36563)
					{
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.I_CANT_STAND_IT_ANYMORE_AAH));
					}
					else if (npc.getId() == 36564)
					{
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.KYAAAK));
					}
					else if (npc.getId() == 36565)
					{
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.GASP_HOW_CAN_THIS_BE));
					}
					
					for (final Npc npcs : r.getNpcs())
					{
						if (npcs.getId() >= 36562 && npcs.getId() <= 36565)
						{
							if (!npcs.isDead())
							{
								npcs.doDie(null);
							}
						}
					}
					finishInstance(r, false);
					break;
				case 25655 :
				case 25657 :
				case 25658 :
					if (r.isStatus(3))
					{
						if (checkAliveNpc(r))
						{
							r.incStatus();
							r.setParam("underAttack", false);
							finishInstance(r, false);
							
							for (final Npc knight : r.getNpcs())
							{
								if (knight.getId() == 36562)
								{
									knight.broadcastPacketToOthers(2000, new NpcSay(knight.getObjectId(), Say2.SHOUT, knight.getId(), NpcStringId.YOUVE_DONE_IT_WE_BELIEVED_IN_YOU_WARRIOR_WE_WANT_TO_SHOW_OUR_SINCERITY_THOUGH_IT_IS_SMALL_PLEASE_GIVE_ME_SOME_OF_YOUR_TIME));
								}
							}
							
							if (player != null)
							{
								final var party = player.getParty();
								if (party == null || party.getMemberCount() < 2)
								{
									final var st = player.getQuestState("_727_HopeWithinTheDarkness");
									if ((st != null) && (st.isCond(1)))
									{
										st.setCond(2, true);
									}
								}
								else
								{
									for (final var m : party.getMembers())
									{
										if ((m != null) && (m.getReflectionId() == player.getReflectionId()))
										{
											final var st = m.getQuestState("_727_HopeWithinTheDarkness");
											if ((st != null) && (st.isCond(1)))
											{
												st.setCond(2, true);
											}
										}
									}
								}
							}
						}
					}
					break;
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	private boolean checkAliveNpc(Reflection r)
	{
		boolean check = false;
		if (r != null && r.isStatus(3))
		{
			check = true;
			for (final Npc npc : r.getNpcs())
			{
				if (npc.getId() == 36562 || npc.getId() == 36563 || npc.getId() == 36564 || npc.getId() == 36565)
				{
					continue;
				}
				
				if (!npc.isDead())
				{
					check = false;
					break;
				}
			}
		}
		return check;
	}
	
	private void firstStage(Reflection r)
	{
		if (r != null)
		{
			addSpawn(36565, 49093, -12077, -9395, 0, false, 0, false, r);
			addSpawn(36563, 49094, -12238, -9386, 0, false, 0, false, r);
			addSpawn(36564, 49093, -12401, -9388, 0, false, 0, false, r);
			addSpawn(36562, 49232, -12239, -9386, 0, false, 0, false, r);
			addSpawn(25653, 50943, -12224, -9321, 32768, false, 0, false, r);
			r.incStatus();
			final long stageDelay = r.getParams().getLong("secondWaveDelay");
			final long spawnDelay = r.getParams().getLong("firstSpawnDelay");
			r.addTimer("FIRST_COMMON", ThreadPoolManager.getInstance().schedule(() -> firstCommonSpawn(r), spawnDelay));
			r.addTimer("SECOND_STAGE", ThreadPoolManager.getInstance().schedule(() -> secondStage(r), stageDelay));
		}
	}
	
	private void firstCommonSpawn(Reflection r)
	{
		if (r != null)
		{
			addSpawn(25656, 50343, -12552, -9388, 32768, false, 0, false, r);
			addSpawn(25656, 50344, -12340, -9380, 32768, false, 0, false, r);
			addSpawn(25656, 50341, -12134, -9381, 32768, false, 0, false, r);
			addSpawn(25656, 50342, -11917, -9389, 32768, false, 0, false, r);
			addSpawn(25656, 50476, -12461, -9392, 32768, false, 0, false, r);
			addSpawn(25656, 50481, -12021, -9390, 32768, false, 0, false, r);
			addSpawn(25656, 50605, -12407, -9392, 32768, false, 0, false, r);
			addSpawn(25656, 50602, -12239, -9380, 32768, false, 0, false, r);
			addSpawn(25656, 50606, -12054, -9390, 32768, false, 0, false, r);
			r.setParam("underAttack", true);
		}
	}
	
	private void secondStage(Reflection r)
	{
		if (r != null)
		{
			addSpawn(25654, 50943, -12224, -9321, 32768, false, 0, false, r);
			r.incStatus();
			final long stageDelay = r.getParams().getLong("thirdWaveDelay");
			final long spawnDelay = r.getParams().getLong("secondSpawnDelay");
			r.addTimer("SECOND_COMMON", ThreadPoolManager.getInstance().schedule(() -> secondCommonSpawn(r), spawnDelay));
			r.addTimer("THIRD_STAGE", ThreadPoolManager.getInstance().schedule(() -> thirdStage(r), stageDelay));
		}
	}
	
	private void secondCommonSpawn(Reflection r)
	{
		if (r != null)
		{
			addSpawn(25657, 50343, -12552, -9388, 32768, false, 0, false, r);
			addSpawn(25657, 50344, -12340, -9380, 32768, false, 0, false, r);
			addSpawn(25657, 50341, -12134, -9381, 32768, false, 0, false, r);
			addSpawn(25657, 50342, -11917, -9389, 32768, false, 0, false, r);
			addSpawn(25657, 50476, -12461, -9392, 32768, false, 0, false, r);
			addSpawn(25657, 50481, -12021, -9390, 32768, false, 0, false, r);
			addSpawn(25657, 50605, -12407, -9392, 32768, false, 0, false, r);
			addSpawn(25657, 50602, -12239, -9380, 32768, false, 0, false, r);
			addSpawn(25657, 50606, -12054, -9390, 32768, false, 0, false, r);
		}
	}
	
	private void thirdStage(Reflection r)
	{
		if (r != null)
		{
			addSpawn(25655, 50943, -12004, -9321, 32768, false, 0, false, r);
			addSpawn(25655, 50943, -12475, -9321, 32768, false, 0, false, r);
			r.incStatus();
			final long spawnDelay = r.getParams().getLong("thirdSpawnDelay");
			r.addTimer("THIRD_COMMON", ThreadPoolManager.getInstance().schedule(() -> thirdCommonSpawn(r), spawnDelay));
		}
	}
	
	private void thirdCommonSpawn(Reflection r)
	{
		if (r != null)
		{
			addSpawn(25657, 50343, -12552, -9388, 32768, false, 0, false, r);
			addSpawn(25657, 50344, -12340, -9380, 32768, false, 0, false, r);
			addSpawn(25657, 50341, -12134, -9381, 32768, false, 0, false, r);
			addSpawn(25657, 50342, -11917, -9389, 32768, false, 0, false, r);
			addSpawn(25658, 50476, -12461, -9392, 32768, false, 0, false, r);
			addSpawn(25658, 50481, -12021, -9390, 32768, false, 0, false, r);
			addSpawn(25658, 50605, -12407, -9392, 32768, false, 0, false, r);
			addSpawn(25658, 50602, -12239, -9380, 32768, false, 0, false, r);
			addSpawn(25658, 50606, -12054, -9390, 32768, false, 0, false, r);
		}
	}

	public static void main(String[] args)
	{
		new RimPailaka727();
	}
}
