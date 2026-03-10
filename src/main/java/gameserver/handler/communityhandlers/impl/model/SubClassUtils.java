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
package gameserver.handler.communityhandlers.impl.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import gameserver.Config;
import gameserver.data.parser.CategoryParser;
import gameserver.model.CategoryType;
import gameserver.model.actor.Player;
import gameserver.model.base.ClassId;
import gameserver.model.base.Race;

public class SubClassUtils
{
	private static final Set<ClassId> mainSubclassSet;
	private static final Set<ClassId> neverSubclassed = EnumSet.of(ClassId.OVERLORD, ClassId.WARSMITH);
	private static final Set<ClassId> subclasseSet1 = EnumSet.of(ClassId.DARKAVENGER, ClassId.PALADIN, ClassId.TEMPLE_KNIGHT, ClassId.SHILLEN_KNIGHT);
	private static final Set<ClassId> subclasseSet2 = EnumSet.of(ClassId.TREASUREHUNTER, ClassId.ABYSSWALKER, ClassId.PLAINSWALKER);
	private static final Set<ClassId> subclasseSet3 = EnumSet.of(ClassId.HAWKEYE, ClassId.SILVERRANGER, ClassId.PHANTOMRANGER);
	private static final Set<ClassId> subclasseSet4 = EnumSet.of(ClassId.WARLOCK, ClassId.ELEMENTAL_SUMMONER, ClassId.PHANTOM_SUMMONER);
	private static final Set<ClassId> subclasseSet5 = EnumSet.of(ClassId.SORCEROR, ClassId.SPELLSINGER, ClassId.SPELLHOWLER);
	private static final EnumMap<ClassId, Set<ClassId>> subclassSetMap = new EnumMap<>(ClassId.class);
	static
	{
		final var subclasses = CategoryParser.getInstance().getCategoryByType(CategoryType.THIRD_CLASS_GROUP).stream().map(ClassId::getClassId).collect(Collectors.toSet());
		subclasses.removeAll(neverSubclassed);
		mainSubclassSet = subclasses;
		subclassSetMap.put(ClassId.DARKAVENGER, subclasseSet1);
		subclassSetMap.put(ClassId.PALADIN, subclasseSet1);
		subclassSetMap.put(ClassId.TEMPLE_KNIGHT, subclasseSet1);
		subclassSetMap.put(ClassId.SHILLEN_KNIGHT, subclasseSet1);
		subclassSetMap.put(ClassId.TREASUREHUNTER, subclasseSet2);
		subclassSetMap.put(ClassId.ABYSSWALKER, subclasseSet2);
		subclassSetMap.put(ClassId.PLAINSWALKER, subclasseSet2);
		subclassSetMap.put(ClassId.HAWKEYE, subclasseSet3);
		subclassSetMap.put(ClassId.SILVERRANGER, subclasseSet3);
		subclassSetMap.put(ClassId.PHANTOMRANGER, subclasseSet3);
		subclassSetMap.put(ClassId.WARLOCK, subclasseSet4);
		subclassSetMap.put(ClassId.ELEMENTAL_SUMMONER, subclasseSet4);
		subclassSetMap.put(ClassId.PHANTOM_SUMMONER, subclasseSet4);
		subclassSetMap.put(ClassId.SORCEROR, subclasseSet5);
		subclassSetMap.put(ClassId.SPELLSINGER, subclasseSet5);
		subclassSetMap.put(ClassId.SPELLHOWLER, subclasseSet5);
	}
	
	public final static Set<ClassId> getSubclasses(Player player, int classId)
	{
		Set<ClassId> subclasses = null;
		final var pClass = ClassId.getClassId(classId);
		if (CategoryParser.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, classId) || (CategoryParser.getInstance().isInCategory(CategoryType.FOURTH_CLASS_GROUP, classId)))
		{
			subclasses = EnumSet.copyOf(mainSubclassSet);
			if (Config.ALT_GAME_SUBCLASS_ALL_CLASSES)
			{
				subclasses.addAll(neverSubclassed);
				subclasses.remove(pClass);
			}
			else if (player.getRace() == Race.KAMAEL)
			{
				for (final var cid : ClassId.values())
				{
					if (cid.getRace() != Race.KAMAEL)
					{
						subclasses.remove(cid);
					}
				}
				
				if (player.getAppearance().getSex())
				{
					subclasses.remove(ClassId.MALE_SOULBREAKER);
				}
				else
				{
					subclasses.remove(ClassId.FEMALE_SOULBREAKER);
				}
				
				if (!player.getSubClasses().containsKey(2) || (player.getSubClasses().get(2).getLevel() < Config.SUBCLASS_MIN_LEVEL))
				{
					subclasses.remove(ClassId.INSPECTOR);
				}
				subclasses.remove(pClass);
			}
			else
			{
				if (player.getRace() == Race.ELF)
				{
					for (final var cid : ClassId.values())
					{
						if (cid.getRace() == Race.DARKELF)
						{
							subclasses.remove(cid);
						}
					}
				}
				else if (player.getRace() == Race.DARKELF)
				{
					for (final var cid : ClassId.values())
					{
						if (cid.getRace() == Race.ELF)
						{
							subclasses.remove(cid);
						}
					}
				}
				
				for (final var cid : ClassId.values())
				{
					if (cid.getRace() == Race.KAMAEL)
					{
						subclasses.remove(cid);
					}
				}
				subclasses.remove(pClass);
				
				final var unavailableClasses = subclassSetMap.get(pClass);
				if (unavailableClasses != null)
				{
					subclasses.removeAll(unavailableClasses);
				}
			}
		}
		
		if (subclasses != null)
		{
			final var currClassId = player.getClassId();
			for (final var tempClass : subclasses)
			{
				if (currClassId.equalsOrChildOf(tempClass))
				{
					subclasses.remove(tempClass);
				}
			}
		}
		return subclasses;
	}
}