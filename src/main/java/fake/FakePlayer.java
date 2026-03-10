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
package fake;

import fake.ai.FakePlayerAI;
import fake.model.FakeSupport;
import gameserver.model.actor.Player;
import gameserver.model.actor.appearance.PcAppearance;
import gameserver.model.actor.templates.player.PcTemplate;

public class FakePlayer extends Player
{
	private FakePlayerAI _fakeAi;
	private boolean _underControl = false;
	
	public boolean isUnderControl()
	{
		return _underControl;
	}
	
	public void setUnderControl(boolean underControl)
	{
		_underControl = underControl;
	}
	
	public FakePlayer(int objectId, PcTemplate template, String accountName, PcAppearance app)
	{
		super(objectId, template, accountName, app);
	}

	public FakePlayerAI getFakeAi()
	{
		return _fakeAi;
	}
	
	public void setFakeAi(FakePlayerAI fakeAi)
	{
		_fakeAi = fakeAi;
	}
	
	public void assignDefaultAI(boolean isPassive)
	{
		try
		{
			setFakeAi(FakeSupport.getAIbyClassId(getClassId(), isPassive).getConstructor(FakePlayer.class).newInstance(this));
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void assignDefaultTraderAI()
	{
		try
		{
			setFakeAi(FakeSupport.getAIbyTrader().getConstructor(FakePlayer.class).newInstance(this));
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDelete()
	{
		_fakeAi.stopActionTask();
		super.onDelete();
	}
	
	@Override
	public Player getActingPlayer()
	{
		return this;
	}
	
	public void heal()
	{
		setCurrentCp(getMaxCp());
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
	}
}