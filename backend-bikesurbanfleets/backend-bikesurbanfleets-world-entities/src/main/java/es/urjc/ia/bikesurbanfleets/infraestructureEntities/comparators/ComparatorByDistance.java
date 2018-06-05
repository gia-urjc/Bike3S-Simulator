package es.urjc.ia.bikesurbanfleets.infraestructureEntities.comparators;

import java.util.Comparator;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.Station;

public class ComparatorByDistance implements Comparator<Station> {
	private GeoPoint point;

	public ComparatorByDistance(GeoPoint point) {
		this.point = point;
	}
	
	@Override
 public int compare(Station s1, Station s2) {
		return Double.compare(s1.getPosition().distanceTo(point), s2.getPosition()
				.distanceTo(point));
	}
	
}
