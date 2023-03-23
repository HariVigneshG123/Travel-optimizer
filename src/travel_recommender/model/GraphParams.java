package travel_recommender.model;

import java.util.Date;

public class GraphParams {
	
	private double weight = 0d;
	private double cost = 0d;
	private Date startDate = null;
	private long duration = 0l;
	private int weightFlag = 0;
	private String mode = "";
	
	public void setWeightFlag(String constraint) {
		weightFlag = constraint.equals("cost") ? 1 : 0;
	}
	
	public void setWeight() {
		weight = weightFlag==1 ? cost : duration;
	}
	
	public void setCost(double c) {
		cost = c;
	}
	
	public void setStartDate(Date s) {
		startDate = s;
	}
	
	public void setDuration(long d) {
		duration = d;
	}
	
	public int getWeightFlag() {
		return weightFlag;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public double getCost() {
		return cost;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public void setMode(String modes) {
		if(modes==null) {
			modes = "TRAIN";
		}
		mode = modes;
	}
	
	public String getMode() {
		return mode;
	}
	

}
