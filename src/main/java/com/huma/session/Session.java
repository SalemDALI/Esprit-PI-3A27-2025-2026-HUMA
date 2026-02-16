package com.huma.session;

import com.huma.model.User;

public final class Session {
    private static User loggedUser;

    private Session() {
    }

    public static User getLoggedUser() {
        return loggedUser;
    }

    public static void setLoggedUser(User user) {
        loggedUser = user;
    }

    public static void clear() {
        loggedUser = null;
    }

    public static boolean isLoggedIn() {
        return loggedUser != null;
    }
}
