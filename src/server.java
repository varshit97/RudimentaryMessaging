
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class server {
    public static Socket clientSocket[] = new Socket[2];
    public static int count = 0;
    
    public static void main(String args[]) {

        Socket s = null;
        ServerSocket ss2 = null;
        System.out.println("Server Listening......");
        try {
            ss2 = new ServerSocket(4445); // can also use static final PORT_NUM , when defined

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server error");

        }

        while (count != 2) {
            try {
                s = ss2.accept();
                clientSocket[count] = s;
                count++;
                System.out.println("connection Established " + count);
            } 
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("Connection Error");

            }
        }
        int count = 0;
        ServerThread st = new ServerThread(clientSocket[0], clientSocket, 1);
        ServerThread st1 = new ServerThread(clientSocket[1], clientSocket, 0);
        st.start();
        st1.start();
    }
}

class ServerThread extends Thread {

    String line = null;
    BufferedReader is = null;
    PrintStream os = null;
    Socket s = null;
    Socket clientSocket[] = null;
    int index = 0;
    
    public ServerThread(Socket s, Socket clientSocket[], int index) {
        this.s = s;
        this.clientSocket = clientSocket;
        this.index = index;
    }

    @Override
    public void run() {
        try {
            is = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
            os = new PrintStream(this.clientSocket[index].getOutputStream());

        } catch (IOException e) {
            System.out.println("IO error in server thread");
        }

        try {
            line = is.readLine();
            while (line.compareTo("QUIT") != 0) {
                os.println(line);
                os.flush();
                System.out.println("Response to Client" + index + " : " + line);
                line = is.readLine();
            }
        } catch (IOException e) {

            line = this.getName(); //reused String line for getting thread name
            System.out.println("IO Error/ Client " + line + " terminated abruptly");
        } catch (NullPointerException e) {
            line = this.getName(); //reused String line for getting thread name
            System.out.println("Client " + line + " Closed");
        } finally {
            try {
                System.out.println("Connection Closing..");
                if (is != null) {
                    is.close();
                    System.out.println(" Socket Input Stream Closed");
                }

                if (os != null) {
                    os.close();
                    System.out.println("Socket Out Closed");
                }
                if (s != null) {
                    s.close();
                    System.out.println("Socket Closed");
                }

            } catch (IOException ie) {
                System.out.println("Socket Close Error");
            }
        }//end finally
    }
}
