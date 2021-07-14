package uk.gov.hmcts.reform.rolecategorymigration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CaseUsersRepository extends PagingAndSortingRepository<CaseUsersEntity, String> {

    @Query("select distinct c.userId from CaseUsersEntity c where c.roleCategory IS NULL order by c.userId asc")
    Page<String> findCaseUsersById(Pageable pageable);

    @Query("update CaseUsersEntity c set c.roleCategory = :roleCategory where c.userId = :userId")
    @Modifying
    void updateRoleCategory(String roleCategory, String userId);

}
