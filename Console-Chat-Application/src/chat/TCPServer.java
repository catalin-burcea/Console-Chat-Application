/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * @author Burcea Catalin
 */

public class TCPServer {
	public static void main(String[] args) throws IOException {
		Server s = new Server();
	}
}

class Server {

	private static ServerSocket servSock;
	private static final int PORT = 12000;
	static ArrayList<HandleClient> Clienti = new ArrayList<HandleClient>();

	Server() throws IOException {
		run();
	}

	public static void run() throws IOException {
		System.out.println("Deschidere port ....\n");
		try {
			servSock = new ServerSocket(PORT);
		} catch (IOException ioEx) {
			System.out.println("Imposibil sa ma conectez la port!");
			System.exit(1);
		}
		do {
			Socket client = servSock.accept();
			HandleClient HC = new HandleClient(client);
			HC.indexOfClient = Clienti.size();
			Clienti.add(HC);
			HC.start();

		} while (true);
	}
}

class HandleClient extends Thread {

	Socket client;
	String nickname = "";
	int indexOfClient;
	PrintWriter output;
	Scanner input;

	HandleClient(Socket client) throws SocketException {
		this.client = client;
	}

	private synchronized void List(String command) {
		output.println(command);
		output.println(Server.Clienti.size());
		for (int i = 0; i < Server.Clienti.size(); i++) {
			String nume = Server.Clienti.get(i).nickname;
			output.println(nume);
		}
	}

	private synchronized void All(String ss, String command) {
		for (int i = 0; i < Server.Clienti.size(); i++) {
			Server.Clienti.get(i).output.println(ss);
			if (Server.Clienti.get(i).nickname.equals(nickname) == false) {
				Server.Clienti.get(i).output.println(nickname + ": " + command.substring(3));
			} else {
				Server.Clienti.get(i).output.println("Mesaj trimis!");
			}
		}
	}

	private synchronized boolean exists(String nick) {
		for (int i = 0; i < Server.Clienti.size(); i++) {
			if (i != indexOfClient && Server.Clienti.get(i).nickname.equals(nick) == true) {
				return true;
			}
		}
		return false;
	}

	private synchronized boolean exists2(String nick) {
		for (int i = 0; i < Server.Clienti.size(); i++) {
			if (Server.Clienti.get(i).nickname.equals(nick) == true) {
				return true;
			}
		}
		return false;
	}

	private synchronized void Msg(String[] ss, String command) {
		String message = "";
		command = command.replaceFirst(" ", "#").replaceFirst(" ", "#");
		String[] s = command.split("#");
		for (int i = 0; i < Server.Clienti.size(); i++) {

			if (Server.Clienti.get(i).nickname.equals(ss[1]) == true) {
				Server.Clienti.get(i).output.println(ss[0]);
				Server.Clienti.get(i).output.println(nickname + ": " + s[2]);

			}
			if (Server.Clienti.get(i).nickname.equals(nickname) == true) {
				Server.Clienti.get(i).output.println(ss[0]);
				if (exists2(s[1]) == true) {
					Server.Clienti.get(i).output.println("Mesaj trimis!");
				} else {
					Server.Clienti.get(i).output.println("Utilizatorul " + s[1] + " nu exista!");
				}

			}
		}
	}

	private synchronized void Nick() {
		String numeNou = input.next();
		output.println("NICK");
		if (exists(numeNou) == true) {
			output.println("Nickname-ul exista deja si nu va fi schimbat!");
		} else {
			output.println("NIckname-ul a fost schimbat cu succes din " + nickname + " in " + numeNou);
			nickname = numeNou;
			Server.Clienti.get(indexOfClient).nickname = numeNou;
		}
	}

	private synchronized void checkingNickname(String nick) {
		int i = 0;
		if (exists(nick) == true) {
			output.println("Nickname-ul exista deja! Se va genera altul automat");
			while (exists(nick) == true) {
				nick = "name" + i;
				i++;
			}
		} else {
			output.println("Nickname valid! ");
		}
		nickname = nick;
	}

	private synchronized void disconnection(Socket link) {
		try {
			link.close();
			System.out.println("\n Utilizatorul cu numele " + nickname + " s-a deconectat! ");
			Server.Clienti.remove(indexOfClient);
			for (int i = indexOfClient; i < Server.Clienti.size(); i++) {
				Server.Clienti.get(i).indexOfClient--;
			}
		} catch (IOException ioEx) {
			System.out.println("Imposibil sa ma deconectez!");
			System.exit(1);
		}
	}

	@Override
	public void run() {
		Socket link = client;
		try {
			input = new Scanner(link.getInputStream());
			output = new PrintWriter(link.getOutputStream(), true);
			String nick = input.next();
			checkingNickname(nick);
			while (true) {
				String command;
				command = input.nextLine();
				String ss[] = command.split(" ");
				if (command.equals(ConsoleChatConstants.LIST_COMMAND) == true) {
					List(command);
				}

				if (ss[0].equals(ConsoleChatConstants.ALL_COMMAND) == true) {
					All(ss[0], command);
				}

				if (ss[0].equals(ConsoleChatConstants.MSG_COMMAND) == true) {
					Msg(ss, command);
				}

				if (ss[0].equals(ConsoleChatConstants.NICK_COMMAND) == true) {
					Nick();
				}

				if (command.equals(ConsoleChatConstants.EXIT_COMMAND) == true) {
					output.println(command);
					break;
				}
			}
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		} finally {
			disconnection(link);
		}
	}
}
