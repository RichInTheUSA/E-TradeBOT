package com.ciscospark;

import java.io.Serializable;

/**
 * Copyright (c) 2015 Cisco Systems, Inc. See LICENSE file.
 */
public class Person implements Serializable {
    private String id;
    private String displayName;
    private String[] emails;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String[] getEmails() {
        return emails;
    }

    public void setEmails(String[] emails) {
        this.emails = emails;
    }
}
