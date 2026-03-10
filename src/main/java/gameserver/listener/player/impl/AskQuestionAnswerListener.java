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
package gameserver.listener.player.impl;

import gameserver.handler.communityhandlers.impl.CommunityBuffer;
import gameserver.listener.player.OnAnswerListener;
import gameserver.model.actor.Player;

public class AskQuestionAnswerListener implements OnAnswerListener
{
	private final Player _player;
	
	public AskQuestionAnswerListener(Player player)
	{
		_player = player;
	}
	
	@Override
	public void sayYes()
	{
		if (_player != null)
		{
			CommunityBuffer.getInstance().deleteScheme(_player.getQuickVarI("schemeToDel"), _player);
			_player.deleteQuickVar("schemeToDel");
			CommunityBuffer.getInstance().showCommunity(_player, CommunityBuffer.main(_player));
		}
	}
	
	@Override
	public void sayNo()
	{
		if (_player != null)
		{
			CommunityBuffer.getInstance().showCommunity(_player, CommunityBuffer.main(_player));
		}
	}
}