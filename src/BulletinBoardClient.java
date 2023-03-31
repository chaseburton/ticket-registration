import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BulletinBoardClient {
	
	public static void main(String[] args) throws IOException {
		// Standard regexes for validating user input
		String optionRegex = "^[0-4]$";
		String rowRegex = "^[A-Z]$";
		String seatRegex = "^(1[0-9]|20|[1-9])$";
		String classRegex = "^[1-3]$";
		String phoneRegex = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
		String usernameRegex = "^[a-zA-Z0-9]+$";
		String nameRegex = "^[a-zA-Z0-9 ]+$";

		boolean loginStatus = false;
		String userId = null;

		while (true) {
			// Connect to the server
			Socket socket = new Socket("localhost", 8989);

			// Create a BufferedReader to read from the server
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Create a PrintWriter to write to the server
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			// Create a BufferedReader to read from the user
			BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

			// Options Menu
			if (userId != null) {
				System.out.println("**************************************************");
				System.out.println("Agent: " + userId);
			}
			
			optionMenu(loginStatus);
	
			// Read a line from the user
			String line = readOption(userIn, optionRegex, "OPTION: ", "Sorry, that's not a valid option. Please enter a number between 0-4.");
			
			// Loop until the user enters the exit command
			// Check to see if the user has logged in or not, offer different options to guests
			if (loginStatus == true) {
				// Check if the user entered the exit command
				// LOGOUT
				if (line.equals("0")) {
					if (userId != null) {
						out.println("DISCONNECT " + userId);
						loginStatus = false;
					}
					// Closing all connections
					userIn.close();
					out.close();
					in.close();
					socket.close();
					// Ending the client application
					break;
				}
				// SHOW AVAILABLE SEATS
				else if (line.equals("1")) {
					System.out.println("GETTING AVAILABLE SEATS...");
					out.println("GET_AVAILABLE_SEATS ");
				}
				// RESERVE A SEAT
				else if (line.equals("2")) {					
					System.out.println("RESERVING A SEAT...");

					String row = readOption(userIn, rowRegex, "ROW: ", "Sorry, that's not a valid option. Please enter a number between a row between A-Z.");
					
					String seat = readOption(userIn, seatRegex, "SEAT: ", "Sorry, that's not a valid option. Please enter a number between a row between 1-20.");
					
					String customerName = readOption(userIn, nameRegex, "CUSTOMER NAME: ", "Sorry, that's not a valid option. Please enter a valid name for the customer.");
					
					String customerPhone = readOption(userIn, phoneRegex, "CUSTOMER PHONE: ", "Sorry, that's not a valid option. Please enter a valid phone number.");
					
					out.println("RESERVE_SEAT " + userId + " " + row + " " + seat + " " + customerName + " "
							+ customerPhone);
				}
				// SHOW RESERVED SEATS
				else if (line.equals("3")) {
					System.out.println("GETTING RESERVED SEATS...");
					out.println("GET_RESERVED_SEATS " + userId);
				}
				// SHOW AVAILABLE SEATS BY PRICE
				else if (line.equals("4")) {
					System.out.println("GETTING AVAILABLE SEATS BY PRICE...");
					System.out.println("ROW: A-H = 1");
					System.out.println("ROW: I-P = 2");
					System.out.println("ROW: Q-Z = 3");
					String selectedClass = readOption(userIn, classRegex, "OPTION: ", "Sorry, that's not a valid option. Please enter a number between 1-3.");
					out.println("GET_AVAILABLE_SEATS_BY_PRICE " + selectedClass);
				}
			//Options displayed if the user is not logged in
			} else {
				// Check if the user entered the exit command
				if (line.equals("0")) {
					out.println("EXIT ");
					out.flush();
					// Closing all connections
					userIn.close();
					out.close();
					in.close();
					socket.close();
					// Ending the client application
					break;
				}
				// REGISTRATION PROCESS
				else if (line.equals("1")) {
					System.out.println("REGISTER...");

					// Getting user input
					String username = readOption(userIn, usernameRegex, "USERNAME: ", "Sorry, that's not a valid username. Please try again without using any special characters.");
					String name = readOption(userIn, nameRegex, "NAME: ", "Sorry, that's not a valid username. Please try again without using any special characters.");
					String phone = readOption(userIn, phoneRegex, "PHONE: ", "Sorry, that's not a valid option. Please enter a valid phone number.");
					String city = readOption(userIn, nameRegex, "CITY: ", "Sorry, that's not a valid city. Please try again without using any special characters.");
					
					// Sending Registration command to server
					out.println("REGISTER " + username + " " + name + " " + phone + " " + city);
					// Storing username
					userId = username;
				}
				// LOGIN PROCESS
				else if (line.equals("2")) {
					System.out.println("LOGGING IN...");
					
					String username = readOption(userIn, usernameRegex, "USERNAME: ", "Sorry, that's not a valid username. Please try again without using any special characters.");
					
					// Sending Login command to server
					out.println("LOGIN " + username);
					
					String response = in.readLine();
					System.out.println(response);
					
					// If login process fails, show appropriate message
					if(response.equals("ERROR_NOT_EXIST")) {
						System.out.println("LOGIN FAILED! User does not exist!");
					}
					
					else if (response.equals("ERROR_ALREADY_LOGGED_IN")) {
						System.out.println("LOGIN FAILED! User already logged-in!");
					}
					else {
						userId = username;
						loginStatus = true;
					}
				}
				// SHOW AVAILABLE SEATS
				else if (line.equals("3")) {
					System.out.println("GETTING AVAILABLE SEATS...");
					out.println("GET_AVAILABLE_SEATS ");
				}
				// SHOW AVAILABLE SEATS BY PRICE
				else if (line.equals("4")) {
					System.out.println("GETTING AVAILABLE SEATS BY PRICE...");
					System.out.println("ROW: A-H = 1");
					System.out.println("ROW: I-P = 2");
					System.out.println("ROW: Q-Z = 3");
					String selectedClass = readOption(userIn, classRegex, "OPTION: ", "Sorry, that's not a valid option. Please enter a number between 1-3.");
					out.println("GET_AVAILABLE_SEATS_BY_PRICE " + selectedClass);
				}
			}

			// Read all lines from the server
			String response;
			while ((response = in.readLine()) != null) {
				// Print the response from the server
				System.out.println(response);
			}
		}
	}
	
	// Regex method
	private static String readOption(BufferedReader reader, String regex, String printStatement, String errorStatement) throws IOException {
		while (true) {
			System.out.print(printStatement);
			String line = reader.readLine();

			if (line.matches(regex)) {
				// The user input is valid
				return line;
			} else {
				// The user input is invalid
				System.out.println(errorStatement);
			}
		}
	}

	// Options Menu
	public static void optionMenu(boolean loginStatus) {
		if (loginStatus == true) {
			System.out.println("Please select an action: ");
			System.out.println("0 - EXIT");
			System.out.println("1 - AVAILABLE SEATS");
			System.out.println("2 - RESERVE A SEAT");
			System.out.println("3 - SHOW RESERVED SEATS");
			System.out.println("4 - AVAILABLE SEATS BY PRICE");
			System.out.println("************************************************** \n");
		} else {
			System.out.println("**************************************************");
			System.out.println("Welcome to TicketFinder! Please select an action: ");
			System.out.println("OPTION: 0 = EXIT");
			System.out.println("OPTION: 1 = REGISTRATION");
			System.out.println("OPTION: 2 = LOGIN");
			System.out.println("OPTION: 3 = SEE AVAILABLE SEATS");
			System.out.println("OPTION: 4 = SEE AVAILABLE SEATS BY PRICE");
			System.out.println("************************************************** \n");
		}
	}
	
}