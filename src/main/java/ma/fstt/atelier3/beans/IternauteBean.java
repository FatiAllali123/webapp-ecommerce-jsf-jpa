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
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.fstt.atelier3.model.Internaute;

import java.io.Serializable;
import java.util.List;

@Named
@Getter
@Setter
@NoArgsConstructor
@SessionScoped

public class IternauteBean implements Serializable {

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    private Internaute currentUser;
    private List<Internaute> allUsers;
    private Internaute selectedUser;

    private Internaute selectedClient;
    @Inject

    private CommandeBean commandeBean;

    @PostConstruct
    public void init() {
        loadUsersIfAdmin();
    }

    // Connexion et Déconnexion
    @Transactional
    public void login(Internaute user) {
        this.currentUser = em.merge(user);
        loadUsersIfAdmin();
    }

    //Logout
    @Transactional
    public String logout() {
        currentUser = null;
        allUsers = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/vitrine.xhtml?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
    public boolean getLoggedIn() {  // Pour #{internauteBean.loggedIn}
        return isLoggedIn();
    }

    public boolean isAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRole());
    }
    public boolean getIsAdmin() {      // ← JSF cherche ÇA !
        return isAdmin();
    }

    public boolean isUser() {
        return currentUser != null && "user".equals(currentUser.getRole());
    }
    public boolean getIsUser() {       // ← JSF cherche ÇA !
        return isUser();
    }

    // Profil
    public String voirProfil(Internaute user) {
        this.selectedUser = user;
        return "/Internaute/profile.xhtml?faces-redirect=true";
    }

    @Transactional
    public void saveProfile() {
        em.merge(currentUser);
        addMessage("Profil mis à jour avec succès !");
    }

    // === Admin ===
    @Transactional
    private void loadUsersIfAdmin() {
        if (isAdmin()) {
            allUsers = em.createQuery("SELECT i FROM Internaute i WHERE i.role = 'user'", Internaute.class)
                    .getResultList();
        } else {
            allUsers = null;
        }
    }

    @Transactional
    public void desactiver(Internaute user) {
        user.setStatut_compte("desactive");
        em.merge(user);
        loadUsersIfAdmin();
        addMessage("Compte désactivé : " + user.getEmail());
    }

    @Transactional
    public void activer(Internaute user) {
        user.setStatut_compte("active");
        em.merge(user);
        loadUsersIfAdmin();
        addMessage("Compte activé : " + user.getEmail());
    }

    @Transactional
    public void supprimer(Internaute user) {
        Internaute managed = em.find(Internaute.class, user.getId());
        if (managed != null) {
            em.remove(managed);
            addMessage("Compte supprimé : " + user.getEmail());
        }
        loadUsersIfAdmin();
    }


    public Internaute getInternaute() {
        return currentUser;
    }


    // voir les commandes d'un client

    public String voirCommandesClient(Internaute client) {
        // Stocker le client sélectionné pour le CommandeBean
        this.selectedClient = client;
        // Charger les commandes via CommandeBean
        commandeBean.loadCommandesForClient(client.getId());
        return "/commande/liste_client.xhtml?faces-redirect=true";
    }


    private void addMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }
}