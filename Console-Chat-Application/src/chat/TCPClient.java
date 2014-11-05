/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TCPClient{
    public static void main(String[] args) throws IOException{
        Client c=new Client();
    }
}


class Client {

    private static InetAddress host;
    private static final int PORT = 12000;
    private static String nickname;
    static Socket link;

    Client() throws IOException{
        runClient();
    }
    public static void runClient() throws IOException {
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException uhEx) {
            System.out.println("Host ID not found!!");
            System.exit(1);
        }
        link = new Socket(host, PORT);
        Scanner input = new Scanner(System.in);
        System.out.print("Nickname: ");
        nickname = input.nextLine();
        Sender as = new Sender(link, nickname);
        Receiver rc = new Receiver(link);
        as.start();
        rc.start();
    }
}


class Sender extends Thread {

    Socket link;
    String nickname;

    public Sender(Socket link, String s) {
        this.link = link;
        nickname = s;
    }

    private synchronized void Nick(String NewNick, PrintWriter output) {
        output.println(NewNick);
        System.out.println();
    }

    private synchronized void disconnection() {
        try {
            System.out.println("\n* Closing connection... *");
            try {
                Thread.currentThread().sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
            link.close();
        } catch (IOException ioEx) {
            System.out.println("Unable to disconnect!");
            System.exit(1);
        }
    }

    @Override
    public void run() {

        try {
            PrintWriter output = new PrintWriter(link.getOutputStream(), true);
            output.println(nickname);
            Scanner userEntry = new Scanner(System.in);

            System.out.println("***********Comenzi************");
            System.out.println("QUIT\nLIST\nMSG name message\nNICK newNick \nALL message");
            System.out.println("******************************\n");

            while (true) {
                String command = userEntry.nextLine();
                output.println(command);
                String ss[] = command.split(" ");
                if (ss[0].equals("NICK") == true) {
                    Nick(ss[1], output);
                }
                if (command.equals("QUIT") == true) {
                    break;
                }
            }
        } catch (IOException ioEx) {
        } finally {
            disconnection();
        }
    }
}

class Receiver extends Thread {

    Scanner input;
    Socket client;

    Receiver(Socket client) throws IOException {
        this.client = client;
        input = new Scanner(client.getInputStream());
    }

    private void List() {
        Integer nrClienti = Integer.parseInt(input.next());
        System.out.println("Utilizatori online:");
        for (int i = 0; i < nrClienti; i++) {
            String cc = input.next();
            System.out.println("# " + cc);
        }
    }

    private void All() {
        String c = input.nextLine();
        System.out.println(c);
    }

    private void Msg() {
        String c = input.nextLine();
        System.out.println(c);
    }

    private synchronized void Nick() {
        String r = input.nextLine();
        System.out.println(r);
    }

    private void checkingNick() {
        String check = input.nextLine();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(check);
    }

    private void disconnection() {
        try {
            client.close();
        } catch (IOException ioEx) {
            System.out.println("Unable to disconnect!");
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            checkingNick();
            while (true) {
                System.out.println();
                String command = input.nextLine();

                if (command.equals("LIST") == true) {
                    List();
                    continue;
                }
                if (command.equals("ALL") == true) {
                    All();
                    continue;
                }
                if (command.equals("MSG") == true) {
                    Msg();
                    continue;
                }
                if (command.equals("NICK") == true) {
                    Nick();
                    continue;
                }
                if (command.equals("QUIT") == true) {
                    break;
                }
                //System.out.println("**Comanda gresita!**");
            }
        } finally {
            disconnection();
        }
    }
}
