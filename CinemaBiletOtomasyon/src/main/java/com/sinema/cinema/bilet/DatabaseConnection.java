package com.sinema.bilet;

import java.sql.*;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:sinema.db";
    
    public static Connection getConnection() throws SQLException {
        createTablesIfNotExist();
        return DriverManager.getConnection(DB_URL);
    }
    
    private static void createTablesIfNotExist() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();
            
            // İller tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS iller (" +
                        "il_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "il_adi VARCHAR(50) NOT NULL," +
                        "durum INTEGER DEFAULT 1)");
            
            // Salonlar tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS salonlar (" +
                        "salon_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "salon_adi VARCHAR(100) NOT NULL," +
                        "il_id INTEGER NOT NULL," +
                        "kapasite INTEGER DEFAULT 15," +
                        "durum INTEGER DEFAULT 1," +
                        "FOREIGN KEY (il_id) REFERENCES iller(il_id))");
            
            // Filmler tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS filmler (" +
                        "film_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "film_adi VARCHAR(100) NOT NULL," +
                        "sure INTEGER NOT NULL," +
                        "yonetmen VARCHAR(100)," +
                        "yapim_yili INTEGER," +
                        "eklenme_tarihi DATE," +
                        "durum INTEGER DEFAULT 1)");
            
            // Seanslar tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS seanslar (" +
                        "seans_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "film_id INTEGER NOT NULL," +
                        "salon_id INTEGER NOT NULL," +
                        "tarih DATE NOT NULL," +
                        "saat TIME NOT NULL," +
                        "durum INTEGER DEFAULT 1," +
                        "FOREIGN KEY (film_id) REFERENCES filmler(film_id)," +
                        "FOREIGN KEY (salon_id) REFERENCES salonlar(salon_id))");
            
            // Biletler tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS biletler (" +
                        "bilet_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "seans_id INTEGER NOT NULL," +
                        "koltuk_no INTEGER NOT NULL," +
                        "kullanici_adi TEXT," +
                        "musteri_ad TEXT NOT NULL," +
                        "musteri_soyad TEXT NOT NULL," +
                        "satis_tarihi TEXT NOT NULL," +
                        "durum INTEGER DEFAULT 1," +
                        "FOREIGN KEY (seans_id) REFERENCES seanslar(seans_id))");

            // Biletler tablosuna kullanici_adi kolonu ekle (eğer yoksa)
            try {
                stmt.execute("ALTER TABLE biletler ADD COLUMN kullanici_adi TEXT");
            } catch (SQLException e) {
                // Kolon zaten varsa hata vermesini engelle
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}