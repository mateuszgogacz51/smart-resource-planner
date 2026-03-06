package pl.gogacz.planner.core.security;

import java.util.List;

public record CreateUserRequest(
        String username,
        String password,
        String firstName,
        String lastName,
        List<String> roles
) {}