import java.util.*;

class Source
{
	int id;
	String packet_gen_type;
	String packet_length_dist;
	float packet_gen_val;
	float link_bandwidth;
	ArrayList<Packet> source_queue;
	int packets_generated;
	int queue_size_max;
	int packet_size;
	float k;
	float alpha;
	float lambda;


	Source(int id, String packet_gen_type, float packet_gen_val, 	float link_bandwidth, 
		int queue_size_max, int packet_size, float k, float alpha, float lambda, String packet_length_dist )
	{
		
		this.id = id;
		this.packet_gen_type = packet_gen_type;
		this.packet_gen_val = packet_gen_val;
		this.link_bandwidth = link_bandwidth;
		source_queue = new ArrayList<Packet>(); 
		packets_generated = 0;
		this.queue_size_max = queue_size_max;
		this.packet_size = packet_size;
		this.k= k;
		this.alpha = alpha;
		this.lambda = lambda;
		this.packet_length_dist = packet_length_dist;


	}

	boolean generate_packet(float time_gen)
	{
		packets_generated++;
		if(packet_length_dist.equals("Pareto"))
		{

			packet_size = (int)Math.ceil(pareto_random(this.k, this.alpha));
		}
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
		else if((packet_gen_type).equals("Uniform"))
			return(time_prev_packet + random_no.nextFloat()* packet_gen_val );
		else
			return(time_prev_packet + poisson_random(this.alpha) );
	}

	Packet transfer_packet()
	{
		Packet packet = source_queue.get(0);
        source_queue.remove(0);
        packet.status = 2;
        return(packet);  
	}

	float pareto_random(float k, float alpha)
	{
		
		Random random_no = new Random();
		float u = random_no.nextFloat();
		float res = k * (float)Math.pow(1.0- u, -1.0 /alpha );
		System.out.println(res);
		return(res);
	}
	
	float poisson_random(float lambda)
	{
		Random random_no = new Random();
		float u = random_no.nextFloat();
		float res = (float)-1.0 * (float)Math.log(u)/(float)lambda;
		return(res);
	}	  		

	

}
