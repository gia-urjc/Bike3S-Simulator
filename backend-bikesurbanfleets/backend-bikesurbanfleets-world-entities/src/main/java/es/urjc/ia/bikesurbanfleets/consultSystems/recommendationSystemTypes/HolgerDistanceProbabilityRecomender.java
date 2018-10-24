package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import com.google.gson.JsonObject;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.common.util.SimpleRandom;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

@RecommendationSystemType("HOLGER_DistanceProbabilityRECOMENDER")
public class HolgerDistanceProbabilityRecomender  extends HolgerRecomender  {

   static public Comparator<StationData> byDistance() {
        return (s1, s2) -> Double.compare(s1.distance, s2.distance);
    }

   public HolgerDistanceProbabilityRecomender(JsonObject recomenderdef, InfraestructureManager infraestructureManager) throws Exception{
        super(recomenderdef, infraestructureManager);
    }

    protected void calculateFinalUtilities(List<StationData> stations, double stationutilityequilibrium, double closestsdistance) {

        List<StationData> temp = stations.stream().sorted(byDistance()).collect(Collectors.toList());
 
        StationData beststation=temp.get(0);
        //if closest station has enogh bikes go there
        if(beststation.stationUtility>=stationutilityequilibrium) {
            beststation.utility=1.0D;
            return;
        }
        // if best station is not the best one
        StationData alternativestation;
        for (int i=1; i<temp.size();i++) {
            alternativestation =temp.get(i);
            //only consider if the alternative station has higher utility of bikes
            if (alternativestation.stationUtility>beststation.stationUtility) {
                
            }
            //to do: select alternativestation if I can improve a lot and it is closed
            //
        }
    }


 }
