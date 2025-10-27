package ma.fstt.atelier3.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.atelier3.model.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@Named
@SessionScoped
@Getter
@Setter
public class PanierBean implements Serializable {

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    @Inject
    private IternauteBean internauteBean;

    private Panier panierCourant;
    private Integer quantite = 1;

    @PostConstruct
    public void init() {

    }

    @Transactional
    public void chargerOuCreerPanier() {
        System.out.println("=== chargerOuCreerPanier() appelé ===");
        // Afficher le contexte JSF
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            String viewId = context.getViewRoot().getViewId();
            System.out.println("Vue actuelle: " + viewId);

            // Vérifier si c'est un postback
            System.out.println("Est un postback? " + context.isPostback());
        }
        if (!internauteBean.isLoggedIn()) {
            System.out.println("Utilisateur non connecté");
            panierCourant = null;
            return;
        }

        if (!"user".equals(internauteBean.getInternaute().getRole())) {
            System.out.println("Utilisateur n'est pas un 'user', rôle: " + internauteBean.getInternaute().getRole());
            panierCourant = null;
            return;
        }

        // Ne recharger que si nécessaire
        if (panierCourant != null) {
            System.out.println("Panier déjà chargé, ID: " + panierCourant.getId());
            return;
        }

        Internaute internaute = internauteBean.getInternaute();
        System.out.println("Chargement du panier pour: " + internaute.getEmail());

        panierCourant = em.createQuery(
                        "SELECT p FROM Panier p " +
                                "LEFT JOIN FETCH p.lignes " +
                                "WHERE p.internaute = :internaute " +
                                "ORDER BY p.dateCreation DESC", Panier.class)
                .setParameter("internaute", internaute)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElseGet(() -> creerNouveauPanier(internaute));

        System.out.println("Panier chargé, ID: " + (panierCourant != null ? panierCourant.getId() : "null"));
        if (panierCourant != null) {
            quantite = 1;
        }
    }

    @Transactional
    public Panier creerNouveauPanier(Internaute internaute) {
        Panier nouveau = new Panier();
        nouveau.setInternaute(internaute);
        nouveau.setDateCreation(LocalDateTime.now());
        nouveau.setTotal(BigDecimal.ZERO);
        nouveau.setLignes(new ArrayList<>());
        em.persist(nouveau);
        this.panierCourant = nouveau;
        return nouveau;
    }

    @Transactional
    public void ajouterAuPanier(Produit produit) {


        FacesContext context2 = FacesContext.getCurrentInstance();
        if (context2 != null) {
            PhaseId phase = context2.getCurrentPhaseId();
            boolean isPostback = context2.isPostback();

            // Bloquer si ce n'est pas un vrai submit de formulaire
            if (phase == PhaseId.RENDER_RESPONSE || !isPostback) {
                System.out.println("⚠️ ajouterAuPanier() IGNORÉ - Appel pendant le rendu");
                return;
            }
        }



        System.out.println("\n========================================");
        System.out.println("=== ajouterAuPanier() APPELÉ ===");
        System.out.println("========================================");

        // Contexte JSF
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            String viewId = context.getViewRoot().getViewId();
            boolean isPostback = context.isPostback();

            System.out.println("Vue actuelle: " + viewId);
            System.out.println("Est un postback? " + isPostback);
            System.out.println("Phase actuelle: " + context.getCurrentPhaseId());

            // Vérifier les paramètres de la requête
            Map<String, String> params = context.getExternalContext().getRequestParameterMap();
            System.out.println("Paramètres de requête: " + params);
        }

        // Stack trace complète
        System.out.println("\n=== STACK TRACE COMPLÈTE ===");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 1; i < Math.min(15, stackTrace.length); i++) {
            StackTraceElement element = stackTrace[i];
            System.out.println("  " + i + ": " + element.getClassName() + "." + element.getMethodName() +
                    " (" + element.getFileName() + ":" + element.getLineNumber() + ")");
        }
        System.out.println("========================================\n");

        System.out.println("Produit: " + (produit != null ? produit.getDesignation() : "null"));
        System.out.println("Panier courant: " + (panierCourant != null ? panierCourant.getId() : "null"));
        System.out.println("Quantité: " + quantite);

        // Vérifier si le panier existe, sinon le créer
        if (panierCourant == null) {
            System.out.println("Panier null, tentative de chargement...");
            chargerOuCreerPanier();
        }

        if (panierCourant == null) {
            addMessage("Erreur", "Impossible de créer le panier. Veuillez vous reconnecter.");
            return;
        }

        if (produit == null) {
            addMessage("Erreur", "Produit manquant.");
            return;
        }

        // Rafraîchir le panier pour s'assurer qu'il est attaché
        panierCourant = em.merge(panierCourant);

        // Vérifier le stock
        if (produit.getStock() < quantite) {
            addMessage("Stock insuffisant", "Seulement " + produit.getStock() + " disponible(s).");
            return;
        }

        // Vérifier si le produit existe déjà dans le panier
        LignePanier ligne = trouverLigneParProduit(produit);
        if (ligne != null) {
            int nouvelleQuantite = ligne.getQuantite() + quantite;
            if (nouvelleQuantite > produit.getStock()) {
                addMessage("Stock dépassé", "Maximum disponible : " + produit.getStock());
                return;
            }

            // Rafraîchir la ligne avant de la mettre à jour
            ligne = em.find(LignePanier.class, ligne.getId());
            if (ligne == null) {
                addMessage("Erreur", "Ligne de panier introuvable.");
                return;
            }

            ligne.setQuantite(nouvelleQuantite);
            ligne.setPrixTotal(BigDecimal.valueOf(produit.getPrixUnitaire() * nouvelleQuantite));
            System.out.println("Ligne mise à jour, nouvelle quantité: " + nouvelleQuantite);
        } else {
            // Créer une nouvelle ligne
            LignePanier nouvelleLigne = new LignePanier();
            nouvelleLigne.setProduit(produit);
            nouvelleLigne.setPanier(panierCourant);
            nouvelleLigne.setQuantite(quantite);
            nouvelleLigne.setPrixTotal(BigDecimal.valueOf(produit.getPrixUnitaire() * quantite));

            panierCourant.getLignes().add(nouvelleLigne);
            em.persist(nouvelleLigne);
            System.out.println("Nouvelle ligne créée");
        }

        recalculerTotal();
        addMessage("Ajouté", produit.getDesignation() + " ajouté au panier !");
        quantite = 1; // Réinitialiser
    }

    private LignePanier trouverLigneParProduit(Produit produit) {
        if (panierCourant.getLignes() == null) return null;
        return panierCourant.getLignes().stream()
                .filter(l -> produit.getId().equals(l.getProduit().getId()))
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void mettreAJourQuantite(LignePanier ligne, Integer nouvelleQuantite) {
        if (nouvelleQuantite <= 0) {
            supprimerLigne(ligne);
            return;
        }
        if (nouvelleQuantite > ligne.getProduit().getStock()) {
            addMessage("Stock", "Max : " + ligne.getProduit().getStock());
            return;
        }
        ligne.setQuantite(nouvelleQuantite);
        ligne.setPrixTotal(BigDecimal.valueOf(ligne.getProduit().getPrixUnitaire() * nouvelleQuantite));
        recalculerTotal();
    }

    @Transactional
    public void supprimerLigne(LignePanier ligne) {
        if (panierCourant == null || ligne == null) return;

        // Rafraîchir le panier pour s'assurer qu'il est attaché
        panierCourant = em.merge(panierCourant);
        panierCourant.getLignes().remove(ligne);
        em.flush();

        recalculerTotal();
        addMessage("Supprimé", "Article retiré du panier.");
    }

    @Transactional
    public void recalculerTotal() {
        if (panierCourant == null) return;

        BigDecimal total = panierCourant.getLignes().stream()
                .map(LignePanier::getPrixTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        panierCourant.setTotal(total);
        em.merge(panierCourant);
    }
    @Transactional
    public void viderPanier() {
        if (panierCourant == null || panierCourant.getLignes() == null) return;
        // Rafraîchir le panier
        panierCourant = em.merge(panierCourant);
        // Vider la collection - orphanRemoval supprimera les lignes
        panierCourant.getLignes().clear();
        panierCourant.setTotal(BigDecimal.ZERO);
        // Forcer la synchronisation
        em.flush();

        addMessage("Panier vidé", "Tous les articles ont été retirés.");
    }

    public int getNombreArticles() {
        return panierCourant != null && panierCourant.getLignes() != null
                ? panierCourant.getLignes().stream().mapToInt(LignePanier::getQuantite).sum()
                : 0;
    }

    public boolean isPanierAccessible() {
        return internauteBean.isLoggedIn() && "user".equals(internauteBean.getInternaute().getRole());
    }

    @Transactional
    public String validerCommande() {
        if (panierCourant == null || panierCourant.getLignes().isEmpty()) {
            addMessage("Erreur", "Panier vide.");
            return null;
        }

        // Créer commande + lignes
        Commande commande = new Commande();
        commande.setInternaute(internauteBean.getInternaute());
        commande.setDate(new Date());
        commande.setMontant(panierCourant.getTotal());

        em.persist(commande);
        panierCourant = em.merge(panierCourant);
        for (LignePanier lp : panierCourant.getLignes()) {
            LigneCommande lc = new LigneCommande();
            lc.setCommande(commande);
            lc.setProduit(lp.getProduit());
            lc.setQuantite(lp.getQuantite());
            lc.setPrixTotal(lp.getPrixTotal());
            em.persist(lc);

            // Réduire stock
            Produit p = lp.getProduit();
            p.setStock(p.getStock() - lp.getQuantite());
            em.merge(p);
        }

        // Vider panier
        viderPanier();

        addMessage("Succès", "Commande passée !");
        return "/commande-confirmation.xhtml?faces-redirect=true";
    }

    public String allerAuPanier() {
        chargerOuCreerPanier();
        return "/Panier/panier.xhtml?faces-redirect=true";
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }
}