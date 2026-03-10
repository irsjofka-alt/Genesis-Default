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

import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.player.impl.TeleportTask;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.quest.QuestState;
import gameserver.model.zone.ZoneType;
import gameserver.network.NpcStringId;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExStartScenePlayer;
import gameserver.network.serverpackets.MagicSkillUse;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.network.serverpackets.NpcSay;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Util;
import scripts.quests._195_SevenSignSecretRitualOfThePriests;

/**
 * Rework by LordWinter 27.10.2020
 */
public final class SecretRitualOfThePriests extends AbstractReflection
{
	private static final int[][] MOVING_GUARDS =
	{
	        {
	                18835, -75048, 212116, -7312, -74842, 212116, -7312
			},
			{
			        18835, -75371, 212116, -7312, -75628, 212116, -7312
			},
			{
			        18835, -74480, 212116, -7312, -74253, 212116, -7312
			},
			{
			        18835, -74703, 211466, -7312, -74703, 211172, -7312
			},
			{
			        18835, -75197, 211466, -7312, -75197, 211172, -7312
			},
			{
			        18834, -75245, 210148, -7415, -74677, 210148, -7415
			},
			{
			        18834, -74683, 209819, -7415, -75241, 209819, -7415
			},
			{
			        18834, -74224, 208285, -7511, -74498, 208285, -7511
			},
			{
			        18834, -74202, 207063, -7509, -74508, 207063, -7509
			},
			{
			        18834, -74954, 206671, -7511, -74954, 206356, -7511
			},
			{
			        18834, -74270, 206518, -7511, -75654, 206518, -7511
			},
			{
			        18834, -75412, 206894, -7504, -75699, 206894, -7504
			},
			{
			        18834, -75553, 208838, -7511, -75553, 207660, -7511
			},
			{
			        18834, -76390, 207855, -7607, -76623, 207855, -7607
			},
			{
			        18834, -76610, 208182, -7606, -76392, 208182, -7606
			},
			{
			        18834, -76384, 208832, -7606, -76620, 208832, -7606
			},
			{
			        18834, -76914, 209443, -7610, -76914, 209195, -7610
			},
			{
			        18834, -77188, 209191, -7607, -77188, 209440, -7607
			},
			{
			        18835, -78039, 208472, -7703, -77369, 208472, -7703
			},
			{
			        18835, -77703, 208231, -7701, -77703, 207284, -7701
			},
			{
			        18835, -77304, 208027, -7701, -76979, 208027, -7703
			},
			{
			        18835, -77044, 207796, -7701, -78350, 207796, -7704
			},
			{
			        18835, -78085, 208038, -7701, -78454, 208038, -7703
			},
			{
			        18835, -77336, 207413, -7702, -77032, 207112, -7703
			},
			{
			        18834, -78894, 206130, -7893, -78729, 206298, -7893
			},
			{
			        18834, -79050, 206272, -7893, -78874, 206442, -7893
			},
			{
			        18834, -79360, 206372, -7893, -79360, 206718, -7893
			},
			{
			        18834, -78910, 205582, -7893, -78748, 205416, -7893
			},
			{
			        18834, -79057, 205436, -7893, -78899, 205275, -7893
			},
			{
			        18834, -79361, 205336, -7893, -79363, 204998, -7893
			},
			{
			        18834, -79655, 205440, -7893, -79820, 205273, -7893
			},
			{
			        18834, -79802, 205579, -7893, -79964, 205415, -7893
			},
			{
			        18834, -79792, 206111, -7893, -79964, 206295, -7893
			},
			{
			        18834, -79648, 206258, -7893, -79814, 206430, -7893
			},
			{
			        27351, -81963, 205857, -7989, -81085, 205857, -7989
			}
	};

	private SecretRitualOfThePriests()
	{
		super(111);

		addStartNpc(32575, 32577, 32578);
		addTalkId(32575, 32577, 32578);
		
		addEnterZoneId(20500, 20501, 20502);
		
		addAggroRangeEnterId(18834, 18835, 27351);
		addAttackId(18834, 18835, 27351);
	}
	
	public final synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 111))
		{
			final var html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile(player, player.getLang(), "data/html/scripts/quests/" + _195_SevenSignSecretRitualOfThePriests.class.getSimpleName() + "/32575-2.htm");
			player.sendPacket(html);
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				spawnMovingGuards(r);
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
	protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		if (!player.isTransformed() || (player.isTransformed() && player.getTransformationId() != 113))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/" + _195_SevenSignSecretRitualOfThePriests.class.getSimpleName() + "/32575-1.htm");
			player.sendPacket(html);
			return false;
		}
		return super.checkSoloType(player, npc, template);
	}
	
	@Override
	public final String onEnterZone(Creature character, ZoneType zone)
	{
		if (character.isPlayer())
		{
			final var r = character.getReflection();
			if (isInReflection(r))
			{
				switch (zone.getId())
				{
					case 20500 :
						if (r.isStatus(0))
						{
							r.incStatus();
						}
						break;
					case 20501 :
						if (r.isStatus(1))
						{
							r.incStatus();
						}
						break;
					case 20502 :
						if (r.isStatus(2))
						{
							r.incStatus();
						}
						break;
				}
			}
		}
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("openDoor"))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				openDoor(player, r);
				final var html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player, player.getLang(), "data/html/scripts/quests/" + _195_SevenSignSecretRitualOfThePriests.class.getSimpleName() + "/32577-1.htm");
				player.sendPacket(html);
			}
		}
		return null;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (npc.getId() == 32578)
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				openDoor(player, r);
				final var html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player, player.getLang(), "data/html/scripts/quests/" + _195_SevenSignSecretRitualOfThePriests.class.getSimpleName() + "/32578-0.htm");
				player.sendPacket(html);
			}
		}
		else if (npc.getId() == 32575)
		{
			enterInstance(player, npc);
		}
		return null;
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		handleReturnMagic(player, npc);
		return super.onAggroRangeEnter(npc, player, isSummon);
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		handleReturnMagic(attacker, npc);
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	private void handleReturnMagic(Player player, Npc npc)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (npc.isScriptValue(0))
			{
				npc.setScriptValue(1);
				switch (npc.getId())
				{
					case 18834 :
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.HOW_DARE_YOU_INTRUDE_WITH_THAT_TRANSFORMATION_GET_LOST));
						break;
					case 18835 :
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.INTRUDER_PROTECT_THE_PRIESTS_OF_DAWN));
						break;
					case 27351 :
						npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.WHO_ARE_YOU_A_NEW_FACE_LIKE_YOU_CANT_APPROACH_THIS_PLACE));
						break;
				}
			
				if (!npc.isCastingNow())
				{
					npc.broadcastPacketToOthers(2000, new MagicSkillUse(npc, player, 5978, 1, 2400, 0));
				}
				
				Location loc = switch (r.getStatus())
				{
					case 0  -> new Location(-76158, 213412, -7120);
					case 1  -> new Location(-74959, 209240, -7472);
					case 2  -> new Location(-77706, 208994, -7616);
					case 3  -> new Location(-80176, 205855, -7893);
					default -> null;
				};
				player.getPersonalTasks().addTask(new TeleportTask(1000, loc, r));
				r.addTimer("RETURN", ThreadPoolManager.getInstance().schedule(() -> returnTask(npc, r), 5000));
			}
		}
	}
	
	private void spawnMovingGuards(Reflection r)
	{
		var i = 0;
		for (final int[] element : MOVING_GUARDS)
		{
			i++;
			final var npc = addSpawn(element[0], element[1], element[2], element[3], 0, false, 0, false, r);
			if (npc != null)
			{
				r.addTimer("MOVING_GUARDS_" + i + "", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> moveTask(npc, r, element[1], element[2], element[3], element[4], element[5], element[6]), getRandom(1000, 5000), getMoveDelay(element)));
			}
		}
	}
	
	private void moveTask(Npc npc, Reflection r, int x1, int y1, int z1, int x2, int y2, int z2)
	{
		if (r != null && npc != null)
		{
			if (npc.getDistance(x1, y1) <= 40)
			{
				npc.getAI().setIntention(CtrlIntention.MOVING, new Location(x2, y2, z2, 0), 0);
			}
			else if (npc.getDistance(x2, y2) <= 40)
			{
				npc.getAI().setIntention(CtrlIntention.MOVING, new Location(x1, y1, z1, 0), 0);
			}
			else
			{
				npc.getAI().setIntention(CtrlIntention.MOVING, new Location(x1, y1, z1, 0), 0);
			}
		}
	}
	
	private void returnTask(Npc npc, Reflection r)
	{
		if (r != null && npc != null)
		{
			npc.setWalking();
			npc.setScriptValue(0);
			npc.getAI().setIntention(CtrlIntention.MOVING, new Location(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), npc.getSpawn().getHeading()), 0);
		}
	}
	
	private synchronized void openDoor(Player player, Reflection r)
	{
		for (final var door : r.getDoors())
		{
			switch (door.getDoorId())
			{
				case 17240001 :
					for (final var pc : World.getAroundPlayers(door, 500, 200))
					{
						if ((pc == player) && door.isClosed())
						{
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USING_INVISIBLE_SKILL_SNEAK_IN));
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MALE_GUARDS_CAN_DETECT_FEMALE_CANT));
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FEMALE_GUARDS_NOTICE_FROM_FAR_AWAY_BEWARE));
							door.openMe();
						}
					}
					break;
				case 17240003 :
					for (final var pc : World.getAroundPlayers(door, 500, 200))
					{
						if ((pc == player) && door.isClosed())
						{
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DOOR_IS_ENTRANCE_APPROACH_DEVICE));
							door.openMe();
							player.showQuestMovie(ExStartScenePlayer.SSQ_RITUAL_OF_PRIEST);
						}
					}
					break;
				case 17240005 :
					for (final var pc : World.getAroundPlayers(door, 500, 200))
					{
						if ((pc == player) && door.isClosed())
						{
							door.openMe();
						}
					}
					break;
			}
		}
	}
	
	private static int getMoveDelay(int[] array)
	{
		return (int) ((Util.calculateDistance(array[1], array[2], array[4], array[5]) / 50) * 1000) + 200;
	}

	public static void main(String[] args)
	{
		new SecretRitualOfThePriests();
	}
}