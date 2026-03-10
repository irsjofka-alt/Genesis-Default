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

import java.util.concurrent.Future;

import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.data.parser.SkillsParser;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Decoy;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.skills.Skill;

public class DecoyInstance extends Decoy
{
	private int _totalLifeTime;
	private int _timeRemaining;
	private Future<?> _DecoyLifeTask;
	private Future<?> _HateSpam;
	private boolean _isInDecayStatus = false;
	private long _decayTime = 0;
	
	public DecoyInstance(int objectId, NpcTemplate template, Player owner, int totalLifeTime)
	{
		super(objectId, template, owner);
		
		setInstanceType(InstanceType.DecoyInstance);
		_totalLifeTime = totalLifeTime;
		_timeRemaining = _totalLifeTime;
		final int skilllevel = getTemplate().getIdTemplate() - 13070;
		_DecoyLifeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new DecoyLifetime(getOwner(), this), 1000, 1000);
		_HateSpam = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HateSpam(this, SkillsParser.getInstance().getInfo(5272, skilllevel)), 2000, 5000);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);
		_isInDecayStatus = true;
		_decayTime = System.currentTimeMillis() + (Config.NPC_DECAY_TIME * 1000L);
		_totalLifeTime = 0;
	}
	
	@Override
	public void endDecayTask()
	{
		final var task = _HateSpam;
		if (task != null)
		{
			task.cancel(true);
			_HateSpam = null;
		}
		onDecay();
	}
	
	private class DecoyLifetime implements Runnable
	{
		private final Player _activeChar;
		
		private final DecoyInstance _Decoy;
		
		DecoyLifetime(Player activeChar, DecoyInstance Decoy)
		{
			_activeChar = activeChar;
			_Decoy = Decoy;
		}
		
		@Override
		public void run()
		{
			try
			{
				_Decoy.decTimeRemaining(1000);
				final double newTimeRemaining = _Decoy.getTimeRemaining();
				if (newTimeRemaining < 0)
				{
					_Decoy.unSummon(_activeChar);
				}
			}
			catch (final Exception e)
			{
				_log.warn("Decoy Error: ", e);
			}
		}
	}
	
	private class HateSpam implements Runnable
	{
		private final DecoyInstance _activeChar;
		
		private final Skill _skill;
		
		HateSpam(DecoyInstance activeChar, Skill Hate)
		{
			_activeChar = activeChar;
			_skill = Hate;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (!isDead() && !_isInDecayStatus)
				{
					_activeChar.setTarget(_activeChar);
					_activeChar.doCast(_skill);
				}
				else
				{
					if (_isInDecayStatus && _decayTime < System.currentTimeMillis())
					{
						endDecayTask();
					}
				}
			}
			catch (final Throwable e)
			{
				_log.warn("Decoy Error: ", e);
			}
		}
	}
	
	@Override
	public void unSummon(Player owner)
	{
		var task = _DecoyLifeTask;
		if (task != null)
		{
			task.cancel(true);
			_DecoyLifeTask = null;
		}
		
		task = _HateSpam;
		if (task != null)
		{
			task.cancel(true);
			_HateSpam = null;
		}
		super.unSummon(owner);
	}
	
	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}
	
	public int getTimeRemaining()
	{
		return _timeRemaining;
	}
	
	public int getTotalLifeTime()
	{
		return _totalLifeTime;
	}
	
	@Override
	public double getColRadius()
	{
		final var player = getActingPlayer();
		if (player == null)
		{
			return 0;
		}
		
		if (player.isTransformed())
		{
			return player.getTransformation().getCollisionRadius(player);
		}
		return player.getAppearance().getSex() ? player.getBaseTemplate().getFCollisionRadiusFemale() : player.getBaseTemplate().getfCollisionRadius();
	}
	
	@Override
	public double getColHeight()
	{
		final var player = getActingPlayer();
		if (player == null)
		{
			return 0;
		}
		
		if (player.isTransformed())
		{
			return player.getTransformation().getCollisionHeight(player);
		}
		return player.getAppearance().getSex() ? player.getBaseTemplate().getFCollisionHeightFemale() : player.getBaseTemplate().getfCollisionHeight();
	}
}