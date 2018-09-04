package es.urjc.ia.bikesurbanfleets.tarifs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.w3c.dom.events.Event;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Truck;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;

public class SystemOperator {
	private List<Station> stations;
	private List<Truck> trucks;
	private Map<Integer, Integer> availableTrucks;	
	private Map<Integer, Integer> unavailableTrucks;
	private SimulationServices services;
	private int revenues;
	private Map<Integer, Integer> pricesToRent;
	private Map<Integer, Integer> pricesToReturn;

	public SystemOperator(InfraestructureManager infraestructure, SimulationServices services) {
		this.stations = infraestructure.consultStations();
		this.trucks = infraestructure.consultTrucks();
		this.availableTrucks = new HashMap<>();
		this.unavailableTrucks = new HashMap<>();
		this.services = services;
		pricesToRent = new HashMap<Integer, Integer>();
		pricesToReturn = new HashMap<Integer, Integer>();
	}
	
	public void init() {
		for (Station station: stations) {
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
	
	public void updatePricesToRent(Station station) {
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
	
	public void updatePricesToReturn(Station station) {
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
	
	public List<Event> evaluateBringBikes(Station station) {
		List<Event> events = new ArrayList();
		double bikesRatio = station.availableBikes()/station.getCapacity();
		double slotsRatio = station.availableSlots()/station.getCapacity();
		double availableResourcesRatio = bikesRatio + slotsRatio;
		int bikesToBring = 0;
		int counter = 0;
		int i = 0;
		
		if (bikesRatio <= 0.25 && slotsRatio >= 0.5 && availableResourcesRatio >= 0.4) {
			int halfOfResources = (int) Math.round(availableResourcesRatio*station.getCapacity()/2);
			bikesToBring = halfOfResources-station.availableBikes();
	}

		if (bikesToBring > 0) {

			Comparator<Station> byDistance = services.getStationComparator().byDistance(station.getPosition());
			List<Station> orderedStations = stations.stream()
				.sorted(byDistance).collect(Collectors.toList());
			
			while (i < orderedStations.size() && counter < bikesToBring) {
				station = stations.get(i);
				bikesRatio = station.availableBikes()/station.getCapacity(); 
				if (bikesRatio >= 0.6) {
					int nBikes = (int) Math.floor((bikesRatio - 0.5) * station.getCapacity());
					if (counter + nBikes > bikesToBring) {
						nBikes = bikesToBring - counter;
					}
					counter += nBikes;
				}
				i++;
			}
		}
		return events;
	}
	
	public void charge(int money) {
		revenues += money;
	}

	
}