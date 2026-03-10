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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.ArrayUtils;

import l2e.commons.util.Rnd;
import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.SkillsParser;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.GrandBossInstance;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.holders.SkillHolder;
import gameserver.model.skills.Skill;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.EarthQuake;
import gameserver.network.serverpackets.ExShowScreenMessage;
import gameserver.network.serverpackets.GameServerPacket;
import gameserver.network.serverpackets.MagicSkillCanceled;
import gameserver.network.serverpackets.MagicSkillUse;
import gameserver.network.serverpackets.NpcInfo.Info;
import gameserver.network.serverpackets.SocialAction;
import gameserver.network.serverpackets.SpecialCamera;

/**
 * Rework by LordWinter 07.02.2020
 */
public class FinalEmperialTomb extends AbstractReflection
{
	private static class FrintezzaSong
	{
		public SkillHolder skill;
		public SkillHolder effectSkill;
		public NpcStringId songName;
		public int chance;
		
		public FrintezzaSong(SkillHolder sk, SkillHolder esk, NpcStringId sn, int ch)
		{
			skill = sk;
			effectSkill = esk;
			songName = sn;
			chance = ch;
		}
	}

	private static final FrintezzaSong[] FRINTEZZASONGLIST =
	{
	        new FrintezzaSong(new SkillHolder(5007, 1), new SkillHolder(5008, 1), NpcStringId.REQUIEM_OF_HATRED, 5), new FrintezzaSong(new SkillHolder(5007, 2), new SkillHolder(5008, 2), NpcStringId.RONDO_OF_SOLITUDE, 50), new FrintezzaSong(new SkillHolder(5007, 3), new SkillHolder(5008, 3), NpcStringId.FRENETIC_TOCCATA, 70), new FrintezzaSong(new SkillHolder(5007, 4), new SkillHolder(5008, 4), NpcStringId.FUGUE_OF_JUBILATION, 90), new FrintezzaSong(new SkillHolder(5007, 5), new SkillHolder(5008, 5), NpcStringId.HYPNOTIC_MAZURKA, 100),
	};

	private static final int[] FIRST_ROOM_DOORS =
	{
	        17130051, 17130052, 17130053, 17130054, 17130055, 17130056, 17130057, 17130058
	};

	private static final int[] SECOND_ROOM_DOORS =
	{
	        17130061, 17130062, 17130063, 17130064, 17130065, 17130066, 17130067, 17130068, 17130069, 17130070
	};

	private static final int[] FIRST_ROUTE_DOORS =
	{
	        17130042, 17130043
	};

	private static final int[] SECOND_ROUTE_DOORS =
	{
	        17130045, 17130046
	};

	private static final int[][] PORTRAIT_SPAWNS =
	{
	        {
	                29048, -89381, -153981, -9168, 3368, -89378, -153968, -9168, 3368
			},
			{
			        29048, -86234, -152467, -9168, 37656, -86261, -152492, -9168, 37656
			},
			{
			        29049, -89342, -152479, -9168, -5152, -89311, -152491, -9168, -5152
			},
			{
			        29049, -86189, -153968, -9168, 29456, -86217, -153956, -9168, 29456
			}
	};
	
	private static final int[] blockANpcs =
	{
	        18329, 18330, 18331, 18333
	};
	private static final int[] blockBNpcs =
	{
	        18334, 18335, 18336, 18337, 18338
	};

	public FinalEmperialTomb()
	{
		super(136);

		addStartNpc(32011, 29061);
		addTalkId(32011, 29061);
		addAttackId(29046, 29045, 29048, 29049);
		addKillId(18328, 18329, 18330, 18331, 18333, 18334, 18335, 18336, 18337, 18338, 18339, 29047, 29046, 29048, 29049, 29050, 29051);
		addSpellFinishedId(18333);
		addSpawnId(18328, 18329, 18330, 18331, 18333, 18334, 18335, 18336, 18337, 18338, 18339, 29045, 29046, 29047, 29048, 29049, 29050, 29051, 29059);
	}

	private synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 136))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				final List<Npc> demons = new CopyOnWriteArrayList<>();
				r.setParam("demons", demons);
				final List<Npc> portraits = new CopyOnWriteArrayList<>();
				r.setParam("portraits", portraits);
				
				final var party = player.getParty();
				if (player.getParty() == null)
				{
					if (player.getInventory().getItemByItemId(8556) != null)
					{
						player.destroyItemByItemId(getName(), 8556, player.getInventory().getInventoryItemCount(8556, -1), null, true);
					}
				}
				else
				{
					final var cc = player.getParty().getCommandChannel();
					if (cc != null)
					{
						for (final var channelMember : cc.getMembers())
						{
							if (channelMember != null && channelMember.getInventory().getItemByItemId(8556) != null)
							{
								channelMember.destroyItemByItemId(getName(), 8556, channelMember.getInventory().getInventoryItemCount(8556, -1), null, true);
							}
						}
					}
					else
					{
						for (final var member : party.getMembers())
						{
							if (member != null && member.getInventory().getItemByItemId(8556) != null)
							{
								member.destroyItemByItemId(getName(), 8556, member.getInventory().getInventoryItemCount(8556, -1), null, true);
							}
						}
					}
				}
				
				if (r.getParams().getBool("onlyRaidFight", false))
				{
					r.setStatus(3);
					r.addTimer("INTRO", ThreadPoolManager.getInstance().schedule(new IntroTask(r, 0), (r.getParams().getInteger("frintezzaDelay", 0) * 1000L)));
				}
				else
				{
					r.setStatus(1);
					r.spawnByGroup("last_imperial_tomb");
					for (final var n : r.getAliveNpcs(18329, 18330, 18331, 18333, 18334, 18335, 18336, 18337, 18338))
					{
						n.block();
						n.setIsInvul(true);
					}
				}
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
			final var teleLoc = r.getParams().getBool("onlyRaidFight", false) ? new Location(-87771, -151805, -9168) : template.getTeleportCoord();
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
			final var teleLoc = r.getStatus() > 2 ? new Location(-87771, -151805, -9168) : template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
	}
	
	@Override
	public final String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player, npc);
			return null;
		}
		return super.onAdvEvent(event, npc, player);
	}

	private boolean controlStatus(Reflection r, int stage)
	{
		if (r != null)
		{
			switch (stage)
			{
				case 3 :
					r.removeTimer("SONG_EFFECT_TASK");
					r.removeTimer("SONG_TASK");
					final var activeScarlet = r.getParams().getObject("activeScarlet", GrandBossInstance.class);
					if (activeScarlet != null)
					{
						activeScarlet.setIsInvul(true);
						if (activeScarlet.isCastingNow())
						{
							activeScarlet.abortCast();
						}
						handleReenterTime(r);
						activeScarlet.doCast(new SkillHolder(5017, 1).getSkill());
					}
					ThreadPoolManager.getInstance().schedule(new SongTask(r, 2), 1500);
					break;
				case 4 :
					final var isScarletSecondStage = r.getParams().getBool("isScarletSecondStage", false);
					if (!isScarletSecondStage)
					{
						r.setParam("isScarletSecondStage", true);
						r.setParam("isVideo", true);
						r.broadcastPacket(new MagicSkillCanceled(r.getParams().getObject("frintezza", GrandBossInstance.class).getObjectId()));
						r.removeTimer("SONG_EFFECT_TASK");
						ThreadPoolManager.getInstance().schedule(new IntroTask(r, 23), 2000);
						ThreadPoolManager.getInstance().schedule(new IntroTask(r, 24), 2100);
					}
					break;
				case 5 :
					r.setParam("isVideo", true);
					r.broadcastPacket(new MagicSkillCanceled(r.getParams().getObject("frintezza", GrandBossInstance.class).getObjectId()));
					r.removeTimer("SONG_EFFECT_TASK");
					r.removeTimer("SONG_TASK");
					ThreadPoolManager.getInstance().schedule(new IntroTask(r, 33), 500);
					break;
				case 6 :
					for (final int doorId : FIRST_ROOM_DOORS)
					{
						r.openDoor(doorId);
					}
					for (final int doorId : FIRST_ROUTE_DOORS)
					{
						r.openDoor(doorId);
					}
					for (final int doorId : SECOND_ROUTE_DOORS)
					{
						r.openDoor(doorId);
					}
					for (final int doorId : SECOND_ROOM_DOORS)
					{
						r.closeDoor(doorId);
					}
					break;
			}
			return true;
		}
		return false;
	}

	private class DemonSpawnTask implements Runnable
	{
		private final Reflection _r;
		
		protected DemonSpawnTask(Reflection r)
		{
			_r = r;
		}
		
		@Override
		public void run()
		{
			if (_r != null)
			{
				final var portraits = _r.getParams().getList("portraits", Npc.class);
				if (portraits != null && !portraits.isEmpty())
				{
					final var demons = _r.getParams().getList("demons", Npc.class);
					if (demons != null && demons.size() < 24)
    				{
						for (final var npc : portraits)
						{
							final int i = npc.getScriptValue();
							final var demon = (MonsterInstance) addSpawn(PORTRAIT_SPAWNS[i][0] + 2, PORTRAIT_SPAWNS[i][5], PORTRAIT_SPAWNS[i][6], PORTRAIT_SPAWNS[i][7], PORTRAIT_SPAWNS[i][8], false, 0, false, _r);
							demons.add(demon);
						}
    				}
					_r.addTimer("DEMON_SPAWN", ThreadPoolManager.getInstance().schedule(new DemonSpawnTask(_r), 20000));
				}
			}
		}
	}

	private void soulBreakingArrow(Npc npc)
	{
		if (npc != null)
		{
			npc.setScriptValue(0);
		}
	}

	private class SongTask implements Runnable
	{
		private final Reflection _r;
		private final int _status;

		public SongTask(Reflection r, int status)
		{
			_r = r;
			_status = status;
		}

		@Override
		public void run()
		{
			if (_r == null)
			{
				return;
			}
			
			final var isVideo = _r.getParams().getBool("isVideo", false);
			switch (_status)
			{
				case 0 :
					if (isVideo)
					{
						_r.addTimer("SONG_TASK", ThreadPoolManager.getInstance().schedule(new SongTask(_r, 0), 1000));
					}
					else
					{
						final var frintezza = _r.getParams().getObject("frintezza", GrandBossInstance.class);
						if (frintezza != null && !frintezza.isDead())
						{
							if (frintezza.getScriptValue() != 1)
							{
								final int rnd = getRandom(100);
								for (final FrintezzaSong element : FRINTEZZASONGLIST)
								{
									if (rnd < element.chance)
									{
										_r.setParam("OnSong", element);
										_r.broadcastPacket(new ExShowScreenMessage(2, -1, 2, 0, 0, 0, 0, true, 4000, false, null, element.songName, null));
										_r.broadcastPacket(new MagicSkillUse(frintezza, frintezza, element.skill.getId(), element.skill.getLvl(), element.skill.getSkill().getHitTime(), 0));
										_r.addTimer("SONG_EFFECT_TASK", ThreadPoolManager.getInstance().schedule(new SongTask(_r, 1), 3000));
										_r.addTimer("SONG_TASK", ThreadPoolManager.getInstance().schedule(new SongTask(_r, 0), element.skill.getSkill().getHitTime()));
										break;
									}
								}
							}
							else
							{
								_r.addTimer("SOUL_BREAK", ThreadPoolManager.getInstance().schedule(() -> soulBreakingArrow(frintezza), 35000));
							}
						}
					}
					break;
				case 1 :
					final var OnSong = _r.getParams().getObject("OnSong", FrintezzaSong.class);
					if (OnSong != null)
					{
						final var skill = OnSong.effectSkill.getSkill();
						if (skill == null)
						{
							return;
						}

						final var activeScarlet = _r.getParams().getObject("activeScarlet", GrandBossInstance.class);
						if ((activeScarlet == null) || activeScarlet.isDead() || !activeScarlet.isVisible())
						{
							return;
						}
						if (isVideo)
						{
							return;
						}
						activeScarlet.doCast(SkillsParser.getInstance().getInfo(skill.getId(), skill.getLevel()));
					}
					break;
				case 2 :
					final var activeScarlet = _r.getParams().getObject("activeScarlet", GrandBossInstance.class);
					if (activeScarlet != null)
					{
						activeScarlet.setRHandId(7903);
						activeScarlet.setIsInvul(false);
					}
					break;
			}
		}
	}

	private class IntroTask implements Runnable
	{
		private final Reflection _r;
		private final int _status;

		public IntroTask(Reflection r, int status)
		{
			_r = r;
			_status = status;
		}

		@Override
		public void run()
		{
			if (_r == null)
			{
				return;
			}
			
			var overheadDummy = _r.getParams().getObject("overheadDummy", Npc.class);
			var frintezzaDummy = _r.getParams().getObject("frintezzaDummy", Npc.class);
			var portraitDummy1 = _r.getParams().getObject("portraitDummy1", Npc.class);
			var portraitDummy3 = _r.getParams().getObject("portraitDummy3", Npc.class);
			var scarletDummy = _r.getParams().getObject("scarletDummy", Npc.class);
			var frintezza = _r.getParams().getObject("frintezza", GrandBossInstance.class);
			var activeScarlet = _r.getParams().getObject("activeScarlet", GrandBossInstance.class);
			final var demons = _r.getParams().getList("demons", Npc.class);
			final var portraits = _r.getParams().getList("portraits", Npc.class);
			switch (_status)
			{
				case 0 :
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 1), 27000);
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 2), 30000);
					_r.broadcastPacket(new EarthQuake(-87784, -155083, -9087, 45, 27));
					break;
				case 1 :
					for (final int doorId : FIRST_ROOM_DOORS)
					{
						_r.closeDoor(doorId);
					}
					for (final int doorId : FIRST_ROUTE_DOORS)
					{
						_r.closeDoor(doorId);
					}
					for (final int doorId : SECOND_ROOM_DOORS)
					{
						_r.closeDoor(doorId);
					}
					for (final int doorId : SECOND_ROUTE_DOORS)
					{
						_r.closeDoor(doorId);
					}
					addSpawn(29061, -87904, -141296, -9168, 0, false, 0, false, _r);
					break;
				case 2 :
					frintezzaDummy = addSpawn(29052, -87784, -155083, -9087, 16048, false, 0, false, _r);
					_r.setParam("frintezzaDummy", frintezzaDummy);
					frintezzaDummy.setIsInvul(true);
					frintezzaDummy.setIsImmobilized(true);

					overheadDummy = addSpawn(29052, -87784, -153298, -9175, 16384, false, 0, false, _r);
					_r.setParam("overheadDummy", overheadDummy);
					overheadDummy.setIsInvul(true);
					overheadDummy.setIsImmobilized(true);
					overheadDummy.setCollisionHeight(600);
					_r.broadcastPacket(new Info(overheadDummy, null));

					portraitDummy1 = addSpawn(29052, -89566, -153168, -9165, 16048, false, 0, false, _r);
					_r.setParam("portraitDummy1", portraitDummy1);
					portraitDummy1.setIsImmobilized(true);
					portraitDummy1.setIsInvul(true);

					portraitDummy3 = addSpawn(29052, -86004, -153168, -9165, 16048, false, 0, false, _r);
					_r.setParam("portraitDummy3", portraitDummy3);
					portraitDummy3.setIsImmobilized(true);
					portraitDummy3.setIsInvul(true);

					scarletDummy = addSpawn(29053, -87784, -153298, -9175, 16384, false, 0, false, _r);
					_r.setParam("scarletDummy", scarletDummy);
					scarletDummy.setIsInvul(true);
					scarletDummy.setIsImmobilized(true);

					stopPc();
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 3), 1000);
					break;
				case 3 :
					if (overheadDummy != null)
					{
						_r.broadcastPacket(new SpecialCamera(overheadDummy, 0, 75, -89, 0, 100, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new SpecialCamera(overheadDummy, 0, 75, -89, 0, 100, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new SpecialCamera(overheadDummy, 300, 90, -10, 6500, 7000, 0, 0, 1, 0, 0));
					}
					frintezza = (GrandBossInstance) addSpawn(29045, -87780, -155086, -9080, 16384, false, 0, false, _r);
					_r.setParam("frintezza", frintezza);
					frintezza.setIsImmobilized(true);
					frintezza.setIsRunner(true);
					frintezza.setIsInvul(true);
					frintezza.disableAllSkills();
					for (final int[] element : PORTRAIT_SPAWNS)
					{
						final var demon = addSpawn(element[0] + 2, element[5], element[6], element[7], element[8], false, 0, false, _r);
						demon.setIsImmobilized(true);
						demon.disableAllSkills();
						demons.add(demon);
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 4), 6500);
					break;
				case 4 :
					if (frintezzaDummy != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezzaDummy, 1800, 90, 8, 6500, 7000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 5), 900);
					break;
				case 5 :
					if (frintezzaDummy != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezzaDummy, 140, 90, 10, 2500, 4500, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 6), 4000);
					break;
				case 6 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 40, 75, -10, 0, 1000, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new SpecialCamera(frintezza, 40, 75, -10, 0, 12000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 7), 1350);
					break;
				case 7 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SocialAction(frintezza.getObjectId(), 2));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 8), 7000);
					break;
				case 8 :
					if (frintezzaDummy != null)
					{
						frintezzaDummy.deleteMe();
						_r.setParam("frintezzaDummy", null);
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 9), 1000);
					break;
				case 9 :
					if (demons.size() > 3)
					{
						_r.broadcastPacket(new SocialAction(demons.get(1).getObjectId(), 1));
						_r.broadcastPacket(new SocialAction(demons.get(2).getObjectId(), 1));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 10), 400);
					break;
				case 10 :
					if (demons.size() > 3)
					{
						_r.broadcastPacket(new SocialAction(demons.getFirst().getObjectId(), 1));
						_r.broadcastPacket(new SocialAction(demons.get(3).getObjectId(), 1));
					}
					
					if (portraitDummy1 != null && portraitDummy3 != null)
					{
						sendPacketX(new SpecialCamera(portraitDummy1, 1000, 118, 0, 0, 1000, 0, 0, 1, 0, 0), new SpecialCamera(portraitDummy3, 1000, 62, 0, 0, 1000, 0, 0, 1, 0, 0), -87784);
						sendPacketX(new SpecialCamera(portraitDummy1, 1000, 118, 0, 0, 10000, 0, 0, 1, 0, 0), new SpecialCamera(portraitDummy3, 1000, 62, 0, 0, 10000, 0, 0, 1, 0, 0), -87784);
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 11), 2000);
					break;
				case 11 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 240, 90, 0, 0, 1000, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new SpecialCamera(frintezza, 240, 90, 25, 5500, 10000, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new SocialAction(frintezza.getObjectId(), 3));
					}
					if (portraitDummy1 != null)
					{
						portraitDummy1.deleteMe();
					}
					
					if (portraitDummy3 != null)
					{
						portraitDummy3.deleteMe();
					}
					_r.setParam("portraitDummy1", null);
					_r.setParam("portraitDummy3", null);
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 12), 4500);
					break;
				case 12 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 100, 195, 35, 0, 10000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 13), 700);
					break;
				case 13 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 100, 195, 35, 0, 10000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 14), 1300);
					break;
				case 14 :
					_r.broadcastPacket(new ExShowScreenMessage(NpcStringId.MOURNFUL_CHORALE_PRELUDE, 2, 5000));
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 120, 180, 45, 1500, 10000, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, 34000, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 15), 1500);
					break;
				case 15 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 520, 135, 45, 8000, 10000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 16), 7500);
					break;
				case 16 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 1500, 110, 25, 10000, 13000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 17), 9500);
					break;
				case 17 :
					if (overheadDummy != null)
					{
						_r.broadcastPacket(new SpecialCamera(overheadDummy, 930, 160, -20, 0, 1000, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new SpecialCamera(overheadDummy, 600, 180, -25, 0, 10000, 0, 0, 1, 0, 0));
						if (scarletDummy != null)
						{
							_r.broadcastPacket(new MagicSkillUse(scarletDummy, overheadDummy, 5004, 1, 5800, 0));
						}
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 18), 5000);
					break;
				case 18 :
					activeScarlet = (GrandBossInstance) addSpawn(29046, -87789, -153295, -9176, 16384, false, 0, false, _r);
					_r.setParam("activeScarlet", activeScarlet);
					activeScarlet.setRHandId(8204);
					activeScarlet.setIsInvul(true);
					activeScarlet.setIsImmobilized(true);
					activeScarlet.disableAllSkills();
					_r.broadcastPacket(new SocialAction(activeScarlet.getObjectId(), 3));
					if (scarletDummy != null)
					{
						_r.broadcastPacket(new SpecialCamera(scarletDummy, 800, 180, 10, 1000, 10000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 19), 2100);
					break;
				case 19 :
					if (activeScarlet != null)
					{
						_r.broadcastPacket(new SpecialCamera(activeScarlet, 300, 60, 8, 0, 10000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 20), 2000);
					break;
				case 20 :
					if (activeScarlet != null)
					{
						_r.broadcastPacket(new SpecialCamera(activeScarlet, 500, 90, 10, 3000, 5000, 0, 0, 1, 0, 0));
					}
					_r.addTimer("SONG_TASK", ThreadPoolManager.getInstance().schedule(new SongTask(_r, 0), 100));
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 21), 3000);
					break;
				case 21 :
					for (int i = 0; i < PORTRAIT_SPAWNS.length; i++)
					{
						final var portrait = addSpawn(PORTRAIT_SPAWNS[i][0], PORTRAIT_SPAWNS[i][1], PORTRAIT_SPAWNS[i][2], PORTRAIT_SPAWNS[i][3], PORTRAIT_SPAWNS[i][4], false, 0, false, _r);
						portrait.setScriptValue(i);
						portraits.add(portrait);
					}

					if (overheadDummy != null)
					{
						overheadDummy.deleteMe();
					}
					if (scarletDummy != null)
					{
						scarletDummy.deleteMe();
					}
					_r.setParam("overheadDummy", null);
					_r.setParam("scarletDummy", null);

					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 22), 2000);
					break;
				case 22 :
					if (!demons.isEmpty())
					{
						for (final var demon : demons)
						{
							demon.setIsImmobilized(false);
							demon.enableAllSkills();
						}
					}
					
					if (activeScarlet != null)
					{
						activeScarlet.setIsInvul(false);
						activeScarlet.setIsImmobilized(false);
						activeScarlet.enableAllSkills();
						activeScarlet.setRunning();
						activeScarlet.doCast(new SkillHolder(5004, 1).getSkill());
					}
					
					if (frintezza != null)
					{
						frintezza.enableAllSkills();
						frintezza.disableCoreAI(true);
						frintezza.setIsMortal(false);
					}
					startPc();
					_r.addTimer("DEMON_SPAWN", ThreadPoolManager.getInstance().schedule(new DemonSpawnTask(_r), 20000));
					break;
				case 23 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SocialAction(frintezza.getObjectId(), 4));
					}
					break;
				case 24 :
					stopPc();
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 250, 120, 15, 0, 1000, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new SpecialCamera(frintezza, 250, 120, 15, 0, 10000, 0, 0, 1, 0, 0));
					}
					
					if (activeScarlet != null)
					{
						activeScarlet.abortAttack();
						activeScarlet.abortCast();
						activeScarlet.setIsInvul(true);
						activeScarlet.setIsImmobilized(true);
						activeScarlet.disableAllSkills();
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 25), 7000);
					break;
				case 25 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, 34000, 0));
						_r.broadcastPacket(new SpecialCamera(frintezza, 500, 70, 15, 3000, 10000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 26), 3000);
					break;
				case 26 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 2500, 90, 12, 6000, 10000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 27), 3000);
					break;
				case 27 :
					if (activeScarlet != null)
					{
						final var loc = activeScarlet.getLocation();
						_r.setParam("scarletLoc", activeScarlet.getLocation());
						int scarlet_a = 0;
						if (loc.getHeading() < 32768)
						{
							scarlet_a = Math.abs(180 - (int) (loc.getHeading() / 182.044444444));
							_r.setParam("scarlet_a", scarlet_a);
						}
						else
						{
							scarlet_a = Math.abs(540 - (int) (loc.getHeading() / 182.044444444));
							_r.setParam("scarlet_a", scarlet_a);
						}
						_r.broadcastPacket(new SpecialCamera(activeScarlet, 250, scarlet_a, 12, 0, 1000, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new SpecialCamera(activeScarlet, 250, scarlet_a, 12, 0, 10000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 28), 500);
					break;
				case 28 :
					if (activeScarlet != null)
					{
						activeScarlet.doDie(activeScarlet);
						_r.broadcastPacket(new SpecialCamera(activeScarlet, 450, _r.getParams().getInteger("scarlet_a", 0), 14, 8000, 8000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 29), 6250);
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 30), 7200);
					break;
				case 29 :
					if (activeScarlet != null)
					{
						activeScarlet.deleteMe();
						_r.setParam("activeScarlet", null);
					}
					break;
				case 30 :
					activeScarlet = (GrandBossInstance) addSpawn(29047, _r.getParams().getObject("scarletLoc", Location.class), _r);
					_r.setParam("activeScarlet", activeScarlet);
					activeScarlet.setIsInvul(true);
					activeScarlet.setIsImmobilized(true);
					activeScarlet.disableAllSkills();
					if (!demons.isEmpty())
					{
						for (final var demon : demons)
						{
							if (demon != null)
							{
								demon.setIsImmobilized(true);
								demon.disableAllSkills();
							}
						}
					}
					_r.broadcastPacket(new SpecialCamera(activeScarlet, 450, _r.getParams().getInteger("scarlet_a", 0), 12, 500, 14000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 31), 8100);
					break;
				case 31 :
					if (activeScarlet != null)
					{
						_r.broadcastPacket(new SocialAction(activeScarlet.getObjectId(), 2));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 32), 9000);
					break;
				case 32 :
					startPc();
					if (activeScarlet != null)
					{
						activeScarlet.setIsInvul(false);
						activeScarlet.setIsImmobilized(false);
						activeScarlet.enableAllSkills();
					}
					if (!demons.isEmpty())
					{
						for (final var demon : demons)
						{
							if (demon != null)
							{
								demon.setIsImmobilized(false);
								demon.enableAllSkills();
							}
						}
					}
					_r.setParam("isVideo", false);
					break;
				case 33 :
					if (activeScarlet != null)
					{
						_r.broadcastPacket(new SpecialCamera(activeScarlet, 300, _r.getParams().getInteger("scarlet_a", 0) - 180, 5, 0, 7000, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new SpecialCamera(activeScarlet, 200, _r.getParams().getInteger("scarlet_a", 0), 85, 4000, 10000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 34), 7400);
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 35), 7500);
					break;
				case 34 :
					if (frintezza != null)
					{
						frintezza.doDie(frintezza);
					}
					break;
				case 35 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 100, 120, 5, 0, 7000, 0, 0, 1, 0, 0));
						_r.broadcastPacket(new SpecialCamera(frintezza, 100, 90, 5, 5000, 15000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 36), 7000);
					break;
				case 36 :
					if (frintezza != null)
					{
						_r.broadcastPacket(new SpecialCamera(frintezza, 900, 90, 25, 7000, 10000, 0, 0, 1, 0, 0));
					}
					ThreadPoolManager.getInstance().schedule(new IntroTask(_r, 37), 9000);
					break;
				case 37 :
					controlStatus(_r, 6);
					_r.setParam("isVideo", false);
					startPc();
					break;
			}
		}

		private void stopPc()
		{
			for (final var player : _r.getReflectionPlayers())
			{
				player.abortAttack();
				player.abortCast();
				player.disableAllSkills();
				player.setTarget(null);
				player.stopMove(null);
				player.setIsImmobilized(true);
				player.getAI().setIntention(CtrlIntention.IDLE);
			}
		}

		private void startPc()
		{
			for (final var player : _r.getReflectionPlayers())
			{
				player.enableAllSkills();
				player.setIsImmobilized(false);
				player.sendActionFailed();
			}
		}

		private void sendPacketX(GameServerPacket packet1, GameServerPacket packet2, int x)
		{
			for (final var player : _r.getReflectionPlayers())
			{
				if (player.getX() < x)
				{
					player.sendPacket(packet1);
				}
				else
				{
					player.sendPacket(packet2);
				}
			}
		}
	}

	private void statusTask(Reflection r, int status)
	{
		if (r != null)
		{
			final var players = r.getReflectionPlayers();
			switch (status)
			{
				case 1 :
					for (final var mob : r.getAliveNpcs(blockANpcs))
					{
						mob.unblock();
						mob.setIsInvul(false);
						mob.setRunning();
						final var target = players.get(Rnd.get(players.size()));
						if (target != null)
						{
							((MonsterInstance) mob).addDamageHate(target, 0, 500);
							mob.getAI().setIntention(CtrlIntention.ATTACK, target);
						}
						else
						{
							mob.getAI().setIntention(CtrlIntention.MOVING, new Location(-87904, -141296, -9168, 0), 0);
						}
					}
					break;
				case 2 :
					for (final var mob : r.getAliveNpcs(18334, 18335, 18336, 18337, 18338))
					{
						mob.unblock();
						mob.setIsInvul(false);
						mob.setRunning();
						final var target = players.get(Rnd.get(players.size()));
						if (target != null)
						{
							((MonsterInstance) mob).addDamageHate(target, 0, 500);
							mob.getAI().setIntention(CtrlIntention.ATTACK, target);
						}
						else
						{
							mob.getAI().setIntention(CtrlIntention.MOVING, new Location(-87954, -147014, -9184, 0), 0);
						}
					}
					break;
			}
		}
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if ((npc.getId() == 29046) && (r.isStatus(3)) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.80)))
			{
				r.setStatus(4);
				controlStatus(r, 3);
			}
			else if ((npc.getId() == 29046) && (r.isStatus(4)) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.20)))
			{
				r.setStatus(5);
				controlStatus(r, 4);
			}
			if (skill != null)
			{
				if ((npc.getId() == 29048 || npc.getId() == 29049) && (skill.getId() == 2276))
				{
					npc.doDie(attacker);
				}
				else if ((npc.getId() == 29045) && (skill.getId() == 2234))
				{
					npc.setScriptValue(1);
					npc.setTarget(null);
					npc.getAI().setIntention(CtrlIntention.IDLE);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}

	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		if (skill.isSuicideAttack())
		{
			return onKill(npc, null, false);
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (npc.getId() == 18328)
			{
				for (final int doorId : FIRST_ROOM_DOORS)
				{
					r.openDoor(doorId);
				}
				r.addTimer("TIMER", ThreadPoolManager.getInstance().schedule(() -> statusTask(r, 1), 2000));
			}
			else if (npc.getId() == 18339)
			{
				if (!r.hasAliveNpcs(18339))
				{
					for (final int doorId : SECOND_ROOM_DOORS)
					{
						r.openDoor(doorId);
					}
					r.addTimer("TIMER", ThreadPoolManager.getInstance().schedule(() -> statusTask(r, 2), 2000));
				}
			}
			else if (npc.getId() == 29046)
			{
				if (r.isStatus(3))
				{
					handleReenterTime(r);
					r.setStatus(5);
					controlStatus(r, 4);
				}
				else if (r.isStatus(4) && !r.getParams().getBool("isScarletSecondStage", false))
				{
					r.setStatus(5);
					controlStatus(r, 4);
				}
			}
			else if (npc.getId() == 29047)
			{
				r.removeTimer("DEMON_SPAWN");
				for (final var n : r.getAliveNpcs(29048, 29049, 29050, 29051))
				{
					n.deleteMe();
				}
				r.setParam("demons", null);
				r.setParam("portraits", null);
				controlStatus(r, 5);
				finishInstance(r, false);
			}
			else if (r.getStatus() <= 2)
			{
				if (ArrayUtils.contains(blockANpcs, npc.getId()))
				{
					if (!r.hasAliveNpcs(blockANpcs))
					{
						for (final int doorId : FIRST_ROUTE_DOORS)
						{
							r.openDoor(doorId);
						}
						r.setStatus(2);
					}
					
					if (npc.getId() == 18329 && getRandom(100) < 5)
					{
						((MonsterInstance) npc).dropSingleItem(player, 8556, 1);
					}
					return super.onKill(npc, player, isSummon);
				}
				
				if (ArrayUtils.contains(blockBNpcs, npc.getId()))
				{
					if (!r.hasAliveNpcs(blockBNpcs))
					{
						for (final int doorId : SECOND_ROUTE_DOORS)
						{
							r.openDoor(doorId);
						}
						r.setStatus(3);
						r.addTimer("INTRO", ThreadPoolManager.getInstance().schedule(new IntroTask(r, 0), (r.getParams().getInteger("frintezzaDelay", 0) * 1000L)));
					}
				}
			}
			else if (npc.getId() == 29050 || npc.getId() == 29051)
			{
				final var demons = r.getParams().getList("demons", Npc.class);
				if (demons != null && demons.contains(npc))
				{
					demons.remove(npc);
				}
			}
			else if (npc.getId() == 29048 || npc.getId() == 29049)
			{
				final var portraits = r.getParams().getList("portraits", Npc.class);
				if (portraits != null)
				{
					if (portraits.contains(npc))
					{
						portraits.remove(npc);
					}
					
					if (portraits.isEmpty())
					{
						r.removeTimer("DEMON_SPAWN");
					}
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	@Override
	public String onSpawn(Npc npc)
	{
		npc.setIsNoRndWalk(true);
		if (npc.getId() == 18328)
		{
			npc.disableCoreAI(true);
		}
		
		if (npc.isAttackable())
		{
			((Attackable) npc).setSeeThroughSilentMove(true);
		}
		return super.onSpawn(npc);
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
		if (npcId == 32011)
		{
			enterInstance(player, npc);
		}
		else if (npc.getId() == 29061)
		{
			final int x = -87534 + getRandom(500);
			final int y = -153048 + getRandom(500);
			player.teleToLocation(x, y, -9165, true, player.getReflection());
			return null;
		}
		return "";
	}

	void main()
	{
		new FinalEmperialTomb();
	}
}