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
package gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import gameserver.data.DocumentParser;
import gameserver.instancemanager.MapRegionManager;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.actor.ColosseumFence;
import gameserver.model.actor.ColosseumFence.FenceState;
import gameserver.model.actor.Player;

public final class ColosseumFenceParser extends DocumentParser
{
	private final Map<Integer, List<ColosseumFence>> _fence = new HashMap<>();
	
	protected ColosseumFenceParser()
	{
		load();
	}

	@Override
	public void load()
	{
		_fence.clear();
		parseDatapackFile("data/stats/admin/colosseum_fences.xml");
		info("Loaded " + _fence.size() + " colosseum fences.");
	}
	
	@Override
	protected void reloadDocument()
	{
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("colosseum_fence".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap fence = d.getAttributes();

						final int x = Integer.parseInt(fence.getNamedItem("x").getNodeValue());
						final int y = Integer.parseInt(fence.getNamedItem("y").getNodeValue());
						final int z = Integer.parseInt(fence.getNamedItem("z").getNodeValue());
						final int minZ = Integer.parseInt(fence.getNamedItem("minZ").getNodeValue());
						final int maxZ = Integer.parseInt(fence.getNamedItem("maxZ").getNodeValue());
						final int width = Integer.parseInt(fence.getNamedItem("width").getNodeValue());
						final int height = Integer.parseInt(fence.getNamedItem("height").getNodeValue());
						final int type = Integer.parseInt(fence.getNamedItem("type").getNodeValue());

						FenceState fenceState = FenceState.HIDDEN;
						if (type == 1)
						{
							fenceState = FenceState.OPEN;
						}
						else if (type == 2)
						{
							fenceState = FenceState.CLOSED;
						}
						final ColosseumFence fenceInstance = new ColosseumFence(ReflectionManager.DEFAULT, x, y, z, minZ, maxZ, width, height, fenceState);
						final Integer region = MapRegionManager.getInstance().getMapRegionLocId(fenceInstance);
						if (!_fence.containsKey(region))
						{
							_fence.put(region, new ArrayList<>());
						}
						_fence.get(region).add(fenceInstance);
						fenceInstance.spawnMe();
					}
				}
			}
		}
	}

	public ColosseumFence addDynamic(Player player, int x, int y, int z, int minZ, int maxZ, int width, int height, int state)
	{
		FenceState fenceState = FenceState.HIDDEN;
		if (state == 1)
		{
			fenceState = FenceState.OPEN;
		}
		else if (state == 2)
		{
			fenceState = FenceState.CLOSED;
		}
		final ColosseumFence fence = new ColosseumFence(player.getReflection(), x, y, z, minZ, maxZ, width, height, fenceState);
		final Integer region = MapRegionManager.getInstance().getMapRegionLocId(fence);
		if (!_fence.containsKey(region))
		{
			_fence.put(region, new ArrayList<>());
		}
		_fence.get(region).add(fence);
		fence.spawnMe();
		return fence;
	}

	public final void removeFence(ColosseumFence fence, int region)
	{
		fence.decayMe();
		_fence.get(region).remove(fence);
	}

	public Map<Integer, List<ColosseumFence>> getFences()
	{
		return _fence;
	}

	public static ColosseumFenceParser getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static final class SingletonHolder
	{
		protected static final ColosseumFenceParser INSTANCE = new ColosseumFenceParser();
	}
}