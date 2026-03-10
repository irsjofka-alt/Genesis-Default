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
package scripts.ai.dragonvalley;

import l2e.commons.util.Rnd;
import gameserver.ai.npc.Fighter;
import gameserver.data.parser.SkillsParser;
import gameserver.geodata.GeoEngine;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;

/**
 * Created by LordWinter 28.05.2019
 */
public class Knoriks extends Fighter
{
	private Location[] _points = null;
	
	private final Location[] _points1 = new Location[]
	{
	        new Location(140604, 114508, -3720), new Location(141213, 114571, -3720), new Location(142089, 114339, -3720), new Location(142576, 113686, -3720), new Location(143010, 113831, -3720), new Location(143602, 114477, -3720), new Location(143991, 114774, -3720), new Location(144817, 114949, -3720), new Location(145290, 115368, -3720), new Location(145585, 115841, -3720), new Location(146135, 116140, -3720), new Location(146707, 116377, -3720), new Location(147285, 116135, -3720), new Location(148116, 116153, -3720), new Location(148834, 115673, -3720), new Location(149253, 114841, -3720), new Location(149268, 114236, -3720), new Location(149118, 113220, -3720), new Location(148658, 112282, -3720), new Location(148093, 112008, -3720), new Location(147011, 112399, -3720), new Location(145848, 112771, -3720), new Location(145003, 113763, -3720), new Location(145123, 114965, -3720), new Location(145672, 115854, -3720), new Location(146267, 116153, -3720), new Location(146862, 116277, -3720), new Location(147254, 117077, -3712), new Location(148486, 117838, -3712),
	};
	
	private final Location[] _points2 = new Location[]
	{
	        new Location(143820, 110172, -3936), new Location(142599, 110060, -3944), new Location(142477, 110686, -3944), new Location(142736, 111263, -3944), new Location(143154, 111808, -3944), new Location(143567, 112067, -3944), new Location(142814, 111870, -3944), new Location(142081, 112055, -3800), new Location(141859, 112305, -3720), new Location(141448, 112739, -3720), new Location(140917, 113042, -3720), new Location(140686, 112332, -3720), new Location(140641, 111953, -3720), new Location(140545, 111532, -3840), new Location(140581, 110501, -3944), new Location(141167, 109649, -3944), new Location(142038, 109301, -3944), new Location(142990, 108693, -3944), new Location(142849, 108131, -3944), new Location(142639, 107729, -3944), new Location(142548, 107332, -3944), new Location(143270, 107225, -3944), new Location(144648, 107664, -3944), new Location(144471, 108299, -3944), new Location(145009, 108670, -3944), new Location(146240, 109339, -3944), new Location(147700, 109819, -3944),
	};
	
	private final Location[] _points3 = new Location[]
	{
	        new Location(146460, 109809, -3424), new Location(146523, 109139, -3472), new Location(146565, 108198, -3704), new Location(146959, 107785, -3920), new Location(147629, 107309, -4040), new Location(148344, 107544, -4176), new Location(149032, 108402, -4352), new Location(149869, 108299, -4568), new Location(150297, 107596, -4680), new Location(151286, 107257, -4872), new Location(152409, 107695, -5072), new Location(153554, 107857, -5152), new Location(153552, 108326, -5152), new Location(153496, 108810, -5152), new Location(152927, 109267, -5152), new Location(151702, 109307, -5152), new Location(151116, 109293, -5136), new Location(150287, 109522, -5120), new Location(149574, 109763, -5224), new Location(149387, 110508, -5400), new Location(149404, 110880, -5464), new Location(149815, 111397, -5504), new Location(151689, 111304, -5520), new Location(153164, 111658, -5520), new Location(153551, 111249, -5520), new Location(153437, 110763, -5520), new Location(152339, 110422, -5520),
	};
	
	private final Location[] _points4 = new Location[]
	{
	        new Location(145484, 120220, -3912), new Location(146262, 121372, -3912), new Location(146180, 121825, -3912), new Location(145691, 121944, -3912), new Location(144585, 121464, -3912), new Location(143985, 121211, -3904), new Location(143526, 121078, -3912), new Location(141770, 121528, -3912), new Location(140740, 120742, -3896), new Location(140174, 119815, -3912), new Location(140492, 119201, -3912), new Location(141119, 117931, -3912), new Location(142105, 117257, -3912), new Location(143028, 117334, -3912), new Location(142917, 118083, -3904), new Location(142349, 118750, -3896), new Location(142659, 119882, -3912),
	};
	
	private int _lastPoint = 0;
	private boolean _firstThought = true;
	private boolean _isRecycle = false;
	private long _moveInterval = 0L;
	private long _checkInterval = 0L;
	private final String _npcsZones = "22_21,23_21,24_21";
	
	public Knoriks(Attackable actor)
	{
		super(actor);
		
		MAX_PURSUE_RANGE = Integer.MAX_VALUE - 10;
		actor.setIsRunner(true);
		actor.setCanReturnToSpawnPoint(false);
		actor.setIsGlobalAI(true);
		_checkInterval = System.currentTimeMillis() + 60000;
	}
	
	@Override
	protected void onEvtSpawn()
	{
		final var npc = getActiveChar();
		final var spawnLoc = npc.getSpawnedLoc();
		if (spawnLoc == null)
		{
			return;
		}
		
		if (spawnLoc.getX() == 140604 && spawnLoc.getY() == 114508)
		{
			_points = _points1;
		}
		else if (spawnLoc.getX() == 143820 && spawnLoc.getY() == 110172)
		{
			_points = _points2;
		}
		else if (spawnLoc.getX() == 146460 && spawnLoc.getY() == 109809)
		{
			_points = _points3;
		}
		else if (spawnLoc.getX() == 145484 && spawnLoc.getY() == 120220)
		{
			_points = _points4;
		}
		super.onEvtSpawn();
	}
	
	@Override
	protected boolean thinkActive()
	{
		final var npc = getActiveChar();
		if (npc.isDead())
		{
			return true;
		}
		
		if (super.thinkActive() || _points == null || _moveInterval > System.currentTimeMillis())
		{
			return true;
		}
		
		if (_firstThought || _lastPoint >= _points.length || _lastPoint < 0)
		{
			startMoveTask();
		}
		else
		{
			final Location loc = _points[_lastPoint];
			if (npc.getDistance(loc) <= 40)
			{
				startMoveTask();
			}
			else
			{
				_moveInterval = System.currentTimeMillis() + 1000L;
				moveTo(loc.correctGeoZ());
			}
		}
		return true;
	}
	
	@Override
	protected void thinkAttack()
	{
		final var actor = getActiveChar();
		if (actor == null || actor.isDead())
		{
			return;
		}
		
		if (_checkInterval < System.currentTimeMillis())
		{
			final int geoX = GeoEngine.getInstance().getMapX(actor.getX());
			final int geoY = GeoEngine.getInstance().getMapY(actor.getY());
			final String reg = "" + geoX + "_" + geoY + "";
			boolean valid = false;
			for (final String region : _npcsZones.split(","))
			{
				if (region.equals(reg))
				{
					valid = true;
					break;
				}
			}
			
			if (!valid)
			{
				returnHome(true, true);
			}
			_checkInterval = System.currentTimeMillis() + 60000;
			return;
		}
		super.thinkAttack();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final var actor = getActiveChar();
		if (actor.isDead())
		{
			return;
		}
		
		if (actor.isScriptValue(0) && _lastPoint > 0)
		{
			actor.setScriptValue(1);
			if (_isRecycle)
			{
				_lastPoint++;
			}
			else
			{
				_lastPoint--;
			}
		}
		
		if (Rnd.chance(5) && attacker != null && !actor.isOutOfControl() && !actor.isActionsDisabled())
		{
			actor.setTarget(actor);
			actor.doCast(SkillsParser.getInstance().getInfo(6744, 1));
			return;
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	private void startMoveTask()
	{
		final var npc = getActiveChar();
		
		if (_firstThought)
		{
			_lastPoint = getIndex(Location.findNearest(npc, _points));
			_firstThought = false;
		}
		else
		{
			if (_isRecycle)
			{
				_lastPoint--;
			}
			else
			{
				_lastPoint++;
			}
		}
		
		if (_isRecycle && _lastPoint <= 0)
		{
			_lastPoint = 0;
			_isRecycle = false;
		}
		
		if (_lastPoint >= _points.length && !_isRecycle)
		{
			_isRecycle = true;
			_lastPoint--;
		}
		
		if (npc.getScriptValue() > 0)
		{
			npc.setScriptValue(0);
		}
		
		npc.setRunning();
		if (Rnd.chance(5))
		{
			npc.makeTriggerCast(SkillsParser.getInstance().getInfo(6757, 1), npc);
		}
		
		Location loc = null;
		try
		{
			loc = _points[_lastPoint];
		}
		catch (final Exception _)
		{}
		
		if (loc == null)
		{
			if (_isRecycle)
			{
				_lastPoint = _points.length - 1;
			}
			else
			{
				_lastPoint = 0;
			}
			loc = _points[_lastPoint];
		}
		
		if (loc != null)
		{
			_moveInterval = System.currentTimeMillis() + 1000L;
			moveTo(loc.correctGeoZ());
		}
	}
	
	private int getIndex(Location loc)
	{
		for (int i = 0; i < _points.length; i++)
		{
			if (_points[i] == loc)
			{
				return i;
			}
		}
		return 0;
	}
	
	@Override
	protected void teleportHome()
	{
	}
	
	@Override
	protected void returnHome(boolean clearAggro, boolean teleport)
	{
		final var actor = getActiveChar();
		if (actor.isDead())
		{
			return;
		}
		super.returnHome(clearAggro, teleport);
		_lastPoint = getIndex(Location.findNearest(actor, _points));
		_firstThought = false;
	}
}
