// BusSeatSelectionPage.java
package gui;

import command.*;
import factory.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import models.*;
import observer.Observer;
import observer.SeatManager;
import repository.*;
import service.*;
import singleton.*;
import state.*;

public class BusSeatSelectionPage extends BasePanel implements Observer {
    private String busCompany;
    private String fromCity;
    private String toCity;
    private String departureDate;
    private String departureTime;
    private String arrivalTime;
    private String basePrice;
    private int passengerCount;
    private String amenities;

    private JPanel seatMapPanel;
    private JLabel selectedSeatsLabel;
    private JLabel totalPriceLabel;
    private JButton confirmButton;
    private JButton backButton;

    private List<BusSeatButton> selectedSeats;
    private List<BusSeatButton> allSeats;
    private double basePriceValue;

    private SeatManager seatManager;
    
    // Services and repositories
    private ReservationRepository reservationRepository;
    private UserRepository userRepository;
    private TripRepository tripRepository;
    private ReservationService reservationService;
    private CommandInvoker commandInvoker;

    private List<Integer> preReservedSeats;

    public BusSeatSelectionPage(String busCompany, String fromCity, String toCity, String departureDate, String arrivalTime,
                               String basePrice, int passengerCount, String amenities) {
        super("Bus Seat Selection - " + busCompany, 1400, 800);
        this.busCompany = busCompany;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.departureDate = departureDate;
        this.departureTime = extractDefault(departureDate); // Extract time if combined
        this.arrivalTime = arrivalTime;
        this.basePrice = basePrice;
        this.passengerCount = passengerCount;
        this.amenities = amenities;
        this.selectedSeats = new ArrayList<>();
        this.allSeats = new ArrayList<>();
        this.seatManager = new SeatManager();

        this.seatManager.addObserver(this);

        try {
            this.basePriceValue = Double.parseDouble(basePrice.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            this.basePriceValue = 45.0;
        }
        
        initializePreReservedSeats();
        initializeServices();
    }
    
    private void initializeServices() {
        this.reservationRepository = new ReservationRepository();
        this.userRepository = UserRepository.getInstance();
        this.tripRepository = TripRepository.getInstance();
        this.reservationService = new ReservationService(reservationRepository, userRepository, tripRepository);
        this.commandInvoker = new CommandInvoker();
    }
    
    private void initializePreReservedSeats() {
        SeatStatusManager seatStatusManager = SeatStatusManager.getInstance();
        Set<Integer> occupiedSeatsSet = seatStatusManager.getOccupiedSeats(busCompany);
        
        preReservedSeats = new ArrayList<>(occupiedSeatsSet);
        
        if (preReservedSeats.isEmpty()) {
            preReservedSeats.add(3);
            preReservedSeats.add(7);
            preReservedSeats.add(12);
            preReservedSeats.add(18);
            preReservedSeats.add(23);
            seatStatusManager.markSeatsAsOccupied(busCompany, preReservedSeats);
        }
    }
    
    private String extractDefault(String dateTimeString) {
        if (dateTimeString != null && dateTimeString.contains(" ")) {
            String[] parts = dateTimeString.split(" ");
            if (parts.length > 1) {
                return parts[1];
            }
        }
        return "08:00"; // Default time
    }

    @Override
    public void setupUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(15, 15, 35));
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(15, 15, 35),
                    getWidth(), getHeight(), new Color(25, 25, 55)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Decorative circles
                g2d.setColor(new Color(138, 43, 226, 30));
                g2d.fillOval(-50, -50, 200, 200);
                g2d.fillOval(getWidth()-150, getHeight()-150, 200, 200);
                
                g2d.setColor(new Color(75, 0, 130, 20));
                g2d.fillOval(getWidth()-100, -50, 150, 150);
                g2d.fillOval(-100, getHeight()-100, 150, 150);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setOpaque(false);

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createBusSeatMapPanel();
    
        JPanel sidebarPanel = createSidebarPanel();

        contentPanel.add(seatMapPanel, BorderLayout.CENTER);
        contentPanel.add(sidebarPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20));

        // Back button
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.setOpaque(false);
        backButton = createButton("← Back", new Color(108, 92, 231), false);
        backButton.setPreferredSize(new Dimension(100, 35));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backPanel.add(backButton);

        // Title
        JLabel titleLabel = new JLabel("Select Your Seat", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Choose the perfect seat for your journey", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(189, 147, 249));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Trip info card 
        JPanel infoCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(new Color(255, 255, 255, 10));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Border
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        infoCard.setLayout(new GridLayout(1, 4, 30, 0));
        infoCard.setOpaque(false);
        infoCard.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        infoCard.setMaximumSize(new Dimension(1000, 100));


        // Info items
        JPanel companyInfo = createInfoItem("🚌", busCompany, "Company");
        JPanel routeInfo = createInfoItem("📍", fromCity + " → " + toCity, "Route");
        JPanel dateInfo = createInfoItem("📅", departureDate + " " + departureTime, "Departure");
        JPanel passengerInfo = createInfoItem("👥", passengerCount + " passenger(s)", "Passengers");

        infoCard.add(companyInfo);
        infoCard.add(routeInfo);
        infoCard.add(dateInfo);
        infoCard.add(passengerInfo);

        headerPanel.add(backPanel);
        headerPanel.add(Box.createVerticalStrut(20));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(30));
        headerPanel.add(infoCard);

        // Back button action
        backButton.addActionListener(e -> {
            dispose();
            new SearchBusTripPage().display();
        });

        return headerPanel;
    }

    private JPanel createInfoItem(String icon, String value, String label) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelLabel.setForeground(new Color(189, 147, 249));
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(labelLabel);

        return panel;
    }

    private void createBusSeatMapPanel() {
        seatMapPanel = new JPanel(new BorderLayout());
        seatMapPanel.setOpaque(false);

        // Bus container 
        JPanel busContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glassmorphism background
                g2d.setColor(new Color(255, 255, 255, 10));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Border
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        busContainer.setOpaque(false);
        busContainer.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Bus header
        JPanel busHeader = new JPanel(new BorderLayout());
        busHeader.setOpaque(false);

        JLabel busLabel = new JLabel(busCompany.toUpperCase() + "  ", SwingConstants.CENTER);
        busLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        busLabel.setForeground(Color.WHITE);

        busHeader.add(busLabel, BorderLayout.CENTER);

        // Seat layout
        JPanel seatsPanel = createSeatLayout();

        // Legend
        JPanel legendPanel = createLegendPanel();

        busContainer.add(busHeader, BorderLayout.NORTH);
        busContainer.add(seatsPanel, BorderLayout.CENTER);
        busContainer.add(legendPanel, BorderLayout.SOUTH);

        seatMapPanel.add(busContainer, BorderLayout.CENTER);
    }

    private JPanel createSeatLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        JPanel busLayout = new JPanel();
        busLayout.setLayout(new BoxLayout(busLayout, BoxLayout.Y_AXIS));
        busLayout.setOpaque(false);

        int seatNumber = 1;

        for (int row = 0; row < 4; row++) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
            rowPanel.setOpaque(false);

            for (int col = 0; col < 10; col++) {
                if (row == 1 || row == 2 && col == 0) {
                    JLabel aisleLabel = new JLabel((row == 1) ? "Corridor" : "");
                    aisleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                    aisleLabel.setForeground(new Color(189, 147, 249));
                    aisleLabel.setPreferredSize(new Dimension(50, 20));
                    aisleLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    rowPanel.add(aisleLabel);
                } else {
                    boolean isOccupied = preReservedSeats.contains(seatNumber);
                    boolean isPremium = col < 3;

                    BusSeatButton seat = new BusSeatButton(seatNumber++, isOccupied, false, isPremium); // isWindow is always false
                    seat.setSeatManager(seatManager);
                    allSeats.add(seat);
                    rowPanel.add(seat);
                } 
            }
            busLayout.add(rowPanel);
        }
        mainPanel.add(busLayout, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel availableItem = createLegendItem("Available", new Color(75, 181, 67), "12");
        JPanel selectedItem = createLegendItem("Selected", new Color(138, 43, 226), "12");
        JPanel occupiedItem = createLegendItem("Occupied", new Color(220, 53, 69), "X");
        JPanel premiumItem = createLegendItem("Premium (+30%)", new Color(255, 193, 7), "P1");

        legendPanel.add(availableItem);
        legendPanel.add(selectedItem);
        legendPanel.add(occupiedItem);
        legendPanel.add(premiumItem);

        return legendPanel;
    }

   private JPanel createLegendItem(String label, Color color, String seatText) {
    JPanel item = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    item.setOpaque(false);

    JButton sampleSeat = new JButton(seatText) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    };

    sampleSeat.setForeground(isLightColor(color) ? Color.BLACK : Color.WHITE);
    sampleSeat.setFont(new Font("Segoe UI", Font.BOLD, 11));
    sampleSeat.setPreferredSize(new Dimension(30, 30));
    sampleSeat.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1, true));
    sampleSeat.setContentAreaFilled(false);
    sampleSeat.setBorderPainted(true);
    sampleSeat.setFocusPainted(false);
    sampleSeat.setOpaque(false);
    sampleSeat.setEnabled(false);

    JLabel labelText = new JLabel(label);
    labelText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    labelText.setForeground(Color.WHITE);

    item.add(sampleSeat);
    item.add(labelText);

    return item;
}

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glassmorphism background
                g2d.setColor(new Color(255, 255, 255, 10));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Border
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));

        // Selection info
        selectedSeatsLabel = new JLabel("No seat selected");
        selectedSeatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectedSeatsLabel.setForeground(new Color(189, 147, 249));
        selectedSeatsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel needSeatsLabel = new JLabel("Need " + passengerCount + " seat(s)");
        needSeatsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        needSeatsLabel.setForeground(Color.WHITE);
        needSeatsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Price display
        totalPriceLabel = new JLabel("Total: 0.00 TL");
        totalPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalPriceLabel.setForeground(new Color(138, 43, 226));
        totalPriceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons
        confirmButton = createButton("Confirm Selection", new Color(138, 43, 226), true);
        confirmButton.setEnabled(false);
        confirmButton.setMaximumSize(new Dimension(280, 50));

        JButton clearButton = createButton("Clear Selection", new Color(108, 92, 231), false);
        clearButton.setMaximumSize(new Dimension(280, 50));

        // Features panel
        JPanel featuresPanel = createFeaturesPanel();

        // Layout
        sidebar.add(Box.createVerticalStrut(25));
        sidebar.add(selectedSeatsLabel);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(needSeatsLabel);
        sidebar.add(Box.createVerticalStrut(25));
        sidebar.add(totalPriceLabel);
        sidebar.add(Box.createVerticalStrut(35));
        sidebar.add(confirmButton);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(clearButton);
        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(featuresPanel);

        // Button actions
        clearButton.addActionListener(e -> clearSelection());
        confirmButton.addActionListener(e -> confirmReservation());

        return sidebar;
    }

    private JButton createButton(String text, Color baseColor, boolean isPrimary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(baseColor.brighter());
                } else {
                    g2d.setColor(baseColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        return button;
    }

    private JPanel createFeaturesPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glassmorphism background
                g2d.setColor(new Color(255, 255, 255, 8));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Border
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Bus Features");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] features = {
            "• Comfortable reclining seats",
            "• " + amenities,
            "• Professional driver service"
        };

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            featureLabel.setForeground(new Color(189, 147, 249));
            featureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(featureLabel);
            panel.add(Box.createVerticalStrut(5));
        }
        return panel;
    }

    private boolean isLightColor(Color color) {
        int yiq = ((color.getRed() * 299) +
                   (color.getGreen() * 587) +
                   (color.getBlue() * 114)) / 1000;
        return yiq >= 180;
    }

    private void clearSelection() {
        for (BusSeatButton seat : selectedSeats) {
            seat.setSelected(false);
        }
        selectedSeats.clear();
        updateSelectionInfo();
    }

    @Override
    public void update() {
        updateSelectionInfo();
    }

    private void updateSelectionInfo() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsLabel.setText("No seat selected");
            totalPriceLabel.setText("Total: 0.00 TL");
            confirmButton.setEnabled(false);
        } else {
            StringBuilder seatNumbers = new StringBuilder();
            double totalPrice = 0;

            for (int i = 0; i < selectedSeats.size(); i++) {
                if (i > 0) seatNumbers.append(", ");
                seatNumbers.append(selectedSeats.get(i).getSeatNumber());
                totalPrice += selectedSeats.get(i).getPrice()/100.0; 
            }
            selectedSeatsLabel.setText("Seats: " + seatNumbers.toString());
            totalPriceLabel.setText(String.format("Total: %.2f TL", totalPrice));

            confirmButton.setEnabled(selectedSeats.size() == passengerCount);
        }
    }
    
   private void confirmReservation() {
        if (selectedSeats.size() != passengerCount) {
            PageComponents.showStyledMessage("Error", 
                "Please select exactly " + passengerCount + " seat(s)!", this);
            return;
        }

        User currentUser = SessionManager.getInstance().getLoggedInUser();
        if (currentUser == null) {
            PageComponents.showStyledMessage("Error", "Please login first!", this);
            return;
        }

        try {
            List<Integer> selectedSeatNumbers = new ArrayList<>();
            for (BusSeatButton seat : selectedSeats) {
                selectedSeatNumbers.add(seat.getSeatNumber());
            }
            SeatStatusManager seatStatusManager = SeatStatusManager.getInstance();
            seatStatusManager.markSeatsAsOccupied(busCompany, selectedSeatNumbers);

            TripFactoryManager factoryManager = new TripFactoryManager();
            TripFactory busFactory = factoryManager.getFactory("Bus");

            String tripId = "BUS_" + java.util.UUID.randomUUID().toString().substring(0, 8);

            Trip busTrip = busFactory.createTrip(
                tripId,
                fromCity,
                toCity,
                java.time.LocalDateTime.now().plusDays(1), 
                java.time.LocalDateTime.now().plusDays(1).plusHours(6), 
                basePriceValue,
                40, 
                busCompany,
                "6h 30m", 
                amenities,
                "BUS-" + tripId
            );

            List<Seat> reservedSeats = new ArrayList<>();
            for (BusSeatButton seatButton : selectedSeats) {
                String seatNo=seatButton.getSeatNumber()+"";
                Seat seat = new Seat(seatButton.getSeatNumber(), false ,seatNo);
                seat.reserve();
                reservedSeats.add(seat);
            }

            String reservationId = java.util.UUID.randomUUID().toString();
            Reservation reservation = new Reservation(reservationId, currentUser, busTrip, reservedSeats);
            ReservationRepository globalReservationRepo = getGlobalReservationRepository();
            globalReservationRepo.save(reservation);
            ReservationContext reservationContext = new ReservationContext(reservationId);
            reservationContext.confirm(); 

            // Calculate total price using strategy pattern
            double totalPrice = 0;
            for (BusSeatButton seat : selectedSeats) {
                totalPrice += seat.getPrice();
            }

            String successMessage = String.format(
                "🎉 Bus Reservation Confirmed!\n\n" +
                "Your bus reservation has been saved successfully!"
            );

            PageComponents.showStyledMessage("Bus Reservation Successful!", successMessage, this);

            // back to main menu
            dispose();
            new MainMenuPage().display();

        } catch (Exception e) {
            PageComponents.showStyledMessage("Error", 
                "Failed to create bus reservation: " + e.getMessage(), this);
            e.printStackTrace();
        }
    } 
   
    private ReservationRepository getGlobalReservationRepository() {
        return GlobalRepositoryManager.getInstance().getReservationRepository();
    }

    public void refreshSeatStatus() {
        // Mevcut seat'leri temizle
        allSeats.clear();
        selectedSeats.clear();
        
        // Occupied seat'leri yeniden yükle
        initializePreReservedSeats();
        
        // UI'yi yeniden oluştur
        seatMapPanel.removeAll();
        createBusSeatMapPanel();
        
        // UI'yi güncelle
        updateSelectionInfo();
        revalidate();
        repaint();
    }

    private class BusSeatButton extends JButton {
        private int seatNumber;
        private boolean isOccupied;
        private boolean isSelected;
        // Removed isWindow
        private boolean isPremium;
        private double price;
        private SeatManager seatManager;

        public BusSeatButton(int seatNumber, boolean isOccupied, boolean isWindow, boolean isPremium) { // isWindow parameter retained for compatibility, but its usage is removed
            this.seatNumber = seatNumber;
            this.isOccupied = isOccupied;
            this.isSelected = false;
            this.isPremium = isPremium;
            this.price = calculateSeatPrice();

            setupButton();
        }

        @Override
    protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                g2.setColor(new Color(138, 43, 226)); // Mor - seçili
            } else if (isOccupied) {
                g2.setColor(new Color(220, 53, 69)); // Kırmızı - dolu
            } else if (isPremium) {
                g2.setColor(new Color(255, 193, 7)); // Sarı - premium
            } else {
                g2.setColor(new Color(75, 181, 67)); // Yeşil - müsait
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
        
            super.paintComponent(g); // Yazı ve ikonları çiz
        }

         public void setSeatManager(SeatManager seatManager) {
             this.seatManager = seatManager;
         }

        private void setupButton() {
            setPreferredSize(new Dimension(35, 35));
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setBorderPainted(false);    
            setOpaque(false); 

            if (isOccupied) {
                setText("X");
                setBackground(new Color(220, 53, 69));
                setForeground(Color.WHITE);
                setEnabled(false);
                setToolTipText("Seat " + seatNumber + " occupied");
            } else {
                setText(isPremium ? "P" + seatNumber : String.valueOf(seatNumber));
                setBackground(isPremium ? new Color(255, 193, 7) : new Color(75, 181, 67));
                setForeground(Color.WHITE);
                setToolTipText(String.format("Seat %d - %.2f TL%s",
                    seatNumber, price,
                    isPremium ? " (Premium)" : ""
                ));
                addActionListener(e -> toggleSelection());
            }
            setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1, true));
        }

        private double calculateSeatPrice() {
            double multiplier = 1.0;

            if (isPremium) multiplier += 0.3;
            return basePriceValue * multiplier;
        }

        private void toggleSelection() {
            if (isSelected) {
                setSelected(false);
                selectedSeats.remove(this);
            } else {
                if (selectedSeats.size() >= passengerCount) {
                    PageComponents.showStyledMessage("Warning",
                        "You can only select " + passengerCount + " seat(s)!",
                        BusSeatSelectionPage.this);
                    return;
                }
                setSelected(true);
                selectedSeats.add(this);
            }
            if (seatManager != null) {
                seatManager.update();
            }
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;

            if (selected) {
                setBorder(BorderFactory.createLineBorder(new Color(189, 147, 249), 2, true));
            } else {
                setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1, true));
            }
            repaint();
        }

        public int getSeatNumber() {
            return seatNumber;
        }

        public double getPrice() {
            return price;
        }
    }
}