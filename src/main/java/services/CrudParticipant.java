package services;

import models.Participant;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CrudParticipant {

    private final Connection connection;

    public CrudParticipant() {
        this.connection = MyDatabase.getInstance().getCnx();
    }

    CrudParticipant(Connection connection) {
        this.connection = connection;
    }

    public boolean ajouter(Participant p) {
        if (existsByEmployeAndFormation(p.getEmployeId(), p.getFormationId(), null)) {
            return false;
        }
        String sql = "INSERT INTO participation (date_inscription, resultat, employe_id, formation_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, p.getDateInscription());
            pst.setString(2, p.getResultat());
            pst.setInt(3, p.getEmployeId());
            pst.setInt(4, p.getFormationId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Participant> afficherAll() {
        List<Participant> list = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(u.nom, ' ', u.prenom) AS nom_employe, f.sujet AS nom_formation "
                + "FROM participation p "
                + "JOIN users u ON p.employe_id = u.id "
                + "JOIN formation f ON p.formation_id = f.id "
                + "ORDER BY p.id DESC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Participant p = new Participant();
                p.setId(rs.getInt("id"));
                p.setDateInscription(rs.getDate("date_inscription"));
                p.setResultat(rs.getString("resultat"));
                p.setEmployeId(rs.getInt("employe_id"));
                p.setFormationId(rs.getInt("formation_id"));
                p.setNomEmploye(rs.getString("nom_employe"));
                p.setNomFormation(rs.getString("nom_formation"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean modifier(Participant p) {
        if (existsByEmployeAndFormation(p.getEmployeId(), p.getFormationId(), p.getId())) {
            return false;
        }
        String sql = "UPDATE participation SET date_inscription=?, resultat=?, employe_id=?, formation_id=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, p.getDateInscription());
            pst.setString(2, p.getResultat());
            pst.setInt(3, p.getEmployeId());
            pst.setInt(4, p.getFormationId());
            pst.setInt(5, p.getId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM participation WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsByEmployeAndFormation(int employeId, int formationId, Integer excludeId) {
        String sql = "SELECT id FROM participation WHERE employe_id=? AND formation_id=?";
        if (excludeId != null) {
            sql += " AND id<>?";
        }
        sql += " LIMIT 1";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, employeId);
            pst.setInt(2, formationId);
            if (excludeId != null) {
                pst.setInt(3, excludeId);
            }
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

