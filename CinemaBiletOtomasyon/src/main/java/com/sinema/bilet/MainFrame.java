package com.sinema.bilet;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainFrame extends JFrame {
    private JButton btnFilmler, btnSalonlar, btnSeanslar, btnBiletSatis, btnKullanicilar, 
            btnDashboard, btnCikis, btnIller, btnBiletIptal;
    private String kullaniciRol;
    private String kullaniciAdi;
    private JPanel centerPanel;

    public MainFrame(String kullaniciAdi, String rol) {
        this.kullaniciAdi = kullaniciAdi;
        this.kullaniciRol = rol;
        
        initializeFrame();
        initializeComponents();
        setupLayout();
    }

    public String getKullaniciAdi() {
        return this.kullaniciAdi;
    }

    private void initializeFrame() {
        setTitle("Sinema Bilet Otomasyonu - " + kullaniciAdi);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        // Butonları oluştur
        btnFilmler = new JButton("Filmler");
        btnSalonlar = new JButton("Salonlar");
        btnSeanslar = new JButton("Seanslar");
        btnBiletSatis = new JButton("Bilet Satış");
        btnKullanicilar = new JButton("Kullanıcılar");
        btnDashboard = new JButton("Dashboard");
        btnCikis = new JButton("Çıkış");
        btnIller = new JButton("İller");
        btnBiletIptal = new JButton("Bilet İptal");

        // Butonları stillendir
        styleButton(btnFilmler);
        styleButton(btnSalonlar);
        styleButton(btnSeanslar);
        styleButton(btnBiletSatis);
        styleButton(btnKullanicilar);
        styleButton(btnDashboard);
        styleButton(btnCikis);
        styleButton(btnIller);
        styleButton(btnBiletIptal);

        // Buton olaylarını ekle
        btnFilmler.addActionListener(e -> showFilmler());
        btnSalonlar.addActionListener(e -> showSalonlar());
        btnSeanslar.addActionListener(e -> showSeanslar());
        btnBiletSatis.addActionListener(e -> showBiletSatis());
        btnKullanicilar.addActionListener(e -> showKullaniciYonetimi());
        btnDashboard.addActionListener(e -> showDashboard());
        btnCikis.addActionListener(e -> cikisYap());
        btnIller.addActionListener(e -> showIller());
        btnBiletIptal.addActionListener(e -> showBiletIptal());
    }

    private void setupLayout() {
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Üst panel (yatay menü)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(btnDashboard);
        topPanel.add(btnCikis);

        // Merkez panel (ana menü)
        centerPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Kullanıcı rolüne göre butonları ekle
        centerPanel.add(btnFilmler);
        centerPanel.add(btnSeanslar);
        centerPanel.add(btnBiletSatis);
        centerPanel.add(btnBiletIptal);

        if ("ADMIN".equals(kullaniciRol)) {
            centerPanel.add(btnSalonlar);
            centerPanel.add(btnIller);
            centerPanel.add(btnKullanicilar);
        }

        // Panelleri ana frame'e ekle
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Ana panel'i frame'e ekle
        setContentPane(mainPanel);
    }

    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(150, 40));
        // Arka plan rengini açık gri yap
        button.setBackground(new Color(240, 240, 240));
        // Yazı rengini siyah yap
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        // Yazı fontunu Arial, BOLD ve biraz daha büyük yap
        button.setFont(new Font("Arial", Font.BOLD, 16));
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

    private void showFilmler() {
        if ("ADMIN".equals(kullaniciRol)) {
            FilmYonetimFrame filmFrame = new FilmYonetimFrame(this);
            filmFrame.setVisible(true);
            this.setVisible(false);
        } else {
            FilmListelemeFrame filmFrame = new FilmListelemeFrame(this);
            filmFrame.setVisible(true);
            this.setVisible(false);
        }
    }

    private void showSalonlar() {
        if ("ADMIN".equals(kullaniciRol)) {
            SalonYonetimFrame salonFrame = new SalonYonetimFrame(this);
            salonFrame.setVisible(true);
            this.setVisible(false);
        }
    }

    private void showSeanslar() {
        if ("ADMIN".equals(kullaniciRol)) {
            SeansYonetimFrame seansFrame = new SeansYonetimFrame(this);
            seansFrame.setVisible(true);
            this.setVisible(false);
        } else {
            SeansListelemeFrame seansFrame = new SeansListelemeFrame(this);
            seansFrame.setVisible(true);
            this.setVisible(false);
        }
    }

    private void showIller() {
        if ("ADMIN".equals(kullaniciRol)) {
            IlYonetimFrame ilFrame = new IlYonetimFrame(this);
            ilFrame.setVisible(true);
            this.setVisible(false);
        }
    }

    private void showBiletSatis() {
        FilmSecmeFrame filmSecmeFrame = new FilmSecmeFrame(this);
        filmSecmeFrame.setVisible(true);
        this.setVisible(false);
    }

    private void showKullaniciYonetimi() {
        if ("ADMIN".equals(kullaniciRol)) {
            UserManagementFrame userFrame = new UserManagementFrame(this);
            userFrame.setVisible(true);
            this.setVisible(false);
        }
    }

    private void showDashboard() {
        DashboardFrame dashboardFrame = new DashboardFrame(this);
        dashboardFrame.setVisible(true);
        this.setVisible(false);
    }

    private void showBiletIptal() {
        BiletIptalFrame biletIptalFrame = new BiletIptalFrame(this, kullaniciAdi);
        biletIptalFrame.setVisible(true);
        this.setVisible(false);
    }

    private void cikisYap() {
        int response = JOptionPane.showConfirmDialog(this,
            "Çıkış yapmak istediğinizden emin misiniz?",
            "Çıkış",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            this.dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        }
    }
}