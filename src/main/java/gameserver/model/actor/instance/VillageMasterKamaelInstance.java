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
package gameserver.model.actor.instance;

import gameserver.Config;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.base.ClassId;
import gameserver.model.base.Race;
import gameserver.model.quest.QuestState;

public final class VillageMasterKamaelInstance extends VillageMasterInstance
{
	public VillageMasterKamaelInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected final String getSubClassMenu(Race race)
	{
		if (Config.ALT_GAME_SUBCLASS_EVERYWHERE || Config.ALT_GAME_SUBCLASS_ALL_CLASSES || race == Race.KAMAEL)
		{
			return "data/html/villagemaster/SubClass.htm";
		}
		return "data/html/villagemaster/SubClass_NoKamael.htm";
	}
	
	@Override
	protected final String getSubClassFail()
	{
		return "data/html/villagemaster/SubClass_Fail_Kamael.htm";
	}
	
	@Override
	protected boolean checkQuests(Player player)
	{
		if (player.isNoble())
		{
			return true;
		}

		QuestState qs = player.getQuestState("_234_FatesWhisper");
		if (qs == null || !qs.isCompleted())
		{
			return false;
		}
		
		qs = player.getQuestState("_236_SeedsOfChaos");
		if (qs == null || !qs.isCompleted())
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	protected boolean checkVillageMasterRace(ClassId pclass)
	{
		return pclass == null ? false : pclass.getRace() == Race.KAMAEL;
	}
}