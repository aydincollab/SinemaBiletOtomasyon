package com.sinema.bilet;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class FilmSecmeFrame extends JFrame {
    private MainFrame parentFrame;
    private JComboBox<String> ilComboBox;
    private JComboBox<String> salonComboBox;
    private JComboBox<String> filmComboBox;
    private JComboBox<String> seansComboBox;
    private JLabel lblBosKoltukSayisi;
    private Map<String, Integer> illerMap;
    private Map<String, Integer> salonlarMap;
    private Map<String, Integer> filmlerMap;
    private Map<String, Integer> seanslarMap;
    private JButton btnDevamEt;
    private JButton btnGeriDon;

    public FilmSecmeFrame(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.illerMap = new HashMap<>();
        this.salonlarMap = new HashMap<>();
        this.filmlerMap = new HashMap<>();
        this.seanslarMap = new HashMap<>();

        initializeFrame();
        initializeComponents();
        loadIller(); // İlleri yükle
    }

    private void initializeFrame() {
        setTitle("Film Seçme");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        // Ana panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form paneli
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // İl seçimi
        gbc.gridy = 0;
        gbc.gridx = 0;
        formPanel.add(new JLabel("İl Seçin:"), gbc);
        
        gbc.gridx = 1;
        ilComboBox = new JComboBox<>();
        formPanel.add(ilComboBox, gbc);

        // Salon seçimi
        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Salon Seçin:"), gbc);
        
        gbc.gridx = 1;
        salonComboBox = new JComboBox<>();
        salonComboBox.setEnabled(false);
        formPanel.add(salonComboBox, gbc);

        // Film seçimi
        gbc.gridy = 2;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Film Seçin:"), gbc);
        
        gbc.gridx = 1;
        filmComboBox = new JComboBox<>();
        filmComboBox.setEnabled(false);
        formPanel.add(filmComboBox, gbc);

        // Seans seçimi
        gbc.gridy = 3;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Seans Seçin:"), gbc);
        
        gbc.gridx = 1;
        seansComboBox = new JComboBox<>();
        seansComboBox.setEnabled(false);
        formPanel.add(seansComboBox, gbc);

        // Boş koltuk bilgisi
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        lblBosKoltukSayisi = new JLabel("Boş koltuk sayısı: -");
        lblBosKoltukSayisi.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblBosKoltukSayisi, gbc);

        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnDevamEt = new JButton("Devam Et");
        btnGeriDon = new JButton("Geri Dön");
        
        styleButton(btnDevamEt);
        styleButton(btnGeriDon);
        
        buttonPanel.add(btnGeriDon);
        buttonPanel.add(btnDevamEt);

        // Event listeners
        ilComboBox.addActionListener(e -> ilSecildi());
        salonComboBox.addActionListener(e -> salonSecildi());
        filmComboBox.addActionListener(e -> filmSecildi());
        seansComboBox.addActionListener(e -> seansSecildi());
        btnGeriDon.addActionListener(e -> geriDon());
        btnDevamEt.addActionListener(e -> devamEt());

        // Ana panele ekle
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Frame'e ekle
        add(mainPanel);

        // Başlangıçta devam et butonu devre dışı
        btnDevamEt.setEnabled(false);
    }

    private void loadIller() {
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "İller yüklenirken hata oluştu: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ilSecildi() {
        String secilenIl = (String) ilComboBox.getSelectedItem();
        if (secilenIl == null || secilenIl.equals("Seçiniz...")) {
            salonComboBox.setEnabled(false);
            return;
        }

        // Salonları yükle
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT salon_id, salon_adi FROM salonlar WHERE il_id = ? AND durum = 1 ORDER BY salon_adi";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, illerMap.get(secilenIl));
            ResultSet rs = pstmt.executeQuery();

            salonComboBox.removeAllItems();
            salonComboBox.addItem("Seçiniz...");
            salonlarMap.clear();

            boolean salonVar = false;
            while (rs.next()) {
                salonVar = true;
                String salonAdi = rs.getString("salon_adi");
                int salonId = rs.getInt("salon_id");
                salonlarMap.put(salonAdi, salonId);
                salonComboBox.addItem(salonAdi);
            }

            salonComboBox.setEnabled(true);
            filmComboBox.setEnabled(false);
            seansComboBox.setEnabled(false);
            btnDevamEt.setEnabled(false);

            if (!salonVar) {
                JOptionPane.showMessageDialog(this,
                    "Seçilen ilde salon bulunmamaktadır!",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
                geriDon();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Salonlar yüklenirken hata oluştu: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salonSecildi() {
        String secilenSalon = (String) salonComboBox.getSelectedItem();
        if (secilenSalon == null || secilenSalon.equals("Seçiniz...")) {
            filmComboBox.setEnabled(false);
            return;
        }

        // Filmleri yükle
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT DISTINCT f.film_id, f.film_adi FROM filmler f " +
                        "JOIN seanslar s ON f.film_id = s.film_id " +
                        "WHERE s.salon_id = ? AND f.durum = 1 AND s.durum = 1 " +
                        "ORDER BY f.film_adi";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, salonlarMap.get(secilenSalon));
            ResultSet rs = pstmt.executeQuery();

            filmComboBox.removeAllItems();
            filmComboBox.addItem("Seçiniz...");
            filmlerMap.clear();

            boolean filmVar = false;
            while (rs.next()) {
                filmVar = true;
                String filmAdi = rs.getString("film_adi");
                int filmId = rs.getInt("film_id");
                filmlerMap.put(filmAdi, filmId);
                filmComboBox.addItem(filmAdi);
            }

            filmComboBox.setEnabled(true);
            seansComboBox.setEnabled(false);
            btnDevamEt.setEnabled(false);

            if (!filmVar) {
                JOptionPane.showMessageDialog(this,
                    "Seçilen salonda gösterimde film bulunmamaktadır!",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
                geriDon();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Filmler yüklenirken hata oluştu: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filmSecildi() {
        String secilenSalon = (String) salonComboBox.getSelectedItem();
        String secilenFilm = (String) filmComboBox.getSelectedItem();
        if (secilenFilm == null || secilenFilm.equals("Seçiniz...")) {
            seansComboBox.setEnabled(false);
            return;
        }

        // Seansları yükle
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT seans_id, tarih, saat FROM seanslar " +
                        "WHERE salon_id = ? AND film_id = ? AND durum = 1 " +
                        "ORDER BY tarih, saat";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, salonlarMap.get(secilenSalon));
            pstmt.setInt(2, filmlerMap.get(secilenFilm));
            ResultSet rs = pstmt.executeQuery();

            seansComboBox.removeAllItems();
            seansComboBox.addItem("Seçiniz...");
            seanslarMap.clear();

            boolean seansVar = false;
            while (rs.next()) {
                seansVar = true;
                String seansAdi = rs.getString("tarih") + " " + rs.getString("saat");
                int seansId = rs.getInt("seans_id");
                seanslarMap.put(seansAdi, seansId);
                seansComboBox.addItem(seansAdi);
            }

            seansComboBox.setEnabled(true);
            btnDevamEt.setEnabled(false);

            if (!seansVar) {
                JOptionPane.showMessageDialog(this,
                    "Seçilen film için uygun seans bulunmamaktadır!",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
                geriDon();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Seanslar yüklenirken hata oluştu: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seansSecildi() {
        String secilenSeans = (String) seansComboBox.getSelectedItem();
        if (secilenSeans == null || secilenSeans.equals("Seçiniz...")) {
            btnDevamEt.setEnabled(false);
            return;
        }

        // Boş koltuk sayısını kontrol et
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.kapasite - COUNT(b.bilet_id) as bos_koltuk " +
                        "FROM salonlar s " +
                        "LEFT JOIN seanslar se ON s.salon_id = se.salon_id " +
                        "LEFT JOIN biletler b ON se.seans_id = b.seans_id AND b.durum = 1 " +
                        "WHERE se.seans_id = ? " +
                        "GROUP BY s.salon_id, s.kapasite";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, seanslarMap.get(secilenSeans));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int bosKoltuk = rs.getInt("bos_koltuk");
                lblBosKoltukSayisi.setText("Boş koltuk sayısı: " + bosKoltuk);
                
                if (bosKoltuk > 0) {
                    btnDevamEt.setEnabled(true);
                } else {
                    btnDevamEt.setEnabled(false);
                    JOptionPane.showMessageDialog(this,
                        "Bu seansta boş koltuk kalmamıştır!",
                        "Uyarı",
                        JOptionPane.WARNING_MESSAGE);
                    geriDon();
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Koltuk bilgisi kontrol edilirken hata oluştu: " + e.getMessage(),
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

    private void devamEt() {
    // Seçilen değerleri al
    String secilenSeans = (String) seansComboBox.getSelectedItem();
    int seansId = seanslarMap.get(secilenSeans);
    
    // Bilet satın alma ekranına yönlendir
    this.dispose();
    BiletSatinAlmaFrame frame = new BiletSatinAlmaFrame(parentFrame, seansId, parentFrame.getKullaniciAdi());
    frame.setVisible(true);
}
}