class Packet
{
	int source_id;
	int packet_number;
	float time_gen;
	float time_recieved;
	int status;
	int packet_size;
	/*
		Status 1: At source
		Status 2: At switch	
		Status 3: At sink
	*/

	Packet(int source_id, int packet_number, float time_gen, int packet_size)
	{
		status = 1;
		this.source_id =  source_id;
		this.time_gen = time_gen;
		this.packet_number = packet_number;
		this.packet_size = packet_size;

	}	
}