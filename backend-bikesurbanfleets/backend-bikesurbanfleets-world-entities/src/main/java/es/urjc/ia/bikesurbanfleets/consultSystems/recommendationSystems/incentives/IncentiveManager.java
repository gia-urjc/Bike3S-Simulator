package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.incentives;

import java.util.List;

import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.User;

public abstract class IncentiveManager<T extends Comparable<T>> {
	public abstract List<Incentive<T>> calculateIncentive(List<Station> station);
	public abstract List<Incentive<T>> calculateIncentive(User user);
	public abstract List<Incentive<T>> calculateIncentive(List<Station> station, User user);
 

}
