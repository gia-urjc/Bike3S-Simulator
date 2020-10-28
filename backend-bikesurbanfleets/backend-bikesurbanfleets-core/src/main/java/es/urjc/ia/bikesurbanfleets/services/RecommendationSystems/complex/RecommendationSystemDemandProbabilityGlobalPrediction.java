package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import static es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.AbstractRecommendationSystemDemandProbabilityBased.costRentComparator;
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
@RecommendationSystemType("DEMAND_PROBABILITY_GLOBAL_PREDICTION")
public class RecommendationSystemDemandProbabilityGlobalPrediction extends AbstractRecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemDemandProbabilityBased.RecommendationParameters {

        private double factorProb = 2000;
        private double factorImp = 500D;
    }

    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbabilityGlobalPrediction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
    }

    @Override
    protected Stream<StationData> specificOrderStationsRent(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        return stationdata
                .map(sd -> {
                    double util = probutils.getGlobalProbabilityImprovementIfTake(sd);
                    sd.Utility = util;
                    return sd;
                })//apply function to calculate cost 
                .sorted(rentComparator());
    }

    @Override
    protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        return stationdata
                .map(sd -> {
                    double util = probutils.getGlobalProbabilityImprovementIfReturn(sd);
                    sd.Utility = util;
                    return sd;
                })//apply function to calculate cost
                .sorted(returnComparator());
    }

    protected Comparator<StationData> rentComparator() {
        return (newSD, oldSD) -> {
            double timediff = (newSD.walktime - oldSD.walktime);
            double utildiff = (newSD.Utility - oldSD.Utility) * this.parameters.factorImp;
            double probdiff = (newSD.probabilityTake - oldSD.probabilityTake) * this.parameters.factorProb;
            return Double.compare(timediff, (utildiff + probdiff));
        };
    }

    protected Comparator<StationData> returnComparator() {
        return (newSD, oldSD) -> {
            double timediff = (newSD.walktime + newSD.biketime - (oldSD.walktime + oldSD.biketime));
            double utildiff = (newSD.Utility - oldSD.Utility) * this.parameters.factorImp;
            double probdiff = (newSD.probabilityReturn - oldSD.probabilityReturn) * this.parameters.factorProb;
            return Double.compare(timediff, (utildiff + probdiff));
        };
    }
}
