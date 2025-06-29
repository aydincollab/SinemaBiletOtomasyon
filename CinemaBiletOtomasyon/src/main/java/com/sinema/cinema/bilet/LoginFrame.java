package com.sinema.bilet;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField txtKullaniciAdi;
    private JPasswordField txtSifre;
    private JButton btnGirisYap;
    private JButton btnKayitOl;

    public LoginFrame() {
        initializeFrame();
        initializeComponents();
        setupLayout();
        setVisible(true);
    }

    private void initializeFrame() {
        setTitle("Sinema Bilet Otomasyonu - Giriş");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initializeComponents() {
        // Metin alanlarını oluştur
        txtKullaniciAdi = new JTextField(20);
        txtSifre = new JPasswordField(20);

        // Butonları oluştur
        btnGirisYap = new JButton("Giriş Yap");
        btnKayitOl = new JButton("Kayıt Ol");
        
        // Buton yazı renklerini ayarla
        btnGirisYap.setFont(new Font("Arial", Font.BOLD, 14));
        btnKayitOl.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Yazı rengini siyah yap
        btnGirisYap.setForeground(Color.BLACK);
        btnKayitOl.setForeground(Color.BLACK);

        // Mouse events
        btnGirisYap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Mouse gelince yazıyı kalın yap
                Font currentFont = btnGirisYap.getFont();
                btnGirisYap.setFont(new Font(currentFont.getName(), Font.BOLD, currentFont.getSize()));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Mouse gidince yazıyı normal yap ve siyah renkte tut
                Font currentFont = btnGirisYap.getFont();
                btnGirisYap.setFont(new Font(currentFont.getName(), Font.PLAIN, currentFont.getSize()));
                btnGirisYap.setForeground(Color.BLACK);
            }
        });

        btnKayitOl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Mouse gelince yazıyı kalın yap
                Font currentFont = btnKayitOl.getFont();
                btnKayitOl.setFont(new Font(currentFont.getName(), Font.BOLD, currentFont.getSize()));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Mouse gidince yazıyı normal yap ve siyah renkte tut
                Font currentFont = btnKayitOl.getFont();
                btnKayitOl.setFont(new Font(currentFont.getName(), Font.PLAIN, currentFont.getSize()));
                btnKayitOl.setForeground(Color.BLACK);
            }
        });

        // Buton olaylarını ekle
        btnGirisYap.addActionListener(e -> girisYap());
        btnKayitOl.addActionListener(e -> kayitOl());
    }

    private void setupLayout() {
        // Ana panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Başlık
        JLabel lblBaslik = new JLabel("Sinema Bilet Otomasyonu");
        lblBaslik.setFont(new Font("Arial", Font.BOLD, 20));
        lblBaslik.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form paneli
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Kullanıcı adı alanı
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Kullanıcı Adı:"), gbc);

        gbc.gridx = 1;
        formPanel.add(txtKullaniciAdi, gbc);

        // Şifre alanı
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Şifre:"), gbc);

        gbc.gridx = 1;
        formPanel.add(txtSifre, gbc);

        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(btnGirisYap);
        buttonPanel.add(btnKayitOl);

        // Panelleri ana panele ekle
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(lblBaslik);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalGlue());

        // Ana paneli frame'e ekle
        setContentPane(mainPanel);
    }

    private void girisYap() {
        String kullaniciAdi = txtKullaniciAdi.getText();
        String sifre = new String(txtSifre.getPassword());

        if (kullaniciAdi.trim().isEmpty() || sifre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Lütfen kullanıcı adı ve şifre giriniz!",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT rol FROM kullanicilar WHERE kullanici_adi = ? AND sifre = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, kullaniciAdi);
            pstmt.setString(2, sifre);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String rol = rs.getString("rol");
                this.dispose();
                MainFrame mainFrame = new MainFrame(kullaniciAdi, rol);
                mainFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Kullanıcı adı veya şifre hatalı!",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Veritabanı hatası: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

   private void kayitOl() {
    this.dispose();
    RegisterFrame registerFrame = new RegisterFrame(this);  // LoginFrame'i parametre olarak geçiyoruz
    registerFrame.setVisible(true);
}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}