package com.example.demo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CaseDataRepository extends PagingAndSortingRepository<CaseUsersEntity, String> {

    @Query("select c.userId from CaseUsersEntity c where c.roleCategory IS NULL order by c.userId asc")
    Slice<String> findCaseUsersById(Pageable pageable);

    @Query("select c from CaseUsersEntity c where c.roleCategory IS NOT NULL order by c.userId asc")
    Slice<CaseUsersEntity> findCaseUsersWithRoleCategory(Pageable pageable);

    @Query("update CaseUsersEntity c set c.roleCategory = :roleCategory where c.userId = :userId")
    @Modifying
    void updateRoleCategory(String roleCategory, String userId);

}
