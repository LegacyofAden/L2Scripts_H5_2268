package npc.model.fightClub;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.fightclub.CaptureTheFlagEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

public class FlagHolderInstance extends NpcInstance
{
  public FlagHolderInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }
  

  public void onAction(Player player, boolean shift)
  {
    if (!isTargetable())
    {
      player.sendActionFailed();
      return;
    }
    
    if (player.getTarget() != this)
    {
      player.setTarget(this);
      return;
    }
    
    if (!isInRange(player, 200))
    {
      if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
      return;
    }
    
    if ((player.isSitting()) || (player.isAlikeDead())) {
      return;
    }
    player.sendActionFailed();
    player.stopMove();
    
    if (player.isInFightClub())
    {
      if ((player.getFightClubEvent() instanceof CaptureTheFlagEvent)) {
        ((CaptureTheFlagEvent)player.getFightClubEvent()).talkedWithFlagHolder(player, this);
      }
    }
  }
}
