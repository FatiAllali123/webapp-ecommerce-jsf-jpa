package ma.fstt.atelier3.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "LignePanier")


public class LignePanier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantite;

    @Column( precision = 10, scale = 2 , nullable = false)
    private BigDecimal prixTotal;




    public LignePanier( Integer quantite , BigDecimal prixTotal  , Long idLigne , Panier panier ,Produit produit) {
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.id = id;
        this.panier = panier;
        this.produit = produit;


    }

    // Ligne paniere<-> panier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_panier")
    private Panier panier;


    // Ligne panier <-> Produit
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_produit")
    private Produit produit;

}