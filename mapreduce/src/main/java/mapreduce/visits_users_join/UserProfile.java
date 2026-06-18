package main.java.mapreduce.visits_users_join;

public class UserProfile {

    byte[] firstName;
    byte[] lastName;
    byte[] age;
    byte[] gender;

    public UserProfile(
        byte[] firstName,
        byte[] lastName,
        byte[] age,
        byte[] gender
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
    }
}