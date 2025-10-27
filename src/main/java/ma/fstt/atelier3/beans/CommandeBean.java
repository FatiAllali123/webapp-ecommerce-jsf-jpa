package ma.fstt.atelier3.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.atelier3.model.Commande;
import ma.fstt.atelier3.model.Internaute;
import ma.fstt.atelier3.model.LigneCommande;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@Getter
@Setter
@SessionScoped
public class CommandeBean implements Serializable {

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    @Inject
    private IternauteBean internauteBean;
    private Internaute client;

    private List<Commande> commandes;
    private Commande selectedCommande;
    private List<LigneCommande> lignesCommande;

    @PostConstruct
    public void init() {
        loadCommandes();
    }


    // Charger les commandes selon le rôle
    public void loadCommandes() {
        if (internauteBean.isAdmin()) {
            commandes = em.createQuery("SELECT c FROM Commande c ORDER BY c.date DESC", Commande.class)
                    .getResultList();
        } else if (internauteBean.isUser()) {
            commandes = em.createQuery(
                            "SELECT c FROM Commande c WHERE c.internaute.id = :userId ORDER BY c.date DESC", Commande.class)
                    .setParameter("userId", internauteBean.getCurrentUser().getId())
                    .getResultList();
        } else {
            commandes = null;
        }
    }


    @Transactional
    public void loadCommandesForClient(Long clientId) {
        if (internauteBean.isAdmin() && clientId != null) {
            this.client = em.find(Internaute.class, clientId);  // Charge le client
            if (this.client != null) {
                commandes = em.createQuery(
                                "SELECT c FROM Commande c WHERE c.internaute.id = :clientId ORDER BY c.date DESC",
                                Commande.class)
                        .setParameter("clientId", clientId)
                        .getResultList();
            } else {
                commandes = new ArrayList<>();
            }
        }
    }


    // Voir les détails d'une commande
    public String voirDetails(Commande commande) {
        this.selectedCommande = em.find(Commande.class, commande.getId());
        this.lignesCommande = em.createQuery(
                        "SELECT lc FROM LigneCommande lc WHERE lc.commande.id = :cmdId", LigneCommande.class)
                .setParameter("cmdId", selectedCommande.getId())
                .getResultList();
   return "/commande/details.xhtml?faces-redirect=true";
    }


    // Retour à la liste
    public String retourListe() {
        loadCommandes();
        return "/commande/liste.xhtml?faces-redirect=true";
    }

    // Utilitaire message
    private void addMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }
}