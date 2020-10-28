package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.AbstractRecommendationSystemDemandProbabilityBased;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.time.LocalDateTime;

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
@RecommendationSystemType("DEMAND_PROBABILITY_GLOBAL_UTILITY")
public class RecommendationSystemDemandProbabilityGlobalUtilityOpenFunction extends AbstractRecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemDemandProbabilityBased.RecommendationParameters {

        private double upperProbabilityBound = 0.999;
        private double desireableProbability = 0.6;

        private double factorProb = 2000D;
        private double factorImp = 1000D;
    }
    private RecommendationParameters parameters;
    private UtilitiesGlobalLocalUtilityMethods recutils;

    public RecommendationSystemDemandProbabilityGlobalUtilityOpenFunction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
        recutils = new UtilitiesGlobalLocalUtilityMethods(getDemandManager());
    }

    @Override
    protected Stream<StationData> specificOrderStationsRent(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        return stationdata
                .map(sd -> {
                    double util = recutils.calculateOpenSquaredStationUtilityDifference(sd, true);
                    double normedUtilityDiff = util
                            * recutils.getDemandManager().getStationTakeRatePerHour(sd.station.getId(), current);
                    sd.Utility = normedUtilityDiff;
                    return sd;
                })//apply function to calculate cost 
                .sorted(rentComparator());
    }

    @Override
    protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        return stationdata
                .map(sd -> {
                    double util = recutils.calculateOpenSquaredStationUtilityDifference(sd, false);
                    double normedUtilityDiff = util
                            * recutils.getDemandManager().getStationReturnRatePerHour(sd.station.getId(), current);
                    sd.Utility = normedUtilityDiff;
                    return sd;
                })//apply function to calculate cost
                .sorted(returnComparator());
    }

    private Comparator<StationData> rentComparator() {
        return (newSD, oldSD) -> {
            double distdiff = (newSD.walktime - oldSD.walktime);
            double probdiff = (newSD.probabilityTake - oldSD.probabilityTake) * this.parameters.factorProb;
            double utildiff = (newSD.Utility - oldSD.Utility) * this.parameters.factorImp;
            return Double.compare(distdiff, (probdiff + utildiff));
        };
    }

    private Comparator<StationData> returnComparator() {
        return (newSD, oldSD) -> {
            double distdiff = (newSD.walktime - oldSD.walktime);
            double probdiff = (newSD.probabilityReturn - oldSD.probabilityReturn) * this.parameters.factorProb;
            double utildiff = (newSD.Utility - oldSD.Utility) * this.parameters.factorImp;
            return Double.compare(distdiff, (probdiff + utildiff));
        };
    }
}
