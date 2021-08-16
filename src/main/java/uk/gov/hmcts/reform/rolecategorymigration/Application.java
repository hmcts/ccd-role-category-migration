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

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam"})
@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

	private static final String EXCEPTION_LABEL = "*EXCEPTION*";
	private static int PAGE_NUMBER;
	private static int PAGE_SIZE;
	private static int CONCURRENT_THREADS;

	@Autowired
	private ApplicationParams applicationParams;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private CaseUsersRepository caseUsersRepository;

	@Autowired
	private IdamRepository idamRepository;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("Data migration task has begun");

		PAGE_NUMBER = applicationParams.getPageNumber();
		PAGE_SIZE = applicationParams.getPageSize();
		CONCURRENT_THREADS = applicationParams.getConcurrentThreads();

		long startTime = System.currentTimeMillis();

		log.info("Starting to populate null role categories");

		populateNewRoleCategory();

		long endTime = System.currentTimeMillis();

		log.info("Completed data population task in " + (endTime - startTime) + " milliseconds");

		SpringApplication.exit(context, () -> 0);
	}

	private void populateNewRoleCategory() {

		Page<String> dataWithoutRoleCategory = caseUsersRepository.findCaseUsersById(PageRequest.of(PAGE_NUMBER, PAGE_SIZE));
		if (dataWithoutRoleCategory.isEmpty()) {
			return;
		}
		String token = idamRepository.getAccessToken();
		List<String> userIds = new ArrayList<>(dataWithoutRoleCategory.getContent());
		Map<String, List<String>> userIdToRolesMap = new ConcurrentHashMap<>();
		ForkJoinPool customThreadPool = new ForkJoinPool(CONCURRENT_THREADS);
		try {
			customThreadPool.submit(() -> userIds.parallelStream().forEach(userId -> {
				try {
					log.info("Getting roles for user id {}", userId);
					userIdToRolesMap.put(userId, retrieveIdamRoles(token, userId));
				} catch (Exception exception) {
					caseUsersRepository.updateRoleCategory(EXCEPTION_LABEL, userId);
					log.error(exception.getMessage());
				}
			})).get();
		} catch (Exception exception) {
			log.error(exception.getMessage());
		} finally {
			customThreadPool.shutdown();
		}


		for (String userId : userIdToRolesMap.keySet()) {
			try {
				RoleCategory matchingCategory = matchRoleCategory(userIdToRolesMap.get(userId), userId);
				caseUsersRepository.updateRoleCategory(matchingCategory.getName(), userId);
			} catch (Exception exception) {
				log.error(exception.getMessage());
			}
		}
		populateNewRoleCategory();
	}

	private List<String> retrieveIdamRoles(String token, String userId) {
		return idamRepository.getUserDetails(token, userId).getRoles();
	}

	private RoleCategory matchRoleCategory(List<String> roles, String userId) {
		RoleCategory matchedCategory = null;
		for (String role : roles) {
			for (RoleCategory category : RoleCategory.values()) {
				if (category.getPattern().matcher(role).matches()) {
					if (matchedCategory == null) {
						matchedCategory = category;
						break;
					} else if (matchedCategory != category) {
						caseUsersRepository.updateRoleCategory(EXCEPTION_LABEL, userId);
						throw new MigrationException("Multiple role categories identified for user_id: " + userId);
					}
				}
			}
		}
		if (matchedCategory == null) {
			// Default to citizen if there are no roles
			if (roles.isEmpty()) {
				return RoleCategory.CITIZEN;
			}
			caseUsersRepository.updateRoleCategory(EXCEPTION_LABEL, userId);
			throw new MigrationException("No matching role category found for user_id: " + userId);
		}
		return matchedCategory;
	}
}
