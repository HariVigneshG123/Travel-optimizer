package travel_recommender.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import travel_recommender.model.abstracts.MapGraph;
import travel_recommender.model.abstracts.Route;

public class Graph implements MapGraph
{
	public final static double DISCONNECTED = Double.MAX_VALUE;
	
	// index of fan-outs of one vertex
	protected Map<Integer, Set<Route>> _fanout_vertices_index =
		new HashMap<Integer, Set<Route>>();
	
	// index for fan-ins of one vertex
	protected Map<Integer, Set<Route>> _fanin_vertices_index =
		new HashMap<Integer, Set<Route>>();
	
	// index for edge weights in the graph
	protected Map<Match<Integer, Integer>, Double> _vertex_pair_weight_index = 
		new HashMap<Match<Integer,Integer>, Double>();
	
	//index for edge params in the graph
	protected Map<Match<Integer, Integer>, GraphParams> _vertex_pair_graph_params_index= 
			new HashMap<Match<Integer,Integer>, GraphParams>();
	
	// index for vertices in the graph
	protected Map<Integer, Route> _id_vertex_index = 
		new HashMap<Integer, Route>();
	
	// list of vertices in the graph 
	protected List<Route> _vertex_list = new Vector<Route>();
	
	// the number of vertices in the graph
	protected int _vertex_num = 0;
	
	// the number of arcs in the graph
	protected int _edge_num = 0;
	
	static private int counter = 0;
	
	static public Map<String, Integer> cityMap = new HashMap<>();
	static public Map<Integer, String> cityMapInverse = new HashMap<>();

	public Graph(final String data_file_name)
	{
		import_from_file(data_file_name);
	}
	
	public Graph(final Graph graph_)
	{
		_vertex_num = graph_._vertex_num;
		_edge_num = graph_._edge_num;
		_vertex_list.addAll(graph_._vertex_list);
		_id_vertex_index.putAll(graph_._id_vertex_index);
		_fanin_vertices_index.putAll(graph_._fanin_vertices_index);
		_fanout_vertices_index.putAll(graph_._fanout_vertices_index);
		_vertex_pair_weight_index.putAll(graph_._vertex_pair_weight_index);
		_vertex_pair_graph_params_index.putAll(graph_._vertex_pair_graph_params_index);
	}

	public Graph(){};

	public void clear()
	{
		_vertex_num = 0;
		_edge_num = 0; 
		_vertex_list.clear();
		_id_vertex_index.clear();
		_fanin_vertices_index.clear();
		_fanout_vertices_index.clear();
		_vertex_pair_weight_index.clear();
		_vertex_pair_graph_params_index.clear();
	}

	public void import_from_file(final String data_file_name)
	{
		try
		{
			// 1. read the file and put the content in the buffer
			FileReader input = new FileReader(data_file_name);
			BufferedReader bufRead = new BufferedReader(input);

			boolean is_first_line = true;
			String line; 	// String that holds current file line
			
			// 2. Read first line
			line = bufRead.readLine();
			while(line != null)
			{
				// 2.1 skip the empty line
				if(line.trim().equals("")) 
				{
					line = bufRead.readLine();
					continue;
				}
				
				// 2.2 generate nodes and edges for the graph
				if(is_first_line)
				{
					//2.2.1 obtain the number of nodes in the graph 
					
					is_first_line = false;
					_vertex_num = Integer.parseInt(line.trim());
					for(int i=0; i<_vertex_num; ++i)
					{
						Route vertex = new City();
						_vertex_list.add(vertex);
						_id_vertex_index.put(vertex.get_id(), vertex);
					}
					
				}else
				{
					//2.2.2 find a new edge and put it in the graph  
//					String[] str_list = line.trim().split("\\s");
					String[] str_list = line.trim().split(",");
					int start_vertex_id = 0;
					int end_vertex_id = 0;
					if(!cityMap.containsKey(str_list[0])) {
						start_vertex_id = counter++;
						addToMap(str_list[0], start_vertex_id);
					}
					else {
						start_vertex_id = cityMap.get(str_list[0]);
					}
					
					if(!cityMap.containsKey(str_list[1])) {
						end_vertex_id = counter++;
						addToMap(str_list[1], end_vertex_id);
					}
					else {
						end_vertex_id = cityMap.get(str_list[1]);
					}
					
//					int start_vertex_id = Integer.parseInt(str_list[0]);
//					int end_vertex_id = Integer.parseInt(str_list[1]);
					GraphParams gp = new GraphParams();
					gp.setWeightFlag("cost");
					gp.setCost(Double.parseDouble(str_list[4]));
					gp.setStartDate(new Date(Long.parseLong(str_list[5])));
					gp.setDuration(durationInMs(str_list[6]));
					gp.setWeight();
					gp.setMode(str_list[7]);
//					ArrayList<String> params = new ArrayList<>();
//					params.add(String.valueOf(str_list[2]));
					add_edge(start_vertex_id, end_vertex_id, gp);
				}
				//
				line = bufRead.readLine();
			}
			bufRead.close();

		}catch (IOException e)
		{
			// If another exception is generated, print a stack trace
			e.printStackTrace();
		}
	}

	private void addToMap(String k, int v) {
		// TODO Auto-generated method stub
		cityMap.put(k, v);
		cityMapInverse.put(v, k);
	}

	private long durationInMs(String duration) {
		long d = Long.parseLong(duration);
		return d;
	}

	protected void add_edge(int start_vertex_id, int end_vertex_id, GraphParams gp)
	{
		double weight = gp.getWeight();
		// actually, we should make sure all vertices ids must be correct. 
		if(!_id_vertex_index.containsKey(start_vertex_id)
		|| !_id_vertex_index.containsKey(end_vertex_id) 
		|| start_vertex_id == end_vertex_id)
		{
			throw new IllegalArgumentException("The edge from "+start_vertex_id
					+" to "+end_vertex_id+" does not exist in the graph.");
		}
		
		// update the adjacent-list of the graph
		Set<Route> fanout_vertex_set = new HashSet<Route>();
		if(_fanout_vertices_index.containsKey(start_vertex_id))
		{
			fanout_vertex_set = _fanout_vertices_index.get(start_vertex_id);
		}
		fanout_vertex_set.add(_id_vertex_index.get(end_vertex_id));
		_fanout_vertices_index.put(start_vertex_id, fanout_vertex_set);
		
		//
		Set<Route> fanin_vertex_set = new HashSet<Route>();
		if(_fanin_vertices_index.containsKey(end_vertex_id))
		{
			fanin_vertex_set = _fanin_vertices_index.get(end_vertex_id);
		}
		fanin_vertex_set.add(_id_vertex_index.get(start_vertex_id));
		_fanin_vertices_index.put(end_vertex_id, fanin_vertex_set);

		// store the new edge 
		_vertex_pair_weight_index.put(
				new Match<Integer, Integer>(start_vertex_id, end_vertex_id), 
				weight);
		
		_vertex_pair_graph_params_index.put(
				new Match<Integer, Integer>(start_vertex_id, end_vertex_id), 
				gp);
		
		++_edge_num;
	}

	public void export_to_file(final String file_name)
	{
		//1. prepare the text to export
		StringBuffer sb = new StringBuffer();
		sb.append(_vertex_num+"\n\n");
		for(Match<Integer, Integer> cur_edge_pair : _vertex_pair_weight_index.keySet())
		{
			int starting_pt_id = cur_edge_pair.first();
			int ending_pt_id = cur_edge_pair.second();
			double weight = _vertex_pair_weight_index.get(cur_edge_pair);
			sb.append(starting_pt_id+"	"+ending_pt_id+"	"+weight+"\n");
		}
		//2. open the file and put the data into the file. 
		Writer output = null;
		try {
			// use buffering
			// FileWriter always assumes default encoding is OK!
			output = new BufferedWriter(new FileWriter(new File(file_name)));
			output.write(sb.toString());
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}catch(IOException e)
		{
			e.printStackTrace();
		}finally {
			// flush and close both "output" and its underlying FileWriter
			try
			{
				if (output != null) output.close();
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public Set<Route> get_adjacent_vertices(Route vertex)
	{
		return _fanout_vertices_index.containsKey(vertex.get_id()) 
				? _fanout_vertices_index.get(vertex.get_id()) 
				: new HashSet<Route>();
	}

	public Set<Route> get_precedent_vertices(Route vertex)
	{
		return _fanin_vertices_index.containsKey(vertex.get_id()) 
				? _fanin_vertices_index.get(vertex.get_id()) 
				: new HashSet<Route>();
	}

	public double get_edge_weight(Route source, Route sink)
	{
		return _vertex_pair_weight_index.containsKey(
					new Match<Integer, Integer>(source.get_id(), sink.get_id()))? 
							_vertex_pair_weight_index.get(
									new Match<Integer, Integer>(source.get_id(), sink.get_id())) 
						  : DISCONNECTED;
	}
	
	public Date get_edge_StartDate(Route source, Route sink)
	{
		return _vertex_pair_graph_params_index.containsKey(
					new Match<Integer, Integer>(source.get_id(), sink.get_id()))? 
							_vertex_pair_graph_params_index.get(
									new Match<Integer, Integer>(source.get_id(), sink.get_id())).getStartDate() 
						  : null;
	}
	
	public long get_edge_Duration(Route source, Route sink)
	{
		return _vertex_pair_graph_params_index.containsKey(
					new Match<Integer, Integer>(source.get_id(), sink.get_id()))? 
							_vertex_pair_graph_params_index.get(
									new Match<Integer, Integer>(source.get_id(), sink.get_id())).getDuration() 
						  : null;
	}
	
	public double get_edge_Cost(Route source, Route sink)
	{
		return _vertex_pair_graph_params_index.containsKey(
					new Match<Integer, Integer>(source.get_id(), sink.get_id()))? 
							_vertex_pair_graph_params_index.get(
									new Match<Integer, Integer>(source.get_id(), sink.get_id())).getCost()
						  : null;
	}
	
	public String get_edge_Mode(Route source, Route sink)
	{
		return _vertex_pair_graph_params_index.containsKey(
					new Match<Integer, Integer>(source.get_id(), sink.get_id()))? 
							_vertex_pair_graph_params_index.get(
									new Match<Integer, Integer>(source.get_id(), sink.get_id())).getMode()
						  : null;
	}


	public void set_vertex_num(int num)
	{
		_vertex_num = num;
	}

	public List<Route> get_vertex_list()
	{
		return _vertex_list;
	}

	public Route get_vertex(int id)
	{
		return _id_vertex_index.get(id);
	}
}
