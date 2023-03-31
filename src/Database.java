import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Database {

	//Constants 
	public static final int PRICE_A_TO_H = 80;
	public static final int PRICE_I_TO_P = 60;
	public static final int PRICE_Q_TO_Z = 30;
	public static final int NUM_ROWS = 26;
	public static final int NUM_SEATS_PER_ROW = 20;
	
	//Connection variable
	Connection conn;
	
	//Statements
	private PreparedStatement getAgentStmt;
	private PreparedStatement registerAgentStmt;
	private PreparedStatement getAvailableSeatsStmt;
	private PreparedStatement getAvailableSeatsByPriceStmt;
	private PreparedStatement reserveSeatStmt;
	private PreparedStatement getReservedSeatsStmt;
	private PreparedStatement insertSeatStmt;
	private PreparedStatement checkSeatStmt;
	
	// Map to store the logged-in agents
	public Map<String, BulletinBoardServer.ClientThread> loggedInAgents = new ConcurrentHashMap<>();
	
	public void createDatabase() throws SQLException {
	// creating database connection
			conn = DriverManager.getConnection("jdbc:sqlite:bulletinboard.db");
			Statement stmt = conn.createStatement();
			String agentsTable = "CREATE TABLE IF NOT EXISTS agents (username TEXT PRIMARY KEY, name TEXT NOT NULL, phone TEXT NOT NULL, city TEXT NOT NULL)";
			String seatsTable = "CREATE TABLE IF NOT EXISTS seats (row TEXT NOT NULL, seat INTEGER NOT NULL, price INTEGER NOT NULL, reserved BOOLEAN NOT NULL DEFAULT 0, customer_name TEXT, customer_phone TEXT, PRIMARY KEY (row, seat))";
			System.out.println("AGENT TABLE ROWS CHANGED: " + stmt.executeUpdate(agentsTable));
			System.out.println("SEAT TABLE ROWS CHANGED: " + stmt.executeUpdate(seatsTable));
			getAgentStmt = conn.prepareStatement("SELECT * FROM agents WHERE username = ?");
			registerAgentStmt = conn
					.prepareStatement("INSERT INTO agents (username, name, phone, city) VALUES (?, ?, ?, ?)");
			getAvailableSeatsStmt = conn.prepareStatement("SELECT row, seat, price FROM seats WHERE reserved = 0");
			getAvailableSeatsByPriceStmt = conn
					.prepareStatement("SELECT row, seat, price FROM seats WHERE reserved = 0 AND price = ?");
			reserveSeatStmt = conn.prepareStatement(
					"UPDATE seats SET reserved = 1, customer_name = ?, customer_phone = ? WHERE row = ? AND seat = ?");
			getReservedSeatsStmt = conn
					.prepareStatement("SELECT row, seat, customer_name, customer_phone FROM seats WHERE reserved = 1");
			checkSeatStmt = conn
					.prepareStatement("SELECT * FROM seats WHERE row = ? AND seat = ? AND customer_name IS NOT NULL");

			ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM seats");
			if (rs.next()) {
				int count = rs.getInt(1);
				if (count == 0) {
					// Table is empty, insert rows
					insertSeatStmt = conn.prepareStatement("INSERT INTO seats (row, seat, price) VALUES (?, ?, ?)");
					// Set values and execute insertSeatStmt
					// Loop through the rows and seats and add them to the seats table
					for (int i = 0; i < NUM_ROWS; i++) {
						char row = (char) ('A' + i);
						int price;
						if (row >= 'A' && row <= 'H') {
							price = PRICE_A_TO_H;
						} else if (row >= 'I' && row <= 'P') {
							price = PRICE_I_TO_P;
						} else {
							price = PRICE_Q_TO_Z;
						}
						for (int j = 0; j < 20; j++) {
							insertSeatStmt.setString(1, "" + row);
							insertSeatStmt.setInt(2, j + 1);
							insertSeatStmt.setInt(3, price);
							insertSeatStmt.executeUpdate();
						}
					}
				}
			}
		}
	
	// Method for adding an agent to the database
	public void registerAgent(String username, String name, String phone, String city) {
		try {
			registerAgentStmt.setString(1, username);
			registerAgentStmt.setString(2, name);
			registerAgentStmt.setString(3, phone);
			registerAgentStmt.setString(4, city);
			registerAgentStmt.executeUpdate();
		} catch (SQLException e) {
			if (e.getErrorCode() == 19) {
				System.out.println("An agent with the same username already exists.");
			} else {
				e.printStackTrace();
			}
		}
	}
	
	//Method for checking if a seat is available
	boolean isSeatAvailable(String row, int seat) throws SQLException {
		// Set the values for the placeholders in the Prepared Statement
		checkSeatStmt.setString(1, row);
		checkSeatStmt.setInt(2, seat);

		// Execute the Prepared Statement and get the ResultSet
		ResultSet rs = checkSeatStmt.executeQuery();

		// Check if there are any rows in the ResultSet
		if (rs.next()) {
			// If there are rows, the seat is reserved
			return false;
		} else {
			// If there are no rows, the seat is not reserved
			return true;
		}
	}

	// Method for retrieving an agent by username
	Agent getAgent(String username) throws SQLException {
		getAgentStmt.setString(1, username);
		ResultSet rs = getAgentStmt.executeQuery();
		if (rs.next()) {
			String name = rs.getString("name");
			String phone = rs.getString("phone");
			String city = rs.getString("city");
			return new Agent(username, name, phone, city);
		} else {
			return null;
		}
	}

	// Method that gets the price of the seat depending on the row
	public int getSeatPrice(String row) {
		if (row.charAt(0) >= 'A' && row.charAt(0) <= 'H') {
			return 80;
		} else if (row.charAt(0) >= 'I' && row.charAt(0) <= 'P') {
			return 60;
		} else {
			return 30;
		}
	}

	// Method that gets the list of available seats
	public List<Seat> getAvailableSeats() throws SQLException {
		List<Seat> seats = new ArrayList<>();
		ResultSet rs = getAvailableSeatsStmt.executeQuery();
		while (rs.next()) {
			Seat seat = new Seat();
			seat.row = rs.getString("row");
			seat.seat = rs.getInt("seat");
			seat.price = rs.getInt("price");
			seats.add(seat);
		}
		return seats;
	}

	// Method that filters the available seats based on price class
	List<Seat> getAvailableSeatsByPrice(int selectedClass) throws SQLException {
		List<Seat> seats = new ArrayList<>();

		if (selectedClass == 1) {
			getAvailableSeatsByPriceStmt.setInt(1, 80);
		} else if (selectedClass == 2) {
			getAvailableSeatsByPriceStmt.setInt(1, 60);
		} else if (selectedClass == 2) {
			getAvailableSeatsByPriceStmt.setInt(1, 30);
		}

		ResultSet rs = getAvailableSeatsByPriceStmt.executeQuery();
		while (rs.next()) {
			Seat seat = new Seat();
			seat.row = rs.getString("row");
			seat.seat = rs.getInt("seat");
			seat.price = rs.getInt("price");
			seats.add(seat);
		}
		return seats;
	}

	// Method for getting the list of reserved seats
	List<Seat> getReservedSeats() throws SQLException {
		List<Seat> seats = new ArrayList<>();
		ResultSet rs = getReservedSeatsStmt.executeQuery();
		while (rs.next()) {
			String row = rs.getString("row");
			int seat = rs.getInt("seat");
			int price = getSeatPrice(row);
			String customerName = rs.getString("customer_name");
			String customerPhone = rs.getString("customer_phone");
			seats.add(new Seat(row, seat, price, customerName, customerPhone));

		}
		return seats;
	}

	// Method to handle the reservation of a seat by an agent
	boolean reserveSeat(String username, String row, int seat, String customerName, String customerPhone)
			throws SQLException {
		// Check if the agent is logged in
		if (loggedInAgents.containsKey(username)) {
			// If the agent is logged in, update the seat in the database and return a success message
			reserveSeatStmt.setString(1, customerName);
			reserveSeatStmt.setString(2, customerPhone);
			reserveSeatStmt.setString(3, row);
			reserveSeatStmt.setInt(4, seat);
			reserveSeatStmt.executeUpdate();
			return true;
		} else {
			// If the agent is not logged in, return false
			return false;
		}
	}

	// Method to handle the login process for agents
	boolean login(String username, BulletinBoardServer.ClientThread clientThread) throws SQLException {
		// Check if the agent is registered in the database
		getAgentStmt.setString(1, username);
		ResultSet rs = getAgentStmt.executeQuery();
		if (rs.next()) {
			// If the agent is registered, add them to the loggedInAgents map
			loggedInAgents.put(username, clientThread);
			return true;
		} else {
			// If the agent is not registered, return false
			return false;
		}
	}

	// Method to handle logout or disconnection of an agent
	public void logout(String username) {
		// Remove the agent from the loggedInAgents map
		System.out.println("LOGGING OUT " + username);
		loggedInAgents.remove(username);
	}
}