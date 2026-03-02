package services.congesAbsences;

import models.congesAbsences.Absence;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceAbsence {

    private final Connection cnx = MyDatabase.getInstance().getCnx();

    // EMPLOYE -> Demander conge/absence
    public boolean demanderConge(Absence a) {
        String sql = "INSERT INTO absence (employe_id, date_debut, date_fin, type_absence, statut) VALUES (?, ?, ?, ?, 'EN_ATTENTE')";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getEmployeId());
            ps.setDate(2, Date.valueOf(a.getDateDebut()));
            ps.setDate(3, Date.valueOf(a.getDateFin()));
            ps.setString(4, a.getTypeAbsence());
            int affected = ps.executeUpdate();
            if (affected <= 0) {
                return false;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int absenceId = rs.getInt(1);
                    syncCongeRow(absenceId, a.getEmployeId(), a.getTypeAbsence(), null, null, false);
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // EMPLOYE -> Voir ses absences
    public List<Absence> getByEmploye(int employeId) {
        List<Absence> list = new ArrayList<>();
        String sql = "SELECT * FROM absence WHERE employe_id=? ORDER BY date_debut DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, employeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // MANAGER -> Voir equipe
    public List<Absence> getAbsencesEquipe(int managerId) {
        List<Absence> list = new ArrayList<>();
        String sql = """
                SELECT a.*, CONCAT(u.nom, ' ', u.prenom) AS employe_nom
                FROM absence a
                JOIN users u ON a.employe_id = u.id
                WHERE u.manager_id = ?
                ORDER BY a.date_debut DESC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // MANAGER -> Voir conges de son equipe (jointure table conge)
    public List<Absence> getCongesEquipe(int managerId) {
        List<Absence> list = new ArrayList<>();
        String sql = """
                SELECT a.*, CONCAT(u.nom, ' ', u.prenom) AS employe_nom,
                       c.date_demande, c.manager_id AS conge_manager_id, c.commentaire_validation, c.date_validation
                FROM absence a
                JOIN users u ON a.employe_id = u.id
                JOIN conge c ON c.absence_id = a.id
                WHERE u.manager_id = ? AND a.type_absence = 'CONGE'
                ORDER BY a.date_debut DESC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
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
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getEmployeId());
            ps.setDate(2, Date.valueOf(a.getDateDebut()));
            ps.setDate(3, Date.valueOf(a.getDateFin()));
            ps.setString(4, a.getTypeAbsence());
            ps.setString(5, a.getStatut());
            int affected = ps.executeUpdate();
            if (affected <= 0) {
                return false;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int absenceId = rs.getInt(1);
                    syncCongeRow(absenceId, a.getEmployeId(), a.getTypeAbsence(), null, null, false);
                }
            }
            return true;
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
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                syncCongeRow(a.getId(), a.getEmployeId(), a.getTypeAbsence(), null, null, false);
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAdmin(int id) {
        try (PreparedStatement psConge = cnx.prepareStatement("DELETE FROM conge WHERE absence_id=?");
             PreparedStatement psAbsence = cnx.prepareStatement("DELETE FROM absence WHERE id=?")) {
            psConge.setInt(1, id);
            psConge.executeUpdate();
            psAbsence.setInt(1, id);
            return psAbsence.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // MANAGER -> Accepter / Refuser
    public boolean changerStatut(int id, String statut) {
        return changerStatut(id, statut, null, null);
    }

    public boolean changerStatut(int id, String statut, Integer managerId, String commentaireValidation) {
        String sql = "UPDATE absence SET statut=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            boolean ok = ps.executeUpdate() > 0;
            if (!ok) {
                return false;
            }
            ensureCongeValidationUpdated(id, managerId, commentaireValidation);
            return true;
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
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                syncCongeRow(a.getId(), employeId, a.getTypeAbsence(), null, null, false);
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteByEmploye(int absenceId, int employeId) {
        String sql = "DELETE FROM absence WHERE id=? AND employe_id=? AND statut='EN_ATTENTE'";
        try (PreparedStatement psConge = cnx.prepareStatement("DELETE FROM conge WHERE absence_id=?");
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            psConge.setInt(1, absenceId);
            psConge.executeUpdate();
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
        String sqlConge = """
                SELECT a.*, CONCAT(u.nom, ' ', u.prenom) AS employe_nom,
                       c.date_demande, c.manager_id AS conge_manager_id, c.commentaire_validation, c.date_validation
                FROM absence a
                JOIN users u ON a.employe_id = u.id
                JOIN conge c ON c.absence_id = a.id
                WHERE a.type_absence = 'CONGE'
                ORDER BY a.date_debut DESC
                """;
        String sqlAbsence = """
                SELECT a.*, CONCAT(u.nom, ' ', u.prenom) AS employe_nom
                FROM absence a
                JOIN users u ON a.employe_id = u.id
                WHERE a.type_absence = ?
                ORDER BY a.date_debut DESC
                """;

        String sql = "CONGE".equalsIgnoreCase(typeAbsence) ? sqlConge : sqlAbsence;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (!"CONGE".equalsIgnoreCase(typeAbsence)) {
                ps.setString(1, typeAbsence);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
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

        try {
            Date dateDemande = rs.getDate("date_demande");
            a.setDateDemandeConge(dateDemande == null ? null : dateDemande.toLocalDate());
        } catch (SQLException ignored) {
            a.setDateDemandeConge(null);
        }
        try {
            int managerId = rs.getInt("conge_manager_id");
            a.setManagerValidationId(rs.wasNull() ? null : managerId);
        } catch (SQLException ignored) {
            a.setManagerValidationId(null);
        }
        try {
            a.setCommentaireValidation(rs.getString("commentaire_validation"));
        } catch (SQLException ignored) {
            a.setCommentaireValidation(null);
        }
        try {
            Date dateValidation = rs.getDate("date_validation");
            a.setDateValidation(dateValidation == null ? null : dateValidation.toLocalDate());
        } catch (SQLException ignored) {
            a.setDateValidation(null);
        }
        return a;
    }

    private void ensureCongeValidationUpdated(int absenceId, Integer managerId, String commentaireValidation) throws SQLException {
        String sqlAbsence = "SELECT employe_id, type_absence FROM absence WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sqlAbsence)) {
            ps.setInt(1, absenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                int employeId = rs.getInt("employe_id");
                String type = rs.getString("type_absence");
                syncCongeRow(absenceId, employeId, type, managerId, commentaireValidation, true);
            }
        }
    }

    private void syncCongeRow(int absenceId,
                              int employeId,
                              String typeAbsence,
                              Integer managerId,
                              String commentaireValidation,
                              boolean validated) throws SQLException {
        if (!"CONGE".equalsIgnoreCase(typeAbsence)) {
            try (PreparedStatement del = cnx.prepareStatement("DELETE FROM conge WHERE absence_id=?")) {
                del.setInt(1, absenceId);
                del.executeUpdate();
            }
            return;
        }

        int resolvedManager = managerId == null ? resolveManagerIdByEmploye(employeId) : managerId;
        boolean exists = false;
        try (PreparedStatement check = cnx.prepareStatement("SELECT id FROM conge WHERE absence_id=? LIMIT 1")) {
            check.setInt(1, absenceId);
            try (ResultSet rs = check.executeQuery()) {
                exists = rs.next();
            }
        }

        if (exists) {
            String update = "UPDATE conge SET manager_id=?, commentaire_validation=?, date_validation=? WHERE absence_id=?";
            try (PreparedStatement ps = cnx.prepareStatement(update)) {
                if (resolvedManager <= 0) {
                    ps.setNull(1, Types.INTEGER);
                } else {
                    ps.setInt(1, resolvedManager);
                }
                if (commentaireValidation == null || commentaireValidation.isBlank()) {
                    ps.setNull(2, Types.VARCHAR);
                } else {
                    ps.setString(2, commentaireValidation);
                }
                if (validated) {
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                } else {
                    ps.setNull(3, Types.DATE);
                }
                ps.setInt(4, absenceId);
                ps.executeUpdate();
            }
        } else {
            String insert = "INSERT INTO conge (absence_id, date_demande, manager_id, commentaire_validation, date_validation) VALUES (?, CURRENT_DATE, ?, ?, ?)";
            try (PreparedStatement ps = cnx.prepareStatement(insert)) {
                ps.setInt(1, absenceId);
                if (resolvedManager <= 0) {
                    ps.setNull(2, Types.INTEGER);
                } else {
                    ps.setInt(2, resolvedManager);
                }
                if (commentaireValidation == null || commentaireValidation.isBlank()) {
                    ps.setNull(3, Types.VARCHAR);
                } else {
                    ps.setString(3, commentaireValidation);
                }
                if (validated) {
                    ps.setDate(4, Date.valueOf(LocalDate.now()));
                } else {
                    ps.setNull(4, Types.DATE);
                }
                ps.executeUpdate();
            }
        }
    }

    private int resolveManagerIdByEmploye(int employeId) {
        String sql = "SELECT manager_id FROM users WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, employeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("manager_id");
                    return rs.wasNull() ? 0 : id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
