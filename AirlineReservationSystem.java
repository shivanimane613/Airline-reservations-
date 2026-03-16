package connectivity;

import java.sql.*;
import java.util.Scanner;

public class AirlineReservationSystem {

	static final String DB_NAME = "airline_db";
	static final String URL_WITHOUT_DB = "jdbc:mysql://localhost:3308/";
	static final String URL = "jdbc:mysql://localhost:3308/" + DB_NAME;
	static final String USER = "root";
	static final String PASSWORD = "root";

	static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {

		// Create DB and Tables automatically
		initializeDatabase();

		int choice;

		do {
			System.out.println("\n=================================");
			System.out.println("   WELCOME TO SKYLINE AIRLINES");
			System.out.println("=================================");
			System.out.println("1. View Flights");
			System.out.println("2. Book Ticket");
			System.out.println("3. View Booking");
			System.out.println("4. Cancel Ticket");
			System.out.println("5. Exit");
			System.out.print("Enter choice: ");

			choice = sc.nextInt();
			sc.nextLine();

			switch (choice) {
			case 1:
				viewFlights();
				break;
			case 2:
				bookTicket();
				break;
			case 3:
				viewBooking();
				break;
			case 4:
				cancelTicket();
				break;
			case 5:
				System.out.println("Thank you!");
				break;
			default:
				System.out.println("Invalid choice!");
			}

		} while (choice != 5);
	}

	// ✅ DATABASE + TABLE CREATION
	static void initializeDatabase() {
		try {
			// Connect without DB
			Connection con = DriverManager.getConnection(URL_WITHOUT_DB, USER, PASSWORD);
			Statement stmt = con.createStatement();

			// Create Database
			stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
			System.out.println("Database checked/created successfully.");

			con.close();

			// Connect to created DB
			Connection dbCon = DriverManager.getConnection(URL, USER, PASSWORD);
			Statement dbStmt = dbCon.createStatement();

			// Create Flights Table
			String createFlights = "CREATE TABLE IF NOT EXISTS flights ("
					+ "flight_no VARCHAR(10) PRIMARY KEY,"
					+ "source VARCHAR(50),"
					+ "destination VARCHAR(50),"
					+ "date VARCHAR(20),"
					+ "available_seats INT,"
					+ "price DOUBLE)";
			dbStmt.executeUpdate(createFlights);

			// Create Bookings Table
			String createBookings = "CREATE TABLE IF NOT EXISTS bookings ("
					+ "booking_id VARCHAR(20) PRIMARY KEY,"
					+ "passenger_name VARCHAR(100),"
					+ "age INT,"
					+ "gender VARCHAR(10),"
					+ "flight_no VARCHAR(10),"
					+ "seats_booked INT,"
					+ "total_amount DOUBLE)";
			dbStmt.executeUpdate(createBookings);

			// Insert sample flights if empty
			ResultSet rs = dbStmt.executeQuery("SELECT COUNT(*) FROM flights");
			rs.next();
			if (rs.getInt(1) == 0) {
				dbStmt.executeUpdate("INSERT INTO flights VALUES "
						+ "('AI101','Mumbai','Delhi','2026-03-01',100,5000),"
						+ "('AI102','Delhi','Bangalore','2026-03-05',80,4500),"
						+ "('AI103','Chennai','Mumbai','2026-03-10',120,5500)");
				System.out.println("Sample flights inserted.");
			}

			dbCon.close();

		} catch (Exception e) {
			System.out.println("Database Initialization Error: " + e.getMessage());
		}
	}

	// DB Connection
	static Connection getConnection() throws Exception {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	// View Flights
	static void viewFlights() {
		try (Connection con = getConnection()) {

			String query = "SELECT * FROM flights";
			PreparedStatement ps = con.prepareStatement(query);
			ResultSet rs = ps.executeQuery();

			System.out.println("\nAvailable Flights:");
			System.out.println("-------------------------------------------------------------");
			System.out.printf("%-8s %-10s %-12s %-12s %-6s %-6s\n",
					"Flight", "From", "To", "Date", "Seats", "Price");
			System.out.println("-------------------------------------------------------------");

			while (rs.next()) {
				System.out.printf("%-8s %-10s %-12s %-12s %-6d %-6.0f\n",
						rs.getString("flight_no"),
						rs.getString("source"),
						rs.getString("destination"),
						rs.getString("date"),
						rs.getInt("available_seats"),
						rs.getDouble("price"));
			}

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	// Book Ticket
	static void bookTicket() {
		try (Connection con = getConnection()) {

			System.out.print("Enter Flight Number: ");
			String flightNo = sc.nextLine();

			PreparedStatement ps = con.prepareStatement(
					"SELECT * FROM flights WHERE flight_no=?");
			ps.setString(1, flightNo);
			ResultSet rs = ps.executeQuery();

			if (!rs.next()) {
				System.out.println("Flight not found!");
				return;
			}

			int availableSeats = rs.getInt("available_seats");
			double price = rs.getDouble("price");

			System.out.print("Enter Passenger Name: ");
			String name = sc.nextLine();

			System.out.print("Enter Age: ");
			int age = sc.nextInt();
			sc.nextLine();

			System.out.print("Enter Gender: ");
			String gender = sc.nextLine();

			System.out.print("Enter Number of Seats: ");
			int seats = sc.nextInt();
			sc.nextLine();

			if (seats > availableSeats) {
				System.out.println("Only " + availableSeats + " seats available!");
				return;
			}

			double totalAmount = seats * price;
			String bookingId = "B" + System.currentTimeMillis();

			PreparedStatement ps2 = con.prepareStatement(
					"INSERT INTO bookings VALUES (?,?,?,?,?,?,?)");
			ps2.setString(1, bookingId);
			ps2.setString(2, name);
			ps2.setInt(3, age);
			ps2.setString(4, gender);
			ps2.setString(5, flightNo);
			ps2.setInt(6, seats);
			ps2.setDouble(7, totalAmount);
			ps2.executeUpdate();

			PreparedStatement ps3 = con.prepareStatement(
					"UPDATE flights SET available_seats=? WHERE flight_no=?");
			ps3.setInt(1, availableSeats - seats);
			ps3.setString(2, flightNo);
			ps3.executeUpdate();

			System.out.println("\nBOOKING CONFIRMED!");
			System.out.println("Booking ID: " + bookingId);
			System.out.println("Total Amount: " + totalAmount);

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	// View Booking
	static void viewBooking() {
		try (Connection con = getConnection()) {

			System.out.print("Enter Booking ID: ");
			String id = sc.nextLine();

			PreparedStatement ps = con.prepareStatement(
					"SELECT * FROM bookings WHERE booking_id=?");
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				System.out.println("\nBooking Details:");
				System.out.println("Passenger: " + rs.getString("passenger_name"));
				System.out.println("Flight No: " + rs.getString("flight_no"));
				System.out.println("Seats: " + rs.getInt("seats_booked"));
				System.out.println("Total Paid: " + rs.getDouble("total_amount"));
			} else {
				System.out.println("Invalid Booking ID!");
			}

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	// Cancel Ticket
	static void cancelTicket() {
		try (Connection con = getConnection()) {

			System.out.print("Enter Booking ID to cancel: ");
			String id = sc.nextLine();

			PreparedStatement ps = con.prepareStatement(
					"SELECT * FROM bookings WHERE booking_id=?");
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();

			if (!rs.next()) {
				System.out.println("Invalid Booking ID!");
				return;
			}

			String flightNo = rs.getString("flight_no");
			int seats = rs.getInt("seats_booked");

			PreparedStatement ps2 = con.prepareStatement(
					"DELETE FROM bookings WHERE booking_id=?");
			ps2.setString(1, id);
			ps2.executeUpdate();

			PreparedStatement ps3 = con.prepareStatement(
					"UPDATE flights SET available_seats = available_seats + ? WHERE flight_no=?");
			ps3.setInt(1, seats);
			ps3.setString(2, flightNo);
			ps3.executeUpdate();

			System.out.println("Booking cancelled successfully!");

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}