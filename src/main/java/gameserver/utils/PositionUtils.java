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
package gameserver.utils;

import java.util.List;

import gameserver.model.GameObject;
import gameserver.model.Location;
import gameserver.model.actor.Creature;

/**
 * Created by LordWinter 28.04.2014 Fixed by L2J Eternity-World
 */
public class PositionUtils
{
	protected static final double HEADINGS_IN_PI = 10430.378350470452724949566316381;
	
	public enum TargetDirection
	{
		NONE, FRONT, SIDE, BEHIND
	}
	
	private static final int MAX_ANGLE = 360;
	
	private static final double FRONT_MAX_ANGLE = 100;
	private static final double BACK_MAX_ANGLE = 40;
	
	public static TargetDirection getDirectionTo(Creature target, Creature attacker)
	{
		if ((target == null) || (attacker == null))
		{
			return TargetDirection.NONE;
		}
		if (isBehind(target, attacker))
		{
			return TargetDirection.BEHIND;
		}
		if (isInFrontOf(target, attacker))
		{
			return TargetDirection.FRONT;
		}
		return TargetDirection.SIDE;
	}
	
	public static boolean isInFrontOf(Creature target, Creature attacker)
	{
		if (target == null)
		{
			return false;
		}
		
		double angleChar, angleTarget, angleDiff;
		angleTarget = calculateAngleFrom(target, attacker);
		angleChar = convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= (-MAX_ANGLE + FRONT_MAX_ANGLE))
		{
			angleDiff += MAX_ANGLE;
		}
		if (angleDiff >= (MAX_ANGLE - FRONT_MAX_ANGLE))
		{
			angleDiff -= MAX_ANGLE;
		}
		if (Math.abs(angleDiff) <= FRONT_MAX_ANGLE)
		{
			return true;
		}
		return false;
	}
	
	public static boolean isBehind(Creature target, Creature attacker)
	{
		if (target == null)
		{
			return false;
		}
		
		double angleChar, angleTarget, angleDiff;
		angleChar = calculateAngleFrom(attacker, target);
		angleTarget = convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= (-MAX_ANGLE + BACK_MAX_ANGLE))
		{
			angleDiff += MAX_ANGLE;
		}
		if (angleDiff >= (MAX_ANGLE - BACK_MAX_ANGLE))
		{
			angleDiff -= MAX_ANGLE;
		}
		if (Math.abs(angleDiff) <= BACK_MAX_ANGLE)
		{
			return true;
		}
		return false;
	}
	
	public static boolean isFacing(Creature attacker, GameObject target, int maxAngle)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff;
		if (target == null)
		{
			return false;
		}
		maxAngleDiff = maxAngle / 2;
		angleTarget = calculateAngleFrom(attacker, target);
		angleChar = convertHeadingToDegree(attacker.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= (-360 + maxAngleDiff))
		{
			angleDiff += 360;
		}
		if (angleDiff >= (360 - maxAngleDiff))
		{
			angleDiff -= 360;
		}
		if (Math.abs(angleDiff) <= maxAngleDiff)
		{
			return true;
		}
		return false;
	}
	
	public static int calculateHeadingFrom(GameObject obj1, GameObject obj2)
	{
		return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
		{
			angleTarget = MAX_ANGLE + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}
	
	public static double calculateAngleFrom(GameObject obj1, GameObject obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return angleTarget;
	}
	
	public static boolean checkIfInRange(int range, int x1, int y1, int x2, int y2)
	{
		return checkIfInRange(range, x1, y1, 0, x2, y2, 0, false);
	}
	
	public static boolean checkIfInRange(int range, int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		final long dx = x1 - x2;
		final long dy = y1 - y2;
		
		if (includeZAxis)
		{
			final long dz = z1 - z2;
			return ((dx * dx) + (dy * dy) + (dz * dz)) <= (range * range);
		}
		return ((dx * dx) + (dy * dy)) <= (range * range);
	}
	
	public static boolean checkIfInRange(int range, GameObject obj1, GameObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		return checkIfInRange(range, obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
	}
	
	public static double convertHeadingToDegree(int heading)
	{
		return heading / 182.044444444;
	}
	
	public static double convertHeadingToRadian(int heading)
	{
		return Math.toRadians(convertHeadingToDegree(heading) - 90);
	}
	
	public static double convertHeadingToRadian(int x, int y, int tx, int ty)
	{
		return Math.toRadians(convertHeadingToDegree(calculateHeadingFrom(x, y, tx, ty)) - 90);
	}
	
	public static int convertDegreeToClientHeading(double degree)
	{
		if (degree < 0)
		{
			degree = 360 + degree;
		}
		return (int) (degree * 182.044444444);
	}
	
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2)
	{
		return calculateDistance(x1, y1, 0, x2, y2, 0, false);
	}
	
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		final long dx = x1 - x2;
		final long dy = y1 - y2;
		
		if (includeZAxis)
		{
			final long dz = z1 - z2;
			return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		}
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	public static double calculateDistance(GameObject obj1, GameObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return Integer.MAX_VALUE;
		}
		return calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
	}
	
	public static double getDistance(GameObject a1, GameObject a2)
	{
		return getDistance(a1.getX(), a2.getY(), a2.getX(), a2.getY());
	}
	
	public static double getDistance(Location loc1, Location loc2)
	{
		return getDistance(loc1.getX(), loc1.getY(), loc2.getX(), loc2.getY());
	}
	
	public static double getDistance(int x1, int y1, int x2, int y2)
	{
		return Math.hypot(x1 - x2, y1 - y2);
	}
	
	public static int getHeadingTo(GameObject actor, GameObject target)
	{
		if ((actor == null) || (target == null) || (target == actor))
		{
			return -1;
		}
		return getHeadingTo(actor.getLocation(), target.getLocation());
	}
	
	public static int getHeadingTo(Location actor, Location target)
	{
		if ((actor == null) || (target == null) || target.equals(actor))
		{
			return -1;
		}
		
		final int dx = target.getX() - actor.getX();
		final int dy = target.getY() - actor.getY();
		int heading = target.getHeading() - (int) ((Math.atan2(-dy, -dx) * HEADINGS_IN_PI) + 32768);
		
		if (heading < 0)
		{
			heading = (heading + 1 + Integer.MAX_VALUE) & 0xFFFF;
		}
		else if (heading > 0xFFFF)
		{
			heading &= 0xFFFF;
		}
		return heading;
	}
	
	public static Location applyOffset(Creature activeChar, Location point, int offset)
	{
		if (offset <= 0)
		{
			return point;
		}
		
		final long dx = point.getX() - activeChar.getX();
		final long dy = point.getY() - activeChar.getY();
		final long dz = point.getZ() - activeChar.getZ();
		
		final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		
		if (distance <= offset)
		{
			point.set(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			return point;
		}
		
		if (distance >= 1)
		{
			final double cut = offset / distance;
			point.setX(point.getX() - (int) (dx * cut + 0.5));
			point.setY(point.getY() - (int) (dy * cut + 0.5));
			point.setZ(point.getZ() - (int) (dz * cut + 0.5));
			
			if (!activeChar.isFlying() && !activeChar.isInVehicle() && !activeChar.isInWater(activeChar) && !activeChar.isVehicle())
			{
				point.correctGeoZ();
			}
		}
		return point;
	}
	
	public static List<Location> applyOffset(List<Location> points, int offset)
	{
		offset = offset >> 4;
		if (offset <= 0)
		{
			return points;
		}
		
		final long dx = points.get(points.size() - 1).getX() - points.get(0).getX();
		final long dy = points.get(points.size() - 1).getY() - points.get(0).getY();
		final long dz = points.get(points.size() - 1).getZ() - points.get(0).getZ();
		
		final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (distance <= offset)
		{
			final Location point = points.get(0);
			points.clear();
			points.add(point);
			return points;
		}
		
		if (distance >= 1)
		{
			final double cut = offset / distance;
			final int num = (int) (points.size() * cut + 0.5);
			for (int i = 1; i <= num && points.size() > 0; i++)
			{
				points.remove(points.size() - 1);
			}
		}
		return points;
	}
}