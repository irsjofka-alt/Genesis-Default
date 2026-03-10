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
package gameserver.model.actor.templates.npc;

public class DamageInfo
{
	private final String _clan;
	private final int _damage;
		
	public DamageInfo(String clan, int damage)
	{
		_clan = clan;
		_damage = damage;
	}
		
	public final String getClanName()
	{
		return _clan;
	}
		
	public final int getDamage()
	{
		return _damage;
	}
		
	@Override
	public int hashCode()
	{
		return 12 * _clan.hashCode() + 11270;
	}
}