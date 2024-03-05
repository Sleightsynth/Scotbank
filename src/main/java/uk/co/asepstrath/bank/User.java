package uk.co.asepstrath.bank;

import java.util.UUID;

public class User {
    protected UUID id = null;
    protected String email = "";
    protected String passwordHash = "";

    protected String name = "";

    protected String phoneNo = "";
    protected String address = "";

    protected boolean admin = false;

    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public User(UUID id, String email, String passwordHash, String name, String phoneNo, String address,
            boolean admin) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.phoneNo = phoneNo;
        this.address = address;
        this.admin = admin;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getAddress() {
        return address;
    }
}
