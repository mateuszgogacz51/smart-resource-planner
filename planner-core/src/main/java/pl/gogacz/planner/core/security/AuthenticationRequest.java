package pl.gogacz.planner.core.security;

public record AuthenticationRequest(String username, String password) {}