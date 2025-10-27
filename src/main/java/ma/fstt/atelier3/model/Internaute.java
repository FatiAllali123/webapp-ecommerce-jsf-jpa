package ma.fstt.atelier3.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Internaute")
@Getter
@Setter
@NoArgsConstructor

public class Internaute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;
    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 255)
    private String adresse;

    @Column(length = 10)
    private String role;

    @Column(name = "statut_compte", length = 15)
    private String statut_compte ;


    public Internaute(String nom,String prenom , String email, String motDePasse, String adresse , String role , String statut_compte) {
        this.nom = nom;
        this.email = email;
        this.password = motDePasse;
        this.adresse = adresse;
        this.prenom = prenom;
        this.role = role;
        this.statut_compte = statut_compte;

    }



    // Internaute <-> Commande
    @OneToMany(mappedBy = "internaute" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commande> commandes ;

    // Internaute <-> Panier
    @OneToMany(mappedBy = "internaute")
    private List<Panier> paniers;



}