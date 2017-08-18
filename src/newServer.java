//Bob

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;

public class newServer implements Runnable {

    ServerSocket serversocket;
    BufferedReader br1, br2;
    PrintWriter pr1;
    Socket socket;
    Thread t1, t2;
    String in = "", out = "";

    public newServer() {
        try {
            t1 = new Thread(this);
            t2 = new Thread(this);
            serversocket = new ServerSocket(5000);
            System.out.println("Server is waiting. . . . ");
            socket = serversocket.accept();
            System.out.println("Client connected with IP " + socket.getInetAddress().getHostAddress());
            t1.start();
            t2.start();

        } catch (Exception e) {
        }
    }

    public void run() {
        try {
            if (Thread.currentThread() == t1) {
                do {
                    br1 = new BufferedReader(new InputStreamReader(System.in));
                    pr1 = new PrintWriter(socket.getOutputStream(), true);
                    System.out.print("You>>");
                    in = br1.readLine();
//                    if(in.isEmpty())
//                    {
//                        System.out.println();
//                    }
                    if (in.startsWith("sendTCP")) {
                        String filename = in.split(" ")[1];
                        pr1.println(in);
                        sendFileTCP(filename);
                        continue;
                    }
                    if (in.startsWith("sendUDP")) {
                        String filename = in.split(" ")[1];
                        pr1.println(in);
                        sendFileUDP(filename);
                        continue;
                    }
                    pr1.println(in);
                } while (!in.equals("END"));
            } else {
                do {
                    br2 = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = br2.readLine();
                    if(!out.isEmpty())
                    {
                        System.out.println();
                    }
                    if (out.startsWith("sendUDP")) {
                        String name = out.split(" ")[1];
                        String[] temp = name.split("/");
                        String filename = temp[temp.length - 1];
                        receiveFileUDP(filename);
                        t2.sleep(500);
                        System.out.print("You>>");
                        continue;
                    }
                    if (out.startsWith("sendTCP")) {
                        receiveFileTCP();
                        t2.sleep(500);
                        System.out.print("You>>");
                        continue;
                    }
                    System.out.println("Alice>>" + out);
                    System.out.print("You>>");
                } while (!out.equals("END"));
            }
        } catch (Exception e) {
        }
    }

    public void sendFileUDP(String fileName) {
        try {

            File myFile = new File(fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);

            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            DatagramPacket sendPacket = new DatagramPacket(mybytearray, mybytearray.length, IPAddress, 5000);
            clientSocket.send(sendPacket);
            clientSocket.close();

            System.out.println("File " + fileName + " sent to Alice.");

        } catch (Exception e) {
            System.err.println("Error! " + e);
        }
    }

    public void receiveFileUDP(String fileName) {
        try {
            byte[] receiveData = new byte[4096];
            DatagramSocket socket = new DatagramSocket(5001);

            FileWriter fw = new FileWriter(new File("received_from_alice_" + fileName));
            while (receiveData != null) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                fw.write(sentence);
                fw.flush();
            }
            fw.flush();
            fw.close();
            socket.close();

            System.out.println("File " + fileName + " received from Alice.");

        } catch (IOException ex) {
            System.err.println("Error." + ex);
        }
    }

    public void sendFileTCP(String fileName) {
        try {

            File myFile = new File(fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);

            OutputStream os = socket.getOutputStream();

            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();

            System.out.println("File " + fileName + " sent to Alice.");

        } catch (Exception e) {
            System.err.println("Error! " + e);
        }
    }

    public void receiveFileTCP() {
        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(socket.getInputStream());

            String fileName = clientData.readUTF();
            System.out.println(fileName);
            OutputStream output = new FileOutputStream(("received_from_alice_" + fileName));
            long size = clientData.readLong();
            byte[] buffer = new byte[4096];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            output.flush();
            output.close();

            System.out.println("File " + fileName + " received from Alice.");

        } catch (IOException ex) {
            System.err.println("Error." + ex);
        }
    }

    public static void main(String[] args) {
        new newServer();
    }
}