package com.smartattendance;

public class Student {
    private final int id;
    private final String name;
    private final String rollNumber;

    public Student(int id, String name, String rollNumber) {
        this.id = id;
        this.name = name;
        this.rollNumber = rollNumber;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRollNumber() { return rollNumber; }

    @Override
    public String toString() {
        return name + " (" + rollNumber + ")";
    }
}
