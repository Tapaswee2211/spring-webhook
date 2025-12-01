package com.bajaj.bajaj.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "challenge")
public class ChallengeProperties {

    /**
     * Your full name.
     */
    private String name;

    /**
     * Your registration number, e.g. REG12347.
     */
    private String regNo;

    /**
     * Your email address.
     */
    private String email;

    /**
     * Final SQL solution for Question 1 (odd last-two digits).
     */
    private String question1Sql;

    /**
     * Final SQL solution for Question 2 (even last-two digits).
     */
    private String question2Sql;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQuestion1Sql() {
        return question1Sql;
    }

    public void setQuestion1Sql(String question1Sql) {
        this.question1Sql = question1Sql;
    }

    public String getQuestion2Sql() {
        return question2Sql;
    }

    public void setQuestion2Sql(String question2Sql) {
        this.question2Sql = question2Sql;
    }
}

