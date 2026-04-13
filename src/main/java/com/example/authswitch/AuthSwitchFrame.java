package com.example.authswitch;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Comparator;

public class AuthSwitchFrame extends JFrame {

    private final AuthDataStore dataStore = new AuthDataStore();
    private final AuthData authData;

    private final DefaultListModel<String> schemeListModel = new DefaultListModel<>();
    private final JList<String> schemeList = new JList<>(schemeListModel);

    private final JTextField nameField = new JTextField();
    private final JTextField keyField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.LEFT);

    public AuthSwitchFrame() {
        this.authData = dataStore.load();

        setTitle("Auth Switch");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(640, 420));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildCenterPanel(), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        refreshSchemeList();
        if (authData.getActiveSchemeName() != null) {
            statusLabel.setText("当前方案: " + authData.getActiveSchemeName());
        }
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        schemeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        schemeList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            String selected = schemeList.getSelectedValue();
            if (selected == null) {
                return;
            }
            authData.getSchemes().stream()
                    .filter(s -> s.getName().equals(selected))
                    .findFirst()
                    .ifPresent(s -> {
                        nameField.setText(s.getName());
                        keyField.setText(s.getApiKey());
                    });
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(schemeList), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        rightPanel.add(new JLabel("方案名"));
        rightPanel.add(nameField);
        rightPanel.add(new JLabel("OPENAI_API_KEY"));
        rightPanel.add(keyField);

        JButton saveButton = new JButton("保存/更新方案");
        saveButton.addActionListener(e -> saveScheme());

        JButton deleteButton = new JButton("删除方案");
        deleteButton.addActionListener(e -> deleteScheme());

        JButton switchButton = new JButton("切换为当前方案");
        switchButton.addActionListener(e -> switchScheme());

        rightPanel.add(saveButton);
        rightPanel.add(deleteButton);
        rightPanel.add(switchButton);

        panel.add(leftPanel);
        panel.add(rightPanel);
        return panel;
    }

    private void saveScheme() {
        String name = nameField.getText().trim();
        String apiKey = keyField.getText().trim();

        if (name.isEmpty() || apiKey.isEmpty()) {
            showError("方案名和 API Key 不能为空");
            return;
        }

        authData.getSchemes().removeIf(s -> s.getName().equals(name));
        authData.getSchemes().add(new AuthScheme(name, apiKey));
        authData.getSchemes().sort(Comparator.comparing(AuthScheme::getName));
        dataStore.save(authData);
        refreshSchemeList();
        schemeList.setSelectedValue(name, true);
        statusLabel.setText("已保存方案: " + name);
    }

    private void deleteScheme() {
        String selected = schemeList.getSelectedValue();
        if (selected == null) {
            showError("请先选择一个方案");
            return;
        }

        authData.getSchemes().removeIf(s -> s.getName().equals(selected));
        if (selected.equals(authData.getActiveSchemeName())) {
            authData.setActiveSchemeName(null);
        }
        dataStore.save(authData);
        refreshSchemeList();
        nameField.setText("");
        keyField.setText("");
        statusLabel.setText("已删除方案: " + selected);
    }

    private void switchScheme() {
        String selected = schemeList.getSelectedValue();
        if (selected == null) {
            showError("请先选择一个方案");
            return;
        }

        AuthScheme selectedScheme = authData.getSchemes().stream()
                .filter(s -> s.getName().equals(selected))
                .findFirst()
                .orElse(null);

        if (selectedScheme == null) {
            showError("未找到选中方案");
            return;
        }

        dataStore.writeCurrentAuthFile(selectedScheme.getApiKey());
        authData.setActiveSchemeName(selectedScheme.getName());
        dataStore.save(authData);
        refreshSchemeList();
        statusLabel.setText("已切换方案: " + selectedScheme.getName() + "，已写入 ~/.codex/auth.json");
    }

    private void refreshSchemeList() {
        String selectedBefore = schemeList.getSelectedValue();
        schemeListModel.clear();
        for (AuthScheme scheme : authData.getSchemes()) {
            schemeListModel.addElement(scheme.getName());
        }

        if (selectedBefore != null) {
            schemeList.setSelectedValue(selectedBefore, true);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.WARNING_MESSAGE);
    }
}
