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
package gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.HostInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nio.impl.HaProxySelectorThread;
import org.nio.impl.SelectorThread;

import l2e.commons.listener.Listener;
import l2e.commons.listener.ListenerList;
import l2e.commons.net.IPSettings;
import l2e.commons.util.StatsUtils;
import fake.FakePlayerManager;
import gameserver.data.dao.ClanVariablesDAO;
import gameserver.data.holder.CharMiniGameHolder;
import gameserver.data.holder.CharNameHolder;
import gameserver.data.holder.CharSummonHolder;
import gameserver.data.holder.ClanHolder;
import gameserver.data.holder.CrestHolder;
import gameserver.data.holder.NpcBufferHolder;
import gameserver.data.holder.SpawnHolder;
import gameserver.data.htm.HtmCache;
import gameserver.data.htm.ImagesCache;
import gameserver.data.parser.AdminParser;
import gameserver.data.parser.ArmorSetsParser;
import gameserver.data.parser.AugmentationParser;
import gameserver.data.parser.BotReportParser;
import gameserver.data.parser.BuyListParser;
import gameserver.data.parser.CategoryParser;
import gameserver.data.parser.CharTemplateParser;
import gameserver.data.parser.ClanParser;
import gameserver.data.parser.ClassBalanceParser;
import gameserver.data.parser.ClassListParser;
import gameserver.data.parser.ClassMasterParser;
import gameserver.data.parser.ColosseumFenceParser;
import gameserver.data.parser.CommunityTeleportsParser;
import gameserver.data.parser.CubicParser;
import gameserver.data.parser.DamageLimitParser;
import gameserver.data.parser.DonateRatesParser;
import gameserver.data.parser.DonationParser;
import gameserver.data.parser.DoorParser;
import gameserver.data.parser.DressArmorParser;
import gameserver.data.parser.DressCloakParser;
import gameserver.data.parser.DressHatParser;
import gameserver.data.parser.DressShieldParser;
import gameserver.data.parser.DressWeaponParser;
import gameserver.data.parser.EnchantItemGroupsParser;
import gameserver.data.parser.EnchantItemHPBonusParser;
import gameserver.data.parser.EnchantItemOptionsParser;
import gameserver.data.parser.EnchantItemParser;
import gameserver.data.parser.EnchantSkillGroupsParser;
import gameserver.data.parser.ExchangeItemParser;
import gameserver.data.parser.ExpPercentLostParser;
import gameserver.data.parser.ExperienceParser;
import gameserver.data.parser.FightEventMapParser;
import gameserver.data.parser.FightEventParser;
import gameserver.data.parser.FishMonstersParser;
import gameserver.data.parser.FishParser;
import gameserver.data.parser.FoundationParser;
import gameserver.data.parser.HennaParser;
import gameserver.data.parser.HerbsDropParser;
import gameserver.data.parser.HitConditionBonusParser;
import gameserver.data.parser.InitialEquipmentParser;
import gameserver.data.parser.InitialShortcutParser;
import gameserver.data.parser.ItemsParser;
import gameserver.data.parser.LimitStatParser;
import gameserver.data.parser.MerchantPriceParser;
import gameserver.data.parser.MultiSellParser;
import gameserver.data.parser.NpcsParser;
import gameserver.data.parser.OptionsParser;
import gameserver.data.parser.PetitionGroupParser;
import gameserver.data.parser.PetsParser;
import gameserver.data.parser.PremiumAccountsParser;
import gameserver.data.parser.ProductItemParser;
import gameserver.data.parser.PromoCodeParser;
import gameserver.data.parser.QuestsParser;
import gameserver.data.parser.RecipeParser;
import gameserver.data.parser.ReflectionParser;
import gameserver.data.parser.SchemesParser;
import gameserver.data.parser.SellBuffsParser;
import gameserver.data.parser.SkillBalanceParser;
import gameserver.data.parser.SkillTreesParser;
import gameserver.data.parser.SkillsParser;
import gameserver.data.parser.SoulCrystalParser;
import gameserver.data.parser.SpawnParser;
import gameserver.data.parser.SpecialRatesParser;
import gameserver.data.parser.StaticObjectsParser;
import gameserver.data.parser.TeleLocationParser;
import gameserver.data.parser.TransformParser;
import gameserver.data.parser.VoteRewardParser;
import gameserver.data.parser.WorldEventParser;
import gameserver.database.DatabaseFactory;
import gameserver.geodata.GeoEngine;
import gameserver.handler.actionhandlers.ActionHandler;
import gameserver.handler.actionshifthandlers.ActionShiftHandler;
import gameserver.handler.admincommandhandlers.AdminCommandHandler;
import gameserver.handler.bypasshandlers.BypassHandler;
import gameserver.handler.chathandlers.ChatHandler;
import gameserver.handler.communityhandlers.CommunityBoardHandler;
import gameserver.handler.effecthandlers.EffectHandler;
import gameserver.handler.itemhandlers.ItemHandler;
import gameserver.handler.skillhandlers.SkillHandler;
import gameserver.handler.targethandlers.TargetHandler;
import gameserver.handler.usercommandhandlers.UserCommandHandler;
import gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import gameserver.idfactory.IdFactory;
import gameserver.instancemanager.AirShipManager;
import gameserver.instancemanager.AuctionManager;
import gameserver.instancemanager.AutoFarmManager;
import gameserver.instancemanager.BloodAltarManager;
import gameserver.instancemanager.BoatManager;
import gameserver.instancemanager.BotCheckManager;
import gameserver.instancemanager.CHSiegeManager;
import gameserver.instancemanager.CastleManager;
import gameserver.instancemanager.CastleManorManager;
import gameserver.instancemanager.ChampionManager;
import gameserver.instancemanager.ClanHallManager;
import gameserver.instancemanager.CoupleManager;
import gameserver.instancemanager.CursedWeaponsManager;
import gameserver.instancemanager.DailyRewardManager;
import gameserver.instancemanager.DailyTaskManager;
import gameserver.instancemanager.DayNightSpawnManager;
import gameserver.instancemanager.DimensionalRiftManager;
import gameserver.instancemanager.DoubleSessionManager;
import gameserver.instancemanager.DragonValleyManager;
import gameserver.instancemanager.DropManager;
import gameserver.instancemanager.EpicBossManager;
import gameserver.instancemanager.FortManager;
import gameserver.instancemanager.FortSiegeManager;
import gameserver.instancemanager.FourSepulchersManager;
import gameserver.instancemanager.HellboundManager;
import gameserver.instancemanager.ItemAuctionManager;
import gameserver.instancemanager.ItemRecoveryManager;
import gameserver.instancemanager.LakfiManager;
import gameserver.instancemanager.MailManager;
import gameserver.instancemanager.MapRegionManager;
import gameserver.instancemanager.MercTicketManager;
import gameserver.instancemanager.NpcStatManager;
import gameserver.instancemanager.OnlineRewardManager;
import gameserver.instancemanager.PetitionManager;
import gameserver.instancemanager.PunishmentManager;
import gameserver.instancemanager.PvpColorManager;
import gameserver.instancemanager.QuestManager;
import gameserver.instancemanager.RaidBossSpawnManager;
import gameserver.instancemanager.ReflectionManager;
import gameserver.instancemanager.RewardManager;
import gameserver.instancemanager.ServerVariables;
import gameserver.instancemanager.SiegeManager;
import gameserver.instancemanager.SoDManager;
import gameserver.instancemanager.SoIManager;
import gameserver.instancemanager.SpecialBypassManager;
import gameserver.instancemanager.TerritoryWarManager;
import gameserver.instancemanager.UndergroundColiseumManager;
import gameserver.instancemanager.VipManager;
import gameserver.instancemanager.WalkingManager;
import gameserver.instancemanager.WeeklyTraderManager;
import gameserver.instancemanager.ZoneManager;
import gameserver.instancemanager.games.FishingChampionship;
import gameserver.instancemanager.games.MonsterRaceManager;
import gameserver.instancemanager.games.krateiscube.KrateisCubeManager;
import gameserver.instancemanager.mods.DailyItemManager;
import gameserver.instancemanager.mods.EnchantSkillManager;
import gameserver.instancemanager.mods.TimeSkillsTaskManager;
import gameserver.listener.ScriptListenerLoader;
import gameserver.listener.game.OnShutdownListener;
import gameserver.listener.game.OnStartListener;
import gameserver.model.AutoSpawnHandler;
import gameserver.model.World;
import gameserver.model.entity.Hero;
import gameserver.model.entity.events.EventsDropManager;
import gameserver.model.entity.events.cleft.AerialCleftEvent;
import gameserver.model.entity.events.custom.Leprechaun;
import gameserver.model.entity.events.custom.achievements.AchievementManager;
import gameserver.model.entity.events.model.FightEventManager;
import gameserver.model.entity.events.model.FightEventNpcManager;
import gameserver.model.entity.events.model.FightLastStatsManager;
import gameserver.model.olympiad.Olympiad;
import gameserver.model.service.autofarm.FarmSettings;
import gameserver.model.strings.server.ServerStorage;
import gameserver.network.GameClient;
import gameserver.network.GamePacketHandler;
import gameserver.network.communication.AuthServerCommunication;
import gameserver.taskmanager.AutoAnnounceTaskManager;
import gameserver.taskmanager.AutoTaskManager;
import gameserver.taskmanager.ItemsAutoDestroy;
import gameserver.taskmanager.RestoreOfflineTraders;
import gameserver.utils.Functions;
import gameserver.utils.strixplatform.StrixPlatform;

public class GameServer
{
	private static final Logger _log = LogManager.getLogger(GameServer.class);

	public class GameServerListenerList extends ListenerList<GameServer>
	{
		public void onStart()
		{
			for (final Listener<GameServer> listener : getListeners())
			{
				if (OnStartListener.class.isInstance(listener))
				{
					((OnStartListener) listener).onStart();
				}
			}
		}
		
		public void onShutdown()
		{
			for (final Listener<GameServer> listener : getListeners())
			{
				if (OnShutdownListener.class.isInstance(listener))
				{
					((OnShutdownListener) listener).onShutdown();
				}
			}
		}
	}
	
	private final List<SelectorThread<GameClient>> _selectorThreads = new ArrayList<>();
	
	private final GameServerListenerList _listeners;
	private final IdFactory _idFactory;
	public static GameServer _instance;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	public static Date server_started;
	
	public List<SelectorThread<GameClient>> getSelectorThreads()
	{
		return _selectorThreads;
	}
	
	public GameServer() throws Exception
	{
		_instance = this;
		_listeners = new GameServerListenerList();
		_idFactory = IdFactory.getInstance();

		if (!_idFactory.isInitialized())
		{
			_log.warn("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}

		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		new File("log/game").mkdirs();
		new File("log/game/chat").mkdirs();
		
		final var host = IPSettings.getInstance().getGameServerHost();
		if (host == null)
		{
			throw new Exception("Server hosts list is empty!");
		}
		
		if (host.getAddress() != null)
		{
			while (!checkFreePort(host.getAddress(), host.getPort()))
			{
				_log.warn("Port '" + host.getPort() + "' on host '" + host.getAddress() + "' is allready binded. Please free it and restart server.");
				try
				{
					Thread.sleep(1000L);
				}
				catch (final InterruptedException e2)
				{
				}
			}
		}
		
		if (Config.ALLOW_CUSTOM_INTERFACE)
		{
			emudev.KeyChecker.getInstance();
		}
		StrixPlatform.getInstance();
		
		printSection("Engines");
		ScriptListenerLoader.getInstance();
		ServerStorage.getInstance();
		
		printSection("World");
		GameTimeController.getInstance();
		ReflectionManager.getInstance();
		World.init();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		EventsDropManager.getInstance();
		CategoryParser.getInstance();
		ServerVariables.getVars();

		printSection("Skills");
		EffectHandler.getInstance().executeScript();
		EnchantSkillGroupsParser.getInstance();
		SkillTreesParser.getInstance();
		SkillsParser.getInstance();
		SchemesParser.getInstance();
		if (Config.ALLOW_SELLBUFFS_COMMAND)
		{
			SellBuffsParser.getInstance();
		}

		printSection("Items");
		ItemsParser.getInstance();
		ProductItemParser.getInstance();
		DonationParser.getInstance();
		ExchangeItemParser.getInstance();
		FoundationParser.getInstance();
		if (Config.ALLOW_VISUAL_ARMOR_COMMAND)
		{
			DressArmorParser.getInstance();
			DressCloakParser.getInstance();
			DressShieldParser.getInstance();
			DressHatParser.getInstance();
			DressWeaponParser.getInstance();
		}
		SoulCrystalParser.getInstance();
		EnchantItemGroupsParser.getInstance();
		EnchantItemParser.getInstance();
		EnchantItemOptionsParser.getInstance();
		OptionsParser.getInstance();
		EnchantItemHPBonusParser.getInstance();
		MerchantPriceParser.getInstance();
		BuyListParser.getInstance();
		MultiSellParser.getInstance();
		RecipeParser.getInstance();
		ArmorSetsParser.getInstance();
		FishMonstersParser.getInstance();
		FishParser.getInstance();
		FishingChampionship.getInstance();
		HennaParser.getInstance();
		CursedWeaponsManager.getInstance();
		HerbsDropParser.getInstance();

		printSection("Characters");
		ClassListParser.getInstance();
		ClassMasterParser.getInstance();
		InitialEquipmentParser.getInstance();
		InitialShortcutParser.getInstance();
		ExperienceParser.getInstance();
		ExpPercentLostParser.getInstance();
		HitConditionBonusParser.getInstance();
		CharTemplateParser.getInstance();
		CharNameHolder.getInstance();
		PremiumAccountsParser.getInstance();
		DailyTaskManager.getInstance();
		AdminParser.getInstance();
		PetsParser.getInstance();
		CharSummonHolder.getInstance().init();
		CubicParser.getInstance();
		if (Config.NEW_PETITIONING_SYSTEM)
		{
			PetitionGroupParser.getInstance();
		}
		PetitionManager.getInstance();
		if (Config.ENABLE_ANTI_BOT_SYSTEM)
		{
			BotCheckManager.getInstance();
		}
		PromoCodeParser.getInstance();
		PvpColorManager.getInstance();
		LimitStatParser.getInstance();
		
		printSection("Clans");
		ClanParser.getInstance();
		ClanHolder.getInstance();
		ClanVariablesDAO.getInstance().restore();
		ClanHallManager.getInstance();
		CHSiegeManager.getInstance();
		AuctionManager.getInstance();

		printSection("Geodata");
		GeoEngine.getInstance();

		printSection("NPCs");
		ZoneManager.getInstance();
		NpcsParser.getInstance();
		DropManager.getInstance();
		NpcStatManager.getInstance();
		WalkingManager.getInstance();
		StaticObjectsParser.getInstance();
		DoorParser.getInstance();
		ColosseumFenceParser.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().load();
		FortManager.getInstance().load();
		NpcBufferHolder.getInstance();
		ChampionManager.getInstance();
		BloodAltarManager.getInstance();
		RaidBossSpawnManager.getInstance();
		SpawnParser.getInstance();
		SpawnHolder.getInstance();
		DamageLimitParser.getInstance();
		HellboundManager.getInstance();
		ReflectionParser.getInstance();
		ZoneManager.getInstance().createZoneReflections();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		EpicBossManager.getInstance().initZones();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		BotReportParser.getInstance();
		TeleLocationParser.getInstance();
		CommunityTeleportsParser.getInstance();
		AugmentationParser.getInstance();
		TransformParser.getInstance();
		ZoneManager.getInstance().generateTimeZones();
		
		printSection("Seven Signs");
		SevenSigns.getInstance();
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();

		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();

		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();

		printSection("Cache");
		HtmCache.getInstance();
		CrestHolder.getInstance();
		ImagesCache.getInstance();

		printSection("Handlers");
		if (!Config.ALT_DEV_NO_HANDLERS)
		{
			AutoSpawnHandler.getInstance();
			ActionHandler.getInstance();
			ActionShiftHandler.getInstance();
			AdminCommandHandler.getInstance();
			BypassHandler.getInstance();
			ChatHandler.getInstance();
			CommunityBoardHandler.getInstance();
			ItemHandler.getInstance();
			SkillHandler.getInstance();
			TargetHandler.getInstance();
			UserCommandHandler.getInstance();
			VoicedCommandHandler.getInstance();
		}
		
		if (Config.BALANCER_ALLOW)
		{
			printSection("Balancer");
			ClassBalanceParser.getInstance();
			SkillBalanceParser.getInstance();
		}
		
		printSection("Gracia");
		SoDManager.getInstance();
		SoIManager.getInstance();
		AerialCleftEvent.getInstance();

		printSection("Vehicles");
		BoatManager.getInstance();
		AirShipManager.getInstance();

		printSection("Game Processes");
		KrateisCubeManager.getInstance();
		UndergroundColiseumManager.getInstance();
		MonsterRaceManager.getInstance();
		CharMiniGameHolder.getInstance().select();
		
		printSection("Events");
		WorldEventParser.getInstance();
		if (Config.ALLOW_FIGHT_EVENTS)
		{
			FightEventMapParser.getInstance();
			FightEventParser.getInstance();
			FightLastStatsManager.getInstance().restoreStats();
			FightEventManager.getInstance();
			FightEventNpcManager.getInstance();
		}
		else
		{
			_log.info("FightEventManager: All fight events disabled.");
		}
		
		if (Config.ENABLED_LEPRECHAUN)
		{
			Leprechaun.getInstance();
		}

		printSection("Scripts");
		QuestManager.getInstance();
		QuestsParser.getInstance();
		CastleManager.getInstance().activate();
		FortManager.getInstance().activate();
		MerchantPriceParser.getInstance().updateReferences();
		ScriptListenerLoader.getInstance().executeScriptList();
		DragonValleyManager.getInstance();
		
		if (Config.LAKFI_ENABLED)
		{
			LakfiManager.getInstance();
		}
		
		DoubleSessionManager.getInstance().registerEvent(DoubleSessionManager.GAME_ID);
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

		printSection("Protection System");
		PunishmentManager.getInstance();

		printSection("Eternity Mods");
		if (Config.ALLOW_VIP_SYSTEM)
		{
			VipManager.getInstance();
		}
		TimeSkillsTaskManager.getInstance();
		RewardManager.getInstance();
		AchievementManager.getInstance();
		DailyRewardManager.getInstance();
		OnlineRewardManager.getInstance();
		SpecialRatesParser.getInstance();
		DonateRatesParser.getInstance();

		if (Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		WeeklyTraderManager.getInstance();
		EnchantSkillManager.getInstance();
		printSection("Other");
		SpecialBypassManager.getInstance();
		VoteRewardParser.getInstance();
		if (Config.ALLOW_DAILY_ITEMS)
		{
			DailyItemManager.getInstance();
		}
		if (Config.ALLOW_FAKE_PLAYERS)
		{
			_log.info("FakePlayerManager: Loading fake players system...");
			FakePlayerManager.getInstance();
		}
		AutoTaskManager.init();
		ItemsAutoDestroy.getInstance();
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		AutoRestart.getInstance();
		if (Config.ALLOW_RECOVERY_ITEMS)
		{
			ItemRecoveryManager.getInstance();
		}
		if (Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL > 0)
		{
			OnlinePlayers.getInstance();
		}
		
		if (FarmSettings.ALLOW_AUTO_FARM)
		{
			AutoFarmManager.getInstance();
		}
		if (Config.OFFLINE_TRADE_ENABLE || FarmSettings.ALLOW_OFFLINE)
		{
			ThreadPoolManager.getInstance().schedule(new RestoreOfflineTraders(), 10000L);
		}
		getListeners().onStart();
		Toolkit.getDefaultToolkit().beep();

		AutoAnnounceTaskManager.getInstance();

		_log.info("-------------------------------------------------------------------------------");
		StatsUtils.getMemUsage(_log);
		EternityWorld.getTeamInfo(_log);
		_log.info("-------------------------------------------------------------------------------");
		registerSelectorThreads(host);
		AuthServerCommunication.getInstance().start();
		server_started = new Date();
	}

	public static void main(String[] args) throws Exception
	{
		final File logFolder = new File(Config.DATAPACK_ROOT, "log");
		logFolder.mkdir();

		Config.load();
		printSection("Database");
		DatabaseFactory.getInstance();
		ThreadPoolManager.getInstance();
		new GameServer();
	}
	
	private static boolean checkFreePort(String hostname, int port)
	{
		ServerSocket ss = null;
		try
		{
			if (hostname.equalsIgnoreCase("*"))
			{
				ss = new ServerSocket(port);
			}
			else
			{
				ss = new ServerSocket(port, 50, InetAddress.getByName(hostname));
			}
		}
		catch (final Exception e)
		{
			return false;
		}
		finally
		{
			try
			{
				ss.close();
			}
			catch (final Exception e)
			{}
		}
		return true;
	}
	
	private void registerSelectorThreads(HostInfo host)
	{
		registerSelectorThread(new GamePacketHandler(), null, host);
	}

	private void registerSelectorThread(GamePacketHandler gph, String ip, HostInfo host)
	{
		try
		{
			if (host.isAllowHaProxy())
			{
				final var selectorThread = new HaProxySelectorThread<>(Config.SELECTOR_CONFIG, gph, gph, gph, null);
				selectorThread.openServerSocket(ip == null ? null : InetAddress.getByName(ip), host.getPort());
				selectorThread.start();
				_selectorThreads.add(selectorThread);
			}
			else
			{
				final var selectorThread = new SelectorThread<>(Config.SELECTOR_CONFIG, gph, gph, gph, null);
				selectorThread.openServerSocket(ip == null ? null : InetAddress.getByName(ip), host.getPort());
				selectorThread.start();
				_selectorThreads.add(selectorThread);
			}
		}
		catch (final Exception e)
		{}
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
		{
			s = "-" + s;
		}
		_log.info(s);
	}

	public GameServerListenerList getListeners()
	{
		return _listeners;
	}
	
	public <T extends Listener<GameServer>> boolean addListener(T listener)
	{
		return _listeners.add(listener);
	}
	
	public <T extends Listener<GameServer>> boolean removeListener(T listener)
	{
		return _listeners.remove(listener);
	}
	
	public static GameServer getInstance()
	{
		return _instance;
	}
	
	public int getOnlineLimit()
	{
		return Functions.isValidKey(Config.USER_KEY) ? Config.MAXIMUM_ONLINE_USERS : 10;
	}
}