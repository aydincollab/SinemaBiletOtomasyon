package com.sinema.bilet;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterFrame extends JFrame {
    private JTextField txtKullaniciAdi;
    private JTextField txtAdSoyad;
    private JPasswordField txtSifre;
    private JPasswordField txtSifreTekrar;
    private JButton btnKaydet;
    private JButton btnIptal;
    
    public RegisterFrame(LoginFrame parentFrame) {
        // Frame ayarları
        setTitle("Kullanıcı Kayıt");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Ana panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));
        
        // Başlık
        JLabel lblBaslik = new JLabel("Yeni Kullanıcı Kaydı", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Form paneli
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Ad Soyad
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Ad Soyad:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        txtAdSoyad = new JTextField(20);
        formPanel.add(txtAdSoyad, gbc);
        
        // Kullanıcı adı
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Kullanıcı Adı:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        txtKullaniciAdi = new JTextField(20);
        formPanel.add(txtKullaniciAdi, gbc);
        
        // Şifre
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Şifre:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        txtSifre = new JPasswordField(20);
        formPanel.add(txtSifre, gbc);
        
        // Şifre Tekrar
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Şifre Tekrar:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        txtSifreTekrar = new JPasswordField(20);
        formPanel.add(txtSifreTekrar, gbc);
        
        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        btnKaydet = new JButton("Kaydet");
        btnIptal = new JButton("İptal");
        
        styleButton(btnKaydet);
        styleButton(btnIptal);
        
        buttonPanel.add(btnKaydet);
        buttonPanel.add(btnIptal);
        
        // Buton olayları
        btnKaydet.addActionListener(e -> kaydet());
        btnIptal.addActionListener(e -> {
            this.dispose();
            parentFrame.setVisible(true);
        });
        
        // Panelleri ana panele ekle
        mainPanel.add(lblBaslik, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Ana paneli frame'e ekle
        add(mainPanel);
    }
    
    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(100, 35));
        button.setBackground(new Color(0, 123, 255));
        // Yazı rengini siyah yap
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
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
    
    private void kaydet() {
        String adSoyad = txtAdSoyad.getText().trim();
        String kullaniciAdi = txtKullaniciAdi.getText().trim();
        String sifre = new String(txtSifre.getPassword());
        String sifreTekrar = new String(txtSifreTekrar.getPassword());
        
        // Validasyonlar
        if (adSoyad.isEmpty() || kullaniciAdi.isEmpty() || sifre.isEmpty() || sifreTekrar.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tüm alanları doldurunuz!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!sifre.equals(sifreTekrar)) {
            JOptionPane.showMessageDialog(this, "Şifreler eşleşmiyor!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (sifre.length() < 6) {
            JOptionPane.showMessageDialog(this, "Şifre en az 6 karakter olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Kullanıcı adı kontrolü
            String kontrolSql = "SELECT COUNT(*) as count FROM kullanicilar WHERE kullanici_adi = ?";
            PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql);
            kontrolStmt.setString(1, kullaniciAdi);
            ResultSet rs = kontrolStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                JOptionPane.showMessageDialog(this, "Bu kullanıcı adı zaten kullanılıyor!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Yeni kullanıcı kaydı
            String insertSql = "INSERT INTO kullanicilar (kullanici_adi, sifre, ad_soyad, rol) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, kullaniciAdi);
            pstmt.setString(2, sifre);
            pstmt.setString(3, adSoyad);
            pstmt.setString(4, "KULLANICI"); // Varsayılan olarak normal kullanıcı
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Kayıt başarıyla tamamlandı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Kayıt sırasında bir hata oluştu!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Veritabanı hatası: " + ex.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}