package com.sinema.bilet;

import javax.swing.*;
import javax.swing.border.TitledBorder; 
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {
    private MainFrame parentFrame;
    private JLabel lblSalonSayisi, lblIlSayisi;
    private JTable sonFilmlerTable;
    private DefaultTableModel filmTableModel;
    private JComboBox<String> ilComboBox;
    private JComboBox<String> salonComboBox;
    private JComboBox<String> filmComboBox;
    private JLabel lblIldekiSalonSayisi;
    private JLabel lblBosKoltukSayisi;
    private Map<String, Integer> illerMap;
    private Map<String, Integer> salonlarMap;
    private Map<String, Integer> filmlerMap;

    public DashboardFrame(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.illerMap = new HashMap<>();
        this.salonlarMap = new HashMap<>();
        this.filmlerMap = new HashMap<>();

        // Frame ayarları
        setTitle("Dashboard");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Üst panel - Genel İstatistikler
        JPanel topPanel = createTopPanel();

        // Orta panel - Son Eklenen Filmler
        JPanel centerPanel = createCenterPanel();

        // Alt panel - Detaylı İstatistikler
        JPanel bottomPanel = createBottomPanel();

        // Geri dön butonu paneli
        JPanel buttonPanel = createButtonPanel();

        // Panel yerleşimi
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(topPanel, BorderLayout.CENTER);
        topContainer.add(buttonPanel, BorderLayout.SOUTH);

        // Panelleri ana panele ekle
        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Ana paneli frame'e ekle
        add(mainPanel);

        // Verileri yükle
        loadData();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
    BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
    "Genel İstatistikler",
    javax.swing.border.TitledBorder.CENTER,
    javax.swing.border.TitledBorder.TOP,
    new Font("Arial", Font.BOLD, 14)
));

        // Salon sayısı
        JPanel salonPanel = createStatPanel("Toplam Salon Sayısı");
        lblSalonSayisi = new JLabel("0", SwingConstants.CENTER);
        styleStatLabel(lblSalonSayisi);
        salonPanel.add(lblSalonSayisi, BorderLayout.CENTER);

        // İl sayısı
        JPanel ilPanel = createStatPanel("Kayıtlı İl Sayısı");
        lblIlSayisi = new JLabel("0", SwingConstants.CENTER);
        styleStatLabel(lblIlSayisi);
        ilPanel.add(lblIlSayisi, BorderLayout.CENTER);

        panel.add(salonPanel);
        panel.add(ilPanel);

        return panel;
    }

    private JPanel createStatPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(51, 51, 51));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        return panel;
    }

    private void styleStatLabel(JLabel label) {
        label.setFont(new Font("Arial", Font.BOLD, 36));
        label.setForeground(new Color(0, 123, 255));
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Son 10 Günde Eklenen Filmler",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));

        // Tablo modeli
        String[] columnNames = {"Film Adı", "Yönetmen", "Süre (dk)", "Eklenme Tarihi"};
        filmTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Tablo
        sonFilmlerTable = new JTable(filmTableModel);
        sonFilmlerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        sonFilmlerTable.setFont(new Font("Arial", Font.PLAIN, 12));
        sonFilmlerTable.setRowHeight(25);
        sonFilmlerTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        sonFilmlerTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        sonFilmlerTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        sonFilmlerTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(sonFilmlerTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Detaylı İstatistikler",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // İl seçimi
        addLabelAndComponent(panel, "İl Seçin:", ilComboBox = new JComboBox<>(), 0, gbc);
        lblIldekiSalonSayisi = new JLabel("Bu ilde 0 salon var", SwingConstants.LEFT);
        styleInfoLabel(lblIldekiSalonSayisi);
        gbc.gridx = 2;
        panel.add(lblIldekiSalonSayisi, gbc);

        // Salon seçimi
        gbc.gridy++;
        addLabelAndComponent(panel, "Salon Seçin:", salonComboBox = new JComboBox<>(), gbc.gridy, gbc);

        // Film seçimi
        gbc.gridy++;
        addLabelAndComponent(panel, "Film Seçin:", filmComboBox = new JComboBox<>(), gbc.gridy, gbc);
        lblBosKoltukSayisi = new JLabel("Boş koltuk sayısı: 0", SwingConstants.LEFT);
        styleInfoLabel(lblBosKoltukSayisi);
        gbc.gridx = 2;
        panel.add(lblBosKoltukSayisi, gbc);

        // ComboBox olayları
        ilComboBox.addActionListener(e -> ilSecildi());
        salonComboBox.addActionListener(e -> salonSecildi());
        filmComboBox.addActionListener(e -> filmSecildi());

        // ComboBox stilleri
        styleComboBox(ilComboBox);
        styleComboBox(salonComboBox);
        styleComboBox(filmComboBox);

        return panel;
    }

    private void addLabelAndComponent(JPanel panel, String labelText, JComponent component, int gridy, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = gridy;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
        gbc.weightx = 0.0;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setPreferredSize(new Dimension(200, 25));
        comboBox.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void styleInfoLabel(JLabel label) {
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(0, 123, 255));
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGeri = new JButton("Geri Dön");
        styleButton(btnGeri);
        btnGeri.addActionListener(e -> geriDon());
        panel.add(btnGeri);
        return panel;
    }

    private void loadData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Salon sayısını yükle
            String salonSql = "SELECT COUNT(*) as count FROM salonlar WHERE durum = 1";
            Statement salonStmt = conn.createStatement();
            ResultSet salonRs = salonStmt.executeQuery(salonSql);
            if (salonRs.next()) {
                lblSalonSayisi.setText(String.valueOf(salonRs.getInt("count")));
            }

            // İl sayısını yükle
            String ilSql = "SELECT COUNT(*) as count FROM iller WHERE durum = 1";
            Statement ilStmt = conn.createStatement();
            ResultSet ilRs = ilStmt.executeQuery(ilSql);
            if (ilRs.next()) {
                lblIlSayisi.setText(String.valueOf(ilRs.getInt("count")));
            }

            // Son 10 günde eklenen filmleri yükle
            String filmSql = "SELECT film_adi, yonetmen, sure, eklenme_tarihi FROM filmler " +
                           "WHERE eklenme_tarihi >= date('now', '-10 days') AND durum = 1 " +
                           "ORDER BY eklenme_tarihi DESC";
            Statement filmStmt = conn.createStatement();
            ResultSet filmRs = filmStmt.executeQuery(filmSql);

            filmTableModel.setRowCount(0);
            while (filmRs.next()) {
                Object[] row = {
                    filmRs.getString("film_adi"),
                    filmRs.getString("yonetmen"),
                    filmRs.getInt("sure"),
                    filmRs.getString("eklenme_tarihi")
                };
                filmTableModel.addRow(row);
            }

            // İlleri yükle
            loadIller();

            // Filmleri yükle
            loadFilmler();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Veriler yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadIller() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT il_id, il_adi FROM iller WHERE durum = 1 ORDER BY il_adi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            ilComboBox.removeAllItems();
            ilComboBox.addItem("Seçiniz...");
            illerMap.clear();

            while (rs.next()) {
                String ilAdi = rs.getString("il_adi");
                int ilId = rs.getInt("il_id");
                illerMap.put(ilAdi, ilId);
                ilComboBox.addItem(ilAdi);
            }
        }
    }

    private void loadFilmler() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT film_id, film_adi FROM filmler WHERE durum = 1 ORDER BY film_adi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            filmComboBox.removeAllItems();
            filmComboBox.addItem("Seçiniz...");
            filmlerMap.clear();

            while (rs.next()) {
                String filmAdi = rs.getString("film_adi");
                int filmId = rs.getInt("film_id");
                filmlerMap.put(filmAdi, filmId);
                filmComboBox.addItem(filmAdi);
            }
        }
    }

    private void ilSecildi() {
        String secilenIl = (String) ilComboBox.getSelectedItem();
        if (secilenIl == null || secilenIl.equals("Seçiniz...")) {
            lblIldekiSalonSayisi.setText("Bu ilde 0 salon var");
            salonComboBox.removeAllItems();
            salonComboBox.addItem("Seçiniz...");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // İldeki salon sayısını bul
            String salonSayisiSql = "SELECT COUNT(*) as count FROM salonlar WHERE il_id = ? AND durum = 1";
            PreparedStatement pstmt = conn.prepareStatement(salonSayisiSql);
            pstmt.setInt(1, illerMap.get(secilenIl));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int salonSayisi = rs.getInt("count");
                lblIldekiSalonSayisi.setText("Bu ilde " + salonSayisi + " salon var");
            }

            // İldeki salonları yükle
            loadSalonlar(illerMap.get(secilenIl));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Salon bilgileri yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSalonlar(int ilId) throws SQLException {
        String sql = "SELECT salon_id, salon_adi FROM salonlar WHERE il_id = ? AND durum = 1 ORDER BY salon_adi";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ilId);
            ResultSet rs = pstmt.executeQuery();

            salonComboBox.removeAllItems();
            salonComboBox.addItem("Seçiniz...");
            salonlarMap.clear();

            while (rs.next()) {
                String salonAdi = rs.getString("salon_adi");
                int salonId = rs.getInt("salon_id");
                salonlarMap.put(salonAdi, salonId);
                salonComboBox.addItem(salonAdi);
            }
        }
    }

    private void salonSecildi() {
        filmSecildi(); // Salon değiştiğinde boş koltuk sayısını güncelle
    }

    private void filmSecildi() {
        String secilenSalon = (String) salonComboBox.getSelectedItem();
        String secilenFilm = (String) filmComboBox.getSelectedItem();

        if (secilenSalon == null || secilenSalon.equals("Seçiniz...") ||
            secilenFilm == null || secilenFilm.equals("Seçiniz...")) {
            lblBosKoltukSayisi.setText("Boş koltuk sayısı: 0");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.kapasite - COALESCE((SELECT COUNT(*) FROM biletler b " +
                        "JOIN seanslar se2 ON b.seans_id = se2.seans_id " +
                        "WHERE se2.salon_id = s.salon_id AND se2.film_id = ? AND b.durum = 1), 0) as bos_koltuk " +
                        "FROM salonlar s " +
                        "WHERE s.salon_id = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, filmlerMap.get(secilenFilm));
            pstmt.setInt(2, salonlarMap.get(secilenSalon));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int bosKoltuk = rs.getInt("bos_koltuk");
                lblBosKoltukSayisi.setText("Boş koltuk sayısı: " + bosKoltuk);
            } else {
                lblBosKoltukSayisi.setText("Bu film için seans bulunamadı");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Koltuk bilgileri yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(120, 35));
        button.setBackground(new Color(0, 123, 255));
        // Yazı rengini siyah yap
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Mouse gelince yazıyı kalın yap
                Font currentFont = button.getFont();
                button.setFont(new Font(currentFont.getName(), Font.BOLD, currentFont.getSize()));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Mouse gidince yazıyı normal yap ve siyah renkte tut
                Font currentFont = button.getFont();
                button.setFont(new Font(currentFont.getName(), Font.PLAIN, currentFont.getSize()));
                button.setForeground(Color.BLACK);
            }
        });
    }

    private void geriDon() {
        this.dispose();
        parentFrame.setVisible(true);
    }
}