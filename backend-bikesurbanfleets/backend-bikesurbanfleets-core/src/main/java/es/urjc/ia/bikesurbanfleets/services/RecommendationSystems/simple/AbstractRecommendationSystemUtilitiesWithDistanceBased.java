package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
public abstract class AbstractRecommendationSystemUtilitiesWithDistanceBased extends RecommendationSystem {

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters {
    }
    protected RecommendationParameters parameters;
    
    public AbstractRecommendationSystemUtilitiesWithDistanceBased(JsonObject recomenderdef, SimulationServices ss,RecommendationParameters parameters) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, parameters);
        this.parameters = (RecommendationParameters) (super.parameters);
    }

    abstract public void getStationUtility(StationData s, boolean rentbike);

    @Override
    public Stream<StationData> recommendStationToRentBike(final Stream<StationData> candidates, final GeoPoint point, double maxdist) {
        return candidates
                .filter(s -> s.availableBikes > 0) //filter station candat¡dates
                .map(s -> {
                    getStationUtility(s, true);
                    return s;
                })//apply function to calculate utilities the utility data
                .sorted(DescUtility); //sort by utility
    }

    public Stream<StationData> recommendStationToReturnBike(final Stream<StationData> candidates, final GeoPoint currentposition, final GeoPoint destination) {
        return candidates
                .filter(s -> s.availableSlots > 0) //filter station candat¡dates
                .map(s -> {
                    getStationUtility(s, false);
                    return s;
                })//apply function to calculate utilities the utility data
                .sorted(DescUtility); //sort by utility
    }
    
    Comparator<StationData> DescUtility = (sq1, sq2) -> Double.compare(sq2.Utility, sq1.Utility);

    @Override
    protected void printRecomendationDetails(List<StationData> su, boolean rentbike, long maxnumber) {
        if (rentbike) {
            System.out.println("             id av ca    wtime    utility optOcup minOOcup maxOOcup");
            int i = 1;
            for (StationData s : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %8.2f %9.8f %9.8f %9.8f %9.8f %n",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        s.walktime, 
                        s.Utility,
                        s.optimalocupation,
                        s.minoptimalocupation,
                        s.maxopimalocupation);
                i++;
            }
        } else {
            System.out.println("             id av ca    wtime    btime    utility optOcup minOOcup maxOOcup");
            int i = 1;
            for (StationData s : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %8.2f %8.2f %9.8f %9.8f %9.8f %9.8f %n",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        s.walktime,
                        s.biketime,
                        s.Utility,
                        s.optimalocupation,
                        s.minoptimalocupation,
                        s.maxopimalocupation);
                i++;
            } 
        }
    }

}
