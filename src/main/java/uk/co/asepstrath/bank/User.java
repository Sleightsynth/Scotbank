package uk.co.asepstrath.bank;

import java.util.UUID;

public class User {
    protected UUID id = null;
    protected String email = "";
    protected String passwordHash = "";

    protected String name = "";

    public User(String email, String passwordHash){
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public User(UUID id, String email, String passwordHash, String name){
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
    }

    public String getEmail(){
        return email;
    }

    public String getPasswordHash(){
        return passwordHash;
    }

    public String getName(){
        return name;
    }

    public UUID getId() {
        return id;
    }
}
