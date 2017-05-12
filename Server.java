import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Server {

    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(23151);
        Socket s;
        int psize;
        String sname;
        
        System.out.println("Trying to establish client connection");

        s = ss.accept();
        System.out.println("Connection established");
        InputStreamReader ir = new InputStreamReader(s.getInputStream());

        BufferedReader br = new BufferedReader(ir);
        psize = Integer.parseInt(br.readLine());
        sname = br.readLine();

        URL obj = new URL(sname);

        URLConnection yc = obj.openConnection();

        PrintWriter pw = new PrintWriter(s.getOutputStream());
        pw.println(yc.getContentLength());
        pw.flush();
        BufferedReader read = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String Response = read.readLine();
        if (Response != null) {
            System.out.println("Client1 Says " + Response);
        }
        s.close();
        ss.close();
        //send UDP packets to port 
        getBytes(psize, sname, yc);

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
