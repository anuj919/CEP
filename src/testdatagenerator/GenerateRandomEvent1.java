package testdatagenerator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import state.ConcurrentState;
import testdatagenerator.parser.ConfigFileParser;
import testdatagenerator.parser.ParseException;
import testdatagenerator.parser.RandomEventConfiguration;
import time.timestamp.IntervalTimeStamp;
import time.timestamp.TimeStamp;
import datastructures.SortedTreeList;
import event.AttributeType;
import event.EventClass;
import event.PrimaryEvent;
import event.eventtype.PrimaryEventType;
import event.util.TypeMismatchException;

public class GenerateRandomEvent1 {
	EventClass eClass;
	IntegerDistribution eventDistconfig;
	Reader reader;
	List<RandomEventConfiguration> eventConfigurations;
	
	public GenerateRandomEvent1(String fileName) throws FileNotFoundException, ParseException {
		//this.eventDistconfig = eventDistconfig;
		reader = new BufferedReader(new FileReader(fileName));
		eventConfigurations = new ConfigFileParser(reader).getEventConfig();
	}
	
	public int getNumEventClasses() {
		return eventConfigurations.size();
	}
	
	public void setDistribution(IntegerDistribution dist) {
		this.eventDistconfig = dist;
	}
	
	public PrimaryEvent generateEvent() {
		int random = eventDistconfig.sample();
		RandomEventConfiguration selected = eventConfigurations.get(random % eventConfigurations.size()); 
		EventClass eClass = selected.eClass; 
		PrimaryEventType et = (PrimaryEventType) eClass.getEventType();
		
		PrimaryEvent pe = new PrimaryEvent(eClass);
		
		try {
			for(String attName : et.getAttributeNames()) {
				AttributeType atype = et.getAttributeType(attName);
				switch(atype) {
				case Integer : 
					IntegerDistribution iDist = (IntegerDistribution)selected.map.get(attName).distConfig.distribution;
					pe.addAttributeValue(attName, iDist.sample());
					break;
				case Double:
					RealDistribution dDist = (RealDistribution)selected.map.get(attName).distConfig.distribution;
					pe.addAttributeValue(attName, dDist.sample());
					break;
				case String:
					IntegerDistribution sDist = (IntegerDistribution)selected.map.get(attName).distConfig.distribution;
					String[] values = selected.map.get(attName).strValues;
					pe.addAttributeValue(attName, values[sDist.sample()]);
					break;
				default:
					throw new RuntimeException("Not supported type");
				}
			}
		} catch(TypeMismatchException te) {
			te.printStackTrace(); // can not happen
		}
		
		return pe;
	}
	
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		int testcases = 20;
		String filePath = "spec.txt";
		GenerateRandomEvent1 generator = new GenerateRandomEvent1(filePath);
		//IntegerDistribution dist = new UniformIntegerDistribution(0,generator.getNumEventClasses()-1);
		IntegerDistribution dist = new BinomialDistribution(generator.getNumEventClasses()-1,0.3);
		generator.setDistribution(dist);
		//for(int i=0;i<testcases;i++)
		//	System.out.println(generator.generateEvent());
		EventClass[] eventClasses = new EventClass[2];
		//EventClass[] eventClasses = new EventClass[generator.getNumEventClasses()];
		//for(int i=0;i<generator.eventConfigurations.size();i++) {
		for(int i=0;i<2;i++) {
			RandomEventConfiguration config=generator.eventConfigurations.get(i);
			eventClasses[i]=config.eClass;
		}
		ConcurrentState state = new ConcurrentState(5,"E1.a1+E2.a2 < 5",Arrays.asList(eventClasses));
		
		List<TimeStamp> randomTimeStamps = getRandomTimeStamps(testcases);
		Iterator<TimeStamp> iterator = randomTimeStamps.iterator();
		
		for(int i=0;i<testcases;i++) {
			PrimaryEvent e = generator.generateEvent();
			e.setTimeStamp(iterator.next());
			System.out.println(e);
			//System.out.println(state.submitNext(e));
			/*for(ComplexEvent ce : state.submitNext(e)) {
				if(!ce.isConsumed()) {
					System.out.println(ce);
					ce.setConsumed(true);
				}
			}*/
		}
			
	}

	private static List<TimeStamp> getRandomTimeStamps(int howMany) {
		int lower=0; int upper=howMany/2;
		UniformIntegerDistribution dist = new UniformIntegerDistribution(lower,upper);
		
		List<TimeStamp> timeStampList = new SortedTreeList<TimeStamp>(IntervalTimeStamp.getComparator());
		for(int i=0;i<howMany;i++) {
			long t = dist.sample();
			timeStampList.add(new IntervalTimeStamp(t,t));
		}
		return timeStampList;
	}
	
	/*IntegerDistribution convertToDistribution(DistributionConfiguration config) {
		switch(config.distType) {
		case Uniform:
			int lower = config.params[0].intValue();
			int upper = config.params[1].intValue();
			return new UniformIntegerDistribution(lower,upper);
		case Zipf:
			int noOfElements = config.params[0].intValue();
			double exponent = config.params[1];
			return new ZipfDistribution(noOfElements, exponent);
		case Binomial:
			int trails = config.params[0].intValue();
			double probOfSuccess = config.params[1];
			return new BinomialDistribution(trails,probOfSuccess);
		case Poisson:
			double mean = config.params[0];
			double epsilon = config.params[1];
			return new PoissonDistribution(mean,epsilon);
		default: 
			throw new UnsupportedOperationException();
		}
	}
	
	RealDistribution getRealDistribution(DistributionConfiguration config) {
		switch(config.distType) {
		case Uniform:
			double lower = config.params[0];
			double upper = config.params[1];
			return new UniformRealDistribution(lower,upper);
		case Normal:
			double mean = config.params[0];
			double stdDev = config.params[1];
			return new NormalDistribution(mean,stdDev);
		case Triangular:
			lower = config.params[0];
			upper = config.params[1];
			double mode = config.params[2];
			return new TriangularDistribution(lower,upper,mode);
		case LogNormal:
			double scale = config.params[0];
			double shape = config.params[1];
			return new LogNormalDistribution(scale, shape);
		case Beta:
			double alpha = config.params[0];
			double beta = config.params[1];
			return new BetaDistribution(alpha,beta);
		case ChiSquare:
			double degreeOfFreedome = config.params[0];
			return new ChiSquaredDistribution(degreeOfFreedome);
		case Exponential:
			mean = config.params[0];
			return new ExponentialDistribution(mean);
		default:
			throw new UnsupportedOperationException();
		}
	}
	*/
}
