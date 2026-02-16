package com.huma.dao;

import com.huma.config.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CandidatDAO {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public boolean existsById(int id) {
        String sql = "SELECT 1 FROM candidat WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur verification candidat: " + e.getMessage(), e);
        }
    }
}
