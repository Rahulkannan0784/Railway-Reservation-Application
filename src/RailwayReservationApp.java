import java.sql.*;
import java.util.*;

public class RailwayReservationApp {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/railway_reservation";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Rahulkannan.1";

    public static void main(String[] args) {
        Train train = new Train();
        train.addCoach(new Coach("AC"));
        train.addCoach(new Coach("Non AC"));
        train.addCoach(new Coach("Seater"));
        System.out.println("Welcome to IRCTC");

        Scanner scanner = new Scanner(System.in);

        int choice;
        do {
            System.out.println("1. Ticket Booking");
            System.out.println("2. Ticket Cancellation");
            System.out.println("3. Status Checking");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    bookTicket(train, scanner);
                    break;
                case 2:
                    cancelTicket(train, scanner);
                    break;
                case 3:
                    checkStatus(train);
                    break;
                case 4:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 4);
        
        scanner.close(); // Close the scanner
    }

    private static void bookTicket(Train train, Scanner scanner) {
        try (Connection connection = getConnection()) {
            System.out.print("Enter source station: ");
            String sourceStation = scanner.next();
            System.out.print("Enter destination station: ");
            String destinationStation = scanner.next();
            System.out.println("Select coach (1. AC, 2. Non AC, 3. Seater): ");
            int coachChoice = scanner.nextInt();
            Coach coach = train.getCoachByType(coachChoice);
            if (coach != null) {
                System.out.print("Enter number of tickets (maximum 6): ");
                int numTickets = scanner.nextInt();
                if (numTickets > 6) {
                    System.out.println("Maximum 6 tickets can be booked at a time.");
                    return;
                }

                String query = "INSERT INTO tickets (source_station, destination_station, coach_type, seat_number, passenger_name, passenger_age, passenger_gender) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    List<Seat> bookedSeats = new ArrayList<>();
                    for (int i = 0; i < numTickets; i++) {
                        System.out.print("Passenger " + (i + 1) + " Name: ");
                        String name = scanner.next();
                        System.out.print("Age: ");
                        int age = scanner.nextInt();
                        System.out.print("Gender (M/F): ");
                        String gender = scanner.next();

                        Seat seat = coach.bookSeat(name, age, gender);
                        if (seat != null) {
                            bookedSeats.add(seat);
                            preparedStatement.setString(1, sourceStation);
                            preparedStatement.setString(2, destinationStation);
                            preparedStatement.setString(3, coach.getType());
                            preparedStatement.setInt(4, seat.getSeatNumber());
                            preparedStatement.setString(5, name);
                            preparedStatement.setInt(6, age);
                            preparedStatement.setString(7, gender);
                            preparedStatement.executeUpdate();
                        } else {
                            System.out.println("Sorry, no seats available.");
                            break;
                        }
                    }

                    if (!bookedSeats.isEmpty()) {
                        System.out.println("Tickets booked successfully. Seat numbers:");
                        for (Seat seat : bookedSeats) {
                            System.out.print(seat.getSeatNumber() + " ");
                        }
                        System.out.println();
                    }
                }
            } else {
                System.out.println("Invalid coach choice.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void cancelTicket(Train train, Scanner scanner) {
        try (Connection connection = getConnection()) {
            System.out.println("Enter coach type (1. AC, 2. Non AC, 3. Seater): ");
            int coachChoice = scanner.nextInt();
            Coach coach = train.getCoachByType(coachChoice);
            if (coach != null) {
                System.out.println("Enter seat number to cancel: ");
                int seatNumber = scanner.nextInt();
                if (coach.cancelSeat(seatNumber)) {
                    // Update database to remove the cancelled ticket
                    String query = "DELETE FROM tickets WHERE seat_number = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setInt(1, seatNumber);
                        preparedStatement.executeUpdate();
                    }
                    System.out.println("Ticket cancelled successfully.");
                } else {
                    System.out.println("Invalid seat number or seat not booked.");
                }
            } else {
                System.out.println("Invalid coach choice.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    private static void checkStatus(Train train) {
        for (Coach coach : train.getCoaches()) {
            System.out.println("Coach: " + coach.getType());
            System.out.println("Available seats: " + coach.getAvailableSeats());
            System.out.println("Waiting list: " + coach.getWaitingListSize());
            System.out.println("-----------------------------");
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

}
// ... (other classes remain unchanged)



class Train {
    private List<Coach> coaches;

    public Train() {
        this.coaches = new ArrayList<>();
    }

    public void addCoach(Coach coach) {
        coaches.add(coach);
    }

    public Coach getCoachByType(int type) {
        for (Coach coach : coaches) {
            if (coach.getTypeCode() == type) {
                return coach;
            }
        }
        return null;
    }

    public List<Coach> getCoaches() {
        return coaches;
    }
}

class Coach {
    private String type;
    private int typeCode;
    private List<Seat> seats;
    private Queue<Integer> waitingList;

    public Coach(String type) {
        this.type = type;
        this.seats = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            seats.add(new Seat(i));
        }
        this.waitingList = new LinkedList<>();
        this.typeCode = getTypeCode(type);
    }

    private int getTypeCode(String type) {
        switch (type) {
            case "AC":
                return 1;
            case "Non AC":
                return 2;
            case "Seater":
                return 3;
            default:
                return -1;
        }
    }

    public String getType() {
        return type;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public Seat bookSeat(String name, int age, String gender) {
        for (Seat seat : seats) {
            if (!seat.isBooked()) {
                seat.book(name, age, gender);
                return seat;
            }
        }
        return null;
    }

    public boolean cancelSeat(int seatNumber) {
        for (Seat seat : seats) {
            if (seat.getSeatNumber() == seatNumber && seat.isBooked()) {
                seat.cancel();
                if (!waitingList.isEmpty()) {
                    seats.get(seatNumber - 1).book();
                    waitingList.poll();
                }
                return true;
            }
        }
        return false;
    }

    public int getAvailableSeats() {
        int count = 0;
        for (Seat seat : seats) {
            if (!seat.isBooked()) {
                count++;
            }
        }
        return count;
    }

    public int getWaitingListSize() {
        return waitingList.size();
    }
}

class Seat {
    private int seatNumber;
    private boolean booked;
    private String passengerName;
    private int passengerAge;
    private String passengerGender;

    public Seat(int seatNumber) {
        this.seatNumber = seatNumber;
        this.booked = false;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public boolean isBooked() {
        return booked;
    }

    public void book(String name, int age, String gender) {
        this.booked = true;
        this.passengerName = name;
        this.passengerAge = age;
        this.passengerGender = gender;
    }

    public void cancel() {
        this.booked = false;
        this.passengerName = null;
        this.passengerAge = 0;
        this.passengerGender = null;
    }

    public void book() {
    }
}
