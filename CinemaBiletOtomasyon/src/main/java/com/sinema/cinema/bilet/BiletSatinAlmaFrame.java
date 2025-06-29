package com.sinema.bilet;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BiletSatinAlmaFrame extends JFrame {
    private MainFrame parentFrame;
    private int seansId;
    private JTextField txtAd;
    private JTextField txtSoyad;
    private JLabel lblFilmAdi;
    private JLabel lblSalonAdi;
    private JLabel lblSeansBilgisi;
    private JComboBox<String> koltukComboBox;
    private String filmAdi;
    private String salonAdi;
    private String seansBilgisi;
    private String kullaniciAdi;

    public BiletSatinAlmaFrame(MainFrame parentFrame, int seansId, String kullaniciAdi) {
        this.parentFrame = parentFrame;
        this.seansId = seansId;
        this.kullaniciAdi = kullaniciAdi;

        // Frame ayarları
        setTitle("Bilet Satın Alma");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Seans bilgilerini yükle
        loadSeansBilgileri();
        
        // Arayüz bileşenlerini oluştur
        initializeComponents();
        
        // Boş koltukları yükle
        loadBosKoltuklar();
    }

    private void loadSeansBilgileri() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT f.film_adi, s.salon_adi, se.tarih, se.saat " +
                        "FROM seanslar se " +
                        "JOIN filmler f ON se.film_id = f.film_id " +
                        "JOIN salonlar s ON se.salon_id = s.salon_id " +
                        "WHERE se.seans_id = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, seansId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                filmAdi = rs.getString("film_adi");
                salonAdi = rs.getString("salon_adi");
                seansBilgisi = rs.getString("tarih") + " " + rs.getString("saat");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Seans bilgileri yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeComponents() {
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form paneli
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Film bilgileri (read-only)
        gbc.gridy = 0;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Film:"), gbc);
        gbc.gridx = 1;
        lblFilmAdi = new JLabel(filmAdi);
        formPanel.add(lblFilmAdi, gbc);

        // Salon bilgileri
        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Salon:"), gbc);
        gbc.gridx = 1;
        lblSalonAdi = new JLabel(salonAdi);
        formPanel.add(lblSalonAdi, gbc);

        // Seans bilgileri
        gbc.gridy = 2;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Seans:"), gbc);
        gbc.gridx = 1;
        lblSeansBilgisi = new JLabel(seansBilgisi);
        formPanel.add(lblSeansBilgisi, gbc);

        // Ad
        gbc.gridy = 3;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Ad:"), gbc);
        gbc.gridx = 1;
        txtAd = new JTextField(20);
        formPanel.add(txtAd, gbc);

        // Soyad
        gbc.gridy = 4;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Soyad:"), gbc);
        gbc.gridx = 1;
        txtSoyad = new JTextField(20);
        formPanel.add(txtSoyad, gbc);

        // Koltuk seçimi
        gbc.gridy = 5;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Koltuk:"), gbc);
        gbc.gridx = 1;
        koltukComboBox = new JComboBox<>();
        formPanel.add(koltukComboBox, gbc);

        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnBiletAl = new JButton("Bilet Al");
        JButton btnGeriDon = new JButton("Geri Dön");
        
        styleButton(btnBiletAl);
        styleButton(btnGeriDon);
        
        btnBiletAl.addActionListener(e -> biletAl());
        btnGeriDon.addActionListener(e -> geriDon());
        
        buttonPanel.add(btnGeriDon);
        buttonPanel.add(btnBiletAl);

        // Panelleri ana panele ekle
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Frame'e ekle
        add(mainPanel);
    }

    private void loadBosKoltuklar() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Önce salon kapasitesini al
            String kapasite_sql = "SELECT s.kapasite " +
                                "FROM salonlar s " +
                                "JOIN seanslar se ON s.salon_id = se.salon_id " +
                                "WHERE se.seans_id = ?";
            
            PreparedStatement kapasitePstmt = conn.prepareStatement(kapasite_sql);
            kapasitePstmt.setInt(1, seansId);
            ResultSet kapasiteRs = kapasitePstmt.executeQuery();
            
            if (kapasiteRs.next()) {
                int kapasite = kapasiteRs.getInt("kapasite");
                
                // Dolu koltukları al
                String dolu_sql = "SELECT koltuk_no FROM biletler WHERE seans_id = ? AND durum = 1";
                PreparedStatement doluPstmt = conn.prepareStatement(dolu_sql);
                doluPstmt.setInt(1, seansId);
                ResultSet doluRs = doluPstmt.executeQuery();
                
                boolean[] doluKoltuklar = new boolean[kapasite + 1];
                while (doluRs.next()) {
                    doluKoltuklar[doluRs.getInt("koltuk_no")] = true;
                }
                
                // Boş koltukları combo box'a ekle
                koltukComboBox.removeAllItems();
                koltukComboBox.addItem("Seçiniz...");
                for (int i = 1; i <= kapasite; i++) {
                    if (!doluKoltuklar[i]) {
                        koltukComboBox.addItem(String.valueOf(i));
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Boş koltuklar yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

  private void biletAl() {
    // Form kontrolü
    if (txtAd.getText().trim().isEmpty() || 
        txtSoyad.getText().trim().isEmpty() || 
        koltukComboBox.getSelectedItem() == null || 
        koltukComboBox.getSelectedItem().equals("Seçiniz...")) {
        
        JOptionPane.showMessageDialog(this,
            "Lütfen tüm alanları doldurunuz!",
            "Uyarı",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    try (Connection conn = DatabaseConnection.getConnection()) {
        // Bilet oluştur
        String sql = "INSERT INTO biletler (seans_id, koltuk_no, ad_soyad, kullanici_adi, durum) " +
                    "VALUES (?, ?, ?, ?, 1)";
        
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, seansId);
        pstmt.setInt(2, Integer.parseInt((String) koltukComboBox.getSelectedItem()));
        // Ad ve soyadı birleştir
        pstmt.setString(3, txtAd.getText().trim() + " " + txtSoyad.getText().trim());
        pstmt.setString(4, kullaniciAdi);
        
        int affectedRows = pstmt.executeUpdate();
        
        if (affectedRows > 0) {
            JOptionPane.showMessageDialog(this,
                "Bilet başarıyla oluşturuldu!",
                "Başarılı",
                JOptionPane.INFORMATION_MESSAGE);
            geriDon();
        } else {
            JOptionPane.showMessageDialog(this,
                "Bilet oluşturulurken bir hata oluştu!",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Bilet oluşturulurken hata: " + e.getMessage(),
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