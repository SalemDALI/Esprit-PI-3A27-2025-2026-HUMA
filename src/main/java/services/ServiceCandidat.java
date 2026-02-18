package services;

import models.Candidat;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceCandidat {

    private final Connection cnx = MyDatabase.getInstance().getCnx();

    public List<Candidat> getAll() {
        List<Candidat> list = new ArrayList<>();
        String sql = "SELECT u.id, u.nom, u.prenom, u.email, u.role, " +
                "(SELECT c.cv FROM candidature c " +
                " WHERE c.candidat_id = u.id AND c.cv IS NOT NULL AND c.cv <> '' " +
                " ORDER BY c.date_candidature DESC, c.id DESC LIMIT 1) AS cv " +
                "FROM users u " +
                "WHERE u.role = ? " +
                "ORDER BY u.id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ) {
            ps.setString(1, "CANDIDAT");
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Candidat c = new Candidat();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setPrenom(rs.getString("prenom"));
                c.setEmail(rs.getString("email"));
                c.setRole(rs.getString("role"));
                c.setCv(rs.getString("cv"));
                list.add(c);
            }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean ajouter(Candidat candidat) {
        String sql = "UPDATE users SET role = 'CANDIDAT' WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, candidat.getId());
            int affected = ps.executeUpdate();
            if (affected > 0 && candidat.getCv() != null && !candidat.getCv().isBlank()) {
                update(candidat);
            }
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Candidat candidat) {
        String sql = "UPDATE candidature SET cv=? WHERE candidat_id=? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, candidat.getCv());
            ps.setInt(2, candidat.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "UPDATE users SET role = 'EMPLOYE' WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
