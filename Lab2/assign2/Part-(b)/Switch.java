import java.util.*;

class Switch
{
	float link_bandwidth;
	ArrayList<Packet> switch_queue;
	int queue_size_max;
	int packets_reached;

	Switch(float link_bandwidth, int queue_size_max)
	{
		this.link_bandwidth = link_bandwidth;
		switch_queue = new ArrayList<Packet>();
		this.queue_size_max = queue_size_max;
		packets_reached = 0;

	}

	boolean enqueue_switch(Packet packet)
	{
		if(switch_queue.size() != queue_size_max)
		{
			this.switch_queue.add(packet);
			packets_reached++;
			return(true);
		}
		else
			return(false);

	}

	Packet dequeue_switch()
	{
		Packet packet = switch_queue.get(0);
        switch_queue.remove(0);
        packet.status = 3;
        return(packet);  
	}
}
