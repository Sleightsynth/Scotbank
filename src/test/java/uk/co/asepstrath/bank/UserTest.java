package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    @Test
    public void initaliseUser(){
        User user = new User("daniel.steven.2022@uni.strath.ac.uk", "password");
        assertNotNull(user);
    }
}
