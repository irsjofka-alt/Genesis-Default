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

import java.util.ArrayList;
import java.util.List;

import gameserver.GameTimeController;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Rework by LordWinter 06.02.2020
 */
public class ZakenDay extends AbstractReflection
{
	private static final int[][] ROOM_SPAWN =
	{
	        {
	                54240, 220133, -3498, 1, 3, 4, 6
			},
			{
			        54240, 218073, -3498, 2, 5, 4, 7
			},
			{
			        55265, 219095, -3498, 4, 9, 6, 7
			},
			{
			        56289, 220133, -3498, 8, 11, 6, 9
			},
			{
			        56289, 218073, -3498, 10, 12, 7, 9
			},
			{
			        54240, 220133, -3226, 13, 15, 16, 18
			},
			{
			        54240, 218073, -3226, 14, 17, 16, 19
			},
			{
			        55265, 219095, -3226, 21, 16, 19, 18
			},
			{
			        56289, 220133, -3226, 20, 23, 21, 18
			},
			{
			        56289, 218073, -3226, 22, 24, 19, 21
			},
			{
			        54240, 220133, -2954, 25, 27, 28, 30
			},
			{
			        54240, 218073, -2954, 26, 29, 28, 31
			},
			{
			        55265, 219095, -2954, 33, 28, 31, 30
			},
			{
			        56289, 220133, -2954, 32, 35, 30, 33
			},
			{
			        56289, 218073, -2954, 34, 36, 31, 33
			}
	};
	
	private static final int[][] CANDLE_SPAWN =
	{
	        {
	                53313, 220133, -3498
			},
			{
			        53313, 218079, -3498
			},
			{
			        54240, 221045, -3498
			},
			{
			        54325, 219095, -3498
			},
			{
			        54240, 217155, -3498
			},
			{
			        55257, 220028, -3498
			},
			{
			        55257, 218172, -3498
			},
			{
			        56280, 221045, -3498
			},
			{
			        56195, 219095, -3498
			},
			{
			        56280, 217155, -3498
			},
			{
			        57215, 220133, -3498
			},
			{
			        57215, 218079, -3498
			},
			{
			        53313, 220133, -3226
			},
			{
			        53313, 218079, -3226
			},
			{
			        54240, 221045, -3226
			},
			{
			        54325, 219095, -3226
			},
			{
			        54240, 217155, -3226
			},
			{
			        55257, 220028, -3226
			},
			{
			        55257, 218172, -3226
			},
			{
			        56280, 221045, -3226
			},
			{
			        56195, 219095, -3226
			},
			{
			        56280, 217155, -3226
			},
			{
			        57215, 220133, -3226
			},
			{
			        57215, 218079, -3226
			},
			{
			        53313, 220133, -2954
			},
			{
			        53313, 218079, -2954
			},
			{
			        54240, 221045, -2954
			},
			{
			        54325, 219095, -2954
			},
			{
			        54240, 217155, -2954
			},
			{
			        55257, 220028, -2954
			},
			{
			        55257, 218172, -2954
			},
			{
			        56280, 221045, -2954
			},
			{
			        56195, 219095, -2954
			},
			{
			        56280, 217155, -2954
			},
			{
			        57215, 220133, -2954
			},
			{
			        57215, 218079, -2954
			},
	};
	
	public ZakenDay()
	{
		super(133, 135);
		
		addStartNpc(32713);
		addTalkId(32713);
		addFirstTalkId(32705);
		addKillId(29176, 29181);
	}
	
	private synchronized void enterInstance(Player player, Npc npc, boolean is83)
	{
		if (enterReflection(player, npc, is83 ? 135 : 133))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.setParam("startTime", System.currentTimeMillis());
				r.setParam("blueFounded", 0);
				spawnCandles(r);
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
			if (getTimeHour() <= 4)
			{
				player.sendMessage((new ServerMessage("Zaken.INVALID_TIME", player.getLang())).toString());
				return false;
			}
		}
		return super.checkConditions(player, npc, template);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("60"))
		{
			enterInstance(player, npc, false);
		}
		else if (event.equalsIgnoreCase("83"))
		{
			enterInstance(player, npc, true);
		}
		else
		{
			final var r = npc.getReflection();
			if (isInReflection(r))
			{
				final long burnDelay = r.getParams().getLong("burnCandleDelay") * 1000L;
				final long zakenDelay = r.getParams().getLong("zakenSpawnDelay") * 1000L;
				final var is83 = r.getTemplateId() == 135;
				switch (event)
				{
					case "burn_good_candle" :
						if (npc.getRightHandItem() == 0)
						{
							npc.setRHandId(15280);
							npc.setDisplayEffect(1);
							startQuestTimer("burn_blue_candle", burnDelay, npc, player);
							final int blueFounded = r.getParams().getInteger("blueFounded", 0);
							if (blueFounded >= 4)
							{
								startQuestTimer("spawn_zaken", zakenDelay, npc, player);
							}
						}
						break;
					case "burn_blue_candle" :
						if (npc.getRightHandItem() == 15280)
						{
							npc.setRHandId(15302);
							npc.setDisplayEffect(3);
						}
						break;
					case "burn_bad_candle" :
						if (npc.getRightHandItem() == 0)
						{
							npc.setRHandId(15280);
							npc.setDisplayEffect(1);
							startQuestTimer("burn_red_candle", burnDelay, npc, player);
						}
						break;
					case "burn_red_candle" :
						if (npc.getRightHandItem() == 15280)
						{
							npc.setRHandId(15281);
							final int room = getRoomByCandle(npc);
							npc.setDisplayEffect(2);
							r.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_CANDLES_CAN_LEAD_YOU_TO_ZAKEN_DESTROY_HIM, 2, 6000));
							spawnInRoom(is83 ? 29182 : 29023, room, player, r);
							spawnInRoom(is83 ? 29183 : 29024, room, player, r);
							spawnInRoom(is83 ? 29185 : 29027, room, player, r);
							spawnInRoom(is83 ? 29184 : 29026, room, player, r);
						}
						break;
					case "spawn_zaken" :
						final int isSpawned = r.getParams().getInteger("isSpawned", 0);
						if (isSpawned <= 0)
						{
							r.setParam("isSpawned", 1);
							final int zakenRoom = r.getParams().getInteger("zakenRoom", 0);
							if (is83)
							{
								r.broadcastPacket(new ExShowScreenMessage(NpcStringId.WHO_DARES_AWKAWEN_THE_MIGHTY_ZAKEN, 2, 6000));
							}
							spawnInRoom(is83 ? 29181 : 29176, zakenRoom, player, r);
							spawnInRoom(is83 ? 29182 : 29023, zakenRoom, player, r);
							spawnInRoom(is83 ? 29185 : 29027, zakenRoom, player, r);
							spawnInRoom(is83 ? 29184 : 29026, zakenRoom, player, r);
							spawnInRoom(is83 ? 29183 : 29024, zakenRoom, player, r);
						}
						break;
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			finishInstance(r, true);
			if (r.getTemplateId() == 135)
			{
				final long finishDiff = System.currentTimeMillis() - r.getParams().getLong("startTime", 0);
				if (finishDiff <= 900000)
				{
					for (final var player : r.getReflectionPlayers())
					{
						timebonus(r, npc, player, finishDiff);
					}
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final boolean isBlue = npc.getVariables().getInteger("isBlue", 0) == 1;
			if (npc.isScriptValue(0))
			{
				npc.setScriptValue(1);
				if (isBlue)
				{
					final int blueFounded = r.getParams().getInteger("blueFounded", 0) + 1;
					r.setParam("blueFounded", blueFounded);
					startQuestTimer("burn_good_candle", 500, npc, player);
				}
				else
				{
					startQuestTimer("burn_bad_candle", 500, npc, player);
				}
			}
		}
		return null;
	}
	
	private void timebonus(Reflection r, Npc npc, Player player, long finishDiff)
	{
		if (player.isInsideRadius(npc, 2000, false, false))
		{
			final int rand = getRandom(100);
			if (finishDiff <= 300000)
			{
				if (rand < 50)
				{
					player.addItem("Zaken", 15763, 1, npc, true);
				}
			}
			else if (finishDiff <= 600000)
			{
				if (rand < 30)
				{
					player.addItem("Zaken", 15764, 1, npc, true);
				}
			}
			else if (finishDiff <= 900000)
			{
				if (rand < 25)
				{
					player.addItem("Zaken", 15763, 1, npc, true);
				}
			}
		}
	}
	
	private void spawnCandles(Reflection r)
	{
		final List<Npc> candles = new ArrayList<>();
		final var isRandomRoom = r.getParams().getBool("isRandomZakenRoom");
		final int zakenRoom = isRandomRoom ? getRandom(1, 15) : 1;
		r.setParam("zakenRoom", zakenRoom);
		for (int i = 0; i < 36; i++)
		{
			final var candle = addSpawn(32705, CANDLE_SPAWN[i][0], CANDLE_SPAWN[i][1], CANDLE_SPAWN[i][2], 0, false, 0, false, r);
			candle.getVariables().set("candleId", i + 1);
			candles.add(candle);
		}
		
		for (int i = 3; i < 7; i++)
		{
			candles.get(ROOM_SPAWN[zakenRoom - 1][i] - 1).getVariables().set("isBlue", 1);
		}
	}
	
	private int getRoomByCandle(Npc npc)
	{
		final int candleId = npc.getVariables().getInteger("candleId", 0);
		for (int i = 0; i < 15; i++)
		{
			if ((ROOM_SPAWN[i][3] == candleId) || (ROOM_SPAWN[i][4] == candleId))
			{
				return i + 1;
			}
		}
		
		if ((candleId == 6) || (candleId == 7))
		{
			return 3;
		}
		else if ((candleId == 18) || (candleId == 19))
		{
			return 8;
		}
		else if ((candleId == 30) || (candleId == 31))
		{
			return 13;
		}
		return 0;
	}
	
	private void spawnInRoom(int npcId, int roomId, Player player, Reflection r)
	{
		if ((player != null) && (npcId != 29176) && (npcId != 29181))
		{
			final var mob = addSpawn(npcId, ROOM_SPAWN[roomId - 1][0] + getRandom(350), ROOM_SPAWN[roomId - 1][1] + getRandom(350), ROOM_SPAWN[roomId - 1][2], 0, false, 0, false, r);
			mob.setRunning();
			mob.setTarget(player);
			((Attackable) mob).addDamageHate(player, 0, 999);
			mob.getAI().setIntention(CtrlIntention.ATTACK, player);
		}
		else
		{
			addSpawn(npcId, ROOM_SPAWN[roomId - 1][0], ROOM_SPAWN[roomId - 1][1], ROOM_SPAWN[roomId - 1][2], 0, false, 0, false, r);
		}
	}
	
	private int getTimeHour()
	{
		return (GameTimeController.getInstance().getGameTime() / 60) % 24;
	}
	
	void main()
	{
		new ZakenDay();
	}
}