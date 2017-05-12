import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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
        PrintWriter pw;
        int psize, tout, pageBytes;
        //TODO: Can probably get rid of false assignment here
        //variable to use as comparison for send/rec success
        boolean success = false;
        String line;
        String[] splitString;

        Scanner scanner = new Scanner(System.in);
        // user inputs for psize waittime and sname
        System.out.println("Enter 3 values in single line and single space");
        System.out.println("psize WaitTime sname");
        line = scanner.nextLine();
        splitString = line.split("\\s+");
        if (splitString.length != 3) {
            System.out.println("wrong input");
            scanner.close();
            return;
        }
        psize = Integer.parseInt(splitString[0]);
        if (psize > 1400) {
            System.out.println("Invalid psize");
            scanner.close();
            return;
        }

        tout = Integer.parseInt(splitString[1]);
        tout = tout + (tout * 1000);
        String sname = "http://" + splitString[2];
        if (!isValidURL(sname)) {
            System.out.println("Invalid sname");
            scanner.close();
            return;
        }

        scanner.close();

        // send psize and sname to server local host
        SocketAddress sockaddr = new InetSocketAddress("127.0.0.1", 23151);
        Socket s = new Socket();
        s.connect(sockaddr, tout);
        pw = new PrintWriter(s.getOutputStream());
        pw.println(psize);
        pw.println(sname);
        System.out.println("Request Sent");
        pw.flush();
        // send "size OK" message to server after total page bytes received
        InputStreamReader ir = new InputStreamReader(s.getInputStream());
        BufferedReader br = new BufferedReader(ir);
        String input = br.readLine();
        // while ((input = br.readLine()) != null)
        if (input != null) {
            pageBytes = Integer.parseInt(input);
            System.out.println("Page bytes received from server : " + pageBytes);
        }
        pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
        pw.println("size OK");
        pw.flush();
        br.close();
        s.close();

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
