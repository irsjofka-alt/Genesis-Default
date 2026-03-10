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
package gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import gameserver.data.htm.HtmCache;
import gameserver.data.parser.SkillsParser;
import gameserver.data.parser.TeleLocationParser;
import gameserver.instancemanager.CHSiegeManager;
import gameserver.instancemanager.ClanHallManager;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Clan;
import gameserver.model.PcCondOverride;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.base.FunctionType;
import gameserver.model.entity.ClanHall;
import gameserver.model.entity.clanhall.SiegableHall;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.AgitDecoInfo;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.utils.Util;

public class ClanHallManagerInstance extends MerchantInstance
{
	protected static final int COND_OWNER_FALSE = 0;
	protected static final int COND_ALL_FALSE = 1;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 2;
	protected static final int COND_OWNER = 3;
	private int _clanHallId = -1;

	public ClanHallManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		setInstanceType(InstanceType.ClanHallManagerInstance);
	}

	@Override
	public boolean isWarehouse()
	{
		return true;
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final var clanHall = getClanHall();
		if (clanHall == null || clanHall.isSiegableHall() && ((SiegableHall) clanHall).isInSiege())
		{
			return;
		}
		
		final var template = clanHall.getTemplate();
		if (template == null)
		{
			return;
		}
		
		final int condition = validateCondition(clanHall, player);
		if (condition <= COND_ALL_FALSE)
		{
			return;
		}

		final var format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		if (condition == COND_OWNER)
		{
			final var st = new StringTokenizer(command, " ");
			final String actualCommand = st.nextToken();
			String val = "";
			if (st.countTokens() >= 1)
			{
				val = st.nextToken();
			}

			if (actualCommand.equalsIgnoreCase("banish_foreigner"))
			{
				final var html = new NpcHtmlMessage(1);
				if ((player.getClanPrivileges() & Clan.CP_CH_DISMISS) == Clan.CP_CH_DISMISS)
				{
					if (val.equalsIgnoreCase("list"))
					{
						html.setFile(player, player.getLang(), "data/html/clanHallManager/banish-list.htm");
					}
					else if (val.equalsIgnoreCase("banish"))
					{
						clanHall.banishForeigners();
						html.setFile(player, player.getLang(), "data/html/clanHallManager/banish.htm");
					}
				}
				else
				{
					html.setFile(player, player.getLang(), "data/html/clanHallManager/not_authorized.htm");
				}
				sendHtmlMessage(player, html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manage_vault"))
			{
				final var html = new NpcHtmlMessage(1);
				if ((player.getClanPrivileges() & Clan.CP_CL_VIEW_WAREHOUSE) == Clan.CP_CL_VIEW_WAREHOUSE)
				{
					if (clanHall.getRentPrice() <= 0)
					{
						html.setFile(player, player.getLang(), "data/html/clanHallManager/vault-chs.htm");
					}
					else
					{
						html.setFile(player, player.getLang(), "data/html/clanHallManager/vault.htm");
						html.replace("%rent%", String.valueOf(clanHall.getRentPrice()));
						html.replace("%date%", format.format(clanHall.getPaidUntil()));
					}
					sendHtmlMessage(player, html);
				}
				else
				{
					html.setFile(player, player.getLang(), "data/html/clanHallManager/not_authorized.htm");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("door"))
			{
				final var html = new NpcHtmlMessage(1);
				if ((player.getClanPrivileges() & Clan.CP_CH_OPEN_DOOR) == Clan.CP_CH_OPEN_DOOR)
				{
					if (val.equalsIgnoreCase("open"))
					{
						clanHall.openCloseDoors(true);
						html.setFile(player, player.getLang(), "data/html/clanHallManager/door-open.htm");
					}
					else if (val.equalsIgnoreCase("close"))
					{
						clanHall.openCloseDoors(false);
						html.setFile(player, player.getLang(), "data/html/clanHallManager/door-close.htm");
					}
					else
					{
						html.setFile(player, player.getLang(), "data/html/clanHallManager/door.htm");
					}
					sendHtmlMessage(player, html);
				}
				else
				{
					html.setFile(player, player.getLang(), "data/html/clanHallManager/not_authorized.htm");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("functions"))
			{
				if (val.equalsIgnoreCase("tele"))
				{
					final var html = new NpcHtmlMessage(1);
					final var function = clanHall.getFunction(ClanHall.FUNC_TELEPORT);
					if (function == null)
					{
						html.setFile(player, player.getLang(), "data/html/clanHallManager/chamberlain-nac.htm");
					}
					else
					{
						final int hallid = clanHall.getId();
						switch (hallid)
						{
							case 21 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Resistance" + function.getLvl() + ".htm");
								break;
							case 22 :
							case 23 :
							case 24 :
							case 25 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Gludio" + function.getLvl() + ".htm");
								break;
							case 26 :
							case 27 :
							case 28 :
							case 29 :
							case 30 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Gludin" + function.getLvl() + ".htm");
								break;
							case 31 :
							case 32 :
							case 33 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Dion" + function.getLvl() + ".htm");
								break;
							case 34 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Devastated" + function.getLvl() + ".htm");
								break;
							case 35 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Bandit" + function.getLvl() + ".htm");
								break;
							case 36 :
							case 37 :
							case 38 :
							case 39 :
							case 40 :
							case 41 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Aden" + function.getLvl() + ".htm");
								break;
							case 42 :
							case 43 :
							case 44 :
							case 45 :
							case 46 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Giran" + function.getLvl() + ".htm");
								break;
							case 47 :
							case 48 :
							case 49 :
							case 50 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Goddard" + function.getLvl() + ".htm");
								break;
							case 51 :
							case 52 :
							case 53 :
							case 54 :
							case 55 :
							case 56 :
							case 57 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Rune" + function.getLvl() + ".htm");
								break;
							case 58 :
							case 59 :
							case 60 :
							case 61 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Schuttgart" + function.getLvl() + ".htm");
								break;
							case 62 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Rainbow" + function.getLvl() + ".htm");
								break;
							case 63 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Beast" + function.getLvl() + ".htm");
								break;
							case 64 :
								html.setFile(player, player.getLang(), "data/html/clanHallManager/tele" + "Fortress" + function.getLvl() + ".htm");
								break;
						}
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("item_creation"))
				{
					final var function = clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
					if (function == null)
					{
						final var html = new NpcHtmlMessage(1);
						html.setFile(player, player.getLang(), "data/html/clanHallManager/chamberlain-nac.htm");
						sendHtmlMessage(player, html);
						return;
					}
					
					if (st.countTokens() < 1)
					{
						return;
					}
					
					final int valbuy = Integer.parseInt(st.nextToken()) + (function.getLvl() * 100000);
					showBuyWindow(player, valbuy);
				}
				else if (val.equalsIgnoreCase("support"))
				{
					final var html = new NpcHtmlMessage(1);
					final var function = clanHall.getFunction(ClanHall.FUNC_SUPPORT);
					if (function == null)
					{
						html.setFile(player, player.getLang(), "data/html/clanHallManager/chamberlain-nac.htm");
					}
					else
					{
						html.setFile(player, player.getLang(), "data/html/clanHallManager/support" + function.getLvl() + ".htm");
						html.replace("%mp%", String.valueOf((int) getCurrentMp()));
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("back"))
				{
					showChatWindow(player);
				}
				else
				{
					final var html = new NpcHtmlMessage(1);
					html.setFile(player, player.getLang(), "data/html/clanHallManager/functions.htm");
					var function = clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP);
					if (function != null)
					{
						html.replace("%xp_regen%", String.valueOf(function.getLvl()));
					}
					else
					{
						html.replace("%xp_regen%", "0");
					}
					
					function = clanHall.getFunction(ClanHall.FUNC_RESTORE_HP);
					if (function != null)
					{
						html.replace("%hp_regen%", String.valueOf(function.getLvl()));
					}
					else
					{
						html.replace("%hp_regen%", "0");
					}
					
					function = clanHall.getFunction(ClanHall.FUNC_RESTORE_MP);
					if (function != null)
					{
						html.replace("%mp_regen%", String.valueOf(function.getLvl()));
					}
					else
					{
						html.replace("%mp_regen%", "0");
					}
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manage"))
			{
				if ((player.getClanPrivileges() & Clan.CP_CH_SET_FUNCTIONS) == Clan.CP_CH_SET_FUNCTIONS)
				{
					if (val.equalsIgnoreCase("recovery"))
					{
						if (st.countTokens() >= 1)
						{
							if (clanHall.getOwnerId() == 0)
							{
								player.sendMessage(new ServerMessage("ClanHall.NO_OWNER", player.getLang()).toString());
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("hp_cancel"))
							{
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery hp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("mp_cancel"))
							{
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery mp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("exp_cancel"))
							{
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery exp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_hp"))
							{
								final int percent = Integer.parseInt(st.nextToken());
								final var function = template.getFunction(FunctionType.RESTORE_HP, percent);
								if (function == null)
								{
									return;
								}
								final var price = function.getPrice();
								
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", function.getName(player.getLang()));
								html.replace("%cost%", String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + "</font>)");
								html.replace("%use%", "" + function.getDescr(player.getLang()) + " <font color=00FFFF>" + String.valueOf(function.getPower()) + "%</font>");
								html.replace("%apply%", "recovery hp " + String.valueOf(function.getPower()));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_mp"))
							{
								final int percent = Integer.parseInt(st.nextToken());
								final var function = template.getFunction(FunctionType.RESTORE_MP, percent);
								if (function == null)
								{
									return;
								}
								final var price = function.getPrice();
								
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", function.getName(player.getLang()));
								html.replace("%cost%", String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + "</font>)");
								html.replace("%use%", "" + function.getDescr(player.getLang()) + " <font color=00FFFF>" + String.valueOf(function.getPower()) + "%</font>");
								html.replace("%apply%", "recovery mp " + String.valueOf(function.getPower()));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_exp"))
							{
								final int percent = Integer.parseInt(st.nextToken());
								final var function = template.getFunction(FunctionType.RESTORE_EXP, percent == 0 ? 1 : percent);
								if (function == null)
								{
									return;
								}
								final var price = function.getPrice();
								
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", function.getName(player.getLang()));
								html.replace("%cost%", String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + "</font>)");
								html.replace("%use%", "" + function.getDescr(player.getLang()) + " <font color=\"00FFFF\">" + String.valueOf(function.getPower()) + "%</font>");
								html.replace("%apply%", "recovery exp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("hp"))
							{
								if (st.countTokens() >= 1)
								{
									final int percent = Integer.parseInt(st.nextToken());
									final var function = template.getFunction(FunctionType.RESTORE_HP, percent);
									if (function == null)
									{
										return;
									}
									
									final var html = new NpcHtmlMessage(1);
									html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									final var chFunction = clanHall.getFunction(ClanHall.FUNC_RESTORE_HP);
									if (chFunction != null)
									{
										if (chFunction.getLvl() == percent)
										{
											html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									
									long[] price = new long[]
									{
									        0L, 0L
									};
									
									if (percent == 0)
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
									}
									else
									{
										price = function.getPrice();
									}
									
									if (!clanHall.updateFunctions(player, ClanHall.FUNC_RESTORE_HP, percent, price, function.getDuration(), (chFunction == null)))
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("mp"))
							{
								if (st.countTokens() >= 1)
								{
									final int percent = Integer.parseInt(st.nextToken());
									final var function = template.getFunction(FunctionType.RESTORE_MP, percent);
									if (function == null)
									{
										return;
									}
									
									final var html = new NpcHtmlMessage(1);
									html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									final var chFunction = clanHall.getFunction(ClanHall.FUNC_RESTORE_MP);
									if (chFunction != null)
									{
										if (chFunction.getLvl() == percent)
										{
											html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									
									long[] price = new long[]
									{
									        0L, 0L
									};
									
									if (percent == 0)
									{
											html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
									}
									else
									{
										price = function.getPrice();
									}
									
									if (!clanHall.updateFunctions(player, ClanHall.FUNC_RESTORE_MP, percent, price, function.getDuration(), (chFunction == null)))
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("exp"))
							{
								if (st.countTokens() >= 1)
								{
									final int percent = Integer.parseInt(st.nextToken());
									final var function = template.getFunction(FunctionType.RESTORE_EXP, percent);
									if (function == null)
									{
										return;
									}
									
									final var html = new NpcHtmlMessage(1);
									html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									final var chFunction = clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP);
									if (chFunction != null)
									{
										if (chFunction.getLvl() == percent)
										{
											html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									
									long[] price = new long[]
									{
									        0L, 0L
									};
									
									if (percent == 0)
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
									}
									else
									{
										price = function.getPrice();
									}
									
									if (!clanHall.updateFunctions(player, ClanHall.FUNC_RESTORE_EXP, percent, price, function.getDuration(), (chFunction == null)))
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						
						final var html = new NpcHtmlMessage(1);
						html.setFile(player, player.getLang(), "data/html/clanHallManager/edit_recovery.htm", false);
						final var hpList = template.getFunctions(FunctionType.RESTORE_HP);
						final var mpList = template.getFunctions(FunctionType.RESTORE_MP);
						final var expList = template.getFunctions(FunctionType.RESTORE_EXP);
						String hp_info = "";
						String mp_info = "";
						String exp_info = "";
						for (final var f : hpList)
						{
							hp_info += "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp " + f.getPower() + "\">" + f.getPower() + "%</a>]";
						}
						for (final var f : mpList)
						{
							mp_info += "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp " + f.getPower() + "\">" + f.getPower() + "%</a>]";
						}
						for (final var f : expList)
						{
							exp_info += "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp " + f.getPower() + "\">" + f.getPower() + "%</a>]";
						}
						
						var function = clanHall.getFunction(ClanHall.FUNC_RESTORE_HP);
						if (function != null)
						{
							hp_info = "";
							for (final var f : hpList)
							{
								if (f.getPower() == function.getLvl())
								{
									continue;
								}
								hp_info += "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp " + f.getPower() + "\">" + f.getPower() + "%</a>]";
							}
							final var price = function.getPrice();
							html.replace("%hp_recovery%", String.valueOf(function.getLvl()) + "%</font> (<font color=\"FFAABB\">" + String.valueOf(price[1]) + "</font> " + Util.getItemName(player, (int) price[0]) + ")");
							final var msg = new ServerMessage("ClanHall.WITHDRAW", player.getLang());
							msg.add(format.format(function.getEndTime()));
							html.replace("%hp_period%", msg.toString());
							html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">" + new ServerMessage("ClanHall.OFF", player.getLang()).toString() + "</a>]" + hp_info);
						}
						else
						{
							html.replace("%hp_recovery%", "");
							html.replace("%hp_period%", new ServerMessage("ClanHall.NONE", player.getLang()).toString());
							html.replace("%change_hp%", hp_info);
						}
						
						function = clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP);
						if (function != null)
						{
							exp_info = "";
							for (final var f : expList)
							{
								if (f.getPower() == function.getLvl())
								{
									continue;
								}
								exp_info += "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp " + f.getPower() + "\">" + f.getPower() + "%</a>]";
							}
							final var price = function.getPrice();
							html.replace("%exp_recovery%", String.valueOf(function.getLvl()) + "%</font> (<font color=\"FFAABB\">" + String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + ")");
							final var msg = new ServerMessage("ClanHall.WITHDRAW", player.getLang());
							msg.add(format.format(function.getEndTime()));
							html.replace("%exp_period%", msg.toString());
							html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">" + new ServerMessage("ClanHall.OFF", player.getLang()).toString() + "</a>]" + exp_info);
						}
						else
						{
							html.replace("%exp_recovery%", "");
							html.replace("%exp_period%", new ServerMessage("ClanHall.NONE", player.getLang()).toString());
							html.replace("%change_exp%", exp_info);
						}
						
						function = clanHall.getFunction(ClanHall.FUNC_RESTORE_MP);
						if (function != null)
						{
							mp_info = "";
							for (final var f : mpList)
							{
								if (f.getPower() == function.getLvl())
								{
									continue;
								}
								mp_info += "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp " + f.getPower() + "\">" + f.getPower() + "%</a>]";
							}
							final var price = function.getPrice();
							html.replace("%mp_recovery%", String.valueOf(function.getLvl()) + "%</font> (<font color=\"FFAABB\">" + String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + ")");
							final var msg = new ServerMessage("ClanHall.WITHDRAW", player.getLang());
							msg.add(format.format(function.getEndTime()));
							html.replace("%mp_period%", msg.toString());
							html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">" + new ServerMessage("ClanHall.OFF", player.getLang()).toString() + "</a>]" + mp_info);
						}
						else
						{
							html.replace("%mp_recovery%", "");
							html.replace("%mp_period%", new ServerMessage("ClanHall.NONE", player.getLang()).toString());
							html.replace("%change_mp%", mp_info);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("other"))
					{
						if (st.countTokens() >= 1)
						{
							if (clanHall.getOwnerId() == 0)
							{
								player.sendMessage(new ServerMessage("ClanHall.NO_OWNER", player.getLang()).toString());
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("item_cancel"))
							{
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other item 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("tele_cancel"))
							{
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other tele 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("support_cancel"))
							{
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other support 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_item"))
							{
								final int stage = Integer.parseInt(st.nextToken());
								final var function = template.getFunction(FunctionType.ITEM_CREATE, stage);
								if (function == null)
								{
									return;
								}
								final var price = function.getPrice();
								
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", function.getName(player.getLang()));
								html.replace("%cost%", String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + "</font>)");
								html.replace("%use%", function.getDescr(player.getLang()));
								html.replace("%apply%", "other item " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_support"))
							{
								final int stage = Integer.parseInt(st.nextToken());
								final var function = template.getFunction(FunctionType.SUPPORT, stage);
								if (function == null)
								{
									return;
								}
								final var price = function.getPrice();
								
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", function.getName(player.getLang()));
								html.replace("%cost%", String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + "</font>)");
								html.replace("%use%", function.getDescr(player.getLang()));
								html.replace("%apply%", "other support " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_tele"))
							{
								final int stage = Integer.parseInt(st.nextToken());
								final var function = template.getFunction(FunctionType.TELEPORT, stage);
								if (function == null)
								{
									return;
								}
								final var price = function.getPrice();
								
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", function.getName(player.getLang()));
								html.replace("%cost%", String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + "</font>)");
								html.replace("%use%", function.getDescr(player.getLang()));
								html.replace("%apply%", "other tele " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("item"))
							{
								if (st.countTokens() >= 1)
								{
									if (clanHall.getOwnerId() == 0)
									{
										player.sendMessage(new ServerMessage("ClanHall.NO_OWNER", player.getLang()).toString());
										return;
									}
									
									final int lvl = Integer.parseInt(st.nextToken());
									final var function = template.getFunction(FunctionType.ITEM_CREATE, lvl);
									if (function == null)
									{
										return;
									}
									
									final var html = new NpcHtmlMessage(1);
									html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									final var chFunction = clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
									if (chFunction != null)
									{
										if (chFunction.getLvl() == lvl)
										{
											html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", "[" + String.valueOf(lvl) + "]");
											sendHtmlMessage(player, html);
											return;
										}
									}
									
									long[] price = new long[]
									{
									        0L, 0L
									};
									
									if (lvl == 0)
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
									}
									else
									{
										price = function.getPrice();
									}
									
									if (!clanHall.updateFunctions(player, ClanHall.FUNC_ITEM_CREATE, lvl, price, function.getDuration(), (chFunction == null)))
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("tele"))
							{
								if (st.countTokens() >= 1)
								{
									final int lvl = Integer.parseInt(st.nextToken());
									final var function = template.getFunction(FunctionType.TELEPORT, lvl);
									if (function == null)
									{
										return;
									}
									
									final var html = new NpcHtmlMessage(1);
									html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									final var chFunction = clanHall.getFunction(ClanHall.FUNC_TELEPORT);
									if (chFunction != null)
									{
										if (chFunction.getLvl() == lvl)
										{
											html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", "[" + String.valueOf(lvl) + "]");
											sendHtmlMessage(player, html);
											return;
										}
									}
									
									long[] price = new long[]
									{
									        0L, 0L
									};
									
									if (lvl == 0)
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
									}
									else
									{
										price = function.getPrice();
									}
									
									if (!clanHall.updateFunctions(player, ClanHall.FUNC_TELEPORT, lvl, price, function.getDuration(), (chFunction == null)))
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("support"))
							{
								if (st.countTokens() >= 1)
								{
									final int lvl = Integer.parseInt(st.nextToken());
									final var function = template.getFunction(FunctionType.SUPPORT, lvl);
									if (function == null)
									{
										return;
									}
									
									final var html = new NpcHtmlMessage(1);
									html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									final var chFunction = clanHall.getFunction(ClanHall.FUNC_SUPPORT);
									if (chFunction != null)
									{
										if (chFunction.getLvl() == lvl)
										{
											html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", "[" + String.valueOf(lvl) + "]");
											sendHtmlMessage(player, html);
											return;
										}
									}
									
									long[] price = new long[]
									{
									        0L, 0L
									};
									
									if (lvl == 0)
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
									}
									else
									{
										price = function.getPrice();
									}
									
									if (!clanHall.updateFunctions(player, ClanHall.FUNC_SUPPORT, lvl, price, function.getDuration(), (chFunction == null)))
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						final var html = new NpcHtmlMessage(1);
						html.setFile(player, player.getLang(), "data/html/clanHallManager/edit_other.htm", false);
						
						final var teleList = template.getFunctions(FunctionType.TELEPORT);
						final var supportList = template.getFunctions(FunctionType.SUPPORT);
						final var itemList = template.getFunctions(FunctionType.ITEM_CREATE);
						String tele_info = "";
						String support_info = "";
						String item_info = "";
						for (final var f : teleList)
						{
							tele_info += "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele " + f.getPower() + "\">" + new ServerMessage("ClanHall.LVL", player.getLang()).toString() + " " + f.getPower() + "</a>]";
						}
						for (final var f : supportList)
						{
							support_info += "[<a action=\"bypass -h npc_%objectId%_manage other edit_support " + f.getPower() + "\">" + new ServerMessage("ClanHall.LVL", player.getLang()).toString() + " " + f.getPower() + "</a>]";
						}
						for (final var f : itemList)
						{
							item_info += "[<a action=\"bypass -h npc_%objectId%_manage other edit_item " + f.getPower() + "\">" + new ServerMessage("ClanHall.LVL", player.getLang()).toString() + " " + f.getPower() + "</a>]";
						}
						
						var function = clanHall.getFunction(ClanHall.FUNC_TELEPORT);
						if (function != null)
						{
							tele_info = "";
							for (final var f : teleList)
							{
								if (f.getPower() == function.getLvl())
								{
									continue;
								}
								tele_info += "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele " + f.getPower() + "\">" + new ServerMessage("ClanHall.LVL", player.getLang()).toString() + " " + f.getPower() + "</a>]";
							}
							final var price = function.getPrice();
							html.replace("%tele%", "[" + String.valueOf(function.getLvl()) + "]</font> (<font color=\"FFAABB\">" + String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + ")");
							final var msg = new ServerMessage("ClanHall.WITHDRAW", player.getLang());
							msg.add(format.format(function.getEndTime()));
							html.replace("%tele_period%", msg.toString());
							html.replace("%change_tele%", "[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">" + new ServerMessage("ClanHall.OFF", player.getLang()).toString() + "</a>]" + tele_info);
						}
						else
						{
							html.replace("%tele%", "");
							html.replace("%tele_period%", new ServerMessage("ClanHall.NONE", player.getLang()).toString());
							html.replace("%change_tele%", tele_info);
						}
						
						function = clanHall.getFunction(ClanHall.FUNC_SUPPORT);
						if (function != null)
						{
							support_info = "";
							for (final var f : supportList)
							{
								if (f.getPower() == function.getLvl())
								{
									continue;
								}
								support_info += "[<a action=\"bypass -h npc_%objectId%_manage other edit_support " + f.getPower() + "\">" + new ServerMessage("ClanHall.LVL", player.getLang()).toString() + " " + f.getPower() + "</a>]";
							}
							final var price = function.getPrice();
							html.replace("%support%", "[" + String.valueOf(function.getLvl()) + "]</font> (<font color=\"FFAABB\">" + String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + ")");
							final var msg = new ServerMessage("ClanHall.WITHDRAW", player.getLang());
							msg.add(format.format(function.getEndTime()));
							html.replace("%support_period%", msg.toString());
							html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">" + new ServerMessage("ClanHall.OFF", player.getLang()).toString() + "</a>]" + support_info);
						}
						else
						{
							html.replace("%support%", "");
							html.replace("%support_period%", new ServerMessage("ClanHall.NONE", player.getLang()).toString());
							html.replace("%change_support%", support_info);
						}
						
						function = clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
						if (function != null)
						{
							item_info = "";
							for (final var f : itemList)
							{
								if (f.getPower() == function.getLvl())
								{
									continue;
								}
								item_info += "[<a action=\"bypass -h npc_%objectId%_manage other edit_item " + f.getPower() + "\">" + new ServerMessage("ClanHall.LVL", player.getLang()).toString() + " " + f.getPower() + "</a>]";
							}
							final var price = function.getPrice();
							html.replace("%item%", "[" + String.valueOf(function.getLvl()) + "]</font> (<font color=\"FFAABB\">" + String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + ")");
							final var msg = new ServerMessage("ClanHall.WITHDRAW", player.getLang());
							msg.add(format.format(function.getEndTime()));
							html.replace("%item_period%", msg.toString());
							html.replace("%change_item%", "[<a action=\"bypass -h npc_%objectId%_manage other item_cancel\">" + new ServerMessage("ClanHall.OFF", player.getLang()).toString() + "</a>]" + item_info);
						}
						else
						{
							html.replace("%item%", "");
							html.replace("%item_period%", new ServerMessage("ClanHall.NONE", player.getLang()).toString());
							html.replace("%change_item%", item_info);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("deco") && !clanHall.isSiegableHall())
					{
						if (st.countTokens() >= 1)
						{
							if (clanHall.getOwnerId() == 0)
							{
								player.sendMessage(new ServerMessage("ClanHall.NO_OWNER", player.getLang()).toString());
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("curtains_cancel"))
							{
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "deco curtains 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("fixtures_cancel"))
							{
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "deco fixtures 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_curtains"))
							{
								final int stage = Integer.parseInt(st.nextToken());
								final var function = template.getFunction(FunctionType.DECO_CURTAINS, stage);
								if (function == null)
								{
									return;
								}
								final var price = function.getPrice();
								
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", function.getName(player.getLang()));
								html.replace("%cost%", String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + "</font>)");
								html.replace("%use%", function.getDescr(player.getLang()));
								html.replace("%apply%", "deco curtains " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_fixtures"))
							{
								final int stage = Integer.parseInt(st.nextToken());
								final var function = template.getFunction(FunctionType.DECO_CURTAINS, stage);
								if (function == null)
								{
									return;
								}
								final var price = function.getPrice();
								
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", function.getName(player.getLang()));
								html.replace("%cost%", String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + "</font>)");
								html.replace("%use%", function.getDescr(player.getLang()));
								html.replace("%apply%", "deco fixtures " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("curtains"))
							{
								if (st.countTokens() >= 1)
								{
									final int lvl = Integer.parseInt(st.nextToken());
									final var function = template.getFunction(FunctionType.DECO_CURTAINS, lvl);
									if (function == null)
									{
										return;
									}
									
									final var html = new NpcHtmlMessage(1);
									html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									final var chFunction = clanHall.getFunction(ClanHall.FUNC_DECO_CURTAINS);
									if (chFunction != null)
									{
										if (chFunction.getLvl() == lvl)
										{
											html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", "[" + String.valueOf(lvl) + "]");
											sendHtmlMessage(player, html);
											return;
										}
									}
									
									long[] price = new long[]
									{
									        0L, 0L
									};
									
									if (lvl == 0)
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
									}
									else
									{
										price = function.getPrice();
									}
									
									if (!clanHall.updateFunctions(player, ClanHall.FUNC_DECO_CURTAINS, lvl, price, function.getDuration(), (chFunction == null)))
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("fixtures"))
							{
								if (st.countTokens() >= 1)
								{
									final int lvl = Integer.parseInt(st.nextToken());
									final var function = template.getFunction(FunctionType.DECO_FRONTPLATEFORM, lvl);
									if (function == null)
									{
										return;
									}
									
									final var html = new NpcHtmlMessage(1);
									html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									final var chFunction = clanHall.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM);
									if (chFunction != null)
									{
										if (chFunction.getLvl() == lvl)
										{
											html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", "[" + String.valueOf(lvl) + "]");
											sendHtmlMessage(player, html);
											return;
										}
									}
									
									long[] price = new long[]
									{
									        0L, 0L
									};
									
									if (lvl == 0)
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
									}
									else
									{
										price = function.getPrice();
									}
									
									if (!clanHall.updateFunctions(player, ClanHall.FUNC_DECO_FRONTPLATEFORM, lvl, price, function.getDuration(), (chFunction == null)))
									{
										html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										revalidateDeco(player);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						
						final var curtainsList = template.getFunctions(FunctionType.DECO_CURTAINS);
						final var fixturesList = template.getFunctions(FunctionType.DECO_FRONTPLATEFORM);
						String curtains_info = "";
						String fixtures_info = "";
						for (final var f : curtainsList)
						{
							curtains_info += "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains " + f.getPower() + "\">" + new ServerMessage("ClanHall.LVL", player.getLang()).toString() + " " + f.getPower() + "</a>]";
						}
						for (final var f : fixturesList)
						{
							fixtures_info += "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures " + f.getPower() + "\">" + new ServerMessage("ClanHall.LVL", player.getLang()).toString() + " " + f.getPower() + "</a>]";
						}
						
						final var html = new NpcHtmlMessage(1);
						html.setFile(player, player.getLang(), "data/html/clanHallManager/deco.htm", false);
						var function = clanHall.getFunction(ClanHall.FUNC_DECO_CURTAINS);
						if (function != null)
						{
							curtains_info = "";
							for (final var f : curtainsList)
							{
								if (f.getPower() == function.getLvl())
								{
									continue;
								}
								curtains_info += "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains " + f.getPower() + "\">" + new ServerMessage("ClanHall.LVL", player.getLang()).toString() + " " + f.getPower() + "</a>]";
							}
							final var price = function.getPrice();
							html.replace("%curtain%", "[" + String.valueOf(function.getLvl()) + "]</font> (<font color=\"FFAABB\">" + String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + ")");
							final var msg = new ServerMessage("ClanHall.WITHDRAW", player.getLang());
							msg.add(format.format(function.getEndTime()));
							html.replace("%curtain_period%", msg.toString());
							html.replace("%change_curtain%", "[<a action=\"bypass -h npc_%objectId%_manage deco curtains_cancel\">" + new ServerMessage("ClanHall.OFF", player.getLang()).toString() + "</a>]" + curtains_info);
						}
						else
						{
							html.replace("%curtain%", "");
							html.replace("%curtain_period%", new ServerMessage("ClanHall.NONE", player.getLang()).toString());
							html.replace("%change_curtain%", curtains_info);
						}
						
						function = clanHall.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM);
						if (function != null)
						{
							fixtures_info = "";
							for (final var f : fixturesList)
							{
								if (f.getPower() == function.getLvl())
								{
									continue;
								}
								fixtures_info += "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures " + f.getPower() + "\">" + new ServerMessage("ClanHall.LVL", player.getLang()).toString() + " " + f.getPower() + "</a>]";
							}
							final var price = function.getPrice();
							html.replace("%fixture%", "[" + String.valueOf(function.getLvl()) + "]</font> (<font color=\"FFAABB\">" + String.valueOf(price[1]) + "</font> <font color=LEVEL>" + Util.getItemName(player, (int) price[0]) + ")");
							final var msg = new ServerMessage("ClanHall.WITHDRAW", player.getLang());
							msg.add(format.format(function.getEndTime()));
							html.replace("%fixture_period%", msg.toString());
							html.replace("%change_fixture%", "[<a action=\"bypass -h npc_%objectId%_manage deco fixtures_cancel\">" + new ServerMessage("ClanHall.OFF", player.getLang()).toString() + "</a>]" + fixtures_info);
						}
						else
						{
							html.replace("%fixture%", "");
							html.replace("%fixture_period%", new ServerMessage("ClanHall.NONE", player.getLang()).toString());
							html.replace("%change_fixture%", fixtures_info);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("back"))
					{
						showChatWindow(player);
					}
					else
					{
						final var html = new NpcHtmlMessage(1);
						html.setFile(player, player.getLang(), clanHall.isSiegableHall() ? "data/html/clanHallManager/manage_siegable.htm" : "data/html/clanHallManager/manage.htm");
						sendHtmlMessage(player, html);
					}
				}
				else
				{
					final var html = new NpcHtmlMessage(1);
					html.setFile(player, player.getLang(), "data/html/clanHallManager/not_authorized.htm");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("support"))
			{
				if (player.isCursedWeaponEquipped())
				{
					player.sendMessage("The wielder of a cursed weapon cannot receive outside heals or buffs");
					return;
				}
				setTarget(player);
				Skill skill;
				if (val.isEmpty())
				{
					return;
				}

				try
				{
					final int skill_id = Integer.parseInt(val);
					try
					{
						int skill_lvl = 0;
						if (st.countTokens() >= 1)
						{
							skill_lvl = Integer.parseInt(st.nextToken());
						}
						skill = SkillsParser.getInstance().getInfo(skill_id, skill_lvl);
						if (skill.getSkillType() == SkillType.SUMMON)
						{
							player.doSimultaneousCast(skill);
						}
						else
						{
							final int mpCost = skill.getMpConsume() + skill.getMpInitialConsume();
							if (getCurrentMp() >= mpCost)
							{
								doCast(skill);
							}
							else
							{
								final var html = new NpcHtmlMessage(1);
								html.setFile(player, player.getLang(), "data/html/clanHallManager/support-no_mana.htm");
								html.replace("%mp%", String.valueOf((int) getCurrentMp()));
								sendHtmlMessage(player, html);
								return;
							}
						}
						
						final var function = clanHall.getFunction(ClanHall.FUNC_SUPPORT);
						if (function == null || function.getLvl() == 0)
						{
							return;
						}
						
						final var html = new NpcHtmlMessage(1);
						html.setFile(player, player.getLang(), "data/html/clanHallManager/support-done.htm");
						html.replace("%mp%", String.valueOf((int) getCurrentMp()));
						sendHtmlMessage(player, html);
					}
					catch (final Exception _)
					{
					}
				}
				catch (final Exception _)
				{
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list_back"))
			{
				final var html = new NpcHtmlMessage(1);
				String file = "data/html/clanHallManager/chamberlain-" + getId() + ".htm";
				if (!HtmCache.getInstance().isLoadable(file))
				{
					file = "data/html/clanHallManager/chamberlain.htm";
				}
				html.setFile(player, player.getLang(), file);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", "" + getTitle(player.getLang()) + " " + getName(player.getLang()));
				sendHtmlMessage(player, html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("support_back"))
			{
				final var html = new NpcHtmlMessage(1);
				final var function = clanHall.getFunction(ClanHall.FUNC_SUPPORT);
				if (function == null || function.getLvl() == 0)
				{
					return;
				}
				html.setFile(player, player.getLang(), "data/html/clanHallManager/support" + function.getLvl() + ".htm");
				html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
				sendHtmlMessage(player, html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("goto"))
			{
				final int whereTo = Integer.parseInt(val);
				doTeleport(player, whereTo);
				return;
			}
		}
		super.onBypassFeedback(player, command);
	}

	private void sendHtmlMessage(Player player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getId()));
		html.replace("%npcname%", "" + getTitle(player.getLang()) + " " + getName(player.getLang()));
		player.sendPacket(html);
	}

	@Override
	public void showChatWindow(Player player)
	{
		final var clanHall = getClanHall();
		if (clanHall == null)
		{
			return;
		}
		
		player.sendActionFailed();
		String filename = "data/html/clanHallManager/chamberlain-no.htm";

		final int condition = validateCondition(clanHall, player);
		if (condition == COND_OWNER)
		{
			filename = "data/html/clanHallManager/chamberlain-" + getId() + ".htm";
			if (!HtmCache.getInstance().isLoadable(filename))
			{
				filename = "data/html/clanHallManager/chamberlain.htm";
			}
		}
		else if (condition == COND_OWNER_FALSE)
		{
			filename = "data/html/clanHallManager/chamberlain-of.htm";
		}
		final var html = new NpcHtmlMessage(1);
		html.setFile(player, player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", "" + getTitle(player.getLang()) + " " + getName(player.getLang()));
		html.replace("%npcId%", String.valueOf(getId()));
		player.sendPacket(html);
	}

	protected int validateCondition(ClanHall clanHall, Player player)
	{
		if (clanHall == null)
		{
			return COND_ALL_FALSE;
		}
		if (player.canOverrideCond(PcCondOverride.CLANHALL_CONDITIONS))
		{
			return COND_OWNER;
		}
		if (player.getClan() != null)
		{
			if (clanHall.getOwnerId() == player.getClanId())
			{
				return COND_OWNER;
			}
			return COND_OWNER_FALSE;
		}
		return COND_ALL_FALSE;
	}

	public final ClanHall getClanHall()
	{
		if (_clanHallId < 0)
		{
			ClanHall temp = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
			if (temp == null)
			{
				temp = CHSiegeManager.getInstance().getNearbyClanHall(this);
			}

			if (temp != null)
			{
				_clanHallId = temp.getId();
			}

			if (_clanHallId < 0)
			{
				return null;
			}
		}
		return ClanHallManager.getInstance().getClanHallById(_clanHallId);
	}

	private void doTeleport(Player player, int val)
	{
		final var list = TeleLocationParser.getInstance().getTemplate(val);
		if (list != null)
		{
			if (player.isCombatFlagEquipped())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
				return;
			}
			else if (player.destroyItemByItemId("Teleport", list.getId(), list.getPrice(), this, true))
			{
				player.teleToLocation(list.getLocation(), true, ReflectionManager.DEFAULT);
			}
		}
		player.sendActionFailed();
	}

	private void revalidateDeco(Player player)
	{
		final var ch = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
		if (ch == null)
		{
			return;
		}
		final var bl = new AgitDecoInfo(ch);
		player.sendPacket(bl);
	}
}