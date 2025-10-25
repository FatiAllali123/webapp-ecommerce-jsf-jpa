package ma.fstt.atelier3.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Commande")
@Getter
@Setter
@NoArgsConstructor

public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="date")
    private Date date;

    @Column(name="montant")
    private BigDecimal montant;

    public Commande(Long idCommande, Date date  , Internaute internaute , BigDecimal montant ) {
        this.id = idCommande;
        this.date = date;
        this.internaute = internaute;
        this.montant = montant;


    }

    // Internaute <-> Commande
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_internaute")
    private Internaute internaute ;


    // Ligne Commande <-> Ligne Commande
    @OneToMany(mappedBy="commande", cascade = CascadeType.ALL, orphanRemoval = true) // garantie supression en cascade
    private List<LigneCommande> lignes;



}