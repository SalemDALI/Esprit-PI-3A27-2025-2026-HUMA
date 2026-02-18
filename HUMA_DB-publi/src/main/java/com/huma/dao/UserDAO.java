package com.huma.dao;

import com.huma.config.MyDatabase;
import com.huma.model.Role;
import com.huma.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public User authenticate(String email, String password) {
        String sql = "SELECT id, nom, prenom, email, mdp, role FROM users WHERE email = ? AND mdp = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setMdp(rs.getString("mdp"));
                    user.setRole(Role.valueOf(rs.getString("role")));
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur authentification: " + e.getMessage(), e);
        }
        return null;
    }
}
