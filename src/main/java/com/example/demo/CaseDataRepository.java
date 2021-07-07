package com.example.demo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CaseDataRepository extends PagingAndSortingRepository<CaseUsersEntity, String> {

    @Query("select c.user_id from CaseUsersEntity c where c.role_category IS NULL order by c.user_id asc")
    Slice<String> findCaseUsersById(Pageable pageable);

    @Query("select c from CaseUsersEntity c where c.role_category IS NOT NULL order by c.user_id asc")
    Slice<CaseUsersEntity> findCaseUsersWithRoleCategory(Pageable pageable);

    @Query("update CaseUsersEntity c set c.role_category = :roleCategory where c.user_id = :userId")
    @Modifying
    void updateRoleCategory(String roleCategory, String userId);

}
