package ma.fstt.atelier3.model;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Panier")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Panier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dateCreation" , nullable = false)
    private LocalDateTime dateCreation;

    @Column(name="Total", precision = 10, scale = 2 , nullable = false)
    private BigDecimal Total;


    public Panier( Long id , LocalDateTime dateCreation, BigDecimal Total , Internaute internaute) {
        this.id = id;
        this.dateCreation = dateCreation;
        this.Total = Total;
        this.internaute = internaute;
    }


    // Internaute <-> Panier
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_internaute")
    private Internaute internaute ;


    // Ligne panier <-> Panier
    @OneToMany(mappedBy="panier" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LignePanier> lignes;

}