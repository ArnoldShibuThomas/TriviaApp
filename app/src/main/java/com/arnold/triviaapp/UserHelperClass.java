package com.arnold.triviaapp;

public class UserHelperClass {
    String username;
    double score;

    public UserHelperClass(String username, double score) {
        this.username = username;
        this.score = score;
    }

    public UserHelperClass() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
