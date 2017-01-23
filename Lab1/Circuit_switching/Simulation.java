class Simulation
{


	public static void main(String[] args)
	{
		int no_sources = 1;
		float link_bandwidth = 150;  
		String packet_gen_type = "Constant"; 
		float packet_gen_val = (float)0.3;
		int queue_size = 10;
		String switching_technique = "Packet";
		int packet_size = 10;
		Network n1;
		float allocation_time = 1;

		/*
		for(no_sources = 1; no_sources < 20; no_sources++)
		{	
			System.out.println("The No of sources: " + no_sources);
			n1 = new Network(no_sources, link_bandwidth, packet_gen_type, 
			packet_gen_val, queue_size, switching_technique, packet_size, allocation_time);

			n1.circuit_switching(500);
			float avg_delay = n1.calculate_average_delay();
			System.out.println("The average delay is: " + avg_delay);

			float pc_packets_dropped = n1.calculate_pc_packets_lost();


			System.out.println("The % of packets dropped: "+ pc_packets_dropped );
			System.out.println("***************************");
		}
		*/

		/*
		no_sources = 10;

		for(packet_gen_val = (float)0.02; packet_gen_val < 1; packet_gen_val += (float)0.04)
		{	
			System.out.println("The inter packet time : " + packet_gen_val);
			n1 = new Network(no_sources, link_bandwidth, packet_gen_type, 
			packet_gen_val, queue_size, switching_technique, packet_size, allocation_time);

			n1.circuit_switching(500);
			float avg_delay = n1.calculate_average_delay();
			System.out.println("The average delay is: " + avg_delay);

			float pc_packets_dropped = n1.calculate_pc_packets_lost();


			System.out.println("The % of packets dropped: "+ pc_packets_dropped );
			System.out.println("***************************");
		}
		*/
		no_sources = 4;
		packet_gen_val = (float)0.2;

		for(queue_size =0; queue_size < 20; queue_size++)
		{	
			System.out.println("The queue_size : " + queue_size);
			n1 = new Network(no_sources, link_bandwidth, packet_gen_type, 
			packet_gen_val, queue_size, switching_technique, packet_size, allocation_time);

			n1.circuit_switching(500);
			float avg_delay = n1.calculate_average_delay();
			System.out.println("The average delay is: " + avg_delay);

			float pc_packets_dropped = n1.calculate_pc_packets_lost();


			System.out.println("The % of packets dropped: "+ pc_packets_dropped );
			System.out.println("***************************");
		}




	}	
}