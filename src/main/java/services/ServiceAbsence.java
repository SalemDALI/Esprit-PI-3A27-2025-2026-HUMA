package services;

import models.Absence;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAbsence {

    private final Connection cnx = MyDatabase.getInstance().getCnx();

    // EMPLOYE → Demander congé
    public boolean demanderConge(Absence a) {
        String sql = "INSERT INTO absence (employe_id, date_debut, date_fin, type_absence, statut) "
                + "VALUES (?, ?, ?, ?, 'EN_ATTENTE')";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, a.getEmployeId());
            ps.setDate(2, Date.valueOf(a.getDateDebut()));
            ps.setDate(3, Date.valueOf(a.getDateFin()));
            ps.setString(4, a.getTypeAbsence());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // EMPLOYE → Voir ses absences
    public List<Absence> getByEmploye(int employeId) {
        List<Absence> list = new ArrayList<>();
        String sql = "SELECT * FROM absence WHERE employe_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, employeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Absence a = map(rs);
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // MANAGER → Voir équipe
    public List<Absence> getAbsencesEquipe(int managerId) {
        List<Absence> list = new ArrayList<>();
        String sql = """
                SELECT a.*, CONCAT(u.nom, ' ', u.prenom) AS employe_nom
                FROM absence a
                JOIN users u ON a.employe_id = u.id
                WHERE u.manager_id = ?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // MANAGER → Voir conges de son equipe
    public List<Absence> getCongesEquipe(int managerId) {
        List<Absence> list = new ArrayList<>();
        String sql = """
                SELECT a.*, CONCAT(u.nom, ' ', u.prenom) AS employe_nom
                FROM absence a
                JOIN users u ON a.employe_id = u.id
                WHERE u.manager_id = ? AND a.type_absence = 'CONGE'
                ORDER BY a.date_debut DESC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ADMIN RH -> Liste des conges
    public List<Absence> getCongesAdmin() {
        return getByTypeForAdmin("CONGE");
    }

    // ADMIN RH -> Liste des absences hors conge
    public List<Absence> getAbsencesAdmin() {
        List<Absence> list = new ArrayList<>();
        String sql = """
                SELECT a.*, CONCAT(u.nom, ' ', u.prenom) AS employe_nom
                FROM absence a
                JOIN users u ON a.employe_id = u.id
                WHERE a.type_absence <> 'CONGE'
                ORDER BY a.date_debut DESC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addAdmin(Absence a) {
        String sql = "INSERT INTO absence (employe_id, date_debut, date_fin, type_absence, statut) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, a.getEmployeId());
            ps.setDate(2, Date.valueOf(a.getDateDebut()));
            ps.setDate(3, Date.valueOf(a.getDateFin()));
            ps.setString(4, a.getTypeAbsence());
            ps.setString(5, a.getStatut());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAdmin(Absence a) {
        String sql = "UPDATE absence SET employe_id=?, date_debut=?, date_fin=?, type_absence=?, statut=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, a.getEmployeId());
            ps.setDate(2, Date.valueOf(a.getDateDebut()));
            ps.setDate(3, Date.valueOf(a.getDateFin()));
            ps.setString(4, a.getTypeAbsence());
            ps.setString(5, a.getStatut());
            ps.setInt(6, a.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAdmin(int id) {
        String sql = "DELETE FROM absence WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // MANAGER → Accepter / Refuser
    public boolean changerStatut(int id, String statut) {
        String sql = "UPDATE absence SET statut=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateByEmploye(Absence a, int employeId) {
        String sql = "UPDATE absence SET date_debut=?, date_fin=?, type_absence=? " +
                "WHERE id=? AND employe_id=? AND statut='EN_ATTENTE'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(a.getDateDebut()));
            ps.setDate(2, Date.valueOf(a.getDateFin()));
            ps.setString(3, a.getTypeAbsence());
            ps.setInt(4, a.getId());
            ps.setInt(5, employeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteByEmploye(int absenceId, int employeId) {
        String sql = "DELETE FROM absence WHERE id=? AND employe_id=? AND statut='EN_ATTENTE'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, absenceId);
            ps.setInt(2, employeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<Absence> getByTypeForAdmin(String typeAbsence) {
        List<Absence> list = new ArrayList<>();
        String sql = """
                SELECT a.*, CONCAT(u.nom, ' ', u.prenom) AS employe_nom
                FROM absence a
                JOIN users u ON a.employe_id = u.id
                WHERE a.type_absence = ?
                ORDER BY a.date_debut DESC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, typeAbsence);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Absence map(ResultSet rs) throws SQLException {
        Absence a = new Absence();
        a.setId(rs.getInt("id"));
        a.setEmployeId(rs.getInt("employe_id"));
        try {
            a.setEmployeNom(rs.getString("employe_nom"));
        } catch (SQLException ignored) {
            a.setEmployeNom(null);
        }
        a.setDateDebut(rs.getDate("date_debut").toLocalDate());
        a.setDateFin(rs.getDate("date_fin").toLocalDate());
        a.setTypeAbsence(rs.getString("type_absence"));
        a.setStatut(rs.getString("statut"));
        return a;
    }
}
