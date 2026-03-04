package services.feedback;

import models.feedback.Feedback;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceFeedback {

    public static final String[] CATEGORIES = {"Bug", "Suggestion", "Félicitation", "Autre"};
    public static final String[] STATUSES = {"nouveau", "en_cours", "resolu"};

    private final Connection cnx;

    public ServiceFeedback() { cnx = MyDatabase.getInstance().getCnx(); }

    /** Admin: all feedbacks (optional search). */
    public List<Feedback> getFiltered(String search) {
        return getFilteredByEmployeId(null, search);
    }

    /** Admin (employeId=null) or Employee (employeId=current user id). */
    public List<Feedback> getFilteredByEmployeId(Integer employeId, String search) {
        List<Feedback> list = new ArrayList<>();
        try {
            String sql = "SELECT id, contenu, date_envoi, est_anonyme, employe_id, admin_id, category, status " +
                    "FROM feedback WHERE contenu LIKE ?";
            if (employeId != null) sql += " AND employe_id=?";
            sql += " ORDER BY date_envoi DESC, id DESC";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, "%" + search + "%");
            if (employeId != null) ps.setInt(2, employeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** For export: admin list with optional date/category/status filters. */
    public List<Feedback> getForExport(java.util.Date dateFrom, java.util.Date dateTo, String category, String status) {
        List<Feedback> list = new ArrayList<>();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT id, contenu, date_envoi, est_anonyme, employe_id, admin_id, category, status FROM feedback WHERE 1=1");
            List<Object> params = new ArrayList<>();
            if (dateFrom != null) {
                sql.append(" AND date_envoi >= ?");
                params.add(new Timestamp(dateFrom.getTime()));
            }
            if (dateTo != null) {
                sql.append(" AND date_envoi <= ?");
                params.add(new Timestamp(dateTo.getTime() + 86400000 - 1));
            }
            if (category != null && !category.isBlank()) {
                sql.append(" AND category = ?");
                params.add(category);
            }
            if (status != null && !status.isBlank()) {
                sql.append(" AND status = ?");
                params.add(status);
            }
            sql.append(" ORDER BY date_envoi DESC, id DESC");
            PreparedStatement ps = cnx.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Integer ajouter(Feedback f) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "INSERT INTO feedback(contenu, date_envoi, est_anonyme, employe_id, admin_id, category, status) " +
                            "VALUES(?, NOW(), ?, ?, NULL, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, f.getContenu());
            ps.setBoolean(2, f.isEstAnonyme());
            if (f.getEmployeId() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, f.getEmployeId());
            ps.setString(4, f.getCategory() != null ? f.getCategory() : "Autre");
            ps.setString(5, f.getStatus() != null ? f.getStatus() : "nouveau");
            int rows = ps.executeUpdate();
            if (rows == 0) return null;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean update(Feedback f) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE feedback SET contenu=?, est_anonyme=?, employe_id=?, admin_id=?, category=?, status=? WHERE id=?");
            ps.setString(1, f.getContenu());
            ps.setBoolean(2, f.isEstAnonyme());
            if (f.getEmployeId() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, f.getEmployeId());
            if (f.getAdminId() == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, f.getAdminId());
            ps.setString(5, f.getCategory() != null ? f.getCategory() : "Autre");
            ps.setString(6, f.getStatus() != null ? f.getStatus() : "nouveau");
            ps.setInt(7, f.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try {
            PreparedStatement ps = cnx.prepareStatement("DELETE FROM feedback WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteByUser(int id, Integer employeId) {
        if (employeId == null) return false;
        try {
            PreparedStatement ps = cnx.prepareStatement("DELETE FROM feedback WHERE id=? AND employe_id=?");
            ps.setInt(1, id);
            ps.setInt(2, employeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateByUser(Feedback f, Integer employeId) {
        if (employeId == null) return false;
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE feedback SET contenu=?, est_anonyme=? WHERE id=? AND employe_id=?");
            ps.setString(1, f.getContenu());
            ps.setBoolean(2, f.isEstAnonyme());
            ps.setInt(3, f.getId());
            ps.setInt(4, employeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Count by category (for analytics). Returns category -> count. */
    public Map<String, Long> getCountByCategory() {
        Map<String, Long> map = new LinkedHashMap<>();
        try {
            String sql = "SELECT COALESCE(category, 'Autre') AS cat, COUNT(*) AS cnt FROM feedback GROUP BY COALESCE(category, 'Autre') ORDER BY cnt DESC";
            try (PreparedStatement ps = cnx.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("cat"), rs.getLong("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    /** Count by status (for analytics). */
    public Map<String, Long> getCountByStatus() {
        Map<String, Long> map = new LinkedHashMap<>();
        try {
            String sql = "SELECT COALESCE(status, 'nouveau') AS st, COUNT(*) AS cnt FROM feedback GROUP BY COALESCE(status, 'nouveau') ORDER BY cnt DESC";
            try (PreparedStatement ps = cnx.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("st"), rs.getLong("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    /** Total count: all (employeId=null) or for one user. */
    public long getTotalCount(Integer employeId) {
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM feedback";
            if (employeId != null) sql += " WHERE employe_id=?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                if (employeId != null) ps.setInt(1, employeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong("cnt");
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Count by month (last 12 months) for period chart. Key = "yyyy-MM". */
    public Map<String, Long> getCountByPeriod() {
        Map<String, Long> map = new LinkedHashMap<>();
        try {
            LocalDate end = LocalDate.now().plusMonths(1);
            for (int i = 11; i >= 0; i--) {
                YearMonth ym = YearMonth.from(end.minusMonths(i));
                map.put(ym.toString(), 0L);
            }
            String sql = "SELECT DATE_FORMAT(date_envoi, '%Y-%m') AS mo, COUNT(*) AS cnt FROM feedback " +
                    "WHERE date_envoi >= ? AND date_envoi < ? GROUP BY DATE_FORMAT(date_envoi, '%Y-%m')";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setObject(1, end.minusMonths(12).atStartOfDay());
                ps.setObject(2, end.atStartOfDay());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) map.put(rs.getString("mo"), rs.getLong("cnt"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    private Feedback map(ResultSet rs) throws SQLException {
        Integer empId = null;
        int e = rs.getInt("employe_id");
        if (!rs.wasNull()) empId = e;
        Integer admId = null;
        int a = rs.getInt("admin_id");
        if (!rs.wasNull()) admId = a;
        Feedback f = new Feedback(
                rs.getInt("id"),
                rs.getString("contenu"),
                rs.getTimestamp("date_envoi"),
                rs.getBoolean("est_anonyme"),
                empId,
                admId
        );
        try {
            f.setCategory(rs.getString("category"));
            f.setStatus(rs.getString("status"));
        } catch (SQLException ignored) { }
        return f;
    }
}
