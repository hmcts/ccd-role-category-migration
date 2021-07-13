package uk.gov.hmcts.reform.rolecategorymigration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam"})
@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

	private final List<Pattern> roleCategoryPatterns = new ArrayList<>();
	private final String ExceptionLabel = "*EXCEPTION*";

	@Autowired
	private ApplicationContext context;

	@Autowired
	private CaseDataRepository caseDataRepository;

	@Autowired
	private IdamRepository idamRepository;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("Data migration task has begun");
		long startTime = System.currentTimeMillis();
		roleCategoryPatterns.add(RoleCategory.CITIZEN.getPattern());
		roleCategoryPatterns.add(RoleCategory.JUDICIAL.getPattern());
		roleCategoryPatterns.add(RoleCategory.PROFESSIONAL.getPattern());

		// TODO: Make first page & size configurable

		log.info("Starting to populate null role categories");

		populateNewRoleCategory();

		long endTime = System.currentTimeMillis();

		log.info("Completed data population task in " + (endTime - startTime) + " milliseconds");

		SpringApplication.exit(context, () -> 0);
	}

	private void populateNewRoleCategory() {

		Page<String> dataWithoutRoleCategory = caseDataRepository.findCaseUsersById(PageRequest.of(0, 5000));
		if (dataWithoutRoleCategory.isEmpty()) {
			return;
		}
		List<String> listOfUserIds = new ArrayList<>(dataWithoutRoleCategory.getContent());
		Map<String, List<String>> rolesToUserIdMap = new ConcurrentHashMap<>();
		ForkJoinPool customThreadPool = new ForkJoinPool(10);
		try {
			customThreadPool.submit(() -> listOfUserIds.parallelStream().forEach(userId -> {
				try {
					rolesToUserIdMap.put(userId, retrieveIdamRoles(userId));
				} catch (Exception exception) {
					caseDataRepository.updateRoleCategory(ExceptionLabel, userId);
					log.error(exception.getMessage());
				}
			})).get();
		} catch (Exception exception) {
			log.error(exception.getMessage());
		} finally {
			customThreadPool.shutdown();
		}

		RoleCategory matchingCategory;

		for (String userId : rolesToUserIdMap.keySet()) {
			try {
				matchingCategory = matchRoleCategory(rolesToUserIdMap.get(userId), userId);
				caseDataRepository.updateRoleCategory(matchingCategory.getName(), userId);
			} catch (Exception exception) {
				log.error(exception.getMessage());
			}
		}
		populateNewRoleCategory();
	}

	private List<String> retrieveIdamRoles(String userId) {
		List<String> idamRoles;
		idamRoles = idamRepository.getUserRoles(userId).getRoles();
		return idamRoles;
	}

	private RoleCategory matchRoleCategory(List<String> roles, String userId) {

		RoleCategory firstCategory = null;
		for (String role : roles) {
			RoleCategory newCategory = null;
			for (Pattern pattern : roleCategoryPatterns) {
				if (pattern.matcher(role).matches()) {
					newCategory = RoleCategory.getEnumFromPattern(pattern);
					if (firstCategory == null) {
						firstCategory = newCategory;
						break;
					} else if (firstCategory != newCategory) {
						caseDataRepository.updateRoleCategory(ExceptionLabel, userId);
						throw new MigrationException("Multiple role categories identified for user_id: " + userId);
					}
				}
			}
			if(newCategory == null) {
				caseDataRepository.updateRoleCategory(ExceptionLabel, userId);
				throw new MigrationException("No matching role category found for role: '" + role + "' of user_id: " + userId);
			}
		}
		return firstCategory;
	}
}
