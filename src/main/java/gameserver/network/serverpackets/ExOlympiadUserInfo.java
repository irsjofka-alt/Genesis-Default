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
package gameserver.network.serverpackets;

import gameserver.model.actor.Player;
import gameserver.model.olympiad.Participant;

public class ExOlympiadUserInfo extends GameServerPacket
{
	private final Player _player;
	private Participant _par = null;
	private int _curHp;
	private int _maxHp;
	private int _curCp;
	private int _maxCp;
	
	public ExOlympiadUserInfo(Player player)
	{
		_player = player;
		if (_player != null)
		{
			_curHp = (int) _player.getCurrentHp();
			_maxHp = (int) _player.getMaxHp();
			_curCp = (int) _player.getCurrentCp();
			_maxCp = (int) _player.getMaxCp();
		}
		else
		{
			_curHp = 0;
			_maxHp = 100;
			_curCp = 0;
			_maxCp = 100;
		}
	}

	public ExOlympiadUserInfo(Participant par)
	{
		_par = par;
		_player = par.getPlayer();
		if (_player != null)
		{
			_curHp = (int) _player.getCurrentHp();
			_maxHp = (int) _player.getMaxHp();
			_curCp = (int) _player.getCurrentCp();
			_maxCp = (int) _player.getMaxCp();
		}
		else
		{
			_curHp = 0;
			_maxHp = 100;
			_curCp = 0;
			_maxCp = 100;
		}
	}

	@Override
	protected final void writeImpl()
	{
		if (_player != null)
		{
			writeC(_player.getOlympiadSide());
			writeD(_player.getObjectId());
			writeS(_player.getName(null));
			writeD(_player.getClassId().getId());
		}
		else
		{
			writeC(_par.getSide());
			writeD(_par.getObjectId());
			writeS(_par.getName());
			writeD(_par.getBaseClass());
		}
		writeD(_curHp);
		writeD(_maxHp);
		writeD(_curCp);
		writeD(_maxCp);
	}
}