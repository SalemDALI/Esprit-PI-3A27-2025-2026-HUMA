package services;

import model.Formation;
import utils.DBconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour la table 'formation'
 * Gère les opérations JDBC pour l'application JavaFX
 */
public class CrudFormation {

    private Connection conn;

    public CrudFormation() {
        // Récupération de la connexion via le Singleton DBConnection
        this.conn = DBconnection.getInstance().getConnection();
    }

    /**
     * AJOUTER : Insère une nouvelle formation en base de données
     * Note: J'ai corrigé 'dateDebut' en 'date_debut' pour correspondre à ta DB
     */
    public void ajouter(Formation f) throws SQLException {
        String sql = "INSERT INTO formation (formateur, type, date_debut, duree, localisation, sujet, admin_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getFormateur());
            ps.setString(2, f.getType());
            ps.setDate(3, f.getDateDebut());
            ps.setInt(4, f.getDuree());
            ps.setString(5, f.getLocalisation());
            ps.setString(6, f.getSujet());
            ps.setInt(7, 1); // admin_id par défaut à 1 comme sur tes captures

            ps.executeUpdate();
            System.out.println("Formation ajoutée avec succès !");
        }
    }

    /**
     * MODIFIER : Met à jour les informations d'une formation existante
     */
    public void modifier(Formation f) throws SQLException {
        String sql = "UPDATE formation SET formateur = ?, type = ?, date_debut = ?, duree = ?, localisation = ?, sujet = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getFormateur());
            ps.setString(2, f.getType());
            ps.setDate(3, f.getDateDebut());
            ps.setInt(4, f.getDuree());
            ps.setString(5, f.getLocalisation());
            ps.setString(6, f.getSujet());
            ps.setInt(7, f.getId());

            ps.executeUpdate();
            System.out.println("Formation ID " + f.getId() + " modifiée !");
        }
    }

    /**
     * SUPPRIMER : Efface une formation par son ID
     */
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM formation WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Formation supprimée !");
        }
    }

    /**
     * AFFICHER TOUT : Récupère la liste complète des formations
     */
    public List<Formation> afficherAll() throws SQLException {
        List<Formation> liste = new ArrayList<>();
        String sql = "SELECT * FROM formation";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                // Respecter l'ordre du constructeur : id, sujet, formateur, type, dateDebut, duree, localisation
                Formation f = new Formation(
                        rs.getInt("id"),
                        rs.getString("sujet"),
                        rs.getString("formateur"),
                        rs.getString("type"),
                        rs.getDate("date_debut"), // Correction: rs.getDate au lieu de rs.getDateDebut
                        rs.getInt("duree"),
                        rs.getString("localisation")
                );
                liste.add(f);
            }
        }
        return liste;
    }
}