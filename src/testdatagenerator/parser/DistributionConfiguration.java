package testdatagenerator.parser;

import java.util.Arrays;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.TriangularDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;

/* This class holds information regarding various probability
 * distributions.
 */

public class DistributionConfiguration {
	public Double params[];
	public enum DistributionType {UniformInteger, UniformReal, Zipf, Exponential, Normal, Binomial, Poisson, Triangular, LogNormal, Beta, ChiSquare}; 
	public DistributionType distType;
	public Object distribution;
	
	@Override
	public String toString() {
		return distType+"("+Arrays.toString(params)+")";
	}
	
	void setDistribution(DistributionType type, Double[] params) {
		this.params=params;
		this.distType = type;
		switch(type) {
		case UniformInteger:
			int lower = params[0].intValue();
			int upper = params[1].intValue();
			distribution = new UniformIntegerDistribution(lower,upper);
			break;
		case Zipf:
			int noOfElements = params[0].intValue();
			double exponent = params[1];
			distribution = new ZipfDistribution(noOfElements, exponent);
			break;
		case Binomial:
			int trails = params[0].intValue();
			double probOfSuccess = params[1];
			distribution = new BinomialDistribution(trails,probOfSuccess);
			break;
		case Poisson:
			double mean = params[0];
			double epsilon = params[1];
			distribution = new PoissonDistribution(mean,epsilon);
			break;
		/* Real Distribution */
		case UniformReal:
			double lowerD = params[0];
			double upperD = params[1];
			distribution = new UniformRealDistribution(lowerD,upperD);
			break;
		case Normal:
			double meanD = params[0];
			double stdDev = params[1];
			distribution =  new NormalDistribution(meanD,stdDev);
			break;
		case Triangular:
			lowerD = params[0];
			upperD = params[1];
			double mode = params[2];
			distribution =  new TriangularDistribution(lowerD,upperD,mode);
			break;
		case LogNormal:
			double scale = params[0];
			double shape = params[1];
			distribution =  new LogNormalDistribution(scale, shape);
			break;
		case Beta:
			double alpha = params[0];
			double beta = params[1];
			distribution =  new BetaDistribution(alpha,beta);
			break;
		case ChiSquare:
			double degreeOfFreedome = params[0];
			distribution =  new ChiSquaredDistribution(degreeOfFreedome);
			break;
		case Exponential:
			meanD = params[0];
			distribution =  new ExponentialDistribution(meanD);
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	
}
