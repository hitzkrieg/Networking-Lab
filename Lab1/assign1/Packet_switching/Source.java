import java.util.*;

class Source
{
	int id;
	String packet_gen_type;
	float packet_gen_val;
	float link_bandwidth;
	ArrayList<Packet> source_queue;
	int packets_generated;
	int queue_size_max;
	int packet_size;

	Source(int id, String packet_gen_type, float packet_gen_val, 	float link_bandwidth, 
		int queue_size_max, int packet_size)
	{
		this.id = id;
		this.packet_gen_type = packet_gen_type;
		this.packet_gen_val = packet_gen_val;
		this.link_bandwidth = link_bandwidth;
		source_queue = new ArrayList<Packet>(); 
		packets_generated = 0;
		this.queue_size_max = queue_size_max;
		this.packet_size = packet_size;

	}

	boolean generate_packet(float time_gen)
	{
		packets_generated++;
		Packet packet= new Packet(this.id, this.packets_generated, time_gen, packet_size);
		if(source_queue.size() != queue_size_max)
		{
			source_queue.add(packet);
			return(true);
		}
		else
		{
			return(false);
		}
	}	

	float time_next_packet(float time_prev_packet)
	{
		Random random_no = new Random();

		if((packet_gen_type).equals("Constant"))
			return(time_prev_packet + packet_gen_val);
		else 
			return(time_prev_packet + random_no.nextFloat()* packet_gen_val );
	}

	Packet transfer_packet()
	{
		Packet packet = source_queue.get(0);
        source_queue.remove(0);
        packet.status = 2;
        return(packet);  
	}


}
