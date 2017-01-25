import java.util.*;


class Network{

	ArrayList<Source> sources;
	Switch switch1;
	String switching_technique;
	int packets_lost;
	float total_delay;
	Sink sink;
	int time_simulation;
	float k;
	float alpha;
	float lambda;
	String packet_length_dist;

	Network(int no_sources, float link_bandwidth, String packet_gen_type, 
		float packet_gen_val, int queue_size, String switching_technique, int packet_size, 
		int time_simulation, float k, float alpha, float lambda, String packet_length_dist )
	{	
		this.time_simulation = time_simulation;
		
		sink = new Sink();
		sources = new ArrayList<Source>();
		for(int i=0; i < no_sources; i++)
		{
			sources.add(new Source(i, packet_gen_type, packet_gen_val, link_bandwidth, 
				queue_size, packet_size, k, alpha, lambda, packet_length_dist));
		}

		switch1 = new Switch(link_bandwidth, queue_size);
		this.switching_technique = switching_technique;
		packets_lost = 0;
		total_delay= 0;
		
		this.k= k;
		this.alpha = alpha;
		this.lambda = lambda;


	} 

	void packet_switching()
	{	
		float time = 0;

		PriorityQueue<Event> events_list = new PriorityQueue<Event>();
		for(int i=0; i< sources.size(); i++)
		{	
			Source source = sources.get(i);
			events_list.add(new Event("Generate", 
				time ,null, i ));
		}
		
		
		while(time < time_simulation)
		{				

			

			Event event = events_list.poll();
			if(event == null)
			{
				System.out.println(events_list.size());
			}
			time = event.timestamp;

			/* For visualizing
			System.out.println(time);
			System.out.println(event.event_name);
			*/
			packet_switching_action(event, events_list, time);
		}
	}

	void packet_switching_action(Event event, PriorityQueue<Event> events_list, float time)
	{
		
		switch(event.event_name)
		{
			case "Generate":
				Source source = sources.get(event.source_id);

				float next_time = source.time_next_packet(time);
				
				events_list.add(new Event("Generate", next_time, null, event.source_id ));
				if(source.generate_packet(time))
				{
					if(source.source_queue.size() == 1)
					{
						events_list.add(new Event("Switch reached", 
							time + ((float)source.source_queue.get(0).packet_size)/source.link_bandwidth, 
							source.source_queue.get(0), event.source_id ));

					}
				} 
				else
				{
					packets_lost++;
					/* total_delay += next_time - time;	*/
					
				}

				

				break;	

			

			case "Switch reached": 

				Source source1 = sources.get(event.packet.source_id);
				Packet packet = source1.transfer_packet();
				assert packet == event.packet;

				
				if(source1.source_queue.size()!= 0)
				{
					events_list.add(new Event("Switch reached", 
						time + ((float)source1.source_queue.get(0).packet_size)/source1.link_bandwidth, 
						source1.source_queue.get(0), event.source_id ));
				}

				Boolean isNotFull = switch1.enqueue_switch(event.packet);
				if(isNotFull)
				{

					if(switch1.switch_queue.size() == 1)
					{
						events_list.add(  new Event("Sink reached", 
							time + ((float)packet.packet_size) / switch1.link_bandwidth, packet, event.source_id) );
					}
				}	
				else
				{
					packets_lost++;
					/* total_delay += time - event.packet.time_gen;	*/


				}	
				break;


			

			case "Sink reached":
				Packet packet1 = switch1.dequeue_switch();
				assert (packet1 == event.packet);

				sink.packets_reached_list.add(event.packet);
				event.packet.time_recieved = time;
				total_delay += (event.packet.time_recieved - event.packet.time_gen); 

				if (switch1.switch_queue.size()!=0)
				{
					Packet new_packet = switch1.switch_queue.get(0);
					events_list.add(new Event("Sink reached", 
						time + ((float)new_packet.packet_size)/switch1.link_bandwidth , new_packet, new_packet.source_id ));
				}
				break;

		}


	}

	float calculate_average_delay()
	{
		return(total_delay/(float)sink.packets_reached_list.size());
	}

	float calculate_link_utilization()
	{
		float time_used =0;
		for(int i=0; i<sink.packets_reached_list.size(); i++)
		{
			time_used += sink.packets_reached_list.get(i).packet_size;
		}

		time_used = time_used / switch1.link_bandwidth;

		float res = time_used / time_simulation* ((float)100.0);
		return(res);
	}

	float calculate_pc_packets_lost()
	{
		int packets_generated = 0;
		for(int i=0; i< sources.size(); i++)
		{
			packets_generated += sources.get(i).packets_generated;
		}
		return((float)packets_lost/ (float)packets_generated * (float)100.0);
	}

	
	
}


