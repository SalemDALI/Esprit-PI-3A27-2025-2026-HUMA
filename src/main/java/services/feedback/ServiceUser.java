package services.feedback;

import models.feedback.User;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser {

    private final Connection cnx;

    public ServiceUser() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email=? AND mdp=? AND (actif = 1 OR actif IS NULL)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /** Login par image de visage (démonstration: correspondance exacte du BLOB). */
    public User loginWithFace(byte[] faceImage) {
        if (faceImage == null || faceImage.length == 0) return null;
        String sql = "SELECT * FROM users WHERE face_image IS NOT NULL";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                byte[] stored = rs.getBytes("face_image");
                if (stored != null && stored.length == faceImage.length) {
                    // Comparaison binaire simple pour la démo.
                    boolean same = true;
                    for (int i = 0; i < stored.length; i++) {
                        if (stored[i] != faceImage[i]) {
                            same = false;
                            break;
                        }
                    }
                    if (same) {
                        return map(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id DESC";
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

    public boolean ajouter(User user) {
        String sql = "INSERT INTO users(nom, prenom, email, mdp, role, manager_id, face_image) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getMdp());
            ps.setString(5, user.getRole());
            if (user.getManagerId() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, user.getManagerId());
            }
            if (user.getFaceImage() == null) {
                ps.setNull(7, java.sql.Types.BLOB);
            } else {
                ps.setBytes(7, user.getFaceImage());
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Variante qui renvoie l'ID genere (utile pour l'enrollement Face ID externe).
     */
    public Integer ajouterEtRetournerId(User user) {
        String sql = "INSERT INTO users(nom, prenom, email, mdp, role, manager_id, face_image) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getMdp());
            ps.setString(5, user.getRole());
            if (user.getManagerId() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, user.getManagerId());
            }
            if (user.getFaceImage() == null) {
                ps.setNull(7, java.sql.Types.BLOB);
            } else {
                ps.setBytes(7, user.getFaceImage());
            }
            int rows = ps.executeUpdate();
            if (rows == 0) return null;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET nom=?, prenom=?, email=?, mdp=?, role=?, manager_id=?, face_image=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getMdp());
            ps.setString(5, user.getRole());
            if (user.getManagerId() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, user.getManagerId());
            }
            if (user.getFaceImage() == null) {
                ps.setNull(7, java.sql.Types.BLOB);
            } else {
                ps.setBytes(7, user.getFaceImage());
            }
            ps.setInt(8, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) AS total FROM users WHERE role = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, role);
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

    public User getById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMdp(rs.getString("mdp"));
        u.setRole(rs.getString("role"));
        int managerId = rs.getInt("manager_id");
        if (rs.wasNull()) {
            u.setManagerId(null);
        } else {
            u.setManagerId(managerId);
        }
        byte[] face = rs.getBytes("face_image");
        u.setFaceImage(face);
        try {
            int a = rs.getInt("actif");
            u.setActif(rs.wasNull() || a == 1);
        } catch (SQLException ignored) { }
        return u;
    }

    /** Email exists for another user (for profile edit uniqueness). */
    public boolean emailExistsExcludingId(String email, int excludeUserId) {
        String sql = "SELECT id FROM users WHERE email = ? AND id != ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeUserId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Update only nom, prenom, email (for profile). */
    public boolean updateProfile(int id, String nom, String prenom, String email) {
        String sql = "UPDATE users SET nom=?, prenom=?, email=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, email);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Change password for user (current user or admin). */
    public boolean updatePassword(int id, String newPassword) {
        String sql = "UPDATE users SET mdp=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Admin: set user active/inactive (can't login when inactive). */
    public boolean setActive(int userId, boolean active) {
        String sql = "UPDATE users SET actif=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Check current password for profile change-password. */
    public boolean checkPassword(int userId, String password) {
        String sql = "SELECT id FROM users WHERE id=? AND mdp=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean userExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Stocke le token et sa date d'expiration (1h)
    public boolean storeResetToken(String email, String token) {
        String sql = "UPDATE users SET reset_token = ?, token_expiry = DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Récupère un utilisateur par token valide
    public User getByResetToken(String token) {
        String sql = "SELECT * FROM users WHERE reset_token = ? AND token_expiry > NOW()";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) return map(rs);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Met à jour le mot de passe et supprime le token
    public boolean resetPassword(String token, String newHashedPassword) {
        String sql = "UPDATE users SET mdp = ?, reset_token = NULL, token_expiry = NULL WHERE reset_token = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setString(2, token);
            return ps.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
