package services;

import entities.OffreEmploi;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceOffre {

    private final Connection cnx = MyDatabase.getInstance().getCnx();

    public boolean ajouter(OffreEmploi o) {
        String req = "INSERT INTO offre_emploi "
                + "(titre, description, departement, date_publication, type_contrat, nombre_postes, admin_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, o.getTitre());
            ps.setString(2, o.getDescription());
            ps.setString(3, o.getDepartement());
            ps.setDate(4, Date.valueOf(o.getDatePublication()));
            ps.setString(5, o.getTypeContrat());
            ps.setInt(6, o.getNombrePostes());
            ps.setInt(7, o.getAdminId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean ajouter(OffreEmploi o, String roleUser) {
        if (!"ADMIN_RH".equals(roleUser)) {
            throw new RuntimeException("Seul ADMIN_RH peut ajouter une offre !");
        }
        return ajouter(o);
    }

    public boolean update(OffreEmploi o) {
        String sql = "UPDATE offre_emploi SET titre=?, description=?, departement=?, date_publication=?, type_contrat=?, nombre_postes=?, admin_id=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, o.getTitre());
            ps.setString(2, o.getDescription());
            ps.setString(3, o.getDepartement());
            ps.setDate(4, Date.valueOf(o.getDatePublication()));
            ps.setString(5, o.getTypeContrat());
            ps.setInt(6, o.getNombrePostes());
            ps.setInt(7, o.getAdminId());
            ps.setInt(8, o.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM offre_emploi WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<OffreEmploi> getAll() {
        List<OffreEmploi> list = new ArrayList<>();
        String sql = "SELECT * FROM offre_emploi ORDER BY id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OffreEmploi o = new OffreEmploi();
                o.setId(rs.getInt("id"));
                o.setTitre(rs.getString("titre"));
                o.setDescription(rs.getString("description"));
                o.setDepartement(rs.getString("departement"));
                Date d = rs.getDate("date_publication");
                o.setDatePublication(d == null ? null : d.toLocalDate());
                o.setTypeContrat(rs.getString("type_contrat"));
                o.setNombrePostes(rs.getInt("nombre_postes"));
                o.setAdminId(rs.getInt("admin_id"));
                list.add(o);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    public int countPostesOuverts() {
        String sql = "SELECT COALESCE(SUM(nombre_postes), 0) AS total FROM offre_emploi";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
}
