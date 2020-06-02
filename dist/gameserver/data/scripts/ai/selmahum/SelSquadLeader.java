package ai.selmahum;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;

public class SelSquadLeader extends Fighter
{
	private boolean isBusy;
	private boolean isImmobilized;

	private long busyTimeout = 0;
	private long idleTimeout = 0;

	private static final NpcString[] phrase = { NpcString.SCHOOL4, NpcString.SCHOOL5, NpcString.SCHOOL6 };

	private static int NPC_ID_FIRE = 18927;
	private static int NPC_ID_FIRE_FEED = 18933;

	public SelSquadLeader(NpcInstance actor)
	{
		super(actor);
		setMaxPursueRange(6000);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		if(!isBusy)
		{
			if(System.currentTimeMillis() > idleTimeout)
			{
				for(NpcInstance npc : actor.getAroundNpc(600, 300))
				{
					if(npc.getNpcId() == NPC_ID_FIRE_FEED && GeoEngine.canSeeTarget(actor, npc)) // Cauldron
					{
						isBusy = true;
						actor.setRunning();
						actor.setNpcState(1); // Yummi State
						busyTimeout = System.currentTimeMillis() + (60 + Rnd.get(15)) * 1000L;
						addTaskMove(Location.findPointToStay(npc, 50, 150), true, false);
						if(Rnd.chance(40))
							Functions.npcSay(actor, phrase[Rnd.get(2)]);
					}
					else if(npc.getNpcId() == NPC_ID_FIRE && npc.getNpcState() == 1 && GeoEngine.canSeeTarget(actor, npc))
					{
						isBusy = true;
						actor.setNpcState(2); // Sleepy State
						busyTimeout = System.currentTimeMillis() + (60 + Rnd.get(60)) * 1000L;
						addTaskMove(Location.findPointToStay(npc, 50, 150), true, false);
					}
				}
			}
		}
		else
		{
			if(System.currentTimeMillis() > busyTimeout)
			{
				wakeUp();
				actor.setWalking();
				addTaskMove(actor.getSpawnedLoc(), true, false);
				return true;
			}
		}

		if(isImmobilized)
			return true;

		return super.thinkActive();
	}

	private void wakeUp()
	{
		NpcInstance actor = getActor();

		if(isBusy)
		{
			isBusy = false;

			busyTimeout = 0;
			idleTimeout = System.currentTimeMillis() + Rnd.get(3 * 60, 5 * 60) * 1000L;

			if(isImmobilized)
			{
				isImmobilized = false;
				actor.stopImmobilized();
				actor.setNpcState(3);
				actor.setRHandId(0);
				actor.broadcastCharInfo();
			}
		}
	}

	@Override
	protected void onEvtArrived()
	{
		NpcInstance actor = getActor();

		super.onEvtArrived();

		if(isBusy)
		{
			isImmobilized = true;
			actor.startImmobilized();
			actor.setRHandId(15280);
			actor.broadcastCharInfo();
		}
	}

	@Override
	protected void onIntentionActive()
	{
		//таймаут после атаки
		idleTimeout = System.currentTimeMillis() + Rnd.get(60, 5 * 60) * 1000L;

		super.onIntentionActive();
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		wakeUp();

		super.onIntentionAttack(target);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected boolean returnHome(boolean clearAggro, boolean teleport, boolean running, boolean force)
	{
		return false;
	}
}