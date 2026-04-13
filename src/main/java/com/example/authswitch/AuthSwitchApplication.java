package com.example.authswitch;

import javax.swing.SwingUtilities;

public class AuthSwitchApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuthSwitchFrame().setVisible(true));
    }

}
