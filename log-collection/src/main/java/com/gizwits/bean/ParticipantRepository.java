package com.gizwits.bean;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ParticipantRepository {

    private Map<String, String> activeSessions = new ConcurrentHashMap<>();

    public void add(String sessionId, String userName) {
        activeSessions.putIfAbsent(sessionId, userName);
    }

    public String getParticipant(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public void removeParticipant(String sessionId) {
        activeSessions.remove(sessionId);
    }

    public Map<String, String> getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(Map<String, String> activeSessions) {
        this.activeSessions = activeSessions;
    }
}