package com.sangjin.habit.DataType;

public class PersonalData {
    private String habitName, created, today;

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    private int idx;

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    private int goalNum;
    private int dayAchieved;
    private int continuity;
    private int dayBefore;
    private int yesterday;

    public String getHabitName() {
        return habitName;
    }

    public void setHabitName(String habitName) {
        this.habitName = habitName;
    }

    public int getGoalNum() {
        return goalNum;
    }

    public void setGoalNum(int goalNum) {
        this.goalNum = goalNum;
    }

    public int getDayAchieved() {
        return dayAchieved;
    }

    public void setDayAchieved(int dayAchieved) {
        this.dayAchieved = dayAchieved;
    }

    public int getContinuity() {
        return continuity;
    }

    public void setContinuity(int continuity) {
        this.continuity = continuity;
    }

    public int getDayBefore() {
        return dayBefore;
    }

    public void setDayBefore(int dayBefore) {
        this.dayBefore = dayBefore;
    }

    public int getYesterday() {
        return yesterday;
    }

    public void setYesterday(int yesterday) {
        this.yesterday = yesterday;
    }


}
