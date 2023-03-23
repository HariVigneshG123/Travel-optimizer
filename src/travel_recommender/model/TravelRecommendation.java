package travel_recommender.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import travel_recommender.control.YensAlgorithm;
import travel_recommender.model.abstracts.Route;

public class TravelRecommendation {
//	mockData_v2_chk - copy

	public static void main(String[] args) {
		Graph graph;
		String fromCity = "New York";
		String toCity = "Detroit";
		String transitMode = "TRAIN";
//		String transitMode = "AIR";
//		String transitMode = "BUS";
//		String transitMode = "train";
		if(transitMode.equals("TRAIN")) {
			graph = new DynamicGraph("data/trainData.csv");
		}else if(transitMode.equals("BUS")) {
				graph = new DynamicGraph("data/busData.csv");
		
		}else if(transitMode.equals("AIR")){
				graph = new DynamicGraph("data/airTravel.csv");
			
		}else {
				graph = new DynamicGraph("data/consolidated.csv");
		}
		YensAlgorithm yenAlg = new YensAlgorithm(graph);
		List<DirectedEdge> shortest_paths_list = yenAlg.get_yens_shortest_paths(
				graph.get_vertex(Graph.cityMap.get(fromCity)), graph.get_vertex(Graph.cityMap.get(toCity)), 500, 4);
		
		boolean[] show = new boolean[shortest_paths_list.size()];
		for (int i = 0; i < show.length; i++) {
			show[i] = true;
		}

		for (int i = 0; i < shortest_paths_list.size(); i++) {
			DirectedEdge path = shortest_paths_list.get(i);
			for (int j = 0; j < path._vertex_list.size() - 2; j++) {
				Date date = graph.get_edge_StartDate(path._vertex_list.get(j), path._vertex_list.get(j + 1));
				date = new Date(date.getTime() + (30 * 60 * 1000) + graph.get_edge_Duration(path._vertex_list.get(j), path._vertex_list.get(j + 1)));
				if (date
						.compareTo(graph.get_edge_StartDate(path._vertex_list.get(j + 1),
								path._vertex_list.get(j + 2))) > 0) {
					show[i] = false;
				}
			}
		}

		int index = 0, count = 0;
		for (DirectedEdge path : shortest_paths_list) {
			if(!show[index++]) continue;
			count++;
			ArrayList<String> routeList = new ArrayList<String>();
			for (Route vertex : path._vertex_list) {
				routeList.add(Graph.cityMapInverse.get(vertex.get_id()));
			}
			path.paths.add(routeList);
		}

		System.out.println("Total route recommendations: "+count+"\n\n");
		String prev = "New York" ;
		index = 0;
		for (DirectedEdge routes : shortest_paths_list) {
			if(!show[index++]) continue;
			for (List<String> rt : routes.paths) {
				int stop = 0;
				for (String place : rt) {
					if (stop == 0)
						System.out.println("source: " + place);
					else {
						if (stop == rt.size() - 1) {
							System.out.println("destination: " + place);
							
							System.out.println(graph.get_edge_Mode(graph.get_vertex(Graph.cityMap.get(prev)),graph.get_vertex(Graph.cityMap.get(place)))==null?transitMode:graph.get_edge_Mode(graph.get_vertex(Graph.cityMap.get(prev)),graph.get_vertex(Graph.cityMap.get(place))));
						}
						else {
							System.out.println(graph.get_edge_Mode(graph.get_vertex(Graph.cityMap.get(prev)),graph.get_vertex(Graph.cityMap.get(place)))==null?transitMode:graph.get_edge_Mode(graph.get_vertex(Graph.cityMap.get(prev)),graph.get_vertex(Graph.cityMap.get(place))));
							System.out.println("Stop " + stop + ": " + place);
						}
					}
					stop++;
				}
			}
			System.out.println("cost: " + routes._weight);
			System.out.println("------------------------------------------------------------------------------\n");
		}
	}

}
