package travel_recommender.test;

import java.util.List;

import org.junit.Test;

import travel_recommender.model.Graph;
import travel_recommender.model.DirectedEdge;
import travel_recommender.model.DynamicGraph;
import travel_recommender.control.DijkstraAlgorithm;
import travel_recommender.control.YensAlgorithm;

public class YenTopKShortestPathsAlgTest
{
	// The graph should be initiated only once to guarantee the correspondence 
	// between vertex id and node id in input text file. 
	static Graph graph = new DynamicGraph("data/test_6_2");
	
	//@Test
	public void testDijkstraShortestPathAlg()
	{
		System.out.println("Testing Dijkstra Shortest Path Algorithm!");
		DijkstraAlgorithm alg = new DijkstraAlgorithm(graph);
		System.out.println(alg.get_shortest_path(graph.get_vertex(4), graph.get_vertex(5),1));
	}
	
	@Test
	public void testYenShortestPathsAlg()
	{		
		System.out.println("Testing batch processing of top-k shortest paths!");
		YensAlgorithm yenAlg = new YensAlgorithm(graph);
		List<DirectedEdge> shortest_paths_list = yenAlg.get_yens_shortest_paths(
				graph.get_vertex(4), graph.get_vertex(5), 100, 3);
		System.out.println(":"+shortest_paths_list);
		System.out.println(yenAlg.get_all_routes().size());	
	}
	
	//@Test
	public void testYenShortestPathsAlg2()
	{
		System.out.println("Obtain all paths in increasing order! - updated!");
		YensAlgorithm yenAlg = new YensAlgorithm(
				graph, graph.get_vertex(4), graph.get_vertex(5));
		int i=0;
		while(yenAlg.has_next())
		{
			System.out.println("Path "+i+++" : "+yenAlg.next());
		}
		
		System.out.println("Result # :"+i);
		System.out.println("Candidate # :"+yenAlg.get_all_options_size());
		System.out.println("All generated : "+yenAlg.get_generated_route_size());
		
	}
	
}
