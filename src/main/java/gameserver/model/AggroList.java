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
package gameserver.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.ai.guard.FortGuardAI;
import gameserver.ai.guard.GuardAI;
import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.DamageLimitParser;
import gameserver.geodata.GeoEngine;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.aggro.AggroInfo;
import gameserver.model.actor.templates.npc.aggro.DamageInfo;
import gameserver.model.actor.templates.npc.aggro.RewardInfo;
import gameserver.model.quest.QuestEventType;
import gameserver.utils.comparators.AggroAttackedComparator;
import gameserver.utils.comparators.AggroDamageComparator;
import gameserver.utils.comparators.AggroHateComparator;

/**
 * Created by LordWinter
 */
public class AggroList
{
	private final Attackable _npc;
	private final Map<Integer, AggroInfo> _aggroList = new ConcurrentHashMap<>();
	private long _totalDamage = 0;
	
	public AggroList(Attackable npc)
	{
		_npc = npc;
	}

	public AggroInfo get(int objectId)
	{
		return _aggroList.get(objectId);
	}
	
	public void clear(boolean onlyHate)
	{
		if (_aggroList.isEmpty())
		{
			_totalDamage = 0;
			return;
		}
		
		if (!onlyHate)
		{
			_aggroList.clear();
			_totalDamage = 0;
			return;
		}
		
		AggroInfo ai;
		for (final var entry : _aggroList.entrySet())
		{
			ai = entry.getValue();
			ai.stopHate();
			if (ai.getDamage() == 0)
			{
				_aggroList.remove(entry.getKey());
			}
		}
	}
	
	public boolean isEmpty()
	{
		return _aggroList.isEmpty();
	}
	
	public List<Creature> getHateList()
	{
		if (_aggroList.isEmpty())
		{
			return Collections.emptyList();
		}
		
		final var hated = _aggroList.values().toArray(new AggroInfo[_aggroList.size()]);
		if (hated.length == 0)
		{
			return Collections.emptyList();
		}
		
		try
		{
			Arrays.sort(hated, AggroHateComparator.getInstance());
		}
		catch (final Exception e)
		{
		}
		
		if (hated[0] == null || hated[0].getHate() == 0)
		{
			return Collections.emptyList();
		}

		final List<Creature> hateList = new ArrayList<>();
		for (final var ai : hated)
		{
			if (ai == null || (ai.getHate() == 0 && ai.getDamage() == 0))
			{
				continue;
			}

			final var cha = getOrRemoveHated(ai.getAttackerId(), true);
			if (cha == null)
			{
				continue;
			}
			hateList.add(cha);
		}
		return hateList;
	}
	
	public List<AggroInfo> getAggroInfo()
	{
		if (_aggroList.isEmpty())
		{
			return Collections.emptyList();
		}
		
		final var hated = _aggroList.values().toArray(new AggroInfo[_aggroList.size()]);
		if (hated.length == 0)
		{
			return Collections.emptyList();
		}
		
		try
		{
			Arrays.sort(hated, AggroHateComparator.getInstance());
		}
		catch (final Exception e)
		{
		}
		
		if (hated[0] == null)
		{
			return Collections.emptyList();
		}
		
		final List<AggroInfo> hateList = new ArrayList<>();
		for (final var ai : hated)
		{
			if (ai == null || ai.getHate() == 0 && ai.getDamage() == 0)
			{
				continue;
			}
			
			final var cha = getOrRemoveHated(ai.getAttackerId(), true);
			if (cha == null)
			{
				continue;
			}
			hateList.add(ai);
		}
		return hateList;
	}
	
	public List<AggroInfo> getDamageInfo()
	{
		if (_aggroList.isEmpty())
		{
			return Collections.emptyList();
		}
		
		final var hated = _aggroList.values().toArray(new AggroInfo[_aggroList.size()]);
		if (hated.length == 0)
		{
			return Collections.emptyList();
		}
		
		try
		{
			Arrays.sort(hated, AggroAttackedComparator.getInstance());
		}
		catch (final Exception e)
		{
		}
		
		if (hated[0] == null)
		{
			return Collections.emptyList();
		}
		
		final List<AggroInfo> hateList = new ArrayList<>();
		for (final var ai : hated)
		{
			if (ai == null || ai.getHate() == 0 && ai.getDamage() == 0)
			{
				continue;
			}
			
			final var cha = getOrRemoveHated(ai.getAttackerId(), true);
			if (cha == null)
			{
				continue;
			}
			hateList.add(ai);
		}
		return hateList;
	}
	
	private Creature getOrRemoveHated(int objectId, boolean allowAll)
	{
		final var object = GameObjectsStorage.findObject(objectId);
		if (object == null || !object.isCreature())
		{
			remove(objectId);
			return null;
		}

		final var character = (Creature) object;
		if (character.isPlayer())
		{
			final var player = character.getActingPlayer();
			if (player != null)
			{
				if ((player.isDead() && !allowAll) || player.isSpawnProtected())
				{
					stopHating(character);
					return null;
				}
				
				if (!player.isOnline())
				{
					remove(objectId);
					return null;
				}
			}
		}
		return character;
	}
	
	public void remove(int objectId)
	{
		final var ai = _aggroList.get(objectId);
		if (ai != null)
		{
			if (ai.getDamage() == 0)
			{
				_aggroList.remove(objectId);
			}
			else
			{
				ai.stopHate();
			}
		}
	}
	
	public void addAggro(Creature attacker, long damage, long aggro)
	{
		if (attacker == null || attacker.isConfused())
		{
			return;
		}
		
		damage = Math.max(damage, 0);
		
		final var targetPlayer = attacker.getActingPlayer();
		
		AggroInfo ai;
		if ((ai = _aggroList.get(attacker.getObjectId())) == null)
		{
			_aggroList.put(attacker.getObjectId(), ai = new AggroInfo(attacker.isTrap() ? targetPlayer != null ? targetPlayer : attacker : attacker));
		}
		
		if (Config.ALLOW_DAMAGE_LIMIT)
		{
			final var limit = DamageLimitParser.getInstance().getDamageLimit(_npc.getId());
			if (limit != null)
			{
				final int damageLimit = limit.getDamage();
				if (damageLimit > 0 && damage > damageLimit)
				{
					damage = damageLimit;
				}
			}
		}
		
		if (damage > _npc.getCurrentHp())
		{
			damage = (int) _npc.getCurrentHp();
		}
		
		ai.addDamage(damage);
		ai.addHate(aggro);
		
		if (damage > 1)
		{
			if (attacker.isPlayable())
			{
				final var player = attacker.getActingPlayer();
				if (player != null)
				{
					final var eventQuests = _npc.getTemplate().getEventQuests(QuestEventType.ON_ATTACK);
					if (eventQuests != null)
					{
						final int dmg = (int) damage;
						eventQuests.stream().filter(q -> q != null).forEach(q -> q.notifyAttack(_npc, player, dmg, attacker.isSummon(), null));
					}
				}
			}
		}
		
		if ((targetPlayer != null) && (aggro == 0))
		{
			addAggro(attacker.isSummon() ? attacker : targetPlayer, 0, damage > 2 ? (int) (damage * Config.PATK_HATE_MOD) : 1);
			if (_npc.getAI().getIntention() == CtrlIntention.IDLE)
			{
				_npc.getAI().setIntention(CtrlIntention.ACTIVE);
			}
			
			final var eventQuests = _npc.getTemplate().getEventQuests(QuestEventType.ON_AGGRO_RANGE_ENTER);
			if (eventQuests != null)
			{
				eventQuests.stream().filter(q -> q != null).forEach(q -> q.notifyAggroRangeEnter(_npc, targetPlayer, attacker.isSummon()));
			}
		}
		else if ((targetPlayer == null) && (aggro == 0))
		{
			aggro = 1;
			ai.addHate(1);
		}
		
		if ((aggro > 0) && (_npc.getAI().getIntention() == CtrlIntention.IDLE))
		{
			_npc.getAI().setIntention(CtrlIntention.ACTIVE);
		}
	}
	
	public void reduceHate(Creature target, long amount, boolean isAddToBlockList)
	{
		if ((_npc.getAI() instanceof GuardAI) || (_npc.getAI() instanceof FortGuardAI))
		{
			stopHating(target);
			_npc.setTarget(null);
			_npc.getAI().setIntention(CtrlIntention.IDLE);
			return;
		}
		
		final var mostHated = getMostHated();
		if (target == null)
		{
			if (mostHated == null)
			{
				_npc.getAI().setGlobalAggro(-25);
				return;
			}
			final var objects = GameObjectsStorage.getAllObjects();
			for (final var i : _aggroList.entrySet())
			{
				final var ai = i.getValue();
				if (ai == null)
				{
					continue;
				}
				ai.reduceHate(amount);
				if (ai.getHate() <= 0 && isAddToBlockList)
				{
					final var attacker = objects.get(ai.getAttackerId());
					if (attacker != null)
					{
						if (_npc.getDistance(attacker) <= Config.ALT_PARTY_RANGE)
						{
							_npc.addToBlockList(ai.getAttackerId());
						}
					}
				}
			}
			
			amount = getHating(mostHated);
			if (amount <= 0)
			{
				_npc.getAI().setGlobalAggro(-25);
				_npc.clearAggroList(false);
				_npc.getAI().setIntention(CtrlIntention.ACTIVE);
				_npc.setWalking();
			}
			return;
		}
		
		final var ai = get(target.getObjectId());
		if (ai == null)
		{
			return;
		}
		ai.reduceHate(amount);
		
		if (ai.getHate() <= 0)
		{
			if (target != null && isAddToBlockList)
			{
				_npc.addToBlockList(target.getObjectId());
			}
			
			if (mostHated == null)
			{
				_npc.getAI().setGlobalAggro(-25);
				_npc.clearAggroList(false);
				_npc.getAI().setIntention(CtrlIntention.ACTIVE);
				_npc.setWalking();
			}
		}
	}
	
	public long getHating(final Creature target)
	{
		if (_aggroList.isEmpty() || (target == null))
		{
			return 0;
		}
		
		long hate = 0;
		
		final var ai = _aggroList.get(target.getObjectId());
		if (ai != null)
		{
			var stop = false;
			final var attacker = target.getActingPlayer();
			if (attacker != null && !target.isSummon())
			{
				if (attacker.isInvisible() || attacker.isSpawnProtected())
				{
					stopHating(target);
					stop = true;
				}
			}
			
			if (!target.isVisible() || target.isInvisible())
			{
				stopHating(target);
				stop = true;
			}
			
			if (target.isAlikeDead())
			{
				ai.stopHate();
				stop = true;
			}
			
			if (!stop)
			{
				hate = ai.getHate();
			}
		}
		return hate;
	}
	
	public void stopHating(Creature target)
	{
		if (target == null)
		{
			return;
		}
		
		final var ai = get(target.getObjectId());
		if (ai != null)
		{
			ai.stopHate();
		}
	}
	
	public void randomizeHateList()
	{
		if (_npc.isAlikeDead() || _aggroList.isEmpty())
		{
			return;
		}
		
		final var infos = _aggroList.values().toArray(new AggroInfo[_aggroList.size()]);
		final var hates = _aggroList.values().stream().mapToLong(AggroInfo::getHate).boxed().collect(Collectors.toList());
		Collections.shuffle(hates);
		for (int i = 0; i < infos.length; ++i)
		{
			infos[i].setHate(hates.get(i));
		}
	}
	
	public Creature getMostHated()
	{
		if (_npc.isAlikeDead() || _aggroList.isEmpty())
		{
			return null;
		}
		
		final var hated = _aggroList.values().toArray(new AggroInfo[_aggroList.size()]);
		if (hated.length == 0)
		{
			return null;
		}
		
		try
		{
			Arrays.sort(hated, AggroHateComparator.getInstance());
		}
		catch (final Exception e)
		{
		}
		
		if (hated[0] == null || hated[0].getHate() == 0)
		{
			return null;
		}
		
		for (final var ai : hated)
		{
			if (ai == null || ai.getHate() == 0)
			{
				continue;
			}
			
			final var cha = getOrRemoveHated(ai.getAttackerId(), false);
			if (cha == null)
			{
				continue;
			}
			return cha;
		}
		return null;
	}
	
	public Creature getRandomHated(int radius)
	{
		if (_npc.isAlikeDead() || _aggroList.isEmpty())
		{
			return null;
		}
		
		final List<Creature> hateList = new ArrayList<>();
		
		Creature mostHated;
		for (final var i : _aggroList.entrySet())
		{
			final var ai = i.getValue();
			if (ai == null)
			{
				continue;
			}
			
			mostHated = getOrRemoveHated(ai.getAttackerId(), false);
			if (mostHated == null)
			{
				continue;
			}
			
			if (radius == -1 || mostHated.isInRangeZ(_npc.getLocation(), radius))
			{
				if (mostHated.isDead() || !GeoEngine.getInstance().canSeeTarget(_npc, mostHated))
				{
					continue;
				}
				hateList.add(mostHated);
			}
		}
		
		if (hateList.isEmpty())
		{
			mostHated = null;
		}
		else
		{
			mostHated = hateList.get(Rnd.get(hateList.size()));
		}
		return mostHated;
	}
	
	public Creature getTopDamager(Creature lastAttacker)
	{
		if (_aggroList.isEmpty())
		{
			return lastAttacker;
		}
		
		final var chars = World.getAroundPlayables(_npc);
		if (chars.isEmpty())
		{
			return lastAttacker;
		}
		
		DamageInfo[] hated = null;
		final Map<PlayerGroup, DamageInfo> aggroList = new WeakHashMap<>();
		
		for (final var i : _aggroList.entrySet())
		{
			final var info = i.getValue();
			if (info == null)
			{
				continue;
			}
			
			for (final var attacker : chars)
			{
				if (attacker == null)
				{
					continue;
				}
				
				final var player = attacker.getActingPlayer();
				if (player != null && ((player.getObjectId() == info.getAttackerId()) || (attacker.isSummon() && attacker.getObjectId() == info.getAttackerId())) && info.getDamage() > 0)
				{
					final var ai = aggroList.computeIfAbsent(player.getPlayerGroup(), DamageInfo::new);
					if (ai != null)
					{
						ai.addDamage(info.getDamage());
					}
				}
			}
		}
		
		if (!aggroList.isEmpty())
		{
			hated = aggroList.values().toArray(new DamageInfo[aggroList.size()]);
		}
		
		if (hated == null)
		{
			return lastAttacker;
		}
		
		try
		{
			Arrays.sort(hated, AggroDamageComparator.getInstance());
		}
		catch (final Exception e)
		{}
		
		if (hated[0] == null || hated[0].getDamage() == 0)
		{
			return lastAttacker;
		}
		
		DamageInfo ai;
		for (int i = 0; i < hated.length; i++)
		{
			ai = hated[i];
			if (ai == null || ai.getDamage() == 0)
			{
				continue;
			}
			
			if (ai.getGroup() != null && ai.getGroup().getGroupLeader() != null)
			{
				if (lastAttacker != null)
				{
					final var player = lastAttacker.getActingPlayer();
					if (player != null && player.getPlayerGroup().getGroupLeader() == ai.getGroup().getGroupLeader())
					{
						return lastAttacker;
					}
					else
					{
						return ai.getGroup().getGroupLeader();
					}
				}
				else
				{
					return ai.getGroup().getGroupLeader();
				}
			}
		}
		return lastAttacker;
	}
	
	public Map<Player, AggroInfo> getCharMap()
	{
		if (isEmpty())
		{
			return Collections.emptyMap();
		}
		
		final var chars = World.getAroundPlayables(_npc);
		if (chars.isEmpty())
		{
			return Collections.emptyMap();
		}
		
		final Map<Player, AggroInfo> aggroMap = new WeakHashMap<>();
		
		for (final var i : _aggroList.entrySet())
		{
			final var ai = i.getValue();
			if (ai == null || ((ai.getDamage() == 0) && (ai.getHate() == 0)))
			{
				continue;
			}
			
			for (final var attacker : chars)
			{
				if (attacker.getObjectId() == ai.getAttackerId())
				{
					final var info = aggroMap.get(attacker.getActingPlayer());
					if (info != null)
					{
						info.addDamage(ai.getDamage());
					}
					else
					{
						aggroMap.put(attacker.getActingPlayer(), ai);
					}
					break;
				}
			}
		}
		return aggroMap;
	}
	
	public Map<Player, AggroInfo> getTotalCharMap()
	{
		if (isEmpty())
		{
			return Collections.emptyMap();
		}
		
		final Map<Player, AggroInfo> aggroMap = new WeakHashMap<>();
		
		final var objects = GameObjectsStorage.getAllObjects();
		for (final var i : _aggroList.entrySet())
		{
			final var ai = i.getValue();
			if (ai == null || ((ai.getDamage() == 0) && (ai.getHate() == 0)))
			{
				continue;
			}
			
			final var attacker = objects.get(ai.getAttackerId());
			if (attacker != null && attacker.isPlayer())
			{
				final var info = aggroMap.get(attacker.getActingPlayer());
				if (info != null)
				{
					info.addDamage(ai.getDamage());
				}
				else
				{
					aggroMap.put(attacker.getActingPlayer(), ai);
				}
			}
		}
		return aggroMap;
	}
	
	public Map<Player, RewardInfo> getCharRewardMap()
	{
		if (isEmpty())
		{
			return Collections.emptyMap();
		}
		
		final var chars = World.getAroundPlayables(_npc, Config.ALT_PARTY_RANGE, 300);
		if (chars.isEmpty())
		{
			return Collections.emptyMap();
		}
		
		final Map<Player, RewardInfo> rewards = new WeakHashMap<>();
		
		for (final var a : _aggroList.entrySet())
		{
			final var ai = a.getValue();
			if (ai == null || ((ai.getDamage() == 0) && (ai.getHate() == 0)))
			{
				continue;
			}
			
			_totalDamage += ai.getDamage();
			
			for (final var attacker : chars)
			{
				if (attacker.getObjectId() == ai.getAttackerId())
				{
					final var i = rewards.computeIfAbsent(attacker.getActingPlayer(), RewardInfo::new);
					if (i != null)
					{
						i.addDamage(ai.getDamage());
					}
					break;
				}
			}
		}
		return rewards;
	}
	
	public long getTotalDamage()
	{
		return _totalDamage;
	}
}