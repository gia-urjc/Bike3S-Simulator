package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

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
@RecommendationSystemType("PROBABILITY")
public final class RecommendationSystemDemandProbability extends AbstractRecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemDemandProbabilityBased.RecommendationParameters {

        double desireableProbability = 0.8;
        double probfactor = 6000D;
    }

    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbability(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
    }

    @Override
    protected Stream<StationData> specificOrderStationsRent(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        return stationdata.sorted(rentComparator());
    }

    @Override
    protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        return stationdata.sorted(returnComparator());
    }

    private Comparator<StationData> rentComparator() {
        return (newSD, oldSD) -> {
     /*       if (newSD.probabilityTake >= this.parameters.desireableProbability
                    && oldSD.probabilityTake < this.parameters.desireableProbability) {
                return -1;
            }
            if (newSD.probabilityTake < this.parameters.desireableProbability
                    && oldSD.probabilityTake >= this.parameters.desireableProbability) {
                return 1;
            }*/
            double timediff = (newSD.walktime - oldSD.walktime);
            double probdiff = (newSD.probabilityTake - oldSD.probabilityTake) * parameters.probfactor;
            return Double.compare(timediff, probdiff);
        };
    }

    private Comparator<StationData> returnComparator() {
        return (newSD, oldSD) -> {
       /*     if (newSD.probabilityReturn >= this.parameters.desireableProbability
                    && oldSD.probabilityReturn < this.parameters.desireableProbability) {
                return -1;
            }
            if (newSD.probabilityReturn < this.parameters.desireableProbability
                    && oldSD.probabilityReturn >= this.parameters.desireableProbability) {
                return 1;
            }
*/
            double timediff = ((newSD.biketime + newSD.walktime)
                    - (oldSD.biketime + oldSD.walktime));
            double probdiff = (newSD.probabilityReturn - oldSD.probabilityReturn) * parameters.probfactor;
            return Double.compare(timediff, probdiff);
        };
    }

}
