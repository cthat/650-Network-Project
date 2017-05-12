import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.Arrays;

public class DangerfieldServer {
    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException {
        int SPORT = 23151;
        boolean listening = true;

        ServerSocket ss = new ServerSocket(SPORT);

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
            int psize = 0, byteSize;
            String input = null;
            BufferedReader br;
            URL sname;
            PrintWriter out;

            try {
                /*
                 * Get psize and url from client
                 */
                br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                psize = Integer.parseInt(br.readLine());
                input = "http://"+br.readLine();
                // TESTING - prints arguments received from client
                System.out.println("Received: "+psize+" "+input); 				
                
                /*
                 * Get URL and send bytes received to client
                 */
                sname = new URL(input);
                br = new BufferedReader(new InputStreamReader(sname.openStream()));
                // TESTING - Prints the HTML received from the url
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);				
                }		
                
                URLConnection yc = sname.openConnection();

                PrintWriter pw = new PrintWriter(s.getOutputStream());
                pw.println(yc.getContentLength());
                pw.flush();
                BufferedReader read = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String Response = read.readLine();
                if (Response != null) {
                    System.out.println("Client1 Says " + Response);
                }
 
                getBytes(psize, String.valueOf(sname), yc);
                s.close();

            } catch (IOException e1) {
                e1.printStackTrace();
            }						
        }
    }
    
    //Send UDP packets 23152
    public static void getBytes(int psize, String sname, URLConnection urlConnection) throws IOException {
        InetAddress address = InetAddress.getByName("127.0.0.1");
        byte[] imgDataBa = new byte[urlConnection.getContentLength()];
        DataInputStream dataIs = new DataInputStream(urlConnection.getInputStream());
        dataIs.readFully(imgDataBa);
        dataIs.close();
        Integer count =0;
      for (int i = 0; i < imgDataBa.length;) {
          count++;
         if (imgDataBa.length - i < psize) { //send last packet with remaining bytes
            byte[] newByte = Arrays.copyOfRange(imgDataBa, i, imgDataBa.length+1);

            newByte[newByte.length-1] = count.byteValue();

            DatagramPacket packet = new DatagramPacket(newByte, newByte.length, address, 23152);
            // Create a datagram socket, send the packet through it, close
            // it.
            DatagramSocket dsocket = new DatagramSocket();
            dsocket.send(packet);
            dsocket.close();
         } else { // send packets with psize
            byte[] newByte = Arrays.copyOfRange(imgDataBa, i, i + psize+1);

                newByte[psize] 	 = count.byteValue();

                DatagramPacket packet = new DatagramPacket(newByte, newByte.length, address, 23152);
            // Create a datagram socket, send the packet through it, close it.
            DatagramSocket dsocket = new DatagramSocket();
            dsocket.send(packet);
            dsocket.close();
         }
         i += psize;
        }

    }
}
			
