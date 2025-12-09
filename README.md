import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// =============================================================
//  CUSTOM EXCEPTIONS  (Exception Handling - OOP)
// =============================================================

class UserNotFoundException extends Exception {
    public UserNotFoundException(String msg) {
        super(msg);
    }
}

// =============================================================
//  INTERFACE + INHERITANCE / POLYMORPHISM (OOP)
// =============================================================

interface Purchasable {
    double getPrice();
    String getDisplayInfo();
}

abstract class User {
    private final String username;

    protected User(String username) {
        this.username = username;
    }

    public String getUsername() { return username; }

    // polymorphic behaviour:
    public abstract String getRole();
}

class AdminUser extends User {
    public AdminUser(String username) { super(username); }

    @Override
    public String getRole() {
        return "admin";
    }
}

class CustomerUser extends User {
    public CustomerUser(String username) { super(username); }

    @Override
    public String getRole() {
        return "user";
    }
}

// =============================================================
//  MODEL CLASSES  (Artwork + Purchase)
// =============================================================

class Artwork implements Purchasable {
    private final int id;
    private final String title;
    private final String artist;
    private final double price;

    public Artwork(int id, String title, String artist, double price) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.price = price;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }

    @Override
    public double getPrice() { return price; }

    @Override
    public String getDisplayInfo() {
        return id + " | " + title + " by " + artist + " | ₹" + price;
    }

    @Override
    public String toString() {
        return getDisplayInfo();
    }
}

class Purchase {
    private final User user;
    private final Artwork artwork;
    private final java.util.Date time;

    public Purchase(User user, Artwork artwork, java.util.Date time) {
        this.user = user;
        this.artwork = artwork;
        this.time = time;
    }

    public User getUser() { return user; }
    public Artwork getArtwork() { return artwork; }
    public java.util.Date getTime() { return time; }
}

// =============================================================
//  DATABASE MANAGER (Classes for DB operations + JDBC)
// =============================================================

class DatabaseManager {

    // TODO: in values ko apne DB ke hisaab se change karo
    private static final String URL = "jdbc:mysql://localhost:3306/artdb";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    // JDBC Connection
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Load artworks from DB (Collections & Generics)
    public List<Artwork> fetchAllArtworks() throws SQLException {
        List<Artwork> list = new ArrayList<>();
        String sql = "SELECT id, title, artist, price FROM artworks";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String artist = rs.getString("artist");
                double price = rs.getDouble("price");
                list.add(new Artwork(id, title, artist, price));
            }
        }
        return list;
    }

    // Insert a purchase using JDBC
    public void insertPurchase(Purchase p) throws SQLException {
        String sql = "INSERT INTO purchases(username, artwork_id, price, purchased_at) "
                + "VALUES(?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getUser().getUsername());
            ps.setInt(2, p.getArtwork().getId());
            ps.setDouble(3, p.getArtwork().getPrice());
            ps.setTimestamp(4, new Timestamp(p.getTime().getTime()));

            ps.executeUpdate();
        }
    }
}

/*
 * Example MySQL tables: (just for reference)
 *
 * CREATE DATABASE artdb;
 * USE artdb;
 *
 * CREATE TABLE artworks (
 *   id INT PRIMARY KEY,
 *   title VARCHAR(100),
 *   artist VARCHAR(100),
 *   price DOUBLE
 * );
 *
 * CREATE TABLE purchases (
 *   id INT PRIMARY KEY AUTO_INCREMENT,
 *   username VARCHAR(100),
 *   artwork_id INT,
 *   price DOUBLE,
 *   purchased_at TIMESTAMP
 * );
 */

// =============================================================
//  SERVICE LAYER  (uses Collections, Generics, Synchronization)
// =============================================================

class ExhibitionService {

    private final List<User> users = new ArrayList<>();
    private final List<Artwork> artworks = new ArrayList<>();
    private final Map<Integer, Artwork> artworkMap = new HashMap<>();
    private final List<Purchase> purchases =
            Collections.synchronizedList(new ArrayList<>());

    private final DatabaseManager dbManager = new DatabaseManager();

    public ExhibitionService() {
        // Users with inheritance / polymorphism
        users.add(new AdminUser("somesh"));
        users.add(new CustomerUser("divyansha"));
        users.add(new CustomerUser("himani"));

        // Try to load artworks from DB; if fail, load sample data
        try {
            List<Artwork> dbArtworks = dbManager.fetchAllArtworks();
            if (dbArtworks.isEmpty()) {
                loadSampleArtworks();
            } else {
                dbArtworks.forEach(this::addArtworkInternal);
            }
        } catch (SQLException e) {
            // Exception handling: fall back to in-memory data
            System.out.println("DB error while loading artworks: " + e.getMessage());
            loadSampleArtworks();
        }
    }

    private void loadSampleArtworks() {
        addArtworkInternal(new Artwork(101, "Sunset Dreams", "A. Sharma", 15000));
        addArtworkInternal(new Artwork(102, "City Lights", "R. Verma", 22000));
        addArtworkInternal(new Artwork(103, "Nature Bliss", "Himani Rao", 18000));
    }

    private void addArtworkInternal(Artwork a) {
        artworks.add(a);
        artworkMap.put(a.getId(), a);
    }

    public User login(String username) throws UserNotFoundException {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    public List<Artwork> getArtworks() {
        // defensive copy
        return new ArrayList<>(artworks);
    }

    public synchronized void recordPurchase(User user, Artwork artwork) throws SQLException {
        // synchronized => thread-safe list update (Multithreading & Synchronization)
        Purchase p = new Purchase(user, artwork, new java.util.Date());
        purchases.add(p);
        dbManager.insertPurchase(p);  // JDBC call
    }
}

// =============================================================
//  MULTITHREADING: Purchase as background task
// =============================================================

class PurchaseTask extends Thread {
    private final ExhibitionService service;
    private final User user;
    private final Artwork artwork;
    private final Component parent;

    public PurchaseTask(ExhibitionService service, User user,
                        Artwork artwork, Component parent) {
        this.service = service;
        this.user = user;
        this.artwork = artwork;
        this.parent = parent;
    }

    @Override
    public void run() {
        try {
            service.recordPurchase(user, artwork);
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(parent,
                            "Purchase saved to database for " + artwork.getTitle(),
                            "Purchase Successful", JOptionPane.INFORMATION_MESSAGE)
            );
        } catch (SQLException ex) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(parent,
                            "Database error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE)
            );
        }
    }
}

// =============================================================
//  SIMPLE "3D / VR" CONCEPT DEMO PANEL (Swing only)
// =============================================================

class Fake3DPanel extends JPanel {

    private final List<Artwork> artworks;

    public Fake3DPanel(List<Artwork> artworks) {
        this.artworks = artworks;
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        g2.setColor(Color.WHITE);
        g2.drawString("Virtual Gallery (Pseudo 3D View)", 20, 20);

        if (artworks == null || artworks.isEmpty()) {
            g2.drawString("No artworks available", 20, 40);
            return;
        }

        int centerY = h / 2;
        int startX = 80;
        int gap = 140;

        for (int i = 0; i < artworks.size(); i++) {
            Artwork a = artworks.get(i);

            int panelWidth = 100;
            int panelHeight = 140;

            int x = startX + i * gap;
            int y = centerY - panelHeight / 2;

            // Simple “perspective”: panels further right look slightly smaller
            double scale = 1.0 - (0.1 * i);
            int pw = (int) (panelWidth * scale);
            int ph = (int) (panelHeight * scale);

            g2.setColor(new Color(50, 50, 80));
            g2.fillRoundRect(x, y, pw, ph, 20, 20);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(x, y, pw, ph, 20, 20);

            // Artwork title text
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.drawString(a.getTitle(), x + 5, y + ph + 15);
            g2.drawString("by " + a.getArtist(), x + 5, y + ph + 30);
        }
    }
}

// =============================================================
//  MAIN GUI  (Swing - single public class)
// =============================================================

public class ArtExhibitionApp extends JFrame {

    private final ExhibitionService service = new ExhibitionService();
    private User currentUser;

    private final CardLayout card = new CardLayout();
    private final JPanel root = new JPanel(card);

    private JTextField usernameField;
    private JLabel imageLabel;

    // CHANGE THIS TO YOUR LOCAL IMAGE PATH
    private static final String ARTWORK_IMAGE_PATH = "art1.jpg";

    public ArtExhibitionApp() {
        setTitle("Online Art Exhibition Platform (with VR Concept View)");
        setSize(950, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        root.add(buildLoginScreen(), "login");
        setContentPane(root);
        card.show(root, "login");
    }

    // --------------------- LOGIN SCREEN -----------------------

    private JPanel buildLoginScreen() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(30, 30, 30));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Online Art Exhibition Platform");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        p.add(title, g);

        JLabel userLbl = new JLabel("Enter Username (somesh / divyansha / himani):");
        userLbl.setForeground(Color.WHITE);
        g.gridy++; g.gridwidth = 1;
        p.add(userLbl, g);

        usernameField = new JTextField(15);
        g.gridx = 1;
        p.add(usernameField, g);

        JButton loginBtn = new JButton("Login");
        g.gridx = 0; g.gridy++; g.gridwidth = 2;
        p.add(loginBtn, g);

        loginBtn.addActionListener(e -> doLogin());

        return p;
    }

    private void doLogin() {
        String name = usernameField.getText().trim();
        try {
            currentUser = service.login(name);   // may throw UserNotFoundException
            root.add(buildMainMenu(), "main");
            card.show(root, "main");
        } catch (UserNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --------------------- MAIN MENU --------------------------

    private JPanel buildMainMenu() {
        JPanel p = new JPanel(new BorderLayout());

        JLabel top = new JLabel(
                "Welcome, " + currentUser.getUsername() +
                        " (" + currentUser.getRole() + ")",
                SwingConstants.CENTER);
        top.setFont(new Font("Arial", Font.BOLD, 18));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        p.add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Purchase Artworks", buildPurchaseTab());
        p.add(tabs, BorderLayout.CENTER);

        // Button to open a pseudo 3D / VR-style view
        JButton open3D = new JButton("3D / VR View (Concept Demo)");
        open3D.addActionListener(e -> openFake3DWindow());
        p.add(open3D, BorderLayout.SOUTH);

        return p;
    }

    private void openFake3DWindow() {
        JDialog dialog = new JDialog(this, "Virtual Gallery (Concept)", true);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(this);

        Fake3DPanel panel = new Fake3DPanel(service.getArtworks());
        dialog.add(panel);

        dialog.setVisible(true);
    }

    // --------------------- PURCHASE TAB -----------------------

    private JPanel buildPurchaseTab() {
        JPanel p = new JPanel(new BorderLayout(10, 10));

        DefaultListModel<Artwork> model = new DefaultListModel<>();
        for (Artwork a : service.getArtworks()) {
            model.addElement(a);
        }
        JList<Artwork> list = new JList<>(model);
        list.setFont(new Font("Arial", Font.PLAIN, 14));

        // IMAGE PREVIEW PANEL
        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        imageLabel = new JLabel("Select artwork to preview", SwingConstants.CENTER);
        imgPanel.add(imageLabel, BorderLayout.CENTER);

        // list selection -> show image
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateImagePreview();
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(list), imgPanel);
        split.setDividerLocation(450);

        JButton buy = new JButton("Buy Artwork");
        buy.addActionListener(e -> {
            Artwork a = list.getSelectedValue();
            if (a == null) {
                JOptionPane.showMessageDialog(this, "Select artwork first!");
                return;
            }
            // Multithreading: run DB write in background
            new PurchaseTask(service, currentUser, a, this).start();
        });

        p.add(split, BorderLayout.CENTER);
        p.add(buy, BorderLayout.SOUTH);

        return p;
    }

    // --------------------- IMAGE PREVIEW ----------------------

    private void updateImagePreview() {
        ImageIcon img = new ImageIcon(ARTWORK_IMAGE_PATH);

        if (img.getIconWidth() <= 0) {
            imageLabel.setText("Image not found: " + ARTWORK_IMAGE_PATH);
            imageLabel.setIcon(null);
            return;
        }

        Image scaled = img.getImage().getScaledInstance(
                300, 300, Image.SCALE_SMOOTH);
        imageLabel.setText("");
        imageLabel.setIcon(new ImageIcon(scaled));
    }

    // --------------------- MAIN METHOD ------------------------

    public static void main(String[] args) {
        // optional better L&F
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new ArtExhibitionApp().setVisible(true));
    }
}
