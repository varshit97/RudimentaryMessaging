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
import java.io.RandomAccessFile;
import static java.lang.Math.log;
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
                    if (in.startsWith("sendTCP")) {
                        String filename = in.split(" ")[1];
                        pr1.println(in);
                        sendFileTCP(filename);
                        continue;
                    }
                    if (in.startsWith("sendUDP")) {
                        String filename = in.split(" ")[1];
                        pr1.println(in + " " + new File(filename).length());
                        sendFileUDP(filename);
                        continue;
                    }
                    pr1.println(in);
                } while (!in.equals("END"));
            } else {
                do {
                    br2 = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = br2.readLine();
                    if (!out.isEmpty()) {
                        System.out.println();
                    }
                    if (out.startsWith("sendUDP")) {
                        String name = out.split(" ")[1];
                        String size = out.split(" ")[2];
                        String[] temp = name.split("/");
                        String filename = temp[temp.length - 1];
                        receiveFileUDP(filename, Integer.parseInt(size));
//                        t2.sleep(200);
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

            System.out.println("Size : " + myFile.length());
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);

            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            DatagramPacket sendPacket = new DatagramPacket(mybytearray, mybytearray.length, IPAddress, 5001);
            clientSocket.send(sendPacket);
            for (int i = 0; i <= myFile.length(); i = i + 10) {
                progressPercentage(i, (int) myFile.length());
                Thread.sleep(500);
            }
            clientSocket.close();

            System.out.println("File " + fileName + " sent to Alice.");

        } catch (Exception e) {
            System.err.println("Error! " + e);
        }
    }

    public void progressPercentage(int remain, int total) {
        if (remain > total) {
            throw new IllegalArgumentException();
        }
        int maxBareSize = 10; // 10unit for 100%
        int remainProcent = ((100 * remain) / total) / maxBareSize;
        char defaultChar = ' ';
        String icon = "=";
        String bare = new String(new String(new char[maxBareSize]) + ">").replace('\0', defaultChar) + "]";
        StringBuilder bareDone = new StringBuilder();
        bareDone.append("[");
        for (int i = 0; i < remainProcent; i++) {
            bareDone.append(icon);
        }
        String bareRemain = bare.substring(remainProcent, bare.length());
        System.out.print("\r" + bareDone + bareRemain + " " + remainProcent * 10 + "%");
        if (remain == total) {
            System.out.print("\n");
        }
    }

    public void receiveFileUDP(String fileName, int size) throws InterruptedException {
        try {
            byte[] receiveData = new byte[size];
            DatagramSocket sockety = new DatagramSocket(5001);

            FileWriter fw = new FileWriter(new File("received_from_alice_" + fileName));
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                sockety.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                fw.write(sentence);
                fw.flush();
                for (int i = 0; i <= receiveData.length; i = i + 10) {
                    progressPercentage(i, receiveData.length);
                    Thread.sleep(500);
                }
                t2.sleep(2);
                break;
            }
            fw.flush();
            fw.close();
            sockety.close();

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

            for (int i = 0; i <= myFile.length(); i = i + 10) {
                progressPercentage(i, (int) myFile.length());
                Thread.sleep(500);
            }

            System.out.println("File " + fileName + " sent to Alice.");

        } catch (Exception e) {
            System.err.println("Error! " + e);
        }
    }

    public void receiveFileTCP() throws InterruptedException {
        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(socket.getInputStream());

            String fileName = clientData.readUTF();
            System.out.println(fileName);
            OutputStream output = new FileOutputStream(("received_from_alice_" + fileName));
            long size = clientData.readLong();
            int newSize = (int) size;
            byte[] buffer = new byte[4096];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
                for (int i = 0; i <= newSize; i = i + 10) {
                    progressPercentage(i, newSize);
                    Thread.sleep(500);
                }
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
