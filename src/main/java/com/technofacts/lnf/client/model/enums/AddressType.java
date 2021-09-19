package com.technofacts.lnf.client.model.enums;

public enum AddressType {

    //@formatter:off
    Communication("Communication"),
    Permanent("Permanent"),
    Work("Work");
    //@formatter:on

    private final String label;

    private AddressType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AddressType valueOfLabel(String label) {
        for (AddressType at : values()) {
            if (at.label.equals(label)) {
                return at;
            }
        }
        return null;
    }
}
