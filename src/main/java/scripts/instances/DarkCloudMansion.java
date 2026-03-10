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

import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.quest.QuestState;
import gameserver.model.skills.Skill;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.MagicSkillUse;
import gameserver.network.serverpackets.NpcSay;

/**
 * Rework by LordWinter 02.10.2020
 */
public class DarkCloudMansion extends AbstractReflection
{
	private class DMCNpc
	{
		public Npc npc;
		public boolean isDead = false;
		public Npc golem = null;
		public int status = 0;
		public int order = 0;
		public int count = 0;
	}
	
	private class DMCRoom
	{
		public List<DMCNpc> npcList = new ArrayList<>();
		public boolean isInCalc = false;
		public boolean isEnd = false;
		public int counter = 0;
		public int reset = 0;
		public int founded = 0;
		public int[] Order;
	}
	
	private static int[] BM =
	{
	        22272, 22273, 22274
	};
	
	private static int[] BS =
	{
	        18371, 18372, 18373, 18374, 18375, 18376, 18377
	};
	
	private static NpcStringId[] _spawnChat =
	{
	        NpcStringId.IM_THE_REAL_ONE, NpcStringId.PICK_ME, NpcStringId.TRUST_ME, NpcStringId.NOT_THAT_DUDE_IM_THE_REAL_ONE, NpcStringId.DONT_BE_FOOLED_DONT_BE_FOOLED_IM_THE_REAL_ONE
	};
	
	private static NpcStringId[] _decayChat =
	{
	        NpcStringId.IM_THE_REAL_ONE_PHEW, NpcStringId.CANT_YOU_EVEN_FIND_OUT, NpcStringId.FIND_ME
	};
	
	private static NpcStringId[] _successChat =
	{
	        NpcStringId.HUH_HOW_DID_YOU_KNOW_IT_WAS_ME, NpcStringId.EXCELLENT_CHOICE_TEEHEE, NpcStringId.YOUVE_DONE_WELL, NpcStringId.OH_VERY_SENSIBLE
	};
	
	private static NpcStringId[] _faildChat =
	{
	        NpcStringId.YOUVE_BEEN_FOOLED, NpcStringId.SORRY_BUT_IM_THE_FAKE_ONE
	};
	
	private static int[][] MonolithOrder = new int[][]
	{
	        {
	                1, 2, 3, 4, 5, 6
			},
	        {
	                6, 5, 4, 3, 2, 1
			},
	        {
	                4, 5, 6, 3, 2, 1
			},
	        {
	                2, 6, 3, 5, 1, 4
			},
	        {
	                4, 1, 5, 6, 2, 3
			},
	        {
	                3, 5, 1, 6, 2, 4
			},
	        {
	                6, 1, 3, 4, 5, 2
			},
	        {
	                5, 6, 1, 2, 4, 3
			},
	        {
	                5, 2, 6, 3, 4, 1
			},
	        {
	                1, 5, 2, 6, 3, 4
			},
	        {
	                1, 2, 3, 6, 5, 4
			},
	        {
	                6, 4, 3, 1, 5, 2
			},
	        {
	                3, 5, 2, 4, 1, 6
			},
	        {
	                3, 2, 4, 5, 1, 6
			},
	        {
	                5, 4, 3, 1, 6, 2
			},
	};
	
	private static int[][] GolemSpawn = new int[][]
	{
	        {
	                18369, 148060, 181389
			},
	        {
	                18370, 147910, 181173
			},
	        {
	                18369, 147810, 181334
			},
	        {
	                18370, 147713, 181179
			},
	        {
	                18369, 147569, 181410
			},
	        {
	                18370, 147810, 181517
			},
	        {
	                18369, 147805, 181281
			}
	};
	
	private static int[][] ColumnRows = new int[][]
	{
	        {
	                1, 1, 0, 1, 0
			},
	        {
	                0, 1, 1, 0, 1
			},
	        {
	                1, 0, 1, 1, 0
			},
	        {
	                0, 1, 0, 1, 1
			},
	        {
	                1, 0, 1, 0, 1
			}
	};
	
	private static int[][] Beleths = new int[][]
	{
	        {
	                1, 0, 1, 0, 1, 0, 0
			},
	        {
	                0, 0, 1, 0, 1, 1, 0
			},
	        {
	                0, 0, 0, 1, 0, 1, 1
			},
	        {
	                1, 0, 1, 1, 0, 0, 0
			},
	        {
	                1, 1, 0, 0, 0, 1, 0
			},
	        {
	                0, 1, 0, 1, 0, 1, 0
			},
	        {
	                0, 0, 0, 1, 1, 1, 0
			},
	        {
	                1, 0, 1, 0, 0, 1, 0
			},
	        {
	                0, 1, 1, 0, 0, 0, 1
			}
	};
	
	public DarkCloudMansion()
	{
		super(9);
		
		addFirstTalkId(32291, 32324);
		addStartNpc(32282);
		addTalkId(32282, 32291);
		addAttackId(18369, 18370, 18371, 18372, 18373, 18374, 18375, 18376, 18377, 22402);
		addKillId(18371, 18372, 18373, 18374, 18375, 18376, 18377, 22318, 22319, 22272, 22273, 22274, 18369, 18370, 22402, 22264);
	}
	
	private synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 9))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				runStartRoom(r);
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
	
	private void runStartRoom(Reflection r)
	{
		r.setStatus(0);
		final var startRoom = new DMCRoom();
		DMCNpc thisnpc;
		
		thisnpc = new DMCNpc();
		thisnpc.npc = addSpawn(22272, 146817, 180335, -6117, 0, false, 0, false, r);
		startRoom.npcList.add(thisnpc);
		thisnpc.npc.setIsNoRndWalk(true);
		
		thisnpc = new DMCNpc();
		thisnpc.npc = addSpawn(22272, 146741, 180589, -6117, 0, false, 0, false, r);
		startRoom.npcList.add(thisnpc);
		thisnpc.npc.setIsNoRndWalk(true);
		r.setParam("startRoom", startRoom);
	}
	
	private void spawnHall(Reflection r)
	{
		if (r != null)
		{
			final var hall = new DMCRoom();
			DMCNpc thisnpc;
			r.setParam("hall", null);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22273, 147217, 180112, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			hall.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22274, 147217, 180209, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			hall.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22273, 148521, 180112, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			hall.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22272, 148521, 180209, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			hall.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22273, 148525, 180910, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			hall.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22274, 148435, 180910, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			hall.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22273, 147242, 180910, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			hall.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22274, 147242, 180819, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			hall.npcList.add(thisnpc);
			r.setParam("hall", hall);
		}
	}
	
	private void runHall(Reflection r)
	{
		if (r != null)
		{
			spawnHall(r);
			r.setStatus(1);
			r.openDoor(24230001);
		}
	}
	
	private void runFirstRoom(Reflection r)
	{
		if (r != null)
		{
			final var firstRoom = new DMCRoom();
			DMCNpc thisnpc;
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22264, 147842, 179837, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			firstRoom.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22264, 147711, 179708, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			firstRoom.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22264, 147842, 179552, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			firstRoom.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(22264, 147964, 179708, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			firstRoom.npcList.add(thisnpc);
			
			r.setParam("firstRoom", firstRoom);
			r.setStatus(2);
			r.openDoor(24230002);
		}
	}
	
	private void runHall2(Reflection r)
	{
		if (r != null)
		{
			addSpawn(32288, 147818, 179643, -6117, 0, false, 0, false, r);
			spawnHall(r);
			r.setStatus(3);
		}
	}
	
	private void runSecondRoom(Reflection r)
	{
		if (r != null)
		{
			final var secondRoom = new DMCRoom();
			DMCNpc thisnpc;
			
			secondRoom.Order = new int[7];
			secondRoom.Order[0] = 1;
			for (int i = 1; i < 7; i++)
			{
				secondRoom.Order[i] = 0;
			}
			
			final int i = getRandom(MonolithOrder.length);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(32324, 147800, 181150, -6117, 0, false, 0, false, r);
			thisnpc.order = MonolithOrder[i][0];
			secondRoom.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(32324, 147900, 181215, -6117, 0, false, 0, false, r);
			thisnpc.order = MonolithOrder[i][1];
			secondRoom.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(32324, 147900, 181345, -6117, 0, false, 0, false, r);
			thisnpc.order = MonolithOrder[i][2];
			secondRoom.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(32324, 147800, 181410, -6117, 0, false, 0, false, r);
			thisnpc.order = MonolithOrder[i][3];
			secondRoom.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(32324, 147700, 181345, -6117, 0, false, 0, false, r);
			thisnpc.order = MonolithOrder[i][4];
			secondRoom.npcList.add(thisnpc);
			
			thisnpc = new DMCNpc();
			thisnpc.npc = addSpawn(32324, 147700, 181215, -6117, 0, false, 0, false, r);
			thisnpc.order = MonolithOrder[i][5];
			secondRoom.npcList.add(thisnpc);
			
			r.setParam("secondRoom", secondRoom);
			r.setStatus(4);
			r.openDoor(24230005);
		}
	}
	
	private void runHall3(Reflection r)
	{
		if (r != null)
		{
			addSpawn(32289, 147808, 181281, -6117, 16383, false, 0, false, r);
			spawnHall(r);
			r.setStatus(5);
		}
	}
	
	private void runThirdRoom(Reflection r)
	{
		if (r != null)
		{
			final var thirdRoom = new DMCRoom();
			final var thisnpc = new DMCNpc();
			thisnpc.isDead = false;
			thisnpc.npc = addSpawn(22273, 148765, 180450, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			thisnpc.npc = addSpawn(22274, 148865, 180190, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			thisnpc.npc = addSpawn(22273, 148995, 180190, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			thisnpc.npc = addSpawn(22272, 149090, 180450, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			thisnpc.npc = addSpawn(22273, 148995, 180705, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			thisnpc.npc = addSpawn(22274, 148865, 180705, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			r.setParam("thirdRoom", thirdRoom);
			r.setStatus(6);
			r.openDoor(24230003);
		}
	}
	
	private void runThirdRoom2(Reflection r)
	{
		if (r != null)
		{
			addSpawn(32290, 148910, 178397, -6117, 16383, false, 0, false, r);
			final var thirdRoom = new DMCRoom();
			final var thisnpc = new DMCNpc();
			thisnpc.isDead = false;
			thisnpc.npc = addSpawn(22273, 148765, 180450, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			thisnpc.npc = addSpawn(22274, 148865, 180190, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			thisnpc.npc = addSpawn(22273, 148995, 180190, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			thisnpc.npc = addSpawn(22272, 149090, 180450, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			thisnpc.npc = addSpawn(22273, 148995, 180705, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			thisnpc.npc = addSpawn(22274, 148865, 180705, -6117, 0, false, 0, false, r);
			thisnpc.npc.setIsNoRndWalk(true);
			thirdRoom.npcList.add(thisnpc);
			r.setParam("thirdRoom2", thirdRoom);
			r.setStatus(8);
		}
	}
	
	private void runForthRoom(Reflection r)
	{
		if (r != null)
		{
			final DMCRoom forthRoom = new DMCRoom();
			forthRoom.counter = 0;
			DMCNpc thisnpc;
			final int temp[] = new int[7];
			final int templist[][] = new int[7][5];
			int xx = 0;
			
			for (int i = 0; i < 7; i++)
			{
				temp[i] = getRandom(ColumnRows.length);
			}
			
			for (int i = 0; i < 7; i++)
			{
				templist[i] = ColumnRows[temp[i]];
			}
			
			for (int x = 148660; x < 149285; x += 125)
			{
				int yy = 0;
				for (int y = 179280; y > 178405; y -= 125)
				{
					thisnpc = new DMCNpc();
					thisnpc.npc = addSpawn(22402, x, y, -6115, 16215, false, 0, false, r);
					thisnpc.status = templist[yy][xx];
					thisnpc.order = yy;
					forthRoom.npcList.add(thisnpc);
					yy++;
				}
				xx++;
			}
			
			for (final var npc : forthRoom.npcList)
			{
				if (npc.status == 0)
				{
					npc.npc.setIsInvul(true);
				}
			}
			
			r.setParam("forthRoom", forthRoom);
			r.setStatus(7);
			r.openDoor(24230004);
		}
	}
	
	private void runFifthRoom(Reflection r)
	{
		if (r != null)
		{
			spawnFifthRoom(r);
			r.setStatus(9);
			r.openDoor(24230006);
		}
	}
	
	private void spawnFifthRoom(Reflection r)
	{
		if (r != null)
		{
			int idx = 0;
			int temp[] = new int[6];
			final var fifthRoom = new DMCRoom();
			DMCNpc thisnpc;
			
			temp = Beleths[getRandom(Beleths.length)];
			
			fifthRoom.reset = 0;
			fifthRoom.founded = 0;
			fifthRoom.isInCalc = false;
			
			for (int x = 148720; x < 149175; x += 65)
			{
				thisnpc = new DMCNpc();
				thisnpc.npc = addSpawn(BS[idx], x, 182145, -6117, 48810, false, 0, false, r);
				thisnpc.npc.setIsNoRndWalk(true);
				thisnpc.order = idx;
				thisnpc.status = temp[idx];
				thisnpc.count = 0;
				fifthRoom.npcList.add(thisnpc);
				if (temp[idx] == 1 && getRandom(100) < 95)
				{
					thisnpc.npc.broadcastPacketToOthers(2000, new NpcSay(thisnpc.npc.getObjectId(), 0, thisnpc.npc.getId(), _spawnChat[getRandom(_spawnChat.length)]));
				}
				else if (temp[idx] != 1 && getRandom(100) < 67)
				{
					thisnpc.npc.broadcastPacketToOthers(2000, new NpcSay(thisnpc.npc.getObjectId(), 0, thisnpc.npc.getId(), _spawnChat[getRandom(_spawnChat.length)]));
				}
				idx++;
			}
			r.setParam("fifthRoom", fifthRoom);
		}
	}
	
	private boolean checkKillProgress(Npc npc, DMCRoom room)
	{
		boolean cont = true;
		if (room != null)
		{
			for (final var npcobj : room.npcList)
			{
				if (npcobj.npc == npc)
				{
					npcobj.isDead = true;
				}
				if (npcobj.isDead == false)
				{
					cont = false;
				}
			}
		}
		return cont;
	}
	
	private void spawnRndGolem(Reflection r, DMCNpc npc)
	{
		if (r != null)
		{
			if (npc.golem != null)
			{
				return;
			}
			
			final int i = getRandom(GolemSpawn.length);
			final int mobId = GolemSpawn[i][0];
			final int x = GolemSpawn[i][1];
			final int y = GolemSpawn[i][2];
			
			npc.golem = addSpawn(mobId, x, y, -6117, 0, false, 0, false, r);
			npc.golem.setIsNoRndWalk(true);
		}
	}
	
	private void checkStone(Npc npc, int order[], DMCNpc npcObj, Reflection r)
	{
		if (r != null)
		{
			for (int i = 1; i < 7; i++)
			{
				if (order[i] == 0 && order[i - 1] != 0)
				{
					if (npcObj.order == i && npcObj.status == 0)
					{
						order[i] = 1;
						npcObj.status = 1;
						npcObj.isDead = true;
						npc.broadcastPacketToOthers(new MagicSkillUse(npc, npc, 5441, 1, 1, 0));
						return;
					}
				}
			}
			spawnRndGolem(r, npcObj);
		}
	}
	
	private void endInstance(Reflection r)
	{
		if (r != null)
		{
			final var rifthRoom = r.getParams().getObject("fifthRoom", DMCRoom.class);
			if (rifthRoom != null)
			{
				for (final var mob : rifthRoom.npcList)
				{
					mob.npc.decayMe();
				}
				r.setStatus(10);
				addSpawn(32291, 148911, 181940, -6117, 16383, false, 0, false, r);
				rifthRoom.npcList.clear();
			}
		}
	}
	
	private void checkBelethSample(Reflection r, Npc npc, Player player)
	{
		if (r != null)
		{
			final var rifthRoom = r.getParams().getObject("fifthRoom", DMCRoom.class);
			if (rifthRoom == null)
			{
				return;
			}
			
			if (rifthRoom.isInCalc && rifthRoom.reset == 0)
			{
				if (npc.isAttackable())
				{
					npc.setTarget(null);
					npc.stopMove(null);
					((Attackable) npc).clearAggroList(false);
					npc.getAttackByList().clear();
				}
				return;
			}
			
			if (rifthRoom.reset == 1)
			{
				return;
			}
			
			rifthRoom.isInCalc = true;
			
			for (final var mob : rifthRoom.npcList)
			{
				if (mob.npc != null && mob.npc == npc)
				{
					if (mob.count == 0)
					{
						mob.count = 1;
						if (mob.status == 1)
						{
							mob.npc.broadcastPacketToOthers(2000, new NpcSay(mob.npc.getObjectId(), Say2.NPC_ALL, mob.npc.getId(), _successChat[getRandom(_successChat.length)]));
							rifthRoom.founded += 1;
							mob.count = 2;
							mob.npc.decayMe();
							if (rifthRoom.founded == 3)
							{
								if (!rifthRoom.isEnd)
								{
									rifthRoom.isEnd = true;
									endInstance(r);
								}
							}
						}
						else
						{
							rifthRoom.reset = 1;
							mob.npc.broadcastPacketToOthers(2000, new NpcSay(mob.npc.getObjectId(), Say2.NPC_ALL, mob.npc.getId(), _faildChat[getRandom(_faildChat.length)]));
							startQuestTimer("decayChatBelethSamples", 100, npc, player);
							startQuestTimer("decayBelethSamples", 1000, npc, player);
						}
					}
				}
			}
			
			if (rifthRoom.reset < 1)
			{
				rifthRoom.isInCalc = false;
			}
		}
	}
	
	private void killedBelethSample(Reflection r, Npc npc)
	{
		if (r != null)
		{
			final var rifthRoom = r.getParams().getObject("fifthRoom", DMCRoom.class);
			if (rifthRoom == null)
			{
				return;
			}
			if (rifthRoom.reset == 1)
			{
				npc.decayMe();
				spawnFifthRoom(r);
			}
			else
			{
				if (rifthRoom.reset == 0 && rifthRoom.founded == 3)
				{
					for (final var mob : rifthRoom.npcList)
					{
						if (mob.npc != null)
						{
							mob.npc.decayMe();
						}
					}
					
					if (!rifthRoom.isEnd)
					{
						rifthRoom.isEnd = true;
						endInstance(r);
					}
				}
			}
		}
	}
	
	private boolean allStonesDone(Reflection r)
	{
		if (r != null)
		{
			final var secondRoom = r.getParams().getObject("secondRoom", DMCRoom.class);
			if (secondRoom != null)
			{
				for (final var mob : secondRoom.npcList)
				{
					if (mob == null || mob.isDead)
					{
						continue;
					}
					return false;
				}
			}
		}
		return true;
	}
	
	private void removeMonoliths(Reflection r)
	{
		if (r != null)
		{
			final var secondRoom = r.getParams().getObject("secondRoom", DMCRoom.class);
			if (secondRoom != null)
			{
				for (final var mob : secondRoom.npcList)
				{
					if (mob.npc != null)
					{
						mob.npc.decayMe();
					}
				}
			}
		}
	}
	
	private void chkShadowColumn(Reflection r, Npc npc)
	{
		if (r != null)
		{
			final var forthRoom = r.getParams().getObject("forthRoom", DMCRoom.class);
			if (forthRoom != null)
			{
				for (final var mob : forthRoom.npcList)
				{
					if (mob.npc == npc)
					{
						for (int i = 0; i < 7; i++)
						{
							if (mob.order == i && forthRoom.counter == i)
							{
								r.openDoor(24230007 + i);
								forthRoom.counter += 1;
								if (forthRoom.counter == 7)
								{
									runThirdRoom2(r);
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (npc == null)
		{
			return "";
		}
		
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final var fifthRoom = r.getParams().getObject("fifthRoom", DMCRoom.class);
			if (fifthRoom != null)
			{
				if (event.equalsIgnoreCase("decayBelethSamples"))
				{
					for (final var mob : fifthRoom.npcList)
					{
						if (mob.count == 0)
						{
							mob.npc.decayMe();
							mob.count = 2;
						}
					}
				}
				else if (event.equalsIgnoreCase("decayChatBelethSamples"))
				{
					for (final var mob : fifthRoom.npcList)
					{
						if (mob.status == 1)
						{
							mob.npc.broadcastPacketToOthers(2000, new NpcSay(mob.npc.getObjectId(), Say2.NPC_ALL, mob.npc.getId(), _decayChat[getRandom(_decayChat.length)]));
						}
					}
				}
				else if (event.equalsIgnoreCase("respawnFifth"))
				{
					spawnFifthRoom(r);
				}
			}
		}
		return "";
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (r.isStatus(0))
			{
				if (checkKillProgress(npc, r.getParams().getObject("startRoom", DMCRoom.class)))
				{
					runHall(r);
				}
			}
			if (r.isStatus(1))
			{
				if (checkKillProgress(npc, r.getParams().getObject("hall", DMCRoom.class)))
				{
					runFirstRoom(r);
				}
			}
			if (r.isStatus(2))
			{
				if (checkKillProgress(npc, r.getParams().getObject("firstRoom", DMCRoom.class)))
				{
					runHall2(r);
				}
			}
			if (r.isStatus(3))
			{
				if (checkKillProgress(npc, r.getParams().getObject("hall", DMCRoom.class)))
				{
					runSecondRoom(r);
				}
			}
			if (r.isStatus(4))
			{
				final var secondRoom = r.getParams().getObject("secondRoom", DMCRoom.class);
				if (secondRoom != null)
				{
					for (final var mob : secondRoom.npcList)
					{
						if (mob.golem == npc)
						{
							mob.golem = null;
						}
					}
				}
			}
			if (r.isStatus(5))
			{
				if (checkKillProgress(npc, r.getParams().getObject("hall", DMCRoom.class)))
				{
					runThirdRoom(r);
				}
			}
			if (r.isStatus(6))
			{
				if (checkKillProgress(npc, r.getParams().getObject("thirdRoom", DMCRoom.class)))
				{
					runForthRoom(r);
				}
			}
			if (r.isStatus(7))
			{
				chkShadowColumn(r, npc);
			}
			if (r.isStatus(8))
			{
				if (checkKillProgress(npc, r.getParams().getObject("thirdRoom2", DMCRoom.class)))
				{
					runFifthRoom(r);
				}
			}
			if (r.isStatus(9))
			{
				killedBelethSample(r, npc);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onAttack(Npc npc, Player player, int damage, boolean isSummon, Skill skill)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (r.isStatus(7))
			{
				final var forthRoom = r.getParams().getObject("forthRoom", DMCRoom.class);
				if (forthRoom != null)
				{
					for (final var mob : forthRoom.npcList)
					{
						if (mob.npc == npc)
						{
							if (mob.npc.isInvul() && getRandom(100) < 12)
							{
								addSpawn(BM[getRandom(BM.length)], player.getX(), player.getY(), player.getZ(), 0, false, 0, false, r);
							}
						}
					}
				}
			}
			if (r.isStatus(9))
			{
				checkBelethSample(r, npc, player);
			}
		}
		return super.onAttack(npc, player, damage, isSummon, skill);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (r.isStatus(4))
			{
				final var secondRoom = r.getParams().getObject("secondRoom", DMCRoom.class);
				if (secondRoom != null)
				{
					for (final var mob : secondRoom.npcList)
					{
						if (mob.npc == npc)
						{
							checkStone(npc, secondRoom.Order, mob, r);
						}
					}
				}
				
				if (allStonesDone(r))
				{
					removeMonoliths(r);
					runHall3(r);
				}
			}
			
			if (npc.getId() == 32291 && r.isStatus(10))
			{
				npc.showChatWindow(player);
				QuestState st = player.getQuestState(getName());
				if (st == null)
				{
					st = newQuestState(player);
				}
				
				if (!st.hasQuestItems(9690))
				{
					st.giveItems(9690, 1);
				}
			}
		}
		return "";
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final int npcId = npc.getId();
		if (npcId == 32282)
		{
			enterInstance(player, npc);
		}
		else
		{
			final var r = npc.getReflection();
			if (isInReflection(r))
			{
				if (npcId == 32291)
				{
					if (r.isAllowed(player.getObjectId()))
					{
						r.removeAllowed(player);
					}
					teleportPlayer(player, new Location(139968, 150367, -3111), ReflectionManager.DEFAULT);
					r.collapse();
					return "";
				}
			}
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new DarkCloudMansion();
	}
}