package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
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

    public class RecommendationParameters {

        private double upperProbabilityBound = 0.999;
        private double desireableProbability = 0.6;

        private double factorProb = 2000D;
        private double factorImp = 1000D;

        @Override
        public String toString() {
            return "upperProbabilityBound=" + upperProbabilityBound + ", desireableProbability=" + desireableProbability + ", factorProb=" + factorProb + ", factorImp=" + factorImp ;
        }
    }
    public String getParameterString(){
        return "RecommendationSystemDemandProbabilityGlobalUtilityOpenFunction Parameters{"+ super.getParameterString() + this.parameters.toString() + "}";
    }
    private RecommendationParameters parameters;
    private UtilitiesGlobalLocalUtilityMethods recutils;

    public RecommendationSystemDemandProbabilityGlobalUtilityOpenFunction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        super(recomenderdef, ss);
        //***********Parameter treatment*****************************
        //if this recomender has parameters this is the right declaration
        //if no parameters are used this code just has to be commented
        //"getparameters" is defined in USER such that a value of Parameters 
        // is overwritten if there is a values specified in the jason description of the recomender
        // if no value is specified in jason, then the orriginal value of that field is mantained
        // that means that teh paramerts are all optional
        // if you want another behaviour, then you should overwrite getParameters in this calss
        this.parameters = new RecommendationParameters();
        getParameters(recomenderdef, this.parameters);
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
