package com.huma.dao;

import com.huma.config.MyDatabase;
import com.huma.model.OffreEmploi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class OffreEmploiDAO {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public ObservableList<OffreEmploi> findAll() {
        ObservableList<OffreEmploi> offres = FXCollections.observableArrayList();
        String sql = "SELECT id, titre, description, departement, date_publication, type_contrat, nombre_postes, admin_id " +
                "FROM offre_emploi ORDER BY date_publication DESC, id DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                offres.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture offres: " + e.getMessage(), e);
        }
        return offres;
    }

    public ObservableList<OffreEmploi> searchByTitreOrDepartement(String keyword) {
        ObservableList<OffreEmploi> offres = FXCollections.observableArrayList();
        String sql = "SELECT id, titre, description, departement, date_publication, type_contrat, nombre_postes, admin_id " +
                "FROM offre_emploi WHERE LOWER(titre) LIKE ? OR LOWER(departement) LIKE ? " +
                "ORDER BY date_publication DESC, id DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String k = "%" + keyword.toLowerCase() + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    offres.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche offres: " + e.getMessage(), e);
        }
        return offres;
    }

    public int insert(OffreEmploi offre) {
        String sql = "INSERT INTO offre_emploi (titre, description, departement, date_publication, type_contrat, nombre_postes, admin_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, offre);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur creation offre: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean update(OffreEmploi offre) {
        String sql = "UPDATE offre_emploi SET titre = ?, description = ?, departement = ?, date_publication = ?, " +
                "type_contrat = ?, nombre_postes = ?, admin_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, offre);
            ps.setInt(8, offre.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise a jour offre: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM offre_emploi WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression offre: " + e.getMessage(), e);
        }
    }

    public OffreEmploi findById(int id) {
        String sql = "SELECT id, titre, description, departement, date_publication, type_contrat, nombre_postes, admin_id " +
                "FROM offre_emploi WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture offre: " + e.getMessage(), e);
        }
        return null;
    }

    private OffreEmploi map(ResultSet rs) throws SQLException {
        OffreEmploi offre = new OffreEmploi();
        offre.setId(rs.getInt("id"));
        offre.setTitre(rs.getString("titre"));
        offre.setDescription(rs.getString("description"));
        offre.setDepartement(rs.getString("departement"));
        Date date = rs.getDate("date_publication");
        offre.setDatePublication(date == null ? LocalDate.now() : date.toLocalDate());
        offre.setTypeContrat(rs.getString("type_contrat"));
        offre.setNombrePostes(rs.getInt("nombre_postes"));
        offre.setAdminId(rs.getInt("admin_id"));
        return offre;
    }

    private void fillStatement(PreparedStatement ps, OffreEmploi offre) throws SQLException {
        ps.setString(1, offre.getTitre());
        ps.setString(2, offre.getDescription());
        ps.setString(3, offre.getDepartement());
        ps.setDate(4, Date.valueOf(offre.getDatePublication()));
        ps.setString(5, offre.getTypeContrat());
        ps.setInt(6, offre.getNombrePostes());
        ps.setInt(7, offre.getAdminId());
    }
}
