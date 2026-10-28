package com.spacecontext.crm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long leadId;                 // Which lead this activity belongs to

    private String type;                 // CALL, NOTE, STAGE_CHANGE, EMAIL, MEETING, FILE_UPLOAD
    private String title;                // Short title (e.g. "Initial Call", "Proposal Sent")
    private String description;          // Detailed notes

    private String performedBy;          // Who did this action (staff name)
    private String oldStage;             // Previous stage (for stage changes)
    private String newStage;             // New stage

    private String recordingFileName;    // If call recording is uploaded

    private LocalDateTime createdAt;

    public Activity() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getOldStage() { return oldStage; }
    public void setOldStage(String oldStage) { this.oldStage = oldStage; }

    public String getNewStage() { return newStage; }
    public void setNewStage(String newStage) { this.newStage = newStage; }

    public String getRecordingFileName() { return recordingFileName; }
    public void setRecordingFileName(String recordingFileName) { this.recordingFileName = recordingFileName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}