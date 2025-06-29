package com.sinema.bilet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class SeansYonetimFrame extends JFrame {
    private JTable seansTable;
    private DefaultTableModel tableModel;
    private JButton btnEkle;
    private JButton btnDuzenle;
    private JButton btnSil;
    private JButton btnGeri;
    private MainFrame parentFrame;
    private Map<Integer, String> filmlerMap;
    private Map<Integer, String> salonlarMap;
    
    public SeansYonetimFrame(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.filmlerMap = new HashMap<>();
        this.salonlarMap = new HashMap<>();
        
        // Frame ayarları
        setTitle("Seans Yönetimi");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tablo modeli
        String[] columnNames = {"ID", "Film", "Salon", "Tarih", "Saat", "Durum"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Tablo
        seansTable = new JTable(tableModel);
        seansTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        seansTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        seansTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        seansTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        seansTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        seansTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(seansTable);
        
        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        btnEkle = new JButton("Yeni Seans");
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
        btnEkle.addActionListener(e -> yeniSeans());
        btnDuzenle.addActionListener(e -> seansDuzenle());
        btnSil.addActionListener(e -> seansSil());
        btnGeri.addActionListener(e -> geriDon());
        
        // Panelleri ana panele ekle
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Ana paneli frame'e ekle
        add(mainPanel);
        
        // Film ve salonları yükle
        loadFilmler();
        loadSalonlar();
        // Seansları yükle
        loadSeanslar();
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
    
    private void loadFilmler() {
        filmlerMap.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT film_id, film_adi FROM filmler WHERE durum = 1 ORDER BY film_adi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                filmlerMap.put(rs.getInt("film_id"), rs.getString("film_adi"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Filmler yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSalonlar() {
        salonlarMap.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.salon_id, s.salon_adi, i.il_adi FROM salonlar s " +
                        "JOIN iller i ON s.il_id = i.il_id " +
                        "WHERE s.durum = 1 ORDER BY i.il_adi, s.salon_adi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                String salonBilgisi = rs.getString("il_adi") + " - " + rs.getString("salon_adi");
                salonlarMap.put(rs.getInt("salon_id"), salonBilgisi);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Salonlar yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSeanslar() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.*, f.film_adi, sl.salon_adi FROM seanslar s " +
                        "JOIN filmler f ON s.film_id = f.film_id " +
                        "JOIN salonlar sl ON s.salon_id = sl.salon_id " +
                        "ORDER BY s.tarih DESC, s.saat DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("seans_id"),
                    filmlerMap.get(rs.getInt("film_id")),
                    salonlarMap.get(rs.getInt("salon_id")),
                    rs.getString("tarih"),
                    rs.getString("saat"),
                    rs.getInt("durum") == 1 ? "Aktif" : "Pasif"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Seanslar yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void yeniSeans() {
        if (filmlerMap.isEmpty() || salonlarMap.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Film ve salon eklemeden seans ekleyemezsiniz!",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Form paneli
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JComboBox<String> filmComboBox = new JComboBox<>(filmlerMap.values().toArray(new String[0]));
        JComboBox<String> salonComboBox = new JComboBox<>(salonlarMap.values().toArray(new String[0]));
        JTextField tarihField = new JTextField("YYYY-MM-DD");
        JTextField saatField = new JTextField("HH:MM");
        
        panel.add(new JLabel("Film:"));
        panel.add(filmComboBox);
        panel.add(new JLabel("Salon:"));
        panel.add(salonComboBox);
        panel.add(new JLabel("Tarih:"));
        panel.add(tarihField);
        panel.add(new JLabel("Saat:"));
        panel.add(saatField);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Yeni Seans", JOptionPane.OK_CANCEL_OPTION);
                
        if (result == JOptionPane.OK_OPTION) {
            String tarih = tarihField.getText().trim();
            String saat = saatField.getText().trim();
            
            try {
                // Tarih ve saat validasyonu
                LocalDate.parse(tarih);
                LocalTime.parse(saat + ":00");
                
                int filmId = getKeyByValue(filmlerMap, filmComboBox.getSelectedItem().toString());
                int salonId = getKeyByValue(salonlarMap, salonComboBox.getSelectedItem().toString());
                
                // Çakışma kontrolü
                if (seanslarCakisiyor(salonId, tarih, saat, -1)) {
                    JOptionPane.showMessageDialog(this,
                        "Bu salon için seçilen tarih ve saatte başka bir seans bulunmaktadır!",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO seanslar (film_id, salon_id, tarih, saat, durum) VALUES (?, ?, ?, ?, 1)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, filmId);
                    pstmt.setInt(2, salonId);
                    pstmt.setString(3, tarih);
                    pstmt.setString(4, saat);
                    
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this,
                            "Seans başarıyla eklendi.",
                            "Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);
                        loadSeanslar();
                    }
                    
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this,
                        "Seans eklenirken hata: " + e.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Geçersiz tarih veya saat formatı!\nTarih: YYYY-MM-DD\nSaat: HH:MM",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void seansDuzenle() {
        int selectedRow = seansTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen düzenlenecek seansı seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int seansId = (int) seansTable.getValueAt(selectedRow, 0);
        
        // Form paneli
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JComboBox<String> filmComboBox = new JComboBox<>(filmlerMap.values().toArray(new String[0]));
        JComboBox<String> salonComboBox = new JComboBox<>(salonlarMap.values().toArray(new String[0]));
        JTextField tarihField = new JTextField(seansTable.getValueAt(selectedRow, 3).toString());
        JTextField saatField = new JTextField(seansTable.getValueAt(selectedRow, 4).toString());
        
        // Mevcut değerleri seç
        filmComboBox.setSelectedItem(seansTable.getValueAt(selectedRow, 1));
        salonComboBox.setSelectedItem(seansTable.getValueAt(selectedRow, 2));
        
        panel.add(new JLabel("Film:"));
        panel.add(filmComboBox);
        panel.add(new JLabel("Salon:"));
        panel.add(salonComboBox);
        panel.add(new JLabel("Tarih:"));
        panel.add(tarihField);
        panel.add(new JLabel("Saat:"));
        panel.add(saatField);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Seans Düzenle", JOptionPane.OK_CANCEL_OPTION);
                
        if (result == JOptionPane.OK_OPTION) {
            String tarih = tarihField.getText().trim();
            String saat = saatField.getText().trim();
            
            try {
                // Tarih ve saat validasyonu
                LocalDate.parse(tarih);
                LocalTime.parse(saat + ":00");
                
                int filmId = getKeyByValue(filmlerMap, filmComboBox.getSelectedItem().toString());
                int salonId = getKeyByValue(salonlarMap, salonComboBox.getSelectedItem().toString());
                
                // Çakışma kontrolü
                if (seanslarCakisiyor(salonId, tarih, saat, seansId)) {
                    JOptionPane.showMessageDialog(this,
                        "Bu salon için seçilen tarih ve saatte başka bir seans bulunmaktadır!",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "UPDATE seanslar SET film_id = ?, salon_id = ?, tarih = ?, saat = ? WHERE seans_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, filmId);
                    pstmt.setInt(2, salonId);
                    pstmt.setString(3, tarih);
                    pstmt.setString(4, saat);
                    pstmt.setInt(5, seansId);
                    
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this,
                            "Seans başarıyla güncellendi.",
                            "Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);
                        loadSeanslar();
                    }
                    
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this,
                        "Seans güncellenirken hata: " + e.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Geçersiz tarih veya saat formatı!\nTarih: YYYY-MM-DD\nSaat: HH:MM",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void seansSil() {
        int selectedRow = seansTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen silinecek seansı seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int seansId = (int) seansTable.getValueAt(selectedRow, 0);
        
        // Önce bu seansa ait biletleri kontrol et
        try (Connection conn = DatabaseConnection.getConnection()) {
            String kontrolSql = "SELECT COUNT(*) as count FROM biletler WHERE seans_id = ?";
            PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql);
            kontrolStmt.setInt(1, seansId);
            ResultSet rs = kontrolStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                JOptionPane.showMessageDialog(this,
                    "Bu seansa ait biletler bulunmaktadır. Seansı silemezsiniz.",
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
            "Seçili seansı silmek istediğinizden emin misiniz?",
            "Seans Sil",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM seanslar WHERE seans_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, seansId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Seans başarıyla silindi.",
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadSeanslar();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Seans silinirken hata: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean seanslarCakisiyor(int salonId, String tarih, String saat, int seansId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM seanslar " +
                        "WHERE salon_id = ? AND tarih = ? AND saat = ? AND seans_id != ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, salonId);
            pstmt.setString(2, tarih);
            pstmt.setString(3, saat);
            pstmt.setInt(4, seansId);
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt("count") > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
    
    private int getKeyByValue(Map<Integer, String> map, String value) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
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