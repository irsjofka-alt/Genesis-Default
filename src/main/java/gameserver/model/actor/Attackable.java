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
package gameserver.model.actor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.SevenSigns;
import gameserver.ThreadPoolManager;
import gameserver.ai.character.CharacterAI;
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.DamageLimitParser;
import gameserver.data.parser.ExperienceParser;
import gameserver.data.parser.ItemsParser;
import gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import gameserver.instancemanager.CursedWeaponsManager;
import gameserver.instancemanager.DailyTaskManager;
import gameserver.instancemanager.VipManager;
import gameserver.instancemanager.WalkingManager;
import gameserver.model.AggroList;
import gameserver.model.CommandChannel;
import gameserver.model.GameObject;
import gameserver.model.Location;
import gameserver.model.Party;
import gameserver.model.Seed;
import gameserver.model.actor.instance.GrandBossInstance;
import gameserver.model.actor.instance.ServitorInstance;
import gameserver.model.actor.status.AttackableStatus;
import gameserver.model.actor.tasks.character.NotifyAITask;
import gameserver.model.actor.tasks.npc.OnKillByMobNotifyTask;
import gameserver.model.actor.tasks.npc.OnKillNotifyTask;
import gameserver.model.actor.templates.items.Item;
import gameserver.model.actor.templates.npc.DamageLimit;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.actor.templates.npc.aggro.GroupInfo;
import gameserver.model.actor.templates.npc.champion.ChampionRewardItem;
import gameserver.model.entity.events.EventsDropManager;
import gameserver.model.holders.ItemHolder;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestEventType;
import gameserver.model.reward.RewardItem;
import gameserver.model.reward.RewardList;
import gameserver.model.reward.RewardType;
import gameserver.model.skills.Skill;
import gameserver.model.stats.Stats;
import gameserver.model.zone.ZoneId;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SocialAction;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.taskmanager.DecayTaskManager;
import gameserver.taskmanager.ItemsAutoDestroy;

public class Attackable extends Npc
{
	private boolean _isRaid = false;
	private boolean _isEpicRaid = false;
	private boolean _isSiegeGuard = false;
	private boolean _isRaidMinion = false;
	private boolean _isLethalImmune = false;
	private boolean _isGlobalAI = false;
	private boolean _isPassiveSweepActive = false;
	
	private boolean _isReturningToSpawnPoint = false;
	private boolean _canReturnToSpawnPoint = true;
	private boolean _seeThroughSilentMove = false;
	private final AtomicReference<Collection<RewardItem>> _sweepItems = new AtomicReference<>();
	private final AtomicReference<ItemHolder> _harvestItems = new AtomicReference<>();
	private final AggroList _aggroList;
	private boolean _seeded;
	private Seed _seed = null;
	private int _seederObjId = 0;

	private boolean _overhit;
	private double _overhitDamage;
	private Creature _overhitAttacker;

	private int _spoilerObjectId = 0;

	private Set<Integer> _absorbersIds;
	private final Set<Integer> _blockList = new HashSet<>();

	protected int _onKillDelay = 5000;
	protected long _findTargetDelay = 0;
	private final Set<Integer> _targetList = ConcurrentHashMap.newKeySet();

	public Attackable(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.Attackable);
		setIsInvul(false);
		if (template.getCanSeeInSilentMove())
		{
			setSeeThroughSilentMove(true);
		}
		
		if (template.isLethalImmune())
		{
			setIsLethalImmune(true);
		}
		_aggroList = new AggroList(this);
		_canReturnToSpawnPoint = getTemplate().getParameter("canReturnToSpawnPoint", true);
	}
	
	@Override
	public CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = getTemplate().getNewAI(this);
				}
			}
		}
		return _ai;
	}

	@Override
	public AttackableStatus getStatus()
	{
		return (AttackableStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new AttackableStatus(this));
	}

	public final boolean isReturningToSpawnPoint()
	{
		return _isReturningToSpawnPoint;
	}

	public final void setisReturningToSpawnPoint(boolean value)
	{
		_isReturningToSpawnPoint = value;
	}

	public final boolean canReturnToSpawnPoint()
	{
		return _canReturnToSpawnPoint;
	}

	public final void setCanReturnToSpawnPoint(boolean value)
	{
		_canReturnToSpawnPoint = value;
	}

	public boolean canSeeThroughSilentMove()
	{
		return _seeThroughSilentMove;
	}

	public void setSeeThroughSilentMove(boolean val)
	{
		_seeThroughSilentMove = val;
	}

	public void useMagic(Skill skill)
	{
		if ((skill == null) || isAlikeDead() || skill.isPassive() || isCastingNow() || isSkillDisabled(skill) || isSkillBlocked(skill))
		{
			return;
		}

		final var mpConsume = getStat().getMpConsume(skill);
		if ((getCurrentMp() < (mpConsume + skill.getMpInitialConsume())) || (getCurrentHp() <= skill.getHpConsume()))
		{
			return;
		}

		if (!skill.isStatic())
		{
			if (skill.isMagic())
			{
				if (isMuted())
				{
					return;
				}
			}
			else
			{
				if (isPhysicalMuted())
				{
					return;
				}
			}
		}

		final GameObject target = skill.getFirstOfTargetList(this);
		if (target != null)
		{
			getAI().setIntention(CtrlIntention.CAST, skill, target);
		}
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill)
	{
		if (Config.ALLOW_DAMAGE_LIMIT)
		{
			final DamageLimit limit = DamageLimitParser.getInstance().getDamageLimit(getId());
			if (limit != null)
			{
				final int damageLimit = skill != null ? skill.isMagic() ? limit.getMagicDamage() : limit.getPhysicDamage() : limit.getDamage();
				if (damageLimit > 0 && damage > damageLimit)
				{
					damage = damageLimit;
				}
			}
		}
		reduceCurrentHp(damage, attacker, true, false, skill);
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill)
	{
		if (isEventMob())
		{
			return;
		}

		if (attacker != null && skill != null && !isInvul())
		{
			addDamage(attacker, (int) damage, skill);
			if (!Config.ALLOW_UNLIM_ENTER_CATACOMBS && isSevenSignsMonster())
			{
				final Player player = attacker.getActingPlayer();
				if (player != null)
				{
					if ((SevenSigns.getInstance().isSealValidationPeriod()) || (SevenSigns.getInstance().isCompResultsPeriod()))
					{
						final int pcabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
						final int wcabal = SevenSigns.getInstance().getCabalHighestScore();
						if ((pcabal != wcabal) && (wcabal != SevenSigns.CABAL_NULL))
						{
							player.sendMessage("You have been teleported to the nearest town because you not signed for winning cabal.");
							player.teleToClosestTown();
							return;
						}
					}
				}
			}
			
			if (attacker.isPlayer() && !_targetList.contains(attacker))
			{
				addToTargetList(attacker.getActingPlayer());
			}
		}
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		getAI().stopAITask();
		super.onDeath(killer);
		_targetList.clear();
		clearBlockList();
		
		if (killer != null && killer.isPlayable())
		{
			if (getTemplate().getEventQuests(QuestEventType.ON_KILL) != null)
			{
				ThreadPoolManager.getInstance().schedule(new OnKillNotifyTask(this, getTemplate().getEventQuests(QuestEventType.ON_KILL), killer.getActingPlayer(), (killer != null && killer.isSummon())), Config.NPC_DEAD_TIME_TASK * 1000L);
			}
				
			if (Config.ALLOW_DAILY_TASKS)
			{
				final var pl = killer.getActingPlayer();
				if (pl != null && (isMonster() || isRaid()))
				{
					var isForAll = false;
					for (final var taskTemplate : pl.getActiveDailyTasks())
					{
						if (taskTemplate.getType().equalsIgnoreCase("Farm") && !taskTemplate.isComplete())
						{
							final var task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
							if ((task.getNpcId() == getId()) && (taskTemplate.getCurrentNpcCount() < task.getNpcCount()))
							{
								taskTemplate.setCurrentNpcCount((taskTemplate.getCurrentNpcCount() + 1));
							}
							
							isForAll = task.isForAll();
								
							if (taskTemplate.isComplete())
							{
								final var vch = VoicedCommandHandler.getInstance().getHandler("missions");
								if (vch != null)
								{
									pl.updateDailyStatus(taskTemplate);
									vch.useVoicedCommand("missions", pl, null);
								}
							}
						}
					}
					
					if (isForAll && pl.isInParty())
					{
						for (final var member : pl.getParty().getMembers())
						{
							if (member != null && !member.isDead() && member != pl && member.isInRange(getLocation(), Config.ALT_PARTY_RANGE2))
							{
								for (final var taskTemplate : member.getActiveDailyTasks())
								{
									if (taskTemplate.getType().equalsIgnoreCase("Farm") && !taskTemplate.isComplete())
									{
										final var task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
										if ((task.getNpcId() == getId()) && (taskTemplate.getCurrentNpcCount() < task.getNpcCount()))
										{
											taskTemplate.setCurrentNpcCount((taskTemplate.getCurrentNpcCount() + 1));
										}
										
										isForAll = task.isForAll();
										
										if (taskTemplate.isComplete())
										{
											final var vch = VoicedCommandHandler.getInstance().getHandler("missions");
											if (vch != null)
											{
												member.updateDailyStatus(taskTemplate);
												vch.useVoicedCommand("missions", member, null);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		else if (killer != null && killer.isNpc())
		{
			if (getTemplate().getEventQuests(QuestEventType.ON_KILL) != null)
			{
				ThreadPoolManager.getInstance().schedule(new OnKillByMobNotifyTask(this, getTemplate().getEventQuests(QuestEventType.ON_KILL), (Npc) killer), Config.NPC_DEAD_TIME_TASK * 1000L);
			}
		}
	}
	
	@Override
	protected void onDelete()
	{
		clearAggroList(false);
		_harvestItems.set(null);
		_sweepItems.set(null);
		_spoilerObjectId = 0;
		super.onDelete();
	}
	
	@Override
	public void onDecay()
	{
		clearAggroList(false);
		_harvestItems.set(null);
		_sweepItems.set(null);
		_spoilerObjectId = 0;
		super.onDecay();
	}
	
	@Override
	protected void calculateRewards(Creature lastAttacker)
	{
		final Creature topDamager = getAggroList().getTopDamager(lastAttacker);
		
		doItemDrop(lastAttacker, topDamager != null ? topDamager : lastAttacker);
		
		final var rewards = getAggroList().getCharRewardMap();
		if (rewards.isEmpty())
		{
			return;
		}
		
		final var attackers = rewards.keySet().toArray(new Player[rewards.size()]);
		final var totalDamage = getAggroList().getTotalDamage();
		double[] expSp = new double[2];
		for (final var attacker : attackers)
		{
			
			if (attacker == null || attacker.isDead())
			{
				continue;
			}
			final var reward = rewards.get(attacker);
			if (reward == null)
			{
				continue;
			}
			
			final var damage = reward.getDamage();
			final var attackerParty = attacker.getParty();
			final float penalty = attacker.hasServitor() ? ((ServitorInstance) attacker.getSummon()).getExpPenalty() : 0;
			
			if (attackerParty == null)
			{
				final int levelDiff = attacker.getLevel() - getLevel();
				if (isInRangeZ(attacker, Config.ALT_PARTY_RANGE))
				{
					expSp = calculateExpAndSp(levelDiff, damage, totalDamage, attacker);
				}
				double exp = expSp[0];
				double sp = expSp[1];
				
				if (attacker.isPlayer() && attacker.getPremiumBonus().isPersonal())
				{
					exp *= attacker.getPremiumBonus().getRateXp();
					sp *= attacker.getPremiumBonus().getRateSp();
				}
				
				if (getChampionTemplate() != null)
				{
					exp *= getChampionTemplate().expMultiplier;
					sp *= getChampionTemplate().spMultiplier;
				}
					
				exp *= 1 - penalty;
					
				final Creature overhitAttacker = getOverhitAttacker();
				if (isOverhit() && (overhitAttacker != null) && (overhitAttacker.getActingPlayer() != null) && (attacker == overhitAttacker.getActingPlayer()))
				{
					attacker.sendPacket(SystemMessageId.OVER_HIT);
					exp += calculateOverhitExp((long) exp);
				}
					
				exp *= attacker.getRExp();
				sp *= attacker.getRSp();
				final long addexp = Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null));
				final int addsp = (int) attacker.calcStat(Stats.EXPSP_RATE, sp, null, null);
				
				attacker.addExpAndSp(addexp, addsp, useVitalityRate());
				if (addexp > 0)
				{
					if (!attacker.getNevitSystem().isActive() && attacker.getNevitSystem().getTime() > 0 && !attacker.isInsideZone(ZoneId.PEACE))
					{
						if ((attacker.getLevel() - getLevel()) <= 9)
						{
							final int nevitPoints = Math.round(((addexp / (getLevel() * getLevel())) * 100) / 20);
							attacker.getNevitSystem().addPoints(nevitPoints);
						}
					}
					attacker.updateVitalityPoints(getVitalityPoints(damage), true, false);
				}
				rewards.remove(attacker);
			}
			else
			{
				int partyDmg = 0;
				float partyMul = 1;
				int partyLvl = 0;
				
				final List<Player> rewardedMembers = new ArrayList<>();
				for (final Player partyPlayer : attackerParty.getMembers())
				{
					final var ai = rewards.remove(partyPlayer);
					if ((partyPlayer == null) || partyPlayer.isDead() || !isInRangeZ(partyPlayer, Config.ALT_PARTY_RANGE))
					{
						continue;
					}

					if (ai != null)
					{
						partyDmg += ai.getDamage();
						rewardedMembers.add(partyPlayer);
						if (partyPlayer.getLevel() > partyLvl)
						{
							partyLvl = partyPlayer.getLevel();
						}
					}
					else
					{
						rewardedMembers.add(partyPlayer);
						if (partyPlayer.getLevel() > partyLvl)
						{
							partyLvl = partyPlayer.getLevel();
						}
					}
				}

				if (partyDmg < totalDamage)
				{
					partyMul = ((float) partyDmg / totalDamage);
				}
				final int levelDiff = partyLvl - getLevel();
				expSp = calculateExpAndSp(levelDiff, partyDmg, totalDamage, attacker);
				double exp_premium = expSp[0];
				double sp_premium = expSp[1];

				expSp = calculateExpAndSp(levelDiff, partyDmg, totalDamage, attacker);
				double exp = expSp[0];
				double sp = expSp[1];

				if (getChampionTemplate() != null)
				{
					exp *= getChampionTemplate().expMultiplier;
					sp *= getChampionTemplate().spMultiplier;
					exp_premium *= getChampionTemplate().expMultiplier;
					sp_premium *= getChampionTemplate().spMultiplier;
				}

				exp *= partyMul;
				sp *= partyMul;
				exp_premium *= partyMul;
				sp_premium *= partyMul;

				final Creature overhitAttacker = getOverhitAttacker();
				if (isOverhit() && (overhitAttacker != null) && (overhitAttacker.getActingPlayer() != null) && (attacker == overhitAttacker.getActingPlayer()))
				{
					attacker.sendPacket(SystemMessageId.OVER_HIT);
					exp += calculateOverhitExp((long) exp);
					exp_premium += calculateOverhitExp((long) exp_premium);
				}

				if (partyDmg > 0)
				{
					attackerParty.distributeXpAndSp((long) exp_premium, (int) sp_premium, (long) exp, (int) sp, rewardedMembers, partyLvl, partyDmg, this);
				}
			}
		}
	}

	@Override
	public void addAttackerToAttackByList(Creature creature)
	{
		if ((creature == null) || (creature == this))
		{
			return;
		}
		
		for (final var ref : getAttackByList())
		{
			if (ref.get() == creature)
			{
				return;
			}
		}
		getAttackByList().add(new WeakReference<>(creature));
	}

	public void addDamage(Creature attacker, int damage, Skill skill)
	{
		if (attacker == null)
		{
			return;
		}

		if (!isDead())
		{
			try
			{
				if (isWalker() && !isCoreAIDisabled() && WalkingManager.getInstance().isOnWalk(this))
				{
					WalkingManager.getInstance().stopMoving(this, false, true);
				}
				
				getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, damage);

				if (attacker.isPlayable())
				{
					final Player player = attacker.getActingPlayer();
					if (player != null)
					{
						if (getTemplate().getEventQuests(QuestEventType.ON_ATTACK) != null)
						{
							for (final Quest quest : getTemplate().getEventQuests(QuestEventType.ON_ATTACK))
							{
								quest.notifyAttack(this, player, damage, attacker.isSummon(), skill);
							}
						}
					}
				}
			}
			catch (final Exception e)
			{
				_log.warn("", e);
			}
		}
	}
	
	public void addDamageHate(Creature attacker, long damage, long aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!isDead())
		{
			getAggroList().addAggro(attacker, damage, aggro);
		}
	}
	
	private void calcVipPointsReward(long totalPoints)
	{
		Map<Object, GroupInfo> groupsInfo = new HashMap<>();
		final double totalHp = getMaxHp();

		var charMap = getAggroList().getCharMap();
		for (final var player : charMap.keySet())
		{
			if (player == null)
			{
				continue;
			}
			
			final var key = player.getParty() != null ? player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel() : player.getParty() : player.getActingPlayer();
			GroupInfo info = groupsInfo.get(key);
			if (info == null)
			{
				info = new GroupInfo();
				groupsInfo.put(key, info);
			}
			
			switch (key) {
				case CommandChannel channel -> {
					for (final Player p : channel)
					{
						if (p.isInRangeZ(this, Config.ALT_PARTY_RANGE2))
						{
							info.getPlayer().add(p);
						}
					}
				}
				case Party party -> {
					for (final Player p : party.getMembers())
					{
						if (p.isInRangeZ(this, Config.ALT_PARTY_RANGE2))
						{
							info.getPlayer().add(p);
						}
					}
				}
				default -> info.getPlayer().add(player);
			}
			info.addReward(charMap.get(player).getDamage());
		}
		charMap = null;
		
		for (final GroupInfo groupInfo : groupsInfo.values())
		{
			final HashSet<Player> players = groupInfo.getPlayer();
			final int perPlayer = (int) Math.round(totalPoints * groupInfo.getReward() / (totalHp * players.size()));
			for (final Player player : players)
			{
				if (player != null)
				{
					int playerReward = perPlayer;
					playerReward = (int) Math.round(playerReward * ExperienceParser.getInstance().penaltyModifier(calculateLevelDiffForDrop(player.getLevel()), 9));
					if (playerReward == 0)
					{
						continue;
					}
					player.setVipPoints(player.getVipPoints() + playerReward);
				}
			}
		}
		groupsInfo = null;
	}
	
	public void doItemDrop(Creature lastAttacker, Creature mainDamageDealer)
	{
		doItemDrop(getTemplate(), lastAttacker, mainDamageDealer);
	}

	public void doItemDrop(NpcTemplate npcTemplate, Creature lastAttacker, Creature mainDamageDealer)
	{
		if (mainDamageDealer == null)
		{
			return;
		}

		final Player player = mainDamageDealer.getActingPlayer();
		if (player == null || mainDamageDealer.isFakePlayer())
		{
			return;
		}
		
		if (Config.ALLOW_VIP_SYSTEM)
		{
			final var vipNpc = VipManager.getInstance().getNpcTemplate(getId());
			if (vipNpc != null)
			{
				calcVipPointsReward(vipNpc.getPoints());
			}
		}

		if (isMonster() && getReflectionId() == 0)
		{
			CursedWeaponsManager.getInstance().checkDrop(this, player);
		}
		
		if (isSiegeGuard() && Config.EPAULETTE_ONLY_FOR_REG)
		{
			return;
		}
		
		player.getCounters().addAchivementInfo("killbyId", getId(), -1, false, true, false);
		
		if (isMonster() && !isRaid() && !isMinion())
		{
			player.getCounters().addAchivementInfo("monsterKiller", getId(), -1, false, true, false);
		}
		
		for (final Map.Entry<RewardType, RewardList> entry : npcTemplate.getRewards().entrySet())
		{
			rollRewards(entry, lastAttacker, mainDamageDealer);
		}
		
		if (!isSpoiled() && player.isPassiveSpoil())
		{
			final var items = takeSweep();
			if (items != null && items.size() > 0)
			{
				_isPassiveSweepActive = true;
				final var overweight = !player.isInventoryUnderRepcent(false, 100.);
				for (final var item : items)
				{
					if (player.isInParty() && player.getParty().getLootDistribution() == 2)
					{
						player.getParty().distributeItem(player, item._itemId, item._count, true, this);
						continue;
					}
					
					final var template = item.getTemplate();
					if (template == null)
					{
						continue;
					}
					
					if (overweight && (!template.isStackable() || (template.isStackable() && player.getInventory().getItemByItemId(item._itemId) == null)))
					{
						continue;
					}
					player.addItem("Sweeper", item._itemId, item._count, this, true);
				}
				DecayTaskManager.getInstance().changeObjectTime(this, (getDeathTime() + 2000L));
			}
		}
		
		if (getChampionTemplate() != null)
		{
			player.getCounters().addAchivementInfo("championKiller", getId(), -1, false, true, false);
			final double mod = 1.0 * ExperienceParser.getInstance().penaltyModifier(calculateLevelDiffForDrop(player.getLevel()), 9);
			if (mod > 0)
			{
				for (final ChampionRewardItem ri : getChampionTemplate().rewards)
				{
					if (ri != null)
					{
						if (Rnd.get(100) < (ri.getDropChance() * mod))
						{
							final long count = Rnd.get(ri.getMinCount(), ri.getMaxCount());
							final Item itemTemplate = ItemsParser.getInstance().getTemplate(ri.getItemId());
							if (itemTemplate != null)
							{
								final ItemHolder item = new ItemHolder(ri.getItemId(), count);
								if ((((player.getUseAutoLoot() || Config.AUTO_LOOT || player.getFarmSystem().isAutofarming()) && !itemTemplate.isHerb()) || isFlying()) || ((player.getUseAutoLootHerbs() || Config.AUTO_LOOT_HERBS) && itemTemplate.isHerb()))
								{
									player.doAutoLoot(this, itemTemplate.getId(), count);
								}
								else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
								{
									if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
									{
										player.doAutoLoot(this, item);
									}
									else
									{
										dropItem(player, item);
									}
								}
								else
								{
									dropItem(player, item);
								}
							}
						}
					}
				}
			}
		}

		if (!EventsDropManager.getInstance().getEventRules().isEmpty())
		{
			final int rewardItem[] = EventsDropManager.getInstance().calculateRewardItem(npcTemplate, mainDamageDealer);
			if ((rewardItem[0] > 0) && (rewardItem[1] > 0))
			{
				final ItemHolder item = new ItemHolder(rewardItem[0], rewardItem[1]);
				if (((player.getUseAutoLoot() || Config.AUTO_LOOT || player.getFarmSystem().isAutofarming()) || isFlying()))
				{
					player.doAutoLoot(this, item.getId(), item.getCount());
				}
				else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
				{
					if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
					{
						player.doAutoLoot(this, item);
					}
					else
					{
						dropItem(player, item);
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
		ItemsAutoDestroy.getInstance().tryRecalcTime();
	}
	
	public void rollRewards(Map.Entry<RewardType, RewardList> entry, final Creature lastAttacker, Creature topDamager)
	{
		final RewardType type = entry.getKey();
		final RewardList list = entry.getValue();

		final Creature activeChar = (type == RewardType.SWEEP ? lastAttacker : topDamager);
		if (activeChar == null)
		{
			return;
		}
		
		final Player activePlayer = activeChar.getActingPlayer();
		if (activePlayer == null)
		{
			return;
		}
		
		if (type == RewardType.SWEEP && !isSpoiled() && !activePlayer.isPassiveSpoil())
		{
			return;
		}

		final boolean isSiegeGuard = isSiegeGuard();
		final int diff = calculateLevelDiffForDrop(topDamager.getLevel());
		final double mod = 1.0;
		final double penaltyMod = isSiegeGuard && Config.EPAULETTE_WITHOUT_PENALTY ? 1 : ExperienceParser.getInstance().penaltyModifier(diff, 9);
		final List<RewardItem> rewardItems = list.roll(activePlayer, penaltyMod, mod, this);
		switch (type)
		{
			case SWEEP :
				_sweepItems.set(rewardItems);
				break;
			default :
				for (final RewardItem drop : rewardItems)
				{
					if (isSeeded() && getSeed() != null && !drop.isAdena())
					{
						continue;
					}

					if (isFlying() || (!drop.isHerb() && (((activePlayer.getUseAutoLoot() || activePlayer.getFarmSystem().isAutofarming()) && !isRaid()) || (!isRaid() && Config.AUTO_LOOT) || (isRaid() && Config.AUTO_LOOT_RAIDS))) || ((activePlayer.getUseAutoLootHerbs() || Config.AUTO_LOOT_HERBS) && drop.isHerb()))
					{
						activePlayer.doAutoLoot(this, drop._itemId, (int) drop._count);
					}
					else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
					{
						if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, drop._itemId) >= 0)
						{
							activePlayer.doAutoLoot(this, new ItemHolder(drop._itemId, (int) drop._count));
						}
						else
						{
							dropItem(activePlayer, drop._itemId, (int) drop._count);
						}
					}
					else
					{
						dropItem(activePlayer, drop._itemId, (int) drop._count);
					}

					if (isRaid() && !isRaidMinion())
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DIED_DROPPED_S3_S2);
						sm.addCharName(this);
						sm.addItemName(drop._itemId);
						sm.addItemNumber((int) drop._count);
						broadcastPacket(sm);
					}
				}
				break;
		}
	}

	public ItemInstance dropItem(Player mainDamageDealer, ItemHolder item)
	{
		if (Config.DISABLE_ITEM_DROP_LIST.contains(item.getId()))
		{
			return null;
		}
		
		final var template = ItemsParser.getInstance();
		ItemInstance ditem = null;
		for (int i = 0; i < item.getCount(); i++)
		{
			final var pos = Location.findAroundPosition(this, 100);
			if (template.getTemplate(item.getId()) != null)
			{
				ditem = template.createItem("Loot", item.getId(), item.getCount(), mainDamageDealer, this);
				ditem.getDropProtection().protect(mainDamageDealer, isRaid());
				ditem.dropMe(this, pos, false);

				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getId()))
				{
					if (((Config.AUTODESTROY_ITEM_AFTER > 0) && (!ditem.getItem().isHerb())) || ((Config.HERB_AUTO_DESTROY_TIME > 0) && (ditem.getItem().isHerb())))
					{
						ItemsAutoDestroy.getInstance().addItem(ditem, 0);
					}
				}
				ditem.setProtected(false);

				if (ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP)
				{
					break;
				}
			}
			else
			{
				_log.warn("Item doesn't exist so cannot be dropped. Item ID: " + item.getId());
			}
		}
		return ditem;
	}
	
	public ItemInstance dropSingleItem(Player mainDamageDealer, ItemHolder item)
	{
		if (Config.DISABLE_ITEM_DROP_LIST.contains(item.getId()))
		{
			return null;
		}
		
		final var manager = ItemsAutoDestroy.getInstance();
		final var template = ItemsParser.getInstance();
		ItemInstance ditem = null;
		for (int i = 0; i < item.getCount(); i++)
		{
			final var pos = Location.findAroundPosition(this, 100);
			if (template.getTemplate(item.getId()) != null)
			{
				ditem = template.createItem("Loot", item.getId(), item.getCount(), mainDamageDealer, this);
				ditem.getDropProtection().protect(mainDamageDealer, isRaid());
				ditem.dropMe(this, pos, false);
				
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getId()))
				{
					if (((Config.AUTODESTROY_ITEM_AFTER > 0) && (!ditem.getItem().isHerb())) || ((Config.HERB_AUTO_DESTROY_TIME > 0) && (ditem.getItem().isHerb())))
					{
						if (manager.addItem(ditem, 0))
						{
							manager.tryRecalcTime();
						}
					}
				}
				ditem.setProtected(false);
				
				if (ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP)
				{
					break;
				}
			}
			else
			{
				_log.warn("Item doesn't exist so cannot be dropped. Item ID: " + item.getId());
			}
		}
		return ditem;
	}
	
	public ItemInstance dropSingleItem(Player lastAttacker, int itemId, long itemCount)
	{
		return dropSingleItem(lastAttacker, new ItemHolder(itemId, itemCount));
	}

	public ItemInstance dropItem(Player lastAttacker, int itemId, long itemCount)
	{
		return dropItem(lastAttacker, new ItemHolder(itemId, itemCount));
	}

	public ItemInstance getActiveWeapon()
	{
		return null;
	}

	public boolean noTarget()
	{
		return getAggroList().isEmpty();
	}

	public boolean containsTarget(Player player)
	{
		return getAggroList().getCharMap().containsKey(player);
	}

	public void clearAggroList(boolean onlyHate)
	{
		getAggroList().clear(onlyHate);
		stopAttackStanceTask();
		_targetList.clear();
		_overhit = false;
		_overhitDamage = 0;
		_overhitAttacker = null;
	}

	@Override
	public boolean isSweepActive()
	{
		return _sweepItems.get() != null;
	}

	public List<Item> getSpoilLootItems()
	{
		final var sweepItems = _sweepItems.get();
		final List<Item> lootItems = new ArrayList<>();
		if (sweepItems != null)
		{
			for (final var item : sweepItems)
			{
				lootItems.add(ItemsParser.getInstance().createDummyItem(item._itemId).getItem());
			}
		}
		return lootItems;
	}

	public Collection<RewardItem> takeSweep()
	{
		return _sweepItems.getAndSet(null);
	}
	
	public ItemHolder takeHarvest()
	{
		return _harvestItems.getAndSet(null);
	}

	public boolean isOldCorpse(Player attacker, int remainingTime, boolean sendMessage)
	{
		if (isDead() && (System.currentTimeMillis() - getDeathTime()) > remainingTime)
		{
			if (sendMessage && (attacker != null))
			{
				attacker.sendPacket(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED);
			}
			return true;
		}
		return false;
	}

	public boolean checkSpoilOwner(Player sweeper, boolean sendMessage)
	{
		if ((sweeper.getObjectId() != _spoilerObjectId) && !sweeper.isInLooterParty(_spoilerObjectId))
		{
			if (sendMessage)
			{
				sweeper.sendPacket(SystemMessageId.SWEEP_NOT_ALLOWED);
			}
			return false;
		}
		return true;
	}

	public void overhitEnabled(boolean status)
	{
		_overhit = status;
	}

	public void setOverhitValues(Creature attacker, double damage)
	{
		final double overhitDmg = -(getCurrentHp() - damage);
		if (overhitDmg < 0)
		{
			overhitEnabled(false);
			_overhitDamage = 0;
			_overhitAttacker = null;
			return;
		}
		overhitEnabled(true);
		_overhitDamage = overhitDmg;
		_overhitAttacker = attacker;
	}

	public Creature getOverhitAttacker()
	{
		return _overhitAttacker;
	}

	public double getOverhitDamage()
	{
		return _overhitDamage;
	}

	public boolean isOverhit()
	{
		return _overhit;
	}

	private double[] calculateExpAndSp(int diff, long damage, long totalDamage, Creature attacker)
	{
		double xp;
		double sp;
		
		xp = ((double) getExpReward(attacker) * damage) / totalDamage;
		if ((Config.ALT_GAME_EXPONENT_XP != 0) && (Math.abs(diff) >= Config.ALT_GAME_EXPONENT_XP))
		{
			xp = 0;
		}
		
		sp = ((double) getSpReward(attacker) * damage) / totalDamage;
		if ((Config.ALT_GAME_EXPONENT_SP != 0) && (Math.abs(diff) >= Config.ALT_GAME_EXPONENT_XP))
		{
			sp = 0;
		}
		
		if ((Config.ALT_GAME_EXPONENT_XP == 0) && (Config.ALT_GAME_EXPONENT_SP == 0))
		{
			if (diff < -5)
			{
				diff = -5;
			}
			
			if (diff > 5)
			{
				final double pow = Math.pow((double) 5 / 6, diff - 5);
				xp = xp * pow;
				sp = sp * pow;
			}
		}
		
		xp = Math.max(0., xp);
		sp = Math.max(0., sp);
		
		return new double[]
		{
		        xp, sp
		};
	}

	public long calculateOverhitExp(long normalExp)
	{
		double overhitPercentage = ((getOverhitDamage() * 100) / getMaxHp());

		if (overhitPercentage > 25)
		{
			overhitPercentage = 25;
		}

		final double overhitExp = ((overhitPercentage / 100) * normalExp);

		final long bonusOverhit = Math.round(overhitExp);
		return bonusOverhit;
	}

	@Override
	public boolean isAttackable()
	{
		return getId() == 18837 ? false : true;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_SPAWN), 100);
		clearAbsorbers();
		_isPassiveSweepActive = false;
		_seeded = false;
		_seed = null;
		_seederObjId = 0;
		overhitEnabled(false);
		setWalking();
		if (getChampionTemplate() != null)
		{
			for (final var effect : getChampionTemplate().abnormalEffect)
			{
				if (effect != null)
				{
					startAbnormalEffect(effect);
				}
			}
		}
		final var ai = getAI();
		if (ai != null && (isInActiveRegion() || isGlobalAI()))
		{
			ai.setIntention(CtrlIntention.ACTIVE);
			ai.startAITask();
		}
	}
	
	@Override
	public void onRandomAnimation()
	{
		broadcastPacket(new SocialAction(getObjectId(), 1));
	}

	@Override
	public boolean isSpoiled()
	{
		return _spoilerObjectId != 0;
	}

	public final int getSpoilerObjectId()
	{
		return _spoilerObjectId;
	}

	public final void setSpoilerObjectId(int value)
	{
		_spoilerObjectId = value;
	}

	public void setSeeded(Player seeder)
	{
		if ((_seed != null) && (_seederObjId == seeder.getObjectId()))
		{
			_seeded = true;
			
			int count = 1;
			for (final int skillId : getTemplate().getSkills().keySet())
			{
				switch (skillId)
				{
					case 4303 :
						count *= 2;
						break;
					case 4304 :
						count *= 3;
						break;
					case 4305 :
						count *= 4;
						break;
					case 4306 :
						count *= 5;
						break;
					case 4307 :
						count *= 6;
						break;
					case 4308 :
						count *= 7;
						break;
					case 4309 :
						count *= 8;
						break;
					case 4310 :
						count *= 9;
						break;
				}
			}
			
			final int diff = getLevel() - _seed.getLevel() - 5;
			if (diff > 0)
			{
				count += diff;
			}
			_harvestItems.set(new ItemHolder(_seed.getCropId(), (long) (count * Config.RATE_DROP_MANOR)));
		}
	}
	
	public final void setSeeded(Seed seed, Player seeder)
	{
		if (!_seeded)
		{
			_seed = seed;
			_seederObjId = seeder.getObjectId();
		}
	}

	public int getSeederId()
	{
		return _seederObjId;
	}

	public final Seed getSeed()
	{
		return _seed;
	}

	@Override
	public boolean isSeeded()
	{
		return _seeded;
	}

	public final void setOnKillDelay(int delay)
	{
		_onKillDelay = delay;
	}

	public final int getOnKillDelay()
	{
		return _onKillDelay;
	}
	
	public final void setFindTargetDelay(int delay)
	{
		_findTargetDelay = (System.currentTimeMillis() + delay);
	}
	
	public final long getFindTargetDelay()
	{
		return _findTargetDelay;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return ((Config.MAX_MONSTER_ANIMATION > 0) && isRandomAnimationEnabled() && !(this instanceof GrandBossInstance));
	}

	@Override
	public boolean isMob()
	{
		return true;
	}

	public void returnHome()
	{
		clearAggroList(false);
		final var ai = getAI();
		if (ai != null && (getSpawn() != null))
		{
			ai.setIntention(CtrlIntention.MOVING, getSpawn().getLocation(), 0);
		}
	}

	public double getVitalityPoints(long damage)
	{
		if (damage <= 0)
		{
			return 0;
		}

		final double divider = getTemplate().getBaseVitalityDivider();
		if (divider == 0)
		{
			return 0;
		}

		return -Math.min(damage, getMaxHp()) / divider;
	}

	public boolean useVitalityRate()
	{
		if (getChampionTemplate() != null && !getChampionTemplate().useVitalityRate)
		{
			return false;
		}
		return true;
	}

	@Override
	public boolean isRaid()
	{
		return _isRaid;
	}
	
	@Override
	public boolean isEpicRaid()
	{
		return _isEpicRaid;
	}
	
	public void setIsEpicRaid(boolean isEpicRaid)
	{
		_isEpicRaid = isEpicRaid;
	}

	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}
	
	@Override
	public boolean isSiegeGuard()
	{
		return _isSiegeGuard;
	}
	
	public void setIsSiegeGuard(boolean isSiegeGuard)
	{
		_isSiegeGuard = isSiegeGuard;
	}
	
	@Override
	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isRaidMinion = val;
	}
	
	@Override
	public boolean isRaidMinion()
	{
		return _isRaidMinion;
	}
	
	@Override
	public boolean isLethalImmune()
	{
		return _isLethalImmune;
	}
	
	public void setIsLethalImmune(boolean isLethalImmune)
	{
		_isLethalImmune = isLethalImmune;
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return _isGlobalAI;
	}
	
	public void setIsGlobalAI(boolean isGlobalAI)
	{
		_isGlobalAI = isGlobalAI;
	}

	public boolean canShowLevelInTitle()
	{
		return !(getName("en").equals("Chest"));
	}

	public void addAbsorber(final Player attacker)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (getCurrentHpPercents() > 50)
		{
			return;
		}
		
		if (_absorbersIds == null)
		{
			_absorbersIds = new HashSet<>();
		}
		_absorbersIds.add(attacker.getObjectId());
	}
	
	public boolean isAbsorbed(Player player)
	{
		if (_absorbersIds == null)
		{
			return false;
		}
		if (!_absorbersIds.contains(player.getObjectId()))
		{
			return false;
		}
		return true;
	}
	
	public void clearAbsorbers()
	{
		if (_absorbersIds != null)
		{
			_absorbersIds.clear();
		}
	}
	
	@Override
	public boolean canBeAttacked()
	{
		return true;
	}
	
	@Override
	public void removeInfoObject(GameObject object)
	{
		super.removeInfoObject(object);
		
		if (object.isPlayable() || object.isAttackable())
		{
			_aggroList.remove(object.getObjectId());
		}
		
		if (isInBlockList(object.getObjectId()))
		{
			_blockList.remove(object.getObjectId());
		}
		
		if (_aggroList.isEmpty())
		{
			stopAttackStanceTask();
			_targetList.clear();
		}
		getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
	}
	
	public AggroList getAggroList()
	{
		return _aggroList;
	}
	
	@Override
	public boolean isPassiveSweepActive()
	{
		return _isPassiveSweepActive;
	}
	
	public void addToBlockList(int objectId)
	{
		if (!_blockList.contains(objectId))
		{
			_blockList.add(objectId);
		}
	}
	
	public boolean isInBlockList(int objectId)
	{
		return _blockList.contains(objectId);
	}
	
	public void clearBlockList()
	{
		_blockList.clear();
	}
	
	public void addToTargetList(Player player)
	{
		if (!_targetList.contains(player.getObjectId()))
		{
			final var party = player.getParty();
			if (party != null)
			{
				party.getMembers().stream().filter(p -> p != null).forEach(m -> _targetList.add(m.getObjectId()));
			}
			else
			{
				_targetList.add(player.getObjectId());
			}
		}
	}
	
	public boolean isInTargetList(Player player)
	{
		return getCurrentHpPercents() > 98 ? true : _targetList.isEmpty() || _targetList.contains(player.getObjectId());
	}
	
	@Override
	public void wakeUp(boolean isActive)
	{
		final var ai = getAI();
		if (isActive)
		{
			getStatus().startHpMpRegeneration();
			if (ai != null)
			{
				ai.setIntention(CtrlIntention.ACTIVE);
				ai.startAITask();
			}
		}
		else
		{
			if (isDead() || isGlobalAI() || (getLeader() != null && !getLeader().isVisible()))
			{
				return;
			}
			
			setTarget(null);
			stopMove(null);
			clearAggroList(true);
			getAttackByList().clear();
			if (ai != null)
			{
				ai.setIntention(CtrlIntention.IDLE);
				ai.stopAITask();
			}
		}
	}
	
	@Override
	public void setTarget(GameObject target)
	{
		if (target != null && target.isPlayer() && !isDead())
		{
			if (getTarget() != target)
			{
				addToTargetList(target.getActingPlayer());
			}
		}
		super.setTarget(target);
	}
}