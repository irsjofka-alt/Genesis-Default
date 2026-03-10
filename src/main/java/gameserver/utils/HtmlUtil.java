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
package gameserver.utils;

import org.apache.commons.lang3.StringUtils;

import gameserver.network.NpcStringId;

public class HtmlUtil
{
	public static final String PREV_BUTTON = "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
	public static final String NEXT_BUTTON = "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
	
	public static String getCpGauge(int width, long current, long max, boolean displayAsPercentage, boolean displayAmount)
	{
		return getGauge(width, current, max, displayAsPercentage, displayAmount, "L2UI_CT1.Gauges.Gauge_DF_Large_CP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_CP_Center", 17, -13);
	}
	
	public static String getHpGauge(int width, long current, long max, boolean displayAsPercentage, boolean displayAmount)
	{
		return getGauge(width, current, max, displayAsPercentage, displayAmount, "L2UI_CT1.Gauges.Gauge_DF_Large_HP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_HP_Center", 21, -13);
	}
	
	public static String getHpWarnGauge(int width, long current, long max, boolean displayAsPercentage, boolean displayAmount)
	{
		return getGauge(width, current, max, displayAsPercentage, displayAmount, "L2UI_CT1.Gauges.Gauge_DF_Large_HPWarn_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_HPWarn_Center", 17, -13);
	}
	
	public static String getHpFillGauge(int width, long current, long max, boolean displayAsPercentage, boolean displayAmount)
	{
		return getGauge(width, current, max, displayAsPercentage, displayAmount, "L2UI_CT1.Gauges.Gauge_DF_Large_HPFill_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_HPFill_Center", 17, -13);
	}
	
	public static String getMpGauge(int width, long current, long max, boolean displayAsPercentage, boolean displayAmount)
	{
		return getGauge(width, current, max, displayAsPercentage, displayAmount, "L2UI_CT1.Gauges.Gauge_DF_Large_MP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_MP_Center", 17, -13);
	}
	
	public static String getExpGauge(int width, long current, long max, boolean displayAsPercentage, boolean displayAmount)
	{
		return getGauge(width, current, max, displayAsPercentage, displayAmount, "L2UI_CT1.Gauges.Gauge_DF_Large_EXP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_EXP_Center", 17, -13);
	}
	
	public static String getFoodGauge(int width, long current, long max, boolean displayAsPercentage, boolean displayAmount)
	{
		return getGauge(width, current, max, displayAsPercentage, displayAmount, "L2UI_CT1.Gauges.Gauge_DF_Large_Food_Bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_Food_Center", 17, -13);
	}
	
	public static String getWeightGauge(int width, long current, long max, boolean displayAsPercentage, boolean displayAmount, long level)
	{
		return getGauge(width, current, max, displayAsPercentage, displayAmount, "L2UI_CT1.Gauges.Gauge_DF_Large_Weight_bg_Center" + level, "L2UI_CT1.Gauges.Gauge_DF_Large_Weight_Center" + level, 17, -13);
	}
	
	public static String getEternityGauge(int width, long current, long max, boolean displayAsPercentage)
	{
		return getEternityGauge(width, current, max, displayAsPercentage, "l2ui_ct1_cn.titlebasebarmini", "branchsys2.br_navitgaugemid", 12, -13);
	}
	
	private static String getEternityGauge(int width, long current, long max, boolean displayAsPercentage, String backgroundImage, String image, long imageHeight, long top)
	{
		current = Math.min(current, max);
		final StringBuilder sb = new StringBuilder();
		sb.append("<table width=");
		sb.append(width);
		sb.append(" cellpadding=0 cellspacing=0 background=" + backgroundImage + " height=" + imageHeight + ">");
		sb.append("<tr>");
		sb.append("<td align=left valign=top>");
		sb.append("<img src=\"");
		sb.append(image);
		sb.append("\" width=");
		sb.append((long) (((double) current / max) * width));
		sb.append(" height=");
		sb.append(imageHeight);
		sb.append(">");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td align=center>");
		sb.append("<table cellpadding=0 cellspacing=");
		sb.append(top);
		sb.append(">");
		sb.append("<tr>");
		sb.append("<td>");
		if (displayAsPercentage)
		{
			sb.append("<table cellpadding=0 cellspacing=2>");
			sb.append("<tr><td>");
			sb.append("%.2f%%".formatted(((double) current / max) * 100));
			sb.append("</td></tr>");
			sb.append("</table>");
		}
		else
		{
			final int tdWidth = (width - 10) / 2;
			sb.append("<table cellpadding=0 cellspacing=0>");
			sb.append("<tr>");
			sb.append("<td width=");
			sb.append(tdWidth);
			sb.append(" align=right>");
			sb.append(current);
			sb.append("</td>");
			sb.append("<td width=10 align=center>/</td>");
			sb.append("<td width=");
			sb.append(tdWidth);
			sb.append(">");
			sb.append(max);
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
		}
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}
	
	private static String getGauge(int width, long current, long max, boolean displayAsPercentage, boolean displayAmount, String backgroundImage, String image, long imageHeight, long top)
	{
		current = Math.min(current, max);
		final StringBuilder sb = new StringBuilder();
		sb.append("<table width=");
		sb.append(width);
		sb.append(" cellpadding=0 cellspacing=0>");
		sb.append("<tr>");
		sb.append("<td background=\"");
		sb.append(backgroundImage);
		sb.append("\">");
		sb.append("<img src=\"");
		sb.append(image);
		sb.append("\" width=");
		sb.append((long) (((double) current / max) * width));
		sb.append(" height=");
		sb.append(imageHeight);
		sb.append(">");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td align=center>");
		sb.append("<table cellpadding=0 cellspacing=");
		sb.append(top);
		sb.append(">");
		sb.append("<tr>");
		sb.append("<td>");
		if (displayAsPercentage)
		{
			sb.append("<table cellpadding=0 cellspacing=2>");
			sb.append("<tr><td>");
			sb.append("%.2f%%".formatted(((double) current / max) * 100));
			sb.append("</td></tr>");
			sb.append("</table>");
		}
		else if (displayAmount)
		{
			final int tdWidth = (width - 10) / 2;
			sb.append("<table cellpadding=0 cellspacing=0>");
			sb.append("<tr>");
			sb.append("<td width=");
			sb.append(tdWidth);
			sb.append(" align=right>");
			sb.append(current);
			sb.append("</td>");
			sb.append("<td width=10 align=center>/</td>");
			sb.append("<td width=");
			sb.append(tdWidth);
			sb.append(">");
			sb.append(max);
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
		}
		else
		{
			final int tdWidth = (width - 10) / 2;
			sb.append("<table cellpadding=0 cellspacing=0>");
			sb.append("<tr>");
			sb.append("<td width=");
			sb.append(tdWidth);
			sb.append(" align=right>&nbsp;</td>");
			sb.append("<td width=10 align=center>&nbsp;</td>");
			sb.append("<td width=");
			sb.append(tdWidth);
			sb.append(">&nbsp;</td>");
			sb.append("</tr>");
			sb.append("</table>");
		}
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}
	
	public static String htmlResidenceName(int id)
	{
		return "&%" + id + ";";
	}
	
	public static String htmlNpcName(int npcId)
	{
		return "&@" + npcId + ";";
	}
	
	public static String htmlSysString(int id)
	{
		return "&$" + id + ";";
	}
	
	public static String htmlItemName(int itemId)
	{
		return "&#" + itemId + ";";
	}
	
	public static String htmlClassName(int classId)
	{
		return "<ClassId>" + classId + "</ClassId>";
	}
	
	public static String htmlNpcString(NpcStringId id, Object... params)
	{
		return htmlNpcString(id.getId(), params);
	}
	
	public static String htmlNpcString(int id, Object... params)
	{
		String replace = "<fstring";
		if (params.length > 0)
		{
			for (int i = 0; i < params.length; i++)
			{
				replace += " p" + (i + 1) + "=\"" + String.valueOf(params[i]) + "\"";
			}
		}
		replace += ">" + id + "</fstring>";
		return replace;
	}
	
	public static String htmlButton(String value, String action, int width)
	{
		return htmlButton(value, action, width, 22);
	}
	
	public static String htmlButton(String value, String action, int width, int height)
	{
		return "<button value=\"%s\" action=\"%s\" back=\"L2UI_CT1.Button_DF_Small_Down\" width=%d height=%d fore=\"L2UI_CT1.Button_DF_Small\">".formatted(value, action, width, height);
	}
	
	public static String switchButtons(String html)
	{
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"QUEST\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+NoblesseTeleport\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+TeleportToRaceTrack\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+TeleportToFantasy\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+CleftTeleport\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+CrumaTower\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+DelusionTeleport\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+ElrokiTeleporters\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+FreyaTeleport+\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+GraciaHeart\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+HuntingGroundsTeleport\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+NewbieTravelToken\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+PaganTeleporters\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(Quest\\s+SeparatedSoul\\s+(Synthesis+))\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"NORMAL\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(Quest\\s+SeparatedSoul\\s+([^\"]+))\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+StakatoNest\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+SteelCitadelTeleport\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+StrongholdsTeleports\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+TeleportCube\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+TeleportWithCharm\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(Quest\\s+ToIVortex\\s+([^\"]+))\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)goto)[^\"]+\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Chat)\\s+0\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"RETURN\" $1>$6</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>(Back|Return|�����|���������|�\\s+������)\\.?</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"RETURN\" $1>$5</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>(Teleport|�����������������)\\.?</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$5</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"NORMAL\" $1>$5</Button>");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button ALIGN=LEFT ICON=\"NORMAL\" $1>$5</Button>");
		return html;
	}
	
	public static String switchEmudevButtons(String html)
	{
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_3;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+NoblesseTeleport\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+TeleportToRaceTrack\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+TeleportToFantasy\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+CleftTeleport\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+CrumaTower\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+DelusionTeleport\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+ElrokiTeleporters\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+FreyaTeleport+\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+GraciaHeart\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+HuntingGroundsTeleport\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+NewbieTravelToken\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+PaganTeleporters\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(Quest\\s+SeparatedSoul\\s+(Synthesis+))\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_2;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(Quest\\s+SeparatedSoul\\s+([^\"]+))\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+StakatoNest\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+SteelCitadelTeleport\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+StrongholdsTeleports\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+TeleportCube\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\\s+TeleportWithCharm\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(Quest\\s+ToIVortex\\s+([^\"]+))\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)goto)[^\"]+\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Chat)\\s+0\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_4;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>(Back|Return|�����|���������|�\\s+������)\\.?</a\\s*>(<br1?>)?)", "<Button value=\"ICON_4;$5\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>(Teleport|�����������������)\\.?</a\\s*>(<br1?>)?)", "<Button value=\"ICON_5;$6\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+-h\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_2;$5\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		html = StringUtils.replaceAll(html, "(?i:<a\\s+(action=\"bypass\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)", "<Button value=\"ICON_2;$5\" $1 width=270 height=20 back=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High_down\" fore=\"L2UI_CT2.HtmlWnd.HtmlWnd_BTN01High\"");
		return html;
	}
}