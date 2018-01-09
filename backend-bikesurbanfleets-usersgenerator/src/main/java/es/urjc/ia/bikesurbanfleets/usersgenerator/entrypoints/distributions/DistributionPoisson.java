package es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoints.distributions;


import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;

/**
 * This class represents a Poisson math distribution.
 * It contains the typical parameter that characterizes a Poisson math distribution.
 * @author IAgroup
 */
public class DistributionPoisson extends Distribution {
    /**
     * This is the Poisson distribution characteristic parameter.
     * It represents the number of users that appear per second.
     */
    private double lambda;

    public DistributionPoisson(DistributionType distribution, double lambda) {
        super(distribution);
        this.lambda = lambda;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    /**
     * It calculates, according to the distribution formula, the time between a user
     * appearance and the next user appearance.
     * It calculates an exponential instant given a lambda parameter.
     * @return a realistic exponential value given a lambda parameter.
     * @see <a href="https://en.wikipedia.org/wiki/Exponential_distribution#Generating_exponential_variates">Generating exponential variates</a>
     */
    public int randomInterarrivalDelay() {
        SimulationRandom random = SimulationRandom.getUserCreationInstance();
        double randomValue = Math.log(1.0 - random.nextDouble(Double.MIN_VALUE, 1));
        Double result = -randomValue /lambda;
        Long longResult = Math.round(result);
        return longResult.intValue();
    }

}