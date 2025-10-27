package ma.fstt.atelier3.beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.fstt.atelier3.model.Produit;
import org.primefaces.model.file.UploadedFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Named
@ViewScoped
@Getter
@Setter
@NoArgsConstructor
public class ProduitBean implements Serializable {

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    private List<Produit> allproduits;
    private Produit selectedproduit;
    private Produit newProduit;
    private UploadedFile uploadedFile;

    @PostConstruct
    public void init() {
        loadProduits();
        initializeNewProduit();

        String idParam = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap().get("id");

        if (idParam != null) {
            loadSelectedProduit(idParam);
        }
    }

    private void initializeNewProduit() {
        if (newProduit == null) {
            newProduit = new Produit();
        }
    }

    private void loadSelectedProduit(String idParam) {
        try {
            Long id = Long.parseLong(idParam);
            selectedproduit = em.find(Produit.class, id);
        } catch (NumberFormatException e) {
            selectedproduit = null;
        }
    }

    @Transactional
    public void loadProduits() {
        allproduits = em.createQuery("SELECT p FROM Produit p ORDER BY p.id", Produit.class)
                .getResultList();
    }

    @Transactional
    public void ajouterProduit() {
        try {
            // Gérer l'upload de l'image
            if (uploadedFile != null && uploadedFile.getContent().length > 0) {
                String fileName = saveUploadedFile(uploadedFile);
                newProduit.setImage(fileName);
            } else {
                // Image par défaut si aucune n'est uploadée
                newProduit.setImage("default-product.png");
            }

            em.persist(newProduit);
            resetForm();
            addSuccessMessage("Produit ajouté avec succès");

        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Erreur lors de l'ajout du produit: " + e.getMessage());
        }
    }

    @Transactional
    public String modifierProduit() {
        try {
            // Gérer l'upload de la nouvelle image si elle existe
            if (uploadedFile != null && uploadedFile.getContent().length > 0) {
                String fileName = saveUploadedFile(uploadedFile);
                selectedproduit.setImage(fileName);
            }
            // Si aucune nouvelle image, l'image actuelle est conservée automatiquement

            em.merge(selectedproduit);
            loadProduits();
            uploadedFile = null;

            addSuccessMessage("Produit modifié avec succès");
            return "List.xhtml?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Erreur lors de la modification du produit: " + e.getMessage());
            return null;
        }
    }

    @Transactional
    public void supprimerProduit(Produit produit) {
        try {
            Produit managedProduit = em.find(Produit.class, produit.getId());
            if (managedProduit != null) {
                em.remove(managedProduit);
                loadProduits();
                addSuccessMessage("Produit supprimé avec succès");
            }
        } catch (Exception e) {
            addErrorMessage("Erreur lors de la suppression du produit");
        }
    }

    private void handleImageUpload() {
        if (uploadedFile != null && uploadedFile.getSize() > 0) {
            String fileName = saveUploadedFile(uploadedFile);
            if (selectedproduit != null) {
                selectedproduit.setImage(fileName);
            } else {
                newProduit.setImage(fileName);
            }
        } else if (newProduit != null && newProduit.getImage() == null) {
            newProduit.setImage("default-product.png");
        }
    }

    private void resetForm() {
        newProduit = new Produit();
        uploadedFile = null;
        loadProduits();
    }


    // Méthode pour sauvegarder le fichier uploadé
    private String saveUploadedFile(UploadedFile file) {
        try {
            String appPath = FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getRealPath("");
            String uploadPath = appPath + File.separator + "images" + File.separator;

            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getFileName();
            File outputFile = new File(uploadPath + fileName);

            Files.copy(
                    file.getInputStream(),
                    outputFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier", e);
        }
    }

    public String getImageUrl(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return "/images/default-product.png";
        }
        return "/images/" + imageName;
    }

    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new jakarta.faces.application.FacesMessage(
                        jakarta.faces.application.FacesMessage.SEVERITY_INFO,
                        "Succès", message));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new jakarta.faces.application.FacesMessage(
                        jakarta.faces.application.FacesMessage.SEVERITY_ERROR,
                        "Erreur", message));
    }
}