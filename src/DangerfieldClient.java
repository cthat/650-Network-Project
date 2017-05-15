import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class DangerfieldClient {
	public static void main(String[] args) throws IOException {		
		
		//Array to store packets, position in array assigned by order in which server sends packets
                DatagramPacket[] datagramPacketArry = new DatagramPacket[100];
            
                int psize, tout, pageSize, TCPPort=23151, UDPPort=23152;
		String sname, srvMsg;
		InetAddress ip = InetAddress.getByName("localhost");
		Socket s = new Socket(ip, TCPPort);
		BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		PrintWriter out = new PrintWriter(s.getOutputStream(), true); 
		
		/* Get arguments from user */
		Scanner scanner = new Scanner(System.in);		
		System.out.println("Enter: psize tout sname");
		String line = scanner.nextLine();
		String[] split = line.split("\\s+");
		scanner.close();
		psize = Integer.parseInt(split[0]);	
		tout = Integer.parseInt(split[1]) * 1000;	
		sname = "http://"+split[2];
				
		/* Send server psize and sname */	
		out.println(psize);
		out.println(sname);		
		
		/* Get page Size from server */		
		pageSize = Integer.parseInt(in.readLine());		
		
		/* Send "size OK" message */
		out.println("size OK");		
		
		/* Set up UDP socket, set timeout to tout */
		DatagramSocket ds = new DatagramSocket(UDPPort);
		ds.setSoTimeout(tout);
			
		String html = "";		
		boolean allBytesRcvd = false;
		int count = 0, seqNum=1, pos;		
		do {
			
			try {
				
				int rcvdBytes = 0;
				while(rcvdBytes < pageSize) {
					
					byte[] rcvdData = new byte[psize];
					DatagramPacket rcvdPkt = new DatagramPacket(rcvdData, rcvdData.length, ip, UDPPort);					
					ds.receive(rcvdPkt);
                                        pos = Integer.parseInt(in.readLine());
					
					//html += new String(rcvdPkt.getData());
					System.out.println("received packet "+seqNum);
			
					/* Increment count based on bytes received */
					rcvdBytes += rcvdData.length;
                                        
                                        //store packets in order sent by server
                                        datagramPacketArry[pos] = rcvdPkt;
					
					seqNum++;
				}
				
				if (rcvdBytes >= pageSize) {
					/* All bytes received */
					allBytesRcvd = true;
				}
				
			} catch (SocketTimeoutException e) {
				
				/* Double Timeout */
				ds.setSoTimeout(tout * 2);
				
				/* Send "page fail" message */
				out.println("page fail");
                                
                                if(count == 1)
                                    out.println("quit");				
			}
			
			count++;
			
		} while (count <= 1 && allBytesRcvd == false);
		
		ds.close();
					
		/* All bytes received */
		if (allBytesRcvd == true) {
			
			/* Send "page OK" message */
			out.println("page OK");			
			
			/* Receive IP Address/OK message */
			srvMsg = in.readLine();
			System.out.println(srvMsg);
			
			/* Print HTML */
			for(int i = 0; i < 100; i++){
                            if(datagramPacketArry[i] != null)
                                html += new String(datagramPacketArry[i].getData());
                }
                        System.out.println(html);
		}		
		
		s.close();		
	}
}