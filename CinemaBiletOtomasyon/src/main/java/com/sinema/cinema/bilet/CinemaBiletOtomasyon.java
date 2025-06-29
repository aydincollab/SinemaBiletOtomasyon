package com.sinema.bilet;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class CinemaBiletOtomasyon {
    public static void main(String[] args) {
        System.out.println("Sinema Bilet Otomasyonu başlatılıyor...");
        
        // Veritabanı tablolarını oluştur
        DatabaseInitializer.initializeDatabase();
        
        // Arayüzü başlat
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}