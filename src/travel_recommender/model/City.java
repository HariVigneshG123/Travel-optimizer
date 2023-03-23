package travel_recommender.model;

import travel_recommender.model.abstracts.Route;

public class City implements Route, Comparable<City>
{
	private static int CURRENT_VERTEX_NUM = 0;
	private int _id = CURRENT_VERTEX_NUM++;
	private double _weight = 0;

	public int get_id()
	{
		return _id;
	}

	public String toString()
	{
		return ""+_id;
	}

	public double get_weight()
	{
		return _weight;
	}
	
	public void set_weight(double status)
	{
		_weight = status;
	}
	
	public int compareTo(City r_vertex)
	{
		double diff = this._weight - r_vertex._weight;
		if(diff > 0)
			return 1;
		else if(diff < 0)
			return -1;
		else 
			return 0;
	}
}
