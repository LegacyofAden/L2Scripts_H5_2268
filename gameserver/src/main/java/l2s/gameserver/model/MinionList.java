package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.instances.MinionInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.npc.MinionData;


/**
 * Список минионов моба. 
 * @author G1ta0
 */
public class MinionList
{
	private final Set<MinionData> _minionData;
	private final Set<MinionInstance> _minions;
	private final Lock lock;
	private final MonsterInstance _master;

	public MinionList(MonsterInstance master)
	{
		_master = master;
		_minions = new HashSet<MinionInstance>();
		_minionData = new HashSet<MinionData>();
		_minionData.addAll(_master.getTemplate().getMinionData());
		lock = new ReentrantLock();
	}

	/**
	 * Добавить шаблон для миниона
	 * @param m
	 */
	public boolean addMinion(MinionData m)
	{
		lock.lock();
		try
		{
			return _minionData.add(m);
		}
		finally
		{
			lock.unlock();
		}
	}
	
	/**
	 * Добавить миниона
	 * @param m
	 * @return true, если успешно добавлен
	 */
	public boolean addMinion(MinionInstance m)
	{
		lock.lock();
		try
		{
			return _minions.add(m);
		}
		finally
		{
			lock.unlock();
		}
	}
	public Set<MinionInstance> getMinionList()
	{
		return _minions;
	}
	/**
	 * @return имеются ли живые минионы
	 */
	public boolean hasAliveMinions()
	{
		lock.lock();
		try
		{
			for(MinionInstance m : _minions)
				if(m.isVisible() && !m.isDead())
					return true;
		}
		finally
		{
			lock.unlock();
		}
		return false;
	}

	public boolean hasMinions()
	{
		return _minionData.size() > 0;
	}

	/**
	 * Возвращает список живых минионов
	 * @return список живых минионов
	 */
	public List<MinionInstance> getAliveMinions()
	{
		List<MinionInstance> result = new ArrayList<MinionInstance>(_minions.size());
		lock.lock();
		try
		{
			for(MinionInstance m : _minions)
				if(m.isVisible() && !m.isDead())
					result.add(m);
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}

	/**
	 *  Спавнит всех недостающих миньонов
	 */
	public void spawnMinions()
	{
		lock.lock();
		try
		{
			int minionCount;
			int minionId;
			for(MinionData minion : _minionData)
			{
				minionId = minion.getMinionId();
				minionCount = minion.getAmount();

				for(MinionInstance m : _minions)
				{
					if(m.getNpcId() == minionId)
						minionCount--;
					if(m.isDead() || !m.isVisible())
					{
						m.refreshID();
						m.stopDecay();
						_master.spawnMinion(m);
					}
				}

				for(int i = 0; i < minionCount; i++)
				{
					MinionInstance m = new MinionInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(minionId));
					m.setLeader(_master);
					_master.spawnMinion(m);
					_minions.add(m);
				}
			}	
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 *  Деспавнит всех минионов
	 */
	public void unspawnMinions()
	{
		lock.lock();
		try
		{
			for(MinionInstance m : _minions)
				m.decayMe();
		}
		finally
		{
			lock.unlock();
		}
	}
	
	/**
	 *	Удаляет минионов и чистит список 
	 */
	public void deleteMinions()
	{
		ThreadPoolManager.getInstance().schedule(new MinionDeletion(this), 60000L);
	}
	protected class MinionDeletion extends RunnableImpl
	{
		private MinionList _list;

		public MinionDeletion(MinionList list)
		{
			_list = list;
		}

		public void runImpl()
		{
			lock.lock();
			try
			{
				for(MinionInstance m : _list.getMinionList())
					m.deleteMe();
				_minions.clear();
			}
			finally
			{
				lock.unlock();
			}
		}
	}
}