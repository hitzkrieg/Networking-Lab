
import java.util.*;
import java.io.*;
import java.net.*;

public class Mutli_threaded
{
	public static String default_File_Name; 
	static int number_of_threads = 0 ;
	public  static ServerSocket welcome_socket;

	public static void main(String[] args) throws Exception
	{
		Scanner in = new Scanner(System.in);
		int port_number = 8090;
		File file = new File("configuration.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		int number_of_threads = Integer.parseInt(br.readLine());
		default_File_Name = br.readLine();
		ArrayList<String> ip_block = new ArrayList<String>();
		String temp = null;
		while((temp = br.readLine()) != null)
		{
			ip_block.add(temp);
		}
		welcome_socket = new ServerSocket(port_number);
		System.out.println("\n\nListening to connection on port : "+port_number);
		int p = 0;
		boo: while (p< number_of_threads)
		{
			try
			{
					Socket conn_socket = welcome_socket.accept();					
					String str = conn_socket.getRemoteSocketAddress().toString().substring(1);
					String[] strx = str.split(":");
					str = strx[0];
					for(int z = 0; z< ip_block.size(); z++)
					{
						if(ip_block.get(z).equals(str))
						{
							System.out.println("BLOCKED IP_ADDRESS");
							continue boo;					
						}
					}
					p++;
				  	System.out.println("currently total number_of_connections opened "+ 
				  		p+"/"+number_of_threads);
					System.out.println("Number of threads running: "+ Thread.activeCount());
					new new_thread_for_this_new_client(conn_socket, default_File_Name);
			}
			catch(IOException e)
			{
				System.err.println("\n\nSomething Bad Happend");
				String err = "Error !";
				System.out.printf("%9s\n\n",err);
				System.exit(1);
			}
		}
	}
}
class new_thread_for_this_new_client extends Thread
{
		public static String 	default_File_Name; 

	private Socket conn_socket ;
	public new_thread_for_this_new_client(Socket conn_socket, String s)
	{
		default_File_Name = s;
		this.conn_socket = conn_socket;
		start();
	}
	public void run()
	{
		try
		{
			BufferedReader in_from_client = new BufferedReader(
					new InputStreamReader(conn_socket.getInputStream()));
			PrintStream out_to_client = new PrintStream(new 
					BufferedOutputStream(conn_socket.getOutputStream()));
			

			String str ="";
			System.out.println("\n\nHTTP request :\n");
			str = in_from_client.readLine();
			System.out.println(str);
			String[] arr_ = str.split("\\s+");
			String filename="";
			if(arr_[0].equals("GET"))
			{
				if(arr_[1].length() == 1 ) filename = default_File_Name;
				else filename = arr_[1].substring(1);
			}
			try
			{
				InputStream input_stream = new FileInputStream(filename);
				out_to_client.print("HTTP/1.1 200 OK\r\n");
				if(filename.endsWith(".html")) out_to_client.print("Content type : text/html\r\n\r\n");

				byte[] array = new byte[4096];
				int n = 0 ;
				while((n=input_stream.read(array))>0) 
				{
					out_to_client.write(array,0,n);
				}
				out_to_client.close();
				Thread.sleep(5000);
			}
			catch (FileNotFoundException x) 
			{
        			out_to_client.println("HTTP/1.0 404 Not Found\r\n"+
          			"Content-type: html\r\n\r\n"+
          			"<html><head></head><body>"+filename+" not found</body></html>\n");
        			out_to_client.close();
      			}
		}
		catch(Exception e)
		{
			System.out.println("Error");
		}
		finally
		{
			try
			{
				conn_socket.close();
			}
			catch (Exception e)
			{
				System.out.println(e);
			}
		}
	}
}