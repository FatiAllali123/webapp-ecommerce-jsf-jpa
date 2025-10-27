package ma.fstt.atelier3.beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.fstt.atelier3.model.Internaute;

import java.io.Serializable;

@Named
@RequestScoped
@Getter
@Setter
@NoArgsConstructor
public class AuthBean implements Serializable {

    private String email, password, nom, prenom, adresse;

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    @Inject
    private IternauteBean internauteBean;

    @Inject
    private PanierBean panierBean; // Pour créer le panier

    @Transactional
    public String login() {
        try {
            Internaute user = em.createQuery(
                            "SELECT i FROM Internaute i WHERE i.email = :email AND i.password = :pass", Internaute.class)
                    .setParameter("email", email)
                    .setParameter("pass", password)
                    .getSingleResult();

            if ("desactive".equals(user.getStatut_compte())) {
                addError("Votre compte est désactivé.");
                return null;
            }

            internauteBean.login(user);
            panierBean.chargerOuCreerPanier(); // Recharge le panier après login
            return "/vitrine.xhtml?faces-redirect=true";

        } catch (NoResultException e) {
            addError("Email ou mot de passe incorrect.");
            return null;
        }
    }

    @Transactional
    public String signup() {
        if (emailExists(email)) {
            addError("Cet email est déjà utilisé.");
            return null;
        }

        Internaute user = new Internaute(nom, prenom, email, password, adresse, "user", "active");
        em.persist(user);

        return "/login.xhtml?faces-redirect=true";
    }


    private boolean emailExists(String email) {
        return !em.createQuery("SELECT 1 FROM Internaute i WHERE i.email = :email", Integer.class)
                .setParameter("email", email)
                .getResultList().isEmpty();
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    private void addMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }
}