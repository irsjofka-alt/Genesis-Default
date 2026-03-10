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
package gameserver.geodata.pathfinding.cellnodes;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gameserver.Config;
import gameserver.geodata.GeoEngine;
import gameserver.geodata.loader.Cell;
import gameserver.geodata.pathfinding.AbstractNode;
import gameserver.geodata.pathfinding.AbstractNodeLoc;
import gameserver.geodata.pathfinding.PathFinding;
import gameserver.model.GameObject;
import gameserver.model.entity.Reflection;
import gameserver.model.items.instance.ItemInstance;

public final class CellPathFinding extends PathFinding
{
	private static final Logger _log = LogManager.getLogger(CellPathFinding.class);

	private BufferInfo[] _allBuffers;
	private int _findSuccess = 0;
	private int _findFails = 0;
	private int _postFilterUses = 0;
	private int _postFilterPlayableUses = 0;
	private int _postFilterPasses = 0;
	private long _postFilterElapsed = 0;

	private List<ItemInstance> _debugItems = null;

	public CellPathFinding()
	{
	}

	@Override
	public void load()
	{
		if (!Config.PATHFIND_BOOST)
		{
			return;
		}

		try
		{
			final String[] array = Config.PATHFIND_BUFFERS.split(";");
			_allBuffers = new BufferInfo[array.length];
			String buf;
			String[] args;
			for (int i = 0; i < array.length; i++)
			{
				buf = array[i];
				args = buf.split("x");
				if (args.length != 2)
				{
					throw new Exception("Invalid buffer definition: " + buf);
				}
				_allBuffers[i] = new BufferInfo(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		}
		catch (final Exception e)
		{
			_log.warn("CellPathFinding: Problem during buffer init: " + e.getMessage(), e);
			throw new Error("CellPathFinding: load aborted");
		}
	}

	@Override
	public boolean pathNodesExist(short regionoffset)
	{
		return false;
	}

	@Override
	public List<AbstractNodeLoc> findPath(GameObject actor, int x, int y, int z, int tx, int ty, int tz, Reflection ref, boolean playable, boolean isDebugPF)
	{
		final var geo = GeoEngine.getInstance();
		final int gx = geo.getGeoX(x);
		final int gy = geo.getGeoY(y);
		if (!geo.hasGeo(x, y))
		{
			return null;
		}
		final double gz = geo.getHeight(x, y, z);

		int gtx = geo.getGeoX(tx);
		int gty = geo.getGeoY(ty);

		if (!geo.hasGeo(tx, ty))
		{
			boolean adjusted = false;
			int gtzSnap = geo.getHeight(tx, ty, tz);
			outer : for (int dx = -1; dx <= 1; dx++)
			{
				for (int dy = -1; dy <= 1; dy++)
				{
					if (dx == 0 && dy == 0)
					{
						continue;
					}

					final int ngx = gtx + dx;
					final int ngy = gty + dy;

					if (geo.hasGeoPos(ngx, ngy))
					{
						final int nswe = geo.NgetNearestNSWE(ngx, ngy, gtzSnap);
						if (nswe == Cell.NSWE_ALL)
						{
							gtx = ngx;
							gty = ngy;
							gtzSnap = geo.getNearestZ(ngx, ngy, gtzSnap);
							adjusted = true;
							break outer;
						}
					}
				}
			}

			if (!adjusted)
			{
				outer2 : for (int dx = -1; dx <= 1; dx++)
				{
					for (int dy = -1; dy <= 1; dy++)
					{
						if (dx == 0 && dy == 0)
						{
							continue;
						}

						final int ngx = gtx + dx;
						final int ngy = gty + dy;

						if (geo.hasGeoPos(ngx, ngy))
						{
							gtx = ngx;
							gty = ngy;
							gtzSnap = geo.getNearestZ(ngx, ngy, gtzSnap);
							adjusted = true;
							break outer2;
						}
					}
				}
			}

			if (!adjusted)
			{
				return null;
			}
			tx = geo.getWorldX(gtx);
			ty = geo.getWorldY(gty);
			tz = gtzSnap;
		}

		final double gtz = geo.getHeight(tx, ty, tz);
		final var buffer = alloc(64 + (2 * Math.max(Math.abs(gx - gtx), Math.abs(gy - gty))), playable, false);
		if (buffer == null)
		{
			return null;
		}

		final boolean debug = playable && Config.DEBUG_PATH;
		if (debug)
		{
			if (_debugItems == null)
			{
				_debugItems = new CopyOnWriteArrayList<>();
			}
			else
			{
				_debugItems.stream().filter(i -> i != null).forEach(i -> i.decayMe());
				_debugItems.clear();
			}
		}

		List<AbstractNodeLoc> path = null;
		try
		{
			final var result = buffer.findPath(gx, gy, (int) gz, gtx, gty, (int) gtz);
			if (debug)
			{
				for (final var n : buffer.debugPath())
				{
					if (n.getCost() < 0)
					{
						dropDebugItem(1831, (int) (-n.getCost() * 10), n.getLoc());
					}
					else
					{
						dropDebugItem(57, (int) (n.getCost() * 10), n.getLoc());
					}
				}
			}

			if (result == null)
			{
				_findFails++;
				final var last = geo.moveCheck(null, geo.getWorldX(gx), geo.getWorldY(gy), (int) gz, tx, ty, (int) gtz, ref, true);
				final int lgx = geo.getGeoX(last.getX());
				final int lgy = geo.getGeoY(last.getY());
				if ((lgx != gx) || (lgy != gy))
				{
					final var fallback = new LinkedList<AbstractNodeLoc>();
					fallback.add(new NodeLoc(lgx, lgy, last.getZ()));
					return fallback;
				}
				return null;
			}
			path = constructPath(result);
		}
		catch (final Exception e)
		{
			_log.warn("", e);
			return null;
		}
		finally
		{
			buffer.free();
		}

		if ((path.size() < 3) || (Config.MAX_POSTFILTER_PASSES <= 0))
		{
			_findSuccess++;
			return path;
		}

		final long timeStamp = System.currentTimeMillis();
		_postFilterUses++;
		if (playable)
		{
			_postFilterPlayableUses++;
		}

		int currentX, currentY, currentZ;
		ListIterator<AbstractNodeLoc> middlePoint;
		boolean remove;
		int pass = 0;
		do
		{
			pass++;
			_postFilterPasses++;
			remove = false;
			middlePoint = path.listIterator();
			currentX = x;
			currentY = y;
			currentZ = z;

			while (middlePoint.hasNext())
			{
				final var locMiddle = middlePoint.next();
				if (!middlePoint.hasNext())
				{
					break;
				}

				final var locEnd = path.get(middlePoint.nextIndex());
				if (geo.canMoveToCoord(actor, currentX, currentY, currentZ, locEnd.getX(), locEnd.getY(), locEnd.getZ(), ref, false))
				{
					middlePoint.remove();
					remove = true;
					if (debug)
					{
						dropDebugItem(735, 1, locMiddle);
					}
				}
				else
				{
					currentX = locMiddle.getX();
					currentY = locMiddle.getY();
					currentZ = locMiddle.getZ();
				}
			}
		}
		while (playable && remove && (path.size() > 2) && (pass < Config.MAX_POSTFILTER_PASSES));
		if (debug)
		{
			path.forEach(n -> dropDebugItem(1375, 1, n));
		}
		_findSuccess++;
		_postFilterElapsed += System.currentTimeMillis() - timeStamp;
		return path;
	}

	private List<AbstractNodeLoc> constructPath(AbstractNode<NodeLoc> node)
	{
		final LinkedList<AbstractNodeLoc> path = new LinkedList<>();
		int previousDirectionX = Integer.MIN_VALUE;
		int previousDirectionY = Integer.MIN_VALUE;
		int directionX, directionY;

		while (node.getParent() != null)
		{
			if (!Config.ADVANCED_DIAGONAL_STRATEGY && (node.getParent().getParent() != null))
			{
				final int tmpX = node.getLoc().getNodeX() - node.getParent().getParent().getLoc().getNodeX();
				final int tmpY = node.getLoc().getNodeY() - node.getParent().getParent().getLoc().getNodeY();
				if (Math.abs(tmpX) == Math.abs(tmpY))
				{
					directionX = tmpX;
					directionY = tmpY;
				}
				else
				{
					directionX = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
					directionY = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
				}
			}
			else
			{
				directionX = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
				directionY = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
			}

			if ((directionX != previousDirectionX) || (directionY != previousDirectionY))
			{
				previousDirectionX = directionX;
				previousDirectionY = directionY;

				path.addFirst(node.getLoc());
				node.setLoc(null);
			}

			node = node.getParent();
		}

		return path;
	}

	private CellNodeBuffer alloc(int size, boolean playable, boolean isDebug)
	{
		if (isDebug)
		{
			return new CellNodeBuffer(0);
		}

		CellNodeBuffer current = null;
		for (final var i : _allBuffers)
		{
			if (i.mapSize >= size)
			{
				for (final var buf : i.bufs)
				{
					if (buf.lock())
					{
						i.uses++;
						if (playable)
						{
							i.playableUses++;
						}
						i.elapsed += buf.getElapsedTime();
						current = buf;
						break;
					}
				}
				if (current != null)
				{
					break;
				}

				current = new CellNodeBuffer(i.mapSize);
				current.lock();
				if (i.bufs.size() < i.count)
				{
					i.bufs.add(current);
					i.uses++;
					if (playable)
					{
						i.playableUses++;
					}
					break;
				}

				i.overflows++;
				if (playable)
				{
					i.playableOverflows++;
				}
			}
		}
		return current;
	}

	private void dropDebugItem(int itemId, int num, AbstractNodeLoc loc)
	{
		final var item = new ItemInstance(itemId);
		item.setCount(num);
		item.spawnMe(loc.getX(), loc.getY(), loc.getZ());
		_debugItems.add(item);
	}

	private static final class BufferInfo
	{
		final int mapSize;
		final int count;
		List<CellNodeBuffer> bufs;
		int uses = 0;
		int playableUses = 0;
		int overflows = 0;
		int playableOverflows = 0;
		long elapsed = 0;

		public BufferInfo(int size, int cnt)
		{
			mapSize = size;
			count = cnt;
			bufs = new CopyOnWriteArrayList<>();
		}

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder(100);
			sb.append(mapSize);
			sb.append("x");
			sb.append(mapSize);
			sb.append(" num:");
			sb.append(bufs.size());
			sb.append("/");
			sb.append(count);
			sb.append(" uses:");
			sb.append(uses);
			sb.append("/");
			sb.append(playableUses);
			if (uses > 0)
			{
				sb.append(" total/avg(ms):");
				sb.append(elapsed);
				sb.append("/");
				sb.append(String.format("%1.2f", (double) elapsed / uses));
			}

			sb.append(" ovf:");
			sb.append(overflows);
			sb.append("/");
			sb.append(playableOverflows);

			return sb.toString();
		}
	}

	@Override
	public String[] getStat()
	{
		final String[] result = new String[_allBuffers.length + 1];
		for (int i = 0; i < _allBuffers.length; i++)
		{
			result[i] = _allBuffers[i].toString();
		}

		final StringBuilder sb = new StringBuilder(128);
		sb.append("LOS postfilter uses:");
		sb.append(_postFilterUses);
		sb.append("/");
		sb.append(_postFilterPlayableUses);
		if (_postFilterUses > 0)
		{
			sb.append(" total/avg(ms):");
			sb.append(_postFilterElapsed);
			sb.append("/");
			sb.append(String.format("%1.2f", (double) _postFilterElapsed / _postFilterUses));
			sb.append(" passes total/avg:");
			sb.append(_postFilterPasses);
			sb.append("/");
			sb.append(String.format("%1.1f", (double) _postFilterPasses / _postFilterUses));
			sb.append(System.lineSeparator());
		}
		sb.append("Pathfind success/fail:");
		sb.append(_findSuccess);
		sb.append("/");
		sb.append(_findFails);
		result[result.length - 1] = sb.toString();
		return result;
	}
}
