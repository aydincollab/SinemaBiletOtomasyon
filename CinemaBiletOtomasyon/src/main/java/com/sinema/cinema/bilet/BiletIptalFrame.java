package com.sinema.bilet;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class BiletIptalFrame extends JFrame {
    private MainFrame parentFrame;
    private String kullaniciAdi;
    private JTable biletlerTable;
    private DefaultTableModel tableModel;
    private JButton btnIptalEt;
    private JButton btnGeriDon;

    public BiletIptalFrame(MainFrame parentFrame, String kullaniciAdi) {
        this.parentFrame = parentFrame;
        this.kullaniciAdi = kullaniciAdi;

        // Frame ayarları
        setTitle("Bilet İptal");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        loadBiletler();
    }

    private void initializeComponents() {
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tablo modeli
        String[] columnNames = {"Bilet No", "Film", "Salon", "Tarih", "Saat", "Koltuk No", "Ad Soyad"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Tablo
        biletlerTable = new JTable(tableModel);
        biletlerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(biletlerTable);
        
        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnIptalEt = new JButton("Bileti İptal Et");
        btnGeriDon = new JButton("Geri Dön");
        
        styleButton(btnIptalEt);
        styleButton(btnGeriDon);
        
        btnIptalEt.addActionListener(e -> biletIptalEt());
        btnGeriDon.addActionListener(e -> geriDon());
        
        buttonPanel.add(btnGeriDon);
        buttonPanel.add(btnIptalEt);

        // Panelleri ana panele ekle
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Frame'e ekle
        add(mainPanel);
    }

    private void loadBiletler() {
        // Tabloyu temizle
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT b.bilet_id, f.film_adi, s.salon_adi, se.tarih, se.saat, " +
                        "b.koltuk_no, b.ad_soyad " +
                        "FROM biletler b " +
                        "JOIN seanslar se ON b.seans_id = se.seans_id " +
                        "JOIN filmler f ON se.film_id = f.film_id " +
                        "JOIN salonlar s ON se.salon_id = s.salon_id " +
                        "WHERE b.kullanici_adi = ? AND b.durum = 1 " +
                        "ORDER BY se.tarih, se.saat";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, kullaniciAdi);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("bilet_id"),
                    rs.getString("film_adi"),
                    rs.getString("salon_adi"),
                    rs.getString("tarih"),
                    rs.getString("saat"),
                    rs.getInt("koltuk_no"),
                    rs.getString("ad_soyad")
                };
                tableModel.addRow(row);
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                    "Aktif biletiniz bulunmamaktadır!",
                    "Bilgi",
                    JOptionPane.INFORMATION_MESSAGE);
                geriDon();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Biletler yüklenirken hata: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void biletIptalEt() {
        int selectedRow = biletlerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen iptal edilecek bileti seçiniz!",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int biletId = (int) tableModel.getValueAt(selectedRow, 0);
        String filmAdi = (String) tableModel.getValueAt(selectedRow, 1);
        
        int response = JOptionPane.showConfirmDialog(this,
            filmAdi + " filmi için olan biletinizi iptal etmek istediğinize emin misiniz?",
            "Bilet İptal Onayı",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE biletler SET durum = 0 WHERE bilet_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, biletId);
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Bilet başarıyla iptal edildi!",
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadBiletler(); // Tabloyu güncelle
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Bilet iptal edilirken bir hata oluştu!",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Bilet iptal edilirken hata: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
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