package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

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
@RecommendationSystemType("DEMAND_PROBABILITY")
public final class RecommendationSystemDemandProbability extends RecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends RecommendationSystemDemandProbabilityBased.RecommendationParameters{
         double desireableProbability = 0.8;
         double probfactor = 6000D;
    }
    
    private RecommendationParameters parameters;
    public RecommendationSystemDemandProbability(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        getParameters(recomenderdef, parameters);
        this.parameters= (RecommendationParameters)(super.parameters);
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
            addrent(sd, orderedlist, maxdistance);
        }
        return orderedlist;
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
            addreturn(sd, orderedlist);
        }
        return orderedlist;
    }

    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
 /*       if (newSD.getProbabilityTake() >= this.parameters.desireableProbability
                && oldSD.getProbabilityTake() < this.parameters.desireableProbability) {
            return true;
        }
        if (newSD.getProbabilityTake() < this.parameters.desireableProbability
                && oldSD.getProbabilityTake() >= this.parameters.desireableProbability) {
            return false;
        }
   */     double timediff = (newSD.getWalkTime() - oldSD.getWalkTime());
    //    double pn=(Math.pow(newSD.getProbabilityTake(),2)+Math.log10(newSD.getProbabilityTake())+1)/2;
    //    double po=(Math.pow(oldSD.getProbabilityTake(),2)+Math.log10(oldSD.getProbabilityTake())+1)/2;
        double probdiff = (newSD.getProbabilityTake() - oldSD.getProbabilityTake()) * parameters.probfactor;
    //    double probdiff = (pn-po) * parameters.probfactor;
        return probdiff > timediff;
    }
 
    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
  /*      if (newSD.getProbabilityReturn() >= this.parameters.desireableProbability
                && oldSD.getProbabilityReturn() < this.parameters.desireableProbability) {
            return true;
        }
        if (newSD.getProbabilityReturn() < this.parameters.desireableProbability
                && oldSD.getProbabilityReturn() >= this.parameters.desireableProbability) {
            return false;
        }
  */      double timediff = ((newSD.getBiketime() + newSD.getWalkTime())
                - (oldSD.getBiketime() + oldSD.getWalkTime()));
    //    double pn=(Math.pow(newSD.getProbabilityReturn(),2)+Math.log10(newSD.getProbabilityReturn())+1)/2;
    //    double po=(Math.pow(oldSD.getProbabilityReturn(),2)+Math.log10(oldSD.getProbabilityReturn())+1)/2;
        double probdiff = (newSD.getProbabilityReturn() - oldSD.getProbabilityReturn()) * parameters.probfactor;
     //   double probdiff = (pn-po) * parameters.probfactor;
        return probdiff > timediff;
    }
}
