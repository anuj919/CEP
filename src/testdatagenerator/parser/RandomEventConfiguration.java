package testdatagenerator.parser;

import java.util.Map;

import org.apache.commons.math3.distribution.IntegerDistribution;

import event.EventClass;

/*
 * This class holds information about given event-class such as:
 * class name, class type etc., and name, type and probability 
 * distribution of each attribute
 */

public class RandomEventConfiguration {
	public EventClass eClass;
	public Map<String, RandomAttributeConfiguration> map;
}
