import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;
    private JPanel forecastPanel;

    public WeatherAppGui() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 780); // Increased height to accommodate forecast
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        addGuiComponents();
    }

    private void addGuiComponents() {
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(15, 15, 351, 45);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(searchTextField);

        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        // Add forecast section title
        JLabel forecastTitle = new JLabel("3-Day Forecast");
        forecastTitle.setBounds(0, 565, 450, 30);
        forecastTitle.setFont(new Font("Dialog", Font.BOLD, 20));
        forecastTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(forecastTitle);

        // Add forecast panel to hold the forecast days
        forecastPanel = new JPanel();
        forecastPanel.setBounds(15, 600, 420, 130);
        forecastPanel.setLayout(new GridLayout(1, 3, 10, 0));
        forecastPanel.setOpaque(false);
        add(forecastPanel);

        // Initialize with empty forecast panels
        setupEmptyForecastPanels();

        JButton searchButton = new JButton(loadImage("src/assets/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = searchTextField.getText().trim();

                if (userInput.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        WeatherAppGui.this,
                        "Please enter a location name.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                try {
                    weatherData = WeatherApp.getWeatherData(userInput);
                
                    if (weatherData == null) {
                        JOptionPane.showMessageDialog(
                            WeatherAppGui.this,
                            "Place not found: \"" + userInput + "\"",
                            "Location Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                
                } catch (Exception ex) {
                    String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                    if (message.contains("unknownhost") || message.contains("unresolved address")) {
                        JOptionPane.showMessageDialog(
                            WeatherAppGui.this,
                            "No internet connection detected.",
                            "Network Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            WeatherAppGui.this,
                            "Something went wrong while retrieving data.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    return;
                }
                

                // Otherwise, update UI with the returned data
                String weatherCondition = (String) weatherData.get("weather_condition");
                switch (weatherCondition) {
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                }

                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");
                weatherConditionDesc.setText(weatherCondition);

                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "km/h</html>");
                
                // Update forecast
                updateForecastDisplay();
            }
        });
        add(searchButton);
    }
    
    private void setupEmptyForecastPanels() {
        forecastPanel.removeAll();
        
        for (int i = 0; i < 3; i++) {
            JPanel dayPanel = createForecastDayPanel("---", "---", "---", "Cloudy");
            forecastPanel.add(dayPanel);
        }
        
        forecastPanel.revalidate();
        forecastPanel.repaint();
    }
    
    private void updateForecastDisplay() {
        if (weatherData == null || !weatherData.containsKey("forecast")) {
            setupEmptyForecastPanels();
            return;
        }
        
        forecastPanel.removeAll();
        
        try {
            JSONObject forecast = (JSONObject) weatherData.get("forecast");
            JSONArray days = (JSONArray) forecast.get("days");
            
            for (int i = 0; i < days.size(); i++) {
                JSONObject dayForecast = (JSONObject) days.get(i);
                
                String day = (String) dayForecast.get("day");
                double maxTemp = ((Number) dayForecast.get("max_temp")).doubleValue();
                double minTemp = ((Number) dayForecast.get("min_temp")).doubleValue();
                String condition = (String) dayForecast.get("condition");
                
                JPanel dayPanel = createForecastDayPanel(
                    day, 
                    String.format("%.1f", maxTemp),
                    String.format("%.1f", minTemp),
                    condition
                );
                
                forecastPanel.add(dayPanel);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setupEmptyForecastPanels();
        }
        
        forecastPanel.revalidate();
        forecastPanel.repaint();
    }
    
    private JPanel createForecastDayPanel(String day, String maxTemp, String minTemp, String condition) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        panel.setBackground(new Color(240, 240, 240));
        
        // Day label
        JLabel dayLabel = new JLabel(day);
        dayLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dayLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        panel.add(dayLabel, BorderLayout.NORTH);
        
        // Weather icon
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        String iconPath = "src/assets/cloudy.png"; // Default
        
        switch (condition) {
            case "Clear":
                iconPath = "src/assets/clear.png";
                break;
            case "Cloudy":
                iconPath = "src/assets/cloudy.png";
                break;
            case "Rain":
                iconPath = "src/assets/rain.png";
                break;
            case "Snow":
                iconPath = "src/assets/snow.png";
                break;
        }
        
        ImageIcon originalIcon = loadImage(iconPath);
        if (originalIcon != null) {
            // Scale down the icon for the forecast panel
            Image img = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(img));
        }
        
        panel.add(iconLabel, BorderLayout.CENTER);
        
        // Temperature panel (min/max)
        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new GridLayout(2, 1));
        tempPanel.setOpaque(false);
        
        JLabel maxTempLabel = new JLabel("H: " + maxTemp + " C");
        maxTempLabel.setHorizontalAlignment(SwingConstants.CENTER);
        maxTempLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        
        JLabel minTempLabel = new JLabel("L: " + minTemp + " C");
        minTempLabel.setHorizontalAlignment(SwingConstants.CENTER);
        minTempLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        
        tempPanel.add(maxTempLabel);
        tempPanel.add(minTempLabel);
        tempPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        panel.add(tempPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private ImageIcon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Could not find resource: " + resourcePath);
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WeatherAppGui().setVisible(true);
        });
    }
}