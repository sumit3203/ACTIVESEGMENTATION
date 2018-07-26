package com.customplugin.activeseg.filter_core;

import ij.process.ImageProcessor;


/* References -
    1. http://homepages.inf.ed.ac.uk/rbf/CVonline/LOCAL_COPIES/SHUTLER3/node10.html
    2. FAST COMPUTATION OF LEGENDRE AND ZERNIKE MOMENTS https://www.sciencedirect.com/science/article/pii/003132039500011N
    3. An Efficient Method for the Computation of Legendre Moments  IEEE TRANSACTIONS ON PATTERN ANALYSIS AND MACHINE INTELLIGENCE, VOL. 27, NO. 12, DECEMBER 2005
        (exact legendre moment calculation method has been followed)
 */


public class LegendreMoments_elm {

    private int degree_m;
    private int degree_n;
    private int highest_degree;
    private double [][] matrix_B;
    private double [][] matrix_Q_m;
    private double [][] matrix_Q_n;
    private int M,N;
    private int Dm, Dn;
    private int highest_dx;

    public LegendreMoments_elm(int degree_m, int degree_n){
        this.degree_m = degree_m;
        this.degree_n = degree_n;

        Dm = degree_m%2==0 ? degree_m/2 : (degree_m-1)/2;
        Dn = degree_n%2==0 ? degree_n/2 : (degree_n-1)/2;
        highest_degree = degree_m>degree_n ? degree_m:degree_n;
        highest_dx = Dm>Dn ? Dm : Dn;
        matrix_B = new double[highest_dx+1][highest_degree+1];
    }

    // calculates and stores value of Bernoulli coefficients using the recursive definition

    private double calculate_B(int k, int n, double [][] B){
        if(k==0 && n ==0){
            return 1.0;
        }
        else if(k==0){
            return (2.0*n-1)*calculate_B(k,n-1,B)/(n+1);
        }
        else if(k==1 && n==0){
            return 0;
        }
        else if(B[k][n]!= 0.0){
            return B[k][n];
        }
        else{
            return ((-1.0)*(n-k+1)*(n-2*k+3)*(n-2*k+2)*calculate_B(k-1,n,B))/((2*n-2*k+2)*(2*n-2*k+1)*k);
        }
    }

    // Returns Legendre moment of image of degree (m+n) in form of array of m*n

    public double[][] extractLegendreMoment(ImageProcessor ip){

        System.out.println("Start Legendre moment extraction process...");
        System.out.println();

        // Height and width of image
        M = ip.getHeight();
        N = ip.getWidth();

        // Calculating matrix B(k,n)
        for(int i=0;i<=highest_dx;i++){
            for(int j=0;j<=highest_degree;j++){
                matrix_B[i][j] = calculate_B(i,j,matrix_B);
            }
        }

        matrix_Q_m = new double[M][degree_m+1];
        matrix_Q_n = new double[N][degree_n+1];

        double DEL_X = 2.0/M;
        double DEL_Y = 2.0/N;

        double value;
        double x_i;
        int p;
        int dm,dn;

        // Calculating matrix Qm(x_i)

        for (int i=0;i<M;i++){
            x_i = -1.0+(i+0.5)*DEL_X;
            for(int n=0;n<=degree_m;n++){
                dm = n%2==0 ? n/2 : (n-1)/2;
                value= 0.0;
                for(int k=0;k<=dm;k++){
                    p = n-2*k+1;
                    value = value + matrix_B[k][n]*(Math.pow(x_i+DEL_X/2,p)-Math.pow(x_i-DEL_X/2,p));
                }
                matrix_Q_m[i][n] = (2*n+1)*value/2;
            }
        }

        // Calculating matrix Qn(y_i)

        for (int i=0;i<N;i++){
            x_i = -1+(i+0.5)*DEL_Y;
            for(int n=0;n<=degree_n;n++){
                dn = n%2==0 ? n/2 : (n-1)/2;
                value= 0.0;
                for(int k=0;k<=dn;k++){
                    p = n-2*k+1;
                    value = value + matrix_B[k][n]*(Math.pow(x_i+DEL_Y/2,p)-Math.pow(x_i-DEL_Y/2,p));
                }
                matrix_Q_n[i][n] = (2*n+1)*value/2;
            }
        }


        // Matrix which stores the Legendre moments up to order (m+n)
        double[][] moment_matrix = new double[degree_m+1][degree_n+1];

        // Calculation of moments (for each pair (m,n)) using the exact moment calculation

        double moment_value;
        for(int m= 0;m<=degree_m;m++) {
            for (int n = 0; n <= degree_n; n++) {
                moment_value = 0.0;
                for(int i=0;i<M;i++){
                    moment_value = moment_value + matrix_Q_m[i][m]*row_moment(i,n,N,ip);
                }
                moment_matrix[m][n] = moment_value;
            }
        }

        //return Legendre moments in form of degree_m*degree_n matrix
        return moment_matrix;
    }

    // returns nth order moment of the ith row,

    private double row_moment(int i, int n, int N, ImageProcessor ip){
        double row_moment_value = 0.0;
        for(int j=0;j<N;j++){
            row_moment_value = row_moment_value + matrix_Q_n[j][n]*(double) ip.getPixel(i,j);
        }
        return row_moment_value;
    }

}