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

import java.util.StringTokenizer;

import gameserver.Config;
import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.AdminParser;
import gameserver.handler.admincommandhandlers.AdminCommandHandler;
import gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import gameserver.handler.bypasshandlers.BypassHandler;
import gameserver.handler.bypasshandlers.IBypassHandler;
import gameserver.handler.communityhandlers.CommunityBoardHandler;
import gameserver.handler.communityhandlers.ICommunityBoardHandler;
import gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import gameserver.model.GameObject;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.entity.Hero;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ConfirmDlg;
import gameserver.network.serverpackets.CreatureSay;
import gameserver.network.serverpackets.ExAutoSoulShot;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.GMAudit;
import gameserver.utils.Util;

public final class RequestBypassToServer extends GameClientPacket
{
	private String _command;

	@Override
	protected void readImpl()
	{
		_command = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (_command.isEmpty())
		{
			activeChar.logout();
			return;
		}
		
		_command = activeChar.isDecodedBypass(_command);
		if (_command == null)
		{
			return;
		}

		try
		{
			if (_command.startsWith("admin_"))
			{
				final String command = _command.split(" ")[0];

				final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);

				if (ach == null)
				{
					if (activeChar.isGM())
					{
						activeChar.sendMessage("The command " + command.substring(6) + " does not exist!");
					}
					_log.warn(activeChar + " requested not registered admin command '" + command + "'");
					return;
				}

				if (!AdminParser.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					_log.warn("Character " + activeChar.getName(null) + " tried to use admin command " + command + ", without proper access level!");
					return;
				}

				if (AdminParser.getInstance().requireConfirm(command))
				{
					activeChar.setAdminConfirmCmd(_command);
					final ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
					dlg.addString("Are you sure you want execute command " + _command.substring(6) + " ?");
					activeChar.sendPacket(dlg);
				}
				else
				{
					if (Config.GMAUDIT)
					{
						GMAudit.auditGMAction(activeChar.getName(null) + " [" + activeChar.getObjectId() + "]", _command, (activeChar.getTarget() != null ? activeChar.getTarget().getName(null) : "no-target"));
					}
					ach.useAdminCommand(_command, activeChar);
				}
			}
			else if (_command.startsWith("."))
			{
				final String command = _command.substring(1).split(" ")[0];
				final String params = _command.substring(1).split(" ").length > 1 ? _command.substring(1).split(" ")[1] : "";
				final IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler(command);
				if (vch != null && !Config.DISABLE_VOICE_BYPASSES.contains(command))
				{
					vch.useVoicedCommand(command, activeChar, params);
				}
			}
			else if (_command.startsWith("voiced_"))
			{
				String command = _command.split(" ")[0];
				command = command.substring(7);
				final IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler(command);
				if (vch != null)
				{
					if (!Config.DISABLE_VOICE_BYPASSES.contains(command))
					{
						vch.useVoicedCommand(_command.substring(7), activeChar, null);
					}
				}
			}
			else if (_command.equals("come_here") && activeChar.isGM())
			{
				comeHere(activeChar);
			}
			else if (_command.startsWith("npc_"))
			{
				final int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(4, endOfId);
				}
				else
				{
					id = _command.substring(4);
				}
				if (Util.isDigit(id))
				{
					final Npc object = GameObjectsStorage.getNpc(Integer.parseInt(id));
					if ((object != null) && (endOfId > 0) && activeChar.isInsideRadius(object, Npc.INTERACTION_DISTANCE, false, false))
					{
						final var cmd = _command.substring(endOfId + 1);
						if (!Config.DISABLE_NPC_BYPASSES.contains(cmd.split(" ")[0]))
						{
							object.onBypassFeedback(activeChar, cmd);
						}
					}
				}
				activeChar.sendActionFailed();
			}
			else if (_command.startsWith("item_"))
			{
				final int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(5, endOfId);
				}
				else
				{
					id = _command.substring(5);
				}
				try
				{
					final ItemInstance item = activeChar.getInventory().getItemByObjectId(Integer.parseInt(id));
					if ((item != null) && (endOfId > 0))
					{
						item.onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
					activeChar.sendActionFailed();
				}
				catch (final NumberFormatException _)
				{
				}
			}
			else if (_command.startsWith("manor_menu_select"))
			{
				final IBypassHandler manor = BypassHandler.getInstance().getHandler("manor_menu_select");
				if (manor != null)
				{
					manor.useBypass(_command, activeChar, null);
				}
			}
			else if (_command.startsWith("_bbs"))
			{
				if (_command.equalsIgnoreCase("_bbshome"))
				{
					final String bypass = !_command.equalsIgnoreCase(Config.BBS_HOME_PAGE) ? Config.BBS_HOME_PAGE : _command;
					final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
					if (handler != null)
					{
						handler.onBypassCommand(bypass, activeChar);
					}
				}
				else if (_command.equalsIgnoreCase("_bbsgetfav"))
				{
					final String bypass = !_command.equalsIgnoreCase(Config.BBS_FAVORITE_PAGE) ? Config.BBS_FAVORITE_PAGE : _command;
					final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
					if (handler != null)
					{
						handler.onBypassCommand(bypass, activeChar);
					}
				}
				else if (_command.equalsIgnoreCase("_bbslink"))
				{
					final String bypass = !_command.equalsIgnoreCase(Config.BBS_LINK_PAGE) ? Config.BBS_LINK_PAGE : _command;
					final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
					if (handler != null)
					{
						handler.onBypassCommand(bypass, activeChar);
					}
				}
				else if (_command.equalsIgnoreCase("_bbsloc"))
				{
					final String bypass = !_command.equalsIgnoreCase(Config.BBS_REGION_PAGE) ? Config.BBS_REGION_PAGE : _command;
					final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
					if (handler != null)
					{
						handler.onBypassCommand(bypass, activeChar);
					}
				}
				else if (_command.equalsIgnoreCase("_bbsclan"))
				{
					final String bypass = !_command.equalsIgnoreCase(Config.BBS_CLAN_PAGE) ? Config.BBS_CLAN_PAGE : _command;
					final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
					if (handler != null)
					{
						handler.onBypassCommand(bypass, activeChar);
					}
				}
				else if (_command.equalsIgnoreCase("_bbsmemo"))
				{
					final String bypass = !_command.equalsIgnoreCase(Config.BBS_MEMO_PAGE) ? Config.BBS_MEMO_PAGE : _command;
					final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
					if (handler != null)
					{
						handler.onBypassCommand(bypass, activeChar);
					}
				}
				else if (_command.equalsIgnoreCase("_bbsaddfav"))
				{
					final String bypass = !_command.equalsIgnoreCase(Config.BBS_ADDFAV_PAGE) ? Config.BBS_ADDFAV_PAGE : _command;
					final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
					if (handler != null)
					{
						handler.onBypassCommand(bypass, activeChar);
					}
				}
				else
				{
					final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(_command);
					if (handler != null)
					{
						handler.onBypassCommand(_command, activeChar);
					}
				}
			}
			else if (_command.startsWith("bbs"))
			{
				final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(_command);
				if (handler != null)
				{
					handler.onBypassCommand(_command, activeChar);
				}
			}
			else if (_command.startsWith("_friendlist_0_"))
			{
				if (_command.equalsIgnoreCase("_friendlist_0_"))
				{
					final String bypass = !_command.equalsIgnoreCase(Config.BBS_FRIENDS_PAGE) ? Config.BBS_FRIENDS_PAGE : _command;
					final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
					if (handler != null)
					{
						handler.onBypassCommand(bypass, activeChar);
					}
				}
			}
			else if (_command.startsWith("_maillist_0_1_0_"))
			{
				if (_command.equalsIgnoreCase("_maillist_0_1_0_"))
				{
					final String bypass = !_command.equalsIgnoreCase(Config.BBS_MAIL_PAGE) ? Config.BBS_MAIL_PAGE : _command;
					final ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
					if (handler != null)
					{
						handler.onBypassCommand(bypass, activeChar);
					}
				}
			}
			else if (_command.startsWith("Quest "))
			{
				final String p = _command.substring(6).trim();
				final int idx = p.indexOf(' ');
				if (idx < 0)
				{
					activeChar.processQuestEvent(p, "");
				}
				else
				{
					activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
				}
			}
			else if (_command.startsWith("_match"))
			{
				final String params = _command.substring(_command.indexOf("?") + 1);
				final StringTokenizer st = new StringTokenizer(params, "&");
				final int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				final int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				final int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroFights(activeChar, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("_diary"))
			{
				final String params = _command.substring(_command.indexOf("?") + 1);
				final StringTokenizer st = new StringTokenizer(params, "&");
				final int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				final int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				final int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("_olympiad?command"))
			{
				final int arenaId = Integer.parseInt(_command.split("=")[2]);
				final IBypassHandler handler = BypassHandler.getInstance().getHandler("arenachange");
				if (handler != null)
				{
					handler.useBypass("arenachange " + (arenaId - 1), activeChar, null);
				}
			}
			else if (_command.startsWith("SomikInterface"))
			{
				if (_command.startsWith("SomikInterface_RequestSettings"))
				{
					final String[] CommandTokens = _command.split("_");
					final String LastCommandToken = CommandTokens[CommandTokens.length - 1];
					if (LastCommandToken != null && !LastCommandToken.isEmpty())
					{
						activeChar.sendPacket(new CreatureSay(0, 13, activeChar.getName(null), Config.INTERFACE_SETTINGS_1 + " Settings=" + LastCommandToken + " "));
						activeChar.sendPacket(new CreatureSay(0, 13, activeChar.getName(null), Config.INTERFACE_SETTINGS_2 + " Settings=" + LastCommandToken + " "));
					}
				}
			}
			else if (_command.startsWith("AutoShotUse"))
			{
				final String[] cmd = _command.split("_");
				
				final int shotId = Integer.parseInt(cmd[1]);
				final int act = Integer.parseInt(cmd[2]);
				
				if ((activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_NONE) && (activeChar.getActiveRequester() == null) && !activeChar.isDead())
				{
					final var item = activeChar.getInventory().getItemByItemId(shotId);
					if (item == null)
					{
						return;
					}
					
					if (act == 1)
					{
						if (!activeChar.getInventory().canManipulateWithItemId(item.getId()))
						{
							activeChar.sendMessage("Cannot use this item.");
							return;
						}
						
						if (((shotId >= 6535) && (shotId <= 6540)) && !Config.ALLOW_AUTO_FISH_SHOTS)
						{
							return;
						}
						
						if ((shotId < 6535) || (shotId > 6540))
						{
							if ((shotId == 6645) || (shotId == 6646) || (shotId == 6647) || (shotId == 20332) || (shotId == 20333) || (shotId == 20334))
							{
								if (activeChar.hasSummon())
								{
									if (item.getEtcItem().getHandlerName().equals("BeastSoulShot"))
									{
										if (activeChar.getSummon().getSoulShotsPerHit() > item.getCount())
										{
											activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
											return;
										}
									}
									else
									{
										if (activeChar.getSummon().getSpiritShotsPerHit() > item.getCount())
										{
											activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET);
											return;
										}
									}
									activeChar.addAutoSoulShot(shotId);
									activeChar.sendPacket(new ExAutoSoulShot(shotId, act));
									
									final var sm = SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
									sm.addItemName(item);
									activeChar.sendPacket(sm);
									
									activeChar.rechargeShots(true, true);
									activeChar.getSummon().rechargeShots(true, true);
								}
								else
								{
									activeChar.sendPacket(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
								}
							}
							else
							{
								activeChar.addAutoSoulShot(shotId);
								activeChar.sendPacket(new ExAutoSoulShot(shotId, act));
								final var sm = SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
								sm.addItemName(item);
								activeChar.sendPacket(sm);
								
								activeChar.rechargeShots(true, true);
							}
						}
					}
					else if (act == 0)
					{
						activeChar.removeAutoSoulShot(shotId);
						activeChar.sendPacket(new ExAutoSoulShot(shotId, act));
						
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
						sm.addItemName(item);
						activeChar.sendPacket(sm);
					}
				}
			}
			else
			{
				final IBypassHandler handler = BypassHandler.getInstance().getHandler(_command);
				if (handler != null)
				{
					handler.useBypass(_command, activeChar, null);
				}
			}
		}
		catch (final Exception e)
		{
			if (activeChar.isGM())
			{
				_log.warn(getClient() + " sent bad RequestBypassToServer: \"" + _command + "\"", e);
				final StringBuilder sb = new StringBuilder(200);
				sb.append("<html><body>");
				sb.append("Bypass error: " + e + "<br1>");
				sb.append("Bypass command: " + _command + "<br1>");
				sb.append("StackTrace:<br1>");
				for (final StackTraceElement ste : e.getStackTrace())
				{
					sb.append(ste.toString() + "<br1>");
				}
				sb.append("</body></html>");

				final NpcHtmlMessage msg = new NpcHtmlMessage(0, 12807);
				msg.setHtml(activeChar, sb.toString());
				activeChar.sendPacket(msg);
			}
		}
	}

	private static void comeHere(Player activeChar)
	{
		final GameObject obj = activeChar.getTarget();
		if (obj == null)
		{
			return;
		}
		if (obj instanceof Npc temp)
		{
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.MOVING, activeChar.getLocation(), 0);
		}
	}
}