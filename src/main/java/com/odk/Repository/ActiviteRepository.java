package com.odk.Repository;

import com.odk.Entity.Activite;
import com.odk.Enum.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ActiviteRepository extends JpaRepository<Activite, Long> {
   Optional<Activite> findByNom(String nom);
   Optional<Activite> findByNomIgnoreCase(String nom);
   long count();
   @Query("SELECT COUNT(DISTINCT e.activite) FROM Etape e WHERE e.statut = :statut")
   long countActivitesByStatut(@Param("statut") Statut statut);

   long countByStatut(Statut statut);

   @Query("SELECT COUNT(a) FROM Activite a WHERE a.statut = :statut")
   long countByStatutCustom(@Param("statut") Statut statut);

  /* @Query("SELECT a FROM Activite a WHERE a.salleId.id = :salleId AND " +
           "((:dateDebut BETWEEN a.dateDebut AND a.dateFin) OR " +
           "(:dateFin BETWEEN a.dateDebut AND a.dateFin) OR " +
           "(a.dateDebut BETWEEN :dateDebut AND :dateFin)) AND " +
           "a.statut != com.odk.Enum.Statut.Termine")
   List<Activite> findConflictingActivites(Long salleId, Date dateDebut, Date dateFin);*/

   @Query("SELECT a FROM Activite a " +
           "WHERE a.salleId.id = :salleId " +
           "AND ((:dateDebut < a.dateFin AND :dateFin > a.dateDebut)) " +
           "AND a.statut <> :statutTermine")
   List<Activite> findConflictingActivites(
           @Param("salleId") Long salleId,
           @Param("dateDebut") Date dateDebut,
           @Param("dateFin") Date dateFin,
           @Param("statutTermine") com.odk.Enum.Statut statutTermine
   );

 @Query(
        value = """
            SELECT DISTINCT a.*
            FROM activite a
            JOIN activite_validation av ON av.activite_id = a.id
            WHERE av.utilisateur_id = :superviseurId
            """,
        nativeQuery = true
    )
    List<Activite> findAllBySuperviseurInValidation(@Param("superviseurId") Long superviseurId);
    @Query(
        value = """
            SELECT DISTINCT a.*
            FROM activite a
            JOIN activite_validation av ON av.activite_id = a.id
            WHERE av.utilisateur_id = :superviseurId and av.statut=1
            """,
        nativeQuery = true
    )
    List<Activite> findAttenteBySuperviseurInValidation(@Param("superviseurId") Long superviseurId);
    
}
