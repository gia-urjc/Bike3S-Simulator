package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes;

import com.google.gson.JsonObject;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfraestructureManager;

@RecommendationSystemType("HOLGER_FixedDistanceRECOMENDER")
public class HolgerFixedDistanceRecomender  extends HolgerRecomender  {

   static public Comparator<StationData> byDistance() {
        return (s1, s2) -> Double.compare(s1.distance, s2.distance);
    }

   public HolgerFixedDistanceRecomender(JsonObject recomenderdef, InfraestructureManager infraestructureManager) throws Exception{
        super(recomenderdef, infraestructureManager);
    }

    protected void calculateFinalUtilities(List<StationData> stations, double stationutilityequilibrium, double closestsdistance) {

        List<StationData> temp = stations.stream().sorted(byDistance()).collect(Collectors.toList());
 
        StationData beststation=temp.get(0);
        //if closest station has enogh bikes go there
        if(beststation.stationUtility>=3) {
            beststation.utility=1.0D;
            return;
        }
        beststation.utility=0.5D;
        StationData alternativestation;
        // if best station has only 2 or 1 bikes or slots
        if (beststation.stationUtility==2) {
            //accept station with more bikes within 300 meters 
            for(int i=1; i<temp.size();i++) {
                alternativestation =temp.get(i);
                //only consider if the alternative station has higher utility of bikes
                if (alternativestation.stationUtility>=3 && (alternativestation.distance-closestsdistance)<150) {
                    alternativestation.utility=1.0D;
                    return;
                }
            }
        }
        if (beststation.stationUtility==1) {
            //accept station with more bikes within 300 meters 
            for(int i=1; i<temp.size();i++) {
                alternativestation =temp.get(i);
                //only consider if the alternative station has higher utility of bikes
                if ((alternativestation.distance-closestsdistance)<300) {
                    if (alternativestation.stationUtility>=3) {
                        alternativestation.utility=1.0D;
                        return;
                    }
                    if (alternativestation.stationUtility>=2) {
                        alternativestation.utility=0.6D;
                    }
                }
            }
        }
        return;
    }


 }
