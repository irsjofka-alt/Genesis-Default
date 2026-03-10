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
package gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import l2e.commons.util.Rnd;
import gameserver.ThreadPoolManager;
import gameserver.ai.character.CharacterAI;
import gameserver.ai.character.DoorAI;
import gameserver.data.parser.DoorParser;
import gameserver.instancemanager.CastleManager;
import gameserver.instancemanager.ClanHallManager;
import gameserver.instancemanager.FortManager;
import gameserver.instancemanager.ReflectionManager;
import gameserver.instancemanager.TerritoryWarManager;
import gameserver.model.Clan;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Playable;
import gameserver.model.actor.Player;
import gameserver.model.actor.status.DoorStatus;
import gameserver.model.actor.templates.door.DoorTemplate;
import gameserver.model.actor.templates.items.Weapon;
import gameserver.model.entity.Castle;
import gameserver.model.entity.ClanHall;
import gameserver.model.entity.Fort;
import gameserver.model.entity.clanhall.SiegableHall;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.skills.Skill;
import gameserver.model.stats.StatsSet;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.EventTrigger;
import gameserver.network.serverpackets.StaticObject;
import gameserver.network.serverpackets.SystemMessage;

public class DoorInstance extends Creature
{
	private static final byte OPEN_BY_CLICK = 1;
	private static final byte OPEN_BY_TIME = 2;
	private static final byte OPEN_BY_ITEM = 4;
	private static final byte OPEN_BY_SKILL = 8;
	private static final byte OPEN_BY_CYCLE = 16;
	
	private int _castleIndex = -2;
	private int _fortIndex = -2;
	private ClanHall _clanHall;
	private boolean _open = false;
	private boolean _isTargetable;
	private final boolean _checkCollision;
	private int _openType = 0;
	private int _meshindex = 1;
	private int _level = 0;
	protected int _closeTime = -1;
	protected int _openTime = -1;
	protected int _randomTime = -1;
	private Future<?> _autoCloseTask;
	private boolean _isAttackableDoor = false;
	
	public DoorInstance(int objectId, DoorTemplate template, StatsSet data)
	{
		super(objectId, template);

		setInstanceType(InstanceType.DoorInstance);
		_ai = new DoorAI(this);
		setIsInvul(false);
		
		_castleIndex = template._castleId;
		_fortIndex = template._fortId;
		_isTargetable = data.getBool("targetable", true);
		if (getGroupName() != null)
		{
			DoorParser.addDoorGroup(getGroupName(), getDoorId());
		}
		if (data.getString("default_status", "close").equals("open"))
		{
			_open = true;
		}
		_closeTime = data.getInteger("close_time", -1);
		_level = data.getInteger("level", 0);
		_openType = data.getInteger("open_method", 0);
		_checkCollision = data.getBool("check_collision", true);
		if (isOpenableByTime())
		{
			_openTime = data.getInteger("open_time");
			_randomTime = data.getInteger("random_time", -1);
			startTimerOpen();
		}
		final int clanhallId = data.getInteger("clanhall_id", 0);
		if (clanhallId > 0)
		{
			final ClanHall hall = ClanHallManager.getInstance().getAllClanHalls().get(clanhallId);
			if (hall != null)
			{
				setClanHall(hall);
				hall.getDoors().add(this);
			}
		}
	}
	
	@Override
	public void moveToLocation(int x, int y, int z, int offset, boolean wasdMove)
	{
	}
	
	@Override
	public void stopMove(Location loc)
	{
	}

	@Override
	public void doAttack(Creature target)
	{
	}

	@Override
	public void doCast(Skill skill)
	{
	}

	@Override
	public CharacterAI getAI()
	{
		return _ai;
	}

	private void startTimerOpen()
	{
		int delay = _open ? _openTime : _closeTime;
		if (_randomTime > 0)
		{
			delay += Rnd.get(_randomTime);
		}
		ThreadPoolManager.getInstance().schedule(new TimerOpen(), delay * 1000);
	}

	@Override
	public DoorTemplate getTemplate()
	{
		return (DoorTemplate) super.getTemplate();

	}

	@Override
	public final DoorStatus getStatus()
	{
		return (DoorStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new DoorStatus(this));
	}

	public final boolean isOpenableBySkill()
	{
		return (_openType & OPEN_BY_SKILL) != 0;
	}

	public final boolean isOpenableByItem()
	{
		return (_openType & OPEN_BY_ITEM) != 0;
	}

	public final boolean isOpenableByClick()
	{
		return (_openType & OPEN_BY_CLICK) != 0;
	}

	public final boolean isOpenableByTime()
	{
		return (_openType & OPEN_BY_TIME) != 0;
	}

	public final boolean isOpenableByCycle()
	{
		return (_openType & OPEN_BY_CYCLE) != 0;
	}

	@Override
	public final int getLevel()
	{
		return _level;
	}

	@Override
	public int getId()
	{
		return getTemplate().getId();
	}

	public int getDoorId()
	{
		return getTemplate().doorId;
	}

	public boolean getOpen()
	{
		return _open;
	}
	
	public boolean isOpened()
	{
		return _open;
	}
	
	public boolean isClosed()
	{
		return !_open;
	}

	public void setOpen(boolean open)
	{
		_open = open;
		if (getChildId() > 0)
		{
			final DoorInstance sibling = getSiblingDoor(getChildId());
			if (sibling != null)
			{
				sibling.notifyChildEvent(open);
			}
			else
			{
				_log.warn(getClass().getSimpleName() + ": cannot find child id: " + getChildId());
			}
		}
	}

	public boolean getIsShowHp()
	{
		return getTemplate().showHp;
	}

	public int getDamage()
	{
		final int dmg = 6 - (int) Math.ceil(getCurrentHpRatio() * 6);
		return Math.max(0, Math.min(6, dmg));
	}

	public final Castle getCastle()
	{
		if (_castleIndex < 0)
		{
			_castleIndex = CastleManager.getInstance().getCastleIndex(this);
		}
		if (_castleIndex < 0)
		{
			return null;
		}
		return CastleManager.getInstance().getCastles().get(_castleIndex);
	}

	public final Fort getFort()
	{
		if (_fortIndex < 0)
		{
			_fortIndex = FortManager.getInstance().getFortIndex(this);
		}
		if (_fortIndex < 0)
		{
			return null;
		}
		return FortManager.getInstance().getForts().get(_fortIndex);
	}

	public void setClanHall(ClanHall clanhall)
	{
		_clanHall = clanhall;
	}

	public ClanHall getClanHall()
	{
		return _clanHall;
	}

	public boolean isEnemy()
	{
		if (isDead() || isOpen())
		{
			return false;
		}
		
		if ((getCastle() != null) && (getCastle().getId() > 0) && getCastle().getZone().isActive() && getIsShowHp())
		{
			return true;
		}
		if ((getFort() != null) && (getFort().getId() > 0) && getFort().getZone().isActive() && getIsShowHp())
		{
			return true;
		}
		if ((getClanHall() != null) && getClanHall().isSiegableHall() && ((SiegableHall) getClanHall()).getSiegeZone().isActive() && getIsShowHp())
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker, boolean isPoleAttack)
	{
		if (!(attacker instanceof Playable))
		{
			return false;
		}

		if (getIsAttackableDoor())
		{
			return true;
		}
		
		if (!getIsShowHp())
		{
			return false;
		}

		final Player actingPlayer = attacker.getActingPlayer();

		if (getClanHall() != null)
		{
			if (!getClanHall().isSiegableHall())
			{
				return false;
			}
			return ((SiegableHall) getClanHall()).isInSiege() && ((SiegableHall) getClanHall()).getSiege().doorIsAutoAttackable() && ((SiegableHall) getClanHall()).getSiege().checkIsAttacker(actingPlayer.getClan());
		}
		final boolean isCastle = ((getCastle() != null) && (getCastle().getId() > 0) && getCastle().getZone().isActive());
		final boolean isFort = ((getFort() != null) && (getFort().getId() > 0) && getFort().getZone().isActive());
		final int activeSiegeId = (getFort() != null ? getFort().getId() : (getCastle() != null ? getCastle().getId() : 0));

		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, activeSiegeId))
			{
				return false;
			}
			return true;
		}
		else if (isFort)
		{
			final Clan clan = actingPlayer.getClan();
			if ((clan != null) && (clan == getFort().getOwnerClan()) || !getFort().getSiege().checkIsAttacker(clan))
			{
				return false;
			}
		}
		else if (isCastle)
		{
			final Clan clan = actingPlayer.getClan();
			if ((clan != null) && (clan.getId() == getCastle().getOwnerId()) || !getCastle().getSiege().checkIsAttacker(clan))
			{
				return false;
			}
		}
		return (isCastle || isFort);
	}

	@Override
	public void updateAbnormalEffect()
	{
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public void broadcastStatusUpdate()
	{
		final var oe = getEmitter() > 0 ? new EventTrigger(getEmitter(), isClosed()) : null;
		if (isInActiveRegion())
		{
			List<Player> players = null;
			try
			{
				players = World.getAroundPlayers(this);
				for (final var player : players)
				{
					if (player != null)
					{
						player.sendPacket(new StaticObject(this, player));
						if (oe != null)
						{
							player.sendPacket(oe);
						}
					}
				}
			}
			finally
			{
				players = null;
			}
		}
	}

	public final void openMe()
	{
		if (getGroupName() != null)
		{
			manageGroupOpen(true, getGroupName());
			return;
		}
		
		if (!isOpen())
		{
			setOpen(true);
			broadcastStatusUpdate();
			startAutoCloseTask();
		}
	}

	public final void closeMe()
	{
		final var oldTask = _autoCloseTask;
		if (oldTask != null)
		{
			oldTask.cancel(false);
			_autoCloseTask = null;
		}
		if (getGroupName() != null)
		{
			manageGroupOpen(false, getGroupName());
			return;
		}
		
		if (isOpen())
		{
			setOpen(false);
			broadcastStatusUpdate();
		}
	}

	private void manageGroupOpen(boolean open, String groupName)
	{
		final var set = DoorParser.getDoorsByGroup(groupName);
		DoorInstance first = null;
		for (final Integer id : set)
		{
			final DoorInstance door = getSiblingDoor(id);
			if (first == null)
			{
				first = door;
			}

			if (door.getOpen() != open)
			{
				door.setOpen(open);
				door.broadcastStatusUpdate();
			}
		}
		if ((first != null) && open)
		{
			first.startAutoCloseTask();
		}
	}

	private void notifyChildEvent(boolean open)
	{
		final byte openThis = open ? getTemplate().masterDoorOpen : getTemplate().masterDoorClose;

		if (openThis == 0)
		{
			return;
		}
		else if (openThis == 1)
		{
			openMe();
		}
		else if (openThis == -1)
		{
			closeMe();
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getTemplate().doorId + "](" + getObjectId() + ")";
	}

	public String getDoorName()
	{
		return getTemplate().name;
	}

	public int getX(int i)
	{
		return getTemplate().nodeX[i];
	}

	public int getY(int i)
	{
		return getTemplate().nodeY[i];
	}

	public int getZMin()
	{
		return getTemplate().nodeZ;
	}

	public int getZMax()
	{
		return getTemplate().nodeZ + getTemplate().height;
	}

	public List<DefenderInstance> getKnownDefenders()
	{
		final List<DefenderInstance> result = new ArrayList<>();
		List<Npc> targets = null;
		try
		{
			targets = World.getAroundNpc(this);
			for (final var obj : targets)
			{
				if (obj instanceof DefenderInstance instance)
				{
					result.add(instance);
				}
			}
		}
		finally
		{
			targets = null;
		}
		return result;
	}

	public void setMeshIndex(int mesh)
	{
		_meshindex = mesh;
	}

	public int getMeshIndex()
	{
		return _meshindex;
	}

	public int getEmitter()
	{
		return getTemplate().emmiter;
	}

	public boolean isWall()
	{
		return getTemplate().isWall;
	}

	public String getGroupName()
	{
		return getTemplate().groupName;
	}

	public int getChildId()
	{
		return getTemplate().childDoorId;
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill)
	{
		if (isWall() && !(attacker instanceof SiegeSummonInstance))
		{
			return;
		}

		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}

	@Override
	public void reduceCurrentHpByDOT(double i, Creature attacker, Skill skill)
	{
	}

	@Override
	protected void onDeath(Creature killer)
	{
		final boolean isFort = ((getFort() != null) && (getFort().getId() > 0) && getFort().getSiege().getIsInProgress());
		final boolean isCastle = ((getCastle() != null) && (getCastle().getId() > 0) && getCastle().getSiege().getIsInProgress());
		final boolean isHall = ((getClanHall() != null) && getClanHall().isSiegableHall() && ((SiegableHall) getClanHall()).isInSiege());

		if (isFort || isCastle || isHall)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTLE_GATE_BROKEN_DOWN));
		}
		super.onDeath(killer);
	}

	@Override
	public void sendInfo(Player activeChar)
	{
		if (isVisibleFor(activeChar))
		{
			if (getEmitter() > 0)
			{
				activeChar.sendPacket(new EventTrigger(this, getOpen()));
			}
			activeChar.sendPacket(new StaticObject(this, activeChar));
		}
	}

	public void setTargetable(boolean b)
	{
		_isTargetable = b;
		broadcastStatusUpdate();
	}

	@Override
	public boolean isTargetable()
	{
		return _isTargetable;
	}

	public boolean checkCollision()
	{
		return _checkCollision;
	}

	private DoorInstance getSiblingDoor(int doorId)
	{
		if (getReflectionId() == 0)
		{
			return DoorParser.getInstance().getDoor(doorId);
		}

		final var inst = ReflectionManager.getInstance().getReflection(getReflectionId());
		if (inst != null)
		{
			return inst.getDoor(doorId);
		}

		return null;
	}

	private void startAutoCloseTask()
	{
		if ((_closeTime < 0) || isOpenableByTime())
		{
			return;
		}
		final var oldTask = _autoCloseTask;
		if (oldTask != null)
		{
			_autoCloseTask = null;
			oldTask.cancel(false);
		}
		_autoCloseTask = ThreadPoolManager.getInstance().schedule(new AutoClose(), _closeTime * 1000);
	}

	class AutoClose implements Runnable
	{
		@Override
		public void run()
		{
			if (getOpen())
			{
				closeMe();
			}
		}
	}

	class TimerOpen implements Runnable
	{
		@Override
		public void run()
		{
			final boolean open = getOpen();
			if (open)
			{
				closeMe();
			}
			else
			{
				openMe();
			}

			int delay = open ? _closeTime : _openTime;
			if (_randomTime > 0)
			{
				delay += Rnd.get(_randomTime);
			}
			ThreadPoolManager.getInstance().schedule(this, delay * 1000);
		}
	}

	@Override
	public boolean isDoor()
	{
		return true;
	}

	public boolean getIsAttackableDoor()
	{
		return _isAttackableDoor;
	}
	
	@Override
	public boolean canBeAttacked()
	{
		return _isAttackableDoor;
	}
	
	public void setIsAttackableDoor(boolean val)
	{
		_isAttackableDoor = val;
	}
	
	public boolean isOpen()
	{
		return _open;
	}
	
	@Override
	public boolean isHealBlocked()
	{
		return true;
	}
	
	@Override
	public boolean isActionsDisabled()
	{
		return true;
	}
}