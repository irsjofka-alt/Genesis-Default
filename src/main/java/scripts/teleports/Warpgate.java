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
package scripts.teleports;

import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.instancemanager.HellboundManager;
import gameserver.model.PcCondOverride;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;
import gameserver.model.zone.ZoneType;

public class Warpgate extends Quest
{
	public Warpgate()
	{
		super(-1);

		addStartNpc(32314, 32315, 32316, 32317, 32318, 32319);
		addFirstTalkId(32314, 32315, 32316, 32317, 32318, 32319);
		addTalkId(32314, 32315, 32316, 32317, 32318, 32319);
		
		addEnterZoneId(40101);
	}
	
	private static final boolean canEnter(Player player)
	{
		if (player.isFlying())
		{
			return false;
		}

		if (Config.ENTER_HELLBOUND_WITHOUT_QUEST || player.isInFightEvent() || player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS) || player.isInVehicle())
		{
			return true;
		}

		QuestState st;
		if (!HellboundManager.getInstance().isLocked())
		{
			st = player.getQuestState("_130_PathToHellbound");
			if ((st != null) && st.isCompleted())
			{
				return true;
			}
		}
		
		st = player.getQuestState("_133_ThatsBloodyHot");
		return ((st != null) && st.isCompleted());
	}
	
	@Override
	public final String onFirstTalk(Npc npc, Player player)
	{
		if (!canEnter(player))
		{
			if (HellboundManager.getInstance().isLocked())
			{
				return "warpgate-locked.htm";
			}
		}
		
		return npc.getId() + ".htm";
	}
	
	@Override
	public final String onTalk(Npc npc, Player player)
	{
		if (!canEnter(player))
		{
			return "warpgate-no.htm";
		}
		player.teleToLocation(-11272, 236464, -3248, true, player.getReflection());
		HellboundManager.getInstance().unlock();
		return null;
	}
	
	@Override
	public final String onEnterZone(Creature character, ZoneType zone)
	{
		if (character.isPlayer())
		{
			if (!canEnter(character.getActingPlayer()))
			{
				ThreadPoolManager.getInstance().schedule(new Teleport(character), 1000);
			}
		}
		return null;
	}
	
	private static final class Teleport implements Runnable
	{
		private final Creature _char;
		
		public Teleport(Creature c)
		{
			_char = c;
		}
		
		@Override
		public void run()
		{
			try
			{
				_char.teleToLocation(-16555, 209375, -3670, true, _char.getReflection());
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	void main()
	{
		new Warpgate();
	}
}