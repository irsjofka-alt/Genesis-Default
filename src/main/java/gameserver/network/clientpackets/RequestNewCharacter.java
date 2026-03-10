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
package gameserver.network.clientpackets;

import gameserver.data.parser.CharTemplateParser;
import gameserver.model.base.ClassId;
import gameserver.network.serverpackets.NewCharacterSuccess;

public final class RequestNewCharacter extends GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		final var ct = new NewCharacterSuccess();
		final var instance = CharTemplateParser.getInstance();
		ct.addChar(instance.getTemplate(ClassId.FIGHTER));
		ct.addChar(instance.getTemplate(ClassId.MAGE));
		ct.addChar(instance.getTemplate(ClassId.ELVEN_FIGHTER));
		ct.addChar(instance.getTemplate(ClassId.ELVEN_MAGE));
		ct.addChar(instance.getTemplate(ClassId.DARK_FIGHTER));
		ct.addChar(instance.getTemplate(ClassId.DARK_MAGE));
		ct.addChar(instance.getTemplate(ClassId.ORC_FIGHTER));
		ct.addChar(instance.getTemplate(ClassId.ORC_MAGE));
		ct.addChar(instance.getTemplate(ClassId.DWARVEN_FIGHTER));
		ct.addChar(instance.getTemplate(ClassId.MALE_SOILDER));
		ct.addChar(instance.getTemplate(ClassId.FEMALE_SOILDER));
		sendPacket(ct);
	}
}