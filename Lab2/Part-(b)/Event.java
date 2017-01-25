class Event implements Comparable<Event>
{
	String event_name;
	Packet packet;
	float timestamp;
	int source_id; /* Required only when no packet present, for "Generate action" */

	Event(String event_name, float timestamp, Packet packet, int source_id)
	{
		this.event_name = event_name;
		this.timestamp = timestamp;
		this.packet = packet;
		this.source_id = source_id;
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
