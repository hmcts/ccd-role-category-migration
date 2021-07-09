package com.example.demo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Table(name = "case_users")
@Entity
@Data
public class CaseUsersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "case_data_id", nullable = false)
    private String caseDataId;

    @Column(name = "case_role", nullable = false)
    private String caseRole;

    @Column(name = "role_category")
    private String roleCategory;

}
