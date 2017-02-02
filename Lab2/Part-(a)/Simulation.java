class Simulation
{


	public static void main(String[] args)
	{
		int no_sources = 2;
		float link_bandwidth = 800;  
		String packet_gen_type = "Uniform"; 
		float packet_gen_val = (float)0.1;
		int queue_size = 10;
		String switching_technique = "Packet";
		int packet_size = 10;
		float k = 10;
		float alpha = 1;
		float lambda = 1;
		String packet_length_dis= "Pareto";
		Network n1;
		
		for(alpha = 1; alpha < 10; alpha++)
		{	
			System.out.println("The value of alpha: " + alpha);
			n1 = new Network(no_sources, link_bandwidth, packet_gen_type, packet_gen_val, queue_size,
			 switching_technique, packet_size, 50000, k, alpha, lambda, packet_length_dis);

			n1.packet_switching();
			float avg_delay = n1.calculate_average_delay();
			 System.out.println("The average delay is: " + avg_delay); 

			float pc_packets_dropped = n1.calculate_pc_packets_lost();
			float link_util = n1.calculate_link_utilization();
			
			System.out.println("The % of packets dropped: "+ pc_packets_dropped );
			System.out.println("The % link utilization: "+ link_util );
			System.out.println("***************************"); 


			/*System.out.println(no_sources + " " + avg_delay + " " + pc_packets_dropped);*/
		}
		

		// no_sources = 10;

		// for(packet_gen_val = (float)0.02; packet_gen_val < 1; packet_gen_val += (float)0.04)
		// {	
		// 	/*System.out.println("The inter packet time : " + packet_gen_val);*/
		// 	n1 = new Network(no_sources, link_bandwidth, packet_gen_type, 
		// 	packet_gen_val, queue_size, switching_technique, packet_size, 500);

		// 	n1.packet_switching();
		// 	float avg_delay = n1.calculate_average_delay();
		// 	/* System.out.println("The average delay is: " + avg_delay); */

		// 	float pc_packets_dropped = n1.calculate_pc_packets_lost();


		// 	/*System.out.println("The % of packets dropped: "+ pc_packets_dropped );
		// 	System.out.println("***************************");*/

		// 	System.out.println(packet_gen_val + " " + avg_delay + " " + pc_packets_dropped);
		// }
		

		
		// no_sources = 4;
		// packet_gen_val = (float)0.2;

		// for(queue_size =0; queue_size < 20; queue_size++)
		// {	
		// 	/* System.out.println("The queue_size : " + queue_size); */
		// 	n1 = new Network(no_sources, link_bandwidth, packet_gen_type, 
		// 	packet_gen_val, queue_size, switching_technique, packet_size, 500);

		// 	n1.packet_switching();
		// 	float avg_delay = n1.calculate_average_delay();
		// 	/*System.out.println("The average delay is: " + avg_delay);*/

		// 	float pc_packets_dropped = n1.calculate_pc_packets_lost();
		// 	System.out.println(queue_size + " " + pc_packets_dropped + " " + avg_delay);

		// 	/* System.out.println("The % of packets dropped: "+ pc_packets_dropped );
		// 	System.out.println("***************************"); */
		// }

		




	}	
}