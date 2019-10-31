/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.common.util;

import javassist.compiler.Javac;
import org.apache.commons.math3.distribution.PoissonDistribution;
import sun.security.pkcs11.P11Util;

/**
 *
 * @author holger
 */
public class ProbabilityDistributions {

    public static final double ACC = 4.0;
    public static final double BIGNO = 1.0e10;
    public static final double BIGNI = 1.0e-10;

    private static double roundprecision = 10000000D;//10000000D;
    private static double checkTerminationvalue = 1.0e-8;

    public static void main(String[] args) {
        double b = new PoissonDistribution(0.0001).cumulativeProbability(5);
        System.out.println(b);
        double lambda = 0.5;
        double lambda2 = 0.2;
        long lasttime = System.currentTimeMillis();
        /*       for (int i = -30; i < 30+1; i++) {
            calculateUpCDFSkellamProbability(lambda, lambda2, i);
            System.out.println("lambda " + lambda + " skellamCFD para k>=" + i + ": " + calculateUpCDFSkellamProbability(lambda, lambda2, i)+ " "+
                   calculateUpCDFSkellamProbabilityIterative(lambda, lambda2 , i) + " " +
                   calculateUpCDFSkellamProbabilityIterative2(lambda, lambda2 , i) + " " );
        }
        long duration=System.currentTimeMillis()-lasttime;
        System.out.println("time: "+ duration);

        for (int i = -30; i < 30+1; i++) {
            System.out.println("lambda " + lambda + " poissonCFD para k>=" + i + ": " + calculateUpCDFPoissonProbability(lambda, i));
        }
        
        lasttime=System.currentTimeMillis();
        for (int i = -30; i < 30+1; i++) {
            calculateUpCDFSkellamProbabilityIterative(lambda, lambda2, i);
        }
        duration=System.currentTimeMillis()-lasttime;
        
        System.out.println("time: "+ duration);
        
         */ System.out.println();
        b = calculatePoissonProbability(0.000001, 3);
        System.out.println(b);
        System.out.println();
        double my1 = 5;
        double my2 = 1.875;
        int desired = 2;
        int knownpos = 1;
        int knownneg = -1;

        int known = 1;
        double p1 = conditionalUpCDFSkellamProbability(my1, my2, desired, 0);
        double p2 = conditionalUpCDFSkellamProbability(my1, my2, desired, known);
        double p3 = conditionalUpCDFSkellamProbability(my1, my2, desired - known, 0);
        double porcentajep2 = (p2 - p1) * 100 / (p3 - p1);
        System.out.println("desired " + desired + " known " + known
                + " p(x>=" + desired + ")=" + p1
                + " p(x>=" + desired + "|" + known + ")=" + p2
                + " p(x>=" + (desired - knownpos) + ")=" + p3 + " porcentaje: " + porcentajep2);
        for (my1 = 0; my1 <= 3; my1 += 0.5) {
//        for (my2 = 0.1; my2 < 3; my2 += 0.3) {
            System.out.println("my1:" + my1 + " my2:" + my2);
            p1 = conditionalUpCDFSkellamProbability(my1, my2, desired, 0);
            p2 = conditionalUpCDFSkellamProbability(my1, my2, desired, knownpos);
            p3 = conditionalUpCDFSkellamProbability(my1, my2, desired - knownpos, 0);
            porcentajep2 = (p2 - p1) * 100 / (p3 - p1);
            System.out.println("desired " + desired + " known " + knownpos
                    + " p(x>=" + desired + ")=" + p1
                    + " p(x>=" + desired + "|" + knownpos + ")=" + p2
                    + " p(x>=" + (desired - knownpos) + ")=" + p3 + " porcentaje: " + porcentajep2);
        }
        System.out.println("negastive");

//        for (my2 = 0.1; my2 < 3; my2 += 0.3) {
        for (my1 = 0; my1 <= 3; my1 += 0.5) {
            System.out.println("my1:" + my1 + " my2:" + my2);
            p1 = conditionalUpCDFSkellamProbability(my1, my2, desired, 0);
            p2 = conditionalUpCDFSkellamProbability(my1, my2, desired, knownneg);
            p3 = conditionalUpCDFSkellamProbability(my1, my2, desired - knownneg, 0);
            porcentajep2 = (p2 - p1) * 100 / (p3 - p1);
            System.out.println("desired " + desired + " known " + knownneg
                    + " p(x>=" + desired + ")=" + p1
                    + " p(x>=" + desired + "|" + knownneg + ")=" + p2
                    + " p(x>=" + (desired - knownneg) + ")=" + p3 + " porcentaje: " + porcentajep2);
        }
    }

    private static double round(double n) {

        double result = n * roundprecision;
        return Math.round(result) / roundprecision;
    }

    // calculates prob of P(x=k) of my1 -my2
    public static double calculateSkellamProbability(double my1, double my2, int k) {
        if (my1 < 0D || my2 < 0D) {
            throw new RuntimeException(" invalid values my");
        }
        if (my1 <= 1.0e-10) {
            my1 = 0;
        }
        if (my2 <= 1.0e-10) {
            my2 = 0;
        }

        if (my1 == 0) {
            return poissonProbabilityHelper(my2, -k);
        } else if (my2 == 0) {
            return poissonProbabilityHelper(my1, k);
        }
        double c = Math.exp(-(my1 + my2));
        double ratio = Math.sqrt(my1 / my2);
        double mean = Math.sqrt(my1 * my2);
        int j = Math.abs(k);
        double s;
        if (j == 0) {
            s = c * Math.pow(ratio, k) * bessi0(2D * mean);
        } else if (j == 1) {
            s = c * Math.pow(ratio, k) * bessi1(2D * mean);
        } else {
            s = c * Math.pow(ratio, k) * bessi(j, 2D * mean);
        }
        if (Double.isNaN(s) || s > 1D) {
            return 1D;
        }
        if (s < 0D) {
            return 0D;
        }
        return round(s);
    }

    // calculates accumulated prob P(X>=k) of my1 -my2
    public static double calculateUpCDFSkellamProbability(double my1, double my2, int k) {
        if (my1 < 0D || my2 < 0D) {
            throw new RuntimeException(" invalid values my");
        }
        if (my1 <= 1.0e-10) {
            my1 = 0;
        }
        if (my2 <= 1.0e-10) {
            my2 = 0;
        }

        double result = 0;
        if (my1 == 0 && my2 == 0) { //special case; no changes expected
            if (k <= 0) {
                result = 1;
            } else {
                result = 0;
            }
        } else if (my1 == 0) {
            result = poissonCumulativeDownHelper(my2, -k);
        } else if (my2 == 0) {
            result = round(poissonCumulativeUpHelper(my1, k));
        } else { //my1>0 and my2>0
            double c = Math.exp(-(my1 + my2));
            double ratio = Math.sqrt(my1 / my2);
            double mean = Math.sqrt(my1 * my2);
            int i;
            double s;
            int meancheck;
            result = 0;
            if (k >= 0) {
                i = k;
                meancheck = (int) Math.ceil(my1 - my2);
                while (true) {
                    int j = Math.abs(i);
                    if (j == 0) {
                        s = c * Math.pow(ratio, i) * bessi0(2D * mean);
                    } else if (j == 1) {
                        s = c * Math.pow(ratio, i) * bessi1(2D * mean);
                    } else {
                        s = c * Math.pow(ratio, i) * bessi(Math.abs(i), 2D * mean);
                    }
                    result += s;
                    if (i > meancheck && s < checkTerminationvalue) {
                        break;
                    }
                    i++;
                }
            } else {
                i = k - 1;
                meancheck = (int) Math.floor(my1 - my2);
                while (true) {
                    int j = Math.abs(i);
                    if (j == 0) {
                        s = c * Math.pow(ratio, i) * bessi0(2D * mean);
                    } else if (j == 1) {
                        s = c * Math.pow(ratio, i) * bessi1(2D * mean);
                    } else {
                        s = c * Math.pow(ratio, i) * bessi(Math.abs(i), 2D * mean);
                    }
                    result += s;
                    if ((i < meancheck) && s < checkTerminationvalue) {
                        break;
                    }
                    i--;
                }
                result = 1 - result;
            }
        }
        if (Double.isNaN(result) || result > 1D) {
            return 1D;
        }
        if (result < 0D) {
            return 0D;
        }
        return round(result);
    }

// calculates accumulated prob P(X>=k) of my1 -my2
// iterating on the negative poisson
    private static double calculateUpCDFSkellamProbabilityIterative(double my1, double my2, int k) {
        if (my1 < 0D || my2 < 0D) {
            throw new RuntimeException(" invalid values my");
        }
        if (my1 <= 1.0e-10) {
            my1 = 0;
        }
        if (my2 <= 1.0e-10) {
            my2 = 0;
        }
        double result = 0;
        if (my1 == 0 && my2 == 0) { //special case; no changes expected
            if (k <= 0) {
                result = 1;
            } else {
                result = 0;
            }
        } else if (my1 == 0) {
            result = poissonCumulativeDownHelper(my2, -k);
        } else if (my2 == 0) {
            result = poissonCumulativeUpHelper(my1, k);
        } else { //my1>0 and my2>0
            //otherwise iterate
            PoissonDistribution positivedist = new PoissonDistribution(my1);
            PoissonDistribution negativedist = new PoissonDistribution(my2);
            int j = 0;
            double prevvalor = 0D;
            int meanneg = (int) Math.floor(my2);
            int meanpos = (int) Math.ceil(my1);
            result = 0;
            while (true) {
                double negativepart = negativedist.probability(j);
                double positivepart = poissonCumulativeUpHelper(positivedist, k + j);
                double valor = (negativepart * positivepart);
                result += valor;
                if (j > meanneg && negativepart < checkTerminationvalue
                        && (k + j) > meanpos && positivepart < checkTerminationvalue) {
                    break;
                }
                prevvalor = valor;
                j++;
            }
        }
        if (Double.isNaN(result) || result > 1D) {
            return 1D;
        }
        if (result < 0D) {
            return 0D;
        }
        return round(result);
    }

    // calculates accumulated prob P(X>=k) of my1 -my2
    // iterating on the positive Poisson
    private static double calculateUpCDFSkellamProbabilityIterative2(double my1, double my2, int k) {
        if (my1 < 0D || my2 < 0D) {
            throw new RuntimeException(" invalid values my");
        }
        if (my1 <= 1.0e-10) {
            my1 = 0;
        }
        if (my2 <= 1.0e-10) {
            my2 = 0;
        }
        double result = 0;
        if (my1 == 0 && my2 == 0) { //special case; no changes expected
            if (k <= 0) {
                result = 1;
            } else {
                result = 0;
            }
        } else if (my1 == 0) {
            result = poissonCumulativeDownHelper(my2, -k);
        } else if (my2 == 0) {
            result = poissonCumulativeUpHelper(my1, k);
        } else { //my1>0 and my2>0
            //otherwise iterate
            PoissonDistribution positivedist = new PoissonDistribution(my1);
            PoissonDistribution negativedist = new PoissonDistribution(my2);
            int i = 0;
            double prevvalor = 0D;
            int meanneg = (int) Math.floor(my2);
            int meanpos = (int) Math.ceil(my1);
            result = 0;
            while (true) {
                double positivepart = positivedist.probability(i);
                double negativepart = negativedist.cumulativeProbability(i - k);
                double valor = (negativepart * positivepart);
                result += valor;
                if (i > meanpos && positivepart < checkTerminationvalue
                        && (i - k) > meanneg && (1 - negativepart) < checkTerminationvalue) {
                    break;
                }
                prevvalor = valor;
                i++;
            }
        }
        if (Double.isNaN(result) || result > 1D) {
            return 1D;
        }
        if (result < 0D) {
            return 0D;
        }
        return round(result);
    }

    private static final double bessi0(double x) {
        double answer;
        double ax = Math.abs(x);
        if (ax < 3.75) { // polynomial fit
            double y = x / 3.75;
            y *= y;
            answer = 1.0 + y * (3.5156229 + y * (3.0899424 + y * (1.2067492 + y * (0.2659732 + y * (0.360768e-1 + y * 0.45813e-2)))));
        } else {
            double y = 3.75 / ax;
            answer = 0.39894228 + y * (0.1328592e-1 + y * (0.225319e-2 + y * (-0.157565e-2 + y * (0.916281e-2 + y * (-0.2057706e-1 + y * (0.2635537e-1 + y * (-0.1647633e-1 + y * 0.392377e-2)))))));
            answer *= (Math.exp(ax) / Math.sqrt(ax));
        }
        return answer;
    }

    private static final double bessi1(double x) {
        double answer;
        double ax = Math.abs(x);
        if (ax < 3.75) { // polynomial fit
            double y = x / 3.75;
            y *= y;
            answer = ax * (0.5 + y * (0.87890594 + y * (0.51498869 + y * (0.15084934 + y * (0.2658733e-1 + y * (0.301532e-2 + y * 0.32411e-3))))));
        } else {
            double y = 3.75 / ax;
            answer = 0.2282967e-1 + y * (-0.2895312e-1 + y * (0.1787654e-1 - y * 0.420059e-2));
            answer = 0.39894228 + y * (-0.3988024e-1 + y * (-0.362018e-2 + y * (0.163801e-2 + y * (-0.1031555e-1 + y * answer))));
            answer *= (Math.exp(ax) / Math.sqrt(ax));
        }
        return answer;
    }

    private static final double bessi(int n, double x) {
        if (n < 2) {
            throw new IllegalArgumentException("Function order must be greater than 1");
        }
        if (x == 0.0) {
            return 0.0;
        } else {
            double tox = 2.0 / Math.abs(x);
            double ans = 0.0;
            double bip = 0.0;
            double bi = 1.0;
            for (int j = 2 * (n + (int) Math.sqrt(ACC * n)); j > 0; --j) {
                double bim = bip + j * tox * bi;
                bip = bi;
                bi = bim;
                if (Math.abs(bi) > BIGNO) {
                    ans *= BIGNI;
                    bi *= BIGNI;
                    bip *= BIGNI;
                }
                if (j == n) {
                    ans = bip;
                }
            }
            ans *= bessi0(x) / bi;
            return (((x < 0.0) && ((n % 2) == 0)) ? -ans : ans);
        }
    }

    //P(X==num)
    public static double calculatePoissonProbability(double my1, int num) {
        if (my1 < 0D) {
            throw new RuntimeException(" invalid values my");
        }
        if (my1 <= 1.0e-10) {
            my1 = 0;
        }
        return round(poissonProbabilityHelper(my1, num));
    }

    //P(X>=num)
    public static double calculateUpCDFPoissonProbability(double my1, int num) {
        if (my1 < 0D) {
            throw new RuntimeException(" invalid values my");
        }
        if (my1 <= 1.0e-10) {
            my1 = 0;
        }
        return round(poissonCumulativeUpHelper(my1, num));
    }

    //helper classes 
    //P(X=num)
    private static double poissonProbabilityHelper(double my1, int num) {
        if (num < 0) {
            return 0;
        }
        if (my1 == 0 && num > 0) {
            return 0;
        }
        if (my1 == 0 && num == 0) {
            return 1;
        }
        //else my1>0 and num>0
        PoissonDistribution p = new PoissonDistribution(my1);
        return p.probability(num);
    }

    //P(X<=num)
    private static double poissonCumulativeDownHelper(double my1, int num) {
        if (num <= 0) {
            return 0;
        }
        if (my1 == 0 && num > 0) {
            return 1;
        }
        //else my1>0 and num>0
        PoissonDistribution p = new PoissonDistribution(my1);
        return p.cumulativeProbability(num);
    }

    //P(X>=num)
    private static double poissonCumulativeUpHelper(double my1, int num) {
        if (num <= 0) {
            return 1;
        }
        if (my1 == 0 && num > 0) {
            return 0;
        }
        //else my1>0 and num>0
        PoissonDistribution p = new PoissonDistribution(my1);
        return poissonCumulativeUpHelper(p, num);
    }

    //P(X>=num)
    private static double poissonCumulativeUpHelper(PoissonDistribution p, int num) {
        return (1 - p.cumulativeProbability(num - 1));
    }

    //my1 and my2 are the two parameters of the two poisson variables
    // Calculates the probability of having k events with the two parameters my1 and my2 and conditiont to that we
    //know that there are already b events compromised
    // b may ne positiv or negativa
    public static double conditionalUpCDFSkellamProbability(double my1, double my2, int k, int b) {
        if (my1 < 0D || my2 < 0D) {
            throw new RuntimeException(" invalid values my");
        }
        if (my1 <= 1.0e-10) {
            my1 = 0;
        }
        if (my2 <= 1.0e-10) {
            my2 = 0;
        }

        if (b == 0) {
            return calculateUpCDFSkellamProbability(my1, my2, k);
        }

        //now bis different to 0
        if (my1 == 0 && my2 == 0) { //special case; no changes expected
            if (k - b <= 0) {
                return 1;
            } else {
                return 0;
            }
        }

        double denominador = 0;
        double intersection = 0;
        if (my1 == 0) { //special case; no positive expected
            if (b > 0) {
                intersection = poissonCumulativeDownHelper(my2, -(k - b));
                denominador = 1;
            } else { //b<0
                if (b < k) {
                    intersection = 0;
                    denominador = 1;
                } else { //b>=k
                    PoissonDistribution p = new PoissonDistribution(my2);
                    intersection = p.cumulativeProbability(-b - 1, -k);
                    denominador = poissonCumulativeUpHelper(p, -b);
                }
            }
        } else if (my2 == 0) {//special case; no negative expected
            if (b > 0) {
                PoissonDistribution p = new PoissonDistribution(my1);
                intersection = poissonCumulativeUpHelper(p, Math.max(k, b));
                denominador = poissonCumulativeUpHelper(p, b);
            } else {// b < 0) 
                PoissonDistribution p = new PoissonDistribution(my1);
                intersection = poissonCumulativeUpHelper(my1, k - b);
                denominador = 1;
                //            intersection = poissonCumulativeUpHelper(p, Math.max(k, b));
                //            denominador = poissonCumulativeUpHelper(p, b);
            }
        } else { //my1>0 and my2>0 normal case
            if (b > 0) {
                intersection = calculateIntersectionCDFSkellamProbabilityVersion1(my1, my2, k, b);
                denominador = poissonCumulativeUpHelper(my1, b);
            } else {// b < 0) 
                intersection = calculateIntersectionCDFSkellamProbabilityVersion1(my1, my2, k, b);
                denominador = poissonCumulativeUpHelper(my2, -b);
            }
        }
        //now determine the results
        //in case of any uncertainty or error return comparative_value2
        double comparative_value1 = calculateUpCDFSkellamProbability(my1, my2, k);
        double comparative_value2 = calculateUpCDFSkellamProbability(my1, my2, k - b);
        //checking for small imprecision in calculations
        if (denominador < intersection && denominador > 0.0000001) {
            denominador = intersection;
        }

        double result = intersection / denominador;
        if (denominador < intersection || denominador <= 0D || Double.isNaN(result)) {
            result = comparative_value2;
        }
        if (b > 0) {
            // it has to fullfill: P(X>=k)<=P(X>=k|b)<=P(x>=k-b)
            if (comparative_value2 < comparative_value1) {
                throw new RuntimeException("invalid values");
            }
            if (result < comparative_value1) {
                result = comparative_value1;
            }
            if (result > comparative_value2) {
                result = comparative_value2;
            }
        } else { //b<0
            // it has to fullfill: P(X>=k)>=P(X>=k|b)>=P(x>=k-b)
            if (comparative_value2 > comparative_value1) {
                throw new RuntimeException("invalid values");
            }
            if (result > comparative_value1) {
                result = comparative_value1;
            }
            if (result < comparative_value2) {
                result = comparative_value2;
            }
        }
        return round(result);
    }

    // calculates accumulated prob P(X>=k |known) with my1 -my2
    // Calculates the probability of having k events with the two parameters my1 and my2 and conditiont to that we
    //know that there are already b events compromised
    // b may ne positiv or negativa
    // iterating on the negative poisson
    private static double calculateIntersectionCDFSkellamProbabilityVersion1(double my1, double my2, int k, int known) {
        // iterate
        PoissonDistribution positivedist = new PoissonDistribution(my1);
        PoissonDistribution negativedist = new PoissonDistribution(my2);
        double result = 0;
        int j = Math.max(0, -known);
        double prevvalor = 0D;
        int meanneg = (int) Math.floor(my2);
        int meanpos = (int) Math.ceil(my1);
        while (true) {
            double negativepart = negativedist.probability(j);
            double positivepart = poissonCumulativeUpHelper(positivedist, Math.max(k + j, known));
            double valor = (negativepart * positivepart);
            result += valor;
            if (j > meanneg && negativepart < checkTerminationvalue
                    && Math.max(k + j, known) > meanpos && positivepart < checkTerminationvalue) {
                break;
            }
            prevvalor = valor;
            j++;
        }
        return result;
    }

}
