package travel_recommender.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import travel_recommender.model.Graph;
import travel_recommender.model.Match;
import travel_recommender.model.DirectedEdge;
import travel_recommender.model.PriorityQueue;
import travel_recommender.model.DynamicGraph;
import travel_recommender.model.abstracts.MapGraph;
import travel_recommender.model.abstracts.Route;

public class YensAlgorithm
{
	private DynamicGraph _graph = null;

	private List<DirectedEdge> _result_list = new Vector<DirectedEdge>();
	private Map<DirectedEdge, Route> _path_derivation_vertex_index = new HashMap<DirectedEdge, Route>();
	private PriorityQueue<DirectedEdge> _path_candidates = new PriorityQueue<DirectedEdge>();
	
	private Route _source_vertex = null;
	private Route _target_vertex = null;

	private int _generated_path_num = 0;

	private int _stops = Integer.MAX_VALUE;

	public YensAlgorithm(MapGraph graph)
	{
		this(graph, null, null);		
	}

	public YensAlgorithm(MapGraph graph, 
			Route source_vt, Route target_vt)
	{
		if(graph == null)
		{
			throw new IllegalArgumentException("A NULL graph object occurs!");
		}
		//
		_graph = new DynamicGraph((Graph)graph);
		_source_vertex = source_vt;
		_target_vertex = target_vt;
		//
		_init();
	}

	private void _init()
	{
		clear();
		if(_source_vertex != null && _target_vertex != null)
		{
			DirectedEdge shortest_path = get_current_dijkstra_shortest_path(_source_vertex, _target_vertex);
			if(!shortest_path.get_vertices().isEmpty())
			{
				_path_candidates.add(shortest_path);
				_path_derivation_vertex_index.put(shortest_path, _source_vertex);				
			}
		}
	}

	public void clear()
	{
		_path_candidates = new PriorityQueue<DirectedEdge>();
		_path_derivation_vertex_index.clear();
		_result_list.clear();
		_generated_path_num = 0;
	}

	public DirectedEdge get_current_dijkstra_shortest_path(Route source_vt, Route target_vt)
	{
		DijkstraAlgorithm dijkstra_alg = new DijkstraAlgorithm(_graph);
		return dijkstra_alg.get_shortest_path(source_vt, target_vt, _stops);
	}

	public boolean has_next()
	{
		return !_path_candidates.isEmpty();
	}

	public DirectedEdge next()
	{
		DirectedEdge cur_path = _path_candidates.poll();
		_result_list.add(cur_path);

		Route cur_derivation = _path_derivation_vertex_index.get(cur_path);
		int cur_path_hash = 
			cur_path.get_vertices().subList(0, cur_path.get_vertices().indexOf(cur_derivation)).hashCode();
		
		int count = _result_list.size();
		
		for(int i=0; i<count-1; ++i)
		{
			DirectedEdge cur_result_path = _result_list.get(i);
							
			int cur_dev_vertex_id = 
				cur_result_path.get_vertices().indexOf(cur_derivation);
			
			if(cur_dev_vertex_id < 0) continue;

			int path_hash = cur_result_path.get_vertices().subList(0, cur_dev_vertex_id).hashCode();
			if(path_hash != cur_path_hash) continue;
			
			Route cur_succ_vertex = 
				cur_result_path.get_vertices().get(cur_dev_vertex_id+1);
			
			_graph.remove_edge(new Match<Integer,Integer>(
					cur_derivation.get_id(), cur_succ_vertex.get_id()));
		}
		
		int path_length = cur_path.get_vertices().size();
		List<Route> cur_path_vertex_list = cur_path.get_vertices();
		for(int i=0; i<path_length-1; ++i)
		{
			_graph.remove_vertex(cur_path_vertex_list.get(i).get_id());
			_graph.remove_edge(new Match<Integer,Integer>(
					cur_path_vertex_list.get(i).get_id(), 
					cur_path_vertex_list.get(i+1).get_id()));
		}

		DijkstraAlgorithm reverse_tree = new DijkstraAlgorithm(_graph);
		reverse_tree.get_shortest_path_flower(_target_vertex);
		
		boolean is_done = false;
		for(int i=path_length-2; i>=0 && !is_done; --i)
		{
			Route cur_recover_vertex = cur_path_vertex_list.get(i);			
			_graph.recover_removed_vertex(cur_recover_vertex.get_id());
			
			if(cur_recover_vertex.get_id() == cur_derivation.get_id()) 
			{
				is_done = true;
			}

			DirectedEdge sub_path = reverse_tree.update_cost_forward(cur_recover_vertex);

			if(sub_path != null) 
			{
				++_generated_path_num;

				double cost = 0; 
				List<Route> pre_path_list = new Vector<Route>();
				reverse_tree.correct_cost_backward(cur_recover_vertex);
				
				for(int j=0; j<path_length; ++j)
				{
					Route cur_vertex = cur_path_vertex_list.get(j);
					if(cur_vertex.get_id() == cur_recover_vertex.get_id())
					{
						j=path_length;
					}else
					{
						cost += _graph.get_edge_weight_of_graph(cur_path_vertex_list.get(j), 
								cur_path_vertex_list.get(j+1));
						pre_path_list.add(cur_vertex);
					}
					
					if(sub_path.get_vertices().size()+pre_path_list.size() > _stops+1) {
						return null;
					}
				}
				pre_path_list.addAll(sub_path.get_vertices());

				sub_path.set_weight(cost+sub_path.get_weight());
				sub_path.get_vertices().clear();
				sub_path.get_vertices().addAll(pre_path_list);

				if(!_path_derivation_vertex_index.containsKey(sub_path))
				{
					_path_candidates.add(sub_path);
					_path_derivation_vertex_index.put(sub_path, cur_recover_vertex);
				}
			}

			Route succ_vertex = cur_path_vertex_list.get(i+1); 
			_graph.recover_removed_edge(new Match<Integer, Integer>(
					cur_recover_vertex.get_id(), succ_vertex.get_id()));
			
			double cost_1 = _graph.get_edge_weight(cur_recover_vertex, succ_vertex) 
				+ reverse_tree.get_source_distance_index().get(succ_vertex);
			
			if(reverse_tree.get_source_distance_index().get(cur_recover_vertex) >  cost_1)
			{
				reverse_tree.get_source_distance_index().put(cur_recover_vertex, cost_1);
				reverse_tree.get_previous_index().put(cur_recover_vertex, succ_vertex);
				reverse_tree.correct_cost_backward(cur_recover_vertex);
			}
		}

		_graph.recover_removed_edges();
		_graph.recover_removed_vertices();

		return cur_path;
	}

	public List<DirectedEdge> get_yens_shortest_paths(Route source_vertex, 
			Route target_vertex, int top_k, int stops)
	{
		_source_vertex = source_vertex;
		_target_vertex = target_vertex;
		_stops = stops;
		
		_init();
		int count = 0;
		while(has_next() && count < top_k)
		{
			next();
			++count;
		}
		
		return _result_list;
	}

	public List<DirectedEdge> get_all_routes()
	{
		return _result_list;
	}

	public int get_all_options_size()
	{
		return _path_derivation_vertex_index.size();
	}

	public int get_generated_route_size()
	{
		return _generated_path_num;
	}
}
