package com.example.authswitch;

import java.util.ArrayList;
import java.util.List;

public class AuthData {

    private List<AuthScheme> schemes = new ArrayList<>();
    private String activeSchemeName;

    public List<AuthScheme> getSchemes() {
        return schemes;
    }

    public void setSchemes(List<AuthScheme> schemes) {
        this.schemes = schemes;
    }

    public String getActiveSchemeName() {
        return activeSchemeName;
    }

    public void setActiveSchemeName(String activeSchemeName) {
        this.activeSchemeName = activeSchemeName;
    }
}
