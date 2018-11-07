package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.incentives;

import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.User;

public interface IncentiveManager {
	public Incentive calculateIncentive(GeoPoint point, StationQuality nearestStationQuality, StationQuality recommendedStationQuality);
}