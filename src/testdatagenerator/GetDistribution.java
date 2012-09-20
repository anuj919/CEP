package testdatagenerator;

import net.sf.doodleproject.numerics4j.special.Erf;

public class GetDistribution {
	
	/* Given a predicate like a + b > VAL, get two distribution for
	 * a and b such that predicate statisfy with PROB probability
	 * 
	 * Input: VAL, PROB and comparision operator '>' or '<'
	 */
	
	public static void main(String[] args) {
		char operator = '<';
		double inputValueThreshold=5.0;
		double inputProbThreshold = 0.8;
		double mu1,mu2,stdv1,stdv2;
		
		
		if(operator=='<') {
			inputProbThreshold=1-inputProbThreshold;
			operator='>';
		}
		
		double reqProbThreshold,mean;
		if(inputProbThreshold>0.5) {
			reqProbThreshold = 1-inputProbThreshold;
			mean = inputValueThreshold+1;
		}
		else {
			reqProbThreshold = 1-inputProbThreshold;
			mean = inputValueThreshold-1;
		}
		
		double stddev = getsigma(inputValueThreshold, mean, reqProbThreshold);
		mu1=mu2=mean/2;
		stdv1=stddev/2;
		stdv2=Math.sqrt(stddev*stddev-stdv1*stdv1);
		
		System.out.println("("+mu1+","+stdv1+") , (" +mu2+","+stdv2+")") ;
		
		
		
		
	}
	
	/* this using CDF formula for Normal dist:
	 * CDF(x) = 1/2 * (1+erf( (x-mean)/(sqrt(2)*stddev)))
	 * Now we have x, CDF(x), mean, we want stddev
	 * So stddev = (x-mean) / (sqrt(2) * InverseErf(2*CDF(x) -1))
	 */
	public static double getsigma(double val, double mean, double probThreshold) {
		return (val-mean)/Math.sqrt(2) / Erf.inverseErf(2*probThreshold -1);
	}
	
	
}
