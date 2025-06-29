package com.sinema.bilet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SalonYonetimFrame extends JFrame {
    private JTable salonTable;
    private DefaultTableModel tableModel;
    private JButton btnEkle;
    private JButton btnDuzenle;
    private JButton btnSil;
    private JButton btnGeri;
    private MainFrame parentFrame;
    private Map<Integer, String> illerMap;
    
    public SalonYonetimFrame(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.illerMap = new HashMap<>();
        
        // Frame ayarları
        setTitle("Salon Yönetimi");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tablo modeli
        String[] columnNames = {"ID", "Salon Adı", "İl", "Kapasite", "Durum"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Tablo
        salonTable = new JTable(tableModel);
        salonTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        salonTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        salonTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        salonTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        salonTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(salonTable);
        
        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        btnEkle = new JButton("Yeni Salon");
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
        btnEkle.addActionListener(e -> yeniSalon());
        btnDuzenle.addActionListener(e -> salonDuzenle());
        btnSil.addActionListener(e -> salonSil());
        btnGeri.addActionListener(e -> geriDon());
        
        // Panelleri ana panele ekle
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Ana paneli frame'e ekle
        add(mainPanel);
        
        // İlleri yükle
        loadIller();
        // Salonları yükle
        loadSalonlar();
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
    
    private void loadIller() {
        illerMap.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT il_id, il_adi FROM iller WHERE durum = 1 ORDER BY il_adi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                illerMap.put(rs.getInt("il_id"), rs.getString("il_adi"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "İller yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSalonlar() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM salonlar ORDER BY salon_adi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("salon_id"),
                    rs.getString("salon_adi"),
                    illerMap.get(rs.getInt("il_id")),
                    rs.getInt("kapasite"),
                    rs.getInt("durum") == 1 ? "Aktif" : "Pasif"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Salonlar yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void yeniSalon() {
        // İl seçimi için ComboBox
        JComboBox<String> ilComboBox = new JComboBox<>(illerMap.values().toArray(new String[0]));
        
        // Form paneli
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Salon Adı:"));
        JTextField salonAdiField = new JTextField();
        panel.add(salonAdiField);
        panel.add(new JLabel("İl:"));
        panel.add(ilComboBox);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Yeni Salon", JOptionPane.OK_CANCEL_OPTION);
                
        if (result == JOptionPane.OK_OPTION) {
            String salonAdi = salonAdiField.getText().trim();
            String secilenIl = (String) ilComboBox.getSelectedItem();
            
            if (salonAdi.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Salon adı boş olamaz!",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Salon adı kontrolü
                String kontrolSql = "SELECT COUNT(*) as count FROM salonlar WHERE salon_adi = ? AND il_id = ?";
                PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql);
                kontrolStmt.setString(1, salonAdi);
                kontrolStmt.setInt(2, getIlId(secilenIl));
                ResultSet rs = kontrolStmt.executeQuery();
                
                if (rs.next() && rs.getInt("count") > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Bu salonda zaten bu isimde bir salon mevcut!",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Yeni salon ekleme
                String sql = "INSERT INTO salonlar (salon_adi, il_id, kapasite, durum) VALUES (?, ?, 15, 1)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, salonAdi);
                pstmt.setInt(2, getIlId(secilenIl));
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Salon başarıyla eklendi.",
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadSalonlar();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Salon eklenirken hata: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void salonDuzenle() {
        int selectedRow = salonTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen düzenlenecek salonu seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int salonId = (int) salonTable.getValueAt(selectedRow, 0);
        String mevcutSalonAdi = (String) salonTable.getValueAt(selectedRow, 1);
        String mevcutIl = (String) salonTable.getValueAt(selectedRow, 2);
        
        // İl seçimi için ComboBox
        JComboBox<String> ilComboBox = new JComboBox<>(illerMap.values().toArray(new String[0]));
        ilComboBox.setSelectedItem(mevcutIl);
        
        // Form paneli
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Salon Adı:"));
        JTextField salonAdiField = new JTextField(mevcutSalonAdi);
        panel.add(salonAdiField);
        panel.add(new JLabel("İl:"));
        panel.add(ilComboBox);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Salon Düzenle", JOptionPane.OK_CANCEL_OPTION);
                
        if (result == JOptionPane.OK_OPTION) {
            String yeniSalonAdi = salonAdiField.getText().trim();
            String secilenIl = (String) ilComboBox.getSelectedItem();
            
            if (yeniSalonAdi.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Salon adı boş olamaz!",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Salon adı kontrolü
                if (!yeniSalonAdi.equals(mevcutSalonAdi) || !secilenIl.equals(mevcutIl)) {
                    String kontrolSql = "SELECT COUNT(*) as count FROM salonlar WHERE salon_adi = ? AND il_id = ? AND salon_id != ?";
                    PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql);
                    kontrolStmt.setString(1, yeniSalonAdi);
                    kontrolStmt.setInt(2, getIlId(secilenIl));
                    kontrolStmt.setInt(3, salonId);
                    ResultSet rs = kontrolStmt.executeQuery();
                    
                    if (rs.next() && rs.getInt("count") > 0) {
                        JOptionPane.showMessageDialog(this,
                            "Bu salonda zaten bu isimde bir salon mevcut!",
                            "Hata",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                // Salon güncelleme
                String sql = "UPDATE salonlar SET salon_adi = ?, il_id = ? WHERE salon_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, yeniSalonAdi);
                pstmt.setInt(2, getIlId(secilenIl));
                pstmt.setInt(3, salonId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Salon başarıyla güncellendi.",
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadSalonlar();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Salon güncellenirken hata: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void salonSil() {
        int selectedRow = salonTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen silinecek salonu seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int salonId = (int) salonTable.getValueAt(selectedRow, 0);
        String salonAdi = (String) salonTable.getValueAt(selectedRow, 1);
        
        // Önce bu salona bağlı seansları kontrol et
        try (Connection conn = DatabaseConnection.getConnection()) {
            String kontrolSql = "SELECT COUNT(*) as count FROM seanslar WHERE salon_id = ?";
            PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql);
            kontrolStmt.setInt(1, salonId);
            ResultSet rs = kontrolStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                JOptionPane.showMessageDialog(this,
                    "Bu salona ait seanslar bulunmaktadır. Önce seansları silmelisiniz.",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Kontrol sırasında hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int response = JOptionPane.showConfirmDialog(this,
            salonAdi + " salonunu silmek istediğinizden emin misiniz?",
            "Salon Sil",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM salonlar WHERE salon_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, salonId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Salon başarıyla silindi.",
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadSalonlar();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Salon silinirken hata: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private int getIlId(String ilAdi) {
        for (Map.Entry<Integer, String> entry : illerMap.entrySet()) {
            if (entry.getValue().equals(ilAdi)) {
                return entry.getKey();
            }
        }
        return -1;
    }
    
    private void geriDon() {
        this.dispose();
        parentFrame.setVisible(true);
    }
}