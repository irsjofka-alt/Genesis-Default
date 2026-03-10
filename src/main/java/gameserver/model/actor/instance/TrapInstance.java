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

import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.data.parser.DamageLimitParser;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.items.Weapon;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.olympiad.OlympiadGameManager;
import gameserver.model.quest.Quest.TrapAction;
import gameserver.model.quest.QuestEventType;
import gameserver.model.skills.Skill;
import gameserver.model.skills.targets.TargetType;
import gameserver.model.zone.ZoneId;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.GameServerPacket;
import gameserver.network.serverpackets.NpcInfo.TrapInfo;
import gameserver.network.serverpackets.SocialAction;
import gameserver.network.serverpackets.SystemMessage;

public final class TrapInstance extends Npc
{
	private boolean _hasLifeTime;
	private boolean _isInArena = false;
	private boolean _isTriggered;
	private final int _lifeTime;
	private Player _owner;
	private final List<Integer> _players = new ArrayList<>();
	private Skill _skill;
	private int _remainingTime;
	private Future<?> _actionTask;

	public TrapInstance(int objectId, NpcTemplate template, Reflection ref, int lifeTime)
	{
		super(objectId, template);
		setInstanceType(InstanceType.TrapInstance);
		setReflection(ref);
		for (final String lang : Config.MULTILANG_ALLOWED)
		{
			if (lang != null)
			{
				setName(lang, template.getName(lang) != null ? template.getName(lang) : template.getName(null));
			}
		}
		setIsInvul(false);
		
		_owner = null;
		_isTriggered = false;
		for (final var skill : template.getSkills().values())
		{
			if ((skill.getId() == 4072) || (skill.getId() == 4186) || (skill.getId() == 5267) || (skill.getId() == 5268) || (skill.getId() == 5269) || (skill.getId() == 5270) || (skill.getId() == 5271) || (skill.getId() == 5340) || (skill.getId() == 5422) || (skill.getId() == 5423) || (skill.getId() == 5424) || (skill.getId() == 5679) || (skill.getId() == 10002))
			{
				_skill = skill;
				break;
			}
		}
		_hasLifeTime = lifeTime >= 0;
		_lifeTime = lifeTime != 0 ? lifeTime : 30000;
		_remainingTime = _lifeTime;
		if (_skill != null)
		{
			_actionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::useSkill, 1000, 1000);
		}
	}
	
	public TrapInstance(int objectId, NpcTemplate template, Player owner, int lifeTime)
	{
		this(objectId, template, owner.getReflection(), lifeTime);
		_owner = owner;
	}
	
	@Override
	public void broadcastPacket(GameServerPacket... packets)
	{
		List<Player> targets = null;
		try
		{
			targets = World.getAroundPlayers(this, 2000, 200);
			targets.stream().filter(p -> p != null && (_isTriggered || canBeSeen(p))).forEach(p -> p.sendPacket(packets));
		}
		finally
		{
			targets = null;
		}
	}
	
	@Override
	public void broadcastPacket(int range, GameServerPacket... packets)
	{
		List<Player> targets = null;
		try
		{
			targets = World.getAroundPlayers(this, range, 200);
			targets.stream().filter(p -> p != null && (_isTriggered || canBeSeen(p))).forEach(p -> p.sendPacket(packets));
		}
		finally
		{
			targets = null;
		}
	}
	
	public boolean canBeSeen(Creature cha)
	{
		if ((cha != null) && _players.contains(cha.getObjectId()))
		{
			return true;
		}
		
		if ((_owner == null) || (cha == null))
		{
			return false;
		}
		if (cha == _owner)
		{
			return true;
		}
		
		final var player = cha.getActingPlayer();
		if (player != null)
		{
			if (player.inObserverMode())
			{
				return false;
			}
			
			if (_owner.isInOlympiadMode() && player.isInOlympiadMode() && player.getOlympiadSide() != _owner.getOlympiadSide())
			{
				return false;
			}
		}
		
		if (_isInArena)
		{
			return true;
		}
		
		final var party = _owner.getParty();
		final var chaParty = cha.getParty();
		if (party != null && chaParty != null && (party.getLeaderObjectId() == chaParty.getLeaderObjectId()))
		{
			return true;
		}
		return false;
	}
	
	public boolean checkTarget(Creature target)
	{
		if (!Skill.checkForAreaOffensiveSkills(this, target, _skill, _isInArena, this))
		{
			return false;
		}
		
		final var player = target.getActingPlayer();
		if (player != null && player.inObserverMode())
		{
			return false;
		}
		
		if ((_owner != null) && _owner.isInOlympiadMode() && player != null)
		{
			if (player.isInOlympiadMode() && (player.getOlympiadSide() == _owner.getOlympiadSide()))
			{
				return false;
			}
		}
		
		if (_isInArena)
		{
			return true;
		}
		
		if (_owner != null)
		{
			if (target instanceof Attackable)
			{
				return true;
			}
			
			if ((player == null) || ((player.getPvpFlag() == 0) && (player.getKarma() == 0)))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void onDelete()
	{
		final var task = _actionTask;
		if (task != null)
		{
			task.cancel(true);
		}
		_actionTask = null;
		
		if (_owner != null)
		{
			_owner.setTrap(null);
			_owner = null;
		}
		_players.clear();
		super.onDelete();
	}
	
	@Override
	public Player getActingPlayer()
	{
		return _owner;
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	public int getKarma()
	{
		return _owner != null ? _owner.getKarma() : 0;
	}
	
	public Player getOwner()
	{
		return _owner;
	}
	
	public byte getPvpFlag()
	{
		return _owner != null ? _owner.getPvpFlag() : 0;
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
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker, boolean isPoleAttack)
	{
		return !canBeSeen(attacker);
	}
	
	@Override
	public boolean isTrap()
	{
		return true;
	}
	
	public boolean isTriggered()
	{
		return _isTriggered;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_isInArena = isInsideZone(ZoneId.PVP) && !isInsideZone(ZoneId.SIEGE);
		_players.clear();
	}
	
	@Override
	public void sendDamageMessage(Creature target, int damage, Skill skill, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss || (_owner == null))
		{
			return;
		}
		
		if (_owner.isInOlympiadMode() && (target.isPlayer()) && ((Player) target).isInOlympiadMode() && (((Player) target).getOlympiadGameId() == _owner.getOlympiadGameId()))
		{
			OlympiadGameManager.getInstance().notifyCompetitorDamage(target.getActingPlayer(), damage);
		}
		
		if (Config.ALLOW_DAMAGE_LIMIT && target.isNpc())
		{
			final var limit = DamageLimitParser.getInstance().getDamageLimit(target.getId());
			if (limit != null)
			{
				final int damageLimit = skill != null ? skill.isMagic() ? limit.getMagicDamage() : limit.getPhysicDamage() : limit.getDamage();
				if (damageLimit > 0 && damage > damageLimit)
				{
					damage = damageLimit;
				}
			}
		}
		
		if (target.isInvul() && !(target instanceof NpcInstance))
		{
			_owner.sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
		}
		else
		{
			final var sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DONE_S3_DAMAGE_TO_C2);
			sm.addCharName(this);
			sm.addCharName(target);
			sm.addNumber(damage);
			_owner.sendPacket(sm);
		}
	}
	
	@Override
	public void sendInfo(Player activeChar)
	{
		if (_isTriggered || canBeSeen(activeChar))
		{
			activeChar.sendPacket(new TrapInfo(this, activeChar));
		}
	}
	
	public void setDetected(Creature detector)
	{
		if (_isInArena)
		{
			if (detector.isPlayable())
			{
				sendInfo(detector.getActingPlayer());
			}
			return;
		}
		
		if ((_owner != null) && (_owner.getPvpFlag() == 0) && (_owner.getKarma() == 0))
		{
			return;
		}
		
		_players.add(detector.getObjectId());
		
		if (getTemplate().getEventQuests(QuestEventType.ON_TRAP_ACTION) != null)
		{
			for (final var quest : getTemplate().getEventQuests(QuestEventType.ON_TRAP_ACTION))
			{
				quest.notifyTrapAction(this, detector, TrapAction.TRAP_DETECTED);
			}
		}
		if (detector.isPlayable())
		{
			sendInfo(detector.getActingPlayer());
		}
	}
	
	public void triggerTrap(Creature target)
	{
		final var task = _actionTask;
		if (task != null)
		{
			task.cancel(true);
		}
		_actionTask = null;
		
		_isTriggered = true;
		broadcastPacket(new TrapInfo(this, null));
		setTarget(target);
		
		if (getTemplate().getEventQuests(QuestEventType.ON_TRAP_ACTION) != null)
		{
			for (final var quest : getTemplate().getEventQuests(QuestEventType.ON_TRAP_ACTION))
			{
				quest.notifyTrapAction(this, target, TrapAction.TRAP_TRIGGERED);
			}
		}
		doCast(_skill);
		_actionTask = ThreadPoolManager.getInstance().schedule(() -> unSummon(), _skill.getHitTime() + 300);
	}
	
	public void unSummon()
	{
		final var task = _actionTask;
		if (task != null)
		{
			task.cancel(true);
		}
		_actionTask = null;
		
		if (_owner != null)
		{
			_owner.setTrap(null);
			_owner = null;
		}
		
		if (isVisible() && !isDead())
		{
			deleteMe();
		}
	}
	
	@Override
	public void updateAbnormalEffect()
	{
	}
	
	public boolean hasLifeTime()
	{
		return _hasLifeTime;
	}
	
	public void setHasLifeTime(boolean val)
	{
		_hasLifeTime = val;
	}
	
	public int getRemainingTime()
	{
		return _remainingTime;
	}
	
	public void setRemainingTime(int time)
	{
		_remainingTime = time;
	}
	
	public void setSkill(Skill skill)
	{
		_skill = skill;
	}
	
	public int getLifeTime()
	{
		return _lifeTime;
	}
	
	private void useSkill()
	{
		if (!isTriggered())
		{
			if (hasLifeTime())
			{
				setRemainingTime(getRemainingTime() - 1000);
				if (getRemainingTime() < (getLifeTime() - 15000))
				{
					broadcastPacket(new SocialAction(getObjectId(), 2));
				}
				
				if (getRemainingTime() < 0)
				{
					switch (_skill.getTargetType())
					{
						case AURA :
						case FRONT_AURA :
						case BEHIND_AURA :
							triggerTrap(this);
							break;
						default :
							unSummon();
					}
					return;
				}
			}
			
			final int range = _skill.getTargetType() == TargetType.ONE ? _skill.getCastRange() / 2 : _skill.getAffectRange();
			Creature target = null;
			List<Creature> targets = null;
			try
			{
				targets = World.getAroundCharacters(this, range, 200);
				for (final var character : targets)
				{
					if (!checkTarget(character))
					{
						continue;
					}
					target = character;
					break;
				}
			}
			finally
			{
				targets = null;
			}
			
			if (target != null)
			{
				triggerTrap(target);
			}
		}
	}
}