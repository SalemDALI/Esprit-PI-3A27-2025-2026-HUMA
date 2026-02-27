package services.communication;

import models.communication.Publication;
import models.communication.PublicationComment;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public boolean addComment(int publicationId, String auteur, String contenu) {
        User currentUser = Session.getUser();
        if (currentUser == null) {
            return false;
        }

        String sql = "INSERT INTO commentaire (contenu, date_commentaire, publication_id, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, contenu);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setInt(3, publicationId);
            ps.setInt(4, currentUser.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
}
