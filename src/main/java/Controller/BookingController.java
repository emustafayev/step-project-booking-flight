package Controller;

import dao.Database;
import entity.Booking;
import entity.Flight;
import entity.Passenger;
import entity.User;
import exceptions.OutOfBound;
import service.BookingService;
import service.FlightService;
import ui.Console;
import util.NumberCheck;
import util.Parser;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookingController {
    private FlightController flightController;

    private Console console;
    private Database db;
    private BookingService bookingService;
    private FlightService flightService;
    private String passengerName, passengerSurname;
    private List<Passenger> passengers;
    private List<Flight> flights;

    public BookingController(Console console, Database db)
    {
        this.console = console;
        this.db = db;
        flightController = new FlightController(this.console, db);
        bookingService = new BookingService(db);
        flightService = new FlightService(db);
    }
    public void bookMe(User user) {
        String destination;
        int seats;
        String date;
        console.print("Please enter the destination:");
        destination = console.readLine();
        console.print("Please enter the number of seats:");
        seats = NumberCheck.getNumber(console.readLine()); //fixme parser change
        console.print("Please enter the date:(year-day-month ex:2020-15-03");
        date = console.readLine();
        flights = flightService.getFlightsByParametr(destination, seats, date);
        flightController.showSearchedFlight(flights);
        if (flights.size() > 0) {
            console.print("Enter flight index: ");
            int flightId = NumberCheck.getNumber(console.readLine());
            passengers = new ArrayList<>();
            for (int i = 1; i <= seats; i++) {
                console.print(String.format("Name of the %d. passenger:", i));
                passengerName = console.readLine();
                console.print(String.format("Surname of the %d. passenger:", i));
                passengerSurname = console.readLine();
                passengers.add(new Passenger(passengerName, passengerSurname));
            }
            Flight flight = flights.stream().filter(f -> f.ID == flightId).collect(Collectors.toList()).get(0);
            Booking booking = new Booking(flight, user, passengers, LocalDate.now());
            if (bookingService.bookAFlight(booking)) {
                console.print("Succesfully booked");
                console.print(booking.toString());
            }

        }
    }

    private int getFlightId() {
        try {
            int id = NumberCheck.getNumber(console.readLine());
            if (id > flights.get(flights.size() - 1).ID) {
                throw new OutOfBound("Entered id is not valid");
            }
            return id;
        }catch (OutOfBound ex) {
            console.print(String.format("%s\nEnter again", ex.getMessage()));
            return getFlightId();
        }
    }

    public void myFlights(User currentUser){
        console.print("======My flights======");
        if(!bookingService.getAllBookings(currentUser.username).isEmpty()){
            bookingService.getAllBookings(currentUser.username)
                    .forEach(System.out::println);
        }else {
            console.print("You did not book a flight yet!");
        }

    }

    public void cancelMyFlight(User currentUser) {
        myFlights(currentUser);
        console.print("Choose booking ID to cancel!");
        int bookID = NumberCheck.getNumber(console.readLine());
        if (bookingService.cancelBooking(bookID)) {
            console.print("Booking canceled successfully!");
        } else {
            console.print("Can not cancel specified booking");
        }
    }
}
