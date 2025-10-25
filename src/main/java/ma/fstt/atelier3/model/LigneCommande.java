package ma.fstt.atelier3.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name="LigneCommande")
@Getter
@Setter
@NoArgsConstructor

public class LigneCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantite ;

    @Column( precision = 10, scale = 2 , nullable = false)
    private BigDecimal prixTotal;



    public LigneCommande(Long idLigne , Integer quantite, BigDecimal prixTotal , Commande commande , Produit produit) {
        this.id = idLigne;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.commande = commande;
        this.produit = produit;

    }

    // Commande <-> Ligne Commande
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_commande")
    private Commande commande;

    // Ligne Commande <-> Produit
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_produit")
    private Produit produit;




}