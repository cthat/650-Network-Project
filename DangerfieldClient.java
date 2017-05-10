import java.net.*;
import java.util.*;
import java.io.*;

public class DangerfieldClient {
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		
		int psize, tout, byteSize, tcpPort = 23151, udpPort = 23152;
		String url;
		Socket s;
		InetAddress sAddr;
		DatagramSocket ds;
		DatagramPacket dp;
		Scanner scanner;
		PrintWriter out;
		BufferedReader br;
		byte[] recvData;
		boolean receiving;
				
		/*
		 * Get arguments: psize tout url
		 */
		scanner = new Scanner(System.in);		
		System.out.println("Enter: psize tout sname");
		String line = scanner.nextLine();
		String[] split = line.split("\\s+");
		scanner.close();
		psize = Integer.parseInt(split[0]);
		tout = Integer.parseInt(split[1]);
		url = split[2];
				
		/*
		 * Establish socket
		 */
		sAddr = InetAddress.getByName("localhost");
		s = new Socket(sAddr,tcpPort);
				
		/*
		 * Send psize and url to server
		 */
		out = new PrintWriter(s.getOutputStream(), true);
		out.println(psize);
		out.println(url);
						
		/*
		 * Get byteSize from server and send "size OK" message
		 */
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		byteSize = Integer.parseInt(br.readLine());
		
		System.out.println("Received: "+byteSize); // TESTING - Print out bytes received from server
		out = new PrintWriter(s.getOutputStream(), true);
		out.println("size OK");
				
		/* TODO: Set timeout to tout and wait to receive all packets from server over UDP 23152
		ds = new DatagramSocket(udpPort);
		recvData = new byte[psize];
		dp = new DatagramPacket(recvData, recvData.length);
						
		receiving = true;
		while (receiving) {
			ds.receive(dp);			
						
		}
		*/
	}
}
		
	