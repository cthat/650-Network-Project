import java.io.IOException;
import java.net.*;
import java.io.*;

public class DangerfieldServer {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		int sPort = 23151;
		boolean listening = true;
	
		ServerSocket ss = new ServerSocket(sPort);
		
		/*
		 * Server listens for new clients
		 * When they arrive they are accepted and a thread spawned to handle the work
		 */
		while(listening) {
			new WorkerThread(ss.accept()).start();
		}		
	}	
	
	/* WorkerThread
	 * 
	 * Handles all the work with the client
	 * 
	 */
	static class WorkerThread extends Thread {
		private Socket s = null;
		
		public WorkerThread(Socket s) {
			super("WorkerThread");
			this.s = s;			
		}
		
		public void run() {
			int psize=0, byteSize;
			String input=null;
			BufferedReader br;
			URL geturl;
			PrintWriter out;
		
			try {
				/*
				 * Get psize and url from client
				 */
				br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				psize = Integer.parseInt(br.readLine());
				input = "http://"+br.readLine();
				System.out.println("Received: "+psize+" "+input); 				// TESTING - prints arguments received from client
				
				/*
				 * Get URL and send bytes received to client
				 */
				geturl = new URL(input);
				br = new BufferedReader(new InputStreamReader(geturl.openStream()));
				// TESTING - Prints the HTML received from the url
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(line);				
				}		

				// TODO - Send bytes received from webpage to client
				// sends dummy value for now: 2 * psize + (psize/2)
				byteSize = (2*psize)+(psize/2);
				out = new PrintWriter(s.getOutputStream(), true);
				out.println(byteSize);
				
				/*
				 * Get "size OK" message for client
				 */
				br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				input = br.readLine();
				System.out.println(input); 				// TESTING - Prints out "size OK" from client
				
				// TODO - Send packets to client with bytes from webpage using UDP socket 23152
				
				s.close();
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}						
		}
	}
}
			
