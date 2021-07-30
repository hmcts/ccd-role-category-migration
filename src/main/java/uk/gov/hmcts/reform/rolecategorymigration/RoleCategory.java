package uk.gov.hmcts.reform.rolecategorymigration;

import java.util.regex.Pattern;

public enum RoleCategory {
    CITIZEN("CITIZEN", Pattern.compile("^citizen(-.*)?$|^letter-holder$")),
    JUDICIAL("JUDICIAL", Pattern.compile(".+-panelmember$")),
    PROFESSIONAL("PROFESSIONAL", Pattern.compile(".+-solicitor$|^caseworker-.+-localAuthority$"));

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
}
