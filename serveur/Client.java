package serveur;

import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            
            System.out.println("Connecté au serveur Scarbell!");
            System.out.println(in.readLine());
            
            String message;
            while (true) {
                System.out.print("Vous: ");
                message = userInput.readLine();
                if (message.equalsIgnoreCase("quit")) break;
                if (message.startsWith("PLACER")) {
                    out.println(message);
                    System.out.println("Serveur: " + in.readLine());
                } else {
                    out.println(message);
                    System.out.println("Serveur: " + in.readLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}