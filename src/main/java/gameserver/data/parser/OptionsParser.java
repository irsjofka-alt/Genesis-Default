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
package gameserver.data.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;

import gameserver.Config;
import gameserver.data.DocumentParser;
import gameserver.model.holders.SkillHolder;
import gameserver.model.skills.funcs.FuncTemplate;
import gameserver.model.skills.funcs.LambdaConst;
import gameserver.model.skills.options.Options;
import gameserver.model.skills.options.Options.AugmentationFilter;
import gameserver.model.skills.options.OptionsSkillHolder;
import gameserver.model.skills.options.OptionsSkillType;
import gameserver.model.stats.Stats;
import gameserver.utils.comparators.ActiveSkillsComparator;
import gameserver.utils.comparators.ChanceSkillsComparator;
import gameserver.utils.comparators.PassiveSkillsComparator;

public class OptionsParser extends DocumentParser
{
	private final Map<Integer, Options> _data = new HashMap<>();
	
	protected OptionsParser()
	{
		load();
	}

	@Override
	public synchronized void load()
	{
		parseDirectory("data/stats/skills/options", false);
		info("Loaded: " + _data.size() + " options.");
	}
	
	@Override
	protected void reloadDocument()
	{
	}
	
	@Override
	protected void parseDocument()
	{
		int id;
		Options op;
		for (var n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (var d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("option".equalsIgnoreCase(d.getNodeName()))
					{
						id = parseInt(d.getAttributes(), "id");
						op = new Options(id);

						for (var cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							switch (cd.getNodeName())
							{
								case "for" :
								{
									for (var fd = cd.getFirstChild(); fd != null; fd = fd.getNextSibling())
									{
										switch (fd.getNodeName())
										{
											case "add" :
											{
												parseFuncs(fd.getAttributes(), "Add", op);
												break;
											}
											case "mul" :
											{
												parseFuncs(fd.getAttributes(), "Mul", op);
												break;
											}
											case "basemul" :
											{
												parseFuncs(fd.getAttributes(), "BaseMul", op);
												break;
											}
											case "sub" :
											{
												parseFuncs(fd.getAttributes(), "Sub", op);
												break;
											}
											case "div" :
											{
												parseFuncs(fd.getAttributes(), "Div", op);
												break;
											}
											case "set" :
											{
												parseFuncs(fd.getAttributes(), "Set", op);
												break;
											}
										}
									}
									break;
								}
								case "active_skill" :
								{
									op.setActiveSkill(new SkillHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level")));
									break;
								}
								case "passive_skill" :
								{
									op.setPassiveSkill(new SkillHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level")));
									break;
								}
								case "attack_skill" :
								{
									op.addActivationSkill(new OptionsSkillHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level"), parseDouble(cd.getAttributes(), "chance"), OptionsSkillType.ATTACK));
									break;
								}
								case "magic_skill" :
								{
									op.addActivationSkill(new OptionsSkillHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level"), parseDouble(cd.getAttributes(), "chance"), OptionsSkillType.MAGIC));
									break;
								}
								case "critical_skill" :
								{
									op.addActivationSkill(new OptionsSkillHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level"), parseDouble(cd.getAttributes(), "chance"), OptionsSkillType.CRITICAL));
									break;
								}
							}
						}
						_data.put(op.getId(), op);
					}
				}
			}
		}
	}

	private void parseFuncs(NamedNodeMap attrs, String func, Options op)
	{
		final var stat = Stats.valueOfXml(parseString(attrs, "stat"));
		final int ord = Integer.decode(parseString(attrs, "order"));
		final double val = parseDouble(attrs, "val");
		op.addFunc(new FuncTemplate(null, null, func, stat, ord, new LambdaConst(val)));
	}

	public Options getOptions(int id)
	{
		return _data.get(id);
	}

	public Collection<Options> getUniqueOptions(AugmentationFilter filter)
	{
		switch (filter)
		{
			case ACTIVE_SKILL :
			{
				final Map<Integer, Options> options = new HashMap<>();
				for (final var option : _data.values())
				{
					if (option.hasActivationSkills() || option.hasPassiveSkill() || !option.hasActiveSkill())
					{
						continue;
					}
					
					for (final int id : Config.SERVICES_AUGMENTATION_DISABLED_LIST)
					{
						if (id == option.getId())
						{
							continue;
						}
					}
					
					if (!options.containsKey(option.getActiveSkill().getId()) || options.get(option.getActiveSkill().getId()).getActiveSkill().getLvl() < option.getActiveSkill().getLvl())
					{
						options.put(option.getActiveSkill().getId(), option);
					}
				}
				final List<Options> augs = new ArrayList<>(options.values());
				Collections.sort(augs, ActiveSkillsComparator.getInstance());
				return augs;
			}
			case PASSIVE_SKILL :
			{
				final Map<Integer, Options> options = new HashMap<>();
				for (final var option : _data.values())
				{
					if (option.hasActivationSkills() || option.hasActiveSkill() || !option.hasPassiveSkill())
					{
						continue;
					}
					
					final var sk = option.getPassiveSkill().getSkill();
					if (sk == null || sk.getTriggeredId() > 0)
					{
						continue;
					}
					
					for (final int id : Config.SERVICES_AUGMENTATION_DISABLED_LIST)
					{
						if (id == option.getId())
						{
							continue;
						}
					}
					
					if (!options.containsKey(sk.getId()) || options.get(sk.getId()).getPassiveSkill().getLvl() < sk.getLevel())
					{
						options.put(sk.getId(), option);
					}
				}
				final List<Options> augs = new ArrayList<>(options.values());
				Collections.sort(augs, PassiveSkillsComparator.getInstance());
				return augs;
			}
			case CHANCE_SKILL :
			{
				final Map<Integer, Options> options = new HashMap<>();
				for (final var option : _data.values())
				{
					if (option.hasActiveSkill())
					{
						continue;
					}
					
					final var skill = option.getPassiveSkill();
					final var haveChanceSkill = skill != null && skill.getSkill() != null && skill.getSkill().getTriggeredId() > 0;
					
					if (option.hasActivationSkills() || haveChanceSkill)
					{
						final var sk = haveChanceSkill ? skill.getSkill() : option.getActivationsSkills().get(0).getSkill();
						for (final int id : Config.SERVICES_AUGMENTATION_DISABLED_LIST)
						{
							if (id == option.getId())
							{
								continue;
							}
						}
						
						Options opt = null;
						if (options.containsKey(sk.getId()))
						{
							opt = options.get(sk.getId());
							final var oldSk = opt.getPassiveSkill();
							final var haveChance = oldSk != null && oldSk.getSkill() != null && oldSk.getSkill().getTriggeredId() > 0;
							final var curSkill = haveChance ? oldSk.getSkill() : opt.getActivationsSkills().get(0).getSkill();
							if (curSkill.getLevel() < sk.getLevel())
							{
								options.put(sk.getId(), option);
							}
						}
						else
						{
							options.put(sk.getId(), option);
						}
					}
				}
				final List<Options> augs = new ArrayList<>(options.values());
				Collections.sort(augs, ChanceSkillsComparator.getInstance());
				return augs;
			}
			case STATS :
			{
				final Map<Integer, Options> options = new HashMap<>();
				for (final var option : _data.values())
				{
					for (final int id : Config.SERVICES_AUGMENTATION_DISABLED_LIST)
					{
						if (id == option.getId())
						{
							continue;
						}
					}
					
					switch (option.getId())
					{
						case 16341 :
						case 16342 :
						case 16343 :
						case 16344 :
							options.put(option.getId(), option);
							break;
					}
				}
				final List<Options> augs = new ArrayList<>(options.values());
				return augs;
			}
		}
		return _data.values();
	}
	
	public Collection<Options> getUniqueAvailableOptions(AugmentationFilter filter)
	{
		if (filter == AugmentationFilter.NONE)
		{
			return _data.values();
		}
		
		List<Options> augs = null;
		switch (filter)
		{
			case ACTIVE_SKILL :
			{
				final Map<Integer, Options> options = new HashMap<>();
				for (final var option : _data.values())
				{
					if (option.hasActivationSkills() || option.hasPassiveSkill() || !option.hasActiveSkill())
					{
						continue;
					}
					
					if (!options.containsKey(option.getActiveSkill().getId()) || options.get(option.getActiveSkill().getId()).getActiveSkill().getLvl() < option.getActiveSkill().getLvl())
					{
						for (final int id : Config.SERVICES_AUGMENTATION_AVAILABLE_LIST)
						{
							if (id == option.getId())
							{
								options.put(option.getActiveSkill().getId(), option);
							}
						}
					}
				}
				augs = new ArrayList<>(options.values());
				Collections.sort(augs, new ActiveSkillsComparator());
				break;
			}
			case PASSIVE_SKILL :
			{
				final Map<Integer, Options> options = new HashMap<>();
				for (final var option : _data.values())
				{
					if (option.hasActivationSkills() || option.hasActiveSkill() || !option.hasPassiveSkill())
					{
						continue;
					}
					
					final var sk = option.getPassiveSkill().getSkill();
					if (sk == null || sk.getTriggeredId() > 0)
					{
						continue;
					}
					
					if (!options.containsKey(sk.getId()) || options.get(sk.getId()).getPassiveSkill().getLvl() < sk.getLevel())
					{
						for (final int id : Config.SERVICES_AUGMENTATION_AVAILABLE_LIST)
						{
							if (id == option.getId())
							{
								options.put(sk.getId(), option);
								break;
							}
						}
					}
				}
				augs = new ArrayList<>(options.values());
				Collections.sort(augs, PassiveSkillsComparator.getInstance());
				break;
			}
			case CHANCE_SKILL :
			{
				final Map<Integer, Options> options = new HashMap<>();
				for (final var option : _data.values())
				{
					if (option.hasActiveSkill())
					{
						continue;
					}
					
					final var skill = option.getPassiveSkill();
					final var haveChanceSkill = skill != null && skill.getSkill() != null && skill.getSkill().getTriggeredId() > 0;
					
					if (option.hasActivationSkills() || haveChanceSkill)
					{
						final var sk = haveChanceSkill ? skill.getSkill() : option.getActivationsSkills().get(0).getSkill();
						
						Options opt = null;
						if (options.containsKey(sk.getId()))
						{
							opt = options.get(sk.getId());
							final var oldSk = opt.getPassiveSkill();
							final var haveChance = oldSk != null && oldSk.getSkill() != null && oldSk.getSkill().getTriggeredId() > 0;
							final var curSkill = haveChance ? oldSk.getSkill() : opt.getActivationsSkills().get(0).getSkill();
							if (curSkill.getLevel() < sk.getLevel())
							{
								for (final int id : Config.SERVICES_AUGMENTATION_AVAILABLE_LIST)
								{
									if (id == option.getId())
									{
										options.put(sk.getId(), option);
										break;
									}
								}
							}
						}
						else
						{
							for (final int id : Config.SERVICES_AUGMENTATION_AVAILABLE_LIST)
							{
								if (id == option.getId())
								{
									options.put(sk.getId(), option);
									break;
								}
							}
						}
					}
				}
				augs = new ArrayList<>(options.values());
				Collections.sort(augs, new ChanceSkillsComparator());
				break;
			}
			case STATS :
			{
				final Map<Integer, Options> options = new HashMap<>();
				for (final var option : _data.values())
				{
					switch (option.getId())
					{
						case 16341 :
						case 16342 :
						case 16343 :
						case 16344 :
							for (final int id : Config.SERVICES_AUGMENTATION_AVAILABLE_LIST)
							{
								if (id == option.getId())
								{
									options.put(option.getId(), option);
									break;
								}
							}
							break;
					}
				}
				augs = new ArrayList<>(options.values());
				break;
			}
		}
		return augs;
	}
	
	public static final OptionsParser getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final OptionsParser _instance = new OptionsParser();
	}
}