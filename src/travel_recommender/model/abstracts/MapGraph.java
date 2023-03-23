package travel_recommender.model.abstracts;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface MapGraph
{
	List<Route> get_vertex_list();
	
	double get_edge_weight(Route source, Route sink);
	Date get_edge_StartDate(Route source, Route sink);
	long get_edge_Duration(Route source, Route sink);
	double get_edge_Cost(Route source, Route sink);
	Set<Route> get_adjacent_vertices(Route vertex);
	Set<Route> get_precedent_vertices(Route vertex);
}
