package com.technofacts.lnf.client.model.enums;

public enum DocumentType {

    //@formatter:off
    image("image"),
    agreement("agreement"),
    others("others");
    //@formatter:on

    private final String label;

    private DocumentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static DocumentType valueOfLabel(String label) {
        for (DocumentType at : values()) {
            if (at.label.equals(label)) {
                return at;
            }
        }
        return null;
    }
}
