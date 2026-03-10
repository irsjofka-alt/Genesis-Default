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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import gameserver.data.parser.ReflectionParser;
import gameserver.instancemanager.ReflectionManager;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.actor.templates.reflection.ReflectionTemplate.ReflectionQuestType;
import gameserver.model.actor.templates.reflection.ReflectionTemplate.ReflectionRemoveType;
import gameserver.model.entity.Reflection;
import gameserver.model.strings.server.ServerMessage;
import gameserver.model.zone.type.ReflectionZone;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExPCCafePointInfo;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.network.serverpackets.updatetype.UserInfoType;
import gameserver.utils.Util;
import scripts.ai.AbstractNpcAI;

/**
 * Update by LordWinter 07.08.2025
 */
public abstract class AbstractReflection extends AbstractNpcAI
{
	private final Set<Integer> _templateIds = new HashSet<>();
	
	public AbstractReflection(int... templateIds)
	{
		for (final int templateId : templateIds)
		{
			_templateIds.add(templateId);
		}
	}
	
	public Set<Integer> getTemplateId()
	{
		return _templateIds;
	}
	
	public boolean isInReflection(Reflection r)
	{
		return r != null && !r.isDefault() && _templateIds.contains(r.getTemplateId());
	}
	
	protected abstract void onTeleportEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance);
	
	protected boolean enterGlobalInstance(Player player, Npc npc, int reflectionId, int refTemplate)
	{
		final var template = ReflectionParser.getInstance().getReflectionId(refTemplate);
		if (template != null)
		{
			var r = ReflectionManager.getInstance().getPlayerReflection(player.getObjectId(), false);
			if (r != null)
			{
				if (r.getTemplateId() == refTemplate)
				{
					if (checkReenterConditions(player, npc, template))
					{
						onTeleportEnter(player, template, r, false);
						if (template.isDispelBuffs())
						{
							handleRemoveBuffs(player);
						}
					}
					return false;
				}
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return false;
			}
			
			if (checkConditions(player, npc, template))
			{
				r = ReflectionManager.getInstance().getReflection(reflectionId);
				if (r != null)
				{
					switch (template.getEntryType())
					{
						case PARTY_COMMAND_CHANNEL :
						case COMMAND_CHANNEL :
							if (player.getParty() != null && player.getParty().getCommandChannel() != null)
							{
								player.getParty().getCommandChannel().setReflection(r);
							}
							break;
					}
					onEnterInstance(player, template, r, true);
					
					if (template.getReuseUponEntry())
					{
						handleReenterTime(r);
					}
					
					if (template.isDispelBuffs())
					{
						handleRemoveBuffs(r);
					}
					
					if (r.getReturnLoc() == null)
					{
						r.setReturnLoc(player.getLocation());
					}
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean enterReflection(Player player, Npc npc, int reflectionId)
	{
		final var template = ReflectionParser.getInstance().getReflectionId(reflectionId);
		if (template != null)
		{
			final var r = ReflectionManager.getInstance().getPlayerReflection(player.getObjectId(), false);
			if (r != null)
			{
				if (r.getTemplateId() == reflectionId)
				{
					if (checkReenterConditions(player, npc, template))
					{
						onTeleportEnter(player, template, r, false);
						if (template.isDispelBuffs())
						{
							handleRemoveBuffs(player);
						}
					}
					return false;
				}
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return false;
			}
		
			if (checkConditions(player, npc, template))
			{
				final var ref = ReflectionManager.getInstance().createDynamicReflection(template);
				ref.setStatus(0);
				ReflectionManager.getInstance().addReflection(ref);
				if (ref.getReturnLoc() == null)
				{
					ref.setReturnLoc(player.getLocation());
				}
				
				switch (template.getEntryType())
				{
					case PARTY_COMMAND_CHANNEL :
					case COMMAND_CHANNEL :
						if (player.getParty() != null && player.getParty().getCommandChannel() != null)
						{
							player.getParty().getCommandChannel().setReflection(r);
						}
						break;
				}
				onEnterInstance(player, template, ref, true);
				
				if (template.getReuseUponEntry())
				{
					handleReenterTime(ref);
				}
				
				if (template.isDispelBuffs())
				{
					handleRemoveBuffs(ref);
				}
				return true;
			}
		}
		return false;
	}
	
	protected void handleRemoveBuffs(Reflection r)
	{
		for (final var player : r.getReflectionPlayers())
		{
			handleRemoveBuffs(player);
		}
	}
	
	protected void onEnterInstance(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		switch (template.getEntryType())
		{
			case SOLO :
				doSoloEnter(player, template, r, firstEntrance);
				break;
			case SOLO_PARTY :
				if (player.getParty() == null)
				{
					doSoloEnter(player, template, r, firstEntrance);
				}
				else
				{
					doPartyEnter(player, template, r, firstEntrance);
				}
				break;
			case PARTY :
				doPartyEnter(player, template, r, firstEntrance);
				break;
			case PARTY_COMMAND_CHANNEL :
				if (player.getParty() != null && player.getParty().getCommandChannel() != null)
				{
					doCommandChannelEnter(player, template, r, firstEntrance);
				}
				else
				{
					doPartyEnter(player, template, r, firstEntrance);
				}
				break;
			case COMMAND_CHANNEL :
				doCommandChannelEnter(player, template, r, firstEntrance);
				break;
		}
	}
	
	private void doSoloEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		if (!template.getRequestItems().isEmpty())
		{
			for (final var tpl : template.getRequestItems())
			{
				if (tpl.isNecessary())
				{
					if (tpl.getId() == -100)
					{
						player.setPcBangPoints((int) (player.getPcBangPoints() - tpl.getCount()));
						final var smsg = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
						smsg.addNumber((int) tpl.getCount());
						player.sendPacket(smsg);
						player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), (int) tpl.getCount(), false, false, 1));
					}
					else if (tpl.getId() == -200)
					{
						player.getClan().takeReputationScore((int) tpl.getCount(), true);
						final var smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						smsg.addItemNumber(tpl.getCount());
						player.sendPacket(smsg);
					}
					else if (tpl.getId() == -300)
					{
						player.setFame((int) (player.getFame() - tpl.getCount()));
						player.sendUserInfo(UserInfoType.VITA_FAME);
					}
					else if (tpl.getId() > 0)
					{
						player.destroyItemByItemId("Instance Check", tpl.getId(), tpl.getCount(), player, true);
					}
				}
			}
		}
		
		if (!template.getRewardItems().isEmpty())
		{
			for (final var tpl : template.getRewardItems())
			{
				player.addItem("Instance reward", tpl.getId(), tpl.getCount(), null, true);
			}
		}
		onTeleportEnter(player, template, r, firstEntrance);
	}
	
	private void doPartyEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		if (!template.getRequestItems().isEmpty())
		{
			for (final var tpl : template.getRequestItems())
			{
				if (tpl.isNecessary() && tpl.getType() == ReflectionRemoveType.LEADER)
				{
					if (tpl.getId() == -100)
					{
						player.setPcBangPoints((int) (player.getPcBangPoints() - tpl.getCount()));
						final var smsg = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
						smsg.addNumber((int) tpl.getCount());
						player.sendPacket(smsg);
						player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), (int) tpl.getCount(), false, false, 1));
					}
					else if (tpl.getId() == -200)
					{
						player.getClan().takeReputationScore((int) tpl.getCount(), true);
						final var smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						smsg.addItemNumber(tpl.getCount());
						player.sendPacket(smsg);
					}
					else if (tpl.getId() == -300)
					{
						player.setFame((int) (player.getFame() - tpl.getCount()));
						player.sendUserInfo(UserInfoType.VITA_FAME);
					}
					else if (tpl.getId() > 0)
					{
						player.destroyItemByItemId("Instance Check", tpl.getId(), tpl.getCount(), player, true);
					}
				}
			}
		}
		
		for (final var partyMember : player.getParty().getMembers())
		{
			if (partyMember != null)
			{
				if (!template.getRequestItems().isEmpty())
				{
					for (final var tpl : template.getRequestItems())
					{
						if (tpl.isNecessary() && tpl.getType() == ReflectionRemoveType.ALL)
						{
							if (tpl.getId() == -100)
							{
								partyMember.setPcBangPoints((int) (partyMember.getPcBangPoints() - tpl.getCount()));
								final var smsg = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
								smsg.addNumber((int) tpl.getCount());
								partyMember.sendPacket(smsg);
								partyMember.sendPacket(new ExPCCafePointInfo(partyMember.getPcBangPoints(), (int) tpl.getCount(), false, false, 1));
							}
							else if (tpl.getId() == -200)
							{
								partyMember.getClan().takeReputationScore((int) tpl.getCount(), true);
								final var smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
								smsg.addItemNumber(tpl.getCount());
								partyMember.sendPacket(smsg);
							}
							else if (tpl.getId() == -300)
							{
								partyMember.setFame((int) (partyMember.getFame() - tpl.getCount()));
								partyMember.sendUserInfo(UserInfoType.VITA_FAME);
							}
							else if (tpl.getId() > 0)
							{
								partyMember.destroyItemByItemId("Instance Check", tpl.getId(), tpl.getCount(), partyMember, true);
							}
						}
					}
				}
				
				if (!template.getRewardItems().isEmpty())
				{
					for (final var tpl : template.getRewardItems())
					{
						partyMember.addItem("Instance reward", tpl.getId(), tpl.getCount(), null, true);
					}
				}
				onTeleportEnter(partyMember, template, r, firstEntrance);
			}
		}
	}
	
	private void doCommandChannelEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		if (!template.getRequestItems().isEmpty())
		{
			for (final var tpl : template.getRequestItems())
			{
				if (tpl.isNecessary() && tpl.getType() == ReflectionRemoveType.LEADER)
				{
					if (tpl.getId() == -100)
					{
						player.setPcBangPoints((int) (player.getPcBangPoints() - tpl.getCount()));
						final var smsg = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
						smsg.addNumber((int) tpl.getCount());
						player.sendPacket(smsg);
						player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), (int) tpl.getCount(), false, false, 1));
					}
					else if (tpl.getId() == -200)
					{
						player.getClan().takeReputationScore((int) tpl.getCount(), true);
						final var smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						smsg.addItemNumber(tpl.getCount());
						player.sendPacket(smsg);
					}
					else if (tpl.getId() == -300)
					{
						player.setFame((int) (player.getFame() - tpl.getCount()));
						player.sendUserInfo(UserInfoType.VITA_FAME);
					}
					else if (tpl.getId() > 0)
					{
						player.destroyItemByItemId("Instance Check", tpl.getId(), tpl.getCount(), player, true);
					}
				}
			}
		}
		
		for (final var ccMember : player.getParty().getCommandChannel().getMembers())
		{
			if (ccMember != null)
			{
				if (!template.getRequestItems().isEmpty())
				{
					for (final var tpl : template.getRequestItems())
					{
						if (tpl.isNecessary() && tpl.getType() == ReflectionRemoveType.ALL)
						{
							if (tpl.getId() == -100)
							{
								ccMember.setPcBangPoints((int) (ccMember.getPcBangPoints() - tpl.getCount()));
								final var smsg = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
								smsg.addNumber((int) tpl.getCount());
								ccMember.sendPacket(smsg);
								ccMember.sendPacket(new ExPCCafePointInfo(ccMember.getPcBangPoints(), (int) tpl.getCount(), false, false, 1));
							}
							else if (tpl.getId() == -200)
							{
								ccMember.getClan().takeReputationScore((int) tpl.getCount(), true);
								final var smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
								smsg.addItemNumber(tpl.getCount());
								ccMember.sendPacket(smsg);
							}
							else if (tpl.getId() == -300)
							{
								ccMember.setFame((int) (ccMember.getFame() - tpl.getCount()));
								ccMember.sendUserInfo(UserInfoType.VITA_FAME);
							}
							else if (tpl.getId() > 0)
							{
								ccMember.destroyItemByItemId("Instance Check", tpl.getId(), tpl.getCount(), ccMember, true);
							}
						}
					}
				}
				
				if (!template.getRewardItems().isEmpty())
				{
					for (final var tpl : template.getRewardItems())
					{
						ccMember.addItem("Instance reward", tpl.getId(), tpl.getCount(), null, true);
					}
				}
				onTeleportEnter(ccMember, template, r, firstEntrance);
			}
		}
	}
	
	protected boolean checkReenterConditions(Player player, Npc npc, ReflectionTemplate template)
	{
		if (player.isCursedWeaponEquipped() || player.isFlying())
		{
			player.sendPacket(SystemMessageId.CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS);
			return false;
		}
		
		switch (template.getEntryType())
		{
			case SOLO_PARTY :
				if (player.getParty() == null)
				{
					return true;
				}
				return checkReenterPartyType(player, npc, template);
			case PARTY :
				return checkReenterPartyType(player, npc, template);
			case PARTY_COMMAND_CHANNEL :
				if (player.getParty() != null && player.getParty().getCommandChannel() != null)
				{
					return checkReenterCommandChannelType(player, npc, template);
				}
				return checkReenterPartyType(player, npc, template);
			case COMMAND_CHANNEL :
				return checkReenterCommandChannelType(player, npc, template);
		}
		return true;
	}
	
	protected boolean checkConditions(Player player, Npc npc, ReflectionTemplate template)
	{
		if (ReflectionManager.getInstance().getCountByIzId(template.getId()) >= template.getMaxChannels())
		{
			player.sendPacket(SystemMessageId.MAXIMUM_INSTANCE_ZONE_NUMBER_EXCEEDED_CANT_ENTER);
			return false;
		}
		
		if (player.isCursedWeaponEquipped() || player.isFlying())
		{
			player.sendPacket(SystemMessageId.CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS);
			return false;
		}
		
		switch (template.getEntryType())
		{
			case SOLO :
				return checkSoloType(player, npc, template);
			case SOLO_PARTY :
				if (player.getParty() == null)
				{
					return checkSoloType(player, npc, template);
				}
				return checkPartyType(player, npc, template);
			case PARTY :
				return checkPartyType(player, npc, template);
			case PARTY_COMMAND_CHANNEL :
				if (player.getParty() != null && player.getParty().getCommandChannel() != null)
				{
					return checkCommandChannelType(player, npc, template);
				}
				return checkPartyType(player, npc, template);
			case COMMAND_CHANNEL :
				return checkCommandChannelType(player, npc, template);
		}
		return true;
	}
	
	protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template)
	{
		if (!isNotCheckTimeRef(template.getId()) && System.currentTimeMillis() < ReflectionParser.getInstance().getMinutesToNextEntrance(template.getId(), player))
		{
			final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		
		if (player.getLevel() < template.getMinLevel() || player.getLevel() > template.getMaxLevel())
		{
			final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		
		if (player.isInParty())
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		
		if (!template.getSkills().isEmpty())
		{
			final var checkAllSkills = template.checkAllSkills();
			var found = false;
			for (final var sk : template.getSkills())
			{
				final var skill = player.getKnownSkill(sk.getId());
				if (!checkAllSkills)
				{
    				if (skill != null && skill.getLevel() >= sk.getLvl())
    				{
    					found = true;
    					break;
    				}
				}
				else
				{
    				if (skill == null || skill.getLevel() < sk.getLvl())
    				{
    					final var msg = new ServerMessage("Reflection.LOW_SKILL", player.getLang());
    					msg.add(sk.getSkill().getName(player.getLang()));
    					player.sendMessage(msg.toString());
    					return false;
    				}
				}
			}
				
			if (!checkAllSkills && !found)
			{
				player.sendMessage(new ServerMessage("Reflection.LOW_SKILLS", player.getLang()).toString());
				return false;
			}
		}
		
		if (template.getRebirth() > 0 && (player.getVarInt("rebirth", 0) < template.getRebirth()))
		{
			final var msg = new ServerMessage("Reflection.LOW_REBIRTH", player.getLang());
			msg.add(template.getRebirth());
			player.sendMessage(msg.toString());
			return false;
		}
		
		if (template.isForNoble() && !player.isNoble())
		{
			player.sendMessage(new ServerMessage("Reflection.FOR_NOBLE", player.getLang()).toString());
			return false;
		}
		
		if (template.isForHero() && !player.isHero())
		{
			player.sendMessage(new ServerMessage("Reflection.FOR_HERO", player.getLang()).toString());
			return false;
		}
		
		if (template.isHasCastle() || template.isHasFort())
		{
			final var clan = player.getClan();
			if (template.isHasCastle() && (clan == null || clan.getCastleId() <= 0))
			{
				player.sendMessage(new ServerMessage("Reflection.HAS_CASTLE", player.getLang()).toString());
				return false;
			}
			
			if (template.isHasFort() && (clan == null || clan.getFortId() <= 0))
			{
				player.sendMessage(new ServerMessage("Reflection.HAS_FORT", player.getLang()).toString());
				return false;
			}
		}
		
		if (template.isForPremium() && !player.hasPremiumBonus())
		{
			player.sendMessage(new ServerMessage("Reflection.ONLY_FOR_PREMIUM", player.getLang()).toString());
			return false;
		}
		
		if (!template.getRequestItems().isEmpty())
		{
			for (final var tpl : template.getRequestItems())
			{
				if (tpl.getId() == -100 && player.getPcBangPoints() < tpl.getCount())
				{
					final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(player);
					player.sendPacket(sm);
					return false;
				}
				else if (tpl.getId() == -200 && (player.getClan() == null || player.getClan().getReputationScore() < tpl.getCount()))
				{
					final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(player);
					player.sendPacket(sm);
					return false;
				}
				else if (tpl.getId() == -300 && player.getFame() < tpl.getCount())
				{
					final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(player);
					player.sendPacket(sm);
					return false;
				}
				else if (tpl.getId() > 0 && (player.getInventory().getItemByItemId(tpl.getId()) == null || player.getInventory().getItemByItemId(tpl.getId()).getCount() < tpl.getCount()))
				{
					final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(player);
					player.sendPacket(sm);
					return false;
				}
			}
		}
		
		if (template.getRequiredQuest() != null)
		{
			final var qs = player.getQuestState(template.getRequiredQuest());
			boolean cannot = false;
			if (template.getQuestType() == ReflectionQuestType.STARTED)
			{
				if (qs == null || !qs.isStarted())
				{
					cannot = true;
				}
			}
			else
			{
				if (qs == null || !qs.isCompleted())
				{
					cannot = true;
				}
			}
			
			if (cannot)
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(player);
				player.sendPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template)
	{
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
		
		if (party.getMemberCount() < template.getMinParty())
		{
			final var sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER);
			sm.addNumber(template.getMinParty());
			player.sendPacket(sm);
			return false;
		}
		
		if (party.getMemberCount() > template.getMaxParty())
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		
		if (!template.getRequestItems().isEmpty())
		{
			for (final var tpl : template.getRequestItems())
			{
				if (tpl.getType() == ReflectionRemoveType.LEADER)
				{
					if (tpl.getId() == -100 && player.getPcBangPoints() < tpl.getCount())
					{
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(player);
						player.sendPacket(sm);
						return false;
					}
					else if (tpl.getId() == -200 && (player.getClan() == null || player.getClan().getReputationScore() < tpl.getCount()))
					{
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(player);
						player.sendPacket(sm);
						return false;
					}
					else if (tpl.getId() == -300 && player.getFame() < tpl.getCount())
					{
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(player);
						player.sendPacket(sm);
						return false;
					}
					else if (tpl.getId() > 0 && (player.getInventory().getItemByItemId(tpl.getId()) == null || player.getInventory().getItemByItemId(tpl.getId()).getCount() < tpl.getCount()))
					{
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(player);
						player.sendPacket(sm);
						return false;
					}
				}
			}
		}
		
		if (template.getHwidsLimit() > 0)
		{
			if (!Util.isAvalibleWindows(party.getMembers(), template.getHwidsLimit(), true))
			{
				final var msg = new ServerMessage("Reflection.HWIDS_LIMIT", true);
				msg.add(template.getHwidsLimit());
				party.broadCastMessage(msg);
				return false;
			}
		}
		
		if (template.getIpsLimit() > 0)
		{
			if (!Util.isAvalibleWindows(party.getMembers(), template.getIpsLimit(), false))
			{
				final var msg = new ServerMessage("Reflection.IPS_LIMIT", true);
				msg.add(template.getHwidsLimit());
				party.broadCastMessage(msg);
				return false;
			}
		}
		
		for (final var partyMember : party.getMembers())
		{
			if (ReflectionManager.getInstance().getPlayerReflection(partyMember.getObjectId(), false) != null)
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				sm.addPcName(partyMember);
				party.broadCast(sm);
				return false;
			}
			
			if (template.getRebirth() > 0 && (partyMember.getVarInt("rebirth", 0) < template.getRebirth()))
			{
				final var msg = new ServerMessage("Reflection.LOW_PLAYER_REBIRTH", true);
				msg.add(partyMember.getName(null));
				msg.add(template.getRebirth());
				party.broadCastMessage(msg);
				return false;
			}
			
			if (!template.getSkills().isEmpty())
			{
				final var checkAllSkills = template.checkAllSkills();
				var found = false;
				for (final var sk : template.getSkills())
				{
					final var skill = partyMember.getKnownSkill(sk.getId());
					if (!checkAllSkills)
					{
	    				if (skill != null && skill.getLevel() >= sk.getLvl())
	    				{
	    					found = true;
	    					break;
	    				}
					}
					else
					{
	    				if (skill == null || skill.getLevel() < sk.getLvl())
	    				{
							final var msg = new ServerMessage("Reflection.LOW_PLAYER_SKILL", true);
							msg.add(partyMember.getName(null));
	    					msg.add(sk.getSkill().getName(player.getLang()));
	    					party.broadCastMessage(msg);
	    					return false;
	    				}
					}
				}
				
				if (!checkAllSkills && !found)
				{
					final var msg = new ServerMessage("Reflection.LOW_PLAYER_SKILLS", true);
					msg.add(partyMember.getName(null));
					party.broadCastMessage(msg);
					return false;
				}
			}
			
			if (template.isForNoble() && !partyMember.isNoble())
			{
				final var msg = new ServerMessage("Reflection.MEMBER_NOBLE", true);
				msg.add(partyMember.getName(null));
				party.broadCastMessage(msg);
				return false;
			}
			
			if (template.isForHero() && !partyMember.isHero())
			{
				final var msg = new ServerMessage("Reflection.MEMBER_HERO", true);
				msg.add(partyMember.getName(null));
				party.broadCastMessage(msg);
				return false;
			}
			
			if (template.isHasCastle() || template.isHasFort())
			{
				final var clan = partyMember.getClan();
				if (template.isHasCastle() && (clan == null || clan.getCastleId() <= 0))
				{
					final var msg = new ServerMessage("Reflection.MEMBER_CASTLE", true);
					msg.add(partyMember.getName(null));
					party.broadCastMessage(msg);
					return false;
				}
				
				if (template.isHasFort() && (clan == null || clan.getFortId() <= 0))
				{
					final var msg = new ServerMessage("Reflection.MEMBER_FORT", true);
					msg.add(partyMember.getName(null));
					party.broadCastMessage(msg);
					return false;
				}
			}
			
			if (template.isForPremium() && !partyMember.hasPremiumBonus())
			{
				final var msg = new ServerMessage("Reflection.PLAYER_WITHOUT_PREMIUM", true);
				msg.add(partyMember.getName(null));
				party.broadCastMessage(msg);
				return false;
			}
			
			if (partyMember.getLevel() < template.getMinLevel() || partyMember.getLevel() > template.getMaxLevel())
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadCast(sm);
				return false;
			}
			
			if (!partyMember.isInsideRadius(player, 500, true, true))
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadCast(sm);
				return false;
			}
			
			if (!template.getRequestItems().isEmpty())
			{
				for (final var tpl : template.getRequestItems())
				{
					if (tpl.getType() == ReflectionRemoveType.ALL)
					{
						if (tpl.getId() == -100 && partyMember.getPcBangPoints() < tpl.getCount())
						{
							final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
							sm.addPcName(partyMember);
							party.broadCast(sm);
							return false;
						}
						else if (tpl.getId() == -200 && (partyMember.getClan() == null || partyMember.getClan().getReputationScore() < tpl.getCount()))
						{
							final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
							sm.addPcName(partyMember);
							party.broadCast(sm);
							return false;
						}
						else if (tpl.getId() == -300 && partyMember.getFame() < tpl.getCount())
						{
							final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
							sm.addPcName(partyMember);
							party.broadCast(sm);
							return false;
						}
						else if (tpl.getId() > 0 && (partyMember.getInventory().getItemByItemId(tpl.getId()) == null || partyMember.getInventory().getItemByItemId(tpl.getId()).getCount() < tpl.getCount()))
						{
							final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
							sm.addPcName(partyMember);
							party.broadCast(sm);
							return false;
						}
					}
				}
			}
			
			if (!isNotCheckTimeRef(template.getId()) && System.currentTimeMillis() < ReflectionParser.getInstance().getMinutesToNextEntrance(template.getId(), partyMember))
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(partyMember);
				party.broadCast(sm);
				return false;
			}
			
			if (template.getRequiredQuest() != null)
			{
				final var qs = partyMember.getQuestState(template.getRequiredQuest());
				boolean cannot = false;
				if (template.getQuestType() == ReflectionQuestType.STARTED)
				{
					if (qs == null || !qs.isStarted())
					{
						cannot = true;
					}
				}
				else
				{
					if (qs == null || !qs.isCompleted())
					{
						cannot = true;
					}
				}
				
				if (cannot)
				{
					final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(partyMember);
					party.broadCast(sm);
					return false;
				}
			}
		}
		return true;
	}
	
	protected boolean checkReenterPartyType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var party = player.getParty();
		if (party == null || party.getMemberCount() < 2)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		
		if (party.getMemberCount() > template.getMaxParty())
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		return true;
	}
	
	protected boolean checkCommandChannelType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var pt = player.getParty();
		if (pt == null || pt.getCommandChannel() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		
		final var cc = pt.getCommandChannel();
		if (cc.getMemberCount() < template.getMinParty())
		{
			final var sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER);
			sm.addNumber(template.getMinParty());
			player.sendPacket(sm);
			return false;
		}
		
		if (cc.getMemberCount() > template.getMaxParty())
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		
		if (!template.getRequestItems().isEmpty())
		{
			for (final var tpl : template.getRequestItems())
			{
				if (tpl.getType() == ReflectionRemoveType.LEADER)
				{
					if (tpl.getId() == -100 && player.getPcBangPoints() < tpl.getCount())
					{
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(player);
						player.sendPacket(sm);
						return false;
					}
					else if (tpl.getId() == -200 && (player.getClan() == null || player.getClan().getReputationScore() < tpl.getCount()))
					{
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(player);
						player.sendPacket(sm);
						return false;
					}
					else if (tpl.getId() == -300 && player.getFame() < tpl.getCount())
					{
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(player);
						player.sendPacket(sm);
						return false;
					}
					else if (tpl.getId() > 0 && (player.getInventory().getItemByItemId(tpl.getId()) == null || player.getInventory().getItemByItemId(tpl.getId()).getCount() < tpl.getCount()))
					{
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(player);
						player.sendPacket(sm);
						return false;
					}
				}
			}
		}
		
		if (template.getHwidsLimit() > 0)
		{
			if (!Util.isAvalibleWindows(cc.getMembers(), template.getHwidsLimit(), true))
			{
				final var msg = new ServerMessage("Reflection.HWIDS_LIMIT", true);
				msg.add(template.getHwidsLimit());
				cc.broadCastMessage(msg);
				return false;
			}
		}
		
		if (template.getIpsLimit() > 0)
		{
			if (!Util.isAvalibleWindows(cc.getMembers(), template.getIpsLimit(), false))
			{
				final var msg = new ServerMessage("Reflection.IPS_LIMIT", true);
				msg.add(template.getHwidsLimit());
				cc.broadCastMessage(msg);
				return false;
			}
		}
		
		for (final var channelMember : cc.getMembers())
		{
			if (ReflectionManager.getInstance().getPlayerReflection(channelMember.getObjectId(), false) != null)
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				sm.addPcName(channelMember);
				cc.broadCast(sm);
				return false;
			}
			
			if (channelMember.getLevel() < template.getMinLevel() || channelMember.getLevel() > template.getMaxLevel())
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(channelMember);
				cc.broadCast(sm);
				return false;
			}
			
			if (template.getRebirth() > 0 && (channelMember.getVarInt("rebirth", 0) < template.getRebirth()))
			{
				final var msg = new ServerMessage("Reflection.LOW_PLAYER_REBIRTH", true);
				msg.add(channelMember.getName(null));
				msg.add(template.getRebirth());
				cc.broadCastMessage(msg);
				return false;
			}
			
			if (!template.getSkills().isEmpty())
			{
				final var checkAllSkills = template.checkAllSkills();
				var found = false;
				for (final var sk : template.getSkills())
				{
					final var skill = channelMember.getKnownSkill(sk.getId());
					if (!checkAllSkills)
					{
						if (skill != null && skill.getLevel() >= sk.getLvl())
						{
							found = true;
							break;
						}
					}
					else
					{
						if (skill == null || skill.getLevel() < sk.getLvl())
						{
							final var msg = new ServerMessage("Reflection.LOW_PLAYER_SKILL", true);
							msg.add(channelMember.getName(null));
							msg.add(sk.getSkill().getName(player.getLang()));
							cc.broadCastMessage(msg);
							return false;
						}
					}
				}
				
				if (!checkAllSkills && !found)
				{
					final var msg = new ServerMessage("Reflection.LOW_PLAYER_SKILLS", true);
					msg.add(channelMember.getName(null));
					cc.broadCastMessage(msg);
					return false;
				}
			}
			
			if (template.isForNoble() && !channelMember.isNoble())
			{
				final var msg = new ServerMessage("Reflection.MEMBER_NOBLE", true);
				msg.add(channelMember.getName(null));
				cc.broadCastMessage(msg);
				return false;
			}
			
			if (template.isForHero() && !channelMember.isHero())
			{
				final var msg = new ServerMessage("Reflection.MEMBER_HERO", true);
				msg.add(channelMember.getName(null));
				cc.broadCastMessage(msg);
				return false;
			}
			
			if (template.isHasCastle() || template.isHasFort())
			{
				final var clan = channelMember.getClan();
				if (template.isHasCastle() && (clan == null || clan.getCastleId() <= 0))
				{
					final var msg = new ServerMessage("Reflection.MEMBER_CASTLE", true);
					msg.add(channelMember.getName(null));
					cc.broadCastMessage(msg);
					return false;
				}
				
				if (template.isHasFort() && (clan == null || clan.getFortId() <= 0))
				{
					final var msg = new ServerMessage("Reflection.MEMBER_FORT", true);
					msg.add(channelMember.getName(null));
					cc.broadCastMessage(msg);
					return false;
				}
			}
			
			if (template.isForPremium() && !channelMember.hasPremiumBonus())
			{
				final var msg = new ServerMessage("Reflection.PLAYER_WITHOUT_PREMIUM", true);
				msg.add(channelMember.getName(null));
				cc.broadCastMessage(msg);
				return false;
			}
			
			if (!Util.checkIfInRange(1000, player, channelMember, true))
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(channelMember);
				cc.broadCast(sm);
				return false;
			}
			
			if (!template.getRequestItems().isEmpty())
			{
				for (final var tpl : template.getRequestItems())
				{
					if (tpl.getType() == ReflectionRemoveType.ALL)
					{
						if (tpl.getId() == -100 && channelMember.getPcBangPoints() < tpl.getCount())
						{
							final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
							sm.addPcName(channelMember);
							cc.broadCast(sm);
							return false;
						}
						else if (tpl.getId() == -200 && (channelMember.getClan() == null || channelMember.getClan().getReputationScore() < tpl.getCount()))
						{
							final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
							sm.addPcName(channelMember);
							cc.broadCast(sm);
							return false;
						}
						else if (tpl.getId() == -300 && channelMember.getFame() < tpl.getCount())
						{
							final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
							sm.addPcName(channelMember);
							cc.broadCast(sm);
							return false;
						}
						else if (tpl.getId() > 0 && (channelMember.getInventory().getItemByItemId(tpl.getId()) == null || channelMember.getInventory().getItemByItemId(tpl.getId()).getCount() < tpl.getCount()))
						{
							final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
							sm.addPcName(channelMember);
							cc.broadCast(sm);
							return false;
						}
					}
				}
			}
			
			if (!isNotCheckTimeRef(template.getId()) && System.currentTimeMillis() < ReflectionParser.getInstance().getMinutesToNextEntrance(template.getId(), channelMember))
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(channelMember);
				cc.broadCast(sm);
				return false;
			}
			
			if (template.getRequiredQuest() != null)
			{
				final var qs = channelMember.getQuestState(template.getRequiredQuest());
				boolean cannot = false;
				if (template.getQuestType() == ReflectionQuestType.STARTED)
				{
					if (qs == null || !qs.isStarted())
					{
						cannot = true;
					}
				}
				else
				{
					if (qs == null || !qs.isCompleted())
					{
						cannot = true;
					}
				}
				
				if (cannot)
				{
					final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(channelMember);
					cc.broadCast(sm);
					return false;
				}
			}
		}
		return true;
	}
	
	protected boolean checkReenterCommandChannelType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var pt = player.getParty();
		if (pt == null || pt.getCommandChannel() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		
		final var cc = pt.getCommandChannel();
		if (cc.getMemberCount() > template.getMaxParty())
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		return true;
	}
	
	protected void handleRemoveBuffs(Player player)
	{
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		final var summon = player.getSummon();
		if (summon != null)
		{
			summon.stopAllEffectsExceptThoseThatLastThroughDeath();
		}
	}
	
	protected void finishInstance(Reflection r, boolean checkTime)
	{
		finishInstance(r, 300000, checkTime);
	}
	
	protected void finishInstance(Reflection r, int duration, boolean checkTime)
	{
		if (!r.getReuseUponEntry() && checkTime)
		{
			handleReenterTime(r);
		}
		
		if (duration == 0)
		{
			r.collapse();
		}
		else if (duration > 0)
		{
			r.setDuration(duration);
			r.setEmptyDestroyTime(0);
		}
	}
	
	protected void handleReenterTime(Reflection r)
	{
		final var reenterData = r.getReenterData();
		long time = -1;
		
		for (final var data : reenterData)
		{
			if (data.getTime() > 0)
			{
				time = System.currentTimeMillis() + data.getTime();
				break;
			}
			
			final var calendar = Calendar.getInstance();
			calendar.set(Calendar.AM_PM, data.getHour() >= 12 ? 1 : 0);
			calendar.set(Calendar.HOUR, data.getHour());
			calendar.set(Calendar.MINUTE, data.getMinute());
			calendar.set(Calendar.SECOND, 0);
			
			if (calendar.getTimeInMillis() <= System.currentTimeMillis())
			{
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			
			if (data.getDay() != null)
			{
				while (calendar.get(Calendar.DAY_OF_WEEK) != (Math.min(data.getDay().getValue() + 1, 7)))
				{
					calendar.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
			
			if (time == -1)
			{
				time = calendar.getTimeInMillis();
			}
			else if (calendar.getTimeInMillis() < time)
			{
				time = calendar.getTimeInMillis();
			}
		}
		
		if (time > 0)
		{
			setReenterTime(r, time, r.isHwidCheck());
		}
	}
	
	protected void setReenterTime(Reflection r, long time, boolean isHwidLimit)
	{
		final var instance = ReflectionManager.getInstance();
		for (final var player : r.getAllowed())
		{
			instance.setReflectionTime(player, r.getTemplateId(), time, isHwidLimit);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED).addString(instance.getReflectionName(player, r.getTemplateId())));
		}
	}
	
	public void getActivatedZone(Reflection reflection, int zoneId, boolean doActivate)
	{
		final var zone = ZoneManager.getInstance().getZoneById(zoneId, ReflectionZone.class);
		if (zone != null)
		{
			if (doActivate)
			{
				zone.addRef(reflection.getId());
				reflection.addZone(zone.getId());
			}
			else
			{
				zone.removeRef(reflection.getId());
				reflection.removeZone(zone.getId());
			}
		}
	}
	
	private boolean isNotCheckTimeRef(int refId)
	{
		return refId == 127 || refId == 128 || refId == 129 || refId == 130;
	}
}