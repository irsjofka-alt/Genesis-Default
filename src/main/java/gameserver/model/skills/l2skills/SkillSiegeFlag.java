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
package gameserver.model.skills.l2skills;

import gameserver.Config;
import gameserver.data.parser.NpcsParser;
import gameserver.idfactory.IdFactory;
import gameserver.instancemanager.CHSiegeManager;
import gameserver.instancemanager.CastleManager;
import gameserver.instancemanager.FortManager;
import gameserver.instancemanager.FortSiegeManager;
import gameserver.instancemanager.TerritoryWarManager;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.SiegeFlagInstance;
import gameserver.model.entity.Castle;
import gameserver.model.entity.Fort;
import gameserver.model.entity.clanhall.SiegableHall;
import gameserver.model.skills.Skill;
import gameserver.model.stats.StatsSet;
import gameserver.model.zone.ZoneId;
import gameserver.model.zone.type.HqZone;
import gameserver.network.SystemMessageId;

public class SkillSiegeFlag extends Skill
{
	private final boolean _isAdvanced;
	private final boolean _isOutpost;
	
	public SkillSiegeFlag(StatsSet set)
	{
		super(set);
		_isAdvanced = set.getBool("isAdvanced", false);
		_isOutpost = set.getBool("isOutpost", false);
	}
	
	@Override
	public void useSkill(Creature activeChar, GameObject[] targets, double cubicPower)
	{
		if (!activeChar.isPlayer())
		{
			return;
		}
		
		final Player player = activeChar.getActingPlayer();

		if ((player.getClan() == null) || (player.getClan().getLeaderId() != player.getObjectId()))
		{
			return;
		}

		if (!checkIfOkToPlaceFlag(player, true, _isOutpost))
		{
			return;
		}

		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			try
			{
				final SiegeFlagInstance flag = new SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate((_isOutpost ? 36590 : 35062)), _isAdvanced, _isOutpost);
				flag.setGlobalTitle(player.getClan().getName());
				flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
				flag.setHeading(player.getHeading());
				flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
				if (_isOutpost)
				{
					TerritoryWarManager.getInstance().setHQForClan(player.getClan(), flag);
				}
				else
				{
					TerritoryWarManager.getInstance().addClanFlag(player.getClan(), flag);
				}
			}
			catch (final Exception e)
			{
				player.sendMessage("Error placing flag: " + e);
				_log.warn("Error placing flag: " + e.getMessage(), e);
			}
			return;
		}

		try
		{
			final SiegeFlagInstance flag = new SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(35062), _isAdvanced, false);
			flag.setGlobalTitle(player.getClan().getName());
			flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			final Castle castle = CastleManager.getInstance().getCastle(activeChar);
			final Fort fort = FortManager.getInstance().getFort(activeChar);
			final SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(activeChar);
			if (castle != null)
			{
				castle.getSiege().getFlag(player.getClan()).add(flag);
			}
			else if (fort != null)
			{
				fort.getSiege().getFlag(player.getClan()).add(flag);
			}
			else
			{
				hall.getSiege().getFlag(player.getClan()).add(flag);
			}

		}
		catch (final Exception e)
		{
			player.sendMessage("Error placing flag:" + e);
			_log.warn("Error placing flag: " + e.getMessage(), e);
		}
	}

	public static boolean checkIfOkToPlaceFlag(Creature activeChar, boolean isCheckOnly, boolean isOutPost)
	{
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			return checkIfOkToPlaceHQ(activeChar, isOutPost);
		}
		else if (isOutPost)
		{
			return false;
		}
		final Castle castle = CastleManager.getInstance().getCastle(activeChar);
		final Fort fort = FortManager.getInstance().getFort(activeChar);
		final SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(activeChar);

		if ((castle == null) && (fort == null) && (hall == null))
		{
			return false;
		}
		if (castle != null)
		{
			return checkIfOkToPlaceFlag(activeChar, castle, isCheckOnly);
		}
		else if (fort != null)
		{
			return checkIfOkToPlaceFlag(activeChar, fort, isCheckOnly);
		}
		return checkIfOkToPlaceFlag(activeChar, hall, isCheckOnly);
	}

	public static boolean checkIfOkToPlaceFlag(Creature activeChar, Castle castle, boolean isCheckOnly)
	{
		if (!activeChar.isPlayer())
		{
			return false;
		}

		String text = "";
		final Player player = activeChar.getActingPlayer();

		if ((castle == null || castle.getTemplate() == null) || (castle.getId() <= 0))
		{
			text = "You must be on castle ground to place a flag.";
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			text = "You can only place a flag during a siege.";
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
		{
			text = "You must be an attacker to place a flag.";
		}
		else if (!player.isClanLeader())
		{
			text = "You must be a clan leader to place a flag.";
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= castle.getTemplate().getMaxFlags())
		{
			text = "You have already placed the maximum number of flags possible.";
		}
		else if (!player.isInsideZone(ZoneId.HQ))
		{
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		}
		else
		{
			return true;
		}

		if (!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
	}

	public static boolean checkIfOkToPlaceFlag(Creature activeChar, Fort fort, boolean isCheckOnly)
	{
		if (!activeChar.isPlayer())
		{
			return false;
		}

		String text = "";
		final Player player = activeChar.getActingPlayer();

		if ((fort == null) || (fort.getId() <= 0))
		{
			text = "You must be on fort ground to place a flag.";
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			text = "You can only place a flag during a siege.";
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			text = "You must be an attacker to place a flag.";
		}
		else if (!player.isClanLeader())
		{
			text = "You must be a clan leader to place a flag.";
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= FortSiegeManager.getInstance().getFlagMaxCount())
		{
			text = "You have already placed the maximum number of flags possible.";
		}
		else if (!player.isInsideZone(ZoneId.HQ))
		{
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		}
		else
		{
			return true;
		}

		if (!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
	}

	public static boolean checkIfOkToPlaceFlag(Creature activeChar, SiegableHall hall, boolean isCheckOnly)
	{
		if (!activeChar.isPlayer())
		{
			return false;
		}

		String text = "";
		final Player player = activeChar.getActingPlayer();
		final int hallId = hall.getId();

		if (hallId <= 0)
		{
			text = "You must be on Siegable clan hall ground to place a flag.";
		}
		else if (!hall.isInSiege())
		{
			text = "You can only place a flag during a siege.";
		}
		else if ((player.getClan() == null) || !player.isClanLeader())
		{
			text = "You must be a clan leader to place a flag.";
		}
		else if (!hall.isRegistered(player.getClan()))
		{
			text = "You must be an attacker to place a flag.";
		}
		else if (hall.getSiege().getAttackerClan(player.getClan()).getNumFlags() > Config.CHS_MAX_FLAGS_PER_CLAN)
		{
			text = "You have already placed the maximum number of flags possible.";
		}
		else if (!player.isInsideZone(ZoneId.HQ))
		{
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		}
		else if (!hall.getSiege().canPlantFlag())
		{
			text = "You cannot place a flag on this siege.";
		}
		else
		{
			return true;
		}

		if (!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
	}

	public static boolean checkIfOkToPlaceHQ(Creature activeChar, boolean isOutPost)
	{
		final var player = activeChar.getActingPlayer();
		if (player == null || !player.isClanLeader())
		{
			return false;
		}
		final var clan = player.getClan();
		final var castle = CastleManager.getInstance().getCastle(activeChar);
		final var fort = FortManager.getInstance().getFort(activeChar);
		final var hqZone = ZoneManager.getInstance().getZone(activeChar, HqZone.class);
		if ((castle == null) && (fort == null))
		{
			return false;
		}

		if (hqZone == null)
		{
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
			return false;
		}
		
		if (clan != null && isOutPost && ((clan.getCastleId() > 0 && clan.getCastleId() == hqZone.getCastleId()) || (clan.getFortId() > 0 && clan.getFortId() == hqZone.getFortId())))
		{
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
			return false;
		}

		if (((fort != null) && (fort.getId() == 0)) || ((castle != null) && (castle.getId() == 0)))
		{
			return false;
		}
		
		if (((fort != null) && !fort.getZone().isActive()) || ((castle != null) && !castle.getZone().isActive()))
		{
			return false;
		}
		
		if ((TerritoryWarManager.getInstance().getHQForClan(player.getClan()) != null) && isOutPost)
		{
			player.sendPacket(SystemMessageId.NOT_ANOTHER_HEADQUARTERS);
			return false;
		}
		
		if ((TerritoryWarManager.getInstance().getFlagForClan(player.getClan()) != null) && !isOutPost)
		{
			player.sendPacket(SystemMessageId.A_FLAG_IS_ALREADY_BEING_DISPLAYED_ANOTHER_FLAG_CANNOT_BE_DISPLAYED);
			return false;
		}
		return true;
	}
}