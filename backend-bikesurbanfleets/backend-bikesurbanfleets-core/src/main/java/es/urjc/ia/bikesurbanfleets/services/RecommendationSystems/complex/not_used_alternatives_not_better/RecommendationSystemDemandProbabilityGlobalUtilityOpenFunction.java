package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.RecommendationSystemDemandProbabilityBased;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

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
public class RecommendationSystemDemandProbabilityGlobalUtilityOpenFunction extends RecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends RecommendationSystemDemandProbabilityBased.RecommendationParameters{
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
        this.parameters= (RecommendationParameters)(super.parameters);
        recutils = new UtilitiesGlobalLocalUtilityMethods(getDemandManager());
    }
    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        LocalDateTime current=SimulationDateTime.getCurrentSimulationDateTime();
        for (StationUtilityData sd : stationdata) {
            double util = recutils.calculateOpenSquaredStationUtilityDifference(sd, true);
            double normedUtilityDiff = util
                * recutils.getDemandManager().getStationTakeRatePerHour(sd.getStation().getId(),current);
            sd.setUtility(normedUtilityDiff);
            addrent(sd, orderedlist, maxdistance);
        }
        return orderedlist;
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        LocalDateTime current=SimulationDateTime.getCurrentSimulationDateTime();
        for (StationUtilityData sd : stationdata) {
            double util = recutils.calculateOpenSquaredStationUtilityDifference(sd, false);
            double normedUtilityDiff = util
                * recutils.getDemandManager().getStationReturnRatePerHour(sd.getStation().getId(),current);
            sd.setUtility(normedUtilityDiff);
            addreturn(sd, orderedlist);
        }
        return orderedlist;
    }
 
    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        double distdiff = (newSD.getWalkdist() - oldSD.getWalkdist());
        double probdiff = (newSD.getProbabilityTake() - oldSD.getProbabilityTake()) * this.parameters.factorProb;
        double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
        if ((probdiff + utildiff) > (distdiff)) {
            return true;
        }
        return false;
    }

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        double distdiff = (newSD.getWalkdist() - oldSD.getWalkdist());
        double probdiff = (newSD.getProbabilityReturn()- oldSD.getProbabilityReturn()) * this.parameters.factorProb;
        double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
        if ((probdiff + utildiff) > (distdiff)) {
            return true;
        }
        return false;
    }
}
