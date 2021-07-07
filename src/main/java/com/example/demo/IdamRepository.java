package com.example.demo;

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

    @Autowired
    public IdamRepository(IdamClient idamClient, ApplicationParams applicationParams) {
        this.idamClient = idamClient;
        this.applicationParams = applicationParams;
    }

    public UserDetails getUserRoles(String userId) {
        String token = idamClient.getAccessToken(applicationParams.getIdamUsername(), applicationParams.getIdamPassword());
        return idamClient.getUserByUserId("Bearer " + token, userId);
    }
}
