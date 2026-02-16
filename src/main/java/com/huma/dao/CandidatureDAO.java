package com.huma.dao;

import com.huma.config.MyDatabase;
import com.huma.model.Candidature;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class CandidatureDAO {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public ObservableList<Candidature> findAllWithDetails() {
        ObservableList<Candidature> list = FXCollections.observableArrayList();
        String sql = "SELECT c.id, c.date_candidature, c.statut, c.candidat_id, c.offre_id, " +
                "CONCAT(u.prenom, ' ', u.nom) AS candidat_nom, o.titre AS offre_titre " +
                "FROM candidature c " +
                "JOIN candidat ca ON ca.id = c.candidat_id " +
                "JOIN users u ON u.id = ca.id " +
                "JOIN offre_emploi o ON o.id = c.offre_id " +
                "ORDER BY c.date_candidature DESC, c.id DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapWithDetails(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture candidatures: " + e.getMessage(), e);
        }
        return list;
    }

    public ObservableList<Candidature> findByCandidatId(int candidatId) {
        ObservableList<Candidature> list = FXCollections.observableArrayList();
        String sql = "SELECT c.id, c.date_candidature, c.statut, c.candidat_id, c.offre_id, o.titre AS offre_titre " +
                "FROM candidature c " +
                "JOIN offre_emploi o ON o.id = c.offre_id " +
                "WHERE c.candidat_id = ? ORDER BY c.date_candidature DESC, c.id DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Candidature c = mapBase(rs);
                    c.setOffreTitre(rs.getString("offre_titre"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture candidatures candidat: " + e.getMessage(), e);
        }
        return list;
    }

    public int insert(Candidature candidature) {
        String sql = "INSERT INTO candidature (date_candidature, statut, candidat_id, offre_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(candidature.getDateCandidature()));
            ps.setString(2, candidature.getStatut());
            ps.setInt(3, candidature.getCandidatId());
            ps.setInt(4, candidature.getOffreId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur creation candidature: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean updateStatut(int candidatureId, String statut) {
        String sql = "UPDATE candidature SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, candidatureId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise a jour statut candidature: " + e.getMessage(), e);
        }
    }

    public boolean existsByCandidatAndOffre(int candidatId, int offreId) {
        String sql = "SELECT 1 FROM candidature WHERE candidat_id = ? AND offre_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidatId);
            ps.setInt(2, offreId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur verification candidature: " + e.getMessage(), e);
        }
    }

    private Candidature mapWithDetails(ResultSet rs) throws SQLException {
        Candidature c = mapBase(rs);
        c.setCandidatNom(rs.getString("candidat_nom"));
        c.setOffreTitre(rs.getString("offre_titre"));
        return c;
    }

    private Candidature mapBase(ResultSet rs) throws SQLException {
        Candidature c = new Candidature();
        c.setId(rs.getInt("id"));
        Date date = rs.getDate("date_candidature");
        c.setDateCandidature(date == null ? LocalDate.now() : date.toLocalDate());
        c.setStatut(rs.getString("statut"));
        c.setCandidatId(rs.getInt("candidat_id"));
        c.setOffreId(rs.getInt("offre_id"));
        return c;
    }
}
