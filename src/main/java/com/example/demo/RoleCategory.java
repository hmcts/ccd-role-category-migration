package com.example.demo;

import java.util.regex.Pattern;

public enum RoleCategory {
    CITIZEN("CITIZEN", Pattern.compile("^citizen(-.*)?$|^letter-holder$")),
    JUDICIAL("JUDICIAL", Pattern.compile(".+-panelmember$")),
    PROFESSIONAL("PROFESSIONAL", Pattern.compile("-localAuthority$"));

    private final String name;
    private final Pattern pattern;

    RoleCategory(String name, Pattern pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getName() {
        return name;
    }

    public static RoleCategory getEnumFromPattern(Pattern pattern) {
        for (RoleCategory role : RoleCategory.values()) {
            if (role.getPattern().equals(pattern)) {
                return role;
            }
        }
        return null;
    }
}
