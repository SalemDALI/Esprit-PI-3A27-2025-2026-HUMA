package services;

import models.Formation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrudFormationTest {

    private Connection connection;
    private CrudFormation crudFormation;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:formation_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Statement st = connection.createStatement()) {
            st.execute("DROP TABLE IF EXISTS formation");
            st.execute("CREATE TABLE formation (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "sujet VARCHAR(150)," +
                    "formateur VARCHAR(100)," +
                    "type VARCHAR(50)," +
                    "date_debut DATE," +
                    "duree INT," +
                    "localisation VARCHAR(150)," +
                    "admin_id INT)");
        }
        crudFormation = new CrudFormation(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void ajouter_modifier_supprimer_shouldWork() {
        Formation f = new Formation();
        f.setSujet("JavaFX");
        f.setFormateur("Formateur A");
        f.setType("Technique");
        f.setDateDebut(Date.valueOf("2026-02-19"));
        f.setDuree(8);
        f.setLocalisation("Tunis");
        f.setAdminId(1);

        assertTrue(crudFormation.ajouter(f));

        List<Formation> formations = crudFormation.afficherAll();
        assertEquals(1, formations.size());
        int id = formations.get(0).getId();

        Formation toUpdate = formations.get(0);
        toUpdate.setSujet("Java Avance");
        assertTrue(crudFormation.modifier(toUpdate));

        List<Formation> updated = crudFormation.afficherAll();
        assertEquals("Java Avance", updated.get(0).getSujet());

        assertTrue(crudFormation.supprimer(id));
        assertTrue(crudFormation.afficherAll().isEmpty());
    }

    @Test
    void afficherAll_shouldReturnMappedData() {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO formation(sujet,formateur,type,date_debut,duree,localisation,admin_id) VALUES" +
                    "('SQL','F1','Tech','2026-01-01',4,'Sousse',2)," +
                    "('Spring','F2','Tech','2026-01-02',6,'Tunis',3)");
        } catch (Exception e) {
            fail(e);
        }

        List<Formation> list = crudFormation.afficherAll();
        assertEquals(2, list.size());
        assertNotNull(list.get(0).getSujet());
    }

    @Test
    void findIdBySujet_shouldReturnValueOrNull() {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO formation(sujet,formateur,type,date_debut,duree,localisation,admin_id) VALUES" +
                    "('Docker','F3','Tech','2026-01-03',5,'Ariana',4)");
        } catch (Exception e) {
            fail(e);
        }

        Integer id = crudFormation.findIdBySujet("Docker");
        assertNotNull(id);
        assertNull(crudFormation.findIdBySujet("Inexistante"));
    }
}


