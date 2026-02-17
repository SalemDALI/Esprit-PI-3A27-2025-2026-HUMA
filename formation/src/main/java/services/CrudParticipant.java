package services;

import model.Participant;
import utils.DBconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudParticipant {

    private Connection connection;

    public CrudParticipant() {
        connection = DBconnection.getInstance().getConnection();
    }

    // CREATE : Ajouter une participation
    public void ajouter(Participant p) {
        // L'ID est en auto-increment dans MySQL, on ne l'insère pas manuellement
        String sql = "INSERT INTO participation (date_inscription, resultat, employe_id, formation_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, p.getDateInscription());
            pst.setString(2, p.getResultat());
            pst.setInt(3, p.getEmployeId());
            pst.setInt(4, p.getFormationId());

            pst.executeUpdate();
            System.out.println("✅ Participation insérée en base de données !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'insertion : " + e.getMessage());
        }
    }

    // READ : Afficher tous les participants
    public List<Participant> afficherAll() {
        List<Participant> liste = new ArrayList<>();
        String sql = "SELECT * FROM participation";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Participant p = new Participant(
                        rs.getInt("id"),
                        rs.getDate("date_inscription"),
                        rs.getString("resultat"),
                        rs.getInt("employe_id"),
                        rs.getInt("formation_id")
                );
                liste.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la lecture : " + e.getMessage());
        }
        return liste;
    }

    // UPDATE : Modifier une participation
    public void modifier(Participant p) {
        String sql = "UPDATE participation SET date_inscription=?, resultat=?, employe_id=?, formation_id=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, p.getDateInscription());
            pst.setString(2, p.getResultat());
            pst.setInt(3, p.getEmployeId());
            pst.setInt(4, p.getFormationId());
            pst.setInt(5, p.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Participation ID " + p.getId() + " mise à jour !");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }

    // DELETE : Supprimer une participation
    public void supprimer(int id) {
        String sql = "DELETE FROM participation WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("✅ Participation ID " + id + " supprimée !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }
}