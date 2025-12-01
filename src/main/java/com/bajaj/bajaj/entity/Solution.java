package com.bajaj.bajaj.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solutions")
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regNo;

    private Integer questionNumber;

    @Column(length = 4000)
    private String finalQuery;

    private LocalDateTime createdAt;

    public Solution() {
    }

    public Solution(String regNo, Integer questionNumber, String finalQuery) {
        this.regNo = regNo;
        this.questionNumber = questionNumber;
        this.finalQuery = finalQuery;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getRegNo() {
        return regNo;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public String getFinalQuery() {
        return finalQuery;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public void setFinalQuery(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

