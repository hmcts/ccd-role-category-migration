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

    @Value("${migration.page.number}")
    private int pageNumber;

    @Value("${migration.page.size}")
    private int pageSize;

    @Value("${idam.concurrent.threads}")
    private int concurrentThreads;

}