package uk.gov.hmcts.reform.rolecategorymigration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ApplicationParams {

    @Value("${idam.username}")
    private String idamUsername;

    @Value("${idam.password}")
    private String idamPassword;

}