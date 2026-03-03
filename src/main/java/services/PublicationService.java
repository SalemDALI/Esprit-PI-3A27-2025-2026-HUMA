package services;

import models.Publication;
import models.PublicationComment;
import models.User;
import utils.MyDatabase;
import utils.Session;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class PublicationService {

    private final Connection cnx;

    public PublicationService() {
        this.cnx = MyDatabase.getInstance().getCnx();
    }

    public List<Publication> getAll() {
        Map<Integer, Publication> map = new LinkedHashMap<>();
        String sql = "SELECT p.id AS publication_id, p.contenu AS publication_contenu, p.date_publication, p.type, p.user_id, "
                + "u.nom AS auteur_nom, u.prenom AS auteur_prenom, "
                + "c.id AS commentaire_id, c.contenu AS commentaire_contenu, c.date_commentaire, c.user_id AS commentaire_user_id, "
                + "cu.nom AS commentaire_nom, cu.prenom AS commentaire_prenom "
                + "FROM publication p "
                + "LEFT JOIN users u ON u.id = p.user_id "
                + "LEFT JOIN commentaire c ON c.publication_id = p.id "
                + "LEFT JOIN users cu ON cu.id = c.user_id "
                + "ORDER BY p.date_publication DESC, p.id DESC, c.date_commentaire ASC, c.id ASC";

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int publicationId = rs.getInt("publication_id");
                Publication publication = map.get(publicationId);
                if (publication == null) {
                    publication = new Publication();
                    publication.setId(publicationId);
                    publication.setTitre(rs.getString("type"));
                    publication.setContenu(rs.getString("publication_contenu"));

                    String auteurNom = rs.getString("auteur_nom");
                    String auteurPrenom = rs.getString("auteur_prenom");
                    String auteur = ((auteurNom == null ? "" : auteurNom) + " " + (auteurPrenom == null ? "" : auteurPrenom)).trim();
                    publication.setAuteur(auteur.isBlank() ? "Utilisateur" : auteur);

                    Date datePublication = rs.getDate("date_publication");
                    publication.setDatePublication(datePublication == null ? null : datePublication.toLocalDate().atStartOfDay());
                    map.put(publicationId, publication);
                }

                Object commentaireIdObj = rs.getObject("commentaire_id");
                if (commentaireIdObj != null) {
                    PublicationComment comment = new PublicationComment();
                    comment.setId(((Number) commentaireIdObj).intValue());
                    comment.setPublicationId(publicationId);
                    comment.setUserId(rs.getInt("commentaire_user_id"));
                    comment.setContenu(rs.getString("commentaire_contenu"));

                    String nom = rs.getString("commentaire_nom");
                    String prenom = rs.getString("commentaire_prenom");
                    String auteurComment = ((nom == null ? "" : nom) + " " + (prenom == null ? "" : prenom)).trim();
                    comment.setAuteur(auteurComment.isBlank() ? "Utilisateur" : auteurComment);

                    Date dateCommentaire = rs.getDate("date_commentaire");
                    comment.setDateCommentaire(dateCommentaire == null ? null : dateCommentaire.toLocalDate().atStartOfDay());
                    publication.getCommentaires().add(comment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(map.values());
    }

    public boolean publish(String titre, String contenu, String auteur) {
        User currentUser = Session.getUser();
        if (currentUser == null) {
            return false;
        }

        String sql = "INSERT INTO publication (contenu, date_publication, type, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, contenu);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setString(3, titre);
            ps.setInt(4, currentUser.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Après addComment() → mettre à jour la réputation de l'auteur de la publication
    public boolean addComment(int publicationId, String auteur, String contenu) {
        User currentUser = Session.getUser();
        if (currentUser == null) return false;

        String sql = "INSERT INTO commentaire (contenu, date_commentaire, publication_id, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, contenu);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setInt(3, publicationId);
            ps.setInt(4, currentUser.getId());
            boolean ok = ps.executeUpdate() > 0;

            if (ok) {
                // ✅ Mettre à jour réputation des 2 users
                ReputationService rs = new ReputationService();
                rs.calculerEtSauvegarder(currentUser.getId()); // celui qui commente
                rs.calculerEtSauvegarder(getAuteurIdByPublication(publicationId)); // auteur publication
            }
            return ok;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Après addReaction() → mettre à jour la réputation
    public boolean addReaction(int publicationId, int userId, String type) {
        String sql = "INSERT INTO reaction_publication (publication_id, user_id, type) " +
                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE type = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ps.setInt(2, userId);
            ps.setString(3, type);
            ps.setString(4, type);
            boolean ok = ps.executeUpdate() > 0;

            if (ok) {
                // ✅ Mettre à jour réputation de l'auteur de la publication
                ReputationService rs = new ReputationService();
                rs.calculerEtSauvegarder(getAuteurIdByPublication(publicationId));
            }
            return ok;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Méthode utilitaire pour récupérer l'auteur d'une publication
    private int getAuteurIdByPublication(int publicationId) {
        String sql = "SELECT user_id FROM publication WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }


    public boolean deleteById(int publicationId) {
        String deleteCommentsSql = "DELETE FROM commentaire WHERE publication_id = ?";
        String deletePublicationSql = "DELETE FROM publication WHERE id = ?";

        try {
            boolean autoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);
            try (PreparedStatement deleteComments = cnx.prepareStatement(deleteCommentsSql);
                 PreparedStatement deletePublication = cnx.prepareStatement(deletePublicationSql)) {

                deleteComments.setInt(1, publicationId);
                deleteComments.executeUpdate();

                deletePublication.setInt(1, publicationId);
                int deleted = deletePublication.executeUpdate();

                cnx.commit();
                cnx.setAutoCommit(autoCommit);
                return deleted > 0;
            } catch (SQLException e) {
                cnx.rollback();
                cnx.setAutoCommit(autoCommit);
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePublication(int publicationId, int userId, boolean admin, String titre, String contenu) {
        String sql = admin
                ? "UPDATE publication SET type=?, contenu=? WHERE id=?"
                : "UPDATE publication SET type=?, contenu=? WHERE id=? AND user_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, titre);
            ps.setString(2, contenu);
            ps.setInt(3, publicationId);
            if (!admin) {
                ps.setInt(4, userId);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateComment(int commentId, int userId, boolean admin, String contenu) {
        String sql = admin
                ? "UPDATE commentaire SET contenu=?, date_commentaire=? WHERE id=?"
                : "UPDATE commentaire SET contenu=?, date_commentaire=? WHERE id=? AND user_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, contenu);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setInt(3, commentId);
            if (!admin) {
                ps.setInt(4, userId);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteComment(int commentId, int userId, boolean admin) {
        String sql = admin
                ? "DELETE FROM commentaire WHERE id=?"
                : "DELETE FROM commentaire WHERE id=? AND user_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            if (!admin) {
                ps.setInt(2, userId);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Ajouter un média
    public boolean addMedia(int publicationId, String type, String path) {
        String sql = "INSERT INTO media_publication (publication_id, type, path) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ps.setString(2, type);
            ps.setString(3, path);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Récupérer les médias d'une publication
    public List<Map<String,String>> getMedia(int publicationId) {
        List<Map<String,String>> list = new ArrayList<>();
        String sql = "SELECT type, path FROM media_publication WHERE publication_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,String> media = new HashMap<>();
                media.put("type", rs.getString("type"));
                media.put("path", rs.getString("path"));
                list.add(media);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // Ajoute cette méthode à la classe PublicationService
    public int addPublicationAndGetId(String titre, String contenu) {
        User currentUser = Session.getUser();
        if (currentUser == null) return -1;

        String sql = "INSERT INTO publication (contenu, date_publication, type, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, contenu);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setString(3, titre);
            ps.setInt(4, currentUser.getId());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // retourne l'ID généré
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    // ══ AJOUTER CES 3 MÉTHODES dans PublicationService.java ══



    // Supprimer une réaction (unlike / undislike)
    public boolean removeReaction(int publicationId, int userId) {
        String sql = "DELETE FROM reaction_publication WHERE publication_id=? AND user_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Récupérer le nombre de likes, dislikes et la réaction de l'utilisateur
    public Map<String, Object> getReactions(int publicationId, int userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("likes", 0);
        result.put("dislikes", 0);
        result.put("userReaction", null);

        String countSql = "SELECT " +
                "SUM(CASE WHEN type='LIKE' THEN 1 ELSE 0 END) as likes, " +
                "SUM(CASE WHEN type='DISLIKE' THEN 1 ELSE 0 END) as dislikes " +
                "FROM reaction_publication WHERE publication_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(countSql)) {
            ps.setInt(1, publicationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result.put("likes", rs.getInt("likes"));
                result.put("dislikes", rs.getInt("dislikes"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        String userSql = "SELECT type FROM reaction_publication WHERE publication_id=? AND user_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(userSql)) {
            ps.setInt(1, publicationId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) result.put("userReaction", rs.getString("type"));
        } catch (SQLException e) { e.printStackTrace(); }

        return result;
    }
}
