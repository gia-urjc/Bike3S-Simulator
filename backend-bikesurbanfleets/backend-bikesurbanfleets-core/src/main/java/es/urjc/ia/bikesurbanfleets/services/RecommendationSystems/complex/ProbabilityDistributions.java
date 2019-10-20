/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

/**
 *
 * @author holger
 */
public class ProbabilityDistributions {

    public static final double ACC = 4.0;
    public static final double BIGNO = 1.0e10;
    public static final double BIGNI = 1.0e-10;

    public static void main(String[] args) {
        for (int i = -30; i < 30; i++) {
            double lambda = 0.5;
            System.out.println("lambda " + lambda + " poissonCFD para k>=" + i + ": " + calculateCDFSkellamProbability(lambda, lambda / 2, i));
        }
        System.out.println();
        double a = calculateCDFPoissonProbability(1, 40);
        System.out.printf("%10.6f", a);
        System.out.println();
        double my1 = 2.375;
        double my2 = 8.0625;
        int desired = -18;
        int knownneg = -20;
        int knownpos = 20;
        System.out.println("desired " + desired + " known " + knownpos
                + " p(x>=" + desired + ")=" + conditionalCDFSkellamProbability(my1, my2, desired, 0)
                + " p(x>=" + desired + "|" + knownpos + ")=" + conditionalCDFSkellamProbability(my1, my2, desired, knownpos)
                + " p(x>=" + (desired - knownpos) + ")=" + conditionalCDFSkellamProbability(my1, my2, desired - knownpos, 0));

        System.out.println("desired " + desired + " known " + knownneg
                + " p(x>=" + desired + ")=" + conditionalCDFSkellamProbability(my1, my2, desired, 0)
                + " p(x>=" + desired + "|" + knownneg + ")=" + conditionalCDFSkellamProbability(my1, my2, desired, knownneg)
                + " p(x>=" + (desired - knownneg) + ")=" + conditionalCDFSkellamProbability(my1, my2, desired - knownneg, 0));
    }
    static double precision=10000000;
    private static double round(double n){
        
        double result= n*precision;
        return Math.round(result)/precision;
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
            return calculatePoissonProbability(my2, -k);
        } else if (my2 == 0) {
            return calculatePoissonProbability(my1, k);
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
        if (s == Double.NaN || s>1D) return 1D;
        if (s<0D) return 0D;
        return round(s);
    }

    // calculates accumulated prob P(X>=k) of my1 -my2
    public static double calculateCDFSkellamProbability(double my1, double my2, int k) {
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
            return 1 - calculateCDFPoissonProbability(my2, -k + 1);
        } else if (my2 == 0) {
            return calculateCDFPoissonProbability(my1, k);
        }
        double c = Math.exp(-(my1 + my2));
        double ratio = Math.sqrt(my1 / my2);
        double mean = Math.sqrt(my1 * my2);
        double sum = 0;
        int i;
        double prevs = 0;
        double s;
        if (k >= 0) {
            i = k;
            while (true) {
                int j = Math.abs(i);
                if (j == 0) {
                    s = c * Math.pow(ratio, i) * bessi0(2D * mean);
                } else if (j == 1) {
                    s = c * Math.pow(ratio, i) * bessi1(2D * mean);
                } else {
                    s = c * Math.pow(ratio, i) * bessi(Math.abs(i), 2D * mean);
                }
                sum += s;
                if (s < prevs && s < 1.0e-10) {
                    break;
                }
                prevs = s;
                i++;
            }
        } else {
            i = k-1;
            while (true) {
                int j = Math.abs(i);
                if (j == 0) {
                    s = c * Math.pow(ratio, i) * bessi0(2D * mean);
                } else if (j == 1) {
                    s = c * Math.pow(ratio, i) * bessi1(2D * mean);
                } else {
                    s = c * Math.pow(ratio, i) * bessi(Math.abs(i), 2D * mean);
                }
                sum += s;
                if (s < prevs && s < 1.0e-10) {
                    break;
                }
                prevs = s;
                i--;
            }
            sum=1-sum;
        }
        if (sum == Double.NaN || sum>1D) return 1D;
        if (sum<0D) return 0D;
        return round(sum);
    }

    public static final double bessi0(double x) {
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

    public static final double bessi1(double x) {
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

    public static final double bessi(int n, double x) {
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
        if (my1 < 0) {
            throw new IllegalArgumentException("illegal arguments Poisson");
        }
        if (my1 <= 1.0e-10) {
            my1 = 0;
        }
        if (num < 0) {
            return 0;
        }
        if (my1 == 0 && num > 0) {
            return 0;
        }
        if (my1 == 0 && num == 0) {
            return 1;
        }
        //my1>0 && num>=0
        double c = Math.exp(-(my1));
        double v = c * Math.pow(my1, num);
        for (int i = 2; i <= num; i++) {
            v = v / i;
        }
        if (v == Double.NaN || v>1D) return 1D;
        if (v<0D) return 0D;
        return round(v);
    }

    //P(X>=num)
    public static double calculateCDFPoissonProbability(double my1, int num) {
        if (my1 < 0) {
            throw new IllegalArgumentException("illegal arguments Poisson");
        }
        if (my1 <= 1.0e-10) {
            my1 = 0;
        }
        if (num <= 0) {
            return 1;
        }
        if (my1 == 0 && num > 0) {
            return 0;
        }
        //my1>0 && num>0
        double c = Math.exp(-(my1));
        double sum = 0;
        double prevv = 0D;
        int i = num;
        double v;
        while (true) {
            v = c * Math.pow(my1, i);
            for (int j = 2; j <= i; j++) {
                v = v / j;
            }
            sum = sum + v;
            if (v < prevv && v < 1.0e-10) {
                break;
            }
            prevv = v;
            i++;
        }
        if (sum == Double.NaN || sum>1D) return 1D;
        if (sum<0D) return 0D;
        return round(sum);
    }

    //my1 and my2 are the two parameters of the two poisson variables
    // Calculates the probability of having k events with the two parameters my1 and my2 and conditiont to that we
    //know that there are already b events compromised
    // b may ne positiv or negativa
    public static double conditionalCDFSkellamProbability(double my1, double my2, int k, int b) {
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
        if (b == 0) {
            result = calculateCDFSkellamProbability(my1, my2, k);
        } else if (b > 0) {
            double intersection = calculateCDFSkellamProbability(my1, my2, k);
            double resta = 0;
            for (int i = k; i < b; i++) {
                double positivepart = calculatePoissonProbability(my1, i);
                double negativepart = 0;
                for (int j = 0; j < i - k + 1; j++) {
                    negativepart += calculatePoissonProbability(my2, j);
                }
                resta = resta + (positivepart * negativepart);
            }
            intersection = intersection - resta;
            double P_Positive_mayorigual_b = calculateCDFPoissonProbability(my1, b);
            result = intersection / P_Positive_mayorigual_b;
        } else if (b < 0) {
            b = Math.abs(b);
            double intersection = calculateCDFSkellamProbability(my1, my2, k);
            for (int i = 0; i <= b - 1; i++) {
                double positivepart = calculateCDFPoissonProbability(my1, k + i);
                double negativepart = calculatePoissonProbability(my2, i);
                intersection = intersection - (positivepart * negativepart);
            }
            double P_Negative_mayorigual_b = calculateCDFPoissonProbability(my2, b);
            result = intersection / P_Negative_mayorigual_b;
        }
        if (result == Double.NaN || result>1D) return 1D;
        if (result<0D) return 0D;
        return round(result);
    }
}
