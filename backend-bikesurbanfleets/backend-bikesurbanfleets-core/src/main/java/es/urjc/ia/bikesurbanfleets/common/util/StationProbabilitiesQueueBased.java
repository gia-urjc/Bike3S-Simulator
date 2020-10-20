package es.urjc.ia.bikesurbanfleets.common.util;

/**
 *
 * @author holger
 */
public class StationProbabilitiesQueueBased {
    double[] probabilities;
    int capacity;
    final double lambda;
    final double  my;
    double h;
    double endtime;
    public enum Type  {Euler, RungeKutta};
    final Updater updater;

    Type type;
    //lambda is the inrate
    //my is the out rate
    public StationProbabilitiesQueueBased(Type type, double h,double lambda, double my, int capacity, int initialnumberbikes) {
        this.capacity = capacity;
        this.lambda = lambda;
        this.my = my;
        this.h = h;
        double hcheck = 1D / (Math.max(lambda, my) * 2);
        if (this.h > hcheck) {
            this.h = hcheck;
        }
        this.endtime = 1; //the applied timeinterval for the rates is always 1
        if (type == Type.Euler) {
            updater = EulerUpdater;
        } else if (type == Type.RungeKutta) {
            updater = RungeKutta4Updater;
            k1 = new double[capacity + 1];
            k2 = new double[capacity + 1];
            k3 = new double[capacity + 1];
            k4 = new double[capacity + 1];
        } else {
            updater = null;
        }
        calculateProbs(initialnumberbikes);
    }
    
    //create a distribution with an alredy given start distribution
    //lambda is the inrate
    //my is the out rate
    public StationProbabilitiesQueueBased(double[] startDistribution, int changes,
            Type type, double h, double lambda, double my) {
        this.capacity = startDistribution.length-1;
        this.lambda = lambda;
        this.my = my;
        this.h = h;
        double hcheck = 1D / (Math.max(lambda, my) * 2);
        if (this.h > hcheck) {
            this.h = hcheck;
        }
        this.endtime = 1; //the applied timeinterval for rates is always 1
        if (type == Type.Euler) {
            updater = EulerUpdater;
        } else if (type == Type.RungeKutta) {
            updater = RungeKutta4Updater;
            k1 = new double[capacity + 1];
            k2 = new double[capacity + 1];
            k3 = new double[capacity + 1];
            k4 = new double[capacity + 1];
        } else {
            updater = null;
        }
        calculateProbsWithStartDistributionAndChanges(changes,startDistribution);
    }

    private void calculateProbs(int initialnumberbikes){
        if (endtime<h) 
            throw new RuntimeException("invalid endtime");

        double[] oldprobs = new double[capacity + 1];
        double[] newprobs = new double[capacity + 1];
        for (int i = 0; i <= capacity; i++) {
            oldprobs[i] = 0;
        }
        oldprobs[initialnumberbikes] = 1;

        double accumulated_h = 0;
        double[] auxpr;
        while (accumulated_h
                <= endtime) {
            updater.update(oldprobs, newprobs);
            accumulated_h = accumulated_h + h;
            auxpr = oldprobs;
            oldprobs = newprobs;
            newprobs = auxpr;
        }
        probabilities = oldprobs;
    }

    private void calculateProbsWithStartDistributionAndChanges(int changes,double[] startDistribution) {
        if (endtime < h) {
            throw new RuntimeException("invalid endtime");
        }
        
        //Copy and apply changes
        double[] oldprobs = new double[capacity + 1];
        double[] newprobs = new double[capacity + 1];
        if (changes<=0){
            oldprobs[0] = startDistribution[0];
            for (int i=1; i<=-changes; i++) {
                oldprobs[0] = oldprobs[0]+startDistribution[i];
            }
            for (int i=1; i<=capacity+changes; i++) {
                oldprobs[i] = startDistribution[i-changes];
            }
            for (int i=capacity+changes+1; i<=capacity; i++) {
                oldprobs[i] = 0;
            }           
        } else { // (changes>0)
            for (int i=0; i<changes; i++) {
                oldprobs[i] = 0;
            }
            for (int i=changes; i<capacity; i++) {
                oldprobs[i] = startDistribution[i-changes];
            }
            oldprobs[capacity]=startDistribution[capacity-changes];
            for (int i=capacity-changes+1; i<=capacity; i++) {
                oldprobs[capacity] = oldprobs[capacity]+startDistribution[i];
            }             
        }

        double accumulated_h = 0;
        double[] auxpr;
        while (accumulated_h<= endtime) {
            updater.update(oldprobs, newprobs);
            accumulated_h = accumulated_h + h;
            auxpr = oldprobs;
            oldprobs = newprobs;
            newprobs = auxpr;
        }
        probabilities = oldprobs;       
    }

    public double[] getProbabilityDistribution() {
        return probabilities;
    }
    //accumulated probability P(x>=k) (there are more or igual k bikes
    public double kOrMoreBikesProbability(int k) {
        double p = 0;
        for (int i = Math.max(0, k); i <= capacity; i++) {
            p = p + probabilities[i];
        }
        return p;
    }

    //accumulated probability P(x<=k) (there are more or igual k slots
    public double kOrMoreSlotsProbability(int k) {
        k = Math.min(capacity - k, capacity);
        double p = 0;
        for (int i = 0; i <= k; i++) {
            p = p + probabilities[i];
        }
        return p;
    }
    public interface Updater {
        void update(double[] oldprobs, double[] newprobs);
    }

    private double fi(double eiminus1, double ei, double eiplus1) {
        return lambda * eiminus1 - (lambda + my) * ei + my * eiplus1;
    }

    private double f0(double ei, double eiplus1) {
        return -lambda * ei + my * eiplus1;
    }

    private double fk(double eiminus1, double ei) {
        return lambda * eiminus1 - my * ei;
    }

    public Updater EulerUpdater = (oldprobs, newprobs) -> {
        //recalculate probs:
        //first state
        newprobs[0] = oldprobs[0] + h * f0(oldprobs[0], oldprobs[1]);;
        //middle states
        for (int i = 1; i < capacity; i++) {
            newprobs[i] = oldprobs[i] + h * fi(oldprobs[i - 1], oldprobs[i], oldprobs[i + 1]);
        }

        //last state
        newprobs[capacity] = oldprobs[capacity] + h * fk(oldprobs[capacity - 1], oldprobs[capacity]);
        return;
    };

    double[] k1;
    double[] k2;
    double[] k3;
    double[] k4;

    public Updater RungeKutta4Updater = (oldprobs, newprobs) -> {
        double a1 = h / 2D;
        //k1s
        k1[0] = f0(oldprobs[0], oldprobs[1]);
        for (int i = 1; i < capacity; i++) {
            k1[i] = fi(oldprobs[i - 1], oldprobs[i], oldprobs[i + 1]);
        }
        k1[capacity] = fk(oldprobs[capacity - 1], oldprobs[capacity]);
        //k2s
        k2[0] = f0(oldprobs[0] + k1[0] * a1, oldprobs[1] + k1[1] * a1);
        for (int i = 1; i < capacity; i++) {
            k2[i] = fi(oldprobs[i - 1] + k1[i - 1] * a1, oldprobs[i] + k1[i] * a1, oldprobs[i + 1] + k1[i + 1] * a1);
        }
        k2[capacity] = fk(oldprobs[capacity - 1] + k1[capacity - 1] * a1, oldprobs[capacity] + k1[capacity] * a1);
        //k3s
        k3[0] = f0(oldprobs[0] + k2[0] * a1, oldprobs[1] + k2[1] * a1);
        for (int i = 1; i < capacity; i++) {
            k3[i] = fi(oldprobs[i - 1] + k2[i - 1] * a1, oldprobs[i] + k2[i] * a1, oldprobs[i + 1] + k2[i + 1] * a1);
        }
        k3[capacity] = fk(oldprobs[capacity - 1] + k2[capacity - 1] * a1, oldprobs[capacity] + k2[capacity] * a1);
        //k4s
        k4[0] = f0(oldprobs[0] + k3[0] * h, oldprobs[1] + k3[1] * h);
        for (int i = 1; i < capacity; i++) {
            k4[i] = fi(oldprobs[i - 1] + k3[i - 1] * h, oldprobs[i] + k3[i] * h, oldprobs[i + 1] + k3[i + 1] * h);
        }
        k4[capacity] = fk(oldprobs[capacity - 1] + k3[capacity - 1] * h, oldprobs[capacity] + k3[capacity] * h);

        //newprobs
        for (int i = 0; i <= capacity; i++) {
            newprobs[i] = oldprobs[i] + (h * (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i])) / 6D;
        }
        return;
    };

}
