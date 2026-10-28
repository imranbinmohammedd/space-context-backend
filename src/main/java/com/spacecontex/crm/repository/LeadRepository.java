package com.spacecontext.crm.repository;

import com.spacecontext.crm.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByLeadId(String leadId);

    List<Lead> findByCurrentStage(String currentStage);

    List<Lead> findByStatus(String status);

    List<Lead> findBySource(String source);

    // Search by name or phone
    @Query("SELECT l FROM Lead l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR l.phone LIKE CONCAT('%', :keyword, '%')")
    List<Lead> searchByNameOrPhone(@Param("keyword") String keyword);

    // Filter combination
    @Query("SELECT l FROM Lead l WHERE " +
           "(:stage IS NULL OR l.currentStage = :stage) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:source IS NULL OR l.source = :source)")
    List<Lead> filterLeads(@Param("stage") String stage,
                           @Param("status") String status,
                           @Param("source") String source);
}