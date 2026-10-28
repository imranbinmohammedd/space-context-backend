package com.spacecontext.crm.controller;

import com.spacecontext.crm.model.Activity;
import com.spacecontext.crm.model.Lead;
import com.spacecontext.crm.service.LeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.spacecontext.crm.model.Stage;
import com.spacecontext.crm.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import com.spacecontext.crm.model.Project;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*")
public class LeadController {

    @Autowired
    private LeadService leadService;

    // Create new lead
    @PostMapping
    public ResponseEntity<Lead> createLead(@RequestBody Lead lead) {
        Lead created = leadService.createLead(lead);
        return ResponseEntity.ok(created);
    }

    // Get all leads
    @GetMapping
    public ResponseEntity<List<Lead>> getAllLeads() {
        return ResponseEntity.ok(leadService.getAllLeads());
    }

    // Get single lead by database ID
    @GetMapping("/{id}")
    public ResponseEntity<Lead> getLeadById(@PathVariable Long id) {
        return leadService.getLeadById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update stage of a lead
    @PutMapping("/{id}/stage")
    public ResponseEntity<Lead> updateStage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String newStage = body.get("newStage");
        String performedBy = body.get("performedBy");
        String notes = body.get("notes");

        Lead updated = leadService.updateStage(id, newStage, performedBy, notes);
        return ResponseEntity.ok(updated);
    }

    // Add Call / Note
    @PostMapping("/{id}/activities")
    public ResponseEntity<Activity> addActivity(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Activity activity = leadService.addActivity(
                id,
                body.get("type"),
                body.get("title"),
                body.get("description"),
                body.get("performedBy")
        );
        return ResponseEntity.ok(activity);
    }

    // Get all activities of a lead
    @GetMapping("/{id}/activities")
    public ResponseEntity<List<Activity>> getActivities(@PathVariable Long id) {
        return ResponseEntity.ok(leadService.getActivitiesByLeadId(id));
    }

    @GetMapping("/stages")
public ResponseEntity<List<String>> getAllStages() {
    return ResponseEntity.ok(Stage.ALL_STAGES);
}

@Autowired
private FileStorageService fileStorageService;

@PostMapping("/{id}/upload")
public ResponseEntity<Activity> uploadFile(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file,
        @RequestParam("type") String type,          // CALL_RECORDING or DOCUMENT
        @RequestParam("title") String title,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam("performedBy") String performedBy) {

    String storedFileName = fileStorageService.storeFile(file);

    Activity activity = new Activity();
    activity.setLeadId(id);
    activity.setType(type);
    activity.setTitle(title);
    activity.setDescription(description != null ? description : "File uploaded");
    activity.setPerformedBy(performedBy);
    activity.setRecordingFileName(storedFileName);

    // We need to save it using the service
    return ResponseEntity.ok(leadService.addActivityWithFile(id, activity));
}

// Search by name or phone
@GetMapping("/search")
public ResponseEntity<List<Lead>> searchLeads(@RequestParam String keyword) {
    return ResponseEntity.ok(leadService.searchLeads(keyword));
}

// Filter leads
@GetMapping("/filter")
public ResponseEntity<List<Lead>> filterLeads(
        @RequestParam(required = false) String stage,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String source) {
    return ResponseEntity.ok(leadService.filterLeads(stage, status, source));
}

@GetMapping("/dashboard")
public ResponseEntity<Map<String, Object>> getDashboard() {
    return ResponseEntity.ok(leadService.getDashboardStats());
}

@PostMapping("/{id}/convert-to-project")
public ResponseEntity<Project> convertToProject(
        @PathVariable Long id,
        @RequestBody Map<String, String> body) {

    String projectManager = body.get("projectManager");
    String architect = body.get("architect");

    Project project = leadService.convertLeadToProject(id, projectManager, architect);
    return ResponseEntity.ok(project);
}

@GetMapping("/files/{filename}")
public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    try {
        Path filePath = Paths.get("uploads").resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}
}