package com.github.alexutzzu.motdplugin.toolWindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Calendar;
import java.util.Objects;

final class MemeToolWindow implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MemeContent toolWindowContent = new MemeContent();
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static class MemeContent {
        private static final String MORNING_ICON_PATH = "/icons/morning.png";
        private static final String AFTERNOON_ICON_PATH = "/icons/afternoon.png";
        private static final String EVENING_ICON_PATH = "/icons/evening.png";
        private static final String NIGHT_ICON_PATH = "/icons/night.png";

        private final JPanel contentPanel = new JPanel();
        private final JLabel greetingMessage = new JLabel();
        private final JLabel beverageMessage = new JLabel();
        private final JLabel randomMeme = new JLabel();
        private final HttpClient client = HttpClient.newHttpClient();

        public MemeContent() {
            contentPanel.setLayout(new BorderLayout(0, 20));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
            contentPanel.add(createWelcomeMessagePanel(), BorderLayout.PAGE_START);
            contentPanel.add(createBeveragePanel(), BorderLayout.CENTER);
            contentPanel.add(createMemePanel(), BorderLayout.PAGE_END);
            updateStatus();
        }

        @NotNull
        private JPanel createWelcomeMessagePanel() {
            JPanel panel = new JPanel();
            panel.add(greetingMessage);
            return panel;
        }

        @NotNull
        private JPanel createBeveragePanel() {
            JPanel panel = new JPanel();
            panel.add(beverageMessage);
            return panel;
        }

        private JPanel createMemePanel() {
            JPanel panel = new JPanel();
            panel.add(randomMeme);
            return panel;
        }

        private void updateStatus() {

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            if (hour >= 8 && hour <= 12) {
                greetingMessage.setText("Good morning! Hope you have a fantastic coding session!");
                greetingMessage.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(MORNING_ICON_PATH))));
            } else if (hour > 12 && hour <= 17) {
                greetingMessage.setText("Good afternoon! This is the perfect time to start coding!");
                greetingMessage.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(AFTERNOON_ICON_PATH))));
            } else if (hour > 17 && hour <= 20) {
                greetingMessage.setText("Good evening! Make sure not to push yourself too much in this session");
                greetingMessage.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(EVENING_ICON_PATH))));
            } else {
                greetingMessage.setText("Good night! Night owl sessions are some of the best for programming");
                greetingMessage.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(NIGHT_ICON_PATH))));
            }


            if (hour >= 8 && hour <= 16) {
                beverageMessage.setText("Make sure to drink your beloved coffee for a great coding experience!");
            } else {
                beverageMessage.setText("Don't forget your favourite tea for maximum concentration in this session!");
            }
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://meme-api.com/gimme/programmerhumor"))
                        .GET()
                        .build();

                var future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

                future.thenAccept(response -> {
                    try {
                        if (response.statusCode() == 200) {
                            JSONObject jsonResponse = new JSONObject(response.body());

                            JSONArray links = (JSONArray) jsonResponse.get("preview");
                            String last = links.getString(links.length() - 1);

                            randomMeme.setIcon(new ImageIcon(new URL(last)));
                        }
                    } catch (Exception ignored) {
                    }
                }).exceptionally(e -> null);

            } catch (Exception ignored) {
            }
        }

        public JBScrollPane getContentPanel() {
            JBScrollPane scrollPane = new JBScrollPane(contentPanel);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            return scrollPane;
        }
    }

}