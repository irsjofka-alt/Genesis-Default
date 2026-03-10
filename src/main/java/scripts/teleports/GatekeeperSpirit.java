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

import gameserver.SevenSigns;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;

/**
 * Based on L2J Eternity-World
 */
public class GatekeeperSpirit extends Quest
{
	private GatekeeperSpirit()
	{
		super(-1);
		
		addStartNpc(31111, 31112);
		addFirstTalkId(31111, 31112);
		addTalkId(31111, 31112);
		
		addKillId(25283, 25286);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		
		if (event.equalsIgnoreCase("enter"))
		{
			final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
			final int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
			final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
			final boolean validation = SevenSigns.getInstance().isSealValidationPeriod();
			
			if (validation && (playerCabal == sealAvariceOwner) && (playerCabal == compWinner))
			{
				switch (sealAvariceOwner)
				{
					case SevenSigns.CABAL_DAWN:
						player.teleToLocation(184448, -10112, -5504, false, player.getReflection());
						break;
					case SevenSigns.CABAL_DUSK:
						player.teleToLocation(184464, -13104, -5504, false, player.getReflection());
						break;
				}
				return null;
			}
			htmltext = "spirit_gate_q0506_01.htm";
		}
		else if (event.equalsIgnoreCase("exit"))
		{
			player.teleToLocation(182960, -11904, -4897, true, player.getReflection());
			return null;
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return switch (npc.getId())
		{
			case 31111  -> "spirit_gate001.htm";
			case 31112  -> "spirit_gate002.htm";
			default -> "";
		};
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final int npcId = npc.getId();
		if (npcId == 25283)
		{
			addSpawn(31112, 184410, -10111, -5488, 0, false, 900000);
		}
		else if (npcId == 25286)
		{
			addSpawn(31112, 184410, -13102, -5488, 0, false, 900000);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new GatekeeperSpirit();
	}
}