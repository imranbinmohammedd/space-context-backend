package com.spacecontext.crm.service;

import com.spacecontext.crm.model.Activity;
import com.spacecontext.crm.model.Lead;
import com.spacecontext.crm.repository.ActivityRepository;
import com.spacecontext.crm.repository.LeadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.spacecontext.crm.model.Stage;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.spacecontext.crm.repository.ProjectRepository;
import com.spacecontext.crm.model.Project;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ActivityRepository activityRepository;

    // Create new lead
    public Lead createLead(Lead lead) {
        // Generate simple Lead ID (SC-0001, SC-0002...)
        long count = leadRepository.count() + 1;
        String leadId = String.format("SC-%04d", count);
        lead.setLeadId(leadId);
        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());
        lead.setCurrentStage("New Lead");
        lead.setStatus("Open");

        Lead savedLead = leadRepository.save(lead);

        // Log activity
        Activity activity = new Activity();
        activity.setLeadId(savedLead.getId());
        activity.setType("STAGE_CHANGE");
        activity.setTitle("Lead Created");
        activity.setDescription("New lead created from source: " + lead.getSource());
        activity.setPerformedBy("System");
        activity.setNewStage("New Lead");
        activityRepository.save(activity);

        return savedLead;
    }

    // Get all leads
    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    // Get lead by ID
    public Optional<Lead> getLeadById(Long id) {
        return leadRepository.findById(id);
    }

    // Get lead by LeadId (SC-0001)
    public Optional<Lead> getLeadByLeadId(String leadId) {
        return leadRepository.findByLeadId(leadId);
    }

    // Update stage
    public Lead updateStage(Long leadId, String newStage, String performedBy, String notes) {
    if (!Stage.isValid(newStage)) {
        throw new RuntimeException("Invalid stage: " + newStage + ". Please use one of the defined stages.");
    }

    Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found"));

    String oldStage = lead.getCurrentStage();
    lead.setCurrentStage(newStage);
    lead.setUpdatedAt(LocalDateTime.now());

    // Optional: Auto change status when contract is signed
    if ("Contract Signed".equals(newStage) || "Advance Received".equals(newStage)) {
        lead.setStatus("Won");
    }

    if ("Ready for Construction".equals(newStage)) {
        lead.setStatus("Won");
    }

    leadRepository.save(lead);

    // Log the stage change
    Activity activity = new Activity();
    activity.setLeadId(leadId);
    activity.setType("STAGE_CHANGE");
    activity.setTitle("Stage Changed: " + oldStage + " → " + newStage);
    activity.setDescription(notes != null ? notes : "Stage updated");
    activity.setPerformedBy(performedBy);
    activity.setOldStage(oldStage);
    activity.setNewStage(newStage);
    activityRepository.save(activity);

    return lead;
}

    // Add Call / Note activity
    public Activity addActivity(Long leadId, String type, String title, String description, String performedBy) {
        Activity activity = new Activity();
        activity.setLeadId(leadId);
        activity.setType(type);          // CALL, NOTE, MEETING, EMAIL
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setPerformedBy(performedBy);
        return activityRepository.save(activity);
    }

    // Get all activities of a lead
    public List<Activity> getActivitiesByLeadId(Long leadId) {
        return activityRepository.findByLeadIdOrderByCreatedAtDesc(leadId);
    }

    public Activity addActivityWithFile(Long leadId, Activity activity) {
    // Just to make sure lead exists
    leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found"));

    return activityRepository.save(activity);
}

public List<Lead> searchLeads(String keyword) {
    return leadRepository.searchByNameOrPhone(keyword);
}

public List<Lead> filterLeads(String stage, String status, String source) {
    return leadRepository.filterLeads(stage, status, source);
}

public Map<String, Object> getDashboardStats() {
    List<Lead> allLeads = leadRepository.findAll();

    long totalLeads = allLeads.size();
    long wonLeads = allLeads.stream().filter(l -> "Won".equals(l.getStatus())).count();
    long openLeads = allLeads.stream().filter(l -> "Open".equals(l.getStatus())).count();

    double conversionRate = totalLeads == 0 ? 0 : (wonLeads * 100.0) / totalLeads;

    // Count by stage
    Map<String, Long> stageCount = allLeads.stream()
            .collect(Collectors.groupingBy(Lead::getCurrentStage, Collectors.counting()));

    // Count by source
    Map<String, Long> sourceCount = allLeads.stream()
            .collect(Collectors.groupingBy(Lead::getSource, Collectors.counting()));

    Map<String, Object> stats = new HashMap<>();
    stats.put("totalLeads", totalLeads);
    stats.put("wonLeads", wonLeads);
    stats.put("openLeads", openLeads);
    stats.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);
    stats.put("stageCount", stageCount);
    stats.put("sourceCount", sourceCount);

    return stats;
}

public Project convertLeadToProject(Long leadId, String projectManager, String architect) {
    Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found"));

    // Check if already converted
    if (projectRepository.findByLeadId(leadId).isPresent()) {
        throw new RuntimeException("This lead is already converted to a project");
    }

    Project project = new Project();
    long count = projectRepository.count() + 1;
    project.setProjectId(String.format("PRJ-%04d", count));
    project.setLeadId(lead.getId());
    project.setClientName(lead.getName());
    project.setPhone(lead.getPhone());
    project.setEmail(lead.getEmail());
    project.setProjectType(lead.getProjectType());
    project.setLocation(lead.getLocation());
    project.setBudgetRange(lead.getBudgetRange());
    project.setCurrentStage(lead.getCurrentStage());
    project.setAssignedProjectManager(projectManager);
    project.setAssignedArchitect(architect);
    project.setStartDate(LocalDateTime.now());

    Project savedProject = projectRepository.save(project);

    // Update lead status
    lead.setStatus("Won");
    lead.setUpdatedAt(LocalDateTime.now());
    leadRepository.save(lead);

    // Log activity
    Activity activity = new Activity();
    activity.setLeadId(leadId);
    activity.setType("PROJECT_CREATED");
    activity.setTitle("Lead converted to Project");
    activity.setDescription("Project ID: " + savedProject.getProjectId());
    activity.setPerformedBy(projectManager);
    activityRepository.save(activity);

    return savedProject;
}
}