package services;

import models.Publication;
import models.PublicationComment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PublicationService {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private static final List<Publication> STORE = new ArrayList<>();

    public List<Publication> getAll() {
        List<Publication> copy = new ArrayList<>(STORE);
        copy.sort(Comparator.comparing(Publication::getDatePublication).reversed());
        return copy;
    }

    public void publish(String titre, String contenu, String auteur) {
        Publication p = new Publication();
        p.setId(NEXT_ID.getAndIncrement());
        p.setTitre(titre);
        p.setContenu(contenu);
        p.setAuteur(auteur);
        p.setDatePublication(LocalDateTime.now());
        STORE.add(p);
    }

    public boolean addComment(int publicationId, String auteur, String contenu) {
        Publication p = findById(publicationId);
        if (p == null) {
            return false;
        }
        PublicationComment c = new PublicationComment();
        c.setAuteur(auteur);
        c.setContenu(contenu);
        c.setDateCommentaire(LocalDateTime.now());
        p.getCommentaires().add(c);
        return true;
    }

    public boolean deleteById(int publicationId) {
        for (int i = 0; i < STORE.size(); i++) {
            if (STORE.get(i).getId() == publicationId) {
                STORE.remove(i);
                return true;
            }
        }
        return false;
    }

    private Publication findById(int id) {
        for (Publication p : STORE) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
}
