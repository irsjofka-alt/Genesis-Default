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
 * this program. If not, see <http://eternity-world.ru/>.
 */
package gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2e.commons.log.LoggerObject;
import fake.ai.AbyssWalkerAI;
import fake.ai.AdventurerAI;
import fake.ai.ArbalesterAI;
import fake.ai.ArchmageAI;
import fake.ai.ArtisanAI;
import fake.ai.AssassinAI;
import fake.ai.BerserkerAI;
import fake.ai.BladedancerAI;
import fake.ai.BountyHunterAI;
import fake.ai.DarkAvengerAI;
import fake.ai.DarkElvenFighterAI;
import fake.ai.DarkElvenMysticAI;
import fake.ai.DarkWizardAI;
import fake.ai.DestroyerAI;
import fake.ai.DominatorAI;
import fake.ai.DoombringerAI;
import fake.ai.DreadnoughtAI;
import fake.ai.DuelistAI;
import fake.ai.DwarvenFighterAI;
import fake.ai.ElvenFighterAI;
import fake.ai.ElvenKnightAI;
import fake.ai.ElvenMysticAI;
import fake.ai.ElvenScoutAI;
import fake.ai.ElvenWizardAI;
import fake.ai.FakePlayerAI;
import fake.ai.FemaleSoldierAI;
import fake.ai.FemaleSoulbreakerAI;
import fake.ai.FemaleSoulhoundAI;
import fake.ai.FortuneSeekerAI;
import fake.ai.GhostHunterAI;
import fake.ai.GhostSentinelAI;
import fake.ai.GladiatorAI;
import fake.ai.GrandKhavatariAI;
import fake.ai.HawkeyeAI;
import fake.ai.HellKnightAI;
import fake.ai.HumanFighterAI;
import fake.ai.HumanMysticAI;
import fake.ai.KnightAI;
import fake.ai.MaestroAI;
import fake.ai.MaleSoldierAI;
import fake.ai.MaleSoulbreakerAI;
import fake.ai.MaleSoulhoundAI;
import fake.ai.MoonlightSentinelAI;
import fake.ai.MysticMuseAI;
import fake.ai.NecromancerAI;
import fake.ai.OrcFighterAI;
import fake.ai.OrcMonkAI;
import fake.ai.OrcMysticAI;
import fake.ai.OrcRaiderAI;
import fake.ai.OrcShamanAI;
import fake.ai.OverlordAI;
import fake.ai.PaladinAI;
import fake.ai.PalusKnightAI;
import fake.ai.PhantomRangerAI;
import fake.ai.PlainsWalkerAI;
import fake.ai.RogueAI;
import fake.ai.SaggitariusAI;
import fake.ai.ScavengerAI;
import fake.ai.ShillienKnightAI;
import fake.ai.ShillienTemplarAI;
import fake.ai.SilverRangerAI;
import fake.ai.SorcerorAI;
import fake.ai.SoultakerAI;
import fake.ai.SpellhowlerAI;
import fake.ai.SpellsingerAI;
import fake.ai.StormScreamerAI;
import fake.ai.SwordSingerAI;
import fake.ai.TempleKnightAI;
import fake.ai.TitanAI;
import fake.ai.TreasureHunterAI;
import fake.ai.TricksterAI;
import fake.ai.TrooperAI;
import fake.ai.TyrantAI;
import fake.ai.WarcryerAI;
import fake.ai.WarderAI;
import fake.ai.WarlordAI;
import fake.ai.WarriorAI;
import fake.ai.WarsmithAI;
import fake.ai.WindRiderAI;
import fake.ai.WizardAI;
import gameserver.model.base.ClassId;

public class FakeClassesParser extends LoggerObject
{
	private static List<ClassId> _baseClasses = new ArrayList<>();
	private static List<ClassId> _firstClasses = new ArrayList<>();
	private static List<ClassId> _secondClasses = new ArrayList<>();
	private static List<ClassId> _thirdClasses = new ArrayList<>();
	
	protected FakeClassesParser()
	{
		load();
	}
	
	public void load()
	{
		_baseClasses.clear();
		_firstClasses.clear();
		_secondClasses.clear();
		_thirdClasses.clear();
		lordClasses();
		info("Loaded " + (_baseClasses.size() + _firstClasses.size() + _secondClasses.size() + _thirdClasses.size()) + " fake classes.");
	}
	
	public void lordClasses()
	{
		_baseClasses.add(ClassId.FIGHTER);
		_baseClasses.add(ClassId.MAGE);
		_baseClasses.add(ClassId.ELVEN_FIGHTER);
		_baseClasses.add(ClassId.ELVEN_MAGE);
		_baseClasses.add(ClassId.DARK_FIGHTER);
		_baseClasses.add(ClassId.DARK_MAGE);
		_baseClasses.add(ClassId.ORC_FIGHTER);
		_baseClasses.add(ClassId.ORC_MAGE);
		_baseClasses.add(ClassId.DWARVEN_FIGHTER);
		_baseClasses.add(ClassId.MALE_SOILDER);
		_baseClasses.add(ClassId.FEMALE_SOILDER);
		_firstClasses.add(ClassId.WARRIOR);
		_firstClasses.add(ClassId.KNIGHT);
		_firstClasses.add(ClassId.ROGUE);
		_firstClasses.add(ClassId.WIZARD);
		_firstClasses.add(ClassId.ELVEN_KNIGHT);
		_firstClasses.add(ClassId.ELVEN_SCOUT);
		_firstClasses.add(ClassId.ELVEN_WIZARD);
		_firstClasses.add(ClassId.PALUS_KNIGHT);
		_firstClasses.add(ClassId.ASSASSIN);
		_firstClasses.add(ClassId.DARK_WIZARD);
		_firstClasses.add(ClassId.ORC_RAIDER);
		_firstClasses.add(ClassId.ORC_MONK);
		_firstClasses.add(ClassId.ORC_SHAMAN);
		_firstClasses.add(ClassId.SCAVENGER);
		_firstClasses.add(ClassId.ARTISAN);
		_firstClasses.add(ClassId.TROOPER);
		_firstClasses.add(ClassId.WARDER);
		_secondClasses.add(ClassId.GLADIATOR);
		_secondClasses.add(ClassId.WARLORD);
		_secondClasses.add(ClassId.PALADIN);
		_secondClasses.add(ClassId.DARKAVENGER);
		_secondClasses.add(ClassId.TREASUREHUNTER);
		_secondClasses.add(ClassId.HAWKEYE);
		_secondClasses.add(ClassId.SORCEROR);
		_secondClasses.add(ClassId.NECROMANCER);
		_secondClasses.add(ClassId.TEMPLE_KNIGHT);
		_secondClasses.add(ClassId.SWORDSINGER);
		_secondClasses.add(ClassId.PLAINSWALKER);
		_secondClasses.add(ClassId.SILVERRANGER);
		_secondClasses.add(ClassId.SPELLSINGER);
		_secondClasses.add(ClassId.SHILLEN_KNIGHT);
		_secondClasses.add(ClassId.BLADEDANCER);
		_secondClasses.add(ClassId.ABYSSWALKER);
		_secondClasses.add(ClassId.PHANTOMRANGER);
		_secondClasses.add(ClassId.SPELLHOWLER);
		_secondClasses.add(ClassId.DESTROYER);
		_secondClasses.add(ClassId.TYRANT);
		_secondClasses.add(ClassId.OVERLORD);
		_secondClasses.add(ClassId.WARCRYER);
		_secondClasses.add(ClassId.BOUNTYHUNTER);
		_secondClasses.add(ClassId.WARSMITH);
		_secondClasses.add(ClassId.BERSERKER);
		_secondClasses.add(ClassId.MALE_SOULBREAKER);
		_secondClasses.add(ClassId.FEMALE_SOULBREAKER);
		_secondClasses.add(ClassId.ARBALESTER);
		_thirdClasses.add(ClassId.STORMSCREAMER);
		_thirdClasses.add(ClassId.GHOSTSENTINEL);
		_thirdClasses.add(ClassId.GHOSTHUNTER);
		_thirdClasses.add(ClassId.SHILLEN_TEMPLAR);
		_thirdClasses.add(ClassId.DOMINATOR);
		_thirdClasses.add(ClassId.TITAN);
		_thirdClasses.add(ClassId.GRANDKHAVATARI);
		_thirdClasses.add(ClassId.MAESTRO);
		_thirdClasses.add(ClassId.FORTUNE_SEEKER);
		_thirdClasses.add(ClassId.SAGITTARIUS);
		_thirdClasses.add(ClassId.ARCHMAGE);
		_thirdClasses.add(ClassId.SOULTAKER);
		_thirdClasses.add(ClassId.MYSTICMUSE);
		_thirdClasses.add(ClassId.MOONLIGHTSENTINEL);
		_thirdClasses.add(ClassId.ADVENTURER);
		_thirdClasses.add(ClassId.WINDRIDER);
		_thirdClasses.add(ClassId.DUELIST);
		_thirdClasses.add(ClassId.DREADNOUGHT);
		_thirdClasses.add(ClassId.HELL_KNIGHT);
		_thirdClasses.add(ClassId.MAESTRO);
		_thirdClasses.add(ClassId.FORTUNE_SEEKER);
		_thirdClasses.add(ClassId.DOOMBRINGER);
		_thirdClasses.add(ClassId.MALE_SOULHOUND);
		_thirdClasses.add(ClassId.FEMALE_SOULHOUND);
		_thirdClasses.add(ClassId.TRICKSTER);
	}
	
	public List<ClassId> getBaseClasses()
	{
		return _baseClasses;
	}
	
	public List<ClassId> getFirstClasses()
	{
		return _firstClasses;
	}
	
	public List<ClassId> getSecondClasses()
	{
		return _secondClasses;
	}
	
	public List<ClassId> getThirdClasses()
	{
		return _thirdClasses;
	}
	
	public Map<ClassId, Class<? extends FakePlayerAI>> getAllAIs()
	{
		final Map<ClassId, Class<? extends FakePlayerAI>> ais = new HashMap<>();
		// Base classes
		ais.put(ClassId.FIGHTER, HumanFighterAI.class);
		ais.put(ClassId.MAGE, HumanMysticAI.class);
		ais.put(ClassId.ELVEN_FIGHTER, ElvenFighterAI.class);
		ais.put(ClassId.ELVEN_MAGE, ElvenMysticAI.class);
		ais.put(ClassId.DARK_FIGHTER, DarkElvenFighterAI.class);
		ais.put(ClassId.DARK_MAGE, DarkElvenMysticAI.class);
		ais.put(ClassId.ORC_FIGHTER, OrcFighterAI.class);
		ais.put(ClassId.ORC_MAGE, OrcMysticAI.class);
		ais.put(ClassId.DWARVEN_FIGHTER, DwarvenFighterAI.class);
		ais.put(ClassId.MALE_SOILDER, MaleSoldierAI.class);
		ais.put(ClassId.FEMALE_SOILDER, FemaleSoldierAI.class);
		// First profession classes
		ais.put(ClassId.WARRIOR, WarriorAI.class);
		ais.put(ClassId.KNIGHT, KnightAI.class);
		ais.put(ClassId.ROGUE, RogueAI.class);
		ais.put(ClassId.WIZARD, WizardAI.class);
		ais.put(ClassId.ELVEN_KNIGHT, ElvenKnightAI.class);
		ais.put(ClassId.ELVEN_SCOUT, ElvenScoutAI.class);
		ais.put(ClassId.ELVEN_WIZARD, ElvenWizardAI.class);
		ais.put(ClassId.PALUS_KNIGHT, PalusKnightAI.class);
		ais.put(ClassId.ASSASSIN, AssassinAI.class);
		ais.put(ClassId.DARK_WIZARD, DarkWizardAI.class);
		ais.put(ClassId.ORC_RAIDER, OrcRaiderAI.class);
		ais.put(ClassId.ORC_MONK, OrcMonkAI.class);
		ais.put(ClassId.ORC_SHAMAN, OrcShamanAI.class);
		ais.put(ClassId.SCAVENGER, ScavengerAI.class);
		ais.put(ClassId.ARTISAN, ArtisanAI.class);
		ais.put(ClassId.TROOPER, TrooperAI.class);
		ais.put(ClassId.WARDER, WarderAI.class);
		// Second profession classes
		ais.put(ClassId.GLADIATOR, GladiatorAI.class);
		ais.put(ClassId.WARLORD, WarlordAI.class);
		ais.put(ClassId.PALADIN, PaladinAI.class);
		ais.put(ClassId.DARKAVENGER, DarkAvengerAI.class);
		ais.put(ClassId.TREASUREHUNTER, TreasureHunterAI.class);
		ais.put(ClassId.HAWKEYE, HawkeyeAI.class);
		ais.put(ClassId.SORCEROR, SorcerorAI.class);
		ais.put(ClassId.NECROMANCER, NecromancerAI.class);
		ais.put(ClassId.TEMPLE_KNIGHT, TempleKnightAI.class);
		ais.put(ClassId.SWORDSINGER, SwordSingerAI.class);
		ais.put(ClassId.PLAINSWALKER, PlainsWalkerAI.class);
		ais.put(ClassId.SILVERRANGER, SilverRangerAI.class);
		ais.put(ClassId.SPELLSINGER, SpellsingerAI.class);
		ais.put(ClassId.SHILLEN_KNIGHT, ShillienKnightAI.class);
		ais.put(ClassId.BLADEDANCER, BladedancerAI.class);
		ais.put(ClassId.ABYSSWALKER, AbyssWalkerAI.class);
		ais.put(ClassId.PHANTOMRANGER, PhantomRangerAI.class);
		ais.put(ClassId.SPELLHOWLER, SpellhowlerAI.class);
		ais.put(ClassId.DESTROYER, DestroyerAI.class);
		ais.put(ClassId.TYRANT, TyrantAI.class);
		ais.put(ClassId.OVERLORD, OverlordAI.class);
		ais.put(ClassId.WARCRYER, WarcryerAI.class);
		ais.put(ClassId.BOUNTYHUNTER, BountyHunterAI.class);
		ais.put(ClassId.WARSMITH, WarsmithAI.class);
		ais.put(ClassId.BERSERKER, BerserkerAI.class);
		ais.put(ClassId.MALE_SOULBREAKER, MaleSoulbreakerAI.class);
		ais.put(ClassId.FEMALE_SOULBREAKER, FemaleSoulbreakerAI.class);
		ais.put(ClassId.ARBALESTER, ArbalesterAI.class);
		// Third profession classes
		ais.put(ClassId.STORMSCREAMER, StormScreamerAI.class);
		ais.put(ClassId.MYSTICMUSE, MysticMuseAI.class);
		ais.put(ClassId.ARCHMAGE, ArchmageAI.class);
		ais.put(ClassId.SOULTAKER, SoultakerAI.class);
		ais.put(ClassId.SAGITTARIUS, SaggitariusAI.class);
		ais.put(ClassId.MOONLIGHTSENTINEL, MoonlightSentinelAI.class);
		ais.put(ClassId.GHOSTSENTINEL, GhostSentinelAI.class);
		ais.put(ClassId.ADVENTURER, AdventurerAI.class);
		ais.put(ClassId.WINDRIDER, WindRiderAI.class);
		ais.put(ClassId.GHOSTHUNTER, GhostHunterAI.class);
		ais.put(ClassId.DOMINATOR, DominatorAI.class);
		ais.put(ClassId.TITAN, TitanAI.class);
		ais.put(ClassId.SHILLEN_TEMPLAR, ShillienTemplarAI.class);
		ais.put(ClassId.DUELIST, DuelistAI.class);
		ais.put(ClassId.HELL_KNIGHT, HellKnightAI.class);
		ais.put(ClassId.GRANDKHAVATARI, GrandKhavatariAI.class);
		ais.put(ClassId.DREADNOUGHT, DreadnoughtAI.class);
		ais.put(ClassId.MAESTRO, MaestroAI.class);
		ais.put(ClassId.FORTUNE_SEEKER, FortuneSeekerAI.class);
		ais.put(ClassId.DOOMBRINGER, DoombringerAI.class);
		ais.put(ClassId.MALE_SOULHOUND, MaleSoulhoundAI.class);
		ais.put(ClassId.FEMALE_SOULHOUND, FemaleSoulhoundAI.class);
		ais.put(ClassId.TRICKSTER, TricksterAI.class);
		return ais;
	}
	
	public static FakeClassesParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FakeClassesParser _instance = new FakeClassesParser();
	}
}