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
package gameserver.model.entity.events.model;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import l2e.commons.log.LoggerObject;
import l2e.commons.util.Rnd;
import gameserver.Announcements;
import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.data.htm.HtmCache;
import gameserver.data.parser.FightEventParser;
import gameserver.handler.communityhandlers.CommunityBoardHandler;
import gameserver.handler.communityhandlers.ICommunityBoardHandler;
import gameserver.instancemanager.DoubleSessionManager;
import gameserver.listener.player.OnAnswerListener;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;
import gameserver.model.base.ClassId;
import gameserver.model.entity.events.AbstractFightEvent;
import gameserver.model.entity.events.model.template.FightEventGameRoom;
import gameserver.model.olympiad.OlympiadManager;
import gameserver.model.strings.server.ServerMessage;
import gameserver.model.zone.ZoneId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.CreatureSay;
import gameserver.network.serverpackets.ShowTutorialMark;
import gameserver.network.serverpackets.TutorialCloseHtml;
import gameserver.network.serverpackets.TutorialShowHtml;
import gameserver.taskmanager.EventTaskManager;
import gameserver.utils.Util;

/**
 * Created by LordWinter
 */
public class FightEventManager extends LoggerObject
{
	public enum CLASSES
	{
		FIGHTERS(0, ClassId.FIGHTER, ClassId.WARRIOR, ClassId.GLADIATOR, ClassId.WARLORD, ClassId.KNIGHT, ClassId.ROGUE, ClassId.ELVEN_FIGHTER, ClassId.ELVEN_KNIGHT, ClassId.ELVEN_SCOUT, ClassId.DARK_FIGHTER, ClassId.PALUS_KNIGHT, ClassId.ASSASSIN, ClassId.ORC_FIGHTER, ClassId.ORC_RAIDER, ClassId.DESTROYER, ClassId.ORC_MONK, ClassId.TYRANT, ClassId.DWARVEN_FIGHTER, ClassId.SCAVENGER, ClassId.BOUNTYHUNTER, ClassId.ARTISAN, ClassId.WARSMITH, ClassId.MALE_SOILDER, ClassId.FEMALE_SOILDER, ClassId.TROOPER, ClassId.WARDER, ClassId.BERSERKER, ClassId.MALE_SOULBREAKER, ClassId.FEMALE_SOULBREAKER, ClassId.INSPECTOR, ClassId.DUELIST, ClassId.DREADNOUGHT, ClassId.TITAN, ClassId.GRANDKHAVATARI, ClassId.MAESTRO, ClassId.DOOMBRINGER, ClassId.MALE_SOULHOUND, ClassId.FEMALE_SOULHOUND), TANKS(1, ClassId.PALADIN, ClassId.DARKAVENGER, ClassId.TEMPLE_KNIGHT, ClassId.SHILLEN_KNIGHT, ClassId.PHOENIX_KNIGHT, ClassId.HELL_KNIGHT, ClassId.EVA_TEMPLAR, ClassId.SHILLEN_TEMPLAR, ClassId.TRICKSTER), ARCHERS(2, ClassId.HAWKEYE, ClassId.SILVERRANGER, ClassId.PHANTOMRANGER, ClassId.ARBALESTER, ClassId.SAGITTARIUS, ClassId.MOONLIGHTSENTINEL, ClassId.GHOSTSENTINEL), DAGGERS(3, ClassId.TREASUREHUNTER, ClassId.PLAINSWALKER, ClassId.ABYSSWALKER, ClassId.ADVENTURER, ClassId.WINDRIDER, ClassId.GHOSTHUNTER, ClassId.FORTUNE_SEEKER), MAGES(4, ClassId.MAGE, ClassId.WIZARD, ClassId.SORCEROR, ClassId.NECROMANCER, ClassId.ELVEN_MAGE, ClassId.ELVEN_WIZARD, ClassId.SPELLSINGER, ClassId.DARK_MAGE, ClassId.DARK_WIZARD, ClassId.SPELLHOWLER, ClassId.ORC_MAGE, ClassId.ORC_SHAMAN, ClassId.ARCHMAGE, ClassId.SOULTAKER, ClassId.MYSTICMUSE, ClassId.STORMSCREAMER), SUMMONERS(5, ClassId.WARLOCK, ClassId.ELEMENTAL_SUMMONER, ClassId.PHANTOM_SUMMONER, ClassId.ARCANALORD, ClassId.ELEMENTAL_MASTER, ClassId.SPECTRAL_MASTER), HEALERS(6, ClassId.BISHOP, ClassId.ELDER, ClassId.SHILLEN_ELDER, ClassId.CARDINAL, ClassId.EVA_SAINT, ClassId.SHILLEN_SAINT, ClassId.DOMINATOR), SUPPORTS(7, ClassId.CLERIC, ClassId.PROPHET, ClassId.SWORDSINGER, ClassId.ORACLE, ClassId.BLADEDANCER, ClassId.SHILLEN_ORACLE, ClassId.OVERLORD, ClassId.WARCRYER, ClassId.HIEROPHANT, ClassId.SWORDMUSE, ClassId.SPECTRAL_DANCER, ClassId.DOOMCRYER, ClassId.JUDICATOR);

		private final int _transformIndex;
		private final ClassId[] _classes;
		
		private CLASSES(int transformId, ClassId... ids)
		{
			_transformIndex = transformId;
			_classes = ids;
		}

		public ClassId[] getClasses()
		{
			return _classes;
		}

		public int getTransformIndex()
		{
			return _transformIndex;
		}
	}

	public static final String BYPASS = "_fightEvent";

	private final Map<Integer, AbstractFightEvent> _activeEvents = new ConcurrentHashMap<>();
	private final Map<Integer, AbstractFightEvent> _activeGlobalEvents = new ConcurrentHashMap<>();
	private final Map<Integer, ScheduledFuture<?>> _eventTasks = new ConcurrentHashMap<>();
	private final List<FightEventGameRoom> _rooms = new CopyOnWriteArrayList<>();
	private AbstractFightEvent _nextEvent = null;

	public FightEventManager()
	{
		startAutoEventsTasks();
	}
	
	public void reload()
	{
		startAutoEventsTasks();
	}

	public void signForEvent(Player player, AbstractFightEvent event)
	{
		if (!event.isGlobal())
		{
			var room = getEventRooms(event);
			if (room != null && room.getSlotsLeft() <= 0)
			{
				player.sendMessage(new ServerMessage("FightEvents.EVENT_FULL", player.getLang()).toString());
				return;
			}
			
			if (room == null)
			{
				addEventToList(event);
				room = createRoom(event, true);
			}
			
			room.addAlonePlayer(player);
			
			final var msg = new ServerMessage("FightEvents.JUST_PARTICIPATE", player.getLang());
			msg.add(player.getEventName(event.getId()));
			player.sendMessage(msg.toString());
		}
		else
		{
			event.addToGlobalEvent(player);
		}
	}

	public void trySignForEvent(Player player, AbstractFightEvent event, boolean checkConditions)
	{
		if (checkConditions && !canPlayerParticipate(player, event, true, false, true))
		{
			return;
		}

		if (!isRegistrationOpened(event))
		{
			final var msg = new ServerMessage("FightEvents.CANT_PARTICIPATE", player.getLang());
			msg.add(player.getEventName(event.getId()));
			player.sendMessage(msg.toString());
		}
		else if (isPlayerRegistered(player, event.getId()))
		{
			player.sendMessage(new ServerMessage("FightEvents.ALREADY_REG", player.getLang()).toString());
		}
		else if ((Config.DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS > 0) && !DoubleSessionManager.getInstance().tryAddPlayer(event.getId(), player, Config.DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS))
		{
			final var msg = new ServerMessage("FightEvents.MAX_IP", player.getLang());
			msg.add(Config.DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS);
			player.sendMessage(msg.toString());
		}
		else
		{
			if (event.isGlobal() && (event.getEventRoom().getPlayersCount() >= event.getEventRoom().getMaxPlayers()))
			{
				player.sendMessage(new ServerMessage("FightEvents.EVENT_FULL", player.getLang()).toString());
				return;
			}
			signForEvent(player, event);
		}
	}

	public void unsignFromEvent(Player player, int eventId)
	{
		for (final var room : _rooms)
		{
			if (room != null && room.containsPlayer(player) && room.getGame().getId() == eventId)
			{
				room.leaveRoom(player);
				if (Config.DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS > 0)
				{
					DoubleSessionManager.getInstance().removePlayer(room.getGame().getId(), player);
				}
				player.sendMessage(new ServerMessage("FightEvents.UNREGISTER", player.getLang()).toString());
			}
		}
	}

	public void unsignFromAllEvents(Player player)
	{
		for (final var room : _rooms)
		{
			if (room != null && room.containsPlayer(player))
			{
				room.leaveRoom(player);
				if (Config.DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS > 0)
				{
					DoubleSessionManager.getInstance().removePlayer(room.getGame().getId(), player);
				}
				player.sendMessage(new ServerMessage("FightEvents.UNREGISTER", player.getLang()).toString());
			}
		}
	}

	public boolean isRegistrationOpened(AbstractFightEvent event)
	{
		if (event != null && event.isGlobal() && event.isInProgress())
		{
			return true;
		}
		
		for (final var room : _rooms)
		{
			if (room.getGame() != null && room.getGame().getId() == event.getId())
			{
				return true;
			}
		}
		return false;
	}

	public boolean isPlayerRegistered(Player player, int eventId)
	{
		if (player == null)
		{
			return false;
		}

		if (player.isInFightEvent())
		{
			return true;
		}

		for (final var room : _rooms)
		{
			if (room != null && room.getGame().getId() == eventId)
			{
				for (final var iPlayer : room.getAllPlayers())
				{
					if (iPlayer.equals(player))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public void startEventCountdown(AbstractFightEvent event)
	{
		if (!Config.ALLOW_FIGHT_EVENTS || getEventById(event.getId()) != null)
		{
			return;
		}
		
		if (FightEventParser.getInstance().getDisabledEvents().contains(event.getId()))
		{
			return;
		}

		if (!event.isGlobal())
		{
			_nextEvent = event;
		}
		
		EventTaskManager.getInstance().removeEventTask(event, true);

		addEventToList(event);
		final var room = createRoom(event, !event.isGlobal());

		DoubleSessionManager.getInstance().registerEvent(event.getId());

		if (Config.ALLOW_REG_CONFIRM_DLG)
		{
			sendEventInvitations(event);
		}
		if (event.isGlobal())
		{
			sendToAllMsg(event, "FightEvents.STARTED");
			event.prepareEvent(room, room.getAllPlayers(), true);
			FightEventNpcManager.getInstance().tryGlobalSpawnRegNpc();
		}
		else
		{
			sendToAllMsg(event, "FightEvents.OPEN_REG");
			ServerMessage message = null;
			switch (Config.FIGHT_EVENTS_REG_TIME)
			{
				case 10 :
				case 9 :
				case 8 :
				case 7 :
				case 6 :
				case 5 :
					message = new ServerMessage("FightEvents.LAST_5MIN", true);
					break;
				case 4 :
				case 3 :
				case 2 :
					message = new ServerMessage("FightEvents.LAST_3MIN", true);
					break;
				case 1 :
					message = new ServerMessage("FightEvents.LAST_1MIN", true);
					break;
			}
			if (message != null)
			{
				for (final String lang : Config.MULTILANG_ALLOWED)
				{
					if (lang != null)
					{
						message.add(lang, event.getName(lang));
						message.add(lang, Config.FIGHT_EVENTS_REG_TIME);
					}
				}
				Announcements.getInstance().announceToAll(message);
			}
			FightEventNpcManager.getInstance().trySpawnRegNpc();
			setEventTask(event, (Config.FIGHT_EVENTS_REG_TIME * 60000L));
		}
	}

	public void setEventTask(AbstractFightEvent event, long time)
	{
		var task = _eventTasks.get(event.getId());
		if (task != null && !task.isDone())
		{
			task.cancel(false);
			task = null;
		}
		task = ThreadPoolManager.getInstance().schedule(new EventTask(event), time);
		_eventTasks.put(event.getId(), task);
	}
	
	private class EventTask implements Runnable
	{
		AbstractFightEvent _event;
		
		private EventTask(AbstractFightEvent event)
		{
			_event = event;
		}
		
		@Override
		public void run()
		{
			startEvent(_event);
		}
	}

	private void startEvent(AbstractFightEvent event)
	{
		final var room = getEventRooms(event);
		if (room == null)
		{
			return;
		}
		
		clearEventIdTask(event.getId());

		_rooms.remove(room);
		FightEventNpcManager.getInstance().tryUnspawnRegNpc();
		if (room.getPlayersCount() < 2)
		{
			info(event.getName(null) + ": Removing room because it doesnt have enough players");
			info(event.getName(null) + ": Player Counts: " + room.getPlayersCount());
			sendToAllMsg(event, "FightEvents.CANCEL");
			room.getAllPlayers().stream().filter(p -> p != null).forEach(pl -> room.leaveRoom(pl));
			event.destroyMe();
			return;
		}
		sendToAllMsg(event, "FightEvents.STARTED");
		room.getGame().prepareEvent(room, room.getAllPlayers(), true);
	}

	public FightEventGameRoom getEventRooms(AbstractFightEvent event)
	{
		for (final var room : _rooms)
		{
			if (room.getGame() != null && room.getGame().getId() == event.getId())
			{
				return room;
			}
		}
		return null;
	}

	public void sendEventInvitations(AbstractFightEvent event)
	{
		for (final var player : GameObjectsStorage.getPlayers())
		{
			if (canPlayerParticipate(player, event, false, true, true) && (player.getEvent(AbstractFightEvent.class) == null))
			{
				final var msg = new ServerMessage("FightEvents.WANT_JOIN", player.getLang());
				msg.add(player.getEventName(event.getId()));
				player.sendConfirmDlg(new AnswerEventInvitation(player, event), 60000, msg.toString());
			}
		}
	}
	
	private class AnswerEventInvitation implements OnAnswerListener
	{
		private final Player _player;
		private final AbstractFightEvent _event;

		private AnswerEventInvitation(Player player, AbstractFightEvent event)
		{
			_player = player;
			_event = event;
		}

		@Override
		public void sayYes()
		{
			trySignForEvent(_player, _event, false);
		}

		@Override
		public void sayNo()
		{
		}
	}

	public FightEventGameRoom createRoom(AbstractFightEvent event, boolean toAdd)
	{
		final var newRoom = new FightEventGameRoom(event);
		if (toAdd)
		{
			_rooms.add(newRoom);
		}
		return newRoom;
	}

	public AbstractFightEvent getNextEvent()
	{
		return _nextEvent;
	}

	private void sendErrorMessageToPlayer(Player player, String msg)
	{
		player.sendPacket(new CreatureSay(player.getObjectId(), Say2.PARTYROOM_COMMANDER, new ServerMessage("FightEvents.ERROR", player.getLang()).toString(), msg));
		player.sendMessage(msg);
	}

	public void sendToAllMsg(AbstractFightEvent event, String msg)
	{
		for (final var player : GameObjectsStorage.getPlayers())
		{
			if (player == null || player.isInOfflineMode() || player.isInFightEvent())
			{
				continue;
			}
			
			final var message = new ServerMessage(msg, player.getLang());
			message.add(player.getEventName(event.getId()));
			player.sendPacket(new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, player.getEventName(event.getId()), message.toString()));
		}
	}

	private void addEventToList(AbstractFightEvent event)
	{
		if (event.isGlobal())
		{
			_activeGlobalEvents.put(event.getId(), event);
		}
		else
		{
			_activeEvents.put(event.getId(), event);
		}
	}

	private void startAutoEventsTasks()
	{
		AbstractFightEvent closestEvent = null;
		long closestEventTime = Long.MAX_VALUE;

		for (final var event : FightEventParser.getInstance().getEvents().valueCollection())
		{
			if (event != null && event.isAutoTimed() && !FightEventParser.getInstance().getDisabledEvents().contains(event.getId()))
			{
				long nextEventTime = 0L;
				if (event.getAutoStartTimes() != null)
				{
					nextEventTime = event.getAutoStartTimes().next(System.currentTimeMillis());
				}
				else
				{
					nextEventTime = (System.currentTimeMillis() + (Rnd.get(1, 5) * 60000L));
				}
				EventTaskManager.getInstance().addEventTask(event, nextEventTime, false);
				if (closestEventTime > nextEventTime)
				{
					closestEvent = event;
					closestEventTime = nextEventTime;
				}
			}
		}
		_nextEvent = closestEvent;
		EventTaskManager.getInstance().recalcTime();
	}
	
	public void calcNewEventTime(AbstractFightEvent event)
	{
		if (!event.isAutoTimed())
		{
			return;
		}
		
		if (!event.isWithoutTime())
		{
			long nextEventTime = 0L;
			if (event.getAutoStartTimes() != null)
			{
				nextEventTime = event.getAutoStartTimes().next(System.currentTimeMillis());
			}
			else
			{
				nextEventTime = (System.currentTimeMillis() + (Rnd.get(1, 5) * 60000L));
			}
			EventTaskManager.getInstance().addEventTask(event, nextEventTime, true);
		}
	}

	public boolean canPlayerParticipate(Player player, AbstractFightEvent event, boolean sendMessage, boolean justMostImportant, boolean checkReflection)
	{
		if (player == null)
		{
			return false;
		}
		
		if (event.getValidLevels() != null)
		{
			if (player.getLevel() < event.getValidLevels()[0] || player.getLevel() > event.getValidLevels()[1])
			{
				final ServerMessage msg = new ServerMessage("FightEvents.WRONG_LEVEL", player.getLang());
				sendErrorMessageToPlayer(player, msg.toString());
				return false;
			}
		}
		
		if (event.getValidProffs() != null)
		{
			if (!event.getValidProffs().contains(player.getClassId().level()))
			{
				final ServerMessage msg = new ServerMessage("FightEvents.WRONG_PROFF", player.getLang());
				sendErrorMessageToPlayer(player, msg.toString());
				return false;
			}
		}
		
		if (event.getExcludedClasses() != null)
		{
			final var clazz = FightEventGameRoom.getPlayerClassGroup(player);
			if (clazz != null)
			{
				final var classes = event.getExcludedClasses();
				for (int i = 0; i < event.getExcludedClasses().length; i++)
				{
					if (classes[i].name().equals(clazz.name()))
					{
						final ServerMessage msg = new ServerMessage("FightEvents.WRONG_PROFF", player.getLang());
						sendErrorMessageToPlayer(player, msg.toString());
						return false;
					}
				}
			}
			else
			{
				final var msg = new ServerMessage("FightEvents.WRONG_PROFF", player.getLang());
				sendErrorMessageToPlayer(player, msg.toString());
				return false;
			}
		}
		
		if (player.isDead() || player.isAlikeDead())
		{
			sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_DEAD", player.getLang()).toString());
			return false;
		}

		if (player.isBlocked() || player.isInKrateisCube() || player.getUCState() > 0)
		{
			return false;
		}

		if (player.getCursedWeaponEquippedId() > 0)
		{
			if (sendMessage)
			{
				sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_CURSE", player.getLang()).toString());
			}
			return false;
		}

		if (OlympiadManager.getInstance().isRegistered(player))
		{
			if (sendMessage)
			{
				sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_OLY", player.getLang()).toString());
			}
			return false;
		}

		if (player.isInOlympiadMode())
		{
			if (sendMessage)
			{
				sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_OLY1", player.getLang()).toString());
			}
			return false;
		}

		if (player.inObserverMode())
		{
			if (sendMessage)
			{
				sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_OBSERVE", player.getLang()).toString());
			}
			return false;
		}

		if (player.isJailed())
		{
			if (sendMessage)
			{
				sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_JAIL", player.getLang()).toString());
			}
			return false;
		}

		if (player.isInOfflineMode())
		{
			if (sendMessage)
			{
				sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_OFFLINE", player.getLang()).toString());
			}
			return false;
		}

		if (player.isInStoreMode())
		{
			if (sendMessage)
			{
				sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_STORE", player.getLang()).toString());
			}
			return false;
		}

		if (!player.getReflection().isDefault() && checkReflection)
		{
			if (sendMessage)
			{
				sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_REF", player.getLang()).toString());
			}
			return false;
		}

		if (player.isInDuel())
		{
			if (sendMessage)
			{
				sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_DUEL", player.getLang()).toString());
			}
			return false;
		}

		if (!justMostImportant)
		{
			if (player.isDead() || player.isAlikeDead())
			{
				if (sendMessage)
				{
					sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_DEAD", player.getLang()).toString());
				}
				return false;
			}

			if (!player.isInsideZone(ZoneId.PEACE) && player.getPvpFlag() > 0)
			{
				if (sendMessage)
				{
					sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_PVP", player.getLang()).toString());
				}
				return false;
			}

			if (player.isInCombat())
			{
				if (sendMessage)
				{
					sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_COMBAT", player.getLang()).toString());
				}
				return false;
			}

			if (player.getKarma() > 0)
			{
				if (sendMessage)
				{
					sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_PK", player.getLang()).toString());
				}
				return false;
			}
		}
		return true;
	}

	public void requestEventPlayerMenuBypass(Player player, String bypass)
	{
		player.sendPacket(TutorialCloseHtml.STATIC_PACKET);

		final var event = player.getFightEvent();
		if (event == null)
		{
			return;
		}

		final var fPlayer = event.getFightEventPlayer(player);
		if (fPlayer == null)
		{
			return;
		}

		fPlayer.setShowTutorial(false);

		if (!bypass.startsWith(BYPASS))
		{
			return;
		}

		final var st = new StringTokenizer(bypass, " ");
		st.nextToken();

		final String action = st.nextToken();

		switch (action)
		{
			case "leave" :
				askQuestion(player, new ServerMessage("FightEvents.WANT_TO_LEAVE", player.getLang()).toString());
				break;
			case "buffer" :
				final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler("_bbsbuffer");
				if (handler != null)
				{
					handler.onBypassCommand("_bbsbuffer", player);
				}
				break;
			case "set" :
				final String set = st.nextToken();
				if (event.isUseEventItems() && Util.isDigit(set))
				{
					event.selectPlayerEventSet(fPlayer, Integer.parseInt(set));
				}
				break;
		}
	}
	
	public void sendEventSetMenu(Player player)
	{
		final var event = player.getFightEvent();
		if (event == null || event.getFightEventPlayer(player) == null || !event.isUseEventItems())
		{
			return;
		}
		
		final var fPlayer = event.getFightEventPlayer(player);
		
		fPlayer.setShowTutorial(true);
		
		String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/events/fightevents/setIndex.htm");
		final String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/events/fightevents/setTemplate.htm");
		String block = "";
		String list = "";
		for (final var t : event.getEventSets().values())
		{
			block = template;
			block = block.replace("%name%", t.getName(player.getLang()));
			block = block.replace("%bypass%", "bypass -h _fightEvent set " + t.getId() + "");
			list += block;
		}
		html = html.replace("%event%", player.getEventName(event.getId()));
		html = html.replace("%list%", list);
		player.sendPacket(new TutorialShowHtml(html));
	}

	public void sendEventPlayerMenu(Player player)
	{
		final var event = player.getFightEvent();
		if (event == null || event.getFightEventPlayer(player) == null)
		{
			return;
		}

		final var fPlayer = event.getFightEventPlayer(player);

		fPlayer.setShowTutorial(true);
		String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/events/fightevents/index.htm");
		final String buffer = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/events/fightevents/buffer.htm");
		html = html.replace("%event%", player.getEventName(event.getId()));
		html = html.replace("%descr%", player.getEventDescr(event.getId()));
		if (event.canUseCommunity())
		{
			html = html.replace("%buffer%", buffer);
		}
		else
		{
			html = html.replace("%buffer%", "");
		}
		player.sendPacket(new TutorialShowHtml(html));
		player.sendPacket(new ShowTutorialMark(100, 0));
	}

	private void leaveEvent(Player player)
	{
		final var event = player.getFightEvent();
		if (event == null)
		{
			return;
		}

		if (event.kickFromEvent(player, true))
		{
			player.sendMessage(new ServerMessage("FightEvents.LEFT_EVENT", player.getLang()).toString());
		}
	}

	private void askQuestion(Player player, String question)
	{
		player.sendConfirmDlg(new AskQuestionAnswerListener(player), 0, question);
	}

	private class AskQuestionAnswerListener implements OnAnswerListener
	{
		private final Player _player;

		private AskQuestionAnswerListener(Player player)
		{
			_player = player;
		}

		@Override
		public void sayYes()
		{
			leaveEvent(_player);
		}

		@Override
		public void sayNo()
		{
		}

	}

	public AbstractFightEvent getEventById(int id)
	{
		if (_activeGlobalEvents.containsKey(id))
		{
			return _activeGlobalEvents.get(id);
		}
		return _activeEvents.get(id);
	}

	public Map<Integer, AbstractFightEvent> getActiveEvents()
	{
		return _activeEvents;
	}
	
	public AbstractFightEvent getGlobalEventById(int id)
	{
		return _activeGlobalEvents.get(id);
	}
	
	public Map<Integer, AbstractFightEvent> getGlobalActiveEvents()
	{
		return _activeGlobalEvents;
	}
	
	public boolean getActiveEventTask(int id)
	{
		return _eventTasks.get(id) != null;
	}
	
	public void clearEventIdTask(int eventId)
	{
		var task = _eventTasks.get(eventId);
		if (task != null && !task.isDone())
		{
			_eventTasks.remove(eventId);
			task.cancel(false);
			task = null;
		}
	}

	public void cleanEventId(int eventId)
	{
		for (final var room : _rooms)
		{
			if (room.getGame() != null && room.getGame().getId() == eventId)
			{
				room.cleanUp();
				_rooms.remove(room);
			}
		}
	}

	public void prepareStartEventId(int eventId)
	{
		clearEventIdTask(eventId);
		final var event = getEventById(eventId);
		if (event != null)
		{
			startEvent(event);
		}
	}

	public void removeEventId(int eventId)
	{
		if (_activeEvents.containsKey(eventId))
		{
			_activeEvents.remove(eventId);
		}
		
		if (_activeGlobalEvents.containsKey(eventId))
		{
			_activeGlobalEvents.remove(eventId);
		}
	}

	public static final FightEventManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FightEventManager _instance = new FightEventManager();
	}
}