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
package gameserver.model.base;

import java.util.HashMap;
import java.util.Map;

import gameserver.model.interfaces.IIdentifiable;
import gameserver.model.strings.server.ServerStorage;

public enum ClassId implements IIdentifiable
{
	FIGHTER(0x00, false, Race.HUMAN, null),
	
	WARRIOR(0x01, false, Race.HUMAN, FIGHTER), GLADIATOR(0x02, false, Race.HUMAN, WARRIOR), WARLORD(0x03, false, Race.HUMAN, WARRIOR), KNIGHT(0x04, false, Race.HUMAN, FIGHTER), PALADIN(0x05, false, Race.HUMAN, KNIGHT), DARKAVENGER(0x06, false, Race.HUMAN, KNIGHT), ROGUE(0x07, false, Race.HUMAN, FIGHTER), TREASUREHUNTER(0x08, false, Race.HUMAN, ROGUE), HAWKEYE(0x09, false, Race.HUMAN, ROGUE), MAGE(0x0a, true, Race.HUMAN, null), WIZARD(0x0b, true, Race.HUMAN, MAGE), SORCEROR(0x0c, true, Race.HUMAN, WIZARD), NECROMANCER(0x0d, true, Race.HUMAN, WIZARD), WARLOCK(0x0e, true, true, Race.HUMAN, WIZARD), CLERIC(0x0f, true, Race.HUMAN, MAGE), BISHOP(0x10, true, Race.HUMAN, CLERIC), PROPHET(0x11, true, Race.HUMAN, CLERIC),

	ELVEN_FIGHTER(0x12, false, Race.ELF, null), ELVEN_KNIGHT(0x13, false, Race.ELF, ELVEN_FIGHTER), TEMPLE_KNIGHT(0x14, false, Race.ELF, ELVEN_KNIGHT), SWORDSINGER(0x15, false, Race.ELF, ELVEN_KNIGHT), ELVEN_SCOUT(0x16, false, Race.ELF, ELVEN_FIGHTER), PLAINSWALKER(0x17, false, Race.ELF, ELVEN_SCOUT), SILVERRANGER(0x18, false, Race.ELF, ELVEN_SCOUT), ELVEN_MAGE(0x19, true, Race.ELF, null), ELVEN_WIZARD(0x1a, true, Race.ELF, ELVEN_MAGE), SPELLSINGER(0x1b, true, Race.ELF, ELVEN_WIZARD), ELEMENTAL_SUMMONER(0x1c, true, true, Race.ELF, ELVEN_WIZARD), ORACLE(0x1d, true, Race.ELF, ELVEN_MAGE), ELDER(0x1e, true, Race.ELF, ORACLE),

	DARK_FIGHTER(0x1f, false, Race.DARKELF, null), PALUS_KNIGHT(0x20, false, Race.DARKELF, DARK_FIGHTER), SHILLEN_KNIGHT(0x21, false, Race.DARKELF, PALUS_KNIGHT), BLADEDANCER(0x22, false, Race.DARKELF, PALUS_KNIGHT), ASSASSIN(0x23, false, Race.DARKELF, DARK_FIGHTER), ABYSSWALKER(0x24, false, Race.DARKELF, ASSASSIN), PHANTOMRANGER(0x25, false, Race.DARKELF, ASSASSIN), DARK_MAGE(0x26, true, Race.DARKELF, null), DARK_WIZARD(0x27, true, Race.DARKELF, DARK_MAGE), SPELLHOWLER(0x28, true, Race.DARKELF, DARK_WIZARD), PHANTOM_SUMMONER(0x29, true, true, Race.DARKELF, DARK_WIZARD), SHILLEN_ORACLE(0x2a, true, Race.DARKELF, DARK_MAGE), SHILLEN_ELDER(0x2b, true, Race.DARKELF, SHILLEN_ORACLE),

	ORC_FIGHTER(0x2c, false, Race.ORC, null), ORC_RAIDER(0x2d, false, Race.ORC, ORC_FIGHTER), DESTROYER(0x2e, false, Race.ORC, ORC_RAIDER), ORC_MONK(0x2f, false, Race.ORC, ORC_FIGHTER), TYRANT(0x30, false, Race.ORC, ORC_MONK), ORC_MAGE(0x31, true, Race.ORC, null), ORC_SHAMAN(0x32, true, Race.ORC, ORC_MAGE), OVERLORD(0x33, true, Race.ORC, ORC_SHAMAN), WARCRYER(0x34, true, Race.ORC, ORC_SHAMAN),

	DWARVEN_FIGHTER(0x35, false, Race.DWARF, null), SCAVENGER(0x36, false, Race.DWARF, DWARVEN_FIGHTER), BOUNTYHUNTER(0x37, false, Race.DWARF, SCAVENGER), ARTISAN(0x38, false, Race.DWARF, DWARVEN_FIGHTER), WARSMITH(0x39, false, Race.DWARF, ARTISAN),

	DUELIST(0x58, false, Race.HUMAN, GLADIATOR), DREADNOUGHT(0x59, false, Race.HUMAN, WARLORD), PHOENIX_KNIGHT(0x5a, false, Race.HUMAN, PALADIN), HELL_KNIGHT(0x5b, false, Race.HUMAN, DARKAVENGER), SAGITTARIUS(0x5c, false, Race.HUMAN, HAWKEYE), ADVENTURER(0x5d, false, Race.HUMAN, TREASUREHUNTER), ARCHMAGE(0x5e, true, Race.HUMAN, SORCEROR), SOULTAKER(0x5f, true, Race.HUMAN, NECROMANCER), ARCANALORD(0x60, true, true, Race.HUMAN, WARLOCK), CARDINAL(0x61, true, Race.HUMAN, BISHOP), HIEROPHANT(0x62, true, Race.HUMAN, PROPHET),
	
	EVA_TEMPLAR(0x63, false, Race.ELF, TEMPLE_KNIGHT), SWORDMUSE(0x64, false, Race.ELF, SWORDSINGER), WINDRIDER(0x65, false, Race.ELF, PLAINSWALKER), MOONLIGHTSENTINEL(0x66, false, Race.ELF, SILVERRANGER), MYSTICMUSE(0x67, true, Race.ELF, SPELLSINGER), ELEMENTAL_MASTER(0x68, true, true, Race.ELF, ELEMENTAL_SUMMONER), EVA_SAINT(0x69, true, Race.ELF, ELDER),

	SHILLEN_TEMPLAR(0x6a, false, Race.DARKELF, SHILLEN_KNIGHT), SPECTRAL_DANCER(0x6b, false, Race.DARKELF, BLADEDANCER), GHOSTHUNTER(0x6c, false, Race.DARKELF, ABYSSWALKER), GHOSTSENTINEL(0x6d, false, Race.DARKELF, PHANTOMRANGER), STORMSCREAMER(0x6e, true, Race.DARKELF, SPELLHOWLER), SPECTRAL_MASTER(0x6f, true, true, Race.DARKELF, PHANTOM_SUMMONER), SHILLEN_SAINT(0x70, true, Race.DARKELF, SHILLEN_ELDER),

	TITAN(0x71, false, Race.ORC, DESTROYER), GRANDKHAVATARI(0x72, false, Race.ORC, TYRANT), DOMINATOR(0x73, true, Race.ORC, OVERLORD), DOOMCRYER(0x74, true, Race.ORC, WARCRYER),

	FORTUNE_SEEKER(0x75, false, Race.DWARF, BOUNTYHUNTER), MAESTRO(0x76, false, Race.DWARF, WARSMITH),

	MALE_SOILDER(0x7b, false, Race.KAMAEL, null), FEMALE_SOILDER(0x7C, false, Race.KAMAEL, null), TROOPER(0x7D, false, Race.KAMAEL, MALE_SOILDER), WARDER(0x7E, false, Race.KAMAEL, FEMALE_SOILDER), BERSERKER(0x7F, false, Race.KAMAEL, TROOPER), MALE_SOULBREAKER(0x80, false, Race.KAMAEL, TROOPER), FEMALE_SOULBREAKER(0x81, false, Race.KAMAEL, WARDER), ARBALESTER(0x82, false, Race.KAMAEL, WARDER), DOOMBRINGER(0x83, false, Race.KAMAEL, BERSERKER), MALE_SOULHOUND(0x84, false, Race.KAMAEL, MALE_SOULBREAKER), FEMALE_SOULHOUND(0x85, false, Race.KAMAEL, FEMALE_SOULBREAKER), TRICKSTER(0x86, false, Race.KAMAEL, ARBALESTER), INSPECTOR(0x87, false, Race.KAMAEL, WARDER), JUDICATOR(0x88, false, Race.KAMAEL, INSPECTOR);
	
	private final int _id;
	private final boolean _isMage;
	private final boolean _isSummoner;
	private final Race _race;
	private final ClassId _parent;
	private static Map<Integer, ClassId> _classIdMap = new HashMap<>(ClassId.values().length);
	static
	{
		for (final ClassId classId : ClassId.values())
		{
			_classIdMap.put(classId.getId(), classId);
		}
	}

	private ClassId(int pId, boolean pIsMage, Race pRace, ClassId pParent)
	{
		_id = pId;
		_isMage = pIsMage;
		_isSummoner = false;
		_race = pRace;
		_parent = pParent;
	}

	private ClassId(int pId, boolean pIsMage, boolean pIsSummoner, Race pRace, ClassId pParent)
	{
		_id = pId;
		_isMage = pIsMage;
		_isSummoner = pIsSummoner;
		_race = pRace;
		_parent = pParent;
	}

	@Override
	public final int getId()
	{
		return _id;
	}

	public final String getName(String lang)
	{
		return ServerStorage.getInstance().getString(lang, "ClassName." + _id);
	}

	public final boolean isMage()
	{
		return _isMage;
	}

	public final boolean isSummoner()
	{
		return _isSummoner;
	}

	public final Race getRace()
	{
		return _race;
	}

	public final boolean childOf(ClassId cid)
	{
		return _parent == null ? false : _parent == cid ? true : _parent.childOf(cid);
	}

	public final boolean equalsOrChildOf(ClassId cid)
	{
		return (this == cid) || childOf(cid);
	}

	public final int level()
	{
		return _parent == null ? 0 : 1 + _parent.level();
	}

	public final ClassId getParent()
	{
		return _parent;
	}

	public static ClassId getClassId(int cId)
	{
		final var classId = _classIdMap.get(cId);
		return classId == null ? values()[0] : classId;
	}
	
	public ClassId getRootClassId()
	{
		if (_parent != null)
		{
			return _parent.getRootClassId();
		}
		return this;
	}
}