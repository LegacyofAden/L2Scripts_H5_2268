package l2s.gameserver.stats;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassType2;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.stats.conditions.ConditionPlayerState;
import l2s.gameserver.stats.conditions.ConditionPlayerState.CheckPlayerState;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

/**
 * Коллекция предопределенных классов функций.
 *
 */
public class StatFunctions
{
	private static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] func = new FuncMultRegenResting[Stats.NUM_STATS];

		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
				func[pos] = new FuncMultRegenResting(stat);
			return func[pos];
		}

		private FuncMultRegenResting(Stats stat)
		{
			super(stat, 0x30, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
		}

		@Override
		public void calc(Env env)
		{
			if(env.character.isPlayer() && env.character.getLevel() <= 40 && ((Player) env.character).getClassLevel() < 2 && stat == Stats.REGENERATE_HP_RATE)
				env.value *= Config.REGEN_HP_REST_40_LOWER; // TODO: переделать красивее
			else
				env.value *= Config.REGEN_REST;
		}
	}

	private static class FuncMultRegenStanding extends Func
	{
		static final FuncMultRegenStanding[] func = new FuncMultRegenStanding[Stats.NUM_STATS];

		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
				func[pos] = new FuncMultRegenStanding(stat);
			return func[pos];
		}

		private FuncMultRegenStanding(Stats stat)
		{
			super(stat, 0x30, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.STANDING, true));
		}

		@Override
		public void calc(Env env)
		{
			env.value *= Config.REGEN_STAND;
		}
	}

	private static class FuncMultRegenRunning extends Func
	{
		static final FuncMultRegenRunning[] func = new FuncMultRegenRunning[Stats.NUM_STATS];

		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
				func[pos] = new FuncMultRegenRunning(stat);
			return func[pos];
		}

		private FuncMultRegenRunning(Stats stat)
		{
			super(stat, 0x30, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RUNNING, true));
		}

		@Override
		public void calc(Env env)
		{
			env.value *= Config.REGEN_RUNNING;
		}
	}

	private static class FuncPAtkMul extends Func
	{
		static final FuncPAtkMul func = new FuncPAtkMul();

		private FuncPAtkMul()
		{
			super(Stats.POWER_ATTACK, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.STR.calcBonus(env.character) * env.character.getLevelMod();
		}
	}

	private static class FuncMAtkMul extends Func
	{
		static final FuncMAtkMul func = new FuncMAtkMul();

		private FuncMAtkMul()
		{
			super(Stats.MAGIC_ATTACK, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			//{Wpn*(lvlbn^2)*[(1+INTbn)^2]+Msty}
			double ib = BaseStats.INT.calcBonus(env.character);
			double lvlb = env.character.getLevelMod();
			env.value *= lvlb * lvlb * ib * ib;
		}
	}

	private static class FuncPDefMul extends Func
	{
		static final FuncPDefMul func = new FuncPDefMul();

		private FuncPDefMul()
		{
			super(Stats.POWER_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= env.character.getLevelMod();
		}
	}

	private static class FuncMDefMul extends Func
	{
		static final FuncMDefMul func = new FuncMDefMul();

		private FuncMDefMul()
		{
			super(Stats.MAGIC_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.MEN.calcBonus(env.character) * env.character.getLevelMod();
		}
	}

	private static class FuncAttackRange extends Func
	{
		static final FuncAttackRange func = new FuncAttackRange();

		private FuncAttackRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			WeaponTemplate weapon = env.character.getActiveWeaponTemplate();
			if(weapon != null)
				env.value += weapon.getAttackRange();
		}
	}

	private static class FuncAccuracyAdd extends Func
	{
		static final FuncAccuracyAdd func = new FuncAccuracyAdd();

		private FuncAccuracyAdd()
		{
			super(Stats.ACCURACY_COMBAT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.character.isPet())
				return;

			//[Square(DEX)]*6 + lvl + weapon hitbonus;
			env.value += Math.sqrt(env.character.getDEX()) * 6 + env.character.getLevel();

			if(env.character.isSummon())
				env.value += env.character.getLevel() < 60 ? 4 : 5;

			if(env.character.getLevel() > 77)
				env.value += env.character.getLevel() - 77;
			if(env.character.getLevel() > 69)
				env.value += env.character.getLevel() - 69;
		}
	}

	private static class FuncEvasionAdd extends Func
	{
		static final FuncEvasionAdd func = new FuncEvasionAdd();

		private FuncEvasionAdd()
		{
			super(Stats.EVASION_RATE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value += Math.sqrt(env.character.getDEX()) * 6 + env.character.getLevel();

			if(env.character.getLevel() > 77)
				env.value += env.character.getLevel() - 77;
			if(env.character.getLevel() > 69)
				env.value += env.character.getLevel() - 69;
		}
	}

	private static class FuncMCriticalRateMul extends Func
	{
		static final FuncMCriticalRateMul func = new FuncMCriticalRateMul();

		private FuncMCriticalRateMul()
		{
			super(Stats.MCRITICAL_RATE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.WIT.calcBonus(env.character);
		}
	}

	private static class FuncPCriticalRateMul extends Func
	{
		static final FuncPCriticalRateMul func = new FuncPCriticalRateMul();

		private FuncPCriticalRateMul()
		{
			super(Stats.CRITICAL_BASE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			if(!env.character.isServitor())
				env.value *= BaseStats.DEX.calcBonus(env.character);
			env.value *= 0.01 * env.character.calcStat(Stats.CRITICAL_RATE, env.target, env.skill);
		}
	}

	private static class FuncMoveSpeedMul extends Func
	{
		static final FuncMoveSpeedMul func = new FuncMoveSpeedMul();

		private FuncMoveSpeedMul()
		{
			super(Stats.RUN_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.character);
		}
	}

	private static class FuncPAtkSpeedMul extends Func
	{
		static final FuncPAtkSpeedMul func = new FuncPAtkSpeedMul();

		private FuncPAtkSpeedMul()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.character);
		}
	}

	private static class FuncMAtkSpeedMul extends Func
	{
		static final FuncMAtkSpeedMul func = new FuncMAtkSpeedMul();

		private FuncMAtkSpeedMul()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.WIT.calcBonus(env.character);
		}
	}

	private static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR func = new FuncHennaSTR();

		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatSTR());
		}
	}

	private static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX func = new FuncHennaDEX();

		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatDEX());
		}
	}

	private static class FuncHennaINT extends Func
	{
		static final FuncHennaINT func = new FuncHennaINT();

		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatINT());
		}
	}

	private static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN func = new FuncHennaMEN();

		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatMEN());
		}
	}

	private static class FuncHennaCON extends Func
	{
		static final FuncHennaCON func = new FuncHennaCON();

		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatCON());
		}
	}

	private static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT func = new FuncHennaWIT();

		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatWIT());
		}
	}

	private static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul func = new FuncMaxHpMul();

		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.character);
		}
	}

	private static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul func = new FuncMaxCpMul();

		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			double cpSSmod = 1;
			int sealOwnedBy = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE);
			int playerCabal = SevenSigns.getInstance().getPlayerCabal((Player) env.character);

			if(sealOwnedBy != SevenSigns.CABAL_NULL)
				if(playerCabal == sealOwnedBy)
					cpSSmod = 1.1;
				else
					cpSSmod = 0.9;

			env.value *= BaseStats.CON.calcBonus(env.character) * cpSSmod;
		}
	}

	private static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul func = new FuncMaxMpMul();

		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.MEN.calcBonus(env.character);
		}
	}

	private static class FuncPDamageResists extends Func
	{
		static final FuncPDamageResists func = new FuncPDamageResists();

		private FuncPDamageResists()
		{
			super(Stats.PHYSICAL_DAMAGE, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.target.isRaid() && (env.character.getLevel() - env.target.getLevel()) > (Config.RAID_MAX_LEVEL_DIFF + Config.PDAM_TO_RAID_SUB_LVL_DIFF))
			{
				env.value = 1;
				return;
			}

			env.value *= 0.01 * env.target.calcStat(env.character.getBaseAttackType().getDefence(), env.character, env.skill);
			env.value = Formulas.calcDamageResists(env.skill, env.character, env.target, env.value);
		}
	}

	private static class FuncMDamageResists extends Func
	{
		static final FuncMDamageResists func = new FuncMDamageResists();

		private FuncMDamageResists()
		{
			super(Stats.MAGIC_DAMAGE, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.target.isRaid() && Math.abs(env.character.getLevel() - env.target.getLevel()) > (Config.RAID_MAX_LEVEL_DIFF + Config.MDAM_TO_RAID_SUB_LVL_DIFF))
			{
				env.value = 1;
				return;
			}
			env.value = Formulas.calcDamageResists(env.skill, env.character, env.target, env.value);
		}
	}

	private static class FuncInventory extends Func
	{
		static final FuncInventory func = new FuncInventory();

		private FuncInventory()
		{
			super(Stats.INVENTORY_LIMIT, 0x01, null);
		}

		@Override
		public void calc(Env env)
		{
			Player player = (Player) env.character;
			if(player.isGM())
				env.value = Config.INVENTORY_MAXIMUM_GM;
			else if(player.getRace() == Race.DWARF)
				env.value = Config.INVENTORY_MAXIMUM_DWARF;
			else
				env.value = Config.INVENTORY_MAXIMUM_NO_DWARF;
			env.value += player.getExpandInventory();
			env.value = Math.min(env.value, Config.SERVICES_EXPAND_INVENTORY_MAX);
		}
	}

	private static class FuncWarehouse extends Func
	{
		static final FuncWarehouse func = new FuncWarehouse();

		private FuncWarehouse()
		{
			super(Stats.STORAGE_LIMIT, 0x01, null);
		}

		@Override
		public void calc(Env env)
		{
			Player player = (Player) env.character;
			if(player.getRace() == Race.DWARF)
				env.value = Config.WAREHOUSE_SLOTS_DWARF;
			else
				env.value = Config.WAREHOUSE_SLOTS_NO_DWARF;
			env.value += player.getExpandWarehouse();
		}
	}

	private static class FuncTradeLimit extends Func
	{
		static final FuncTradeLimit func = new FuncTradeLimit();

		private FuncTradeLimit()
		{
			super(Stats.TRADE_LIMIT, 0x01, null);
		}

		@Override
		public void calc(Env env)
		{
			Player _cha = (Player) env.character;
			if(_cha.getRace() == Race.DWARF)
				env.value = Config.MAX_PVTSTORE_SLOTS_DWARF;
			else
				env.value = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}
	}

	private static class FuncSDefInit extends Func
	{
		static final Func func = new FuncSDefInit();

		private FuncSDefInit()
		{
			super(Stats.SHIELD_RATE, 0x01, null);
		}

		@Override
		public void calc(Env env)
		{
			Creature cha = env.character;
			env.value = cha.getTemplate().getBaseShldRate();
		}
	}

	private static class FuncSDefAll extends Func
	{
		static final FuncSDefAll func = new FuncSDefAll();

		private FuncSDefAll()
		{
			super(Stats.SHIELD_RATE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.value == 0)
				return;

			Creature target = env.target;
			if(target != null)
			{
				switch(target.getBaseAttackType())
				{
					case BOW:
					case CROSSBOW:
						env.value += 30.;
						break;
					case DAGGER:
					case DUALDAGGER:
						env.value += 12.;
						break;
				}
			}
		}
	}

	private static class FuncSDefPlayers extends Func
	{
		static final FuncSDefPlayers func = new FuncSDefPlayers();

		private FuncSDefPlayers()
		{
			super(Stats.SHIELD_RATE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.value == 0)
				return;

			Creature cha = env.character;
			ItemInstance shld = ((Player) cha).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			if(shld == null || shld.getItemType() != WeaponType.NONE)
				return;
			env.value *= BaseStats.DEX.calcBonus(env.character);
		}
	}

	private static class FuncMaxHpLimit extends Func
	{
		static final Func func = new FuncMaxHpLimit();

		private FuncMaxHpLimit()
		{
			super(Stats.MAX_HP, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.MAX_HP_LIMIT, env.value);
		}
	}

	private static class FuncMaxMpLimit extends Func
	{
		static final Func func = new FuncMaxMpLimit();

		private FuncMaxMpLimit()
		{
			super(Stats.MAX_MP, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.MAX_MP_LIMIT, env.value);
		}
	}

	private static class FuncMaxCpLimit extends Func
	{
		static final Func func = new FuncMaxCpLimit();

		private FuncMaxCpLimit()
		{
			super(Stats.MAX_CP, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.MAX_CP_LIMIT, env.value);
		}
	}

	private static class FuncRunSpdLimit extends Func
	{
		static final Func func = new FuncRunSpdLimit();

		private FuncRunSpdLimit()
		{
			super(Stats.RUN_SPEED, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.character.isPlayer() && env.character.getPlayer().isGM())
				env.value = Math.min(env.character.calcStat(Stats.RUN_SPEED_LIMIT, Config.GM_LIM_MOVE), env.value);
			else
				env.value = Math.min(env.character.calcStat(Stats.RUN_SPEED_LIMIT, Config.LIM_MOVE), env.value);
		}
	}

	private static class FuncPDefLimit extends Func
	{
		static final Func func = new FuncPDefLimit();

		private FuncPDefLimit()
		{
			super(Stats.POWER_DEFENCE, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_PDEF, env.value);
		}
	}

	private static class FuncMDefLimit extends Func
	{
		static final Func func = new FuncMDefLimit();

		private FuncMDefLimit()
		{
			super(Stats.MAGIC_DEFENCE, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_MDEF, env.value);
		}
	}

	private static class FuncPAtkLimit extends Func
	{
		static final Func func = new FuncPAtkLimit();

		private FuncPAtkLimit()
		{
			super(Stats.POWER_ATTACK, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_PATK, env.value);
		}
	}

	private static class FuncMAtkLimit extends Func
	{
		static final Func func = new FuncMAtkLimit();

		private FuncMAtkLimit()
		{
			super(Stats.MAGIC_ATTACK, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_MATK, env.value);
		}
	}

	private static class FuncPAtkSpdLimit extends Func
	{
		static final Func func = new FuncPAtkSpdLimit();

		private FuncPAtkSpdLimit()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_PATK_SPD, env.value);
		}
	}

	private static class FuncMAtkSpdLimit extends Func
	{
		static final Func func = new FuncMAtkSpdLimit();

		private FuncMAtkSpdLimit()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_MATK_SPD, env.value);
		}
	}

	private static class FuncCAtkLimit extends Func
	{
		static final Func func = new FuncCAtkLimit();

		private FuncCAtkLimit()
		{
			super(Stats.CRITICAL_DAMAGE, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_CRIT_DAM / 2., env.value);
		}
	}

	private static class FuncEvasionLimit extends Func
	{
		static final Func func = new FuncEvasionLimit();

		private FuncEvasionLimit()
		{
			super(Stats.EVASION_RATE, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_EVASION, env.value);
		}
	}

	private static class FuncAccuracyLimit extends Func
	{
		static final Func func = new FuncAccuracyLimit();

		private FuncAccuracyLimit()
		{
			super(Stats.ACCURACY_COMBAT, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_ACCURACY, env.value);
		}
	}

	private static class FuncCritLimit extends Func
	{
		static final Func func = new FuncCritLimit();

		private FuncCritLimit()
		{
			super(Stats.CRITICAL_BASE, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_CRIT, env.value);
		}
	}

	private static class FuncMCritLimit extends Func
	{
		static final Func func = new FuncMCritLimit();

		private FuncMCritLimit()
		{
			super(Stats.MCRITICAL_RATE, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value = Math.min(Config.LIM_MCRIT, env.value);
		}
	}

	private static class FuncAttributeAttackInit extends Func
	{
		static final Func[] func = new FuncAttributeAttackInit[Element.VALUES.length];

		static
		{
			for(int i = 0; i < Element.VALUES.length; i++)
				func[i] = new FuncAttributeAttackInit(Element.VALUES[i]);
		}

		static Func getFunc(Element element)
		{
			return func[element.getId()];
		}

		private Element element;

		private FuncAttributeAttackInit(Element element)
		{
			super(element.getAttack(), 0x01, null);
			this.element = element;
		}

		@Override
		public void calc(Env env)
		{
			env.value += env.character.getTemplate().getBaseAttributeAttack()[element.getId()];
		}
	}

	private static class FuncAttributeDefenceInit extends Func
	{
		static final Func[] func = new FuncAttributeDefenceInit[Element.VALUES.length];

		static
		{
			for(int i = 0; i < Element.VALUES.length; i++)
				func[i] = new FuncAttributeDefenceInit(Element.VALUES[i]);
		}

		static Func getFunc(Element element)
		{
			return func[element.getId()];
		}

		private Element element;

		private FuncAttributeDefenceInit(Element element)
		{
			super(element.getDefence(), 0x01, null);
			this.element = element;
		}

		@Override
		public void calc(Env env)
		{
			env.value += env.character.getTemplate().getBaseAttributeDefence()[element.getId()];
		}
	}
	
	private static class FuncAttributeAttackSet extends Func
	{
		static final Func[] func = new FuncAttributeAttackSet[Element.VALUES.length];

		static
		{
			for(int i = 0; i < Element.VALUES.length; i++)
				func[i] = new FuncAttributeAttackSet(Element.VALUES[i].getAttack());
		}
		
		static Func getFunc(Element element)
		{
			return func[element.getId()];
		}

		private FuncAttributeAttackSet(Stats stat)
		{
			super(stat, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			ClassId classId = env.character.getPlayer().getClassId();
			if(classId.getType2() == ClassType2.Summoner || classId.equalsOrChildOf(ClassId.NECROMANCER))
				env.value = env.character.getPlayer().calcStat(stat, 0.);
		}
	}
	
	private static class FuncAttributeDefenceSet extends Func
	{
		static final Func[] func = new FuncAttributeDefenceSet[Element.VALUES.length];

		static
		{
			for(int i = 0; i < Element.VALUES.length; i++)
				func[i] = new FuncAttributeDefenceSet(Element.VALUES[i].getDefence());
		}
		
		static Func getFunc(Element element)
		{
			return func[element.getId()];
		}

		private FuncAttributeDefenceSet(Stats stat)
		{
			super(stat, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			ClassId classId = env.character.getPlayer().getClassId();
			if(classId.getType2() == ClassType2.Summoner || classId.equalsOrChildOf(ClassId.NECROMANCER))
				env.value = env.character.getPlayer().calcStat(stat, 0.);
		}
	}

	private static class FuncMaxLoadMul extends Func
	{
		static final FuncMaxLoadMul func = new FuncMaxLoadMul();

		private FuncMaxLoadMul()
		{
			super(Stats.MAX_LOAD, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.character) * Config.MAXLOAD_MODIFIER;
		}
	}

	private static class FuncBreathMul extends Func
	{
		static final FuncBreathMul func = new FuncBreathMul();

		private FuncBreathMul()
		{
			super(Stats.BREATH, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.character); //TODO: Сверить с оффом.
		}
	}

	private static class FuncHpRegenMul extends Func
	{
		static final FuncHpRegenMul func = new FuncHpRegenMul();

		private FuncHpRegenMul()
		{
			super(Stats.REGENERATE_HP_RATE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.character);
			if(env.character.isSummon()) //TODO: [Bonux] Наверно пет тоже?
				env.value *= 2;
		}
	}

	private static class FuncMpRegenMul extends Func
	{
		static final FuncMpRegenMul func = new FuncMpRegenMul();

		private FuncMpRegenMul()
		{
			super(Stats.REGENERATE_MP_RATE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.MEN.calcBonus(env.character);
			if(env.character.isSummon()) //TODO: [Bonux] Наверно пет тоже?
				env.value *= 2;
		}
	}

	private static class FuncHpRegenPenalty extends Func
	{
		static final FuncHpRegenPenalty func = new FuncHpRegenPenalty();

		private FuncHpRegenPenalty()
		{
			super(Stats.REGENERATE_HP_RATE, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.character.isPet())
			{
				/*PetInstance pet = (PetInstance) env.character;
				if(pet.getWeightPenalty() == 1 || pet.getWeightPenalty() == 2)
					env.value *= 0.5; //TODO: [Bonux] от балды, проверить на оффе.
				else if(pet.getWeightPenalty() == 3)
					env.value = 0.;*/
			}
		}
	}

	private static class FuncMpRegenPenalty extends Func
	{
		static final FuncMpRegenPenalty func = new FuncMpRegenPenalty();

		private FuncMpRegenPenalty()
		{
			super(Stats.REGENERATE_MP_RATE, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.character.isPet())
			{
				/*PetInstance pet = (PetInstance) env.character;
				if(pet.getWeightPenalty() == 1 || pet.getWeightPenalty() == 2)
					env.value *= 0.5; //TODO: [Bonux] от балды, проверить на оффе.
				else if(pet.getWeightPenalty() == 3)
					env.value = 0.;*/
			}
		}
	}

	private static class FuncMoveSpeedPenalty extends Func
	{
		static final FuncMoveSpeedPenalty func = new FuncMoveSpeedPenalty();

		private FuncMoveSpeedPenalty()
		{
			super(Stats.RUN_SPEED, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.character.isPlayer())
			{
				/*Player player = env.character.getPlayer();
				if(player.isMounted())
				{
					Mount mount = player.getMount();
					//Если уровень маунта на 50% и более, скорость урезается в 2х раза.
					//Если уровень маунта на 10 уровней и более больше хозяина, скорость урезается в 2х раза.
					if(mount.isHungry() || (mount.getLevel() - player.getLevel()) >= 10)
						env.value *= 0.5;
				}*/
			}
			else if(env.character.isPet())
			{
				/*PetInstance pet = (PetInstance) env.character;
				//Если питомец голоден, скорость урезается в 2х раза.
				if(pet.isHungry() || pet.getWeightPenalty() >= 2)
					env.value *= 0.5;*/
			}
		}
	}

	private static class FuncPAtkSpeedPenalty extends Func
	{
		static final FuncPAtkSpeedPenalty func = new FuncPAtkSpeedPenalty();

		private FuncPAtkSpeedPenalty()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x100, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.character.isPlayer())
			{
				/*Player player = env.character.getPlayer();
				//Если маунт голоден на 50% и более, скорость атаки урезается в 2х раза.
				if(player.isMounted() && player.getMount().isHungry())
					env.value *= 0.5;*/
			}
		}
	}
	
	public static void addPredefinedFuncs(Creature cha)
	{
		if(cha.isPlayer())
		{
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_MP_RATE));

			cha.addStatFunc(FuncMaxCpMul.func);

			cha.addStatFunc(FuncAttackRange.func);

			cha.addStatFunc(FuncMoveSpeedMul.func);

			cha.addStatFunc(FuncHennaSTR.func);
			cha.addStatFunc(FuncHennaDEX.func);
			cha.addStatFunc(FuncHennaINT.func);
			cha.addStatFunc(FuncHennaMEN.func);
			cha.addStatFunc(FuncHennaCON.func);
			cha.addStatFunc(FuncHennaWIT.func);

			cha.addStatFunc(FuncInventory.func);
			cha.addStatFunc(FuncWarehouse.func);
			cha.addStatFunc(FuncTradeLimit.func);

			cha.addStatFunc(FuncSDefPlayers.func);

			cha.addStatFunc(FuncMaxHpLimit.func);
			cha.addStatFunc(FuncMaxMpLimit.func);
			cha.addStatFunc(FuncMaxCpLimit.func);
			cha.addStatFunc(FuncRunSpdLimit.func);
			cha.addStatFunc(FuncPDefLimit.func);
			cha.addStatFunc(FuncMDefLimit.func);
			cha.addStatFunc(FuncPAtkLimit.func);
			cha.addStatFunc(FuncMAtkLimit.func);

			cha.addStatFunc(FuncMaxLoadMul.func); //TODO: Наверно и петы тоже..
			cha.addStatFunc(FuncBreathMul.func);
		}

		cha.addStatFunc(FuncMaxHpMul.func);
		cha.addStatFunc(FuncMaxMpMul.func);
		cha.addStatFunc(FuncHpRegenMul.func);
		cha.addStatFunc(FuncMpRegenMul.func);

		if(cha.isPet())
		{
			cha.addStatFunc(FuncMpRegenPenalty.func);
			cha.addStatFunc(FuncHpRegenPenalty.func);
		}

		if(cha.isPlayer() || cha.isPet())
		{
			cha.addStatFunc(FuncMoveSpeedPenalty.func);
		}

		cha.addStatFunc(FuncPAtkMul.func);
		cha.addStatFunc(FuncMAtkMul.func);
		cha.addStatFunc(FuncPDefMul.func);
		cha.addStatFunc(FuncMDefMul.func);

		if(cha.isSummon())
		{
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.FIRE));
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.WATER));
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.EARTH));
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.WIND));
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.HOLY));
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.UNHOLY));
			
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.FIRE));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.WATER));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.EARTH));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.WIND));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.HOLY));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.UNHOLY));
		}

		cha.addStatFunc(FuncAccuracyAdd.func);
		cha.addStatFunc(FuncEvasionAdd.func);

		cha.addStatFunc(FuncPAtkSpeedMul.func);
		cha.addStatFunc(FuncMAtkSpeedMul.func);

		cha.addStatFunc(FuncSDefInit.func);
		cha.addStatFunc(FuncSDefAll.func);

		cha.addStatFunc(FuncPAtkSpdLimit.func);
		cha.addStatFunc(FuncMAtkSpdLimit.func);
		cha.addStatFunc(FuncCAtkLimit.func);
		cha.addStatFunc(FuncEvasionLimit.func);
		cha.addStatFunc(FuncAccuracyLimit.func);
		cha.addStatFunc(FuncCritLimit.func);
		cha.addStatFunc(FuncMCritLimit.func);

		cha.addStatFunc(FuncMCriticalRateMul.func);
		cha.addStatFunc(FuncPCriticalRateMul.func);
		cha.addStatFunc(FuncPDamageResists.func);
		cha.addStatFunc(FuncMDamageResists.func);
		
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.FIRE));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.WATER));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.EARTH));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.WIND));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.HOLY));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.UNHOLY));
		
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.FIRE));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.WATER));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.EARTH));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.WIND));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.HOLY));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.UNHOLY));
	}
}
