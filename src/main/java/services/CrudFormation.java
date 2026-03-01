package services;

import models.Formation;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CrudFormation {

    private final Connection conn;

    public CrudFormation() {
        this.conn = MyDatabase.getInstance().getCnx();
    }

    CrudFormation(Connection connection) {
        this.conn = connection;
    }

    public boolean ajouter(Formation f) {
        String sql = "INSERT INTO formation (formateur, type, date_debut, duree, localisation, sujet, admin_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getFormateur());
            ps.setString(2, f.getType());
            ps.setDate(3, f.getDateDebut());
            ps.setInt(4, f.getDuree());
            ps.setString(5, f.getLocalisation());
            ps.setString(6, f.getSujet());
            ps.setInt(7, f.getAdminId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modifier(Formation f) {
        String sql = "UPDATE formation SET formateur=?, type=?, date_debut=?, duree=?, localisation=?, sujet=?, admin_id=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getFormateur());
            ps.setString(2, f.getType());
            ps.setDate(3, f.getDateDebut());
            ps.setInt(4, f.getDuree());
            ps.setString(5, f.getLocalisation());
            ps.setString(6, f.getSujet());
            ps.setInt(7, f.getAdminId());
            ps.setInt(8, f.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM formation WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Formation> afficherAll() {
        List<Formation> list = new ArrayList<>();
        String sql = "SELECT * FROM formation ORDER BY id DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Formation f = new Formation();
                f.setId(rs.getInt("id"));
                f.setSujet(rs.getString("sujet"));
                f.setFormateur(rs.getString("formateur"));
                f.setType(rs.getString("type"));
                f.setDateDebut(rs.getDate("date_debut"));
                f.setDuree(rs.getInt("duree"));
                f.setLocalisation(rs.getString("localisation"));
                f.setAdminId(rs.getInt("admin_id"));
                list.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Integer findIdBySujet(String sujet) {
        String sql = "SELECT id FROM formation WHERE LOWER(sujet)=LOWER(?) LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sujet);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

