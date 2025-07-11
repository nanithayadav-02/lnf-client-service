/*
 *
 *  * Copyright © 2024 Lever And Fulcrum Solutions (hereinafter referred to as "LNF").
 *  * All rights reserved.
 *  *
 *  * This source code is the proprietary property of LNF
 *  *
 *  * Unauthorized copying, redistribution, or modification of this code,
 *  * via any medium, is strictly prohibited unless expressly authorized
 *  * in writing by LNF.
 *  *
 *  * This code is confidential and intended solely for the use of LNF
 *  * and its authorized personnel.
 *
 */

package com.lnf.client.model.enums;

public enum Status {

    //@formatter:off
    APPROVED("Approved"),
    IN_REVIEW("In Review"),
    DRAFT("Draft"),
    CANCELLED("Cancelled");
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
