/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.infraestructure;

/**
 *
 * @author holger
 */
public class SellamDistribution {
    
    public static final double ACC = 4.0; 
    public static final double BIGNO = 1.0e10;
    public static final double BIGNI = 1.0e-10;

    public static void main(String[] args) {
        double a =calculateSkellamProbability(10,10,-11);
        System.out.printf("%10.6f", a);
     }

   // calculates prob of P(x=k) of my1 -my2
   public static double calculateSkellamProbability(double my1, double my2, int k){     
        if (my1==0){
            return 1-calculatePoissonProbability( my2,  -k+1);
        } else if (my2==0){
            return calculatePoissonProbability( my1,  k);
        }  
        if (my1<=0.0000000000001D  ||my2<=0.0000000000001D) {
            throw new RuntimeException(" my values to small for good results of distribution");
        }
        if (my1<=0.0000000000001D  ||my2<=0.0000000000001D) {
            throw new RuntimeException(" my values to small for good results of distribution");
        }
        double c=Math.exp(-(my1+my2));
        double ratio=Math.sqrt(my1/my2);
        double mean=Math.sqrt(my1*my2);        
        int j=Math.abs(k);
        double s;
        if (j==0) s= c*Math.pow(ratio,k)*bessi0(2D*mean);
        else if (j==1) s= c*Math.pow(ratio,k)*bessi1(2D*mean);
        else s= c*Math.pow(ratio,k)*bessi(j,2D*mean);
        if (s==Double.NaN || s>=1D) return 1D;
        return s;
    }

   static int highest_value=30;
   // calculates accumulated prob P(X>=k) of my1 -my2
   public static double calculateCDFSkellamProbability(double my1, double my2, int k){
      
        if (my1==0){
            return 1-calculateCDFPoissonProbability( my2,  -k+1);
        } else if (my2==0){
            return calculateCDFPoissonProbability( my1,  k);
        }  
        if (my1<=0.0000000000001D  ||my2<=0.0000000000001D) {
            throw new RuntimeException(" my values to small for good results of distribution");
        }
        double c=Math.exp(-(my1+my2));
        double ratio=Math.sqrt(my1/my2);
        double mean=Math.sqrt(my1*my2);
        double sum=0;
        for (int i=k; i<=highest_value;i++){
            int j=Math.abs(i);
            double s;
            if (j==0) s= c*Math.pow(ratio,i)*bessi0(2D*mean);
            else if (j==1) s= c*Math.pow(ratio,i)*bessi1(2D*mean);
            else s= c*Math.pow(ratio,i)*bessi(Math.abs(i),2D*mean);
            sum+=s;
        }
        if (sum==Double.NaN || sum>=1D) {
            sum=1D;
            
        }
        return sum;
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
        if (n < 2)
            throw new IllegalArgumentException("Function order must be greater than 1");
        if (x == 0.0) {
            return 0.0;
        } else {
            double tox = 2.0/Math.abs(x);
            double ans = 0.0;
            double bip = 0.0;
            double bi  = 1.0;
            for (int j = 2*(n + (int)Math.sqrt(ACC*n)); j > 0; --j) {
                double bim = bip + j*tox*bi;
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
            ans *= bessi0(x)/bi;
            return (((x < 0.0) && ((n % 2) == 0)) ? -ans : ans);
        }
    }
    
    //P(X==num)
    public static double calculatePoissonProbability(double my1, int num){
        if (my1 <0)
                throw new IllegalArgumentException("illegal arguments Poisson");
        if (num<0) return 0;
        if (my1==0 && num>0) return 0;
        if (my1==0 && num==0) return 1;
        //my1>0 && num>=0
        double c=Math.exp(-(my1));
        double v=c*Math.pow(my1, num)/Factorial(num);
        if (v==Double.NaN || v>=1D) return 1D;
        return v;
    }
    //P(X>=num)
    public static double calculateCDFPoissonProbability(double my1, int num){
        if (my1 <0)
                throw new IllegalArgumentException("illegal arguments Poisson");
        if (num<=0) return 1;
        if (my1==0 && num>0) return 0;
        if (my1==0 && num<=0) return 1;
        //my1>0 && num>0
        double c=Math.exp(-(my1));
        double sum=0;
        for (int i=Math.min(num, 0); i<=highest_value;i++){
            sum=sum + c*Math.pow(my1, i)/Factorial(i);
        }
        if (sum==Double.NaN || sum>=1D) {
            sum=1D;
        }
        return sum;
    }

    public static double Factorial(int i) {
        
        long factorial = 1; // declarar e inicializar factorial en 1
        
        while(i != 0)
        {
            factorial *= i;
            i--;
        }
        return i;
 
    }
}
