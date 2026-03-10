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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Summon;
import gameserver.model.olympiad.OlympiadGameManager;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectFlag;
import gameserver.model.skills.effects.EffectType;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.AbnormalStatusUpdate;
import gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import gameserver.network.serverpackets.PartySpelled;
import gameserver.network.serverpackets.ShortBuffStatusUpdate;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.network.serverpackets.Interface.ExAbnormalStatusUpdateFromTarget;
import gameserver.utils.comparators.EffectsComparator;

public class CharEffectList
{
	private static final Effect[] EMPTY_EFFECTS = new Effect[0];
	
	private final List<Effect> _buffs;
	private final List<Effect> _debuffs;
	private final List<Effect> _passives;
	
	private final Map<String, List<Effect>> _stackedEffects;
	
	private volatile boolean _hasBuffsRemovedOnAnyAction = false;
	private volatile boolean _hasBuffsRemovedOnDamage = false;
	private volatile boolean _hasDebuffsRemovedOnDamage = false;
	
	private boolean _queuesInitialized = false;
	private LinkedBlockingQueue<Effect> _addQueue;
	private LinkedBlockingQueue<Effect> _removeQueue;
	private final AtomicBoolean _queueLock = new AtomicBoolean();
	private int _effectFlags;
	
	private boolean _partyOnly = false;
	private final Creature _owner;
	
	private Effect[] _effectCache;
	private volatile boolean _rebuildCache = true;
	private final Object _buildEffectLock = new Object();
	private Future<?> _effectIconsUpdate;
	private final Set<String> _blockedBuffSlots;
	private Effect _shortBuff = null;
	
	public CharEffectList(Creature owner)
	{
		_owner = owner;
		_buffs = new CopyOnWriteArrayList<>();
		_debuffs = new CopyOnWriteArrayList<>();
		_passives = new CopyOnWriteArrayList<>();
		_stackedEffects = new ConcurrentHashMap<>();
		_blockedBuffSlots = ConcurrentHashMap.newKeySet();
		init();
	}
	
	public final Effect[] getAllEffects()
	{
		if (isEmpty())
		{
			return EMPTY_EFFECTS;
		}
		
		synchronized (_buildEffectLock)
		{
			if (!_rebuildCache)
			{
				return _effectCache;
			}
			
			_rebuildCache = false;
			
			final List<Effect> temp = new ArrayList<>();
			temp.addAll(_buffs);
			temp.addAll(_debuffs);
			
			final var tempArray = new Effect[temp.size()];
			temp.toArray(tempArray);
			return (_effectCache = tempArray);
		}
	}
	
	public final Effect[] getAllSortEffects()
	{
		if (isEmpty())
		{
			return new Effect[0];
		}
		final var effects = getAllEffects();
		try
		{
			Arrays.sort(effects, EffectsComparator.getInstance());
		}
		catch (final Exception e)
		{
		}
		return effects;
	}
	
	public int getEffectTypeAmount(EffectType tp)
	{
		int i = 0;
		
		for (final var e : _buffs)
		{
			if (e == null)
			{
				continue;
			}
			
			if (e.getEffectType() == tp)
			{
				i++;
			}
		}
		return i;
	}
	
	public final Effect getFirstEffect(EffectType tp)
	{
		Effect effectNotInUse = null;
		
		if (!_buffs.isEmpty())
		{
			for (final var e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getEffectType() == tp)
				{
					if (e.isInUse())
					{
						return e;
					}
					effectNotInUse = e;
				}
			}
		}
		
		if (effectNotInUse == null && !_debuffs.isEmpty())
		{
			for (final var e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getEffectType() == tp)
				{
					if (e.isInUse())
					{
						return e;
					}
					effectNotInUse = e;
				}
			}
		}
		return effectNotInUse;
	}
	
	public final Effect getFirstEffect(Skill skill)
	{
		Effect effectNotInUse = null;
		
		if (skill.isDebuff())
		{
			if (!_debuffs.isEmpty())
			{
				for (final var e : _debuffs)
				{
					if (e != null && e.getSkill() == skill)
					{
						if (e.isInUse())
						{
							return e;
						}
						effectNotInUse = e;
					}
				}
			}
		}
		else
		{
			if (!_buffs.isEmpty())
			{
				for (final var e : _buffs)
				{
					if (e != null && e.getSkill() == skill)
					{
						if (e.isInUse())
						{
							return e;
						}
						effectNotInUse = e;
					}
				}
			}
		}
		return effectNotInUse;
	}
	
	public final Effect getFirstEffect(String abnormalType)
	{
		for (final var effect : getEffects())
		{
			if (effect == null)
			{
				continue;
			}
			if (effect.getAbnormalType().equals(abnormalType))
			{
				return effect;
			}
		}
		return null;
	}
	
	public final Effect getFirstAbnormalType(Skill skill)
	{
		if ((skill._effectTemplates == null) || (skill._effectTemplates.length < 1) || (skill._effectTemplates[0].abnormalType == null) || "none".equals(skill._effectTemplates[0].abnormalType))
		{
			return null;
		}
		
		final String stackType = skill._effectTemplates[0].abnormalType;
		
		if (!_buffs.isEmpty())
		{
			for (final var e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				
				final String abnormal = e.getAbnormalType();
				if (abnormal == null || "none".equals(abnormal) || !stackType.equalsIgnoreCase(abnormal))
				{
					continue;
				}
				
				if (e.getAbnormalLvl() > skill.getAbnormalLvl())
				{
					return e;
				}
			}
		}
		return null;
	}
	
	public final Effect getFirstEffect(int skillId)
	{
		Effect effectNotInUse = null;
		
		if (!_buffs.isEmpty())
		{
			for (final var e : _buffs)
			{
				if (e != null && e.getSkill().getId() == skillId)
				{
					if (e.isInUse())
					{
						return e;
					}
					effectNotInUse = e;
				}
			}
		}
		
		if (effectNotInUse == null && !_debuffs.isEmpty())
		{
			for (final var e : _debuffs)
			{
				if (e != null && e.getSkill().getId() == skillId)
				{
					if (e.isInUse())
					{
						return e;
					}
					effectNotInUse = e;
				}
			}
		}
		return effectNotInUse;
	}
	
	public final Effect getFirstPassiveEffect(EffectType type)
	{
		if (!_passives.isEmpty())
		{
			for (final var e : _passives)
			{
				if ((e != null) && (e.getEffectType() == type))
				{
					if (e.isInUse())
					{
						return e;
					}
				}
			}
		}
		return null;
	}
	
	private boolean doesStack(Skill checkSkill)
	{
		if (_buffs.isEmpty() || (checkSkill._effectTemplates == null) || (checkSkill._effectTemplates.length < 1) || (checkSkill._effectTemplates[0].abnormalType == null) || "none".equals(checkSkill._effectTemplates[0].abnormalType))
		{
			return false;
		}
		
		final String stackType = checkSkill._effectTemplates[0].abnormalType;
		
		for (final var e : _buffs)
		{
			if (e != null && e.getAbnormalType().equalsIgnoreCase(stackType))
			{
				return true;
			}
		}
		return false;
	}
	
	public int getBuffCount()
	{
		if (_buffs.isEmpty())
		{
			return 0;
		}
		
		final List<Integer> buffIds = new LinkedList<>();
		for (final var e : _buffs)
		{
			if (e != null && e.getAbnormalTime() > 1 && !e.getEffectTemplate().isWithoutCalcChance() && e.isIconDisplay() && !e.getSkill().isDance() && !e.getSkill().isTriggeredSkill() && !e.getSkill().is7Signs())
			{
				if (!e.getSkill().isPassive() && !e.getSkill().isToggle() && !e.getSkill().isDebuff() && !e.getSkill().isHealingPotionSkill())
				{
					final int skillId = e.getSkill().getId();
					if (!buffIds.contains(skillId))
					{
						buffIds.add(skillId);
					}
				}
			}
		}
		return buffIds.size();
	}
	
	public int getDanceCount()
	{
		if (_buffs.isEmpty())
		{
			return 0;
		}
		
		final List<Integer> list = new LinkedList<>();
		for (final var e : _buffs)
		{
			if (e != null && e.getAbnormalTime() > 1 && !e.getEffectTemplate().isWithoutCalcChance() && e.getSkill().isDance() && e.isInUse() && !e.isInstant())
			{
				final int skillId = e.getSkill().getId();
				if (!list.contains(skillId))
				{
					list.add(skillId);
				}
			}
		}
		return list.size();
	}
	
	private List<Effect> getEffects()
	{
		if (isEmpty())
		{
			return Collections.<Effect> emptyList();
		}
		
		final List<Effect> buffs = new ArrayList<>();
		buffs.addAll(_buffs);
		buffs.addAll(_debuffs);
		return buffs;
	}
	
	public int getTriggeredBuffCount()
	{
		if (_buffs.isEmpty())
		{
			return 0;
		}
		
		final List<Integer> list = new LinkedList<>();
		for (final var e : _buffs)
		{
			if (e != null && e.getAbnormalTime() > 1 && !e.getEffectTemplate().isWithoutCalcChance() && e.getSkill().isTriggeredSkill() && e.isInUse())
			{
				final int skillId = e.getSkill().getId();
				if (!list.contains(skillId))
				{
					list.add(skillId);
				}
			}
		}
		return list.size();
	}
	
	public final void stopAllEffects()
	{
		stopAllEffects(true, true);
	}
	
	public final void stopAllEffects(boolean all, boolean update)
	{
		if (isEmpty())
		{
			return;
		}
		
		var found = false;
		if (all)
		{
			for (final var e : getEffects())
			{
				if (e != null)
				{
					e.exit(true, Config.DISPLAY_MESSAGE, false);
					found = true;
				}
			}
		}
		else
		{
			for (final var e : getEffects())
			{
				if (e != null && e.canBeStolen())
				{
					e.exit(true, Config.DISPLAY_MESSAGE, false);
					found = true;
				}
			}
		}
		
		if (found && update)
		{
			updateEffectList(update);
		}
	}
	
	public void stopAllBuffs()
	{
		stopAllBuffs(true);
	}
	
	public final void stopAllCancelBuffs()
	{
		var update = false;
		for (final var e : _buffs)
		{
			if (e != null && !e.getSkill().isStayAfterDeath() && e.canBeStolen())
			{
				e.exit(false);
				update = true;
			}
		}
		updateEffectList(update);
	}
	
	public final void stopAllBuffs(boolean all)
	{
		var update = false;
		if (all)
		{
			for (final var e : _buffs)
			{
				if (e != null)
				{
					e.exit(false);
					update = true;
				}
			}
		}
		else
		{
			for (final var e : _buffs)
			{
				if (e != null && !e.getSkill().isStayAfterDeath())
				{
					e.exit(false);
					update = true;
				}
			}
		}
		updateEffectList(update);
	}
	
	public final void stopAllReflectionBuffs()
	{
		if (!_buffs.isEmpty())
		{
			var update = false;
			for (final var e : _buffs)
			{
				if (e != null && e.getSkill().isReflectionBuff())
				{
					e.exit(false);
					update = true;
				}
			}
			updateEffectList(update);
		}
	}
	
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		if (!isEmpty())
		{
			var update = false;
			for (final var e : getEffects())
			{
				if (e != null && !e.getSkill().isStayAfterDeath())
				{
					e.exit(false);
					update = true;
				}
			}
			updateEffectList(update);
		}
	}
	
	public void stopAllToggles()
	{
		if (!_buffs.isEmpty())
		{
			var update = false;
			for (final var e : _buffs)
			{
				if (e != null && e.getSkill().isToggle())
				{
					e.exit(false);
					update = true;
				}
			}
			updateEffectList(update);
		}
	}
	
	public void stopAllDances()
	{
		if (!_buffs.isEmpty())
		{
			var update = false;
			for (final var e : _buffs)
			{
				if (e != null && e.getSkill().isDance())
				{
					e.exit(false);
					update = true;
				}
			}
			updateEffectList(update);
		}
	}
	
	public final void stopAllDebuffs()
	{
		if (isEmpty())
		{
			return;
		}
		
		var update = false;
		if (!_debuffs.isEmpty())
		{
			for (final var e : _debuffs)
			{
				if (e != null)
				{
					e.exit(false);
					update = true;
				}
			}
		}
		
		if (!_buffs.isEmpty())
		{
			for (final var e : _buffs)
			{
				if (e != null && !e.getSkill().isStayAfterDeath() && e.getSkill().hasDebuffEffects())
				{
					e.exit(false);
					update = true;
				}
			}
		}
		updateEffectList(update);
	}
	
	public final void stopEffects(EffectType type, boolean update)
	{
		var found = false;
		if (!_buffs.isEmpty())
		{
			for (final var e : _buffs)
			{
				if (e != null && e.getEffectType() == type)
				{
					stopSkillEffects(e.getSkill().getId(), false);
					found = true;
				}
			}
		}
		
		if (!_debuffs.isEmpty())
		{
			for (final var e : _debuffs)
			{
				if (e != null && e.getEffectType() == type)
				{
					stopSkillEffects(e.getSkill().getId(), false);
					found = true;
				}
			}
		}
		
		if (found && update)
		{
			updateEffectList(update);
		}
	}
	
	public final void stopSkillEffects(int skillId, boolean update)
	{
		stopSkillEffects(skillId, false, update);
	}
	
	public final void stopSkillEffects(int skillId, boolean removed, boolean update)
	{
		var found = false;
		if (!_buffs.isEmpty())
		{
			for (final var e : _buffs)
			{
				if (e != null && e.getSkill().getId() == skillId)
				{
					e.setRemoved(removed);
					e.exit(false);
					found = true;
				}
			}
		}
		
		if (!_debuffs.isEmpty())
		{
			for (final var e : _debuffs)
			{
				if (e != null && e.getSkill().getId() == skillId)
				{
					e.setRemoved(removed);
					e.exit(false);
					found = true;
				}
			}
		}
		
		if (found && update)
		{
			updateEffectList(update);
		}
	}
	
	public final void stopSkillEffect(Effect newEffect)
	{
		if (!_debuffs.isEmpty())
		{
			var update = false;
			for (final var e : _debuffs)
			{
				if ((e != null) && (e.getClass().getSimpleName().equalsIgnoreCase(newEffect.getClass().getSimpleName())) && e.getSkill().getId() == newEffect.getSkill().getId())
				{
					if ((e.getEffectType() == newEffect.getEffectType()) && (e.getAbnormalLvl() <= newEffect.getAbnormalLvl()) && e.getAbnormalType().equalsIgnoreCase(newEffect.getAbnormalType()))
					{
						e.exit(false);
						update = true;
					}
				}
			}
			updateEffectList(update);
		}
	}
	
	public void stopEffectsOnAction()
	{
		if (_hasBuffsRemovedOnAnyAction && !_buffs.isEmpty())
		{
			var update = false;
			for (final var e : _buffs)
			{
				if (e != null && e.getSkill().isRemovedOnAnyActionExceptMove())
				{
					e.exit(true, true, false);
					update = true;
				}
			}
			updateEffectList(update);
		}
	}
	
	public void stopEffectsOnDamage(boolean awake)
	{
		var update = false;
		if (_hasBuffsRemovedOnDamage && !_buffs.isEmpty())
		{
			for (final var e : _buffs)
			{
				if ((e != null) && e.getSkill().isRemovedOnDamage() && (awake || (e.getSkill().getSkillType() != SkillType.SLEEP)))
				{
					e.exit(true, true, false);
					update = true;
				}
			}
		}
		
		if (_hasDebuffsRemovedOnDamage && !_debuffs.isEmpty())
		{
			for (final var e : _debuffs)
			{
				if ((e != null) && e.getSkill().isRemovedOnDamage() && (awake || (e.getSkill().getSkillType() != SkillType.SLEEP)))
				{
					e.exit(true, true, false);
					update = true;
				}
			}
		}
		updateEffectList(update);
	}
	
	public void updateEffectIcons(boolean partyOnly, boolean printMessage)
	{
		if (_buffs.isEmpty() && _debuffs.isEmpty())
		{
			return;
		}
		
		if (partyOnly)
		{
			_partyOnly = true;
		}
		queueRunner(printMessage, true);
	}
	
	public void queueEffect(Effect effect, boolean remove, boolean printMessage, boolean update)
	{
		if (effect == null)
		{
			return;
		}
		
		if (!_queuesInitialized)
		{
			init();
		}
		
		if (remove)
		{
			_removeQueue.offer(effect);
		}
		else
		{
			_addQueue.offer(effect);
		}
		queueRunner(printMessage, update);
	}
	
	private synchronized void init()
	{
		if (_queuesInitialized)
		{
			return;
		}
		_addQueue = new LinkedBlockingQueue<>();
		_removeQueue = new LinkedBlockingQueue<>();
		_queuesInitialized = true;
	}
	
	private void queueRunner(boolean printMessage, boolean update)
	{
		if (!_queueLock.compareAndSet(false, true))
		{
			return;
		}
		
		try
		{
			Effect effect;
			do
			{
				while ((effect = _removeQueue.poll()) != null)
				{
					removeEffectFromQueue(effect, printMessage);
					_partyOnly = false;
				}
				
				if ((effect = _addQueue.poll()) != null)
				{
					addEffectFromQueue(effect);
					_partyOnly = false;
				}
			}
			while (!_addQueue.isEmpty() || !_removeQueue.isEmpty());
			updateEffectList(update);
		}
		finally
		{
			_queueLock.set(false);
		}
	}
	
	private void removeEffectFromQueue(Effect effect, boolean printMessage)
	{
		if (effect == null)
		{
			return;
		}
		
		if (effect.getSkill().isPassive())
		{
			if (effect.setInUse(false))
			{
				_owner.removeStatsOwner(effect.getStatFuncs());
				_passives.remove(effect);
			}
		}
		
		List<Effect> effectList;
		
		_rebuildCache = true;
		
		if (effect.getSkill().isDebuff())
		{
			if (_debuffs.isEmpty())
			{
				return;
			}
			effectList = _debuffs;
		}
		else
		{
			if (_buffs.isEmpty())
			{
				return;
			}
			effectList = _buffs;
		}
		
		if ("none".equals(effect.getAbnormalType()))
		{
			_owner.removeStatsOwner(effect);
		}
		else
		{
			final var stackQueue = _stackedEffects.get(effect.getAbnormalType());
			if ((stackQueue == null) || stackQueue.isEmpty())
			{
				return;
			}
			
			final int index = stackQueue.indexOf(effect);
			if (index >= 0)
			{
				stackQueue.remove(effect);
				
				if (index == 0)
				{
					_owner.removeStatsOwner(effect);
					
					if (!stackQueue.isEmpty())
					{
						final var newStackedEffect = listsContains(stackQueue.get(0));
						if (newStackedEffect != null && newStackedEffect.setInUse(true))
						{
							_owner.addStatFuncs(newStackedEffect.getStatFuncs());
						}
					}
				}
				if (stackQueue.isEmpty())
				{
					_stackedEffects.remove(effect.getAbnormalType());
				}
				else
				{
					_stackedEffects.put(effect.getAbnormalType(), stackQueue);
				}
			}
		}
		
		if (effectList.remove(effect) && _owner.isPlayer() && effect.isIconDisplay() && !effect.isInstant())
		{
			if (printMessage)
			{
				final SystemMessage sm;
				if (effect.getSkill().isToggle())
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED);
				}
				else if (effect.isRemoved())
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
				}
				sm.addSkillName(effect);
				_owner.sendPacket(sm);
			}
		}
		
		if (effect == _owner.getEffectList().getShortBuff())
		{
			_owner.getEffectList().shortBuffStatusUpdate(null);
		}
	}
	
	public boolean isBlockedEffect(Effect newEffect)
	{
		return _blockedBuffSlots != null && _blockedBuffSlots.contains(newEffect.getAbnormalType());
	}
	
	private void addEffectFromQueue(Effect newEffect)
	{
		if (newEffect == null || isBlockedEffect(newEffect))
		{
			return;
		}
		
		if (newEffect.getSkill().isPassive())
		{
			if ("none".equals(newEffect.getAbnormalType()))
			{
				if (newEffect.setInUse(true))
				{
					for (final var eff : _passives)
					{
						if (eff == null)
						{
							continue;
						}
						
						if (eff.getEffectTemplate().equals(newEffect.getEffectTemplate()))
						{
							eff.exit(false);
						}
						
					}
					_owner.addStatFuncs(newEffect.getStatFuncs());
					_passives.add(newEffect);
				}
			}
			return;
		}
		_rebuildCache = true;
		
		if (newEffect.getSkill().isDebuff())
		{
			for (final var e : _debuffs)
			{
				if (e != null && e.getAbnormalTime() > 1 && !e.isNoNameType() && e.getSkill().getId() == newEffect.getSkill().getId() && e.getEffectType() == newEffect.getEffectType() && e.getAbnormalLvl() == newEffect.getAbnormalLvl() && e.getAbnormalType().equalsIgnoreCase(newEffect.getAbnormalType()))
				{
					newEffect.stopEffectTask(true, true);
					return;
				}
			}
			
			int effectsToRemove;
			effectsToRemove = _debuffs.size() - _owner.getMaxDebuffCount();
			if (effectsToRemove >= 0)
			{
				for (final var e : _debuffs)
				{
					if ((e == null) || e.getAbnormalTime() < 2 || !e.isIconDisplay())
					{
						continue;
					}
					
					e.exit(false);
					effectsToRemove--;
					if (effectsToRemove < 0)
					{
						break;
					}
				}
			}
			
			int pos = 0;
			for (final var e : _debuffs)
			{
				if (e != null)
				{
					pos++;
				}
			}
			_debuffs.add(pos, newEffect);
		}
		else
		{
			for (final var e : _buffs)
			{
				if (e != null && e.getAbnormalTime() > 1 && !e.isNoNameType() && e.getSkill().getId() == newEffect.getSkill().getId() && e.getEffectType() == newEffect.getEffectType() && e.getAbnormalLvl() == newEffect.getAbnormalLvl() && e.getAbnormalType().equalsIgnoreCase(newEffect.getAbnormalType()))
				{
					e.exit(false);
				}
			}
			
			if (!doesStack(newEffect.getSkill()) && !newEffect.getSkill().is7Signs())
			{
				int effectsToRemove;
				if (newEffect.getSkill().isDance())
				{
					effectsToRemove = getDanceCount() - Config.DANCES_MAX_AMOUNT;
					if (effectsToRemove >= 0)
					{
						for (final var e : _buffs)
						{
							if ((e == null) || !e.isIconDisplay() || !e.getSkill().isDance() || e.isWithoutCalcChance())
							{
								continue;
							}
							
							e.exit(false);
							effectsToRemove--;
							if (effectsToRemove < 0)
							{
								break;
							}
						}
					}
				}
				else if (newEffect.getSkill().isTriggeredSkill())
				{
					effectsToRemove = getTriggeredBuffCount() - Config.TRIGGERED_BUFFS_MAX_AMOUNT;
					if (effectsToRemove >= 0)
					{
						for (final var e : _buffs)
						{
							if ((e == null) || e.getAbnormalTime() < 2 || !e.isIconDisplay() || !e.getSkill().isTriggeredSkill() || e.isWithoutCalcChance())
							{
								continue;
							}
							
							e.exit(false);
							effectsToRemove--;
							if (effectsToRemove < 0)
							{
								break;
							}
						}
					}
				}
				else if (!newEffect.getSkill().isHealingPotionSkill() && !newEffect.isInstant())
				{
					effectsToRemove = getBuffCount() - _owner.getMaxBuffCount();
					if (effectsToRemove >= 0)
					{
						if (newEffect.getSkill().getSkillType() == SkillType.BUFF)
						{
							for (final var e : _buffs)
							{
								if ((e == null) || e.getAbnormalTime() < 2 || !e.isIconDisplay() || e.getEffectTemplate().isWithoutCalcChance() || e.getSkill().isDance() || e.getSkill().isTriggeredSkill() || e.getEffectType() == EffectType.TRANSFORMATION)
								{
									continue;
								}
								
								if (e.getSkill().getSkillType() == SkillType.BUFF)
								{
									e.exit(false);
									effectsToRemove--;
								}
								else
								{
									continue;
								}
								
								if (effectsToRemove < 0)
								{
									break;
								}
							}
						}
					}
				}
			}
			
			if (newEffect.getSkill().isTriggeredSkill())
			{
				_buffs.add(newEffect);
			}
			else
			{
				int pos = 0;
				if (newEffect.getSkill().isToggle())
				{
					for (final var e : _buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isDance())
						{
							break;
						}
						pos++;
					}
				}
				else if (newEffect.getSkill().isDance())
				{
					for (final var e : _buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isTriggeredSkill())
						{
							break;
						}
						pos++;
					}
				}
				else
				{
					for (final var e : _buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isToggle() || e.getSkill().is7Signs() || e.getSkill().isDance() || e.getSkill().isTriggeredSkill())
						{
							break;
						}
						pos++;
					}
				}
				_buffs.add(pos, newEffect);
			}
		}
		
		if ("none".equals(newEffect.getAbnormalType()))
		{
			if (newEffect.setInUse(true))
			{
				_owner.addStatFuncs(newEffect.getStatFuncs());
			}
			return;
		}
		Effect effectToAdd = null;
		Effect effectToRemove = null;
		
		var stackQueue = _stackedEffects.get(newEffect.getAbnormalType());
		if (stackQueue == null)
		{
			stackQueue = new ArrayList<>();
		}
		
		if (!stackQueue.isEmpty())
		{
			int pos = 0;
			if (!stackQueue.isEmpty())
			{
				effectToRemove = listsContains(stackQueue.get(0));
				
				final var queueIterator = stackQueue.iterator();
				while (queueIterator.hasNext())
				{
					if (newEffect.getAbnormalLvl() < queueIterator.next().getAbnormalLvl())
					{
						pos++;
					}
					else
					{
						break;
					}
				}
				stackQueue.add(pos, newEffect);
				
				final var isValid = newEffect.getSkill().isStatic() && !newEffect.getSkill().isPotion() || newEffect.getSkill().isAbnormalInstant();
				if (Config.EFFECT_CANCELING && !isValid && (stackQueue.size() > 1))
				{
					if (newEffect.getSkill().isDebuff())
					{
						_debuffs.remove(stackQueue.remove(1));
					}
					else
					{
						_buffs.remove(stackQueue.remove(1));
					}
				}
			}
			else
			{
				stackQueue.add(0, newEffect);
			}
		}
		else
		{
			stackQueue.add(0, newEffect);
		}
		_stackedEffects.put(newEffect.getAbnormalType(), stackQueue);
		
		if (!stackQueue.isEmpty())
		{
			effectToAdd = listsContains(stackQueue.get(0));
		}
		
		if (effectToRemove != effectToAdd)
		{
			if (effectToRemove != null)
			{
				_owner.removeStatsOwner(effectToRemove);
				effectToRemove.setInUse(false);
			}
			if (effectToAdd != null)
			{
				if (effectToAdd.setInUse(true))
				{
					_owner.addStatFuncs(effectToAdd.getStatFuncs());
				}
			}
		}
	}
	
	public void removePassiveEffects(int skillId)
	{
		if (!_passives.isEmpty())
		{
			var update = false;
			for (final var e : _passives)
			{
				if (e != null && e.getSkill().getId() == skillId)
				{
					e.exit(false);
					update = true;
				}
			}
			updateEffectList(update);
		}
	}
	
	private void updateEffectList(boolean update)
	{
		if (update)
		{
			computeEffectFlags();
			updateEffectIcons();
		}
	}
	
	public void updateEffectIcons()
	{
		if (_owner == null || (_effectIconsUpdate != null && !_effectIconsUpdate.isDone()))
		{
			return;
		}
		_effectIconsUpdate = ThreadPoolManager.getInstance().schedule(new UpdateEffectIconsTask(), Config.USER_ABNORMAL_EFFECTS_INTERVAL);
	}
	
	private class UpdateEffectIconsTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_owner == null || (_owner.isInHideStatus()))
			{
				return;
			}
			
			AbnormalStatusUpdate asu = null;
			PartySpelled ps = null;
			PartySpelled psSummon = null;
			ExOlympiadSpelledInfo os = null;
			boolean isSummon = false;
			final List<Effect> effectList = new ArrayList<>();
			
			if (_owner.isPlayer())
			{
				if (_partyOnly)
				{
					_partyOnly = false;
				}
				else
				{
					asu = new AbnormalStatusUpdate();
				}
				
				if (_owner.isInParty())
				{
					ps = new PartySpelled(_owner);
				}
				
				if (_owner.getActingPlayer().isInOlympiadMode() && _owner.getActingPlayer().isOlympiadStart())
				{
					os = new ExOlympiadSpelledInfo(_owner.getActingPlayer());
				}
			}
			else if (_owner.isSummon())
			{
				isSummon = true;
				ps = new PartySpelled(_owner);
				psSummon = new PartySpelled(_owner);
			}
			
			var foundRemovedOnAction = false;
			var foundRemovedOnDamage = false;
			var foundRemovedDebuffOnDamage = false;
			
			if (!isEmpty())
			{
				for (final var e : getAllSortEffects())
				{
					if (e == null)
					{
						continue;
					}
					
					if (e.getSkill().isDebuff())
					{
						if (e.getSkill().isRemovedOnAnyActionExceptMove())
						{
							foundRemovedOnAction = true;
						}
						if (e.getSkill().isRemovedOnDamage())
						{
							foundRemovedDebuffOnDamage = true;
						}
					}
					
					if (e.getSkill().isRemovedOnAnyActionExceptMove())
					{
						foundRemovedOnAction = true;
					}
					if (e.getSkill().isRemovedOnDamage())
					{
						foundRemovedOnDamage = true;
					}
					
					if (!e.isIconDisplay() || e.isInstant() || (e.getEffectType() == EffectType.SIGNET_GROUND))
					{
						continue;
					}
					
					if (e.isInUse())
					{
						if (e.getSkill().isHealingPotionSkill())
						{
							shortBuffStatusUpdate(e);
						}
						else
						{
							addIcon(e, asu, ps, psSummon, os, isSummon, effectList);
						}
					}
				}
			}
			
			_hasDebuffsRemovedOnDamage = foundRemovedDebuffOnDamage;
			_hasBuffsRemovedOnAnyAction = foundRemovedOnAction;
			_hasBuffsRemovedOnDamage = foundRemovedOnDamage;
			
			if (asu != null)
			{
				_owner.sendPacket(asu);
			}
			
			if (ps != null)
			{
				if (_owner.isSummon())
				{
					final var summonOwner = ((Summon) _owner).getOwner();
					if (summonOwner != null)
					{
						if (summonOwner.isInParty())
						{
							summonOwner.getParty().broadcastToPartyMembers(summonOwner, psSummon);
						}
						summonOwner.sendPacket(ps);
					}
				}
				else if (_owner.isPlayer() && _owner.isInParty())
				{
					_owner.getParty().broadCast(ps);
				}
			}
			
			if (os != null)
			{
				final var game = OlympiadGameManager.getInstance().getOlympiadTask(_owner.getActingPlayer().getOlympiadGameId());
				if ((game != null) && game.isBattleStarted())
				{
					game.getZone().broadcastPacketToObservers(os);
				}
			}
			
			if (Config.ALLOW_CUSTOM_INTERFACE)
			{
				final var canSend = _owner.isPlayable() || _owner.isNpc();
				final var upd = new ExAbnormalStatusUpdateFromTarget(_owner, asu != null ? asu.getEffects() : effectList);
				for (final var creature : _owner.getStatus().getStatusListener())
				{
					if ((creature != null) && creature.isPlayer())
					{
						if (canSend)
						{
							upd.sendTo(creature.getActingPlayer());
						}
					}
				}
				
				if (_owner.isPlayer() && (_owner.getTarget() == _owner))
				{
					_owner.sendPacket(upd);
				}
			}
			effectList.clear();
			_effectIconsUpdate = null;
		}
	}
	
	private Effect listsContains(Effect effect)
	{
		if (_buffs.contains(effect))
		{
			return effect;
		}
		if (_debuffs.contains(effect))
		{
			return effect;
		}
		return null;
	}
	
	private final void computeEffectFlags()
	{
		int flags = 0;
		
		if (!_buffs.isEmpty())
		{
			for (final var e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectFlags();
			}
		}
		
		if (!_debuffs.isEmpty())
		{
			for (final var e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectFlags();
			}
		}
		
		if (!_passives.isEmpty())
		{
			for (final var e : _passives)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectFlags();
			}
		}
		_effectFlags = flags;
	}
	
	public boolean isEmpty()
	{
		return _buffs.isEmpty() && _debuffs.isEmpty();
	}
	
	public List<Effect> getBuffs()
	{
		return _buffs;
	}
	
	public List<Effect> getDebuffs()
	{
		return _debuffs;
	}
	
	public List<Effect> getPassives()
	{
		return _passives;
	}
	
	public boolean isAffected(EffectFlag flag)
	{
		return (_effectFlags & flag.getMask()) != 0;
	}
	
	public void clear()
	{
		try
		{
			stopAllEffects(true, false);
		}
		finally
		{
			final var task = _effectIconsUpdate;
			if (task != null)
			{
				task.cancel(false);
				_effectIconsUpdate = null;
			}
			_addQueue.clear();
			_removeQueue.clear();
			_buffs.clear();
			_debuffs.clear();
			_passives.clear();
			_stackedEffects.clear();
			_blockedBuffSlots.clear();
			_effectCache = null;
			_queuesInitialized = false;
		}
	}
	
	public void addBlockedBuffSlots(Set<String> blockedBuffSlots)
	{
		_blockedBuffSlots.addAll(blockedBuffSlots);
	}
	
	public boolean removeBlockedBuffSlots(Set<String> blockedBuffSlots)
	{
		return _blockedBuffSlots.removeAll(blockedBuffSlots);
	}
	
	public Set<String> getAllBlockedBuffSlots()
	{
		return _blockedBuffSlots;
	}
	
	public Effect getShortBuff()
	{
		return _shortBuff;
	}
	
	private void addIcon(Effect info, AbnormalStatusUpdate asu, PartySpelled ps, PartySpelled psSummon, ExOlympiadSpelledInfo os, boolean isSummon, List<Effect> effectList)
	{
		if ((info == null) || !info.isInUse())
		{
			return;
		}
		
		final var skill = info.getSkill();
		if (asu != null)
		{
			asu.addSkill(info);
		}
		effectList.add(info);
		
		if ((ps != null) && (isSummon || !skill.isToggle()))
		{
			ps.addSkill(info);
		}
		
		if ((psSummon != null) && !skill.isToggle())
		{
			psSummon.addSkill(info);
		}
		
		if (os != null)
		{
			os.addSkill(info);
		}
	}
	
	public void shortBuffStatusUpdate(Effect info)
	{
		if (_owner.isPlayer())
		{
			_shortBuff = info;
			_owner.sendPacket(info == null ? ShortBuffStatusUpdate.RESET_SHORT_BUFF : new ShortBuffStatusUpdate(info.getSkill().getId(), info.getSkill().getLevel(), info.getTimeLeft()));
		}
	}
	
	public boolean hasAbnormalType(String type)
	{
		return _stackedEffects.containsKey(type);
	}
	
	public Effect getFirstBuffInfoByAbnormalType(String type)
	{
		if (hasAbnormalType(type))
		{
			return _stackedEffects.get(type).get(0);
		}
		return null;
	}
}