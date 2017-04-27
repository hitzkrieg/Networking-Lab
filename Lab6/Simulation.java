import java.util.*;

class Packet
{
	int sender_id;
	int sender_port;

	int receiver_id;
	int receiver_port;
	int packet_number;
	double time_gen;
	double time_received;
	int status;
	int packet_size;
	int rwnd_size; /* for flow control */
	/*
		Status 1: At sender
		Status 2: At router	
		Status 3: At receiver
	*/

	Packet(int sender_id, int sender_port, int receiver_id, int receiver_port ,int packet_number, double time_gen, int packet_size, int rwnd_size)
	{
		status = 1;
		this.sender_id =  sender_id;
		this.sender_port = sender_port;
		this.receiver_id = receiver_id;
		this.receiver_port = receiver_port;
		this.time_gen = time_gen;
		this.packet_number = packet_number;
		this.packet_size = packet_size;
		this.rwnd_size = rwnd_size;
		this.status = 1;

	}	


}



class Transfer /* Used to represent a connection. The sender and the receiver have their own versions of the Transfer object for the same connection */
	{
		int source_id;
		int source_port;
		int destination_id;
		int destination_port;
		int filesize;
		int last_data_byte_sent;
		int last_data_byte_generated;
		int last_data_byte_received;
		int last_acknowledgment_sent;
		int last_acknowledgment_generated;
		int last_acknowledgment_received;
		int counter;	
		int source_window_size;	
		double dev_RTT;
		double estimated_RTT;
		double timeout;

		Transfer(int source_id, int source_port, int destination_id, int destination_port, int filesize)
		{
			this.source_window_size = 5;
			this.source_id = source_id;
			this.source_port = source_port;
			this.destination_id = destination_id;
			this.destination_port = destination_port;
			this.filesize = filesize;
			this.last_data_byte_sent = 0;
			this.last_data_byte_received = 0;
			this.last_acknowledgment_sent = 0;
			this.last_acknowledgment_received = 0;
			this.last_data_byte_generated = 0;
			this.last_acknowledgment_generated =0;
			this.timeout = 0;
			this.dev_RTT =0;
			this. estimated_RTT =0;
			this.counter = 0;
		}

		void update_timeout1(double sample_RTT)
		{
			double alpha = (double)0.125;
			double beta = (double)0.25;
			estimated_RTT = (1- alpha)*estimated_RTT + alpha*sample_RTT;
			dev_RTT = (1-beta)* dev_RTT + beta* Math.abs(sample_RTT - estimated_RTT);
			timeout = estimated_RTT + 4*dev_RTT;
			
		}
	}



class Event implements Comparable<Event>
{
	String event_name;
	Packet packet;
	double timestamp;
	int source_id; /* Required only when no packet present, for "Generate action" */
	int source_port; /* Required only when no packet present, for "Generate action" */
	Event(String event_name, double timestamp, Packet packet, int source_id, int port_no)
	{
		this.event_name = event_name;
		this.timestamp = timestamp;
		this.packet = packet;
		this.source_id = source_id;
		this.source_port = source_port;
	}

	@Override
    public int compareTo(Event event) {
        
		if((event.timestamp - this.timestamp)>0)
			return(-1);
		else if((event.timestamp - this.timestamp)==0)
			return(0);
		else
			return(1);

    }

}




class Source
{

	int id;
	double link_bandwidth;
	ArrayList<Packet> source_queue_in;
	ArrayList<Packet> source_queue_out;
	ArrayList<Integer> ports_used; 
	static final int total_ports= 100;
	int[] packets_generated;
	int[] packets_received;
	int queue_in_size_max;
	int queue_out_size_max;
	int packet_size;
	
	ArrayList<Integer> window;
	int timeout;
	Transfer[] transfers;
	

	Source(int id, double link_bandwidth, 
		int queue_in_size_max, int queue_out_size_max, int packet_size)
	{
		
		this.id = id;
		this.packets_generated = new int[total_ports];
		this.packets_received  = new int[total_ports];
		this.transfers = new Transfer[total_ports];

		this.link_bandwidth = link_bandwidth;
		source_queue_in = new ArrayList<Packet>(); 
		source_queue_out = new ArrayList<Packet>(); 
		ports_used = new ArrayList<Integer>();
		

		for(int i = 0; i<total_ports; i++)
		{
			packets_generated[i] = 0;
			packets_received[i] = 0;
		}

		this.queue_in_size_max = queue_in_size_max;
		this.queue_out_size_max = queue_out_size_max;
		this.packet_size = packet_size;
		this.window = new ArrayList<Integer>();
		
		this.transfers = new Transfer[total_ports];

	}

	int find_available_port()
	{
		for (int i=0; i<total_ports; i++)
		{
			if(! ports_used.contains(new Integer(i)))
			{
				ports_used.add(new Integer(i));
				return(i);
			}
		}
		return(-1);
	}

	boolean deallocate_port(int port_no)
	{
		if(ports_used.contains(new Integer(port_no)))
			return true;
		return false;	
	}

	void initiate_transfer(int source_port, int destination_id, int destination_port, int filesize)
	{
		transfers[source_port] = new Transfer(this.id, source_port, destination_id, destination_port, filesize);
	}

	boolean generate_data_packet(int source_port, int destination_id, int destination_port, int packet_number,  double time_gen)
	{
		packets_generated[source_port]++;
		
		Packet packet= new Packet(this.id, source_port, destination_id, destination_port, packet_number, time_gen, this.packet_size,  0 );
		if(source_queue_out.size() != queue_out_size_max)
		{
			source_queue_out.add(packet);
			return(true);
		}
		else
		{
			return(false);
		}
	}	


	Packet transfer_packet()
		{
			Packet packet = source_queue_out.get(0);
	        source_queue_out.remove(0);
	        packet.status = 2;
	        return(packet);  
		}

	void receive_ack(int source_port)
	{	
		packets_received[source_port]++;

	}	

}





class Destination
{
	int id;
	double link_bandwidth;
	ArrayList<Packet> dest_queue_in;
	ArrayList<Packet> dest_queue_out;
	ArrayList<Integer> ports_used; 
	static final int total_ports = 100;
	int[] packets_generated;
	int[] packets_received;
	int queue_in_size_max;
	int queue_out_size_max;
	int packet_size; /* Maximum Segment Size is kept as the fixed packet size for this experiment */
	ArrayList<Integer> window;
	Transfer[] transfers;
	
	

	Destination(int id, double link_bandwidth, 
		int queue_in_size_max,int queue_out_size_max, int packet_size)
	{
		
		this.id = id;
		this.packets_generated = new int[total_ports];
		this.packets_received = new int[total_ports];
		this.transfers = new Transfer[total_ports];

		this.link_bandwidth = link_bandwidth;
		dest_queue_in = new ArrayList<Packet>(); 
		dest_queue_out = new ArrayList<Packet>(); 
		ports_used = new ArrayList<Integer>();
		
		for(int i = 0; i<total_ports; i++)
		{
			packets_generated[i] = 0;
			packets_received[i] = 0;
		}

		this.queue_in_size_max = queue_in_size_max;
		this.queue_out_size_max = queue_out_size_max;
		this.packet_size = packet_size;
		this.window = new ArrayList<Integer>();
		


	}

	int find_available_port()
	{
		for (int i=0; i<total_ports; i++)
		{
			if(! ports_used.contains(new Integer(i)))
			{
				ports_used.add(new Integer(i));
				return(i);
			}
		}
		return(-1);
	}

	boolean generate_ack_packet(int dest_port, int source_id, int source_port, int packet_number,  double time_gen)
	{
		packets_generated[dest_port]++;
		
		Packet packet= new Packet(this.id, dest_port, source_id, source_port, packet_number, time_gen, this.packet_size, 0 );
		if(dest_queue_out.size() != queue_out_size_max)
		{
			dest_queue_out.add(packet);
			return(true);
		}
		else
		{
			return(false);
		}
	}	

	//For flow control
	

	void receive_packet(int dest_port)
	{	
		packets_received[dest_port]++;


	}

	void initiate_transfer(int dest_port, int source_id, int source_port, int filesize)
		{
			transfers[dest_port] = new Transfer(source_id, source_port, this.id, dest_port, filesize);
		}

	Packet transfer_packet()
		{
			Packet packet = dest_queue_out.get(0);
	        dest_queue_out.remove(0);
	        packet.status = 2;
	        return(packet);  
		}
}		



class Switch
{
	int no_of_devices; /* Max devices that the switch may be connected to */
	ArrayList<Packet> switch_queue_in;  
	ArrayList[] switch_queue_out; 
	int[] device_list; /* Maps devices connected to switch to their ip addresses ie. id */
	int queue_in_size_max;
	int queue_out_size_max;
	int packets_reached;


	Switch(int queue_in_size_max, int queue_out_size_max, int no_of_devices)
	{
		switch_queue_out = new ArrayList[no_of_devices];
		switch_queue_in = new ArrayList<Packet>();
		this.queue_in_size_max = queue_in_size_max;
		this.queue_out_size_max = queue_out_size_max;
		packets_reached = 0;
		device_list = new int[no_of_devices];

	}

	//to be used if required

	boolean add_devices(int[] device_ids) 
	{	
		try{
			assert(device_ids.length == no_of_devices);
			for(int i = 0; i<device_ids.length; i++)
			{
				device_list[i] = device_ids[i];
			}
			return(true);
		}
		catch(Exception e)
		{

		}
		return(false);

	} 

	boolean enqueue_switch(Packet packet)
	{
		if(switch_queue_in.size() != queue_in_size_max)
		{
			this.switch_queue_in.add(packet);
			packets_reached++;
			if(transfer_from_inputq_to_outputq())
				return(true);
		}
		return(false);

	}

	int get_device_index(int id)
	{
		int index = 0;
		for(index = 0; index < no_of_devices; index++)
		{
			if(device_list[index] == id)
				return(index);
		}
		return(-1);
	}

	boolean transfer_from_inputq_to_outputq()
	{
		Packet p = switch_queue_in.get(0);
		int id = p.receiver_id;
		int device_index = 0;
		for(int i=0; i<no_of_devices; i++)
		{
			if(device_list[i] == id)
			{
				device_index = i; 
				break;
			}
		}
		if(switch_queue_out[device_index].size() == queue_out_size_max)
		{
			switch_queue_in.remove(0);
			return false;
		}


        switch_queue_in.remove(0);
        switch_queue_out[device_index].add(p);
		return true;

	}

	Packet dequeue_switch(int id)
	{	
		int device_index = 0;
		for(int i=0; i<no_of_devices; i++)
		{
			if(device_list[i] == id)
			{
				device_index = i; 
				break;
			}
		}

		Packet packet = (Packet)switch_queue_out[device_index].get(0);
        switch_queue_out[device_index].remove(0);
        packet.status = 3;
        return(packet);  
	}
}



//Issues
/*packets_lost should be incremented at a few more places which have been ignored for now. Change that when required */



//Controls the different elements of the network and is responsible for their simulation via the events_list Priority Queue
class Network{

	ArrayList<Source> sources;
	ArrayList<Destination> destinations;
	Switch switch1;
	int packet_size;
	int packets_lost;
	double total_delay;
	int no_devices;
	int no_sources;
	int no_destinations;
	
	

	Network(int packet_size)
	{	
		this.packet_size = packet_size;
		
		this.sources = new ArrayList<Source>();
		this.destinations = new ArrayList<Destination>();

		this.packets_lost = 0;
		this.total_delay= 0;
		this.no_devices = 0;
		this.no_sources = 0;
		this.no_destinations = 0;
			
		
	} 

	 /* returns the id of the newly created source */

	int add_source(double link_bandwidth, int queue_in_size_max, int queue_out_size_max)
	{
		no_devices++;
		no_sources++;
		this.sources.add(new Source(no_devices, link_bandwidth, queue_in_size_max,  queue_out_size_max, this.packet_size));
		return no_devices; 
	}

	 /* returns the id of the newly created destination */

	int add_destination(double link_bandwidth, 
		int queue_in_size_max, int queue_out_size_max)
	{
		no_devices++;
		no_destinations++;
		this.destinations.add(new Destination(no_devices, link_bandwidth, 
		 queue_in_size_max,  queue_out_size_max, this.packet_size));
		return(no_devices);
	}

	//Establish connection and initiate transfer between the source and the destination

	Boolean handshake(int source_id, int destination_id, int file_size)
	{
		int source_index = 0;
		int destination_index = 0;
		for(int i = 0; i<no_sources; i++)
		{
			if(sources.get(i).id == source_id)
			{
				source_index = i; break;
			}

		}

		for(int i = 0; i<no_destinations; i++)
		{
			if(destinations.get(i).id == destination_id)
			{
				destination_index = i; break;
			}

		}

		Source s = this.sources.get(source_index);
		Destination d = this.destinations.get(destination_index);
		int source_port = s.find_available_port();
		int destination_port = d.find_available_port();
		if(source_port == -1 || destination_port == -1)
			return(false);
		s.initiate_transfer(source_port, destination_id, destination_port, file_size);
		d.initiate_transfer(destination_port, source_id, source_port, file_size);
		System.out.println("Initiated transfer between Source id: "+ source_id + " port "+ source_port + " and destination id" + destination_id + " port "+ destination_port);
		return(true);
	}

	int get_source_index(int source_id)
	{
		int source_index= -1;
		for(int i = 0; i< sources.size(); i++)
				{
					if(sources.get(i).id == source_id)
					{
						source_index = i;
						break;
					}
				}
		return(source_index);		
	}

	int get_destination_index(int destination_id)
	{
		int destination_index= -1;
		for(int i = 0; i< destinations.size(); i++)
				{
					if(destinations.get(i).id == destination_id)
					{
						destination_index = i;
						break;
					}
				}
		return(destination_index);		
	}


	void packet_switching()
	{	
		double time = 0;
		int flag = 0;

		PriorityQueue<Event> events_list = new PriorityQueue<Event>();
		for(int i=0; i< sources.size(); i++)
		{	
			Source source = sources.get(i);
			for(int j=0; j<source.total_ports; j++)
			{
				if(source.transfers[j]!= null)
					events_list.add(new Event("Generate data packet", time ,null, source.id, source.transfers[j].source_port));
			}
		}
		
		
		while(flag == 0)
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
			flag = packet_switching_action(event, events_list, time);
		}
	}

	int packet_switching_action(Event event, PriorityQueue<Event> events_list, double time)
	{
		
		switch(event.event_name)
		{
			case "Generate data packet":
				int source_index = get_source_index(event.source_id);
				Source source = sources.get(source_index);
				Transfer t = source.transfers[event.source_port];
				
				if(t.last_data_byte_generated < t.filesize)
				{			
					int packets_left_to_be_generated_total = (t.filesize - t.last_data_byte_generated)/packet_size;
					int packets_to_be_generated = Math.min(packets_left_to_be_generated_total, (source.queue_in_size_max - source.source_queue_in.size()));

					for(int i = 0; i<packets_to_be_generated; i++)
					{
						t.last_data_byte_generated += packet_size;
						if(source.generate_data_packet(t.source_port, t.destination_id, t.destination_port, t.last_data_byte_generated, time))
						{
							if(source.source_queue_out.size() == 1)
							{
								events_list.add(new Event("Data packet switch reached", 
									time + ((double)packet_size)/source.link_bandwidth, 
									source.source_queue_out.get(0), event.source_id, event.source_port ));

									events_list.add(new Event("Timeout", time + t.timeout, source.source_queue_out.get(0), event.source_id, event.source_port ));

							}
						} 

						else
						{
							System.out.println("Some problem here.");
						}
					}
				}
					

				

				break;	

			

			case "Data packet switch reached": 

				int source_index1 = get_source_index(event.source_id);
				Source source1 = sources.get(source_index1);
				Transfer t1 = source1.transfers[event.source_port];
				Packet packet = source1.transfer_packet();
				assert packet == event.packet;
				double probab = Math.random();
				
				int destination_index1 = get_destination_index(packet.receiver_id);
				Destination destination1 = destinations.get(destination_index1);

				t1.last_data_byte_sent += packet_size;
				Boolean isNotFull = switch1.enqueue_switch(event.packet);
				if(isNotFull)
				{

					if(switch1.switch_queue_out[switch1.get_device_index(packet.receiver_id)].size() == 1)
					{
						events_list.add(  new Event("Destination reached", 
							time + ((double)packet.packet_size) / destination1.link_bandwidth, packet, event.source_id, event.source_port) );
					}
				}	
				else
				{
					packets_lost++;
					/* total_delay += time - event.packet.time_gen;	*/


				}	

				
				if(source1.source_queue_out.size()!= 0)
				{
					Transfer t2 = source1.transfers[source1.source_queue_out.get(0).sender_port];
					if( (t2.last_data_byte_sent - t2.last_acknowledgment_received) /packet_size < t2.source_window_size)   
					{
						events_list.add(new Event("Data packet switch reached", 
							time + ((double)packet_size)/source1.link_bandwidth, 
							source1.source_queue_out.get(0), event.source_id, t2.source_port));
					}	
				}

				
				break;


			

			case "Destination reached":
				//Remove packet from switch to destination
				Packet packet1 = switch1.dequeue_switch(event.packet.receiver_id);
				assert (packet1 == event.packet);

				Destination d1 = destinations.get(get_destination_index(packet1.receiver_id));
				Transfer t2 = d1.transfers[packet1.receiver_port];

				
					
				int acknowledgment_number = 0;

				//if packet received is the one that was awaited
				if(t2.last_acknowledgment_sent + packet_size == packet1.packet_number)
				{
					acknowledgment_number = packet1.packet_number;
					t2.last_data_byte_received  = packet1.packet_number;
					t2.last_acknowledgment_sent = packet1.packet_number;
				}
				else
					acknowledgment_number = t2.last_acknowledgment_sent;

				int receiver_window = (d1.queue_in_size_max - d1.dest_queue_in.size());  
				Packet packet2 = new Packet(d1.id, t2.destination_port, t2.source_id, t2.source_port, 
					acknowledgment_number, time, packet_size, receiver_window);
				if(d1.dest_queue_out.size()!= d1.queue_out_size_max)
				{
					d1.dest_queue_out.add(packet2);
					if(d1.dest_queue_out.size() == 1)
					{
						events_list.add(new Event("acknowledgment packet switch reached", time + ((double)packet_size)/d1.link_bandwidth , packet2, d1.id, t2.destination_port));
					}
				}

				
				


				int switch_device_index = switch1.get_device_index(d1.id);
				if (switch1.switch_queue_out[switch_device_index].size()!=0)
				{
					Packet new_packet = (Packet)switch1.switch_queue_out[switch_device_index].get(0);
					events_list.add(new Event("Destination reached", 
						time + ((double)new_packet.packet_size)/d1.link_bandwidth , new_packet, 
							new_packet.sender_id, new_packet.sender_port ));
				}
				break;

				case "acknowledgment packet switch reached": 

				int source_index2 = get_source_index(event.packet.receiver_id);
				int destination_index2 = get_destination_index(event.packet.sender_id);
				Destination destination2 = destinations.get(destination_index2);
				Source source2 = sources.get(source_index2);
				Transfer t3 = destination2.transfers[event.source_port];
				Packet packet3 = destination2.transfer_packet();
				assert packet3 == event.packet;
				

				Boolean isNotFull1 = switch1.enqueue_switch(event.packet);
				if(isNotFull1)
				{

					if(switch1.switch_queue_out[switch1.get_device_index(packet3.sender_id)].size() == 1)
					{
						events_list.add(  new Event("Source reached", 
							time + ((double)packet_size) / source2.link_bandwidth, packet3, event.source_id, event.source_port) );
					}
				}	
				else
				{
					packets_lost++;
					/* total_delay += time - event.packet.time_gen;	*/
				}	

				
				if(destination2.dest_queue_out.size()!= 0)
				{
					Transfer t4 = destination2.transfers[destination2.dest_queue_out.get(0).sender_port];
					
					events_list.add(new Event("acknowledgment packet switch reached", 
						time + ((double)packet_size)/destination2.link_bandwidth, 
						destination2.dest_queue_out.get(0), event.source_id, t4.destination_port));	
				}

				
				break;


				case "Source reached":
					//Remove packet from switch to destination
					Packet p = switch1.dequeue_switch(event.packet.receiver_id);
					assert (p == event.packet);
					p.time_received = time;
					Destination des = destinations.get(get_destination_index(p.sender_id));
					
					Source s = sources.get(get_source_index(p.receiver_id));
					Transfer trans = s.transfers[p.receiver_port];
						
					trans.update_timeout1(2*(p.time_received - p.time_gen));

					if(trans.last_acknowledgment_received + packet_size == p.packet_number)
					{
						trans.last_acknowledgment_received  = p.packet_number;
						trans.counter++;
						if(trans.counter == trans.source_window_size)
						{
							trans.counter =0;
							trans.source_window_size++;
						}
					}
					
					trans.source_window_size = Math.min(p.rwnd_size, trans.source_window_size);
					
					int switch_device_index2 = switch1.get_device_index(s.id);
					if (switch1.switch_queue_out[switch_device_index2].size()!=0)
					{
						Packet new_packet = (Packet)switch1.switch_queue_out[switch_device_index2].get(0);
						events_list.add(new Event("Source reached", 
							time + ((double)packet_size)/s.link_bandwidth , p, 
								p.sender_id, p.sender_port ));
					}

					if(trans.last_acknowledgment_received == trans.filesize)
					{
						return(1);
					}
					break;


				case "Timeout":

					int source_index_p = get_source_index(event.source_id);
					Source source_p = sources.get(source_index_p);
					Transfer t_p = source_p.transfers[event.source_port];
				
					if(event.packet.packet_number == t_p.last_data_byte_sent)
					{
						/*Multiplicative decrease */
						t_p.source_window_size = t_p.source_window_size/2;

						if(source_p.generate_data_packet(t_p.source_port, t_p.destination_id, t_p.destination_port, t_p.last_data_byte_generated, time))
							{
								if(source_p.source_queue_out.size() == 1)
								{
									events_list.add(new Event("Data packet switch reached", 
										time + ((double)packet_size)/source_p.link_bandwidth, 
										source_p.source_queue_out.get(0), event.source_id, event.source_port ));

										events_list.add(new Event("Timeout", time + t_p.timeout, source_p.source_queue_out.get(0), event.source_id, event.source_port ));

								}
							} 
					}	


					break;


								
		}

	return(0);	
}

// 	double calculate_average_delay()
// 	{
// 		return(total_delay/(double)sink.packets_reached_list.size());
// 	}

// 	double calculate_link_utilization()
// 	{
// 		double time_used =0;
// 		for(int i=0; i<sink.packets_reached_list.size(); i++)
// 		{
// 			time_used += sink.packets_reached_list.get(i).packet_size;
// 		}

// 		time_used = time_used / switch1.link_bandwidth;

// 		double res = time_used / time_simulation* ((double)100.0);
// 		return(res);
// 	}

// 	double calculate_pc_packets_lost()
// 	{
// 		int packets_generated = 0;
// 		for(int i=0; i< sources.size(); i++)
// 		{
// 			packets_generated += sources.get(i).packets_generated;
// 		}
// 		return((double)packets_lost/ (double)packets_generated * (double)100.0);
// 	}

	
	
}








class Simulation
{


	public static void main(String[] args)
	{

		int packet_size = 10;

		/* for source and destination */
		double link_bandwidth = 150;  
		int queue_in_size_max = 20;
		int queue_out_size_max = 20;
		int filesize = 200;

		/* for switch */
		int switch_queue_in_size_max = 20;
		int switch_queue_out_size_max = 20;
		int switch_no_of_devices = 2;



		Network n1 = new Network(packet_size);
		n1.switch1 = new Switch(switch_queue_in_size_max, switch_queue_out_size_max, switch_no_of_devices);


		int s_id = n1.add_source(link_bandwidth, queue_in_size_max, queue_out_size_max);
		int d_id = n1.add_destination(link_bandwidth, queue_in_size_max, queue_out_size_max);
		

		/* Set up ip addresses (id) for routing packets by the switch */
		n1.switch1.device_list[0] = s_id;
		n1.switch1.device_list[1] = d_id;

		/* Establish connection. Initialize the objects of the Transfer class */
		n1.handshake(s_id, d_id, filesize);
		n1.packet_switching();		

	}	
}