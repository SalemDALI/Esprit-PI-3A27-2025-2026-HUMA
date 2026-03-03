package services;

import models.Reputation;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReputationService {

    private final Connection cnx;
    private static final int POINTS_PAR_COMMENTAIRE_FAIT = 2;

    public ReputationService() {
        this.cnx = MyDatabase.getInstance().getCnx();
    }

    // ── Calcul employé seulement ──
    public void calculerEtSauvegarder(int userId) {
        int commentairesFaits = countCommentairesFaits(userId);
        int score = commentairesFaits * POINTS_PAR_COMMENTAIRE_FAIT;
        String badge = determinerBadge(score);
        sauvegarder(new Reputation(userId, score, badge));
    }

    // ── Commentaires faits par l'employé ──
    private int countCommentairesFaits(int userId) {
        String sql = "SELECT COUNT(*) FROM commentaire WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── Sauvegarder ou mettre à jour ──
    private void sauvegarder(Reputation r) {
        String sql = "INSERT INTO reputation (user_id, total_score, badge, last_updated) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "total_score=VALUES(total_score), " +
                "badge=VALUES(badge), " +
                "last_updated=VALUES(last_updated)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getTotalScore());
            ps.setString(3, r.getBadge());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Récupérer réputation d'un user ──
    public Reputation getByUserId(int userId) {
        String sql = "SELECT * FROM reputation WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Reputation r = new Reputation();
                r.setId(rs.getInt("id"));
                r.setUserId(rs.getInt("user_id"));
                r.setTotalScore(rs.getInt("total_score"));
                r.setBadge(rs.getString("badge"));
                return r;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── Top 10 employés ──
    public List<Reputation> getTop10() {
        List<Reputation> list = new ArrayList<>();
        String sql = "SELECT r.*, u.nom, u.prenom FROM reputation r " +
                "JOIN users u ON u.id = r.user_id " +
                "WHERE u.role = 'EMPLOYE' " +
                "ORDER BY r.total_score DESC LIMIT 10";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Reputation r = new Reputation();
                r.setId(rs.getInt("id"));
                r.setUserId(rs.getInt("user_id"));
                r.setTotalScore(rs.getInt("total_score"));
                r.setBadge(rs.getString("badge"));
                r.setNom(rs.getString("nom"));
                r.setPrenom(rs.getString("prenom"));
                list.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Badge selon score ──
    private String determinerBadge(int score) {
        if (score >= 402) return "🏅 ENGAGE";
        if (score >= 102) return "🎖️ ACTIF";
        return "🎗️ NOUVEAU";
    }

}