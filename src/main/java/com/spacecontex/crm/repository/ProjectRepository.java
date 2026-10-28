package com.spacecontext.crm.repository;

import com.spacecontext.crm.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByLeadId(Long leadId);
    Optional<Project> findByProjectId(String projectId);
}