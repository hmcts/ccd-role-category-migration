package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam"})
@SpringBootApplication
@Slf4j
public class DemoApplication implements CommandLineRunner {

	private final List<Pattern> roleCategoryPatterns = new ArrayList<>();

	@Autowired
	private ApplicationContext context;

	@Autowired
	private CaseDataRepository caseDataRepository;

	@Autowired
	private IdamRepository idamRepository;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("Data migration task has begun");
		long startTime = System.currentTimeMillis();

		log.info("Starting to update existing role categories");

		// TODO: Make first page & size configurable
		updateExistingRoleCategory(PageRequest.of(0, 5000));

		log.info("Starting to populate null role categories");

		populateNewRoleCategory(PageRequest.of(0, 5000));

		long endTime = System.currentTimeMillis();

		log.info("Completed data population task in " + (endTime - startTime) + " milliseconds");

		SpringApplication.exit(context, () -> 0);
	}

	private void updateExistingRoleCategory(Pageable pageable) {
		log.info("Processing page " + pageable.getPageNumber() + " with offset " + pageable.getOffset());

		Slice<CaseUsersEntity> dataWithRoleCategory = caseDataRepository.findCaseUsersWithRoleCategory(pageable);
		for (CaseUsersEntity entity : dataWithRoleCategory) {
			caseDataRepository.updateRoleCategory(entity.getRoleCategory(), entity.getUserId());
		}

		//commit here?

		if (dataWithRoleCategory.hasNext()) {
			updateExistingRoleCategory(dataWithRoleCategory.nextPageable());
		}
	}

	private void populateNewRoleCategory(Pageable pageable) {
		log.info("Processing page " + pageable.getPageNumber() + " with offset " + pageable.getOffset());

		Slice<String> dataWithoutRoleCategory = caseDataRepository.findCaseUsersById(pageable);

		for (String userId : dataWithoutRoleCategory) {
			List<String> idamRoles = retrieveIdamRoles(userId);

			RoleCategory matchingCategory = matchRoleCategory(idamRoles, userId);

			if (matchingCategory != null) {
				caseDataRepository.updateRoleCategory(matchingCategory.getName(), userId);
			} else {
				throw new MigrationException("No matching role category found for user_id: " + userId);
			}
		}

		//commit here?
		
		if (dataWithoutRoleCategory.hasNext()) {
			populateNewRoleCategory(dataWithoutRoleCategory.nextPageable());
		}
	}

	private List<String> retrieveIdamRoles(String userId) {
		List<String> idamRoles;

		try {
			idamRoles = idamRepository.getUserRoles(userId).getRoles();
		} catch (Exception ex) {
			throw new MigrationException("Error retrieving IdAM roles: " + ex.getMessage());
		}
		return idamRoles;
	}

	private RoleCategory matchRoleCategory(List<String> roles, String userId) {
		roleCategoryPatterns.add(RoleCategory.CITIZEN.getPattern());
		roleCategoryPatterns.add(RoleCategory.JUDICIAL.getPattern());
		roleCategoryPatterns.add(RoleCategory.PROFESSIONAL.getPattern());

		RoleCategory firstCategory = null;
		for (String role : roles) {
			for (Pattern pattern : roleCategoryPatterns) {
				if (pattern.matcher(role).matches()) {
					RoleCategory newCategory = RoleCategory.getEnumFromPattern(pattern);
					if (firstCategory == null) {
						firstCategory = newCategory;
					} else if (firstCategory != newCategory) {
						throw new MigrationException("Multiple role categories identified for user_id: " + userId);
					}
				}
			}
		}
		return firstCategory;
	}
}
