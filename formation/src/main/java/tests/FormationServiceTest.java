package tests;

import model.Formation;
import services.CrudFormation;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FormationServiceTest {

    static CrudFormation service;
    static int idFormationTest = -1;

    @BeforeAll
    static void setup() {
        service = new CrudFormation();
    }

    @Test
    @Order(1)
    @DisplayName("Test ajout d'une formation")
    void testAjouterFormation() throws Exception {
        String sujet = "JUnit_JavaAvance";
        String formateur = "Mohamed Ali";
        String type = "Presentiel";
        Date dateDebut = Date.valueOf("2024-03-01");
        int duree = 5;
        String localisation = "Tunis";

        Formation f = new Formation(sujet, formateur, type, dateDebut, duree, localisation);
        service.ajouter(f);

        List<Formation> list = service.afficherAll();
        assertFalse(list.isEmpty(), "La liste des formations ne doit pas être vide");

        Formation found = list.stream()
                .filter(x -> sujet.equals(x.getSujet()) &&
                        formateur.equals(x.getFormateur()) &&
                        localisation.equals(x.getLocalisation()))
                .findFirst()
                .orElse(null);

        assertNotNull(found, "La formation ajoutée n'a pas été trouvée dans la liste.");

        idFormationTest = found.getId();
        assertTrue(idFormationTest > 0, "L'ID doit être positif");
        assertEquals(type, found.getType());
        assertEquals(duree, found.getDuree());
    }

    @Test
    @Order(2)
    @DisplayName("Test récupération de toutes les formations")
    void testAfficherAll() throws Exception {
        List<Formation> list = service.afficherAll();
        assertNotNull(list, "La liste ne doit pas être null");
        assertFalse(list.isEmpty(), "La liste doit contenir au moins une formation");
    }

    @Test
    @Order(3)
    @DisplayName("Test modification d'une formation")
    void testModifierFormation() throws Exception {
        assertTrue(idFormationTest > 0, "Ajout pas fait -> idFormationTest invalide.");

        Formation f = new Formation(
                idFormationTest,
                "JUnit_SpringBoot",
                "Ahmed Ben Ali",
                "En ligne",
                Date.valueOf("2024-06-01"),
                10,
                "Sfax"
        );

        service.modifier(f);

        List<Formation> list = service.afficherAll();
        boolean updated = list.stream().anyMatch(x ->
                x.getId() == idFormationTest &&
                        "JUnit_SpringBoot".equals(x.getSujet()) &&
                        "En ligne".equals(x.getType()) &&
                        "Sfax".equals(x.getLocalisation())
        );

        assertTrue(updated, "La modification n'a pas été appliquée.");
    }

    @Test
    @Order(4)
    @DisplayName("Test ajout formation type Hybride")
    void testAjouterFormationHybride() throws Exception {
        Formation f = new Formation(
                "JUnit_Hybride",
                "Sami Trabelsi",
                "Hybride",
                Date.valueOf("2024-09-01"),
                3,
                "Sousse"
        );
        service.ajouter(f);

        List<Formation> list = service.afficherAll();
        Formation found = list.stream()
                .filter(x -> "JUnit_Hybride".equals(x.getSujet()))
                .findFirst()
                .orElse(null);

        assertNotNull(found, "La formation Hybride doit être trouvée");
        assertEquals("Hybride", found.getType());

        service.supprimer(found.getId());
    }

    @Test
    @Order(5)
    @DisplayName("Test suppression d'une formation")
    void testSupprimerFormation() throws Exception {
        Formation f = new Formation(
                "JUnit_ToDelete",
                "Formateur Test",
                "Presentiel",
                Date.valueOf("2024-12-01"),
                2,
                "Bizerte"
        );
        service.ajouter(f);

        List<Formation> list = service.afficherAll();
        Formation found = list.stream()
                .filter(x -> "JUnit_ToDelete".equals(x.getSujet()))
                .findFirst()
                .orElse(null);

        assertNotNull(found, "La formation à supprimer doit exister.");

        service.supprimer(found.getId());

        list = service.afficherAll();
        boolean encorePresente = list.stream()
                .anyMatch(x -> x.getId() == found.getId());

        assertFalse(encorePresente, "La formation supprimée ne doit plus être dans la liste.");
    }

    @Test
    @Order(6)
    @DisplayName("Test que toutes les formations ont des données valides")
    void testDonneesValides() throws Exception {
        List<Formation> list = service.afficherAll();

        for (Formation f : list) {
            assertNotNull(f.getSujet(), "Le sujet ne doit pas être null pour ID: " + f.getId());
            assertFalse(f.getSujet().trim().isEmpty(), "Le sujet ne doit pas être vide pour ID: " + f.getId());
            assertNotNull(f.getFormateur(), "Le formateur ne doit pas être null pour ID: " + f.getId());
            assertTrue(f.getDuree() > 0, "La durée doit être positive pour ID: " + f.getId());
            assertNotNull(f.getDateDebut(), "La date de début ne doit pas être null pour ID: " + f.getId());
        }
    }

    @AfterAll
    static void cleanUp() throws Exception {
        if (idFormationTest > 0) {
            service.supprimer(idFormationTest);
            System.out.println("Nettoyage: Formation de test supprimée (ID: " + idFormationTest + ")");
        }
    }
}
