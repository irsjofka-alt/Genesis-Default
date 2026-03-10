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
package gameserver.model.skills.l2skills;

import gameserver.data.parser.NpcsParser;
import gameserver.geodata.GeoEngine;
import gameserver.idfactory.IdFactory;
import gameserver.model.GameObject;
import gameserver.model.Location;
import gameserver.model.actor.Creature;
import gameserver.model.actor.instance.EffectPointInstance;
import gameserver.model.skills.Skill;
import gameserver.model.skills.targets.TargetType;
import gameserver.model.stats.StatsSet;

public final class SkillSignet extends Skill
{
	public SkillSignet(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature caster, GameObject[] targets, double cubicPower)
	{
		if (caster.isAlikeDead())
		{
			return;
		}

		final var template = NpcsParser.getInstance().getTemplate(getNpcId());
		final var effectPoint = new EffectPointInstance(IdFactory.getInstance().getNextId(), template, caster, this);
		effectPoint.setCurrentHp(effectPoint.getMaxHp());
		effectPoint.setCurrentMp(effectPoint.getMaxMp());

		int x = caster.getX();
		int y = caster.getY();
		int z = caster.getZ();

		if (caster.isPlayer() && (getTargetType() == TargetType.GROUND))
		{
			final Location wordPosition = caster.getActingPlayer().getCurrentSkillWorldPosition();

			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		z = GeoEngine.getInstance().getSpawnHeight(x, y, z);
		getEffects(caster, effectPoint, true, true);

		effectPoint.setIsInvul(true);
		effectPoint.spawnMe(x, y, z);
		final var ai = effectPoint.getAI();
		if (ai != null)
		{
			ai.startAITask();
		}
	}
}