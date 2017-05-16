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
		
		int psize, tout, pageSize, TCPPort=23151, UDPPort=23152, maxPkts=1000;
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
			
		/* Calculate Max Packets expected */
		maxPkts = (pageSize/psize)+1;		
		
		/* String array to hold contents of packets in order */
		String[] dpArray = new String[maxPkts];
		
		/* Receive packets over UDP */
		String html = "";		
		boolean allBytesRcvd = false;
		int count = 0;
		do {			

			try {
				
				int seqNum;
				int rcvdBytes = 0;
				
				while(rcvdBytes < pageSize) {
					
					/* set up a packet to receive from server */
					/* psize + 1 accounts for seqNum appended */
					byte[] rcvdData = new byte[psize+1];					
					DatagramPacket rcvdPkt = new DatagramPacket(rcvdData, rcvdData.length, ip, UDPPort);					
					ds.receive(rcvdPkt);
					
					/* TODO */
					/* Extract seqNum from rcvdData */
					String temp = new String(rcvdData).split("~")[0];					
					seqNum = Integer.parseInt(temp);					
					
					/* Print received message and sequence number */
					System.out.println("received packet "+seqNum);										
					
					/* Add packet data to string array to keep order */
					/* Use substring to ignore seqNum, use length of temp string + 1 for index */
					/* seqNum starts at 1, seqNum-1 to account for that */
					dpArray[seqNum-1] = new String(rcvdPkt.getData()).substring(temp.length()+1);			
								
					/* Increment count based on bytes received */
					rcvdBytes += rcvdData.length;					

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
			for (int i = 0; i < maxPkts; i++) {
				if (dpArray[i] != null) {
					html += dpArray[i];
				}				
			}
			System.out.println(html);
			
		} else {
			
			/* Receiving packets failed */
			System.out.println("quit");
		}				
		
		/* Close socket */
		s.close();		
	}
}