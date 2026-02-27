package services.congesAbsences;

import models.congesAbsences.Conge;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceConge {

    private final Connection cnx = MyDatabase.getInstance().getCnx();

    public boolean demanderConge(int employeId, LocalDate dateDebut, LocalDate dateFin) {
        if (employeId <= 0 || dateDebut == null || dateFin == null || dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("Parametres de conge invalides.");
        }

        String insertAbsence = "INSERT INTO absence (employe_id, date_debut, date_fin, type_absence, statut) VALUES (?, ?, ?, 'CONGE', 'EN_ATTENTE')";
        String insertConge = "INSERT INTO conge (absence_id, date_demande, manager_id, commentaire_validation, date_validation) VALUES (?, CURRENT_DATE, ?, NULL, NULL)";

        boolean previousAutoCommit;
        try {
            previousAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            int absenceId;
            try (PreparedStatement ps = cnx.prepareStatement(insertAbsence, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, employeId);
                ps.setDate(2, Date.valueOf(dateDebut));
                ps.setDate(3, Date.valueOf(dateFin));
                if (ps.executeUpdate() <= 0) {
                    cnx.rollback();
                    return false;
                }
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        cnx.rollback();
                        return false;
                    }
                    absenceId = keys.getInt(1);
                }
            }

            Integer managerId = getManagerIdByEmploye(employeId);
            try (PreparedStatement ps = cnx.prepareStatement(insertConge)) {
                ps.setInt(1, absenceId);
                if (managerId == null) {
                    ps.setNull(2, Types.INTEGER);
                } else {
                    ps.setInt(2, managerId);
                }
                if (ps.executeUpdate() <= 0) {
                    cnx.rollback();
                    return false;
                }
            }

            cnx.commit();
            cnx.setAutoCommit(previousAutoCommit);
            return true;
        } catch (SQLException e) {
            try {
                cnx.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        }
    }

    public List<Conge> getByEmploye(int employeId) {
        String sql = """
                SELECT c.id, c.absence_id, c.date_demande, c.manager_id, c.commentaire_validation, c.date_validation,
                       a.employe_id, a.date_debut, a.date_fin, a.statut,
                       CONCAT(u.nom, ' ', u.prenom) AS employe_nom
                FROM conge c
                JOIN absence a ON a.id = c.absence_id
                JOIN users u ON u.id = a.employe_id
                WHERE a.employe_id = ?
                ORDER BY a.date_debut DESC
                """;
        return queryList(sql, ps -> ps.setInt(1, employeId));
    }

    public List<Conge> getByManager(int managerId) {
        String sql = """
                SELECT c.id, c.absence_id, c.date_demande, c.manager_id, c.commentaire_validation, c.date_validation,
                       a.employe_id, a.date_debut, a.date_fin, a.statut,
                       CONCAT(u.nom, ' ', u.prenom) AS employe_nom
                FROM conge c
                JOIN absence a ON a.id = c.absence_id
                JOIN users u ON u.id = a.employe_id
                WHERE u.manager_id = ?
                ORDER BY a.date_debut DESC
                """;
        return queryList(sql, ps -> ps.setInt(1, managerId));
    }

    public List<Conge> getAll() {
        String sql = """
                SELECT c.id, c.absence_id, c.date_demande, c.manager_id, c.commentaire_validation, c.date_validation,
                       a.employe_id, a.date_debut, a.date_fin, a.statut,
                       CONCAT(u.nom, ' ', u.prenom) AS employe_nom
                FROM conge c
                JOIN absence a ON a.id = c.absence_id
                JOIN users u ON u.id = a.employe_id
                ORDER BY a.date_debut DESC
                """;
        return queryList(sql, ps -> {
        });
    }

    public boolean validerConge(int absenceId, int managerId, String statut, String commentaireValidation) {
        if (!"ACCEPTE".equalsIgnoreCase(statut) && !"REFUSE".equalsIgnoreCase(statut) && !"EN_ATTENTE".equalsIgnoreCase(statut)) {
            throw new IllegalArgumentException("Statut conge invalide.");
        }

        String updateAbsence = "UPDATE absence SET statut=? WHERE id=? AND type_absence='CONGE'";
        String updateConge = "UPDATE conge SET manager_id=?, commentaire_validation=?, date_validation=? WHERE absence_id=?";

        boolean previousAutoCommit;
        try {
            previousAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            try (PreparedStatement ps = cnx.prepareStatement(updateAbsence)) {
                ps.setString(1, statut.toUpperCase());
                ps.setInt(2, absenceId);
                if (ps.executeUpdate() <= 0) {
                    cnx.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = cnx.prepareStatement(updateConge)) {
                ps.setInt(1, managerId);
                if (commentaireValidation == null || commentaireValidation.isBlank()) {
                    ps.setNull(2, Types.VARCHAR);
                } else {
                    ps.setString(2, commentaireValidation);
                }
                if ("EN_ATTENTE".equalsIgnoreCase(statut)) {
                    ps.setNull(3, Types.DATE);
                } else {
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                }
                ps.setInt(4, absenceId);
                if (ps.executeUpdate() <= 0) {
                    cnx.rollback();
                    return false;
                }
            }

            cnx.commit();
            cnx.setAutoCommit(previousAutoCommit);
            return true;
        } catch (SQLException e) {
            try {
                cnx.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean modifierCongeByEmploye(int absenceId, int employeId, LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null || dateFin == null || dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("Dates invalides.");
        }
        String sql = "UPDATE absence SET date_debut=?, date_fin=? WHERE id=? AND employe_id=? AND type_absence='CONGE' AND statut='EN_ATTENTE'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(dateDebut));
            ps.setDate(2, Date.valueOf(dateFin));
            ps.setInt(3, absenceId);
            ps.setInt(4, employeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimerCongeByEmploye(int absenceId, int employeId) {
        String deleteConge = "DELETE c FROM conge c JOIN absence a ON a.id = c.absence_id WHERE c.absence_id=? AND a.employe_id=? AND a.statut='EN_ATTENTE'";
        String deleteAbsence = "DELETE FROM absence WHERE id=? AND employe_id=? AND type_absence='CONGE' AND statut='EN_ATTENTE'";

        boolean previousAutoCommit;
        try {
            previousAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            try (PreparedStatement ps = cnx.prepareStatement(deleteConge)) {
                ps.setInt(1, absenceId);
                ps.setInt(2, employeId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = cnx.prepareStatement(deleteAbsence)) {
                ps.setInt(1, absenceId);
                ps.setInt(2, employeId);
                boolean ok = ps.executeUpdate() > 0;
                if (!ok) {
                    cnx.rollback();
                    return false;
                }
            }

            cnx.commit();
            cnx.setAutoCommit(previousAutoCommit);
            return true;
        } catch (SQLException e) {
            try {
                cnx.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        }
    }

    public int countByStatut(String statut) {
        String sql = "SELECT COUNT(*) AS total FROM conge c JOIN absence a ON a.id = c.absence_id WHERE a.statut = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Integer getManagerIdByEmploye(int employeId) {
        String sql = "SELECT manager_id FROM users WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, employeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("manager_id");
                    return rs.wasNull() ? null : id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Conge> queryList(String sql, SqlConsumer<PreparedStatement> binder) {
        List<Conge> out = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            binder.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    private Conge map(ResultSet rs) throws SQLException {
        Conge c = new Conge();
        c.setId(rs.getInt("id"));
        c.setAbsenceId(rs.getInt("absence_id"));
        c.setEmployeId(rs.getInt("employe_id"));
        c.setEmployeNom(rs.getString("employe_nom"));
        c.setDateDebut(rs.getDate("date_debut").toLocalDate());
        c.setDateFin(rs.getDate("date_fin").toLocalDate());
        c.setStatut(rs.getString("statut"));

        Date dateDemande = rs.getDate("date_demande");
        c.setDateDemande(dateDemande == null ? null : dateDemande.toLocalDate());

        int managerId = rs.getInt("manager_id");
        c.setManagerId(rs.wasNull() ? null : managerId);

        c.setCommentaireValidation(rs.getString("commentaire_validation"));

        Date dateValidation = rs.getDate("date_validation");
        c.setDateValidation(dateValidation == null ? null : dateValidation.toLocalDate());
        return c;
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
