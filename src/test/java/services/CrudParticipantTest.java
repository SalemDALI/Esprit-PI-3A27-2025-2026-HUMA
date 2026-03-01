package services;

import models.Participant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrudParticipantTest {

    private Connection connection;
    private CrudParticipant crudParticipant;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:participant_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Statement st = connection.createStatement()) {
            st.execute("DROP TABLE IF EXISTS participation");
            st.execute("DROP TABLE IF EXISTS formation");
            st.execute("DROP TABLE IF EXISTS users");

            st.execute("CREATE TABLE users (" +
                    "id INT PRIMARY KEY," +
                    "nom VARCHAR(100)," +
                    "prenom VARCHAR(100)," +
                    "email VARCHAR(150)," +
                    "mdp VARCHAR(255)," +
                    "role VARCHAR(30)," +
                    "manager_id INT)");

            st.execute("CREATE TABLE formation (" +
                    "id INT PRIMARY KEY," +
                    "sujet VARCHAR(150)," +
                    "formateur VARCHAR(100)," +
                    "type VARCHAR(50)," +
                    "date_debut DATE," +
                    "duree INT," +
                    "localisation VARCHAR(150)," +
                    "admin_id INT)");

            st.execute("CREATE TABLE participation (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "date_inscription DATE," +
                    "resultat VARCHAR(50)," +
                    "employe_id INT," +
                    "formation_id INT)");

            st.execute("INSERT INTO users(id,nom,prenom,email,mdp,role,manager_id) VALUES" +
                    "(1,'Ali','Ben','ali@mail.com','x','EMPLOYE',NULL)," +
                    "(2,'Sami','Trabelsi','sami@mail.com','x','EMPLOYE',NULL)");
            st.execute("INSERT INTO formation(id,sujet,formateur,type,date_debut,duree,localisation,admin_id) VALUES" +
                    "(10,'Java','F1','Tech','2026-01-01',4,'Tunis',1)," +
                    "(11,'SQL','F2','Tech','2026-01-02',6,'Sousse',1)");
        }
        crudParticipant = new CrudParticipant(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void ajouter_shouldAllowManyFormationsForSameEmploye_andBlockDuplicatePair() {
        Participant p1 = new Participant();
        p1.setDateInscription(Date.valueOf("2026-02-19"));
        p1.setResultat("EN_COURS");
        p1.setEmployeId(1);
        p1.setFormationId(10);

        Participant p2 = new Participant();
        p2.setDateInscription(Date.valueOf("2026-02-20"));
        p2.setResultat("EN_COURS");
        p2.setEmployeId(1);
        p2.setFormationId(11);

        Participant duplicate = new Participant();
        duplicate.setDateInscription(Date.valueOf("2026-02-21"));
        duplicate.setResultat("TERMINE");
        duplicate.setEmployeId(1);
        duplicate.setFormationId(10);

        assertTrue(crudParticipant.ajouter(p1));
        assertTrue(crudParticipant.ajouter(p2));
        assertFalse(crudParticipant.ajouter(duplicate));

        List<Participant> list = crudParticipant.afficherAll();
        assertEquals(2, list.size());
    }

    @Test
    void afficherAll_shouldReturnJoinedNames() {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO participation(date_inscription,resultat,employe_id,formation_id) VALUES" +
                    "('2026-02-22','VALIDE',2,11)");
        } catch (Exception e) {
            fail(e);
        }

        List<Participant> list = crudParticipant.afficherAll();
        assertEquals(1, list.size());
        assertEquals("Sami Trabelsi", list.get(0).getNomEmploye());
        assertEquals("SQL", list.get(0).getNomFormation());
    }

    @Test
    void modifier_supprimer_exists_shouldWork() {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO participation(date_inscription,resultat,employe_id,formation_id) VALUES" +
                    "('2026-02-22','EN_COURS',2,11)");
        } catch (Exception e) {
            fail(e);
        }

        List<Participant> list = crudParticipant.afficherAll();
        assertEquals(1, list.size());
        Participant p = list.get(0);

        assertTrue(crudParticipant.existsByEmployeAndFormation(2, 11, null));
        assertFalse(crudParticipant.existsByEmployeAndFormation(2, 10, null));

        p.setResultat("TERMINE");
        assertTrue(crudParticipant.modifier(p));

        List<Participant> updated = crudParticipant.afficherAll();
        assertEquals("TERMINE", updated.get(0).getResultat());

        assertTrue(crudParticipant.supprimer(p.getId()));
        assertTrue(crudParticipant.afficherAll().isEmpty());
    }}
