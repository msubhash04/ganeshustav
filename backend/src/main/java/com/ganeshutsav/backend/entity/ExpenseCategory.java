package com.ganeshutsav.backend.entity;

public enum ExpenseCategory {
    IDOL_MURTI("Idol (Murti)"),
    PANDAL_DECORATION("Pandal Decoration"),
    ELECTRICITY_LIGHTING("Electricity/Lighting"),
    SOUND_SYSTEM("Sound System"),
    PRIEST_POOJA_MATERIALS("Priest/Pooja Materials"),
    FOOD_PRASAD("Food & Prasad"),
    IMMERSION_VISARJAN("Immersion (Visarjan)"),
    SECURITY("Security"),
    CULTURAL_PROGRAMS("Cultural Programs"),
    MISCELLANEOUS("Miscellaneous");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
