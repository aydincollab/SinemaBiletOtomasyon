package com.sinema.bilet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SeansListelemeFrame extends JFrame {
    private JTable seansTable;
    private DefaultTableModel tableModel;
    private JButton btnGeri;
    private MainFrame parentFrame;
    
    public SeansListelemeFrame(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        // Frame ayarları
        setTitle("Film Seansları");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tablo modeli
        String[] columnNames = {"Film", "Salon", "İl", "Tarih", "Saat", "Boş Koltuk"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Tablo
        seansTable = new JTable(tableModel);
        seansTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        seansTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        seansTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        seansTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        seansTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        seansTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(seansTable);
        
        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnGeri = new JButton("Geri Dön");
        styleButton(btnGeri);
        buttonPanel.add(btnGeri);
        
        // Buton olayı
        btnGeri.addActionListener(e -> geriDon());
        
        // Panelleri ana panele ekle
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Ana paneli frame'e ekle
        add(mainPanel);
        
        // Seansları yükle
        loadSeanslar();
    }
    
    private void styleButton(JButton button) {
    button.setPreferredSize(new Dimension(120, 35));
    button.setBackground(new Color(0, 123, 255));
    button.setForeground(Color.BLACK); // Yazı rengini siyah yap
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
    
    private void loadSeanslar() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT f.film_adi, s.salon_adi, i.il_adi, se.tarih, se.saat, " +
                        "(s.kapasite - COALESCE((SELECT COUNT(*) FROM biletler b WHERE b.seans_id = se.seans_id AND b.durum = 1), 0)) as bos_koltuk " +
                        "FROM seanslar se " +
                        "JOIN filmler f ON se.film_id = f.film_id " +
                        "JOIN salonlar s ON se.salon_id = s.salon_id " +
                        "JOIN iller i ON s.il_id = i.il_id " +
                        "WHERE se.durum = 1 AND f.durum = 1 AND s.durum = 1 " +
                        "ORDER BY se.tarih, se.saat";
                        
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("film_adi"),
                    rs.getString("salon_adi"),
                    rs.getString("il_adi"),
                    rs.getString("tarih"),
                    rs.getString("saat"),
                    rs.getInt("bos_koltuk")
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
    
    private void geriDon() {
        this.dispose();
        parentFrame.setVisible(true);
    }
}