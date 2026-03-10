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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gameserver.data.DocumentParser;
import gameserver.model.actor.templates.cubic.CubicSkill;
import gameserver.model.actor.templates.cubic.CubicTemplate;
import gameserver.model.actor.templates.cubic.conditions.HealthCondition;
import gameserver.model.actor.templates.cubic.conditions.HpCondition;
import gameserver.model.actor.templates.cubic.conditions.HpCondition.HpConditionType;
import gameserver.model.actor.templates.cubic.conditions.ICubicCondition;
import gameserver.model.actor.templates.cubic.conditions.RangeCondition;
import gameserver.model.stats.StatsSet;

public class CubicParser extends DocumentParser
{
	private final Map<Integer, Map<Integer, CubicTemplate>> _cubics = new ConcurrentHashMap<>();
	
	protected CubicParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_cubics.clear();
		parseDirectory("data/stats/chars/cubics", false);
		info("Loaded " + _cubics.size() + " cubic templates.");
	}
	
	@Override
	protected void reloadDocument()
	{
	}
	
	@Override
	protected void parseDocument()
	{
		for (var n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equals(n.getNodeName()))
			{
				for (var d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("cubic".equals(d.getNodeName()))
					{
						var attrs = d.getAttributes();
						final int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
						for (var c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("level".equals(c.getNodeName()))
							{
								attrs = c.getAttributes();
								var set = new StatsSet();
								set.set("id", id);
								for (int i = 0; i < attrs.getLength(); i++)
								{
									final var att = attrs.item(i);
									set.set(att.getNodeName(), att.getNodeValue());
								}
								final var template = new CubicTemplate(set);
								final List<ICubicCondition> conditions = new ArrayList<>();
								for (var s = c.getFirstChild(); s != null; s = s.getNextSibling())
								{
									if ("skill".equals(s.getNodeName()))
									{
										attrs = s.getAttributes();
										set = new StatsSet();
										final List<ICubicCondition> skillsConditions = new ArrayList<>();
										for (int i = 0; i < attrs.getLength(); i++)
										{
											final var att = attrs.item(i);
											set.set(att.getNodeName(), att.getNodeValue());
										}
										final var skill = new CubicSkill(set);
										for (var sk = s.getFirstChild(); sk != null; sk = sk.getNextSibling())
										{
											if ("conditions".equals(sk.getNodeName()))
											{
												for (var i = sk.getFirstChild(); i != null; i = i.getNextSibling())
												{
													if ("hp".equals(i.getNodeName()))
													{
														final var type = parseEnum(i.getAttributes(), HpConditionType.class, "type");
														final int hpPer = parseInteger(i.getAttributes(), "percent");
														skillsConditions.add(new HpCondition(type, hpPer));
													}
													else if ("distance".equals(i.getNodeName()))
													{
														final int range = parseInteger(i.getAttributes(), "value");
														skillsConditions.add(new RangeCondition(range));
													}
													else if ("percent".equals(i.getNodeName()))
													{
														final int min = parseInteger(i.getAttributes(), "min");
														final int max = parseInteger(i.getAttributes(), "max");
														skillsConditions.add(new HealthCondition(min, max));
													}
												}
											}
										}
										skill.addConditions(skillsConditions);
										template.getSkills().add(skill);
									}
									else if ("conditions".equals(s.getNodeName()))
									{
										for (var i = s.getFirstChild(); i != null; i = i.getNextSibling())
										{
											if ("hp".equals(i.getNodeName()))
											{
												final var type = parseEnum(i.getAttributes(), HpConditionType.class, "type");
												final int hpPer = parseInteger(i.getAttributes(), "percent");
												conditions.add(new HpCondition(type, hpPer));
											}
											else if ("range".equals(i.getNodeName()))
											{
												final int range = parseInteger(i.getAttributes(), "value");
												conditions.add(new RangeCondition(range));
											}
											else if ("healthPercent".equals(i.getNodeName()))
											{
												final int min = parseInteger(i.getAttributes(), "min");
												final int max = parseInteger(i.getAttributes(), "max");
												conditions.add(new HealthCondition(min, max));
											}
										}
									}
								}
								template.addConditions(conditions);
								_cubics.computeIfAbsent(template.getId(), _ -> new HashMap<>()).put(template.getLevel(), template);
							}
						}
					}
				}
			}
		}
	}
	
	public CubicTemplate getCubicTemplate(int id, int level)
	{
		return _cubics.getOrDefault(id, Collections.emptyMap()).get(level);
	}
	
	public static CubicParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CubicParser _instance = new CubicParser();
	}
}
