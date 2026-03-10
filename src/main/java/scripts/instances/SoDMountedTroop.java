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

import org.apache.commons.lang3.ArrayUtils;

import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.SoDManager;
import gameserver.model.Location;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.utils.Util;
import scripts.quests._693_DefeatingDragonkinRemnants;

/**
 * Created by LordWinter 20.12.2020
 */
public class SoDMountedTroop extends AbstractReflection
{
	private static final int[] ENTRANCE_ROOM_DOORS =
	{
	        12240001, 12240002
	};
	
	private static final int[] _templates =
	{
	        123, 124, 125, 126,
	};
	

	public SoDMountedTroop()
	{
		super(123, 124, 125, 126);

		addStartNpc(32527);
		addTalkId(32527);
		addKillId(18703);
		addKillId(18784, 18785, 18786, 18787, 18788, 18789, 18790);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc, int templateId)
	{
		if (enterReflection(player, npc, templateId))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.setParam("startTime", System.currentTimeMillis());
			}
		}
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
	protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var html = new NpcHtmlMessage(npc.getObjectId());
		if (SoDManager.getInstance().isAttackStage())
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/" + _693_DefeatingDragonkinRemnants.class.getSimpleName() + "/32527-15.htm");
			player.sendPacket(html);
			return false;
		}
		return super.checkSoloType(player, npc, template);
	}
	
	@Override
	protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		if (SoDManager.getInstance().isAttackStage())
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/" + _693_DefeatingDragonkinRemnants.class.getSimpleName() + "/32527-15.htm");
			player.sendPacket(html);
			return false;
		}
		return super.checkPartyType(player, npc, template);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (npc.getId() == 32527 && Util.isDigit(event) && ArrayUtils.contains(_templates, Integer.valueOf(event)))
		{
			enterInstance(player, npc, Integer.valueOf(event));
			return null;
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (npc.getId() == 18786 && r.getStatus() == 0)
			{
				r.incStatus();
				for (final int i : ENTRANCE_ROOM_DOORS)
				{
					r.openDoor(i);
				}
			}
			
			if (checkNpcsStatus(npc, r))
			{
				r.setDuration(300000);
				final var players = r.getReflectionPlayers();
				if (!players.isEmpty())
				{
					final long timeDiff = (System.currentTimeMillis() - r.getParams().getLong("startTime", 0)) / 60000L;
					for (final var pl : players)
					{
						if (pl != null && pl.isOnline())
						{
							final var qst = pl.getQuestState("_693_DefeatingDragonkinRemnants");
							if (qst != null)
							{
								qst.setCond(2, true);
								qst.set("timeDiff", String.valueOf(timeDiff));
								qst.set("reflectionId", r.getTemplateId());
							}
						}
					}
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private boolean checkNpcsStatus(Npc npc, Reflection r)
	{
		for (final var n : r.getNpcs())
		{
			if (n != null && !n.isDead())
			{
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args)
	{
		new SoDMountedTroop();
	}
}