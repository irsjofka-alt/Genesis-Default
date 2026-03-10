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
package gameserver.listener.actor;

import l2e.commons.listener.Listener;
import l2e.commons.listener.ListenerList;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;

public class CharListenerList extends ListenerList<Creature>
{
	public static final ListenerList<Creature> _global = new ListenerList<>();

	protected final Creature _actor;

	public CharListenerList(Creature actor)
	{
		_actor = actor;
	}

	public Creature getActor()
	{
		return _actor;
	}

	@SafeVarargs
	public final static void addGlobal(Listener<Creature>... listener)
	{
		for (final var l : listener)
		{
			_global.add(l);
		}
	}

	@SafeVarargs
	public static final void removeGlobal(Listener<Creature>... listener)
	{
		for (final var l : listener)
		{
			_global.remove(l);
		}
	}
	
	public void onAiIntention(CtrlIntention intention, Object... args)
	{
		_global.getListeners().stream().filter(l -> l != null && OnAiIntentionListener.class.isInstance(l)).forEach(l -> ((OnAiIntentionListener) l).onAiIntention(getActor(), intention, args));
		getListeners().stream().filter(l -> l != null && OnAiIntentionListener.class.isInstance(l)).forEach(l -> ((OnAiIntentionListener) l).onAiIntention(getActor(), intention, args));
	}
	
	public void onAttack(Creature target)
	{
		_global.getListeners().stream().filter(l -> l != null && OnAttackListener.class.isInstance(l)).forEach(l -> ((OnAttackListener) l).onAttack(getActor(), target));
		getListeners().stream().filter(l -> l != null && OnAttackListener.class.isInstance(l)).forEach(l -> ((OnAttackListener) l).onAttack(getActor(), target));
	}
	
	public void onAttackHit(Creature attacker)
	{
		_global.getListeners().stream().filter(l -> l != null && OnAttackHitListener.class.isInstance(l)).forEach(l -> ((OnAttackHitListener) l).onAttackHit(getActor(), attacker));
		getListeners().stream().filter(l -> l != null && OnAttackHitListener.class.isInstance(l)).forEach(l -> ((OnAttackHitListener) l).onAttackHit(getActor(), attacker));
	}
	
	public void onMagicUse(Skill skill, GameObject[] targets, boolean alt)
	{
		_global.getListeners().stream().filter(l -> l != null && OnMagicUseListener.class.isInstance(l)).forEach(l -> ((OnMagicUseListener) l).onMagicUse(getActor(), skill, targets, alt));
		getListeners().stream().filter(l -> l != null && OnMagicUseListener.class.isInstance(l)).forEach(l -> ((OnMagicUseListener) l).onMagicUse(getActor(), skill, targets, alt));
	}
	
	public void onMagicHit(Skill skill, Creature caster)
	{
		_global.getListeners().stream().filter(l -> l != null && OnMagicHitListener.class.isInstance(l)).forEach(l -> ((OnMagicHitListener) l).onMagicHit(getActor(), skill, caster));
		getListeners().stream().filter(l -> l != null && OnMagicHitListener.class.isInstance(l)).forEach(l -> ((OnMagicHitListener) l).onMagicHit(getActor(), skill, caster));
	}
	
	public void onDeath(Creature killer)
	{
		_global.getListeners().stream().filter(l -> l != null && OnDeathListener.class.isInstance(l)).forEach(l -> ((OnDeathListener) l).onDeath(getActor(), killer));
		getListeners().stream().filter(l -> l != null && OnDeathListener.class.isInstance(l)).forEach(l -> ((OnDeathListener) l).onDeath(getActor(), killer));
	}
	
	public void onKill(Creature victim)
	{
		_global.getListeners().stream().filter(l -> l != null && OnKillListener.class.isInstance(l)).forEach(l -> ((OnKillListener) l).onKill(getActor(), victim));
		getListeners().stream().filter(l -> l != null && OnKillListener.class.isInstance(l)).forEach(l -> ((OnKillListener) l).onKill(getActor(), victim));
	}
	
	public void onKillIgnorePetOrSummon(Creature victim)
	{
		_global.getListeners().stream().filter(l -> l != null && OnKillListener.class.isInstance(l)).forEach(l -> ((OnKillListener) l).onKill(getActor(), victim));
		getListeners().stream().filter(l -> l != null && OnKillListener.class.isInstance(l)).forEach(l -> ((OnKillListener) l).onKill(getActor(), victim));
	}
	
	public void onCurrentHpDamage(double damage, Creature attacker, Skill skill)
	{
		_global.getListeners().stream().filter(l -> l != null && OnCurrentHpDamageListener.class.isInstance(l)).forEach(l -> ((OnCurrentHpDamageListener) l).onCurrentHpDamage(getActor(), damage, attacker, skill));
		getListeners().stream().filter(l -> l != null && OnCurrentHpDamageListener.class.isInstance(l)).forEach(l -> ((OnCurrentHpDamageListener) l).onCurrentHpDamage(getActor(), damage, attacker, skill));
	}
	
	public void onChangeCurrentCp(double oldCp, double newCp)
	{
		_global.getListeners().stream().filter(l -> l != null && OnChangeCurrentCpListener.class.isInstance(l)).forEach(l -> ((OnChangeCurrentCpListener) l).onChangeCurrentCp(getActor(), oldCp, newCp));
		getListeners().stream().filter(l -> l != null && OnChangeCurrentCpListener.class.isInstance(l)).forEach(l -> ((OnChangeCurrentCpListener) l).onChangeCurrentCp(getActor(), oldCp, newCp));
	}
	
	public void onChangeCurrentHp(double oldHp, double newHp)
	{
		_global.getListeners().stream().filter(l -> l != null && OnChangeCurrentHpListener.class.isInstance(l)).forEach(l -> ((OnChangeCurrentHpListener) l).onChangeCurrentHp(getActor(), oldHp, newHp));
		getListeners().stream().filter(l -> l != null && OnChangeCurrentHpListener.class.isInstance(l)).forEach(l -> ((OnChangeCurrentHpListener) l).onChangeCurrentHp(getActor(), oldHp, newHp));
	}
	
	public void onChangeCurrentMp(double oldMp, double newMp)
	{
		_global.getListeners().stream().filter(l -> l != null && OnChangeCurrentMpListener.class.isInstance(l)).forEach(l -> ((OnChangeCurrentMpListener) l).onChangeCurrentMp(getActor(), oldMp, newMp));
		getListeners().stream().filter(l -> l != null && OnChangeCurrentMpListener.class.isInstance(l)).forEach(l -> ((OnChangeCurrentMpListener) l).onChangeCurrentMp(getActor(), oldMp, newMp));
	}
	
	public void onAct(final String act, final Object... args)
	{
		_global.getListeners().stream().filter(l -> l != null && OnActorAct.class.isInstance(l)).forEach(l -> ((OnActorAct) l).onAct(getActor(), act, args));
		getListeners().stream().filter(l -> l != null && OnActorAct.class.isInstance(l)).forEach(l -> ((OnActorAct) l).onAct(getActor(), act, args));
	}
}