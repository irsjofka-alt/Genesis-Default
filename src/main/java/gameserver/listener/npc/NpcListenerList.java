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
package gameserver.listener.npc;

import gameserver.listener.actor.CharListenerList;
import gameserver.model.actor.Npc;

public class NpcListenerList extends CharListenerList
{
	public NpcListenerList(Npc actor)
	{
		super(actor);
	}

	@Override
	public Npc getActor()
	{
		return (Npc) _actor;
	}

	public void onSpawn()
	{
		_global.getListeners().stream().filter(l -> l != null && OnSpawnListener.class.isInstance(l)).forEach(l -> ((OnSpawnListener) l).onSpawn(getActor()));
		getListeners().stream().filter(l -> l != null && OnSpawnListener.class.isInstance(l)).forEach(l -> ((OnSpawnListener) l).onSpawn(getActor()));
	}

	public void onShowChat()
	{
		_global.getListeners().stream().filter(l -> l != null && OnShowChatListener.class.isInstance(l)).forEach(l -> ((OnShowChatListener) l).onShowChat(getActor()));
		getListeners().stream().filter(l -> l != null && OnShowChatListener.class.isInstance(l)).forEach(l -> ((OnShowChatListener) l).onShowChat(getActor()));
	}
	
	public void onDecay()
	{
		_global.getListeners().stream().filter(l -> l != null && OnDecayListener.class.isInstance(l)).forEach(l -> ((OnDecayListener) l).onDecay(getActor()));
		getListeners().stream().filter(l -> l != null && OnDecayListener.class.isInstance(l)).forEach(l -> ((OnDecayListener) l).onDecay(getActor()));
	}
}
