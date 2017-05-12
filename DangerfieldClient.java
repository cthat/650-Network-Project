import java.net.*;
import java.util.*;
import java.io.*;

public class DangerfieldClient {
	
    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {

        int psize, tout, byteSize;
        int TCPPORT = 23151, UDPPORT = 23152;
        //TODO: Can probably get rid of false assignment here
        //variable to use as comparison for send/rec success
        boolean success = false;
        String url, line;
        String [] split;
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
        //TODO: Need to add back in input validation. Need to convert tout input from sec to ms?
        scanner = new Scanner(System.in);		
        System.out.println("Enter: psize tout sname");
        line = scanner.nextLine();
        split = line.split("\\s+");
        scanner.close();
        psize = Integer.parseInt(split[0]);
        tout = Integer.parseInt(split[1]);
        url = split[2];

        /*
         * Establish socket
         */
        sAddr = InetAddress.getByName("localhost");
        s = new Socket(sAddr,TCPPORT);

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
        
        // Client port 23152
        connectToPort23152(psize, tout);
        
        //TODO: Need to compare rec'd page bytes to pageBytes.
        
        //TODO: If pageBytes matches, send single message page OK
        
        //TODO: Print the text in the page, in the correct order regardless of when packets arrived
        
        //TODO: Server needs to print IP address/OK when client rec'd everything correctly
        
        //TODO: If all bytes not rec'd before timeout, client sends message page fail to server
        
        //TODO: if server gets page fail msg, it retransmits all page bytes w/o another request to web server
        if(success == false){
            //doubling size of tout to retry connection
            tout = tout * 2;
            connectToPort23152(psize, tout);
        }
        //TODO: Will need to see if second time was successful, and if so, update success to true
        
        if(success == false){
            System.out.println("\n quit");
            //TODO: If fails again, server prints msg quit
        }
    }
    
    public static void connectToPort23152(int psize, int tout) throws IOException{
        try {
            DatagramSocket dsocket = new DatagramSocket(23152);
            byte[] buffer = new byte[psize+1];
            DatagramPacket dpacket = new DatagramPacket(buffer, buffer.length);
            //dsocket.setSoTimeout(10000);
            dsocket.setSoTimeout(tout);
            while (true) {
                try { // recieve data until timeout
                    dsocket.receive(dpacket);
                    Byte dd = buffer[dpacket.getLength()-1];
                    System.out.println("Received packet " +dd.intValue());
                    String packet = new String(buffer, 0, dpacket.getLength());
                    dpacket.setLength(buffer.length);
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout reached!!! ");
                    dsocket.close();
                }
            }
        } catch (SocketException e1) {
            System.out.println("Socket closed");
        }
    }
}
		
	