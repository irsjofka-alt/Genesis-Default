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
package scripts.ai.custom;

import gameserver.ThreadPoolManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.holders.SkillHolder;
import gameserver.model.skills.Skill;
import gameserver.model.zone.ZoneId;
import scripts.ai.AbstractNpcAI;

public class ChristmasTree extends AbstractNpcAI
{
	private static final int CHRISTMAS_TREE = 13007;

	private ChristmasTree()
	{
		addFirstTalkId(CHRISTMAS_TREE);
		addSpawnId(CHRISTMAS_TREE);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return null;
	}

	@Override
	public String onSpawn(Npc npc)
	{
		addTask(npc);
		return super.onSpawn(npc);
	}

	private void addTask(Npc npc)
	{
		final SkillHolder holder = new SkillHolder(2139, 1);
		ThreadPoolManager.getInstance().schedule(new ChristmasTreeAI(npc, holder), 1000);
	}

	protected class ChristmasTreeAI implements Runnable
	{
		private final Npc _npc;
		private final SkillHolder _holder;

		protected ChristmasTreeAI(Npc npc, SkillHolder holder)
		{
			_npc = npc;
			_holder = holder;
		}

		@Override
		public void run()
		{
			if ((_npc == null) || !_npc.isVisible() || (_holder == null) || (_holder.getSkill() == null))
			{
				return;
			}

			if (!_npc.isInsideZone(ZoneId.PEACE))
			{
				final Skill skill = _holder.getSkill();

				if ((_npc.getSummoner() == null) || !_npc.getSummoner().isPlayer())
				{
					ThreadPoolManager.getInstance().schedule(this, 1000);
					return;
				}

				final Player player = _npc.getSummoner().getActingPlayer();

				if (!player.isInParty())
				{
					if (player.isInsideRadius(_npc, skill.getAffectRange(), true, true))
					{
						skill.getEffects(_npc, player, false, true);
					}
				}
				else
				{
					for (final Player member : player.getParty().getMembers())
					{
						if ((member != null) && member.isInsideRadius(_npc, skill.getAffectRange(), true, true))
						{
							skill.getEffects(_npc, member, false, true);
						}
					}
				}
			}
			ThreadPoolManager.getInstance().schedule(this, 1000);
		}
	}

	public static void main(String[] args)
	{
		new ChristmasTree();
	}
}
