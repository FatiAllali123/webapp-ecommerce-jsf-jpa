package ma.fstt.atelier3.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name="produit")
@Setter
@Getter
@NoArgsConstructor

public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @Column(name="designation" , length = 250, nullable = false)
    private String designation ;

    @Column(name="prixUnitaire" , nullable = false)
    private float prixUnitaire ;

    @Column(name="stock" , nullable = false)
    private float stock ;


    @Column(name="description" ,length = 500, nullable = false)
    private String description ;


    @Column(name="image" , nullable = false , length = 250)
    private String image ;

    public Produit(Long idProduit, String designation, float prixUnitaire, float stock , String image) {
        this.id = idProduit;
        this.designation = designation;
        this.prixUnitaire = prixUnitaire;
        this.stock = stock;
        this.image = image;

    }

    // produit <-> ligneCommande
    @OneToMany(mappedBy="produit" )
    private List<LigneCommande> lignesCommande ;

    // produit <-> lignePanier
    @OneToMany(mappedBy="produit")
    private List<LignePanier> lignesPanier ;
    // categori <-> Produit
    @ManyToOne
    @JoinColumn(name = "id_categorie")
    private Categorie categorie;

}