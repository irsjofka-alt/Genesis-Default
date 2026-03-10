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

import gameserver.Config;
import gameserver.data.htm.HtmCache;
import gameserver.model.actor.Player;
import gameserver.utils.HtmlUtil;

public final class ExNpcQuestHtmlMessage extends GameServerPacket
{
	private final int _npcObjId;
	private String _html;
	private int _questId = 0;

	public ExNpcQuestHtmlMessage(int npcObjId, int questId)
	{
		_npcObjId = npcObjId;
		_questId = questId;
	}
	
	public void setHtml(Player player, String text)
	{
		if (!text.contains("<html>"))
		{
			text = "<html><body>" + text + "</body></html>";
		}
		
		if (Config.ALLOW_CUSTOM_BUTTONS)
		{
			text = HtmlUtil.switchEmudevButtons(text);
		}
		_html = text;
	}
	
	public void setHtml(Player player, String text, boolean isSwitch)
	{
		if (!text.contains("<html>"))
		{
			text = "<html><body>" + text + "</body></html>";
		}
		
		if (Config.ALLOW_CUSTOM_BUTTONS)
		{
			text = HtmlUtil.switchEmudevButtons(text);
		}
		_html = text;
	}

	public boolean setFile(Player player, String prefix, String path)
	{
		if (prefix == null)
		{
			prefix = "en";
		}
		
		String oriPath = path;
		if (prefix != null)
		{
			if (path.contains("html/"))
			{
				path = path.replace("html/", "html/" + prefix + "/");
				oriPath = oriPath.replace("html/", "html/en/");
			}
		}
		
		String content = HtmCache.getInstance().getHtm(player, path);
		if (content == null && !oriPath.equals(path))
		{
			content = HtmCache.getInstance().getHtm(player, oriPath);
		}
		
		if (content == null)
		{
			setHtml(player, "<html><body>My Text is missing:<br>" + path + "</body></html>");
			_log.warn("missing html page " + path);
			return false;
		}
		setHtml(player, content);
		return true;
	}

	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
	}
	
	public void replace(String pattern, long value)
	{
		replace(pattern, String.valueOf(value));
	}
	
	public void replace2(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value);
	}

	@Override
	protected final void writeImpl()
	{
		final var player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		_html = _html.replaceAll("\t", "");
		_html = _html.replaceAll("\r\n", "");
		_html = _html.replaceAll("</html> ", "</html>");
		_html = player.getBypassStorage().encodeBypasses(_html, false);
		writeD(_npcObjId);
		writeS(_html);
		writeD(_questId);
	}
}