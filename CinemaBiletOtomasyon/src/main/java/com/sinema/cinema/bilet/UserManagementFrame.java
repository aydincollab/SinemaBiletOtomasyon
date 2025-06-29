package com.sinema.bilet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UserManagementFrame extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton btnEkle;
    private JButton btnDuzenle;
    private JButton btnSil;
    private JButton btnGeri;
    private MainFrame parentFrame;
    
    public UserManagementFrame(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        // Frame ayarları
        setTitle("Kullanıcı Yönetimi");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tablo modeli
        String[] columnNames = {"ID", "Kullanıcı Adı", "Ad Soyad", "Rol", "Durum"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Tablo
        userTable = new JTable(tableModel);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        
        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        btnEkle = new JButton("Yeni Kullanıcı");
        btnDuzenle = new JButton("Düzenle");
        btnSil = new JButton("Sil");
        btnGeri = new JButton("Geri Dön");
        
        styleButton(btnEkle);
        styleButton(btnDuzenle);
        styleButton(btnSil);
        styleButton(btnGeri);
        
        buttonPanel.add(btnEkle);
        buttonPanel.add(btnDuzenle);
        buttonPanel.add(btnSil);
        buttonPanel.add(btnGeri);
        
        // Buton olayları
        btnEkle.addActionListener(e -> yeniKullanici());
        btnDuzenle.addActionListener(e -> kullaniciDuzenle());
        btnSil.addActionListener(e -> kullaniciSil());
        btnGeri.addActionListener(e -> geriDon());
        
        // Panelleri ana panele ekle
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Ana paneli frame'e ekle
        add(mainPanel);
        
        // Kullanıcıları yükle
        loadUsers();
    }
    
    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(120, 35));
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
    
    private void loadUsers() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM kullanicilar ORDER BY kullanici_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("kullanici_id"),
                    rs.getString("kullanici_adi"),
                    rs.getString("ad_soyad"),
                    rs.getString("rol"),
                    rs.getInt("durum") == 1 ? "Aktif" : "Pasif"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Kullanıcılar yüklenirken hata oluştu: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void yeniKullanici() {
        RegisterFrame registerFrame = new RegisterFrame(null);
        registerFrame.setVisible(true);
        registerFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                loadUsers();
            }
        });
    }
    
    private void kullaniciDuzenle() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen düzenlenecek kullanıcıyı seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int kullaniciId = (int) userTable.getValueAt(selectedRow, 0);
        String kullaniciAdi = (String) userTable.getValueAt(selectedRow, 1);
        
        // Kullanıcı düzenleme diyaloğu
        JDialog dialog = new JDialog(this, "Kullanıcı Düzenle", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField txtAdSoyad = new JTextField();
        JPasswordField txtSifre = new JPasswordField();
        JComboBox<String> cmbRol = new JComboBox<>(new String[]{"ADMIN", "KULLANICI"});
        JComboBox<String> cmbDurum = new JComboBox<>(new String[]{"Aktif", "Pasif"});
        
        panel.add(new JLabel("Ad Soyad:"));
        panel.add(txtAdSoyad);
        panel.add(new JLabel("Yeni Şifre:"));
        panel.add(txtSifre);
        panel.add(new JLabel("Rol:"));
        panel.add(cmbRol);
        panel.add(new JLabel("Durum:"));
        panel.add(cmbDurum);
        
        // Mevcut değerleri yükle
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM kullanicilar WHERE kullanici_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, kullaniciId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                txtAdSoyad.setText(rs.getString("ad_soyad"));
                cmbRol.setSelectedItem(rs.getString("rol"));
                cmbDurum.setSelectedIndex(rs.getInt("durum"));
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog,
                "Kullanıcı bilgileri yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JButton btnKaydet = new JButton("Kaydet");
        // Kaydet butonunu da stillendirmek gerekirse
        styleDialogButton(btnKaydet);
        
        btnKaydet.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE kullanicilar SET ad_soyad = ?, rol = ?, durum = ?";
                if (txtSifre.getPassword().length > 0) {
                    sql += ", sifre = ?";
                }
                sql += " WHERE kullanici_id = ?";
                
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, txtAdSoyad.getText());
                pstmt.setString(2, (String) cmbRol.getSelectedItem());
                pstmt.setInt(3, cmbDurum.getSelectedIndex());
                
                if (txtSifre.getPassword().length > 0) {
                    pstmt.setString(4, new String(txtSifre.getPassword()));
                    pstmt.setInt(5, kullaniciId);
                } else {
                    pstmt.setInt(4, kullaniciId);
                }
                
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(dialog,
                    "Kullanıcı başarıyla güncellendi.",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);
                    
                dialog.dispose();
                loadUsers();
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Güncelleme sırasında hata: " + ex.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnKaydet);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void styleDialogButton(JButton button) {
        button.setPreferredSize(new Dimension(100, 30));
        button.setBackground(new Color(0, 123, 255));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Font currentFont = button.getFont();
                button.setFont(new Font(currentFont.getName(), Font.BOLD, currentFont.getSize()));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                Font currentFont = button.getFont();
                button.setFont(new Font(currentFont.getName(), Font.PLAIN, currentFont.getSize()));
                button.setForeground(Color.BLACK);
            }
        });
    }
    
    private void kullaniciSil() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen silinecek kullanıcıyı seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int kullaniciId = (int) userTable.getValueAt(selectedRow, 0);
        String kullaniciAdi = (String) userTable.getValueAt(selectedRow, 1);
        
        int response = JOptionPane.showConfirmDialog(this,
            kullaniciAdi + " kullanıcısını silmek istediğinizden emin misiniz?",
            "Kullanıcı Sil",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM kullanicilar WHERE kullanici_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, kullaniciId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Kullanıcı başarıyla silindi.",
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Kullanıcı silinirken hata: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void geriDon() {
        this.dispose();
        parentFrame.setVisible(true);
    }
}