package org.myexample.spinningmotion.business.impl.user;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserTrackingUseCaseImpl {
    private final ConcurrentHashMap<String, String> activeUsers = new ConcurrentHashMap<>();

    public void addUser(String sessionId, String userEmail) {
        activeUsers.put(sessionId, userEmail);
    }

    public void removeUser(String sessionId) {
        activeUsers.remove(sessionId);
    }

    public int getActiveUsersCount() {
        return activeUsers.size();
    }
}