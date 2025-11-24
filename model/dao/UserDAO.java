package dao;

import model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Simple in-memory users
    private final List<User> users = new ArrayList<>();

    public UserDAO() {
        users.add(new User("admin", "admin"));
        users.add(new User("john", "user"));
        users.add(new User("guest", "user"));
    }

    public User login(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }
}
