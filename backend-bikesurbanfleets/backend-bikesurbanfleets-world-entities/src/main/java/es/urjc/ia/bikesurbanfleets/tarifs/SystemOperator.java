package es.urjc.ia.bikesurbanfleets.tarifs;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

public class SystemOperator {
	private static int revenues;
	private static Map<Integer, Integer> pricesToRent;
	private static Map<Integer, Integer> pricesToReturn;
	
	public static void init(InfraestructureManager infraestructure) {
		pricesToRent = new HashMap<Integer, Integer>();
		pricesToReturn = new HashMap<Integer, Integer>();
		List<Station> infraestructureStations = infraestructure.consultStations();
		for(Station station: infraestructureStations) {
			double bikesRatio = station.availableBikes()/station.getCapacity();
			double slotsRatio = station.availableSlots() / station.getCapacity();
			
			
			if (bikesRatio <= 0.3) {			
				pricesToRent.put(station.getId(), 65);
			}
			else if (bikesRatio <= 0.45) {
				pricesToRent.put(station.getId(), 60);
			}
			else if (bikesRatio <= 0.55) {
				pricesToRent.put(station.getId(), 50);
			}
			else if (bikesRatio <= 0.70) {
				pricesToRent.put(station.getId(), 45);
			}
			else {
				pricesToRent.put(station.getId(), 40);
			}
			
			if (slotsRatio <= 0.3) {
				pricesToReturn.put(station.getId(), 65);
			}
			else if (slotsRatio <= 0.45) {
				pricesToReturn.put(station.getId(), 60);
			}
			else if (slotsRatio <= 0.55) {
				pricesToReturn.put(station.getId(), 50);
			}
			else if (slotsRatio <= 0.70) {
				pricesToReturn.put(station.getId(), 45);
			}
			else {
				pricesToReturn.put(station.getId(), 40);
			}
		}
	}
	
	public static void updatePricesToRent(Station station) {
		double bikesRatio = station.availableBikes()/station.getCapacity();
		if (bikesRatio <= 0.3) {			
			pricesToRent.put(station.getId(), 65);
		}
		else if (bikesRatio <= 0.45) {
			pricesToRent.put(station.getId(), 60);
		}
		else if (bikesRatio <= 0.55) {
			pricesToRent.put(station.getId(), 50);
		}
		else if (bikesRatio <= 0.70) {
			pricesToRent.put(station.getId(), 45);
		}
		else {
			pricesToRent.put(station.getId(), 40);
		}
		
	}
	
	public static void updatePricesToReturn(Station station) {
		double slotsRatio = station.availableSlots()/station.getCapacity();
		if (slotsRatio <= 0.3) {			
			pricesToReturn.put(station.getId(), 65);
		}
		else if (slotsRatio <= 0.45) {
			pricesToReturn.put(station.getId(), 60);
		}
		else if (slotsRatio <= 0.55) {
			pricesToReturn.put(station.getId(), 50);
		}
		else if (slotsRatio <= 0.70) {
			pricesToReturn.put(station.getId(), 45);
		}
		else {
			pricesToReturn.put(station.getId(), 40);
		}
		
	}
	
	public static void valueBringBikes(Station station) {
		double bikesRatio = station.availableBikes()/station.getCapacity();
		double slotsRatio = station.availableSlots()/station.getCapacity();
		double availableResourcesRatio = bikesRatio + slotsRatio;
		
		if (bikesRatio <= 0.25 && slotsRatio >= 0.5 && availableResourcesRatio >= 0.4) {
			int halfOfResources = (int) Math.round(availableResourcesRatio*station.getCapacity()/2);
			int bikesToBring = halfOfResources-station.availableBikes();
			
		}
	}
	
	public static void charge(int money) {
		revenues += money;
	}

	
}