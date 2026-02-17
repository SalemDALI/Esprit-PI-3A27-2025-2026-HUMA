package tests;

import model.Participant;
import services.CrudParticipant;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParticipantServiceTest {

    static CrudParticipant service;
    static int idParticipantTest = -1;

    static final int EMPLOYE_ID_TEST = 1;
    static final int FORMATION_ID_TEST = 1;

    @BeforeAll
    static void setup() {
        service = new CrudParticipant();
    }

    @Test
    @Order(1)
    @DisplayName("Test ajout d'une participation")
    void testAjouterParticipant() {
        Date dateInscription = Date.valueOf("2024-03-15");
        String resultat = "En attente";

        Participant p = new Participant(dateInscription, resultat, EMPLOYE_ID_TEST, FORMATION_ID_TEST);
        service.ajouter(p);

        List<Participant> list = service.afficherAll();
        assertFalse(list.isEmpty(), "La liste des participations ne doit pas être vide");

        Participant found = list.stream()
                .filter(x -> resultat.equals(x.getResultat()) &&
                        x.getEmployeId() == EMPLOYE_ID_TEST &&
                        x.getFormationId() == FORMATION_ID_TEST)
                .findFirst()
                .orElse(null);

        assertNotNull(found, "La participation ajoutée n'a pas été trouvée dans la liste.");

        idParticipantTest = found.getId();
        assertTrue(idParticipantTest > 0, "L'ID doit être positif");
        assertEquals(EMPLOYE_ID_TEST, found.getEmployeId());
        assertEquals(FORMATION_ID_TEST, found.getFormationId());
    }

    @Test
    @Order(2)
    @DisplayName("Test récupération de toutes les participations")
    void testAfficherAll() {
        List<Participant> list = service.afficherAll();
        assertNotNull(list, "La liste ne doit pas être null");
        assertFalse(list.isEmpty(), "La liste doit contenir au moins une participation");
    }

    @Test
    @Order(3)
    @DisplayName("Test modification d'une participation")
    void testModifierParticipant() {
        assertTrue(idParticipantTest > 0, "Ajout pas fait -> idParticipantTest invalide.");

        Participant p = new Participant(
                idParticipantTest,
                Date.valueOf("2024-04-01"),
                "Validé",
                EMPLOYE_ID_TEST,
                FORMATION_ID_TEST
        );

        service.modifier(p);

        List<Participant> list = service.afficherAll();
        boolean updated = list.stream().anyMatch(x ->
                x.getId() == idParticipantTest &&
                        "Validé".equals(x.getResultat())
        );

        assertTrue(updated, "La modification n'a pas été appliquée.");
    }

    @Test
    @Order(4)
    @DisplayName("Test ajout participation avec résultat Échoué")
    void testAjouterParticipantEchoue() {
        Participant p = new Participant(
                Date.valueOf("2024-05-10"),
                "Échoué",
                EMPLOYE_ID_TEST,
                FORMATION_ID_TEST
        );
        service.ajouter(p);

        List<Participant> list = service.afficherAll();
        Participant found = list.stream()
                .filter(x -> "Échoué".equals(x.getResultat()) &&
                        x.getEmployeId() == EMPLOYE_ID_TEST)
                .findFirst()
                .orElse(null);

        assertNotNull(found, "La participation Échoué doit être trouvée");
        assertEquals("Échoué", found.getResultat());

        service.supprimer(found.getId());
    }

    @Test
    @Order(5)
    @DisplayName("Test suppression d'une participation")
    void testSupprimerParticipant() {
        Participant p = new Participant(
                Date.valueOf("2024-07-20"),
                "En attente",
                EMPLOYE_ID_TEST,
                FORMATION_ID_TEST
        );
        service.ajouter(p);

        List<Participant> list = service.afficherAll();
        Participant found = list.stream()
                .filter(x -> "En attente".equals(x.getResultat()) &&
                        x.getEmployeId() == EMPLOYE_ID_TEST)
                .reduce((first, second) -> second)
                .orElse(null);

        assertNotNull(found, "La participation à supprimer doit exister.");

        service.supprimer(found.getId());

        list = service.afficherAll();
        boolean encorePresente = list.stream()
                .anyMatch(x -> x.getId() == found.getId());

        assertFalse(encorePresente, "La participation supprimée ne doit plus être dans la liste.");
    }

    @Test
    @Order(6)
    @DisplayName("Test que toutes les participations ont des données valides")
    void testDonneesValides() {
        List<Participant> list = service.afficherAll();

        for (Participant p : list) {
            assertTrue(p.getId() > 0, "L'ID doit être positif");
            assertTrue(p.getEmployeId() > 0, "L'employeId doit être positif pour ID: " + p.getId());
            assertTrue(p.getFormationId() > 0, "Le formationId doit être positif pour ID: " + p.getId());
            assertNotNull(p.getDateInscription(), "La date d'inscription ne doit pas être null pour ID: " + p.getId());
        }
    }

    @AfterAll
    static void cleanUp() {
        if (idParticipantTest > 0) {
            service.supprimer(idParticipantTest);
            System.out.println("Nettoyage: Participation de test supprimée (ID: " + idParticipantTest + ")");
        }
    }
}