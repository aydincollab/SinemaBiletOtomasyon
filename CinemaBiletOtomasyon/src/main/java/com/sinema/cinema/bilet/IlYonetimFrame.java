package com.sinema.bilet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class IlYonetimFrame extends JFrame {
    private JTable ilTable;
    private DefaultTableModel tableModel;
    private JButton btnEkle;
    private JButton btnDuzenle;
    private JButton btnSil;
    private JButton btnGeri;
    private MainFrame parentFrame;
    
    public IlYonetimFrame(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        // Frame ayarları
        setTitle("İl Yönetimi");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tablo modeli
        String[] columnNames = {"ID", "İl Adı", "Durum"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Tablo
        ilTable = new JTable(tableModel);
        ilTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        ilTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        ilTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(ilTable);
        
        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        btnEkle = new JButton("Yeni İl");
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
        btnEkle.addActionListener(e -> yeniIl());
        btnDuzenle.addActionListener(e -> ilDuzenle());
        btnSil.addActionListener(e -> ilSil());
        btnGeri.addActionListener(e -> geriDon());
        
        // Panelleri ana panele ekle
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Ana paneli frame'e ekle
        add(mainPanel);
        
        // İlleri yükle
        loadIller();
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
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM iller ORDER BY il_adi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("il_id"),
                    rs.getString("il_adi"),
                    rs.getInt("durum") == 1 ? "Aktif" : "Pasif"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "İller yüklenirken hata oluştu: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void yeniIl() {
        String ilAdi = JOptionPane.showInputDialog(this,
            "İl adını giriniz:",
            "Yeni İl",
            JOptionPane.PLAIN_MESSAGE);
            
        if (ilAdi != null && !ilAdi.trim().isEmpty()) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // İl adı kontrolü
                String kontrolSql = "SELECT COUNT(*) as count FROM iller WHERE il_adi = ?";
                PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql);
                kontrolStmt.setString(1, ilAdi.trim());
                ResultSet rs = kontrolStmt.executeQuery();
                
                if (rs.next() && rs.getInt("count") > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Bu il zaten mevcut!",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Yeni il ekleme
                String sql = "INSERT INTO iller (il_adi, durum) VALUES (?, 1)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, ilAdi.trim());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "İl başarıyla eklendi.",
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadIller();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "İl eklenirken hata: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void ilDuzenle() {
        int selectedRow = ilTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen düzenlenecek ili seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int ilId = (int) ilTable.getValueAt(selectedRow, 0);
        String mevcutIlAdi = (String) ilTable.getValueAt(selectedRow, 1);
        
        String yeniIlAdi = JOptionPane.showInputDialog(this,
            "İl adını giriniz:",
            "İl Düzenle",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            mevcutIlAdi).toString();
            
        if (yeniIlAdi != null && !yeniIlAdi.trim().isEmpty()) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // İl adı kontrolü
                if (!yeniIlAdi.equals(mevcutIlAdi)) {
                    String kontrolSql = "SELECT COUNT(*) as count FROM iller WHERE il_adi = ? AND il_id != ?";
                    PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql);
                    kontrolStmt.setString(1, yeniIlAdi.trim());
                    kontrolStmt.setInt(2, ilId);
                    ResultSet rs = kontrolStmt.executeQuery();
                    
                    if (rs.next() && rs.getInt("count") > 0) {
                        JOptionPane.showMessageDialog(this,
                            "Bu il adı zaten kullanılıyor!",
                            "Hata",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                // İl güncelleme
                String sql = "UPDATE iller SET il_adi = ? WHERE il_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, yeniIlAdi.trim());
                pstmt.setInt(2, ilId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "İl başarıyla güncellendi.",
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadIller();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "İl güncellenirken hata: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void ilSil() {
        int selectedRow = ilTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen silinecek ili seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int ilId = (int) ilTable.getValueAt(selectedRow, 0);
        String ilAdi = (String) ilTable.getValueAt(selectedRow, 1);
        
        // Önce bu ile bağlı salonları kontrol et
        try (Connection conn = DatabaseConnection.getConnection()) {
            String kontrolSql = "SELECT COUNT(*) as count FROM salonlar WHERE il_id = ?";
            PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql);
            kontrolStmt.setInt(1, ilId);
            ResultSet rs = kontrolStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                JOptionPane.showMessageDialog(this,
                    "Bu ile ait salonlar bulunmaktadır. Önce salonları silmelisiniz.",
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
            ilAdi + " ilini silmek istediğinizden emin misiniz?",
            "İl Sil",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM iller WHERE il_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, ilId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "İl başarıyla silindi.",
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadIller();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "İl silinirken hata: " + e.getMessage(),
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