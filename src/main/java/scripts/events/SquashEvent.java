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
package scripts.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.ArrayUtils;

import l2e.commons.util.Rnd;
import gameserver.Announcements;
import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.data.parser.SkillsParser;
import gameserver.data.parser.WorldEventParser;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.GameObject;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.ChronoMonsterInstance;
import gameserver.model.entity.events.AbstractWorldEvent;
import gameserver.model.entity.events.EventsDropManager;
import gameserver.model.entity.events.model.template.WorldEventTemplate;
import gameserver.model.skills.Skill;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;
import gameserver.network.serverpackets.PlaySound;
import gameserver.utils.TimeUtils;

/**
 * Updated by LordWinter 13.07.2020
 */
public class SquashEvent extends AbstractWorldEvent
{
	private boolean _isActive = false;
	private WorldEventTemplate _template = null;
	private ScheduledFuture<?> _eventTask = null;
	
	private final List<Npc> _npcList = new ArrayList<>();

	private static final int[] CHRONO_LIST =
	{
	        4202, 5133, 5817, 7058, 8350
	};
	
	private static final NpcStringId[] ON_SPAWN_TEXTS_1 = new NpcStringId[]
	{
	        NpcStringId.WHATS_THIS_WHY_AM_I_BEING_DISTURBED, NpcStringId.TA_DA_HERE_I_AM, NpcStringId.WHAT_ARE_YOU_LOOKING_AT, NpcStringId.IF_YOU_GIVE_ME_NECTAR_THIS_LITTLE_SQUASH_WILL_GROW_UP_QUICKLY, NpcStringId.ARE_YOU_MY_MOMMY, NpcStringId.FANCY_MEETING_YOU_HERE, NpcStringId.ARE_YOU_AFRAID_OF_THE_BIG_BAD_SQUASH, NpcStringId.IMPRESSIVE_ARENT_I, NpcStringId.OBEY_ME, NpcStringId.RAISE_ME_WELL_AND_YOULL_BE_REWARDED_NEGLECT_ME_AND_SUFFER_THE_CONSEQUENCES
	};
	
	private static final NpcStringId[] ON_SPAWN_TEXTS_2 = new NpcStringId[]
	{
	        NpcStringId.BRING_ME_NECTAR, NpcStringId.I_MUST_HAVE_NECTAR_TO_GROW, NpcStringId.GIVE_ME_SOME_NECTAR_QUICKLY_OR_YOULL_GET_NOTHING, NpcStringId.PLEASE_GIVE_ME_SOME_NECTAR_IM_HUNGRY, NpcStringId.NECTAR_PLEASE, NpcStringId.NECTAR_WILL_MAKE_ME_GROW_QUICKLY, NpcStringId.DONT_YOU_WANT_A_BIGGER_SQUASH_GIVE_ME_SOME_NECTAR_AND_ILL_GROW_MUCH_LARGER, NpcStringId.IF_YOU_RAISE_ME_WELL_YOULL_GET_PRIZES_OR_NOT, NpcStringId.YOU_ARE_HERE_FOR_THE_STUFF_EH_WELL_ITS_MINE_ALL_MINE, NpcStringId.TRUST_ME_GIVE_ME_NECTAR_AND_ILL_BECOME_A_GIANT_SQUASH
	};

	private static final NpcStringId[] WAIT_TEXTS = new NpcStringId[]
	{
	        NpcStringId.SO_LONG_SUCKERS, NpcStringId.IM_OUT_OF_HERE, NpcStringId.I_MUST_BE_GOING_HAVE_FUN_EVERYBODY, NpcStringId.TIME_IS_UP_PUT_YOUR_WEAPONS_DOWN, NpcStringId.GOOD_FOR_ME_BAD_FOR_YOU
	};
	
	private static final NpcStringId[] ON_ATTACK_TEXTS_1 = new NpcStringId[]
	{
	        NpcStringId.KEEP_IT_COMING, NpcStringId.THATS_WHAT_IM_TALKING_ABOUT, NpcStringId.MAY_I_HAVE_SOME_MORE, NpcStringId.THAT_HIT_THE_SPOT, NpcStringId.I_FEEL_SPECIAL, NpcStringId.I_THINK_ITS_WORKING, NpcStringId.YOU_DO_UNDERSTAND, NpcStringId.YUCK_WHAT_IS_THIS_HA_HA_JUST_KIDDING, NpcStringId.A_TOTAL_OF_FIVE_AND_ILL_BE_TWICE_AS_ALIVE, NpcStringId.NECTAR_IS_SUBLIME
	};
	
	private static final NpcStringId[] ON_ATTACK_TEXTS_2 = new NpcStringId[]
	{
	        NpcStringId.TRANSFORM, NpcStringId.I_FEEL_DIFFERENT, NpcStringId.IM_BIGGER_NOW_BRING_IT_ON, NpcStringId.IM_NOT_A_KID_ANYMORE, NpcStringId.BIG_TIME, NpcStringId.GOOD_LUCK, NpcStringId.IM_ALL_GROWN_UP_NOW, NpcStringId.IF_YOU_LET_ME_GO_ILL_BE_YOUR_BEST_FRIEND, NpcStringId.IM_CHUCK_FULL_OF_GOODNESS, NpcStringId.GOOD_JOB_NOW_WHAT_ARE_YOU_GOING_TO_DO
	};

	private static final NpcStringId[] ON_ATTACK_TEXTS_3 = new NpcStringId[]
	{
	        NpcStringId.YOU_CALL_THAT_A_HIT, NpcStringId.WHY_ARE_YOU_HITTING_ME_OUCH_STOP_IT_GIVE_ME_NECTAR, NpcStringId.STOP_OR_ILL_WILT, NpcStringId.IM_NOT_FULLY_GROWN_YET_OH_WELL_DO_WHAT_YOU_WILL_ILL_FADE_AWAY_WITHOUT_NECTAR_ANYWAY, NpcStringId.GO_AHEAD_AND_HIT_ME_AGAIN_IT_WONT_DO_YOU_ANY_GOOD, NpcStringId.WOE_IS_ME_IM_WILTING, NpcStringId.IM_NOT_FULLY_GROWN_YET_HOW_ABOUT_SOME_NECTAR_TO_EASE_MY_PAIN, NpcStringId.THE_END_IS_NEAR, NpcStringId.PRETTY_PLEASE_WITH_SUGAR_ON_TOP_GIVE_ME_SOME_NECTAR, NpcStringId.IF_I_DIE_WITHOUT_NECTAR_YOULL_GET_NOTHING
	};

	private static final NpcStringId[] ON_ATTACK_TEXTS_4 = new NpcStringId[]
	{
	        NpcStringId.BETTER_LUCK_NEXT_TIME, NpcStringId.NICE_SHOT, NpcStringId.IM_NOT_AFRAID_OF_YOU, NpcStringId.IF_I_KNEW_THIS_WAS_GOING_TO_HAPPEN_I_WOULD_HAVE_STAYED_HOME, NpcStringId.TRY_HARDER_OR_IM_OUT_OF_HERE, NpcStringId.IM_TOUGHER_THAN_I_LOOK, NpcStringId.GOOD_STRIKE, NpcStringId.OH_MY_GOURD, NpcStringId.THATS_ALL_YOUVE_GOT, NpcStringId.WHY_ME
	};

	private static final NpcStringId[] ON_ATTACK_TEXTS_5 = new NpcStringId[]
	{
	        NpcStringId.SOUNDTASTIC, NpcStringId.I_CAN_SING_ALONG_IF_YOU_LIKE, NpcStringId.I_THINK_YOU_NEED_SOME_BACKUP, NpcStringId.KEEP_UP_THAT_RHYTHM_AND_YOULL_BE_A_STAR, NpcStringId.MY_HEART_YEARNS_FOR_MORE_MUSIC, NpcStringId.YOURE_OUT_OF_TUNE_AGAIN, NpcStringId.THIS_IS_AWFUL, NpcStringId.I_THINK_I_BROKE_SOMETHING, NpcStringId.WHAT_A_LOVELY_MELODY_PLAY_IT_AGAIN, NpcStringId.MUSIC_TO_MY_UH_EARS
	};

	private static final NpcStringId[] ON_ATTACK_TEXTS_6 = new NpcStringId[]
	{
	        NpcStringId.YOU_NEED_MUSIC_LESSONS, NpcStringId.I_CANT_HEAR_YOU, NpcStringId.YOU_CANT_HURT_ME_LIKE_THAT, NpcStringId.IM_STRONGER_THAN_YOU_ARE, NpcStringId.NO_MUSIC_IM_OUT_OF_HERE, NpcStringId.THAT_RACKET_IS_GETTING_ON_MY_NERVES_TONE_IT_DOWN_A_BIT, NpcStringId.YOU_CAN_ONLY_HURT_ME_THROUGH_MUSIC, NpcStringId.ONLY_MUSICAL_INSTRUMENTS_CAN_HURT_ME_NOTHING_ELSE, NpcStringId.YOUR_SKILLS_ARE_IMPRESSIVE_BUT_SADLY_USELESS, NpcStringId.CATCH_A_CHRONO_FOR_ME_PLEASE
	};

	private static final NpcStringId[] ON_DEAD_TEXTS_1 = new NpcStringId[]
	{
	        NpcStringId.YOU_GOT_ME, NpcStringId.NOW_LOOK_AT_WHAT_YOUVE_DONE, NpcStringId.YOU_WIN, NpcStringId.SQUASHED, NpcStringId.DONT_TELL_ANYONE, NpcStringId.GROSS_MY_GUTS_ARE_COMING_OUT, NpcStringId.TAKE_IT_AND_GO, NpcStringId.I_SHOULDVE_LEFT_WHEN_I_COULD, NpcStringId.NOW_LOOK_WHAT_YOU_HAVE_DONE, NpcStringId.I_FEEL_DIRTY
	};

	private class TheInstance
	{
		long despawnTime;
	}

	private final Map<ChronoMonsterInstance, TheInstance> _monsterInstances = new ConcurrentHashMap<>();

	private TheInstance create(ChronoMonsterInstance mob)
	{
		final var mons = new TheInstance();
		_monsterInstances.put(mob, mons);
		return mons;
	}

	private TheInstance get(ChronoMonsterInstance mob)
	{
		return _monsterInstances.get(mob);
	}

	private void remove(ChronoMonsterInstance mob)
	{
		cancelQuestTimer("countdown", mob, null);
		cancelQuestTimer("delayChat", mob, null);
		cancelQuestTimer("despawn", mob, null);
		_monsterInstances.remove(mob);
	}

	private SquashEvent()
	{
		addStartNpc(31255);
		addFirstTalkId(31255);
		addTalkId(31255);
		
		addAttackId(12774, 12775, 12776, 12777, 12778, 12779, 13016, 13017);
		addKillId(12774, 12775, 12776, 12777, 12778, 12779, 13016, 13017);
		addSpawnId(12774, 12775, 12776, 12777, 12778, 12779, 13016, 13017);
		addSkillSeeId(12774, 12777);

		_template = WorldEventParser.getInstance().getEvent(16);
		if (_template != null && !_isActive)
		{
			if (_template.isNonStop())
			{
				eventStart(-1, false);
			}
			else
			{
				final long startTime = calcEventStartTime(_template, false);
				final long expireTime = calcEventStopTime(_template, false);
				if (startTime <= System.currentTimeMillis() && expireTime > System.currentTimeMillis() || (expireTime < startTime && expireTime > System.currentTimeMillis()))
				{
					eventStart(expireTime - System.currentTimeMillis(), false);
				}
				else
				{
					checkTimerTask(startTime);
				}
			}
		}
	}

	@Override
	public boolean isEventActive()
	{
		return _isActive;
	}

	@Override
	public WorldEventTemplate getEventTemplate()
	{
		return _template;
	}
	
	@Override
	public boolean eventStart(long totalTime, boolean force)
	{
		if (_isActive || totalTime == 0)
		{
			return false;
		}
		
		final var task = _eventTask;
		if (task != null)
		{
			task.cancel(false);
			_eventTask = null;
		}
		
		_isActive = true;
		
		final var spawnList = _template.getSpawnList();
		if (spawnList != null && !spawnList.isEmpty())
		{
			spawnList.stream().filter(s -> s != null).forEach(s -> _npcList.add(addSpawn(s.getNpcId(), s.getLocation().getX(), s.getLocation().getY(), s.getLocation().getZ(), s.getLocation().getHeading(), false, 0, false, ReflectionManager.DEFAULT, s.getTriggerId())));
		}
		
		if (_template.getDropList() != null && !_template.getDropList().isEmpty())
		{
			EventsDropManager.getInstance().addRule(_template.getId(), _template.getDropList(), true);
		}

		final var msg = new ServerMessage("EventSquashes.START", true);
		Announcements.getInstance().announceToAll(msg);

		if (totalTime > 0)
		{
			_eventTask = ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run()
				{
					eventStop();
				}
			}, totalTime);
			_log.info("Event " + _template.getName(null) + " will end in: " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + totalTime));
		}
		return true;
	}

	@Override
	public boolean eventStop()
	{
		if (!_isActive)
		{
			return false;
		}
		
		final var task = _eventTask;
		if (task != null)
		{
			task.cancel(false);
			_eventTask = null;
		}
		_isActive = false;

		_npcList.stream().filter(n -> n != null).forEach(n -> n.deleteMe());
		_npcList.clear();

		EventsDropManager.getInstance().removeRule(_template.getId());
		
		final var msg = new ServerMessage("EventSquashes.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		checkTimerTask(calcEventStartTime(_template, false));
		
		return true;
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (!_isActive)
		{
			return null;
		}
		
		if (event.equalsIgnoreCase("countdown"))
		{
			final var mob = (ChronoMonsterInstance) npc;
			final var self = get(mob);
			final int timeLeft = (int) ((self.despawnTime - System.currentTimeMillis()) / 1000);
			switch (npc.getId())
			{
				case 12774 :
				case 12777 :
					final var lastTime = timeLeft % 60;
					if (lastTime == 2)
					{
						mob.broadcastPacketToOthers(2000, new NpcSay(mob, Say2.ALL, NpcStringId.GIVE_ME_NECTAR_OR_ILL_BE_GONE_IN_TWO_MINUTES));
					}
					else if (lastTime == 1)
					{
						mob.broadcastPacketToOthers(2000, new NpcSay(mob, Say2.ALL, NpcStringId.GIVE_ME_NECTAR_OR_ILL_BE_GONE_IN_ONE_MINUTE));
					}
					break;
				case 12775 :
				case 12776 :
				case 12778 :
				case 12779 :
				case 13016 :
				case 13017 :
					if (timeLeft == 30)
					{
						mob.broadcastPacketToOthers(2000, new NpcSay(mob, Say2.ALL, NpcStringId.IM_FEELING_BETTER_ANOTHER_THIRTY_SECONDS_AND_ILL_BE_OUT_OF_HERE));
					}
					else if (timeLeft == 20)
					{
						mob.broadcastPacketToOthers(2000, new NpcSay(mob, Say2.ALL, NpcStringId.TWENTY_SECONDS_AND_ITS_CIAO_BABY));
					}
					else if (timeLeft == 10)
					{
						mob.broadcastPacketToOthers(2000, new NpcSay(mob, Say2.ALL, NpcStringId.WOOHOO_JUST_TEN_SECONDS_LEFT_NINE_EIGHT_SEVEN));
					}
					break;
			}
			return null;
		}
		else if (event.equalsIgnoreCase("delayChat"))
		{
			npc.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(ON_SPAWN_TEXTS_2)));
			return null;
		}
		else if (event.equalsIgnoreCase("despawn"))
		{
			remove((ChronoMonsterInstance) npc);
			switch (npc.getId())
			{
				case 12775 :
				case 12776 :
				case 12778 :
				case 12779 :
				case 13016 :
				case 13017 :
					npc.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(WAIT_TEXTS)));
					break;
			}
			npc.deleteMe();
			return null;
		}
		else if (event.equalsIgnoreCase("sound"))
		{
			npc.broadcastPacketToOthers(2000, new PlaySound(0, "ItemSound3.sys_sow_success", 0, 0, 0, 0, 0));
			return null;
		}
		return event;
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		
		switch (npc.getId())
		{
			case 31255 :
				return "31255.htm";
		}
		return null;
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		if (!_isActive)
		{
			return super.onAttack(npc, attacker, damage, isSummon);
		}
		
		if (attacker != null)
		{
			final var mob = (ChronoMonsterInstance) npc;
			final var weapon = attacker.getActiveWeaponItem();
			final boolean isChronoAttack = !isSummon && weapon != null && ArrayUtils.contains(CHRONO_LIST, weapon.getId());
			switch (mob.getId())
			{
				case 12774 :
				case 12777 :
					if (Rnd.getChance(5))
					{
						npc.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(ON_ATTACK_TEXTS_3)));
					}
					
					if (isChronoAttack)
					{
						mob.setIsInvul(false);
					}
					else
					{
						mob.setIsInvul(true);
						mob.setCurrentHp(mob.getMaxHp());
					}
					break;
				case 12775 :
				case 12776 :
				case 12778 :
				case 12779 :
				case 13016 :
				case 13017 :
					if (isChronoAttack)
					{
						mob.setIsInvul(false);
						if (Rnd.getChance(5))
						{
							if (mob.getId() == 12779 || mob.getId() == 13016 || mob.getId() == 13017)
							{
								npc.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(ON_ATTACK_TEXTS_5)));
							}
							else
							{
								npc.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(ON_ATTACK_TEXTS_4)));
							}
						}
					}
					else
					{
						mob.setIsInvul(true);
						mob.setCurrentHp(mob.getMaxHp());
						if (Rnd.getChance(5))
						{
							if (mob.getId() == 12779 || mob.getId() == 13016 || mob.getId() == 13017)
							{
								npc.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(ON_ATTACK_TEXTS_6)));
							}
						}
					}
					break;
			}
			mob.getStatus().stopHpMpRegeneration();
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon)
	{
		if ((skill.getId() == 2005) && (targets[0] == npc))
		{
			final var mob = (ChronoMonsterInstance) npc;
			switch (mob.getId())
			{
				case 12774 :
				case 12777 :
					if (mob.getScriptValue() < 5)
					{
						mob.setScriptValue(mob.getScriptValue() + 1);
						if (mob.getScriptValue() <= 1)
						{
							npc.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(ON_ATTACK_TEXTS_1)));
						}
						else
						{
							npc.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(ON_ATTACK_TEXTS_2)));
						}
						
						if (getRandom(100) < 50)
						{
							npc.doCast(SkillsParser.getInstance().getInfo(4514, 1));
						}
						else
						{
							npc.doCast(SkillsParser.getInstance().getInfo(4513, 1));
							mob.setLevelUp(mob.getLevelUp() + 1);
						}
						
						if (mob.getScriptValue() >= 5)
						{
							randomSpawn(mob);
						}
					}
					break;
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (_isActive)
		{
			final var mob = (ChronoMonsterInstance) npc;
			remove(mob);
			switch (npc.getId())
			{
				case 12774 :
				case 12777 :
					npc.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(ON_DEAD_TEXTS_1)));
					break;
				case 12775 :
				case 12776 :
				case 12778 :
				case 12779 :
				case 13016 :
				case 13017 :
					npc.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(ON_DEAD_TEXTS_1)));
					if (killer != null)
					{
						switch (npc.getId())
						{
							case 12775 :
								calcRandomGroupReward(npc, killer, _template, 1);
								break;
							case 12776 :
								calcRandomGroupReward(npc, killer, _template, 2);
								break;
							case 13016 :
								calcRandomGroupReward(npc, killer, _template, 3);
								break;
							case 12778 :
								calcRandomGroupReward(npc, killer, _template, 4);
								break;
							case 12779 :
								calcRandomGroupReward(npc, killer, _template, 5);
								break;
							case 13017 :
								calcRandomGroupReward(npc, killer, _template, 6);
								break;
						}
					}
					break;
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onSpawn(Npc npc)
	{
		if (_isActive && npc instanceof ChronoMonsterInstance)
		{
			final var mob = (ChronoMonsterInstance) npc;
			mob.setOnKillDelay(1500);
			final var self = create(mob);
			
			switch (mob.getId())
			{
				case 12774 :
				case 12777 :
					startQuestTimer("countdown", 60000, mob, null, true);
					startQuestTimer("despawn", 180000, mob, null);
					startQuestTimer("delayChat", 3000, mob, null);
					self.despawnTime = System.currentTimeMillis() + 180000;
					mob.broadcastPacketToOthers(2000, new NpcSay(npc, Say2.ALL, Rnd.get(ON_SPAWN_TEXTS_1)));
					break;
				case 12775 :
				case 12776 :
				case 12778 :
				case 12779 :
				case 13016 :
				case 13017 :
					startQuestTimer("countdown", 10000, mob, null, true);
					startQuestTimer("despawn", 90000, mob, null);
					startQuestTimer("sound", 100, mob, null);
					self.despawnTime = System.currentTimeMillis() + 90000;
					break;
			}
		}
		return super.onSpawn(npc);
	}

	private void randomSpawn(ChronoMonsterInstance mob)
	{
		int npcId = 0;
		switch (mob.getLevelUp())
		{
			case 5 :
				npcId = mob.getId() == 12774 ? 13016 : mob.getId() == 12777 ? 13017 : 0;
				break;
			case 4 :
				npcId = mob.getId() == 12774 ? 12775 : mob.getId() == 12777 ? 12778 : 0;
				break;
			case 3 :
			case 2 :
			case 1 :
			case 0 :
				npcId = mob.getId() == 12774 ? 12776 : mob.getId() == 12777 ? 12779 : 0;
				break;
		}
		
		if (npcId > 0)
		{
			spawnNext(npcId, mob);
		}
	}

	private void spawnNext(int npcId, ChronoMonsterInstance oldMob)
	{
		remove(oldMob);
		final var newMob = (ChronoMonsterInstance) addSpawn(npcId, oldMob.getX(), oldMob.getY(), oldMob.getZ(), oldMob.getHeading(), false, 0);
		newMob.setOwner(oldMob.getOwner());
		for (final String lang : Config.MULTILANG_ALLOWED)
		{
			if (lang != null)
			{
				newMob.setTitle(lang, oldMob.getTitle(lang));
			}
		}
		newMob.broadcastInfo();
		oldMob.deleteMe();
	}
	
	@Override
	public void startTimerTask(long time)
	{
		final var task = _eventTask;
		if (task != null)
		{
			task.cancel(false);
			_eventTask = null;
		}
		
		_eventTask = ThreadPoolManager.getInstance().schedule(new Runnable()
		{
			@Override
			public void run()
			{
				final long expireTime = calcEventStopTime(_template, false);
				if (expireTime > System.currentTimeMillis())
				{
					eventStart(expireTime - System.currentTimeMillis(), false);
				}
			}
		}, (time - System.currentTimeMillis()));
		_log.info("Event " + _template.getName(null) + " will start in: " + TimeUtils.toSimpleFormat(time));
	}
	
	@Override
	public boolean isReloaded()
	{
		if (isEventActive())
		{
			return false;
		}
		
		_template = WorldEventParser.getInstance().getEvent(16);
		if (_template != null)
		{
			if (_template.isNonStop())
			{
				eventStart(-1, false);
			}
			else
			{
				final long startTime = calcEventStartTime(_template, false);
				final long expireTime = calcEventStopTime(_template, false);
				if (startTime <= System.currentTimeMillis() && expireTime > System.currentTimeMillis() || (expireTime < startTime && expireTime > System.currentTimeMillis()))
				{
					eventStart(expireTime - System.currentTimeMillis(), false);
				}
				else
				{
					checkTimerTask(startTime);
				}
			}
			return true;
		}
		return false;
	}

	public static void main(String[] args)
	{
		new SquashEvent();
	}
}
