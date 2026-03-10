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
package gameserver.network.clientpackets;

import gameserver.model.actor.Player;
import gameserver.model.actor.instance.player.impl.SkillCoolTimeTask;
import gameserver.network.GameClient;

public class RequestSkillCoolTime extends GameClientPacket
{
	GameClient client;
	
	@Override
	protected void readImpl()
	{
		client = getClient();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		player.getPersonalTasks().addTask(new SkillCoolTimeTask(500L));
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}