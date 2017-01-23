import java.util.*;


class Network{

	ArrayList<Source> sources;
	Switch switch1;
	String switching_technique;
	int packets_lost;
	float total_delay;
	float allocation_time;
	int source_allocated;
	Sink sink;

	Network(int no_sources, float link_bandwidth, String packet_gen_type, 
		float packet_gen_val, int queue_size, String switching_technique, int packet_size, float allocation_time)
	{
		sink = new Sink();
		sources = new ArrayList<Source>();
		for(int i=0; i < no_sources; i++)
		{
			sources.add(new Source(i, packet_gen_type, packet_gen_val, link_bandwidth, queue_size, packet_size));
		}

		switch1 = new Switch(link_bandwidth, queue_size);
		this.allocation_time = allocation_time;
		this.switching_technique = switching_technique;
		source_allocated = 0;
		packets_lost = 0;
		total_delay= 0;


	} 

	void circuit_switching(int time_simulation)
	{
		float time = 0;

		PriorityQueue<Event> events_list = new PriorityQueue<Event>();

		events_list.add(new Event("Allocate time", time, null, 0));

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

			circuit_switching_action(event, events_list, time);
		}
	}


	int allocate_source(float time)
	{	
		int source_id = (int)((time % ((float)sources.size() * allocation_time) )/allocation_time);
		return(source_id);

	}

	void circuit_switching_action(Event event, PriorityQueue<Event> events_list, float time)
	{
		switch(event.event_name)
		{
			case "Allocate time":
				int source_to_allocate = allocate_source(time);
				Source source3 = sources.get(source_to_allocate);
				if(source3.source_queue.size()!=0)
				{
					if((float)source3.source_queue.get(0).packet_size/ source3.link_bandwidth < allocation_time)
					{
						events_list.add(new Event("Switch reached", 
							time + ((float)source3.source_queue.get(0).packet_size)/source3.link_bandwidth, 
							source3.source_queue.get(0),source3.id ));
					} 

				}

				events_list.add(new Event("Allocate time", time + allocation_time, null, 0));

				break;

			case "Generate":
				Source source = sources.get(event.source_id);

				float next_time = source.time_next_packet(time);
				
				events_list.add(new Event("Generate", next_time, null, event.source_id ));
				if(source.generate_packet(time))
				{
					if((source.source_queue.size() == 1)&&(allocate_source(time)== source.id)&&
						(allocate_source(time + ((float)source.source_queue.get(0).packet_size)/source.link_bandwidth)==source.id))
					{
						events_list.add(new Event("Switch reached", 
							time + ((float)source.source_queue.get(0).packet_size)/source.link_bandwidth, 
							source.source_queue.get(0), event.source_id ));

					}
				} 
				else
				{
					packets_lost++;
					/* total_delay += next_time - time;*/	
					
				}

				

				break;	

			

			case "Switch reached": 

				Source source1 = sources.get(event.packet.source_id);
				Packet packet = source1.transfer_packet();
				assert packet == event.packet;

				
				if((source1.source_queue.size()!= 0)&& (source1.id == allocate_source(time)) 
					&&(source1.id == allocate_source(time + ((float)source1.source_queue.get(0).packet_size)/ source1.link_bandwidth)  )) 
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
					/* total_delay += time - event.packet.time_gen; */		


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


