package travel_recommender.model.abstracts;

public interface Edge
{
	int get_weight();
	
	Route get_start_vertex();
	Route get_end_vertex();
}
