package es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints.config.distributions;

/**
 * This class represents the math distribution concept.
 * It forces all distribution instances to have a property which indicates what
 * type of distribution they represent.
 * It provides an enum type which serves to represent all kind of distributions
 * used at the system.
 * @author IAgroup
 *
 */
public class Distribution {

    public enum DistributionType {
        POISSON, RANDOM, NONEDISTRIBUTION
    }

    private DistributionType type;

    public Distribution(DistributionType type) {
        this.type = type;
    }

    public DistributionType getDistribution() {
        return type;
    }

    public void setDistribution(DistributionType distribution) {
        this.type = distribution;
    }


}
