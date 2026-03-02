package services.recrutement;

import models.recrutement.Candidature;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceCandidature {

    Connection cnx = MyDatabase.getInstance().getCnx();

    public boolean ajouter(Candidature c) {
        String sql = "INSERT INTO candidature (date_candidature, statut, candidat_id, offre_id, cv) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(c.getDateCandidature()));
            ps.setString(2, c.getStatut());
            ps.setInt(3, c.getCandidatId());
            ps.setInt(4, c.getOffreId());
            ps.setString(5, c.getCheminCv());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<Candidature> getAll() {
        List<Candidature> list = new ArrayList<>();
        String sql = "SELECT * FROM candidature ORDER BY id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Candidature c = new Candidature();
                c.setId(rs.getInt("id"));
                c.setDateCandidature(rs.getDate("date_candidature").toLocalDate());
                c.setStatut(rs.getString("statut"));
                c.setCandidatId(rs.getInt("candidat_id"));
                c.setOffreId(rs.getInt("offre_id"));
                c.setCheminCv(rs.getString("cv"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    public List<Candidature> getByCandidatId(int candidatId) {
        List<Candidature> list = new ArrayList<>();
        String sql = "SELECT c.id, c.date_candidature, c.statut, c.candidat_id, c.offre_id, " +
                "c.cv, o.titre AS offre_titre " +
                "FROM candidature c JOIN offre_emploi o ON o.id = c.offre_id WHERE c.candidat_id = ? " +
                "ORDER BY c.date_candidature DESC, c.id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, candidatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Candidature c = new Candidature();
                    c.setId(rs.getInt("id"));
                    c.setDateCandidature(rs.getDate("date_candidature").toLocalDate());
                    c.setStatut(rs.getString("statut"));
                    c.setCandidatId(rs.getInt("candidat_id"));
                    c.setOffreId(rs.getInt("offre_id"));
                    c.setOffreTitre(rs.getString("offre_titre"));
                    c.setCheminCv(rs.getString("cv"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    public List<Candidature> getByOffreId(int offreId) {
        List<Candidature> list = new ArrayList<>();
        String sql = "SELECT c.id, c.date_candidature, c.statut, c.candidat_id, c.offre_id, c.cv, " +
                "o.titre AS offre_titre, CONCAT(COALESCE(u.nom, ''), ' ', COALESCE(u.prenom, '')) AS candidat_nom, " +
                "u.email AS candidat_email " +
                "FROM candidature c " +
                "JOIN offre_emploi o ON o.id = c.offre_id " +
                "LEFT JOIN users u ON u.id = c.candidat_id " +
                "WHERE c.offre_id = ? " +
                "ORDER BY c.date_candidature DESC, c.id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, offreId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Candidature c = new Candidature();
                    c.setId(rs.getInt("id"));
                    c.setDateCandidature(rs.getDate("date_candidature").toLocalDate());
                    c.setStatut(rs.getString("statut"));
                    c.setCandidatId(rs.getInt("candidat_id"));
                    c.setOffreId(rs.getInt("offre_id"));
                    c.setOffreTitre(rs.getString("offre_titre"));
                    c.setCheminCv(rs.getString("cv"));
                    c.setCandidatNom(rs.getString("candidat_nom") == null ? "" : rs.getString("candidat_nom").trim());
                    c.setCandidatEmail(rs.getString("candidat_email"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    public boolean update(Candidature c) {
        String sql = "UPDATE candidature SET date_candidature=?, statut=?, candidat_id=?, offre_id=?, cv=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(c.getDateCandidature()));
            ps.setString(2, c.getStatut());
            ps.setInt(3, c.getCandidatId());
            ps.setInt(4, c.getOffreId());
            ps.setString(5, c.getCheminCv());
            ps.setInt(6, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean updateStatut(int id, String statut) {
        String sql = "UPDATE candidature SET statut=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM candidature WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public int countByStatut(String statut) {
        String sql = "SELECT COUNT(*) AS total FROM candidature WHERE statut = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
}
