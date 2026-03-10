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
package gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2e.commons.util.Rnd;
import gameserver.data.parser.SkillsParser;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.base.ClassId;
import gameserver.model.skills.Skill;
import gameserver.model.zone.TaskZoneSettings;
import gameserver.model.zone.ZoneId;
import gameserver.model.zone.ZoneType;
import gameserver.taskmanager.EffectTaskManager;

/**
 * Updated by LordWinter 08.10.2020
 */
public class DravonValleyZone extends ZoneType
{
	private static final Map<ClassId, Double> weight = new HashMap<>();

	private int _chance;
	private int _initialDelay;
	private int _reuse;
	private int _buffEffectRange;

	public DravonValleyZone(int id)
	{
		super(id);
		
		var settings = ZoneManager.getSettings(getName());
		if (settings == null)
		{
			settings = new TaskZoneSettings();
		}
		setSettings(settings);
		addZoneId(ZoneId.ALTERED);
	}
	
	static
	{
		weight.put(ClassId.DUELIST, 0.2);
		weight.put(ClassId.DREADNOUGHT, 0.7);
		weight.put(ClassId.PHOENIX_KNIGHT, 0.5);
		weight.put(ClassId.HELL_KNIGHT, 0.5);
		weight.put(ClassId.SAGITTARIUS, 0.3);
		weight.put(ClassId.ADVENTURER, 0.4);
		weight.put(ClassId.ARCHMAGE, 0.3);
		weight.put(ClassId.SOULTAKER, 0.3);
		weight.put(ClassId.ARCANALORD, 1.);
		weight.put(ClassId.CARDINAL, -0.6);
		weight.put(ClassId.HIEROPHANT, 0.);
		weight.put(ClassId.EVA_TEMPLAR, 0.8);
		weight.put(ClassId.SWORDMUSE, 0.5);
		weight.put(ClassId.WINDRIDER, 0.4);
		weight.put(ClassId.MOONLIGHTSENTINEL, 0.3);
		weight.put(ClassId.MYSTICMUSE, 0.3);
		weight.put(ClassId.ELEMENTAL_MASTER, 1.);
		weight.put(ClassId.EVA_SAINT, -0.6);
		weight.put(ClassId.SHILLEN_TEMPLAR, 0.8);
		weight.put(ClassId.SPECTRAL_DANCER, 0.5);
		weight.put(ClassId.GHOSTHUNTER, 0.4);
		weight.put(ClassId.GHOSTSENTINEL, 0.3);
		weight.put(ClassId.STORMSCREAMER, 0.3);
		weight.put(ClassId.SPECTRAL_MASTER, 1.);
		weight.put(ClassId.SHILLEN_SAINT, -0.6);
		weight.put(ClassId.TITAN, 0.3);
		weight.put(ClassId.DOMINATOR, 0.1);
		weight.put(ClassId.GRANDKHAVATARI, 0.2);
		weight.put(ClassId.DOOMCRYER, 0.1);
		weight.put(ClassId.FORTUNE_SEEKER, 0.9);
		weight.put(ClassId.MAESTRO, 0.7);
		weight.put(ClassId.DOOMBRINGER, 0.2);
		weight.put(ClassId.TRICKSTER, 0.5);
		weight.put(ClassId.JUDICATOR, 0.1);
		weight.put(ClassId.MALE_SOULHOUND, 0.3);
		weight.put(ClassId.FEMALE_SOULHOUND, 0.3);
	}
	
	@Override
	public TaskZoneSettings getSettings()
	{
		return (TaskZoneSettings) super.getSettings();
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("chance"))
		{
			_chance = Integer.parseInt(value);
		}
		else if (name.equals("initialDelay"))
		{
			_initialDelay = Integer.parseInt(value);
		}
		else if (name.equals("reuse"))
		{
			_reuse = Integer.parseInt(value);
		}
		else if (name.equals("buffEffectRange"))
		{
			_buffEffectRange = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (isEnabled() && character.isPlayer())
		{
			enableEffect();
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (getPlayersInside().isEmpty() && (getSettings().getTask() != null))
		{
			getSettings().clear();
		}
	}
	
	private void enableEffect()
	{
		if (getSettings().getTask() == null)
		{
			synchronized (this)
			{
				if (getSettings().getTask() == null)
				{
					getSettings().setTask(EffectTaskManager.getInstance().scheduleAtFixedRate(new BuffTask(), _initialDelay, _reuse));
				}
			}
		}
	}
	
	@Override
	public void clearTask()
	{
		if (getSettings().getTask() != null)
		{
			getSettings().getTask().cancel(false);
		}
		super.clearTask();
	}
	
	protected Skill getSkill(int skillId, int skillLvl)
	{
		return SkillsParser.getInstance().getInfo(skillId, skillLvl);
	}
	
	protected int getBuffLevel(Player player)
	{
		if (player == null)
		{
			return 0;
		}
		
		final var party = player.getParty();
		if (party == null || party.getMemberCount() < 4)
		{
			return 0;
		}
		
		final List<Player> validMembers = new ArrayList<>();
		for (final var p : party.getMembers())
		{
			if (p == null || p.isDead() || p.getLevel() < 80 || p.getClassId().level() != 3 || !p.isInZone(this) || p.getDistance(player) > _buffEffectRange)
			{
				continue;
			}
			validMembers.add(p);
		}
		
		if (validMembers.size() < 4)
		{
			return 0;
		}
		
		double points = 0;
		for (final var p : validMembers)
		{
			points += weight.get(p.getClassId());
		}
		return (int) Math.max(0, Math.min(3, Math.round(points * getCoefficient(validMembers.size()))));
	}
	
	private double getCoefficient(int count)
	{
		return switch (count)
		{
			case 1  -> 0.7;
			case 4  -> 0.7;
			case 5  -> 0.75;
			case 6  -> 0.8;
			case 7  -> 0.85;
			case 8  -> 0.9;
			case 9  -> 0.95;
			default  -> 1;
		};
	}
	
	public int getChance()
	{
		return _chance;
	}
	
	protected final class BuffTask implements Runnable
	{
		@Override
		public void run()
		{
			for (final var player : getPlayersInside())
			{
				if ((player != null) && !player.isDead())
				{
					final var level = getBuffLevel(player);
					if (level > 0)
					{
						if (Rnd.get(100) < getChance())
						{
							final var skill = getSkill(6885, level);
							if (skill != null)
							{
								if (player.getFirstEffect(6885) == null)
								{
									skill.getEffects(player, player, false, true);
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void setIsEnabled(boolean val)
	{
		super.setIsEnabled(val);
		if (!val)
		{
			getSettings().clear();
		}
		else
		{
			if (!getPlayersInside().isEmpty())
			{
				enableEffect();
			}
		}
	}
}