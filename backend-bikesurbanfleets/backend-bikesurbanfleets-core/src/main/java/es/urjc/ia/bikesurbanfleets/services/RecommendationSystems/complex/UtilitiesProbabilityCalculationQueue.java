/*
Here probability is calculatest as follows:
P(finding a bike in sercain time)=P(x>=k) where k=1-currentbikes-expected bikes in future
The probability is calculated through skellam
That is here, expected bikes in the futer (or takes of bikes) are treated as if they have already happened

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import java.util.List;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.PastRecommendations;
import es.urjc.ia.bikesurbanfleets.common.util.ProbabilityDistributions;
import es.urjc.ia.bikesurbanfleets.common.util.StationProbabilitiesQueueBased;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

/**
 *
 * @author holger
 */
public class UtilitiesProbabilityCalculationQueue extends UtilitiesProbabilityCalculator {

    final private double maxAverageDistanceToStationForTake = 200D;
    final private double squaredMaxAverageDistanceToStationForTake = maxAverageDistanceToStationForTake * maxAverageDistanceToStationForTake;
    final private double maxAverageDistanceToStationForReturn = 6000D;
    final private double squaredMaxAverageDistanceToStationForReturn = maxAverageDistanceToStationForReturn * maxAverageDistanceToStationForReturn;
    final private double probabilityUsersObey;
    final private boolean takeintoaccountexpected;
    final private boolean takeintoaccountcompromised;
    final private PastRecommendations pastrecs;
    final private int additionalResourcesDesiredInProbability;
    final public static double h = 0.05D;
    final private double probabilityExponent;

    public UtilitiesProbabilityCalculationQueue(double probabilityExponent, DemandManager dm, PastRecommendations pastrecs, double probabilityUsersObey,
            boolean takeintoaccountexpected, boolean takeintoaccountcompromised, int additionalResourcesDesiredInProbability
    ) {
        this.dm = dm;
        this.probabilityExponent = probabilityExponent;
        this.pastrecs = pastrecs;
        this.probabilityUsersObey = probabilityUsersObey;
        this.takeintoaccountexpected = takeintoaccountexpected;
        this.takeintoaccountcompromised = takeintoaccountcompromised;
        if (additionalResourcesDesiredInProbability < 0 || additionalResourcesDesiredInProbability > 3) {
            throw new RuntimeException("invalid parameters");
        }
        this.additionalResourcesDesiredInProbability = additionalResourcesDesiredInProbability;
    }

    class IntTuple {

        int avcap;
        int avslots;
        int avbikes;
        int minpostchanges = 0;
        int maxpostchanges = 0;
        double takedemandrate;
        double returndemandrate;
        int changesInTime = 0;
    }

    // get current capacity and available bikes
    // this takes away reserved bikes and slots and takes into account expected changes
    public IntTuple getAvailableCapandBikes(Station s, double timeoffset) {
        IntTuple res = new IntTuple();
        res.avcap = s.getCapacity() - s.getReservedBikes() - s.getReservedSlots();
        res.avbikes = s.availableBikes();
        res.avslots = res.avcap - res.avbikes;
        res.takedemandrate = dm.getStationTakeRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        res.returndemandrate = dm.getStationReturnRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), 0, timeoffset);
            res.avbikes = res.avbikes + (int) Math.floor(er.changes * probabilityUsersObey);
            res.avslots = res.avslots - (int) Math.floor(er.changes * probabilityUsersObey);
            res.changesInTime = (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                res.maxpostchanges = (int) er.maxpostchanges;
                res.minpostchanges = (int) er.minpostchanges;
            }
            if (res.avbikes < 0) {
                res.avbikes = 0;
            }
            if (res.avbikes > res.avcap) {
                res.avbikes = res.avcap;
            }
            if (res.avslots < 0) {
                res.avslots = 0;
            }
            if (res.avslots > res.avcap) {
                res.avslots = res.avcap;
            }
            //modify the demandrates because of knowing some info
            //     double factor = (SimulationDateTime.getCurrentSimulationInstant()
            //             + timeoffset) - er.lastendinstantexpected;
            //     factor = Math.max(0, Math.min(1, factor));
            //     res.takedemandrate = res.takedemandrate * 0.5;
            //     res.returndemandrate = res.returndemandrate * 0.5;
        }
        return res;
    }

    private IntTuple getChangesInIntervall(Station s, double fromtimeoffset, double totimeoffset) {
        if (totimeoffset < fromtimeoffset || fromtimeoffset < 0) {
            throw new RuntimeException("error in data");
        }
        IntTuple res = new IntTuple();
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), fromtimeoffset, totimeoffset);
            res.changesInTime = (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                res.maxpostchanges = (int) er.maxpostchanges;
                res.minpostchanges = (int) er.minpostchanges;
            }
        }
        res.takedemandrate = dm.getStationTakeRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtimeoffset), totimeoffset - fromtimeoffset);
        res.returndemandrate = dm.getStationReturnRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtimeoffset), totimeoffset - fromtimeoffset);
        return res;
    }

    // Probabilities form now to timeoffset 
    public double calculateTakeProbability(Station s, double timeoffset) {
        IntTuple avCB = getAvailableCapandBikes(s, timeoffset);
        //if there are other demands already registered, dont' take the last bike
//        if (avCB.avbikes<=-avCB.minpostchanges && avCB.minpostchanges<0) {
        //           return 0;
        //       }
        int initialbikes = avCB.avbikes;//+avCB.minpostchanges;
        initialbikes = Math.max(Math.min(initialbikes, avCB.avcap), 0);
        StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta, h, avCB.returndemandrate,
                avCB.takedemandrate, avCB.avcap, initialbikes);
        int requiredbikes = 1 + additionalResourcesDesiredInProbability - avCB.minpostchanges;
        double prob = pc.kOrMoreBikesProbability(requiredbikes);
        return Math.pow(prob, probabilityExponent);
    }

    public double calculateReturnProbability(Station s, double timeoffset) {
        IntTuple avCB = getAvailableCapandBikes(s, timeoffset);
        //if there are other demands already registered, dont' take the last bike
//        if (avCB.avslots<=avCB.maxpostchanges && avCB.maxpostchanges>0) {
//            return 0;
        //       }
        int initialbikes = avCB.avcap - (avCB.avslots);//-avCB.maxpostchanges);
        initialbikes = Math.max(Math.min(initialbikes, avCB.avcap), 0);
        StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta, h, avCB.returndemandrate,
                avCB.takedemandrate, avCB.avcap, initialbikes);
        int requiredslots = 1 + additionalResourcesDesiredInProbability + avCB.maxpostchanges;
        double prob = pc.kOrMoreSlotsProbability(requiredslots);
        return Math.pow(prob, probabilityExponent);
    }

    //method for calculation the expected fails (rental and return) if a bike is taken at 
    //currenttime+aarivalofset for users that arrive between 
    //currenttime+aarivalofset and currenttime+aarivalofset+checkintervall
    @Override
    public ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutRent(Station s, double arrivaloffset, double checkintervall) {

        ExpectedUnsuccessData pd = new ExpectedUnsuccessData();

        //first analyse the probs if no take/return takes place or the take/return occures after currenttime+predictionoffset
        //(probability distribution  obtained from currenttime up to currenttime+totime) 
        IntTuple avCB = getAvailableCapandBikes(s, arrivaloffset);
        int initialbikes = Math.max(Math.min(avCB.avbikes, avCB.avcap), 0);
        StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta, h, avCB.returndemandrate,
                avCB.takedemandrate, avCB.avcap, initialbikes);
        //this is the probability distribution of bikes at the moment the user arrives
        double[] probdist = pc.getProbabilityDistribution();

        //get the deman rates
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) arrivaloffset), checkintervall);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) arrivaloffset), checkintervall);

        //now analyse the case where no bike is taken 
        StationProbabilitiesQueueBased pcnew = new StationProbabilitiesQueueBased(
                probdist, 0, StationProbabilitiesQueueBased.Type.RungeKutta, h, returndemandrate,
                takedemandrate);
        pd.expUnsuccessRentIfNoChange = pcnew.getExpectedRentFailors();
        pd.expUnsuccessReturnIfNoChange = pcnew.getExpectedReturnFailors();

        //now analyse the case where a bike is taken 
        pcnew = new StationProbabilitiesQueueBased(
                probdist, -1, StationProbabilitiesQueueBased.Type.RungeKutta, h, returndemandrate,
                takedemandrate);
        pd.expUnsuccessRentAfterTake = pcnew.getExpectedRentFailors();
        pd.expUnsuccessReturnAfterTake = pcnew.getExpectedReturnFailors();
        return pd;
    }

    //method for calculation the expected fails (rental and return) if a bike is taken at 
    //currenttime+aarivalofset for users that arrive between 
    //currenttime+aarivalofset and currenttime+aarivalofset+checkintervall
    @Override
    public ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutReturn(Station s, double arrivaloffset, double checkintervall) {

        ExpectedUnsuccessData pd = new ExpectedUnsuccessData();

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //case1: predictionoffset<arrivaloffset
        //     here we consider the take/return like q compromised bike, thus, 
        //     of one bike we calc the prob of getting two bikes (similar for all combinations)
        //first analyse the probs if no take/return takes place or the take/return occures after currenttime+predictionoffset
        //(probability distribution  obtained from currenttime up to currenttime+totime) 
        IntTuple avCB = getAvailableCapandBikes(s, arrivaloffset);
        int initialbikes = Math.max(Math.min(avCB.avbikes, avCB.avcap), 0);
        StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta, h, avCB.returndemandrate,
                avCB.takedemandrate, avCB.avcap, initialbikes);
        //this is the probability distribution of bikes at the moment the user arrives
        double[] probdist = pc.getProbabilityDistribution();

        //get the deman rates
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) arrivaloffset), checkintervall);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) arrivaloffset), checkintervall);

        //now analyse the case where no bike is returned 
        StationProbabilitiesQueueBased pcnew = new StationProbabilitiesQueueBased(
                probdist, 0, StationProbabilitiesQueueBased.Type.RungeKutta, h, returndemandrate,
                takedemandrate);
        pd.expUnsuccessRentIfNoChange = pcnew.getExpectedRentFailors();
        pd.expUnsuccessReturnIfNoChange = pcnew.getExpectedReturnFailors();

        //now analyse the case where a bike is returned 
        pcnew = new StationProbabilitiesQueueBased(
                probdist, 1, StationProbabilitiesQueueBased.Type.RungeKutta, h, returndemandrate,
                takedemandrate);
        pd.expUnsuccessRentAfterReturn = pcnew.getExpectedRentFailors();
        pd.expUnsuccessReturnAfterReturn = pcnew.getExpectedReturnFailors();
        return pd;
    }

    //method for calculation the expected fails (rental and return) in the surroundings
    //if a bike is taken at a station 
    //currenttime+aarivalofset for users that arrive between 
    //currenttime+aarivalofset and currenttime+aarivalofset+checkintervall
    public ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutRentSurrounding(
            Station s, double arrivaloffset, double checkintervall,
            double maxdistancesurrounding, List<Station> allStations) {

        ExpectedUnsuccessData pd = new ExpectedUnsuccessData();
        //get the deman rates
        double futuretakedemandrate = 0;
        double futurereturndemandrate = 0;
        double currenttakedemanrate = 0;
        double currentreturndemandrate = 0;
        double avbikes = 0;
        double capacity = 0;
        double factor;
        for (Station other : allStations) {
            double dist = s.getPosition().eucleadeanDistanceTo(other.getPosition());
            if (dist < maxdistancesurrounding) {
                //distance factor
                factor = (maxdistancesurrounding - dist) / maxdistancesurrounding;
                //available bikes, capacity and current demand rates
                IntTuple avCB = getAvailableCapandBikes(other, arrivaloffset);
                avbikes += factor * (double) avCB.avbikes;
                capacity += factor * (double) avCB.avcap;
                currenttakedemanrate += factor * avCB.takedemandrate;
                currentreturndemandrate += factor * avCB.returndemandrate;
                //future deman rates
                futuretakedemandrate += factor * dm.getStationTakeRateIntervall(other.getId(),
                        SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) arrivaloffset), checkintervall);
                futurereturndemandrate += factor * dm.getStationReturnRateIntervall(other.getId(),
                        SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) arrivaloffset), checkintervall);
            }
        }
        //normalize
        int initcapacity = (int) (Math.round(capacity));
        int initialbikes = (int) (Math.round(Math.max(Math.min(avbikes, capacity), 0)));

        //first analyse the probs if no take/return takes place or the take/return occures after currenttime+predictionoffset
        //(probability distribution  obtained from currenttime up to currenttime+totime) 
        StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta, h, currentreturndemandrate,
                currenttakedemanrate, initcapacity, initialbikes);
        //this is the probability distribution of bikes at the moment the user arrives
        double[] probdist = pc.getProbabilityDistribution();

        //now analyse the case where no bike is taken 
        StationProbabilitiesQueueBased pcnew = new StationProbabilitiesQueueBased(
                probdist, 0, StationProbabilitiesQueueBased.Type.RungeKutta, h, futurereturndemandrate,
                futuretakedemandrate);
        pd.expUnsuccessRentIfNoChange = pcnew.getExpectedRentFailors();
        pd.expUnsuccessReturnIfNoChange = pcnew.getExpectedReturnFailors();

        //now analyse the case where a bike is taken 
        pcnew = new StationProbabilitiesQueueBased(
                probdist, -1, StationProbabilitiesQueueBased.Type.RungeKutta, h, futurereturndemandrate,
                futuretakedemandrate);
        pd.expUnsuccessRentAfterTake = pcnew.getExpectedRentFailors();
        pd.expUnsuccessReturnAfterTake = pcnew.getExpectedReturnFailors();
        return pd;
    }

    public ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutReturnSurrounding(
            Station s, double arrivaloffset, double checkintervall,
            double maxdistancesurrounding, List<Station> allStations) {

        ExpectedUnsuccessData pd = new ExpectedUnsuccessData();
        //get the deman rates
        double futuretakedemandrate = 0;
        double futurereturndemandrate = 0;
        double currenttakedemanrate = 0;
        double currentreturndemandrate = 0;
        double avbikes = 0;
        double capacity = 0;
        double factor;
        for (Station other : allStations) {
            double dist = s.getPosition().eucleadeanDistanceTo(other.getPosition());
            if (dist < maxdistancesurrounding) {
            //distance factor
            factor = (maxdistancesurrounding -dist) / maxdistancesurrounding;
            //available bikes, capacity and current demand rates
            IntTuple avCB = getAvailableCapandBikes(other, arrivaloffset);
            avbikes += factor * (double) avCB.avbikes;
            capacity += factor * (double) avCB.avcap;
            currenttakedemanrate += factor * avCB.takedemandrate;
            currentreturndemandrate += factor * avCB.returndemandrate;
            //future deman rates
            futuretakedemandrate += factor * dm.getStationTakeRateIntervall(other.getId(),
                    SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) arrivaloffset), checkintervall);
            futurereturndemandrate += factor * dm.getStationReturnRateIntervall(other.getId(),
                    SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) arrivaloffset), checkintervall);
            }
        }
        //normalize
        int initcapacity = (int) (Math.round(capacity));
        int initialbikes = (int) (Math.round(Math.max(Math.min(avbikes, capacity), 0)));

        //first analyse the probs if no take/return takes place or the take/return occures after currenttime+predictionoffset
        //(probability distribution  obtained from currenttime up to currenttime+totime) 
        StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta, h, currentreturndemandrate,
                currenttakedemanrate, initcapacity, initialbikes);
        //this is the probability distribution of bikes at the moment the user arrives
        double[] probdist = pc.getProbabilityDistribution();

        //now analyse the case where no bike is taken 
        StationProbabilitiesQueueBased pcnew = new StationProbabilitiesQueueBased(
                probdist, 0, StationProbabilitiesQueueBased.Type.RungeKutta, h, futurereturndemandrate,
                futuretakedemandrate);
        pd.expUnsuccessRentIfNoChange = pcnew.getExpectedRentFailors();
        pd.expUnsuccessReturnIfNoChange = pcnew.getExpectedReturnFailors();

        //now analyse the case where a bike is taken 
        pcnew = new StationProbabilitiesQueueBased(
                probdist, 1, StationProbabilitiesQueueBased.Type.RungeKutta, h, futurereturndemandrate,
                futuretakedemandrate);
        pd.expUnsuccessRentAfterReturn = pcnew.getExpectedRentFailors();
        pd.expUnsuccessReturnAfterReturn = pcnew.getExpectedReturnFailors();
        return pd;
    }

    //methods for calculation probabilities    
    //calculates the probabilities of taking or returning bikes at a station at the moment 
    //currenttime+predictionoffset,
    // if (or if not) a bike is taken/returned at time currenttime+arrivaloffset
    // arrivaltime may be before or after the predictioninterval
    public ProbabilityData calculateFutureProbabilitiesWithAndWithoutArrival(Station s, double arrivaloffset, double predictionoffset) {

        ProbabilityData pd = new ProbabilityData();

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // now there are two different cases
        //case1: predictionoffset<arrivaloffset
        //     here we consider the take/return like q compromised bike, thus, 
        //     of one bike we calc the prob of getting two bikes (similar for all combinations)
        if (predictionoffset <= arrivaloffset) {

            //first analyse the probs if no take/return takes place or the take/return occures after currenttime+predictionoffset
            //(probability distribution  obtained from currenttime up to currenttime+totime) 
            IntTuple avCB = getAvailableCapandBikes(s, predictionoffset);
            int initialbikes = Math.max(Math.min(avCB.avbikes, avCB.avcap), 0);
            StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                    StationProbabilitiesQueueBased.Type.RungeKutta, h, avCB.returndemandrate,
                    avCB.takedemandrate, avCB.avcap, initialbikes);
            int requiredbikes = 1 + additionalResourcesDesiredInProbability - avCB.minpostchanges;
            int requiredslots = 1 + additionalResourcesDesiredInProbability + avCB.maxpostchanges;
            //probabilities if no take/return occurres
            pd.probabilityTake = Math.pow(pc.kOrMoreBikesProbability(requiredbikes), probabilityExponent);
            pd.probabilityReturn = Math.pow(pc.kOrMoreSlotsProbability(requiredslots), probabilityExponent);

            pd.probabilityTakeAfterTake = Math.pow(pc.kOrMoreBikesProbability(requiredbikes + 1), probabilityExponent);
            pd.probabilityReturnAfterTake = pd.probabilityReturn;
            pd.probabilityReturnAfterReturn = Math.pow(pc.kOrMoreSlotsProbability(requiredslots + 1), probabilityExponent);
            pd.probabilityTakeAfterRerturn = pd.probabilityTake;
        } //case2: predictionoffset>arrivaloffset
        //     here we calculate the distribution at currenttime+arrivaloffset; then add/return a bike
        //     and calculate the distribution from there to currenttime+predictionoffset 
        else {
            //calculate the probability distribution  obtained from currenttime up to currenttime+arrivaloffset 
            IntTuple avCB = getAvailableCapandBikes(s, arrivaloffset);
            int initialbikes = avCB.avbikes;
            initialbikes = Math.max(Math.min(initialbikes, avCB.avcap), 0);
            StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                    StationProbabilitiesQueueBased.Type.RungeKutta, h, avCB.returndemandrate,
                    avCB.takedemandrate, avCB.avcap, initialbikes);

            //analyse the case when no take or return takes place
            // and calculate the prob at currenttime+predictionoffset 
            IntTuple avCB2 = getChangesInIntervall(s, arrivaloffset, predictionoffset);
            int changes = avCB2.changesInTime;
            StationProbabilitiesQueueBased pcnew = new StationProbabilitiesQueueBased(
                    pc.getProbabilityDistribution(), changes,
                    StationProbabilitiesQueueBased.Type.RungeKutta, h, avCB2.returndemandrate,
                    avCB2.takedemandrate);
            int requiredbikes = 1 + additionalResourcesDesiredInProbability - avCB2.minpostchanges;
            int requiredslots = 1 + additionalResourcesDesiredInProbability + avCB2.maxpostchanges;
            pd.probabilityTake = Math.pow(pcnew.kOrMoreBikesProbability(requiredbikes), probabilityExponent);
            pd.probabilityReturn = Math.pow(pcnew.kOrMoreSlotsProbability(requiredslots), probabilityExponent);

            //analyse the case with a take at currenttime+arrivaloffset and calculate the prob at 
            // currenttime+predictionoffset 
            changes = avCB2.changesInTime - 1;
            pcnew = new StationProbabilitiesQueueBased(
                    pc.getProbabilityDistribution(), changes,
                    StationProbabilitiesQueueBased.Type.RungeKutta, h, avCB2.returndemandrate,
                    avCB2.takedemandrate);
            pd.probabilityTakeAfterTake = Math.pow(pcnew.kOrMoreBikesProbability(requiredbikes), probabilityExponent);
            pd.probabilityReturnAfterTake = Math.pow(pcnew.kOrMoreSlotsProbability(requiredslots), probabilityExponent);

            //analyse the case with a return at currenttime+arrivaloffset and calculate the prob at 
            // currenttime+predictionoffset 
            changes = avCB2.changesInTime + 1;
            pcnew = new StationProbabilitiesQueueBased(
                    pc.getProbabilityDistribution(), changes,
                    StationProbabilitiesQueueBased.Type.RungeKutta, h, avCB2.returndemandrate,
                    avCB2.takedemandrate);
            pd.probabilityReturnAfterReturn = Math.pow(pcnew.kOrMoreSlotsProbability(requiredslots), probabilityExponent);
            pd.probabilityTakeAfterRerturn = Math.pow(pcnew.kOrMoreBikesProbability(requiredbikes), probabilityExponent);
        }
        return pd;
    }

    // this case is the same as the one before, but the arrival is exactly the same as the predictionoffset
    // that is, we predict at currenttime+predictionoffset with and without 1 more bike taken or returned
    public ProbabilityData calculateFutureProbabilitiesWithAndWithoutArrival(Station sd, double predictionoffset) {
        return calculateFutureProbabilitiesWithAndWithoutArrival(sd, predictionoffset, predictionoffset);
    }

    //methods for calculation probabilities    
    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTake(Station s, double fromtime, double duration) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtime), duration);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, 1);
    }

    public double calculateProbabilityAtLeast1UserArrivingForReturn(Station s, double fromtime, double duration) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtime), duration);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, 1);
    }

    //methods for calculation probabilities    
    public double calculateExpectedTakes(Station s, double fromtime, double duration) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtime), duration);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFSkellamProbabilityTimesNumer(takedemandrate, returndemandrate, 1);
    }

    public double calculateExpectedReturns(Station s, double fromtime, double duration) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtime), duration);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFSkellamProbabilityTimesNumer(returndemandrate, takedemandrate, 1);
    }

    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(Station s, double fromtime, double duration) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFPoissonProbability(takedemandrate, 1);
    }

    public double calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(Station s, double fromtime, double duration) {
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(),
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int) fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFPoissonProbability(returndemandrate, 1);
    }

    public double getGlobalProbabilityImprovementIfTake(StationData sd) {
        int timeoffset = (int) sd.walktime;
        double futtakedemand = dm.getStationTakeRateIntervall(sd.station.getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futreturndemand = dm.getStationReturnRateIntervall(sd.station.getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobaltakedem = dm.getGlobalTakeRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobalretdem = dm.getGlobalReturnRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);

        double relativeimprovemente = (futtakedemand / futglobaltakedem)
                * (sd.probabilityTakeAfterTake - sd.probabilityTake)
                + (futreturndemand / futglobalretdem)
                * (sd.probabilityReturnAfterTake - sd.probabilityReturn);
        return relativeimprovemente;
    }

    public double getGlobalProbabilityImprovementIfReturn(StationData sd) {
        int timeoffset = (int) sd.biketime;
        double futtakedemand = dm.getStationTakeRateIntervall(sd.station.getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futreturndemand = dm.getStationReturnRateIntervall(sd.station.getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobaltakedem = dm.getGlobalTakeRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobalretdem = dm.getGlobalReturnRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);

        double relativeimprovemente = (futtakedemand / futglobaltakedem)
                * (sd.probabilityTakeAfterRerturn - sd.probabilityTake)
                + (futreturndemand / futglobalretdem)
                * (sd.probabilityReturnAfterReturn - sd.probabilityReturn);
        return relativeimprovemente;
    }
}
