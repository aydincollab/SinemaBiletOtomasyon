package com.sinema.bilet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FilmYonetimFrame extends JFrame {
    private JTable filmTable;
    private DefaultTableModel tableModel;
    private JButton btnEkle;
    private JButton btnDuzenle;
    private JButton btnSil;
    private JButton btnGeri;
    private MainFrame parentFrame;
    
    public FilmYonetimFrame(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        // Frame ayarları
        setTitle("Film Yönetimi");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tablo modeli
        String[] columnNames = {"ID", "Film Adı", "Süre (dk)", "Yönetmen", "Yapım Yılı", "Eklenme Tarihi", "Durum"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Tablo
        filmTable = new JTable(tableModel);
        filmTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        filmTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        filmTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        filmTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        filmTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        filmTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        filmTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(filmTable);
        
        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        btnEkle = new JButton("Yeni Film");
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
        btnEkle.addActionListener(e -> yeniFilm());
        btnDuzenle.addActionListener(e -> filmDuzenle());
        btnSil.addActionListener(e -> filmSil());
        btnGeri.addActionListener(e -> geriDon());
        
        // Panelleri ana panele ekle
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Ana paneli frame'e ekle
        add(mainPanel);
        
        // Filmleri yükle
        loadFilmler();
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
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM filmler ORDER BY film_adi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("film_id"),
                    rs.getString("film_adi"),
                    rs.getInt("sure"),
                    rs.getString("yonetmen"),
                    rs.getInt("yapim_yili"),
                    rs.getString("eklenme_tarihi"),
                    rs.getInt("durum") == 1 ? "Aktif" : "Pasif"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Filmler yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void yeniFilm() {
        // Form paneli
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField filmAdiField = new JTextField();
        JTextField sureField = new JTextField();
        JTextField yonetmenField = new JTextField();
        JTextField yapimYiliField = new JTextField();
        
        panel.add(new JLabel("Film Adı:"));
        panel.add(filmAdiField);
        panel.add(new JLabel("Süre (dk):"));
        panel.add(sureField);
        panel.add(new JLabel("Yönetmen:"));
        panel.add(yonetmenField);
        panel.add(new JLabel("Yapım Yılı:"));
        panel.add(yapimYiliField);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Yeni Film", JOptionPane.OK_CANCEL_OPTION);
                
        if (result == JOptionPane.OK_OPTION) {
            String filmAdi = filmAdiField.getText().trim();
            String sure = sureField.getText().trim();
            String yonetmen = yonetmenField.getText().trim();
            String yapimYili = yapimYiliField.getText().trim();
            
            // Validasyonlar
            if (filmAdi.isEmpty() || sure.isEmpty() || yonetmen.isEmpty() || yapimYili.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Tüm alanları doldurunuz!",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                int sureInt = Integer.parseInt(sure);
                int yapimYiliInt = Integer.parseInt(yapimYili);
                
                if (sureInt <= 0 || yapimYiliInt < 1900 || yapimYiliInt > 2100) {
                    JOptionPane.showMessageDialog(this,
                        "Geçersiz süre veya yapım yılı!",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO filmler (film_adi, sure, yonetmen, yapim_yili, eklenme_tarihi, durum) VALUES (?, ?, ?, ?, ?, 1)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, filmAdi);
                    pstmt.setInt(2, sureInt);
                    pstmt.setString(3, yonetmen);
                    pstmt.setInt(4, yapimYiliInt);
                    pstmt.setString(5, LocalDate.now().format(DateTimeFormatter.ISO_DATE));
                    
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this,
                            "Film başarıyla eklendi.",
                            "Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);
                        loadFilmler();
                    }
                    
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this,
                        "Film eklenirken hata: " + e.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Süre ve yapım yılı sayısal değer olmalıdır!",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void filmDuzenle() {
        int selectedRow = filmTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen düzenlenecek filmi seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int filmId = (int) filmTable.getValueAt(selectedRow, 0);
        
        // Form paneli
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField filmAdiField = new JTextField(filmTable.getValueAt(selectedRow, 1).toString());
        JTextField sureField = new JTextField(filmTable.getValueAt(selectedRow, 2).toString());
        JTextField yonetmenField = new JTextField(filmTable.getValueAt(selectedRow, 3).toString());
        JTextField yapimYiliField = new JTextField(filmTable.getValueAt(selectedRow, 4).toString());
        
        panel.add(new JLabel("Film Adı:"));
        panel.add(filmAdiField);
        panel.add(new JLabel("Süre (dk):"));
        panel.add(sureField);
        panel.add(new JLabel("Yönetmen:"));
        panel.add(yonetmenField);
        panel.add(new JLabel("Yapım Yılı:"));
        panel.add(yapimYiliField);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Film Düzenle", JOptionPane.OK_CANCEL_OPTION);
                
        if (result == JOptionPane.OK_OPTION) {
            String filmAdi = filmAdiField.getText().trim();
            String sure = sureField.getText().trim();
            String yonetmen = yonetmenField.getText().trim();
            String yapimYili = yapimYiliField.getText().trim();
            
            // Validasyonlar
            if (filmAdi.isEmpty() || sure.isEmpty() || yonetmen.isEmpty() || yapimYili.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Tüm alanları doldurunuz!",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                int sureInt = Integer.parseInt(sure);
                int yapimYiliInt = Integer.parseInt(yapimYili);
                
                if (sureInt <= 0 || yapimYiliInt < 1900 || yapimYiliInt > 2100) {
                    JOptionPane.showMessageDialog(this,
                        "Geçersiz süre veya yapım yılı!",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "UPDATE filmler SET film_adi = ?, sure = ?, yonetmen = ?, yapim_yili = ? WHERE film_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, filmAdi);
                    pstmt.setInt(2, sureInt);
                    pstmt.setString(3, yonetmen);
                    pstmt.setInt(4, yapimYiliInt);
                    pstmt.setInt(5, filmId);
                    
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this,
                            "Film başarıyla güncellendi.",
                            "Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);
                        loadFilmler();
                    }
                    
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this,
                        "Film güncellenirken hata: " + e.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Süre ve yapım yılı sayısal değer olmalıdır!",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void filmSil() {
        int selectedRow = filmTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen silinecek filmi seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int filmId = (int) filmTable.getValueAt(selectedRow, 0);
        String filmAdi = (String) filmTable.getValueAt(selectedRow, 1);
        
        // Önce bu filme bağlı seansları kontrol et
        try (Connection conn = DatabaseConnection.getConnection()) {
            String kontrolSql = "SELECT COUNT(*) as count FROM seanslar WHERE film_id = ?";
            PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql);
            kontrolStmt.setInt(1, filmId);
            ResultSet rs = kontrolStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                JOptionPane.showMessageDialog(this,
                    "Bu filme ait seanslar bulunmaktadır. Önce seansları silmelisiniz.",
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
            filmAdi + " filmini silmek istediğinizden emin misiniz?",
            "Film Sil",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM filmler WHERE film_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, filmId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Film başarıyla silindi.",
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadFilmler();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Film silinirken hata: " + e.getMessage(),
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