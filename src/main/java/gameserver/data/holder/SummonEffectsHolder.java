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
package gameserver.data.holder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import gameserver.model.actor.Player;
import gameserver.model.actor.Summon;
import gameserver.model.actor.instance.PetInstance;
import gameserver.model.actor.instance.ServitorInstance;
import gameserver.model.skills.Skill;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.stats.Env;

public class SummonEffectsHolder
{
	private final Map<Integer, Map<Integer, Map<Integer, List<SummonEffect>>>> _servitorEffects = new HashMap<>();
	
	private Map<Integer, List<SummonEffect>> getServitorEffects(Player owner)
	{
		final Map<Integer, Map<Integer, List<SummonEffect>>> servitorMap = _servitorEffects.get(owner.getObjectId());
		if (servitorMap == null)
		{
			return null;
		}
		return servitorMap.get(owner.getClassIndex());
	}
	
	private List<SummonEffect> getServitorEffects(Player owner, int referenceSkill)
	{
		return containsOwner(owner) ? getServitorEffects(owner).get(referenceSkill) : null;
	}
	
	private boolean containsOwner(Player owner)
	{
		return _servitorEffects.getOrDefault(owner.getObjectId(), Collections.emptyMap()).containsKey(owner.getClassIndex());
	}
	
	private void removeEffects(List<SummonEffect> effects, int skillId)
	{
		if ((effects != null) && !effects.isEmpty())
		{
			for (final var effect : effects)
			{
				final Skill skill = effect.getSkill();
				if ((skill != null) && (skill.getId() == skillId))
				{
					effects.remove(effect);
				}
			}
		}
	}
	
	private void applyEffects(Summon summon, List<SummonEffect> summonEffects)
	{
		if (summonEffects == null || summonEffects.isEmpty())
		{
			return;
		}
		
		for (final var se : summonEffects)
		{
			if ((se != null) && se.getSkill().hasEffects())
			{
				final Env env = new Env();
				env.setCharacter(summon);
				env.setTarget(summon);
				env.setSkill(se.getSkill());
				Effect ef;
				for (final EffectTemplate et : se.getSkill().getEffectTemplates())
				{
					ef = et.getEffect(env);
					if (ef != null)
					{
						switch (ef.getEffectType())
						{
							case CANCEL :
							case CANCEL_ALL :
							case CANCEL_BY_SLOT :
								continue;
						}
						ef.setCount(se.getEffectCount());
						ef.setAbnormalTime(se.getEffectTotalTime());
						ef.setFirstTime(se.getEffectCurTime());
						ef.scheduleEffect(true, false);
					}
				}
			}
		}
		summon.updateEffectIcons();
	}
	
	public boolean containsSkill(Player owner, int referenceSkill)
	{
		return containsOwner(owner) && getServitorEffects(owner).containsKey(referenceSkill);
	}
	
	public void clearServitorEffects(Player owner, int referenceSkill)
	{
		if (containsOwner(owner))
		{
			getServitorEffects(owner).getOrDefault(referenceSkill, Collections.emptyList()).clear();
		}
	}
	
	public void addServitorEffect(Player owner, int referenceSkill, Skill skill, int effectCount, int effectTime, int effectTotalTime)
	{
		_servitorEffects.putIfAbsent(owner.getObjectId(), new HashMap<>());
		_servitorEffects.get(owner.getObjectId()).putIfAbsent(owner.getClassIndex(), new HashMap<>());
		getServitorEffects(owner).putIfAbsent(referenceSkill, new CopyOnWriteArrayList<>());
		getServitorEffects(owner).get(referenceSkill).add(new SummonEffect(skill, effectCount, effectTime, effectTotalTime));
	}
	
	public void removeServitorEffects(Player owner, int referenceSkill, int skillId)
	{
		removeEffects(getServitorEffects(owner, referenceSkill), skillId);
	}
	
	public void applyServitorEffects(ServitorInstance l2ServitorInstance, Player owner, int referenceSkill)
	{
		applyEffects(l2ServitorInstance, getServitorEffects(owner, referenceSkill));
	}
	
	private final Map<Integer, List<SummonEffect>> _petEffects = new ConcurrentHashMap<>();
	
	public void addPetEffect(int controlObjectId, Skill skill, int effectCount, int effectTime, int effectTotalTime)
	{
		_petEffects.computeIfAbsent(controlObjectId, _ -> new CopyOnWriteArrayList<>()).add(new SummonEffect(skill, effectCount, effectTime, effectTotalTime));
	}
	
	public boolean containsPetId(int controlObjectId)
	{
		return _petEffects.containsKey(controlObjectId);
	}
	
	public void applyPetEffects(PetInstance l2PetInstance, int controlObjectId)
	{
		applyEffects(l2PetInstance, _petEffects.get(controlObjectId));
	}
	
	public void clearPetEffects(int controlObjectId)
	{
		_petEffects.getOrDefault(controlObjectId, Collections.emptyList()).clear();
	}
	
	public void removePetEffects(int controlObjectId, int skillId)
	{
		removeEffects(_petEffects.get(controlObjectId), skillId);
	}
	
	private class SummonEffect
	{
		Skill _skill;
		int _effectCount;
		int _effectCurTime;
		int _effectTotalTime;
		
		public SummonEffect(Skill skill, int effectCount, int effectCurTime, int effectTotalTime)
		{
			_skill = skill;
			_effectCount = effectCount;
			_effectCurTime = effectCurTime;
			_effectTotalTime = effectTotalTime;
		}
		
		public Skill getSkill()
		{
			return _skill;
		}
		
		public int getEffectCount()
		{
			return _effectCount;
		}
		
		public int getEffectCurTime()
		{
			return _effectCurTime;
		}
		
		public int getEffectTotalTime()
		{
			return _effectTotalTime;
		}
	}
	
	public static SummonEffectsHolder getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SummonEffectsHolder _instance = new SummonEffectsHolder();
	}
}