package quests;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;

/**
 * @author VISTALL
 * @date 8:17/10.06.2011
 */
public class _732_ProtectTheReligiousAssociationLeader extends QuestScript
{
	public _732_ProtectTheReligiousAssociationLeader()
	{
		super(PARTY_ALL, REPEATABLE);
		DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
		runnerEvent.addBreakQuest(this);
	}
}
