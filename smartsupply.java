import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * SmartSupplyApp.java — with theme toggle and enhanced login UI.
 */
public class SmartSupplyApp {
    // In‑memory data stores
    private final Map<String, Product> products = new LinkedHashMap<>();
    private final List<String> ledger = new ArrayList<>();
    private final DefaultListModel<String> customerProductListModel = new DefaultListModel<>();
    private final DefaultListModel<String> deliveryProductListModel = new DefaultListModel<>();

    // Current user / role
    private String currentRole = "Customer";
    private String currentUserName = "DemoUser";

    // UI root
    private final JFrame frame = new JFrame("SmartSupply - Demo");
    private final CardLayout rootCardLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCardLayout);

    private final JPanel customerPanel = new JPanel(new BorderLayout());
    private final JPanel deliveryPanel = new JPanel(new BorderLayout());

    private final JLabel statusBar = new JLabel("Welcome to SmartSupply demo");
    private final JTextArea ledgerArea = new JTextArea(6, 40);

    private final DefaultListModel<String> chatModel = new DefaultListModel<>();

    // Theme state & colors
    private boolean darkMode = false;
    private final Color LIGHT_BG = Color.WHITE;
    private final Color LIGHT_FG = Color.BLACK;
    private final Color DARK_BG = new Color(45, 45, 45);
    private final Color DARK_FG = new Color(230, 230, 230);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new SmartSupplyApp().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void start() {
        createSampleProducts();
        buildUI();
        frame.setSize(980, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // After showing, apply initial theme
        applyTheme(frame.getContentPane(), darkMode);
    }

    private void createSampleProducts() {
        addSampleProduct(new Product("PROD001", "Vitamin C Supplement", "ABC Pharma", "XY2 Distributors", "Retailer One", "DeliveryGuy1", 12.9716, 77.5946));
        addSampleProduct(new Product("PROD002", "Organic Green Tea", "GreenLeaf Co", "DistX", "HealthyStore", "DeliveryGuy1", 12.9710, 77.5950));
        addSampleProduct(new Product("PROD003", "Fitness Band", "FitLLC", "LogiCorp", "SportMart", "DeliveryGuy2", 12.9720, 77.5936));
    }

    private void addSampleProduct(Product p) {
        products.put(p.id, p);
        customerProductListModel.addElement(displayForList(p));
        deliveryProductListModel.addElement(displayForList(p));
        ledger.add(timestamp() + " - REGISTERED - " + p.id + " by " + p.manufacturer);
    }

    private String displayForList(Product p) {
        return String.format("%s — %s — %s", p.id, p.name, p.status);
    }

    private void rebuildLists() {
        customerProductListModel.clear();
        deliveryProductListModel.clear();
        for (Product p : products.values()) {
            customerProductListModel.addElement(displayForList(p));
            deliveryProductListModel.addElement(displayForList(p));
        }
    }

    private void buildUI() {
        frame.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("SmartSupply", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        String[] roles = {"Customer", "Delivery Person"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setSelectedItem(currentRole);
        roleCombo.addActionListener(e -> {
            currentRole = (String) roleCombo.getSelectedItem();
            switchToRole(currentRole);
        });
        rolePanel.add(new JLabel("Role:"));
        rolePanel.add(roleCombo);

        // Theme toggle
        String[] themes = {"Light", "Dark"};
        JComboBox<String> themeCombo = new JComboBox<>(themes);
        themeCombo.setSelectedIndex(darkMode ? 1 : 0);
        themeCombo.addActionListener(e -> {
            darkMode = "Dark".equals(themeCombo.getSelectedItem());
            applyTheme(frame.getContentPane(), darkMode);
        });
        rolePanel.add(new JLabel("Theme:"));
        rolePanel.add(themeCombo);

        JButton ledgerBtn = new JButton("View Ledger");
        ledgerBtn.addActionListener(e -> showLedgerDialog());
        rolePanel.add(ledgerBtn);

        header.add(rolePanel, BorderLayout.EAST);
        frame.add(header, BorderLayout.NORTH);

        // Root panels (login, customer, delivery)
        rootPanel.add(buildLoginPanel(), "login");
        rootPanel.add(buildCustomerMain(), "customerMain");
        rootPanel.add(buildDeliveryMain(), "deliveryMain");
        frame.add(rootPanel, BorderLayout.CENTER);

        statusBar.setBorder(new EmptyBorder(6, 10, 6, 10));
        frame.add(statusBar, BorderLayout.SOUTH);

        rootCardLayout.show(rootPanel, "login");
    }

    private JPanel buildLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(72, 61, 139), 0, getHeight(), new Color(123, 104, 238));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel brand = new JLabel("🚚 SmartSupply");
        brand.setFont(new Font("SansSerif", Font.BOLD, 36));
        brand.setForeground(Color.WHITE);
        brand.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(brand, gbc);

        gbc.gridwidth = 1;

        gbc.gridy++; gbc.gridx = 0;
        JLabel roleLbl = new JLabel("Select Role:");
        roleLbl.setForeground(Color.WHITE);
        loginPanel.add(roleLbl, gbc);

        gbc.gridx = 1;
        JComboBox<String> roleSelect = new JComboBox<>(new String[]{"Customer", "Delivery Person"});
        loginPanel.add(roleSelect, gbc);

        gbc.gridy++; gbc.gridx = 0;
        JLabel userLbl = new JLabel("User Name:");
        userLbl.setForeground(Color.WHITE);
        loginPanel.add(userLbl, gbc);

        gbc.gridx = 1;
        JTextField userField = new JTextField("DemoUser");
        loginPanel.add(userField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Color.WHITE);
        loginPanel.add(passLbl, gbc);

        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField();
        loginPanel.add(passField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton loginBtn = new JButton("🚀 Enter App");
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginBtn.setBackground(new Color(100, 149, 237));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginPanel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            currentUserName = userField.getText().trim().isEmpty() ? "DemoUser" : userField.getText().trim();
            currentRole = (String) roleSelect.getSelectedItem();
            statusBar.setText("Logged in as " + currentUserName + " (" + currentRole + ")");
            switchToRole(currentRole);
            applyTheme(frame.getContentPane(), darkMode);
        });

        return loginPanel;
    }

    private void switchToRole(String role) {
        if ("Customer".equals(role)) {
            rootCardLayout.show(rootPanel, "customerMain");
        } else {
            rootCardLayout.show(rootPanel, "deliveryMain");
        }
    }

    /* ===== CUSTOMER UI ===== */
    private JPanel buildCustomerMain() {
        customerPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton scanQrBtn = new JButton("Scan QR (enter code)");
        JButton trackBtn = new JButton("Track Selected Product");
        JButton supportBtn = new JButton("Support / Chat");
        top.add(scanQrBtn);
        top.add(trackBtn);
        top.add(supportBtn);
        customerPanel.add(top, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.35);

        JList<String> productList = new JList<>(customerProductListModel);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane leftScroll = new JScrollPane(productList);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Your Products / Scans"));
        split.setLeftComponent(leftScroll);

        JPanel rightDetail = new JPanel(new BorderLayout());
        rightDetail.setBorder(new EmptyBorder(6, 6, 6, 6));
        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Product Details"));
        rightDetail.add(detailScroll, BorderLayout.CENTER);

        JPanel mapHolder = new JPanel(new BorderLayout());
        mapHolder.setBorder(BorderFactory.createTitledBorder("Tracking Map (simulated)"));
        MapPanel mapPanel = new MapPanel();
        mapHolder.add(mapPanel, BorderLayout.CENTER);
        rightDetail.add(mapHolder, BorderLayout.SOUTH);

        split.setRightComponent(rightDetail);
        customerPanel.add(split, BorderLayout.CENTER);

        productList.addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                String sel = productList.getSelectedValue();
                if (sel == null) return;
                String id = sel.split(" — ")[0].trim();
                Product p = products.get(id);
                if (p != null) {
                    detailArea.setText(renderProductDetails(p));
                    mapPanel.setMarker(p.lat, p.lon);
                }
            }
        });

        scanQrBtn.addActionListener(e -> {
            String code = JOptionPane.showInputDialog(frame, "Enter QR code string to scan (paste):", "Scan QR", JOptionPane.PLAIN_MESSAGE);
            if (code != null) {
                Product p = findProductByQr(code);
                if (p != null) {
                    detailArea.setText(renderProductDetails(p));
                    mapPanel.setMarker(p.lat, p.lon);
                    statusBar.setText("Product " + p.id + " validated on blockchain.");
                } else {
                    JOptionPane.showMessageDialog(frame, "QR not recognized / product not found on chain.", "Scan Result", JOptionPane.WARNING_MESSAGE);
                    statusBar.setText("Scan returned no product.");
                }
            }
        });

        trackBtn.addActionListener(e -> {
            String sel = productList.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(frame, "Select a product from the list to track.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String id = sel.split(" — ")[0].trim();
            Product p = products.get(id);
            if (p != null) {
                detailArea.setText(renderProductDetails(p));
                mapPanel.setMarker(p.lat, p.lon);
                JOptionPane.showMessageDialog(frame, "Opened tracking map for " + p.id, "Tracking", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        supportBtn.addActionListener(e -> openSupportChatDialog());

        return customerPanel;
    }

    private String renderProductDetails(Product p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Product: ").append(p.name).append("\n");
        sb.append("ID: ").append(p.id).append("\n");
        sb.append("Batch: ").append(p.batchNo).append("\n");
        sb.append("Manufacturer: ").append(p.manufacturer).append("\n");
        sb.append("Distributor: ").append(p.distributor).append("\n");
        sb.append("Retailer: ").append(p.retailer).append("\n");
        sb.append("Current Status: ").append(p.status).append("\n");
        sb.append("Assigned Delivery Person: ").append(p.assignedDelivery).append("\n");
        sb.append("Last Known Location: ").append(String.format("%.5f, %.5f", p.lat, p.lon)).append("\n\n");
        sb.append("Blockchain Transaction Timeline:\n");
        for (String t : p.timeline) {
            sb.append(" - ").append(t).append("\n");
        }
        return sb.toString();
    }

    private void openSupportChatDialog() {
        JDialog d = new JDialog(frame, "Support Chat", true);
        d.setSize(520, 520);
        d.setLayout(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(6, 6, 6, 6));
        top.add(new JLabel("Customer Support — Chat"), BorderLayout.WEST);
        JButton reportBtn = new JButton("Report Fake Product");
        top.add(reportBtn, BorderLayout.EAST);
        d.add(top, BorderLayout.NORTH);

        JList<String> chatList = new JList<>(chatModel);
        JScrollPane chatScroll = new JScrollPane(chatList);
        d.add(chatScroll, BorderLayout.CENTER);

        JPanel sendPanel = new JPanel(new BorderLayout());
        JTextField msgField = new JTextField();
        JButton sendBtn = new JButton("Send");
        sendPanel.add(msgField, BorderLayout.CENTER);
        sendPanel.add(sendBtn, BorderLayout.EAST);
        d.add(sendPanel, BorderLayout.SOUTH);

        if (chatModel.getSize() == 0) {
            chatModel.addElement("Support: Hello! How can we help you today?");
        }

        sendBtn.addActionListener(e -> {
            String txt = msgField.getText().trim();
            if (txt.isEmpty()) return;
            chatModel.addElement("You: " + txt);
            chatModel.addElement("Support: Thanks for the message — we'll look into it.");
            msgField.setText("");
        });

        reportBtn.addActionListener(e -> {
            String pid = JOptionPane.showInputDialog(d, "Enter product ID to report as fake:", "Report Fake", JOptionPane.PLAIN_MESSAGE);
            if (pid != null && products.containsKey(pid.trim())) {
                Product p = products.get(pid.trim());
                p.flagged = true;
                p.timeline.add(timestamp() + " - FLAGGED BY CUSTOMER");
                ledger.add(timestamp() + " - FLAGGED - " + p.id + " reported by " + currentUserName);
                rebuildLists();
                JOptionPane.showMessageDialog(d, "Product " + pid + " flagged. Support will review.", "Reported", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(d, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    /* ===== DELIVERY UI ===== */
    private JPanel buildDeliveryMain() {
        deliveryPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewAssignedBtn = new JButton("My Deliveries");
        JButton genQrBtn = new JButton("Generate QR for Selected");
        JButton updateBtn = new JButton("Scan & Update Status");
        top.add(viewAssignedBtn);
        top.add(genQrBtn);
        top.add(updateBtn);
        deliveryPanel.add(top, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.45);

        JList<String> deliveryList = new JList<>(deliveryProductListModel);
        deliveryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane leftScroll = new JScrollPane(deliveryList);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Assigned Deliveries"));
        split.setLeftComponent(leftScroll);

        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBorder(BorderFactory.createTitledBorder("QR / Update Panel"));
        JLabel qrImageLabel = new JLabel();
        qrImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qrImageLabel.setPreferredSize(new Dimension(240, 240));
        qrPanel.add(qrImageLabel, BorderLayout.CENTER);

        JPanel qrTextPanel = new JPanel(new GridLayout(4, 1));
        JLabel qrIdLabel = new JLabel("Product ID:");
        JLabel qrStatusLabel = new JLabel("Status:");
        JTextField qrScanField = new JTextField();
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Picked Up", "In Transit", "Delivered"});
        qrTextPanel.add(qrIdLabel);
        qrTextPanel.add(qrStatusLabel);
        qrTextPanel.add(qrScanField);
        qrTextPanel.add(statusCombo);
        qrPanel.add(qrTextPanel, BorderLayout.SOUTH);

        right.add(qrPanel, BorderLayout.CENTER);

        JButton submitUpdateBtn = new JButton("Submit Update to Blockchain");
        right.add(submitUpdateBtn, BorderLayout.SOUTH);

        split.setRightComponent(right);
        deliveryPanel.add(split, BorderLayout.CENTER);

        deliveryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = deliveryList.getSelectedValue();
                if (sel == null) return;
                String id = sel.split(" — ")[0].trim();
                Product p = products.get(id);
                if (p != null) {
                    String qr = p.generateQrString();
                    qrImageLabel.setIcon(new ImageIcon(generateQrImage(qr, 220, 220)));
                    qrIdLabel.setText("Product ID: " + p.id + " | " + p.name);
                    qrStatusLabel.setText("Status: " + p.status);
                    qrScanField.setText(qr);
                }
            }
        });

        genQrBtn.addActionListener(e -> {
            String sel = deliveryList.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(frame, "Select a product to generate QR for.", "Select Product", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String id = sel.split(" — ")[0].trim();
            Product p = products.get(id);
            String qr = p.generateQrString();
            qrImageLabel.setIcon(new ImageIcon(generateQrImage(qr, 220, 220)));
            qrScanField.setText(qr);
            statusBar.setText("QR generated for " + p.id);
        });

        updateBtn.addActionListener(e -> {
            String code = JOptionPane.showInputDialog(frame, "Enter QR scanned (paste) to update status:", "Scan & Update", JOptionPane.PLAIN_MESSAGE);
            if (code != null) {
                Product p = findProductByQr(code);
                if (p != null) {
                    String[] opts = {"Picked Up", "In Transit", "Delivered"};
                    String status = (String) JOptionPane.showInputDialog(frame, "Choose new status for " + p.id, "Update Status", JOptionPane.PLAIN_MESSAGE, null, opts, opts[1]);
                    if (status != null) {
                        performDeliveryUpdate(p, status, currentUserName);
                        rebuildLists();
                        JOptionPane.showMessageDialog(frame, "Status updated: " + p.id + " -> " + status, "Updated", JOptionPane.INFORMATION_MESSAGE);
                        statusBar.setText("Updated " + p.id + " to " + status);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "QR not recognized.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        submitUpdateBtn.addActionListener(e -> {
            String sel = deliveryList.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(frame, "Select a product to update.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String id = sel.split(" — ")[0].trim();
            Product p = products.get(id);
            if (p == null) return;
            String scanned = qrScanField.getText().trim();
            if (scanned.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Provide QR text in the QR scan field (or generate first).", "No QR", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!scanned.equals(p.generateQrString())) {
                JOptionPane.showMessageDialog(frame, "Scanned QR does not match selected product. Please ensure correct QR.", "QR Mismatch", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String status = (String) statusCombo.getSelectedItem();
            performDeliveryUpdate(p, status, currentUserName);
            rebuildLists();
            qrStatusLabel.setText("Status: " + p.status);
            JOptionPane.showMessageDialog(frame, "Update submitted for " + p.id, "Submitted", JOptionPane.INFORMATION_MESSAGE);
        });

        viewAssignedBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            for (Product p : products.values()) {
                if (p.assignedDelivery != null && p.assignedDelivery.equalsIgnoreCase(currentUserName)) {
                    sb.append(p.id).append(" - ").append(p.name).append(" (").append(p.status).append(")\n");
                }
            }
            if (sb.length() == 0) sb.append("No deliveries assigned to ").append(currentUserName);
            JOptionPane.showMessageDialog(frame, sb.toString(), "Assigned Deliveries", JOptionPane.INFORMATION_MESSAGE);
        });

        return deliveryPanel;
    }

    private void performDeliveryUpdate(Product p, String status, String actor) {
        String t = timestamp() + " - " + status.toUpperCase() + " - " + p.id + " by " + actor;
        p.status = status;
        p.timeline.add(t);
        ledger.add(t);
        double dx = (Math.random() - 0.5) * 0.0015;
        double dy = (Math.random() - 0.5) * 0.0015;
        p.lat += dx;
        p.lon += dy;
    }

    private void showLedgerDialog() {
        JDialog d = new JDialog(frame, "Blockchain Ledger (simulated)", true);
        d.setSize(600, 400);
        ledgerArea.setEditable(false);
        StringBuilder sb = new StringBuilder();
        for (String s : ledger) sb.append(s).append("\n");
        ledgerArea.setText(sb.toString());
        JScrollPane sp = new JScrollPane(ledgerArea);
        d.add(sp, BorderLayout.CENTER);
        JButton close = new JButton("Close");
        close.addActionListener(e -> d.dispose());
        d.add(close, BorderLayout.SOUTH);
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private Product findProductByQr(String qr) {
        for (Product p : products.values()) {
            if (p.generateQrString().equals(qr)) return p;
        }
        return null;
    }

    private BufferedImage generateQrImage(String text, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        byte[] bytes = sha256(text);
        Random rnd = new Random(Arrays.hashCode(bytes));
        int grid = 21;
        int cell = Math.min(w, h) / grid;
        for (int y = 0; y < grid; y++) {
            for (int x = 0; x < grid; x++) {
                boolean black = rnd.nextBoolean();
                g.setColor(black ? Color.BLACK : Color.WHITE);
                g.fillRect(x * cell, y * cell, cell, cell);
            }
        }
        g.setColor(new Color(0, 120, 215));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(text.length() > 8 ? text.substring(0, 8) : text, 8, h - 8);
        g.dispose();
        return img;
    }

    private byte[] sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(s.getBytes("UTF-8"));
        } catch (Exception ex) {
            return s.getBytes();
        }
    }

    // Recursively apply theme colors to component tree
    private void applyTheme(Component comp, boolean dark) {
        Color bg = dark ? DARK_BG : LIGHT_BG;
        Color fg = dark ? DARK_FG : LIGHT_FG;

        // Panels, scroll panes, split panes
        if (comp instanceof JPanel || comp instanceof JScrollPane || comp instanceof JSplitPane) {
            comp.setBackground(bg);
        }
        if (comp instanceof JComponent) {
            comp.setForeground(fg);
        }

        // Specific components
        if (comp instanceof JLabel || comp instanceof JButton
                || comp instanceof JTextField || comp instanceof JTextArea
                || comp instanceof JComboBox || comp instanceof JList) {
            comp.setBackground(bg);
            comp.setForeground(fg);

            if (comp instanceof JTextField || comp instanceof JTextArea) {
                ((JComponent) comp).setBorder(BorderFactory.createLineBorder(dark ? Color.GRAY : Color.LIGHT_GRAY));
            }
        }

        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyTheme(child, dark);
            }
        }
    }

    private static class MapPanel extends JPanel {
        private double markerLat = 0;
        private double markerLon = 0;
        private boolean hasMarker = false;

        public MapPanel() {
            setPreferredSize(new Dimension(400, 160));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }

        public void setMarker(double lat, double lon) {
            this.markerLat = lat;
            this.markerLon = lon;
            this.hasMarker = true;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(242, 248, 255));
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(220, 230, 240));
            for (int i = 0; i < w; i += 30) g2.fillRect(i, h/3, 8, h/3);

            if (hasMarker) {
                int x = w/2 + (int) ((markerLon - 77.5946) * 50000);
                int y = h/2 - (int) ((markerLat - 12.9716) * 50000);
                g2.setColor(Color.RED);
                g2.fillOval(Math.max(4, Math.min(w-18, x)), Math.max(4, Math.min(h-18, y)), 14, 14);
                g2.setColor(Color.BLACK);
                g2.drawString("Package", Math.max(4, Math.min(w-60, x)), Math.max(14, Math.min(h-8, y-6)));
            } else {
                g2.setColor(Color.GRAY);
                g2.drawString("No tracking available", 10, 20);
            }
        }
    }

    private static class Product {
        String id;
        String name;
        String batchNo;
        String manufacturer;
        String distributor;
        String retailer;
        String status = "Registered";
        String assignedDelivery;
        double lat, lon;
        List<String> timeline = new ArrayList<>();
        boolean flagged = false;

        Product(String id, String name, String manufacturer, String distributor, String retailer, String assignedDelivery, double lat, double lon) {
            this.id = id;
            this.name = name;
            this.batchNo = "BATCH" + (1000 + (int) (Math.random() * 9000));
            this.manufacturer = manufacturer;
            this.distributor = distributor;
            this.retailer = retailer;
            this.assignedDelivery = assignedDelivery;
            this.lat = lat;
            this.lon = lon;
            timeline.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - CREATED - " + id);
        }

        String generateQrString() {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                String base = id + "|" + name + "|" + batchNo;
                byte[] digest = md.digest(base.getBytes("UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 12 && i < digest.length; i++) {
                    sb.append(String.format("%02x", digest[i]));
                }
                return "SS-" + sb.toString().toUpperCase();
            } catch (Exception ex) {
                return "SS-" + id;
            }
        }
    }
}
