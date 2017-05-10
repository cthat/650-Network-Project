import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

public class Client {
	public static void main(String[] args) throws Exception {
		// Client port 23152
		PrintWriter pw;
		Scanner scanner = new Scanner(System.in);
		// user inputs for psize waittime and sname
		System.out.println("Enter 3 values in single line and single space");
		System.out.println("psize WaitTime sname");
		String line = scanner.nextLine();
		String[] splited = line.split("\\s+");
		if (splited.length != 3) {
			System.out.println("wrong input");
			scanner.close();
			return;
		}
		int psize = Integer.parseInt(splited[0]);
		if (psize > 1400) {
			System.out.println("Invalid psize");
			scanner.close();
			return;
		}

		int waitTime = Integer.parseInt(splited[1]);
		String sname = "http://" + splited[2];
		if (!isValidURL(sname)) {
			System.out.println("Invalid sname");
			scanner.close();
			return;
		}
		
		scanner.close();

		// send psize and sname to server local host
		SocketAddress sockaddr = new InetSocketAddress("127.0.0.1", 23151);
		Socket s = new Socket();
		s.connect(sockaddr, waitTime);
		pw = new PrintWriter(s.getOutputStream());
		pw.println(psize);
		pw.println(sname);
		System.out.println("Request Send");
		pw.flush();
		// send "size OK" message to server after total page bytes received
		InputStreamReader ir = new InputStreamReader(s.getInputStream());
		BufferedReader br = new BufferedReader(ir);
		String input = br.readLine();
		// while ((input = br.readLine()) != null)
		if (input != null) {
			int pageBytes = Integer.parseInt(input);
			System.out.println("Page byte received from server : " + pageBytes);
		}
		pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
		pw.println("size OK");
		pw.flush();
		br.close();
		s.close();

		// Client port 23152
		try {
			DatagramSocket dsocket = new DatagramSocket(23152);
			byte[] buffer = new byte[psize+1];
			DatagramPacket dpacket = new DatagramPacket(buffer, buffer.length);
			dsocket.setSoTimeout(10000);
			while (true) {
				try { // recieve data until timeout
					dsocket.receive(dpacket);
					Byte dd = buffer[dpacket.getLength()-1];
					System.out.println("Received packet " +dd.intValue());
					String packet = new String(buffer, 0, dpacket.getLength());
//					System.out.println("Received packet " + packet);
					dpacket.setLength(buffer.length);
				} catch (SocketTimeoutException e) {
					// timeout exception.
					System.out.println("Timeout reached!!! ");
					dsocket.close();
				}
			}
		} catch (SocketException e1) {
			System.out.println("Socket closed");
		}

	}

	public static boolean isValidURL(String url) {

		URL u = null;

		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}

		try {
			u.toURI();
		} catch (URISyntaxException e) {
			return false;
		}

		return true;
	}
}
