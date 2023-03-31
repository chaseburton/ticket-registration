import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class BulletinBoardServer {
	// Declaring database object
	public Database myDatabase;
	// Declaring map to represent loggedInAgents in Database class
	public Map<String, BulletinBoardServer.ClientThread> loggedInAgentsMap;
	int portNumber = 8989;
	ServerSocket serverSocket;

	public BulletinBoardServer() throws SQLException {
	      // Initalizing database
	      myDatabase = new Database();
	      myDatabase.createDatabase();
	      loggedInAgentsMap = myDatabase.loggedInAgents;
	      System.out.println("BulletinBoardServer running...");
	   }

	public static void main(String[] args) throws Exception {
		BulletinBoardServer server = new BulletinBoardServer();
		server.start();
	}

	public void start() throws Exception {
		try {
			serverSocket = new ServerSocket(portNumber);
			
			while (true) {
				Socket socket = serverSocket.accept();
				new ClientThread(socket).start();
				
			}
		} catch (Exception e) {
			System.out.println("Server running at port: " + portNumber);
		}
	}

	class ClientThread extends Thread {
		private Socket socket;

		public ClientThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				// Create input and output streams for the socket
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

				Agent agent;
				String line = in.readLine();
				String[] tokens = line.split(" ");
				String action = tokens[0];

				// Logging the requested action
				System.out.println("REQUESTED ACTION: " + action + "\n");

				// REGISTRATION
				if (action.equals("REGISTER")) {
					System.out.println("REGISTERING");
					// Register an agent
					String username = tokens[1];
					String name = tokens[2];
					String phone = tokens[3];
					String city = tokens[4];
					myDatabase.registerAgent(username, name, phone, city);

					out.println("REGISTRATION SUCCESSFUL, PLEASE LOG IN");
					out.flush();

				}
				// LOGIN
				else if (action.equals("LOGIN")) {
					System.out.println("LOGIN");
					String username = tokens[1];
					// Login an agent
					agent = myDatabase.getAgent(username);
					
					if (agent != null) {
						myDatabase.login(username, this);
						out.println("AGENT " + username + " SUCCESSFULLY LOGGED-IN!");
						out.flush();
					} else {
						// If the agent is not found throw error
						out.println("ERROR_NOT_EXIST");
						out.flush();
					}
					
				}
				// SHOW AVAILABLE SEATS
				else if (action.equals("GET_AVAILABLE_SEATS")) {
					System.out.println("GET AVAILABLE SEATS");
					// Get the list of available seats
					List<Seat> seats = myDatabase.getAvailableSeats();

					// Iterate through each element in the list displaying the attributes row, seat, and price
					for (int i = 0; i < seats.size(); i++) {
						Seat element = seats.get(i);
						out.println(element.row + " " + element.seat + " " + element.price + "$");
						out.flush();
					}
					// Printing the number of unreserved seats
					out.println("AVAILABLE SEATS: " + seats.size());
					out.flush();

				}
				// SHOW AVAILABLE SEATS BY PRICE
				else if (action.equals("GET_AVAILABLE_SEATS_BY_PRICE")) {
					System.out.println("GET AVAILABLE SEATS BY PRICE");
					// Get the list of available seats by price class
					int selectedClass = Integer.parseInt(tokens[1]);
					List<Seat> seats = myDatabase.getAvailableSeatsByPrice(selectedClass);
					for (Seat seat : seats) {
						out.println(seat.row + " " + seat.seat + " " + seat.price + "$");
						out.flush();
					}
					// Printing the number of unreserved seats in the specified class
					out.println("AVAILABLE SEATS: " + seats.size());
					out.flush();

				}
				// RESERVE SEAT
				else if (action.equals("RESERVE_SEAT")) {
					System.out.println("RESERVE SEAT");
					// Reserve a seat for a customer
					String username = tokens[1];
					String row = tokens[2];
					String seat = tokens[3];
					int seatInt = Integer.parseInt(seat);
					String customerName = tokens[4];
					String customerPhone = tokens[5];
					// Checking seat availability
					boolean seatStatus = myDatabase.isSeatAvailable(row, seatInt);
					System.out.println("Status: " + seatStatus);

					if (username == null) {
						out.println("USER ERROR");
						out.flush();
					}

					if (seatStatus) {
						myDatabase.reserveSeat(username, row, seatInt, customerName, customerPhone);
						out.println("SEAT: " + row + seatInt + " RESERVED FOR " + customerName);
						out.flush();
					} else if (!seatStatus) {
						out.println("SEAT IS ALREADY RESERVED, PLEASE CHOOSE ANOTHER!");
						out.flush();
					}

				}
				// SHOW RESERVED SEATS
				else if (action.equals("GET_RESERVED_SEATS")) {
					System.out.println("GET RESERVED SEATS");
					String username = tokens[1];
					// Get the list of reserved seats
					if (username == null) {
						out.println("ERROR: USER NOT SPECIFIED!");
						out.flush();
					} else {
						List<Seat> seats = myDatabase.getReservedSeats();
						out.println("NUMBER OF RESERVED SEATS: " + seats.size());
						for (Seat seat : seats) {
							out.println(
									seat.row + " " + seat.seat + " " + seat.customerName + " " + seat.customerPhone);
						}
						out.flush();
					}

				}
				// DISCONNECT
				else if (action.equals("DISCONNECT")) {
					// Logging out the user
					String username = tokens[1];
					myDatabase.logout(username);
					out.println(username + " DISCONNECTED!");
					System.out.println("USER: " + username + " HAS DISCONNECTED!");
				} else if (action.equals("EXIT")) { 
					System.out.println("GUEST USER DISCONNECTED");

				// ERROR SELECTING ACTION
				} else {
					out.println("ERROR SELECTING ACTION");
					out.flush();
				}
			} catch (IOException e) {
				System.out.println("ERROR RUNNING SERVER: " + e);
			} catch (SQLException e) {
				System.out.println("DATABASE ERROR: " + e);
			} finally {
				try {
					System.out.println("***CLOSING CONNECTION***");
					socket.close();
				} catch (IOException e) {
					System.out.println("ERROR CLOSING CONNECTION: " + e);
				}
			}
		}
	}
}