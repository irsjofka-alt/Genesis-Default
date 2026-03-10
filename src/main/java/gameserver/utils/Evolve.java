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
package gameserver.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gameserver.ThreadPoolManager;
import gameserver.data.parser.NpcsParser;
import gameserver.data.parser.PetsParser;
import gameserver.database.DatabaseFactory;
import gameserver.model.GameObjectsStorage;
import gameserver.model.PetData;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.PetInstance;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExUserInfoInvenWeight;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.network.serverpackets.MagicSkillLaunched;
import gameserver.network.serverpackets.MagicSkillUse;
import gameserver.network.serverpackets.StatusUpdate;
import gameserver.network.serverpackets.SystemMessage;

public final class Evolve
{
	private static final Logger _log = LogManager.getLogger(Evolve.class);
	
	public static final boolean doEvolve(Player player, Npc npc, int itemIdtake, int itemIdgive, int petminlvl)
	{
		if ((itemIdtake == 0) || (itemIdgive == 0) || (petminlvl == 0))
		{
			return false;
		}
		
		if (!player.hasPet())
		{
			return false;
		}
		
		final PetInstance currentPet = (PetInstance) player.getSummon();
		if (currentPet.isAlikeDead())
		{
			Util.handleIllegalPlayerAction(player, "" + player.getName(null) + " tried to use death pet exploit!");
			return false;
		}
		
		ItemInstance item = null;
		long petexp = currentPet.getStat().getExp();
		final String oldname = currentPet.getName(null);
		final int oldX = currentPet.getX();
		final int oldY = currentPet.getY();
		final int oldZ = currentPet.getZ();
		
		final PetData oldData = PetsParser.getInstance().getPetDataByItemId(itemIdtake);
		
		if (oldData == null)
		{
			return false;
		}
		
		final int oldnpcID = oldData.getNpcId();
		
		if ((currentPet.getStat().getLevel() < petminlvl) || (currentPet.getId() != oldnpcID))
		{
			return false;
		}
		
		final PetData petData = PetsParser.getInstance().getPetDataByItemId(itemIdgive);
		
		if (petData == null)
		{
			return false;
		}
		
		final int npcID = petData.getNpcId();
		
		if (npcID == 0)
		{
			return false;
		}
		
		final NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(npcID);
		
		currentPet.unSummon(player);
		
		currentPet.destroyControlItem(player, true);
		
		item = player.getInventory().addItem("Evolve", itemIdgive, 1, player, npc);
		
		final PetInstance petSummon = PetInstance.spawnPet(npcTemplate, player, item);
		
		if (petSummon == null)
		{
			return false;
		}
		
		final long _minimumexp = petSummon.getStat().getExpForLevel(petminlvl);
		if (petexp < _minimumexp)
		{
			petexp = _minimumexp;
		}
		
		petSummon.getStat().addExp(petexp);
		petSummon.setCurrentHp(petSummon.getMaxHp());
		petSummon.setCurrentMp(petSummon.getMaxMp());
		petSummon.setCurrentFed(petSummon.getMaxFed());
		petSummon.setGlobalTitle(player.getName(null));
		petSummon.setGlobalName(oldname);
		petSummon.setRunning();
		petSummon.store();
		
		player.setPet(petSummon);
		
		player.sendPacket(new MagicSkillUse(npc, 2046, 1, 1000, 600000));
		player.sendPacket(SystemMessageId.SUMMON_A_PET);
		petSummon.spawnMe(oldX, oldY, oldZ);
		petSummon.startFeed();
		item.setEnchantLevel(petSummon.getLevel());
		
		ThreadPoolManager.getInstance().schedule(new EvolveFinalizer(player, petSummon), 900);
		
		if (petSummon.getCurrentFed() <= 0)
		{
			ThreadPoolManager.getInstance().schedule(new EvolveFeedWait(player, petSummon), 60000);
		}
		else
		{
			petSummon.startFeed();
		}
		
		return true;
	}
	
	public static final boolean doRestore(Player player, Npc npc, int itemIdtake, int itemIdgive, int petminlvl)
	{
		if ((itemIdtake == 0) || (itemIdgive == 0) || (petminlvl == 0))
		{
			return false;
		}
		
		final ItemInstance item = player.getInventory().getItemByItemId(itemIdtake);
		if (item == null)
		{
			return false;
		}
		
		int oldpetlvl = item.getEnchantLevel();
		if (oldpetlvl < petminlvl)
		{
			oldpetlvl = petminlvl;
		}
		
		final PetData oldData = PetsParser.getInstance().getPetDataByItemId(itemIdtake);
		if (oldData == null)
		{
			return false;
		}
		
		final PetData petData = PetsParser.getInstance().getPetDataByItemId(itemIdgive);
		if (petData == null)
		{
			return false;
		}
		
		final int npcId = petData.getNpcId();
		if (npcId == 0)
		{
			return false;
		}
		
		final NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(npcId);
		
		final ItemInstance removedItem = player.getInventory().destroyItem("PetRestore", item, player, npc);
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(removedItem);
		player.sendPacket(sm);
		
		final ItemInstance addedItem = player.getInventory().addItem("PetRestore", itemIdgive, 1, player, npc);
		
		final PetInstance petSummon = PetInstance.spawnPet(npcTemplate, player, addedItem);
		if (petSummon == null)
		{
			return false;
		}
		
		final long _maxexp = petSummon.getStat().getExpForLevel(oldpetlvl);
		
		petSummon.getStat().addExp(_maxexp);
		petSummon.setCurrentHp(petSummon.getMaxHp());
		petSummon.setCurrentMp(petSummon.getMaxMp());
		petSummon.setCurrentFed(petSummon.getMaxFed());
		petSummon.setGlobalTitle(player.getName(null));
		petSummon.setRunning();
		petSummon.store();
		
		player.setPet(petSummon);
		
		player.sendPacket(new MagicSkillUse(npc, 2046, 1, 1000, 600000));
		player.sendPacket(SystemMessageId.SUMMON_A_PET);
		petSummon.spawnMe(player.getX(), player.getY(), player.getZ());
		petSummon.startFeed();
		addedItem.setEnchantLevel(petSummon.getLevel());
		
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addRemovedItem(removedItem);
		player.sendInventoryUpdate(iu);
		
		if (player.isHFClient())
		{
			player.sendStatusUpdate(false, false, StatusUpdate.CUR_LOAD);
		}
		else
		{
			player.sendPacket(new ExUserInfoInvenWeight(player));
		}
		
		player.broadcastUserInfo(true);
		
		GameObjectsStorage.removeItem(removedItem);
		
		ThreadPoolManager.getInstance().schedule(new EvolveFinalizer(player, petSummon), 900);
		
		if (petSummon.getCurrentFed() <= 0)
		{
			ThreadPoolManager.getInstance().schedule(new EvolveFeedWait(player, petSummon), 60000);
		}
		else
		{
			petSummon.startFeed();
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?"))
		{
			ps.setInt(1, removedItem.getObjectId());
			ps.execute();
		}
		catch (final Exception _)
		{
		}
		return true;
	}
	
	static final class EvolveFeedWait implements Runnable
	{
		private final Player _activeChar;
		private final PetInstance _petSummon;
		
		EvolveFeedWait(Player activeChar, PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_petSummon.getCurrentFed() <= 0)
				{
					_petSummon.unSummon(_activeChar);
				}
				else
				{
					_petSummon.startFeed();
				}
			}
			catch (final Exception e)
			{
				_log.warn("", e);
			}
		}
	}
	
	static final class EvolveFinalizer implements Runnable
	{
		private final Player _activeChar;
		private final PetInstance _petSummon;
		
		EvolveFinalizer(Player activeChar, PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}
		
		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_petSummon.setFollowStatus(true);
				_petSummon.setShowSummonAnimation(false);
			}
			catch (final Throwable e)
			{
				_log.warn("", e);
			}
		}
	}
}