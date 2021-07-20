package uk.gov.hmcts.reform.rolecategorymigration;

public class MigrationException extends RuntimeException {

    public MigrationException(String message) {
        super(message);
    }
}
