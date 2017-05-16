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
	
	static class ThreadHandler extends Thread {
		private Socket s = null;
		
		public ThreadHandler(Socket s) {
			super("ThreadHandler");
			this.s = s;
		}
		
		public void run() {
			
			int psize, pageSize, UDPPort = 23152;
			String url=null, clientMsg=null, html=null;
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
				String webPage = "";
				while((html = in.readLine()) != null) {
					webPage += html;
				}							
							
				/* Get pageSize from webPage length */
				pageSize = webPage.getBytes().length;
				
				/* Adjust pageSize based on number of packets expected */
				/* Accounts for appended seqNum for each packet */				
				pageSize += (pageSize/psize);				
				
				/* Send page byte size to client */
				out = new PrintWriter(s.getOutputStream(), true);
				out.println(pageSize);
				
				/* Receive "size OK" message */
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				clientMsg = in.readLine();
				System.out.println(clientMsg);
				
				/* Establish UDP socket */
				DatagramSocket ds = new DatagramSocket();
				InetAddress ip = InetAddress.getByName("localhost");				
		
				/* Send packets over UDP */
				int count = 0;
				do {
				    
					int bytesSent = 0;
					int soff = 0, eoff = psize;					
					int seqNum = 1;
																									
					while(bytesSent < pageSize) {
										
						/* Chunk string into a psized chunk */
						byte[] sendData = new byte[psize];
						sendData = Arrays.copyOfRange(webPage.getBytes(), soff,  eoff);
						
						/* TODO */
						/* Take new psized string and append the sequence number */
						String seqString = new String(sendData);
						seqString = seqNum + "~" + seqString;						
						
						/* Convert it back into a byte array for sending */
						sendData = seqString.getBytes();
	
						/* Set up packet and send */
						DatagramPacket sendPkt = new DatagramPacket(sendData, sendData.length, ip, UDPPort);						
						ds.send(sendPkt);							
						
						/* Increment counter based on bytes sent */
						bytesSent += sendData.length;
						
						/* Adjust offsets */
						soff = eoff;
						eoff += psize;
						
						/* Next packet number */ 
						seqNum++;
					}
					
					/* Receive "page fail" or "page OK" message */					
					clientMsg = in.readLine();
					System.out.println(clientMsg);
					
					count++;
				} while (count <= 1 && clientMsg.equals("page OK") == false);
				
				ds.close();				
				
				/* Client received all packets */
				if (clientMsg.equals("page OK") == true) {			
					
					/* Send "IPAddress/OK" message */
					out.println(s.getInetAddress()+"/OK");					
					
				} else {
					
					/* Sending webpage failed */
					System.out.println("quit");
				}				
				
				/* Close socket */
				s.close();			
								
			} catch (IOException e) {
				e.printStackTrace();
			}				
		}	
	}
}
