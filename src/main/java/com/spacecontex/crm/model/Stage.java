package com.spacecontext.crm.model;

import java.util.List;

public class Stage {

    public static final List<String> ALL_STAGES = List.of(
            "New Lead",
            "Initial Contact",
            "Qualification",
            "Discovery Meeting",
            "Proposal Sent",
            "Negotiation",
            "Contract Signed",
            "Advance Received",
            "Project Kick-off",
            "Concept Design",
            "Schematic Design",
            "Design Development",
            "Documentation",
            "Ready for Construction"
    );

    public static boolean isValid(String stage) {
        return ALL_STAGES.contains(stage);
    }
}