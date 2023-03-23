package travel_recommender.control;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

import travel_recommender.model.Graph;
import travel_recommender.model.DirectedEdge;
import travel_recommender.model.abstracts.MapGraph;
import travel_recommender.model.abstracts.Route;

public class DijkstraAlgorithm
{
	// Input
	MapGraph _graph = null;

	// Intermediate variables
	Set<Route> _determined_vertex_set = new HashSet<Route>();
	PriorityQueue<Route> _vertex_candidate_queue = new PriorityQueue<Route>();
	Map<Route, Double> _start_vertex_distance_index = new HashMap<Route, Double>();
	
	Map<Route, Route> _predecessor_index = new HashMap<Route, Route>();

	public DijkstraAlgorithm(final MapGraph graph)
	{
		_graph = graph;
	}

	public void clear()
	{
		_determined_vertex_set.clear();
		_vertex_candidate_queue.clear();
		_start_vertex_distance_index.clear();
		_predecessor_index.clear();
	}

	public Map<Route, Double> get_source_distance_index()
	{
		return _start_vertex_distance_index;
	}

	public Map<Route, Route> get_previous_index()
	{
		return _predecessor_index;
	}

	public void get_shortest_path_tree(Route root)
	{
		determine_shortest_paths(root, null, true);
	}

	public void get_shortest_path_flower(Route root)
	{
		determine_shortest_paths(null, root, false);
	}

	protected void determine_shortest_paths(Route source_vertex, 
			Route sink_vertex, boolean is_source2sink)
	{
		// 0. clean up variables
		clear();
		
		// 1. initialize members
		Route end_vertex = is_source2sink ? sink_vertex : source_vertex;
		Route start_vertex = is_source2sink ? source_vertex : sink_vertex;
		_start_vertex_distance_index.put(start_vertex, 0d);
		start_vertex.set_weight(0d);
		_vertex_candidate_queue.add(start_vertex);

		// 2. start searching for the shortest path
		while(!_vertex_candidate_queue.isEmpty())
		{
			Route cur_candidate = _vertex_candidate_queue.poll();

			if(cur_candidate.equals(end_vertex)) break;

			_determined_vertex_set.add(cur_candidate);

			_improve_to_vertex(cur_candidate, is_source2sink);
		}
	}

	private void _improve_to_vertex(Route vertex, boolean is_source2sink)
	{
		// 1. get the neighboring vertices 
		Set<Route> neighbor_vertex_list = is_source2sink ? 
			_graph.get_adjacent_vertices(vertex) : _graph.get_precedent_vertices(vertex);
		// 2. update the distance passing on current vertex
		for(Route cur_adjacent_vertex : neighbor_vertex_list)
		{
			// 2.1 skip if visited before
			if(_determined_vertex_set.contains(cur_adjacent_vertex)) continue;
			
			// 2.2 calculate the new distance
			double distance = _start_vertex_distance_index.containsKey(vertex)?
					_start_vertex_distance_index.get(vertex) : Graph.DISCONNECTED;
					
			distance += is_source2sink ? _graph.get_edge_weight(vertex, cur_adjacent_vertex)
					: _graph.get_edge_weight(cur_adjacent_vertex, vertex);

			// 2.3 update the distance if necessary
			if(!_start_vertex_distance_index.containsKey(cur_adjacent_vertex) 
			|| _start_vertex_distance_index.get(cur_adjacent_vertex) > distance)
			{
				_start_vertex_distance_index.put(cur_adjacent_vertex, distance);

				_predecessor_index.put(cur_adjacent_vertex, vertex);
				
				cur_adjacent_vertex.set_weight(distance);
				_vertex_candidate_queue.add(cur_adjacent_vertex);
			}
		}
	}

	public DirectedEdge get_shortest_path(Route source_vertex, Route sink_vertex, int stops)
	{
		determine_shortest_paths(source_vertex, sink_vertex, true);
		//
		List<Route> vertex_list = new Vector<Route>();
		double weight = _start_vertex_distance_index.containsKey(sink_vertex) ?  
			_start_vertex_distance_index.get(sink_vertex) : Graph.DISCONNECTED;
		if(weight != Graph.DISCONNECTED)
		{
			Route cur_vertex = sink_vertex;
			do{
				if(vertex_list.size()+1>stops) {
					System.out.println("Stops count greater than 4");
					return new DirectedEdge();
				}
				vertex_list.add(cur_vertex);
				cur_vertex = _predecessor_index.get(cur_vertex);
			}while(cur_vertex != null && cur_vertex != source_vertex);
			//
			vertex_list.add(source_vertex);
			Collections.reverse(vertex_list);
		}
		//
		return new DirectedEdge(vertex_list, weight);
	}
	
	/// for updating the cost
	
	public DirectedEdge update_cost_forward(Route vertex)
	{
		double cost = Graph.DISCONNECTED;
		
		// 1. get the set of successors of the input vertex
		Set<Route> adj_vertex_set = _graph.get_adjacent_vertices(vertex);
		
		// 2. make sure the input vertex exists in the index
		if(!_start_vertex_distance_index.containsKey(vertex))
		{
			_start_vertex_distance_index.put(vertex, Graph.DISCONNECTED);
		}
		
		// 3. update the distance from the root to the input vertex if necessary
		for(Route cur_vertex : adj_vertex_set)
		{
			// 3.1 get the distance from the root to one successor of the input vertex
			double distance = _start_vertex_distance_index.containsKey(cur_vertex)?
					_start_vertex_distance_index.get(cur_vertex) : Graph.DISCONNECTED;
					
			// 3.2 calculate the distance from the root to the input vertex
			distance += _graph.get_edge_weight(vertex, cur_vertex);
			//distance += ((VariableGraph)_graph).get_edge_weight_of_graph(vertex, cur_vertex);
			
			// 3.3 update the distance if necessary 
			double cost_of_vertex = _start_vertex_distance_index.get(vertex);
			if(cost_of_vertex > distance)
			{
				_start_vertex_distance_index.put(vertex, distance);
				_predecessor_index.put(vertex, cur_vertex);
				cost = distance;
			}
		}
		
		// 4. create the sub_path if exists
		DirectedEdge sub_path = null;
		if(cost < Graph.DISCONNECTED) 
		{
			sub_path = new DirectedEdge();
			sub_path.set_weight(cost);
			List<Route> vertex_list = sub_path.get_vertices();
			vertex_list.add(vertex);
			
			Route sel_vertex = _predecessor_index.get(vertex);
			while(sel_vertex != null)
			{
				vertex_list.add(sel_vertex);
				sel_vertex = _predecessor_index.get(sel_vertex);
			}
		}
		
		return sub_path;
	}

	public void correct_cost_backward(Route vertex)
	{
		// 1. initialize the list of vertex to be updated
		List<Route> vertex_list = new LinkedList<Route>();
		vertex_list.add(vertex);
		
		// 2. update the cost of relevant precedents of the input vertex
		while(!vertex_list.isEmpty())
		{
			Route cur_vertex = vertex_list.remove(0);
			double cost_of_cur_vertex = _start_vertex_distance_index.get(cur_vertex);
			
			Set<Route> pre_vertex_set = _graph.get_precedent_vertices(cur_vertex);
			for(Route pre_vertex : pre_vertex_set)
			{
				double cost_of_pre_vertex = _start_vertex_distance_index.containsKey(pre_vertex) ?
						_start_vertex_distance_index.get(pre_vertex) : Graph.DISCONNECTED;
						
				double fresh_cost = cost_of_cur_vertex + _graph.get_edge_weight(pre_vertex, cur_vertex);
				//double fresh_cost = cost_of_cur_vertex + ((VariableGraph)_graph).get_edge_weight_of_graph(pre_vertex, cur_vertex);
				if(cost_of_pre_vertex > fresh_cost)
				{
					_start_vertex_distance_index.put(pre_vertex, fresh_cost);
					_predecessor_index.put(pre_vertex, cur_vertex);
					vertex_list.add(pre_vertex);
				}
			}
		}
	}
	
}
