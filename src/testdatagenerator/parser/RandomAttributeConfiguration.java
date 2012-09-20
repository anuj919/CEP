package testdatagenerator.parser;

import java.util.Arrays;

import event.AttributeType;

public class RandomAttributeConfiguration {
	public String attrName;
	public AttributeType attrType;
	public DistributionConfiguration distConfig;
	
	public String[] strValues;
	
	@Override
	public String toString() {
		return attrName+":"+attrType+":"+distConfig+"("+ Arrays.toString(strValues)+")";
	}
}
