package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Absence;
import services.ServiceAbsence;

import java.time.LocalDate;

public class DemandeCongeController {

    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> typeAbsence;

    private final ServiceAbsence service = new ServiceAbsence();

    private int employeId; // à injecter après login

    public void setEmployeId(int id){
        this.employeId = id;
    }

    @FXML
    public void handleDemande() {
        Absence a = new Absence(
                employeId,
                dateDebut.getValue(),
                dateFin.getValue(),
                typeAbsence.getValue(),
                "EN_ATTENTE"
        );

        if(service.demanderConge(a)){
            new Alert(Alert.AlertType.INFORMATION, "Demande envoyée").show();
        }
    }
}
