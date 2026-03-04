package services.feedback;

import models.feedback.FeedbackComment;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFeedbackComment {

    private final Connection cnx;

    public ServiceFeedbackComment() { cnx = MyDatabase.getInstance().getCnx(); }

    public boolean add(FeedbackComment c) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "INSERT INTO feedback_comments(feedback_id, user_id, contenu) VALUES(?, ?, ?)");
            ps.setInt(1, c.getFeedbackId());
            ps.setInt(2, c.getUserId());
            ps.setString(3, c.getContenu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<FeedbackComment> getByFeedbackId(int feedbackId) {
        List<FeedbackComment> list = new ArrayList<>();
        try {
            String sql = "SELECT c.id, c.feedback_id, c.user_id, c.contenu, c.date_creation, " +
                    "CONCAT(u.prenom, ' ', u.nom) AS user_nom FROM feedback_comments c " +
                    "LEFT JOIN users u ON u.id = c.user_id WHERE c.feedback_id = ? ORDER BY c.date_creation ASC";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, feedbackId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private FeedbackComment map(ResultSet rs) throws SQLException {
        FeedbackComment c = new FeedbackComment();
        c.setId(rs.getInt("id"));
        c.setFeedbackId(rs.getInt("feedback_id"));
        c.setUserId(rs.getInt("user_id"));
        c.setContenu(rs.getString("contenu"));
        c.setDateCreation(rs.getTimestamp("date_creation"));
        try {
            c.setUserNom(rs.getString("user_nom"));
        } catch (SQLException ignored) { }
        return c;
    }
}
