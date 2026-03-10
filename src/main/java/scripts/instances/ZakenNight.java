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

import gameserver.GameTimeController;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.serverpackets.MagicSkillUse;

/**
 * Rework by LordWinter 06.02.2020
 */
public class ZakenNight extends AbstractReflection
{
	private final int[][] SPAWNS =
	{
	        // Floor 1
	        {
	                54240, 220133, -3498
			},
			{
			        54240, 218073, -3498
			},
			{
			        55265, 219095, -3498
			},
			{
			        56289, 220133, -3498
			},
			{
			        56289, 218073, -3498
			},

			// Floor 2
			{
			        54240, 220133, -3226
			},
			{
			        54240, 218073, -3226
			},
			{
			        55265, 219095, -3226
			},
			{
			        56289, 220133, -3226
			},
			{
			        56289, 218073, -3226
			},

			// Floor 3
			{
			        54240, 220133, -2954
			},
			{
			        54240, 218073, -2954
			},
			{
			        55265, 219095, -2954
			},
			{
			        56289, 220133, -2954
			},
			{
			        56289, 218073, -2954
			}
	};

	public ZakenNight()
	{
		super(114);

		addStartNpc(32713);
		addTalkId(32713);
		addAttackId(29022);
		addKillId(29022);
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
	protected boolean checkConditions(Player player, Npc npc, ReflectionTemplate template)
	{
		final boolean checkTime = template.getParams().getBool("checkValidTime");
		if (checkTime)
		{
			if ((getTimeHour() > 4) && (getTimeHour() < 24))
			{
				player.sendMessage((new ServerMessage("Zaken.INVALID_TIME", player.getLang())).toString());
				return false;
			}
		}
		return super.checkConditions(player, npc, template);
	}

	private final synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 114))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				addSpawn(29022, 55312, 219168, -3223, 0, false, 0, false, r);
			}
		}
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("enter"))
		{
			enterInstance(player, npc);
			return null;
		}
		else
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				if (npc.getId() == 29022 && !npc.isDead())
				{
					switch (event)
					{
						case "teleport" :
							npc.abortAttack();
							npc.abortCast();
							npc.broadcastPacketToOthers(2000, new MagicSkillUse(npc, 4222, 1, 1000, 0));
							startQuestTimer("finish_teleport", 1500, npc, player);
							break;
						case "finish_teleport" :
							final int i = getRandom(SPAWNS.length);
							npc.teleToLocation(SPAWNS[i][0], SPAWNS[i][1], SPAWNS[i][2], true, npc.getReflection());
							((Attackable) npc).getAggroList().clear(true);
							npc.getSpawn().setX(SPAWNS[i][0]);
							npc.getSpawn().setY(SPAWNS[i][1]);
							npc.getSpawn().setZ(SPAWNS[i][2]);
							npc.setScriptValue(0);
							break;
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (npc.isScriptValue(0))
			{
				npc.setScriptValue(1);
				startQuestTimer("teleport", 300000, npc, attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			finishInstance(r, true);
		}
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		final int npcId = npc.getId();
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (npcId == 32713)
		{
			enterInstance(player, npc);
		}
		return "";
	}

	private int getTimeHour()
	{
		return (GameTimeController.getInstance().getGameTime() / 60) % 24;
	}
	
	void main()
	{
		new ZakenNight();
	}
}
