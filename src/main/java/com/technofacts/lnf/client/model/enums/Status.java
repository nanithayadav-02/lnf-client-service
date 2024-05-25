package com.technofacts.lnf.client.model.enums;

public enum Status {

    //@formatter:off
    Approved("Approved"),
    In_Review("In Review"),
    Draft("Draft"),
    Cancelled("Cancelled");
    //@formatter:on

    private final String label;

    private Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Status valueOfLabel(String label) {
        for (Status at : values()) {
            if (at.label.equals(label)) {
                return at;
            }
        }
        return null;
    }

}
