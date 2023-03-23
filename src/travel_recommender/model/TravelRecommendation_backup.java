package travel_recommender.model;

import java.util.ArrayList;
import java.util.List;

import travel_recommender.control.YensAlgorithm;
import travel_recommender.model.abstracts.Route;

public class TravelRecommendation_backup {
//	mockData_v2_chk - copy

	public static void main(String[] args) {
		Graph graph;
		String fromCity = "New York";
		String toCity = "Detroit";
		String transitMode = "any";
//		String transitMode = "air";
//		String transitMode = "bus";
//		String transitMode = "train";
		if(transitMode.equals("train")) {
			graph = new DynamicGraph("data/trainData.csv");
		}else if(transitMode.equals("bus")) {
				graph = new DynamicGraph("data/busData.csv");
		
		}else if(transitMode.equals("air")){
				graph = new DynamicGraph("data/airTravel.csv");
			
		}else {
				graph = new DynamicGraph("data/consolidated.csv");
		}
		YensAlgorithm yenAlg = new YensAlgorithm(graph);
		List<DirectedEdge> shortest_paths_list = yenAlg.get_yens_shortest_paths(
				graph.get_vertex(Graph.cityMap.get(fromCity)), graph.get_vertex(Graph.cityMap.get(toCity)), 1, 4);

		
		for (int i = 0; i < shortest_paths_list.size(); i++) {
			DirectedEdge path = shortest_paths_list.get(i);
			for (int j = 0; j < path._vertex_list.size() - 2; j++) {
				if (graph.get_edge_StartDate(path._vertex_list.get(j), path._vertex_list.get(j + 1))
						.compareTo(graph.get_edge_StartDate(path._vertex_list.get(j + 1),
								path._vertex_list.get(j + 2))) > 0) {
					shortest_paths_list.remove(i--);
				}
			}
		}

		for (DirectedEdge path : shortest_paths_list) {

			ArrayList<String> routeList = new ArrayList<String>();
			for (Route vertex : path._vertex_list) {
				routeList.add(Graph.cityMapInverse.get(vertex.get_id()));
			}
			path.paths.add(routeList);
		}

		System.out.println("Total route recommendations: "+yenAlg.get_all_routes().size()+"\n\n");
		String prev = "New York" ;
		for (DirectedEdge routes : shortest_paths_list) {
			for (List<String> rt : routes.paths) {
				int stop = 0;
				for (String place : rt) {
					if (stop == 0)
						System.out.println("source: " + place);
					else {
						if (stop == rt.size() - 1) {
							System.out.println(graph.get_edge_Mode(graph.get_vertex(Graph.cityMap.get(prev)),graph.get_vertex(Graph.cityMap.get(place))));
							System.out.println("destination: " + place);
						}
						else {
							
							System.out.println(graph.get_edge_Mode(graph.get_vertex(Graph.cityMap.get(prev)),graph.get_vertex(Graph.cityMap.get(place))));
							System.out.println("Stop " + stop + ": " + place);
						}
					}
					prev = place;
					stop++;
				}
			}
			System.out.println("cost: " + routes._weight);
			System.out.println("------------------------------------------------------------------------------\n");
		}
	}

}
