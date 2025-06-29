package com.sinema.bilet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {
    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Kullanıcılar tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS kullanicilar ("
                    + "kullanici_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "kullanici_adi TEXT NOT NULL UNIQUE,"
                    + "sifre TEXT NOT NULL,"
                    + "ad_soyad TEXT NOT NULL,"
                    + "rol TEXT NOT NULL CHECK(rol IN ('ADMIN', 'KULLANICI')),"
                    + "durum INTEGER DEFAULT 1"
                    + ")");
                    
            // İller tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS iller ("
                    + "il_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "il_adi TEXT NOT NULL UNIQUE,"
                    + "durum INTEGER DEFAULT 1"
                    + ")");

            // Salonlar tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS salonlar ("
                    + "salon_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "salon_adi TEXT NOT NULL,"
                    + "il_id INTEGER NOT NULL,"
                    + "kapasite INTEGER DEFAULT 15,"
                    + "durum INTEGER DEFAULT 1,"
                    + "FOREIGN KEY (il_id) REFERENCES iller(il_id)"
                    + ")");
                    
            // Filmler tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS filmler ("
                    + "film_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "film_adi TEXT NOT NULL,"
                    + "sure INTEGER,"
                    + "yonetmen TEXT,"
                    + "yapim_yili INTEGER,"
                    + "eklenme_tarihi DATE DEFAULT CURRENT_DATE,"
                    + "durum INTEGER DEFAULT 1"
                    + ")");
                    
            // Seanslar tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS seanslar ("
                    + "seans_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "film_id INTEGER,"
                    + "salon_id INTEGER,"
                    + "tarih DATE NOT NULL,"
                    + "saat TIME NOT NULL,"
                    + "durum INTEGER DEFAULT 1,"
                    + "FOREIGN KEY (film_id) REFERENCES filmler(film_id),"
                    + "FOREIGN KEY (salon_id) REFERENCES salonlar(salon_id)"
                    + ")");
                    
            // Biletler tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS biletler ("
                    + "bilet_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "seans_id INTEGER,"
                    + "koltuk_no INTEGER NOT NULL,"
                    + "musteri_adi TEXT NOT NULL,"
                    + "musteri_soyadi TEXT NOT NULL,"
                    + "fiyat DECIMAL(10,2) NOT NULL,"
                    + "satis_tarihi DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "durum INTEGER DEFAULT 1,"
                    + "FOREIGN KEY (seans_id) REFERENCES seanslar(seans_id)"
                    + ")");

            // İlk admin kullanıcısını kontrol et ve ekle
            String adminKontrol = "SELECT COUNT(*) as count FROM kullanicilar WHERE rol = 'ADMIN'";
            ResultSet rs = stmt.executeQuery(adminKontrol);
            if (rs.getInt("count") == 0) {
                String adminEkle = "INSERT INTO kullanicilar (kullanici_adi, sifre, ad_soyad, rol) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(adminEkle);
                pstmt.setString(1, "admin");
                pstmt.setString(2, "admin123");
                pstmt.setString(3, "Sistem Yöneticisi");
                pstmt.setString(4, "ADMIN");
                pstmt.executeUpdate();
                System.out.println("Admin kullanıcısı oluşturuldu.");
            }
            
            System.out.println("Veritabanı tabloları başarıyla oluşturuldu.");
            
        } catch (SQLException e) {
            System.err.println("Tablo oluşturma hatası: " + e.getMessage());
        }
    }
}