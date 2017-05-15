import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.Arrays;


public class DangerfieldServer {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		
		int TCPPort = 23151;
		ServerSocket ss = new ServerSocket(TCPPort);		
			
		while(true) {
			/* Accept connection and spawn thread */
			new ThreadHandler(ss.accept()).start();
		}		
	}
	
	private static class ThreadHandler extends Thread {
		private Socket s = null;
		
		public ThreadHandler(Socket s) {
			super("ThreadHandler");
			this.s = s;
		}
		
		public void run() {
			
			int psize, pageSize, UDPPort = 23152;
			String url=null, input=null;
			BufferedReader in;
			PrintWriter out;
						
			try {				
				
				/* Get Arguments from Client */
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				psize = Integer.parseInt(in.readLine());
				url = in.readLine();

				/* Open URL and receive contents */
				URLConnection ucon = new URL(url).openConnection();	
				in = new BufferedReader(new InputStreamReader(ucon.getInputStream()));
				String surl = "";
				while((input = in.readLine()) != null) {
					surl += input;
				}
							
				/* Send page byte size to client */
				out = new PrintWriter(s.getOutputStream(), true);
				pageSize = surl.getBytes().length;
				out.println(pageSize);
				
				/* Receive "size OK" message */
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				input = in.readLine();
				System.out.println(input);
				
				/* Establish UDP socket */
				DatagramSocket ds = new DatagramSocket();
				InetAddress ip = InetAddress.getByName("localhost");
		
				/* Send packets */
				int count = 0;		
				
				do {
				    
					/* Chunk data to psize packets and send over UDP */
					int bytesSent = 0;
					int soff = 0, eoff = psize;
					
					while(bytesSent < pageSize) {
						
						byte[] sendData = new byte[psize];
						sendData = Arrays.copyOfRange(surl.getBytes(), soff,  eoff);
											
						DatagramPacket sendPkt = new DatagramPacket(sendData, sendData.length, ip, UDPPort);
						ds.send(sendPkt);																	
						
						/* Increment counter based on bytes sent */
						bytesSent += sendData.length;
						
						/* Adjust offset */
						soff = eoff;
						eoff += psize;
					}
					
					/* Receive "page fail" or "page OK" message */
					input = in.readLine();
					System.out.println(input);
					
					count++;
				} while (count <= 1 && input.equals("page OK") == false);
				
				ds.close();
				
				/* Client received all packets */
				if (input.equals("page OK") == true) {
					
					/* Send "IPAddress/OK" message */
					out.println(s.getRemoteSocketAddress()+"/OK");
				}				

				s.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}				
		}	
	}

}
