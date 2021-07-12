package uk.gov.hmcts.reform.rolecategorymigration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Component
@Slf4j
public class IdamRepository {

    private final IdamClient idamClient;
    private final ApplicationParams applicationParams;
    private final String username;
    private final String password;

    @Autowired
    public IdamRepository(IdamClient idamClient, ApplicationParams applicationParams) {
        this.idamClient = idamClient;
        this.applicationParams = applicationParams;
        username = applicationParams.getIdamUsername();
        password = applicationParams.getIdamPassword();
    }

    public UserDetails getUserRoles(String userId) {
        String token = idamClient.getAccessToken(username, password);
        return idamClient.getUserByUserId(token, userId);
    }
}
