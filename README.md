# Railway Reservation Application

This repository contains the source code for a Railway Reservation Application developed using Java and MySQL. The application provides the following features:

## Features

1. **Ticket Booking**: Allows users to book tickets by selecting a source station, destination station, and coach type (AC, Non-AC, or Seater). Users can book up to 6 tickets at a time.

2. **Ticket Cancellation**: Allows users to cancel their booked tickets by specifying the coach type and seat number.

3. **Status Checking**: Provides information about the availability of seats and the waiting list for each coach.

## SQL Schema

The application uses a MySQL database to store ticket information. Here's an example SQL query for creating the `tickets` table:

```sql
CREATE TABLE tickets (
    id INT PRIMARY KEY AUTO_INCREMENT,
    source_station VARCHAR(255),
    destination_station VARCHAR(255),
    coach_type VARCHAR(50),
    seat_number INT,
    passenger_name VARCHAR(255),
    passenger_age INT,
    passenger_gender VARCHAR(1)
); 
```

## Database Connection

To connect to the MySQL database, you will need to include the MySQL Connector/J JAR file in your project. You can download the JAR file from the [MySQL Connector](https://jar-download.com/artifacts/mysql/mysql-connector-java/8.0.11/source-code). Once downloaded, add the JAR file to your project's classpath.


## Usage

1. **Setup**: Ensure you have MySQL installed and running. Create a database named `railway_reservation` and execute the SQL query provided above to create the `tickets` table.

2. **Running the Application**: Compile and run the `RailwayReservationApp.java` file to start the application. Follow the on-screen prompts to interact with the application.

3. **Contributing**: Feel free to contribute to this project by forking the repository, making your changes, and submitting a pull request.
