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

import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;

/**
 * Rework by LordWinter 27.09.2020
 */
public class TowerofInfinitum5Floor extends AbstractReflection
{
	public TowerofInfinitum5Floor()
	{
		super(142);
		
		addStartNpc(32375, 32748);
		addTalkId(32375, 32748);
		addKillId(25540);
	}
	
	@Override
	protected void onTeleportEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			r.addAllowed(player);
			player.getAI().setIntention(CtrlIntention.IDLE);
			final Location teleLoc = template.getTeleportCoord();
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
			final Location teleLoc = template.getTeleportCoord();
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
		enterReflection(player, npc, 142);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		switch (npc.getId())
		{
			case 32748 :
				enterInstance(player, npc);
				break;
			case 32375 :
				final var r = npc.getReflection();
				if (isInReflection(r))
				{
					r.removeAllowed(player);
				}
				teleportPlayer(player, new Location(-19024, 277122, -8256), ReflectionManager.DEFAULT);
				break;
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			r.setReturnLoc(new Location(-19024, 277122, -8256));
			finishInstance(r, true);
			addSpawn(32375, -22144, 278744, -8256, 0, false, 0, false, r);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new TowerofInfinitum5Floor();
	}
}