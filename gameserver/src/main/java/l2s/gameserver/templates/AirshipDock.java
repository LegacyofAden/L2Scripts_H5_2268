package l2s.gameserver.templates;

import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.entity.events.objects.BoatPoint;
import l2s.gameserver.network.l2.components.SceneMovie;
import l2s.gameserver.utils.Location;


/**
 * @author VISTALL
 */
public class AirshipDock
{
	public static class AirshipPlatform
	{
		private final SceneMovie _oustMovie;
		private final Location _oustLoc;
		private final Location _spawnLoc;
		private List<BoatPoint> _arrivalPoints = Collections.emptyList();
		private List<BoatPoint> _departPoints = Collections.emptyList();

		public AirshipPlatform(SceneMovie movie, Location oustLoc, Location spawnLoc, List<BoatPoint> arrival, List<BoatPoint> depart)
		{
			_oustMovie = movie;
			_oustLoc = oustLoc;
			_spawnLoc = spawnLoc;
			_arrivalPoints = arrival;
			_departPoints = depart;
		}

		public SceneMovie getOustMovie()
		{
			return _oustMovie;
		}

		public Location getOustLoc()
		{
			return _oustLoc;
		}

		public Location getSpawnLoc()
		{
			return _spawnLoc;
		}

		public List<BoatPoint> getArrivalPoints()
		{
			return _arrivalPoints;
		}

		public List<BoatPoint> getDepartPoints()
		{
			return _departPoints;
		}
	}

	private final int _id;
	private List<BoatPoint> _teleportList = Collections.emptyList();
	private List<AirshipPlatform> _platformList = Collections.emptyList();

	public AirshipDock(int id, List<BoatPoint> teleport, List<AirshipPlatform> platformList)
	{
		_id = id;
		_teleportList = teleport;
		_platformList = platformList;
	}

	public int getId()
	{
		return _id;
	}

	public List<BoatPoint> getTeleportList()
	{
		return _teleportList;
	}

	public AirshipPlatform getPlatform(int id)
	{
		return _platformList.get(id);
	}
}
