import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;

class Single_threaded
{
	public static void main(String[] args) throws Exception
	{
		ServerSocket welcome_socket=null;

		Scanner in = new Scanner(System.in);
		int port_number = 8090;

		welcome_socket = new ServerSocket(port_number);
		System.out.println("Listening to connection on port : "+port_number);

		Socket conn_socket=null; String filename="";
		PrintStream out_to_client = null;
		BufferedReader in_from_client= null ;

		while(true)
		{
			try
			{
				conn_socket = welcome_socket.accept();
				in_from_client = new BufferedReader(new 
						InputStreamReader(conn_socket.getInputStream()));
				out_to_client  = new PrintStream(new 
			  			BufferedOutputStream(conn_socket.getOutputStream()));
				String str ="";
				System.out.println("\n\nHTTP request :\n");
				str = in_from_client.readLine();
				System.out.println(str);
				String[] arr_ = str.split("\\s+");
				if(arr_[0].equals("GET"))
				{
					filename = arr_[1].substring(1);
				}
				out_to_client.println(filename);
				
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
							}
			catch (FileNotFoundException x) 
			{
				
				filename = "404.html" ;
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
}