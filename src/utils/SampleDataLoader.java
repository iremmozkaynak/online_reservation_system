package utils;

import factory.BusTripFactory;
import factory.FlightTripFactory;
import factory.TripFactoryManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import models.Trip;
import repository.TripRepository;
import service.TripService;

public class SampleDataLoader {

    public static void loadSampleTripData() {
        TripRepository tripRepository = TripRepository.getInstance();
        TripService tripService = new TripService(tripRepository); 
        TripFactoryManager factoryManager = new TripFactoryManager();

        if (tripRepository.findAll().isEmpty()) {
            try {
                factoryManager.registerFactory("Bus", new BusTripFactory());
                factoryManager.registerFactory("Flight", new FlightTripFactory());
                Trip busTrip1 = factoryManager.getFactory("Bus").createTrip(
                        "BT001", "Istanbul", "Ankara",
                        LocalDateTime.of(2025, 6, 15, 8, 0),
                        LocalDateTime.of(2025, 6, 15, 14, 30),
                        45.0, 29, "Metro Turizm", "6h 30m", "AC, WiFi, TV", "34-MT-001");
                tripService.addTrip(busTrip1);

                Trip busTrip2 = factoryManager.getFactory("Bus").createTrip(
                        "BT002", "Istanbul", "Izmir",
                        LocalDateTime.of(2025, 6, 15, 10, 15),
                        LocalDateTime.of(2025, 6, 15, 16, 45),
                        50.0, 29, "Varan Turizm", "6h 30m", "AC, Refreshment", "34-VR-002");
                tripService.addTrip(busTrip2);
                
                Trip busTrip3 = factoryManager.getFactory("Bus").createTrip(
                        "BT003", "Istanbul", "Ankara",
                        LocalDateTime.of(2025, 6, 15, 14, 0),
                        LocalDateTime.of(2025, 6, 15, 20, 30),
                        45.0, 29, "Kamil Koç", "6h 30m", "AC, WiFi", "34-KK-003");
                tripService.addTrip(busTrip3);
                
                Trip busTrip4 = factoryManager.getFactory("Bus").createTrip(
                        "BT004", "Istanbul", "Ankara",
                        LocalDateTime.of(2025, 6, 15, 18, 30),
                        LocalDateTime.of(2025, 6, 16, 1, 0),
                        50.0, 29, "Pamukkale Turizm", "6h 30m", "AC, TV, Meal", "34-PK-004");
                tripService.addTrip(busTrip4);
                
                Trip busTrip5 = factoryManager.getFactory("Bus").createTrip(
                        "BT005", "Istanbul", "Ankara",
                        LocalDateTime.of(2025, 6, 15, 22, 0),
                        LocalDateTime.of(2025, 6, 16, 4, 30),
                        45.0, 29, "Ulusoy", "6h 30m", "AC, WiFi", "34-UL-005");
                tripService.addTrip(busTrip5);
                
                Trip busTrip6 = factoryManager.getFactory("Bus").createTrip(
                        "BT006", "Ankara", "Izmir",
                        LocalDateTime.of(2025, 6, 15, 9, 0),
                        LocalDateTime.of(2025, 6, 15, 16, 0),
                        50.0, 29, "Metro Turizm", "7h 0m", "AC, WiFi, TV", "06-MT-006");
                tripService.addTrip(busTrip6);
                
                Trip busTrip7 = factoryManager.getFactory("Bus").createTrip(
                        "BT007", "Izmir", "Istanbul",
                        LocalDateTime.of(2025, 6, 15, 11, 30),
                        LocalDateTime.of(2025, 6, 15, 19, 0),
                        70.0, 29, "Varan Turizm", "7h 30m", "AC, Refreshment", "35-VR-007");
                tripService.addTrip(busTrip7);

                
                Trip flightTrip1 = factoryManager.getFactory("Flight").createTrip(
                        "FT001", "Istanbul", "Ankara",
                        LocalDate.of(2025, 06, 18).atTime(8, 30),
                        LocalDate.of(2025, 06, 18).atTime(9, 45),
                        150.0, 150, "THY ", "1h 15m", "WiFi, Meal Service", "Boeing 737");
                tripService.addTrip(flightTrip1);

                Trip flightTrip2 = factoryManager.getFactory("Flight").createTrip(
                        "FT002", "Istanbul", "Izmir",
                        LocalDate.of(2025, 06, 18).atTime(10, 15),
                        LocalDate.of(2025, 06, 18).atTime(11, 30),
                        120.0, 150, "Pegasus", "1h 15m", "WiFi, Meal Service", "Airbus A320");
                tripService.addTrip(flightTrip2);

                Trip flightTrip3 = factoryManager.getFactory("Flight").createTrip(
                        "FT003", "Istanbul", "Antalya",
                        LocalDate.of(2025, 06, 18).atTime(14, 0),
                        LocalDate.of(2025, 06, 18).atTime(15, 15),
                        180.0, 150, "SunExpress", "1h 15m", "WiFi, Meal Service", "Boeing 7377");
                tripService.addTrip(flightTrip3);

                Trip flightTrip4 = factoryManager.getFactory("Flight").createTrip(
                        "FT004", "Ankara", "Bodrum",
                        LocalDate.of(2025, 06, 18).atTime(16, 30),
                        LocalDate.of(2025, 06, 18).atTime(17, 45),
                        200.0, 150, "THY", "1h 15m", "WiFi, Meal Service", "Airbus A321");
                tripService.addTrip(flightTrip4);

                Trip flightTrip5 = factoryManager.getFactory("Flight").createTrip(
                        "FT005", "Izmir", "Trabzon",
                        LocalDate.of(2025, 06, 18).atTime(19, 0),
                        LocalDate.of(2025, 06, 18).atTime(20, 15),
                        160.0, 150, "Pegasus", "1h 15m", "WiFi, Meal Service", "Boeing 737");
                tripService.addTrip(flightTrip5);

            } catch (Exception e) {
                System.err.println("Örnek veri yüklenirken hata oluştu: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Örnek seyahat verileri zaten yüklü. Tekrar yüklenmiyor.");
        }
    }
}