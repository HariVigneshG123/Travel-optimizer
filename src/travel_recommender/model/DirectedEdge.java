package travel_recommender.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import travel_recommender.model.abstracts.WeightedEdge;
import travel_recommender.model.abstracts.Route;

public class DirectedEdge implements WeightedEdge
{
	List<Route> _vertex_list = new Vector<Route>();
	double _weight = -1;
	public ArrayList<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
	
	public DirectedEdge(){};
	
	public DirectedEdge(List<Route> _vertex_list, double _weight)
	{
		this._vertex_list = _vertex_list;
		this._weight = _weight;
	}

	public double get_weight()
	{
		return _weight;
	}
	
	public void set_weight(double weight)
	{
		_weight = weight;
	}
	
	public List<Route> get_vertices()
	{
		return _vertex_list;
	}
	
	@Override
	public boolean equals(Object right)
	{
		if(right instanceof DirectedEdge)
		{
			DirectedEdge r_path = (DirectedEdge) right;
			return _vertex_list.equals(r_path._vertex_list);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return _vertex_list.hashCode();
	}
	
	public String toString()
	{
		return paths.toString()+":"+_weight;
	}
}
