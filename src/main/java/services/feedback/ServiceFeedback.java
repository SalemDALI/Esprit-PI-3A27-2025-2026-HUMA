package services.feedback;

import models.feedback.Feedback;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFeedback {

    private final Connection cnx;

    public ServiceFeedback() { cnx = MyDatabase.getInstance().getCnx(); }

    /** All feedbacks (admin): optional search and statut filter. */
    public List<Feedback> getFiltered(String search, String statut) {
        return getFilteredByUserEmail(null, search, statut);
    }

    /** Feedbacks for admin (userEmail=null) or for one employee (userEmail=current user email). */
    public List<Feedback> getFilteredByUserEmail(String userEmail, String search, String statut) {
        List<Feedback> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM feedback WHERE (email LIKE ? OR message LIKE ?)";
            int idx = 3;
            if (userEmail != null && !userEmail.isEmpty()) {
                sql += " AND email=?";
            }
            if (statut != null && !statut.isEmpty()) sql += " AND statut=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            if (userEmail != null && !userEmail.isEmpty()) {
                ps.setString(3, userEmail);
                idx = 4;
            }
            if (statut != null && !statut.isEmpty()) ps.setString(idx, statut);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean ajouter(Feedback f) {
        try {
            PreparedStatement ps = cnx.prepareStatement("INSERT INTO feedback(email,message,statut) VALUES(?,?,?)");
            ps.setString(1, f.getEmail());
            ps.setString(2, f.getMessage());
            ps.setString(3, f.getStatut());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Feedback f) {
        try {
            PreparedStatement ps = cnx.prepareStatement("UPDATE feedback SET email=?, message=?, statut=? WHERE id=?");
            ps.setString(1, f.getEmail());
            ps.setString(2, f.getMessage());
            ps.setString(3, f.getStatut());
            ps.setInt(4, f.getId());
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

    /** Employee: delete only if feedback belongs to this email. */
    public boolean deleteByUser(int id, String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return false;
        try {
            PreparedStatement ps = cnx.prepareStatement("DELETE FROM feedback WHERE id=? AND email=?");
            ps.setInt(1, id);
            ps.setString(2, userEmail);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Employee: update only if feedback belongs to this email (message only; statut remains). */
    public boolean updateByUser(Feedback f, String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return false;
        try {
            PreparedStatement ps = cnx.prepareStatement("UPDATE feedback SET message=? WHERE id=? AND email=?");
            ps.setString(1, f.getMessage());
            ps.setInt(2, f.getId());
            ps.setString(3, userEmail);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Feedback map(ResultSet rs) throws SQLException {
        return new Feedback(rs.getInt("id"), rs.getString("message"), rs.getString("email"), rs.getString("statut"));
    }
}