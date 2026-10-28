package com.spacecontext.crm.controller;

import com.spacecontext.crm.model.Project;
import com.spacecontext.crm.repository.ProjectRepository;
import com.spacecontext.crm.model.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    // Get all projects
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectRepository.findAll());
    }

    // Get single project
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update project stage
    @PutMapping("/{id}/stage")
    public ResponseEntity<Project> updateProjectStage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        String newStage = body.get("newStage");
        if (!Stage.isValid(newStage)) {
            throw new RuntimeException("Invalid stage: " + newStage);
        }

        project.setCurrentStage(newStage);
        project.setUpdatedAt(LocalDateTime.now());

        if ("Ready for Construction".equals(newStage)) {
            project.setStatus("Ready for Construction");
        }

        return ResponseEntity.ok(projectRepository.save(project));
    }
}