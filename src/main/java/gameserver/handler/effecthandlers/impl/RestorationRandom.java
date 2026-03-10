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
package gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.data.parser.ItemsParser;
import gameserver.model.holders.ItemHolder;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExPCCafePointInfo;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Log;
import gameserver.utils.Util;

public class RestorationRandom extends Effect
{
	public RestorationRandom(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffector() == null) || (getEffected() == null) || !getEffector().isPlayer() || !getEffected().isPlayer())
		{
			return false;
		}
		final var player = getEffected().getActingPlayer();
		final var exSkill = getSkill().getExtractableSkill();
		if (exSkill == null)
		{
			return false;
		}

		final var items = exSkill.getProductItems();
		if (items.isEmpty())
		{
			_log.warn("Extractable Skill with no data, probably wrong/empty table in Skill Id: " + getSkill().getId());
			return false;
		}
		
		final double rndNum = 100 * Rnd.nextDouble();
		double chance = 0;
		double chanceFrom = 0;
		
		final List<ItemHolder> creationList = new ArrayList<>();
		for (final var expi : items)
		{
			chance = expi.getChance();
			if ((rndNum >= chanceFrom) && (rndNum <= (chance + chanceFrom)))
			{
				creationList.addAll(expi.getItems());
				break;
			}
			chanceFrom += chance;
		}
		
		if (creationList.isEmpty())
		{
			player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			return false;
		}

		for (final var item : creationList)
		{
			if (item.getCount() <= 0)
			{
				continue;
			}
			
			var amount = item.getCount();
			if (amount != item.getCountMax())
			{
				amount = Rnd.get(amount, item.getCountMax());
			}
			amount *= Config.RATE_EXTRACTABLE;
			
			switch (item.getId())
			{
				case -100 :
					if (player.getPcBangPoints() < Config.MAX_PC_BANG_POINTS)
					{
						if ((player.getPcBangPoints() + amount) > Config.MAX_PC_BANG_POINTS)
						{
							amount = Config.MAX_PC_BANG_POINTS - player.getPcBangPoints();
						}
						
						player.setPcBangPoints((int) (player.getPcBangPoints() + amount));
						final var smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
						smsg.addNumber((int) amount);
						player.sendPacket(smsg);
						player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), (int) amount, false, false, 1));
					}
					break;
				case -200 :
					if (player.getClan() != null)
					{
						player.getClan().addReputationScore((int) amount, true);
					}
					break;
				case -300 :
					player.setFame((int) (player.getFame() + amount));
					player.sendUserInfo();
					break;
				case -1 :
					player.setGamePoints(player.getGamePoints() + amount);
					player.sendMessage("Your game points count changed to " + player.getGamePoints());
					break;
				default :
					final var template = ItemsParser.getInstance().getTemplate(item.getId());
					if (template != null)
					{
						if (template.isStackable())
						{
							player.addItem("Extract", item.getId(), amount, player, true);
						}
						else
						{
							final var count = amount;
							for (int it = 1; it <= count; it++)
							{
								final var newItem = player.addItem("Extract", item.getId(), 1, player, true);
								if (newItem != null && item.getEnchantLevel() > 0)
								{
									newItem.setEnchantLevel(item.getEnchantLevel());
									newItem.updateDatabase();
								}
							}
						}
					}
					break;
			}
			Log.addLogItem("RestorationRandom", player.getName(null) + " -> receive ->", Util.getItemName(item.getId()), amount, item.getEnchantLevel(), null);
		}
		return true;
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.RESTORATION_RANDOM;
	}
}