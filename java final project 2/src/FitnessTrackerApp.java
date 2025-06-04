import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.time.DayOfWeek; // Added for WeeklyPlan
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map; // Added for WeeklyPlan
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
// Added imports
import javax.swing.Timer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Timestamp;


public class FitnessTrackerApp { // Renamed from main to FitnessTrackerApp

    private JFrame frame;
    private JComboBox<BodyPart> bodyPartComboBox;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JTextField cardioTime;
    private JList<Exercise> availableExercisesList;
    private DefaultListModel<Exercise> availableExercisesListModel;
    private JList<WorkoutExercise> selectedExercisesList;
    private DefaultListModel<WorkoutExercise> selectedExercisesListModel;
    // private JTextField setsField;
    // private JTextField repsField;
    // private JTextField weightField;
    private JComboBox<Integer> setsComboBox;
    private JComboBox<Integer> repsComboBox;
    private JComboBox<Double> weightComboBox;    // Timer components
    private JButton restTimerButton;
    private JLabel restTimerLabel;
    private javax.swing.Timer stopwatch;
    private boolean stopwatchRunning = false;
    private int elapsedSeconds = 0;

    // Modern UI Color Scheme
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color CARD_SHADOW = new Color(0, 0, 0, 20);
    private static final Color PRIMARY_COLOR = new Color(64, 128, 255);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color BORDER_COLOR = new Color(222, 226, 230);

    // Custom Card Panel with rounded corners and shadow
    private static class CardPanel extends JPanel {
        private int arcWidth = 15;
        private int arcHeight = 15;
        private Color shadowColor = CARD_SHADOW;
        private int shadowSize = 4;

        public CardPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBackground(CARD_BACKGROUND);
            setBorder(new EmptyBorder(shadowSize, shadowSize, shadowSize, shadowSize));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth() - shadowSize;
            int height = getHeight() - shadowSize;

            // Draw shadow
            g2.setColor(shadowColor);
            g2.fillRoundRect(shadowSize, shadowSize, width, height, arcWidth, arcHeight);

            // Draw card background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, width, height, arcWidth, arcHeight);

            g2.dispose();
            super.paintComponent(g);
        }
    }    // Modern Button with 3D shadow effect
    private static class ModernButton extends JButton {
        private boolean isPressed = false;

        public ModernButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(getStaticChineseFont(Font.BOLD, 12));
            setForeground(Color.WHITE);
            setBackground(PRIMARY_COLOR);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 24, 12, 24));

            // Add mouse listeners for 3D effect
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int shadowOffset = isPressed ? 2 : 4;
            int buttonOffset = isPressed ? 2 : 0;

            // Draw shadow
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(shadowOffset, shadowOffset, width - shadowOffset, height - shadowOffset, 10, 10);

            // Draw button
            g2.setColor(getBackground());
            g2.fillRoundRect(buttonOffset, buttonOffset, width - shadowOffset, height - shadowOffset, 10, 10);

            // Draw text
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int textX = (width - fm.stringWidth(getText())) / 2;
            int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int width = fm.stringWidth(getText()) + 48; // 24 padding on each side
            int height = fm.getHeight() + 24; // 12 padding top and bottom
            return new Dimension(width, height);
        }

        // Method to set button color scheme
        public void setButtonStyle(Color backgroundColor, Color textColor) {
            setBackground(backgroundColor);
            setForeground(textColor);
            repaint();
        }

        // Static method for Chinese font in static context
        private static Font getStaticChineseFont(int style, int size) {
            String[] chineseFonts = {
                "Microsoft YaHei",
                "SimHei",
                "SimSun",
                "Microsoft JhengHei",
                "PMingLiU",
                "Dialog"
            };
            
            for (String fontName : chineseFonts) {
                Font font = new Font(fontName, style, size);
                if (font.canDisplay('中') && font.canDisplay('文')) {
                    return font;
                }
            }
            
            return new Font(Font.SANS_SERIF, style, size);
        }
    }

    // Weekly Plan components
    private WeeklyPlan weeklyPlanManager;
    private JComboBox<DayOfWeek> dayOfWeekComboBox;
    private JComboBox<BodyPart> planBodyPartComboBox;
    private JList<Exercise> planAvailableExercisesList;
    private DefaultListModel<Exercise> planAvailableExercisesListModel;
    private JList<String> dailyPlanList; // To display plan for the selected day
    private DefaultListModel<String> dailyPlanListModel;

    public FitnessTrackerApp() { // Renamed constructor
        weeklyPlanManager = new WeeklyPlan(); // Initialize WeeklyPlan manager
        createAndShowGUI();
    }    private void createAndShowGUI() {
        frame = new JFrame("健身追蹤器");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BACKGROUND_COLOR);

        // Modern styled tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(getChineseFont(Font.BOLD, 14));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setForeground(SECONDARY_COLOR);

        // Panel for recording workouts
        JPanel recordWorkoutPanel = createRecordWorkoutPanel();
        tabbedPane.addTab("記錄健身課程", recordWorkoutPanel);

        // Panel for weekly plan
        JPanel weeklyPlanPanel = createWeeklyPlanPanel(); // Call new method
        tabbedPane.addTab("每週計劃", weeklyPlanPanel);

        JPanel gymStatusPanel = new GymStatusPanel();
        tabbedPane.addTab("健身房即時狀態", gymStatusPanel);

        frame.add(tabbedPane);
        frame.setVisible(true);
        new TrainingReminderGUI();
    }private JPanel createRecordWorkoutPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));        // Top Panel: Body part, Start time, End time - Modern Card Style
        CardPanel topCard = new CardPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topCard.setBorder(new CompoundBorder(
            topCard.getBorder(),
            new TitledBorder(null, "健身課程設置", TitledBorder.LEFT, TitledBorder.TOP, 
                getChineseFont(Font.BOLD, 14), PRIMARY_COLOR)
        ));

        topCard.add(createStyledLabel("選擇部位:"));
        bodyPartComboBox = createStyledComboBox(BodyPart.values());
        bodyPartComboBox.setSelectedItem(BodyPart.CHEST); // Default selection
        // Add ActionListener to bodyPartComboBox
        bodyPartComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAvailableExercises();
            }
        });
        topCard.add(bodyPartComboBox);

        topCard.add(Box.createRigidArea(new Dimension(10, 0)));
        topCard.add(createStyledLabel("輸入有氧時間(分鐘分鐘):"));
        cardioTime = createStyledTextField("", 4);
        topCard.add(cardioTime);

        topCard.add(Box.createRigidArea(new Dimension(10, 0)));
        topCard.add(createStyledLabel(""));  /*開始時間 (YYYY-MM-DD HH:MM):*/
        startTimeField = createStyledTextField("2025-06-03 09:20", 16);  /* 這邊我想放現在時間local time */
        topCard.add(startTimeField);

        topCard.add(Box.createRigidArea(new Dimension(10, 0)));
        topCard.add(createStyledLabel("結束時間 (YYYY-MM-DD HH:MM):"));
        endTimeField = createStyledTextField("2025-05-20 10:48", 16);
        topCard.add(endTimeField);

        mainPanel.add(topCard, BorderLayout.NORTH);        // Center Panel: Available Exercises, Add/Remove Buttons, Selected Exercises - Modern Card Style
        CardPanel centerCard = new CardPanel(new GridBagLayout());
        centerCard.setBorder(new CompoundBorder(
            centerCard.getBorder(),
            new TitledBorder(null, "練習選擇", TitledBorder.LEFT, TitledBorder.TOP, 
                getChineseFont(Font.BOLD, 14), PRIMARY_COLOR)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Available Exercises
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;        CardPanel availablePanel = new CardPanel(new BorderLayout());
        availablePanel.setBorder(new CompoundBorder(
            availablePanel.getBorder(),
            new TitledBorder(null, "可用練習", TitledBorder.CENTER, TitledBorder.TOP, 
                getChineseFont(Font.BOLD, 12), SECONDARY_COLOR)
        ));        availableExercisesListModel = new DefaultListModel<>();
        availableExercisesList = new JList<>(availableExercisesListModel);
        availableExercisesList.setCellRenderer(new ExerciseListCellRenderer());
        availableExercisesList.setBackground(Color.WHITE);
        // Improved selection colors for better readability
        availableExercisesList.setSelectionBackground(new Color(230, 245, 255)); // Light blue background
        availableExercisesList.setSelectionForeground(new Color(0, 60, 120)); // Dark blue text
        availableExercisesList.setFont(getChineseFont(Font.PLAIN, 14)); // Larger font size
        JScrollPane availableScrollPane = new JScrollPane(availableExercisesList);
        availableScrollPane.setBorder(null);
        availablePanel.add(availableScrollPane, BorderLayout.CENTER);
        centerCard.add(availablePanel, gbc);

        // Add/Remove Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        
        ModernButton addExerciseButton = new ModernButton("添加練習 >>");
        addExerciseButton.setButtonStyle(SUCCESS_COLOR, Color.WHITE);
        ModernButton removeExerciseButton = new ModernButton("<< 移除練習");
        removeExerciseButton.setButtonStyle(new Color(220, 53, 69), Color.WHITE);

        addExerciseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSelectedExercise();
            }
        });        
        removeExerciseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Add remove functionality here
                int selectedIndex = selectedExercisesList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    selectedExercisesListModel.remove(selectedIndex);
                }
            }
        });

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(addExerciseButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(removeExerciseButton);
        buttonPanel.add(Box.createVerticalGlue());

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        centerCard.add(buttonPanel, gbc);

        // Selected Exercises
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;        CardPanel selectedPanel = new CardPanel(new BorderLayout());
        selectedPanel.setBorder(new CompoundBorder(
            selectedPanel.getBorder(),
            new TitledBorder(null, "已選練習", TitledBorder.CENTER, TitledBorder.TOP, 
                getChineseFont(Font.BOLD, 12), SECONDARY_COLOR)
        ));        selectedExercisesListModel = new DefaultListModel<>();
        selectedExercisesList = new JList<>(selectedExercisesListModel);
        selectedExercisesList.setCellRenderer(new WorkoutExerciseListCellRenderer());
        selectedExercisesList.setBackground(Color.WHITE);
        // Improved selection colors for better readability
        selectedExercisesList.setSelectionBackground(new Color(245, 250, 245)); // Light green background
        selectedExercisesList.setSelectionForeground(new Color(0, 100, 0)); // Dark green text
        selectedExercisesList.setFont(getChineseFont(Font.PLAIN, 14)); // Larger font size
        JScrollPane selectedScrollPane = new JScrollPane(selectedExercisesList);
        selectedScrollPane.setBorder(null);
        selectedPanel.add(selectedScrollPane, BorderLayout.CENTER);
        centerCard.add(selectedPanel, gbc);
        
        mainPanel.add(centerCard, BorderLayout.CENTER);
          // Bottom Panel: Sets, Reps, Weight, Record Button - Modern Card Style
        CardPanel bottomCard = new CardPanel(new BorderLayout(10, 10));        bottomCard.setBorder(new CompoundBorder(
            bottomCard.getBorder(),
            new TitledBorder(null, "練習詳細與記錄", TitledBorder.LEFT, TitledBorder.TOP, 
                getChineseFont(Font.BOLD, 14), PRIMARY_COLOR)
        ));

        // Exercise Details Panel
        JPanel exerciseDetailsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        exerciseDetailsPanel.setOpaque(false);
        
        exerciseDetailsPanel.add(createStyledLabel("組數:"));
        Integer[] setValues = new Integer[10];
        for (int i = 0; i < 10; i++) setValues[i] = i + 1;
        setsComboBox = createStyledComboBox(setValues);
        exerciseDetailsPanel.add(setsComboBox);

        exerciseDetailsPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        exerciseDetailsPanel.add(createStyledLabel("次數:"));
        Integer[] repValues = new Integer[20];
        for (int i = 0; i < 20; i++) repValues[i] = i + 1;
        repsComboBox = createStyledComboBox(repValues);
        exerciseDetailsPanel.add(repsComboBox);

        exerciseDetailsPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        exerciseDetailsPanel.add(createStyledLabel("重量 (kg):"));
        Double[] weightValues = new Double[41];
        for (int i = 0; i <= 40; i++) weightValues[i] = i * 2.5;
        weightComboBox = createStyledComboBox(weightValues);
        exerciseDetailsPanel.add(weightComboBox);

        bottomCard.add(exerciseDetailsPanel, BorderLayout.CENTER);

        // Action Buttons Panel
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonPanel.setOpaque(false);
        
        ModernButton recordExercisesButton = new ModernButton("記錄健身課程");
        recordExercisesButton.setButtonStyle(PRIMARY_COLOR, Color.WHITE);
        ModernButton callWindowButton = new ModernButton("分析數據");
        callWindowButton.setButtonStyle(SECONDARY_COLOR, Color.WHITE);

        // Action Listener for recordExercisesButton
        recordExercisesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveWorkoutSessionAndExercises();
            }
        });

        actionButtonPanel.add(recordExercisesButton);
        actionButtonPanel.add(callWindowButton);        // Timer components
        restTimerButton = new ModernButton("開始休息");
        ((ModernButton)restTimerButton).setButtonStyle(new Color(255, 193, 7), new Color(33, 37, 41));        restTimerLabel = createStyledLabel("休息時間: 00:00");
        restTimerLabel.setFont(getChineseFont(Font.BOLD, 14));
        restTimerLabel.setForeground(PRIMARY_COLOR);
        
        restTimerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!stopwatchRunning) {
                    elapsedSeconds = 0; // Reset for new rest period
                    if (stopwatch == null) {
                        stopwatch = new Timer(1000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                elapsedSeconds++;
                                int minutes = elapsedSeconds / 60;
                                int seconds = elapsedSeconds % 60;
                                restTimerLabel.setText(String.format("休息時間: %02d:%02d", minutes, seconds));
                            }
                        });
                    }
                    restTimerLabel.setText("休息時間: 00:00"); // Reset label display
                    stopwatch.start();
                    restTimerButton.setText("結束休息");
                    stopwatchRunning = true;
                } else {
                    if (stopwatch != null) {
                        stopwatch.stop();
                    }
                    // The label already shows the final time.
                    restTimerButton.setText("開始休息");
                    stopwatchRunning = false;
                    // elapsedSeconds now holds the total rest time for the last period
                }
            }
        });
        
        actionButtonPanel.add(restTimerButton);
        actionButtonPanel.add(restTimerLabel);
        
        bottomCard.add(actionButtonPanel, BorderLayout.SOUTH);        
        callWindowButton.addActionListener(e->{
            List<WorkoutData> data = WorkoutData.fetchRecentData();
            if(!data.isEmpty()){
                new AnalysisWindow(data);
            } else {
                JOptionPane.showMessageDialog(mainPanel, "沒有可用的健身數據進行分析。", "無數據", JOptionPane.WARNING_MESSAGE);
            }           // Open the call window with the workout data
        });
        
        mainPanel.add(bottomCard, BorderLayout.SOUTH);

        // Initial population of available exercises
        updateAvailableExercises();

        return mainPanel;

// recordExercisesButton 加入ACTIONLISTNER存入資料庫的 workout_exercises 資料表，並加入計時器按鈕，按移下開始碼表計時，在按一下結束，計算每組的休息時間
    }    // Helper methods for creating styled components
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(getChineseFont(Font.BOLD, 12));
        label.setForeground(SECONDARY_COLOR);
        return label;
    }

    private JTextField createStyledTextField(String text, int columns) {
        JTextField textField = new JTextField(text, columns);
        textField.setFont(getChineseFont(Font.PLAIN, 12));
        textField.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        textField.setBackground(Color.WHITE);
        return textField;
    }

    private <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(getChineseFont(Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return comboBox;
    }    // Method to get proper Chinese font
    private Font getChineseFont(int style, int size) {
        // Try different Chinese fonts in order of preference
        String[] chineseFonts = {
            "Microsoft YaHei",  // 微软雅黑
            "SimHei",           // 黑体
            "SimSun",           // 宋体
            "Microsoft JhengHei", // 微軟正黑體
            "PMingLiU",         // 新細明體
            "Dialog"            // Fallback font
        };
        
        for (String fontName : chineseFonts) {
            Font font = new Font(fontName, style, size);
            if (font.canDisplay('中') && font.canDisplay('文')) {
                return font;
            }
        }
        
        // Final fallback
        return new Font(Font.SANS_SERIF, style, size);
    }

    // Static method for Chinese font in static context (for renderers)
    private static Font getStaticChineseFont(int style, int size) {
        String[] chineseFonts = {
            "Microsoft YaHei",
            "SimHei",
            "SimSun",
            "Microsoft JhengHei",
            "PMingLiU",
            "Dialog"
        };
        
        for (String fontName : chineseFonts) {
            Font font = new Font(fontName, style, size);
            if (font.canDisplay('中') && font.canDisplay('文')) {
                return font;
            }
        }
        
        return new Font(Font.SANS_SERIF, style, size);
    }

    private void saveWorkoutSessionAndExercises() {
        String startTimeStr = startTimeField.getText();
        String endTimeStr = endTimeField.getText();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
        Timestamp startTimeTs = null;
        Timestamp endTimeTs = null;
        int cardioNum = Integer.parseInt(cardioTime.getText());

        try {
            if (startTimeStr.trim().isEmpty() || endTimeStr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "開始和結束時間不能為空。", "時間錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Date parsedStartTime = sdf.parse(startTimeStr);
            startTimeTs = new Timestamp(parsedStartTime.getTime());
            Date parsedEndTime = sdf.parse(endTimeStr);
            endTimeTs = new Timestamp(parsedEndTime.getTime());

            if(endTimeTs.before(startTimeTs)){
                JOptionPane.showMessageDialog(frame, "結束時間不能早於開始時間。", "時間邏輯錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }

        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(frame, "開始或結束時間格式不正確。請使用 YYYY-MM-DD HH:MM 格式。", "時間格式錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedExercisesListModel.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "請先添加運動到已選練習列表。", "無運動記錄", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection(); 
            conn.setAutoCommit(false); // Start transaction

            // 1. Save WorkoutSession and get its ID
            String sessionSql = "INSERT INTO workout_sessions (start_time, end_time, cardio_time) VALUES (?, ?, ?)";
            PreparedStatement sessionPstmt = conn.prepareStatement(sessionSql, PreparedStatement.RETURN_GENERATED_KEYS);
            sessionPstmt.setTimestamp(1, startTimeTs);
            sessionPstmt.setTimestamp(2, endTimeTs);
            sessionPstmt.setInt(3, cardioNum); // Save cardio time in minutes
            int affectedRows = sessionPstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("創建健身課程記錄失敗，沒有行受到影響。");
            }

            int sessionId;
            try (ResultSet generatedKeys = sessionPstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    sessionId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("創建健身課程記錄失敗，無法獲取ID。");
                }
            }
            sessionPstmt.close();

            // 2. Save each WorkoutExercise to workout_exercises table
            // Column order: workout_session_id, exercise_id, sets, reps, weight, rest_periods_seconds
            String exerciseSql = "INSERT INTO workout_exercises (workout_session_id, exercise_id, sets, reps, weight, rest_periods_seconds) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement exercisePstmt = conn.prepareStatement(exerciseSql);

            for (int i = 0; i < selectedExercisesListModel.getSize(); i++) {
                WorkoutExercise we = selectedExercisesListModel.getElementAt(i);
                if (we.getExercise() == null || we.getExercise().getId() == 0) { 
                     JOptionPane.showMessageDialog(frame, "選中的運動 '" + (we.getExercise() != null ? we.getExercise().getName() : "未知") + "' 沒有有效的ID。", "運動數據錯誤", JOptionPane.ERROR_MESSAGE);
                     conn.rollback();
                     return;
                }
                exercisePstmt.setInt(1, sessionId);
                exercisePstmt.setInt(2, we.getExercise().getId()); 
                exercisePstmt.setInt(3, we.getSets());
                exercisePstmt.setInt(4, we.getReps());
                exercisePstmt.setDouble(5, we.getWeight());
                exercisePstmt.setInt(6, elapsedSeconds); // Save the last recorded rest period
                exercisePstmt.addBatch();
            }
            exercisePstmt.executeBatch();
            exercisePstmt.close();

            conn.commit(); // Commit transaction
            JOptionPane.showMessageDialog(frame, "健身課程已成功記錄！", "記錄成功", JOptionPane.INFORMATION_MESSAGE);

            selectedExercisesListModel.clear();
            // Reset timer display and value after successful save
            restTimerLabel.setText("休息時間: 00:00");
            elapsedSeconds = 0; 
            // Optionally clear/reset time fields if desired
            // startTimeField.setText("YYYY-MM-DD HH:MM"); 
            // endTimeField.setText("YYYY-MM-DD HH:MM");

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback(); 
                } catch (SQLException e_roll) {
                    System.err.println("Rollback failed: " + e_roll.getMessage());
                }
            }
            JOptionPane.showMessageDialog(frame, "記錄健身課程時發生資料庫錯誤: " + ex.getMessage(), "資料庫錯誤", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); 
        } catch (NullPointerException npe) { 
             JOptionPane.showMessageDialog(frame, "發生了空指針異常，可能是運動數據不完整: " + npe.getMessage(), "程序錯誤", JOptionPane.ERROR_MESSAGE);
             npe.printStackTrace();
             if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
        }
        finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex_close) {
                     System.err.println("Failed to close connection: " + ex_close.getMessage());
                }
            }
        }
    }

    private JPanel createWeeklyPlanPanel() {
        JPanel weeklyPlanPanel = new JPanel(new BorderLayout(10, 10));
        weeklyPlanPanel.setBackground(BACKGROUND_COLOR);
        weeklyPlanPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top panel for day selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.add(createStyledLabel("選擇星期:"));
        dayOfWeekComboBox = createStyledComboBox(DayOfWeek.values());
        dayOfWeekComboBox.addActionListener(e -> updateDailyPlanView());
        topPanel.add(dayOfWeekComboBox);
        weeklyPlanPanel.add(topPanel, BorderLayout.NORTH);

        // Main content split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setBorder(null);

        // Left panel for adding exercises
        CardPanel addExerciseCard = new CardPanel(new BorderLayout(10, 10));
        addExerciseCard.setBorder(new CompoundBorder(
            new TitledBorder(BorderFactory.createLineBorder(BORDER_COLOR), "添加運動到計劃", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, getChineseFont(Font.BOLD, 12), PRIMARY_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel addExerciseControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addExerciseControlsPanel.setBackground(CARD_BACKGROUND);
        addExerciseControlsPanel.add(createStyledLabel("身體部位:"));
        planBodyPartComboBox = createStyledComboBox(BodyPart.values());
        planBodyPartComboBox.addActionListener(e -> updatePlanAvailableExercises());
        addExerciseControlsPanel.add(planBodyPartComboBox);
        addExerciseCard.add(addExerciseControlsPanel, BorderLayout.NORTH);

        planAvailableExercisesListModel = new DefaultListModel<>();
        planAvailableExercisesList = new JList<>(planAvailableExercisesListModel);
        planAvailableExercisesList.setCellRenderer(new ExerciseListCellRenderer());
        planAvailableExercisesList.setFont(getChineseFont(Font.PLAIN, 14));
        JScrollPane planAvailableExercisesScrollPane = new JScrollPane(planAvailableExercisesList);
        planAvailableExercisesScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        addExerciseCard.add(planAvailableExercisesScrollPane, BorderLayout.CENTER);

        ModernButton addExerciseToPlanButton = new ModernButton("添加到計劃");
        addExerciseToPlanButton.setButtonStyle(SUCCESS_COLOR, Color.WHITE);
        addExerciseToPlanButton.addActionListener(e -> addExerciseToWeeklyPlan()); // Added ActionListener
        addExerciseCard.add(addExerciseToPlanButton, BorderLayout.SOUTH);

        splitPane.setLeftComponent(addExerciseCard);

        // Right panel for displaying daily plan
        CardPanel dailyPlanCard = new CardPanel(new BorderLayout(10, 10));
        dailyPlanCard.setBorder(new CompoundBorder(
            new TitledBorder(BorderFactory.createLineBorder(BORDER_COLOR), "當日計劃", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, getChineseFont(Font.BOLD, 12), PRIMARY_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));

        dailyPlanListModel = new DefaultListModel<>();
        dailyPlanList = new JList<>(dailyPlanListModel);
        dailyPlanList.setFont(getChineseFont(Font.PLAIN, 14));
        JScrollPane dailyPlanScrollPane = new JScrollPane(dailyPlanList);
        dailyPlanScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        dailyPlanCard.add(dailyPlanScrollPane, BorderLayout.CENTER);

        ModernButton removeExerciseFromPlanButton = new ModernButton("從計劃中移除選定項");
        removeExerciseFromPlanButton.setButtonStyle(SECONDARY_COLOR, Color.WHITE);
        removeExerciseFromPlanButton.addActionListener(e -> removeExerciseFromWeeklyPlan());
        dailyPlanCard.add(removeExerciseFromPlanButton, BorderLayout.SOUTH);

        splitPane.setRightComponent(dailyPlanCard);
        weeklyPlanPanel.add(splitPane, BorderLayout.CENTER);

        // Initialize views
        updatePlanAvailableExercises();
        updateDailyPlanView();

        return weeklyPlanPanel;
    }

    private static class GymStatusPanel extends JPanel {
        private JLabel occupancyLabel;
        private LightPanel lightPanel;
        private JLabel suggestionLabel;
        private JButton refreshButton;
        private JLabel idLabel;
        private JTextField idField;
        private JButton checkMembershipButton;
        private JLabel membershipLabel;

        private Connection conn;

        private static final String DB_URL = "jdbc:mysql://140.119.19.73:3315/TG10?useSSL=false";
        private static final String DB_USER = "TG10";
        private static final String DB_PW = "iRIzsI";

        public GymStatusPanel() {
            setLayout(new BorderLayout());
            initDB();
            buildUI();
            updateOccupancy();
        }

        private void initDB() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PW);
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "找不到資料庫驅動: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "資料庫連線失敗: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void buildUI() {
            JPanel top = new JPanel(new FlowLayout());
            occupancyLabel = new JLabel("目前人數: -- / --");
            lightPanel = new LightPanel();
            suggestionLabel = new JLabel("建議: --");
            refreshButton = new JButton("刷新狀態");
            refreshButton.addActionListener(e -> updateOccupancy());
            top.add(occupancyLabel);
            top.add(lightPanel);
            top.add(suggestionLabel);
            top.add(refreshButton);
            add(top, BorderLayout.NORTH);

            ImagePanel imgPanel = new ImagePanel("/images/gym_diagram.png");
            add(imgPanel, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout());
            idLabel = new JLabel("會員ID:");
            idField = new JTextField(10);
            checkMembershipButton = new JButton("查詢會員");
            checkMembershipButton.addActionListener(e -> checkMembership());
            membershipLabel = new JLabel("剩餘時效: --");
            bottom.add(idLabel);
            bottom.add(idField);
            bottom.add(checkMembershipButton);
            bottom.add(membershipLabel);
            add(bottom, BorderLayout.SOUTH);
        }

        class ImagePanel extends JPanel {
            private final Image image;

            /** path 可以用 getResource 取得的 classpath 路徑 */
            public ImagePanel(String pathInClasspath) {
                URL url = getClass().getResource(pathInClasspath);
                if (url == null)
                    throw new IllegalArgumentException("找不到圖片: " + pathInClasspath);
                image = new ImageIcon(url).getImage();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (image == null) return;

                int pw = getWidth(), ph = getHeight();
                double iw = image.getWidth(null), ih = image.getHeight(null);
                double scale = Math.min(pw / iw, ph / ih);     // 等比例
                int w = (int) (iw * scale);
                int h = (int) (ih * scale);
                int x = (pw - w) / 2;
                int y = (ph - h) / 2;

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(image, x, y, w, h, this);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(400, 300); 
            }
        }


        private void updateOccupancy() {
            if (conn == null) return;
            String sql = "SELECT current_people, total_people FROM real_time_status ORDER BY time DESC LIMIT 1";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    int cur = rs.getInt("current_people");
                    int tot = rs.getInt("total_people");
                    occupancyLabel.setText("目前人數: " + cur + " / " + tot);
                    double ratio = (double) cur / tot;
                    if (ratio > 0.8) {
                        lightPanel.setColor(Color.RED);
                        suggestionLabel.setText("建議: 請避開高峰");
                    } else if (ratio > 0.5) {
                        lightPanel.setColor(Color.ORANGE);
                        suggestionLabel.setText("建議: 適中，可斟酌");
                    } else {
                        lightPanel.setColor(Color.GREEN);
                        suggestionLabel.setText("建議: 舒適，適合前往");
                    }
                    lightPanel.repaint();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "查詢佔用率失敗: " + ex.getMessage());
            }
        }

        private void checkMembership() {
            if (conn == null) return;
            String id = idField.getText().trim();
            if (id.isEmpty()) { membershipLabel.setText("請輸入ID"); return; }
            String sql = "SELECT register_date, duration FROM member WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        LocalDate reg = rs.getDate("register_date").toLocalDate();
                        int dur = rs.getInt("duration");
                        LocalDate expiry = reg.plusDays(dur);
                        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
                        membershipLabel.setText(daysLeft < 0 ? "已過期" : "剩餘: " + daysLeft + " 天");
                    } else {
                        membershipLabel.setText("查無此會員");
                    }
                }
            } catch (SQLException e) {
                membershipLabel.setText("查詢失敗");
            }
        }
        private static class LightPanel extends JPanel {
            private Color color = Color.GRAY;
            public void setColor(Color c) { this.color = c; }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(color);
                g.fillOval(0, 0, getWidth(), getHeight());
            }
            @Override public Dimension getPreferredSize() { return new Dimension(30,30); }
        }
    }

    private void updatePlanAvailableExercises() {
        BodyPart selectedPart = (BodyPart) planBodyPartComboBox.getSelectedItem();
        planAvailableExercisesListModel.clear();
        if (selectedPart != null) {
            List<Exercise> exercises = ExerciseData.getExercisesByBodyPart(selectedPart);
            for (Exercise ex : exercises) {
                planAvailableExercisesListModel.addElement(ex);
            }
        } else { // If no specific body part, show all or a default set
            List<Exercise> allExercises = ExerciseData.getAllExercises(); // Assuming this method exists or can be added
            for (Exercise ex : allExercises) {
                planAvailableExercisesListModel.addElement(ex);
            }
        }
    }

    private void addExerciseToWeeklyPlan() {
        DayOfWeek selectedDay = (DayOfWeek) dayOfWeekComboBox.getSelectedItem();
        BodyPart selectedBodyPart = (BodyPart) planBodyPartComboBox.getSelectedItem();
        Exercise selectedExercise = planAvailableExercisesList.getSelectedValue();

        if (selectedDay == null || selectedBodyPart == null || selectedExercise == null) {
            JOptionPane.showMessageDialog(frame, "請選擇星期、身體部位和要添加的運動。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        weeklyPlanManager.addExerciseToPlan(selectedDay, selectedBodyPart, selectedExercise);
        weeklyPlanManager.savePlanToDatabase(); // Save to DB after adding
        updateDailyPlanView(); // Refresh the view of the plan for the day
    }

    private void removeExerciseFromWeeklyPlan() {
        DayOfWeek selectedDay = (DayOfWeek) dayOfWeekComboBox.getSelectedItem();
        String selectedPlanEntry = dailyPlanList.getSelectedValue(); // This will be a string like "BodyPart: ExerciseName"

        if (selectedDay == null || selectedPlanEntry == null) {
            JOptionPane.showMessageDialog(frame, "請選擇星期和要移除的計劃項。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Need to parse selectedPlanEntry to get BodyPart and Exercise
        // This is a simplified approach; a more robust way would be to store Exercise objects or more structured data in dailyPlanListModel
        try {
            String[] parts = selectedPlanEntry.split(": ");
            String bodyPartString = parts[0].trim();
            String exerciseNameString = parts[1].trim();
            
            BodyPart partToRemove = BodyPart.valueOf(bodyPartString.toUpperCase()); // This might fail if BodyPart enum names don't match display
            
            // Find the exercise object by name (assuming names are unique for simplicity here)
            Exercise exerciseToRemove = null;
            Map<BodyPart, List<Exercise>> planForDay = weeklyPlanManager.getPlanForDay(selectedDay);
            if (planForDay != null && planForDay.containsKey(partToRemove)) {
                for (Exercise ex : planForDay.get(partToRemove)) {
                    if (ex.getName().equals(exerciseNameString)) {
                        exerciseToRemove = ex;
                        break;
                    }
                }
            }

            if (exerciseToRemove != null) {
                weeklyPlanManager.removeExerciseFromPlan(selectedDay, partToRemove, exerciseToRemove);
                weeklyPlanManager.savePlanToDatabase(); // Save to DB after removing
                updateDailyPlanView();
            } else {
                JOptionPane.showMessageDialog(frame, "無法找到要移除的運動。", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "移除運動時出錯: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // For debugging
        }
    }

    private void updateDailyPlanView() {
        DayOfWeek selectedDay = (DayOfWeek) dayOfWeekComboBox.getSelectedItem();
        dailyPlanListModel.clear();
        if (selectedDay != null) {
            Map<BodyPart, List<Exercise>> planForDay = weeklyPlanManager.getPlanForDay(selectedDay);
            if (planForDay != null && !planForDay.isEmpty()) {
                for (Map.Entry<BodyPart, List<Exercise>> entry : planForDay.entrySet()) {
                    BodyPart part = entry.getKey();
                    for (Exercise ex : entry.getValue()) {
                        dailyPlanListModel.addElement(part.toString() + ": " + ex.getName());
                    }
                }
            }
            if (dailyPlanListModel.isEmpty()){
                dailyPlanListModel.addElement("本日無計劃");
            }
        }
    }

    private void updateAvailableExercises() {
        BodyPart selectedPart = (BodyPart) bodyPartComboBox.getSelectedItem();
        if (selectedPart != null) {
            // Assuming ExerciseData.getExercisesByBodyPart is a static method
            List<Exercise> exercises = ExerciseData.getExercisesByBodyPart(selectedPart);
            availableExercisesListModel.clear();
            for (Exercise ex : exercises) {
                availableExercisesListModel.addElement(ex);
            }
        }
    }

    private void addSelectedExercise() {
        Exercise selectedExercise = availableExercisesList.getSelectedValue();
        if (selectedExercise == null) {
            JOptionPane.showMessageDialog(frame, "請先從可用練習列表中選擇一個練習。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // String setsText = setsField.getText();
        // String repsText = repsField.getText();
        // String weightText = weightField.getText();

        Integer selectedSets = (Integer) setsComboBox.getSelectedItem();
        Integer selectedReps = (Integer) repsComboBox.getSelectedItem();
        Double selectedWeight = (Double) weightComboBox.getSelectedItem();

        // if (setsText.isEmpty() || repsText.isEmpty() || weightText.isEmpty()) {
        //     JOptionPane.showMessageDialog(frame, "請輸入組數、次數和重量。", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
        //     return;
        // }

        if (selectedSets == null || selectedReps == null || selectedWeight == null) {
            JOptionPane.showMessageDialog(frame, "請選擇組數、次數和重量。", "選擇錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // int sets = Integer.parseInt(setsText);
            // int reps = Integer.parseInt(repsText);
            // double weight = Double.parseDouble(weightText);
            int sets = selectedSets;
            int reps = selectedReps;
            double weight = selectedWeight;

            if (sets <= 0 || reps <= 0 || weight < 0) { // Weight can be 0
                JOptionPane.showMessageDialog(frame, "組數、次數必須大於0。", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }

            WorkoutExercise workoutExercise = new WorkoutExercise(selectedExercise, sets, reps, weight);
            selectedExercisesListModel.addElement(workoutExercise);

            // Clear the input fields after adding - not applicable for JComboBox in the same way
            // setsComboBox.setSelectedIndex(0); 
            // repsComboBox.setSelectedIndex(0);
            // weightComboBox.setSelectedIndex(0);

        } catch (NumberFormatException ex) {
            // This catch block might be less relevant now with JComboBoxes, 
            // but kept for safety, though direct parsing is removed.
            JOptionPane.showMessageDialog(frame, "組數、次數和重量必須是有效的數字。", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }    // Custom ListCellRenderer for Exercise objects
    private static class ExerciseListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Exercise) {
                setText(((Exercise) value).getName());
            }
            
            // Set proper Chinese font
            setFont(getStaticChineseFont(Font.PLAIN, 14));
            
            // Improved colors for better readability
            if (isSelected) {
                setBackground(new Color(230, 245, 255)); // Light blue background
                setForeground(new Color(0, 60, 120)); // Dark blue text
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 150, 255), 1),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
                setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            }
            
            setOpaque(true);
            return this;
        }
    }    // Custom ListCellRenderer for WorkoutExercise objects
    private static class WorkoutExerciseListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof WorkoutExercise) {
                WorkoutExercise we = (WorkoutExercise) value;
                setText(String.format("%s - %d組 x %d次 @ %.1fkg", 
                                      we.getExercise().getName(), 
                                      we.getSets(), 
                                      we.getReps(), 
                                      we.getWeight()));
            }
            
            // Set proper Chinese font
            setFont(getStaticChineseFont(Font.PLAIN, 14));
            
            // Improved colors for better readability
            if (isSelected) {
                setBackground(new Color(245, 250, 245)); // Light green background
                setForeground(new Color(0, 100, 0)); // Dark green text
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 200, 100), 1),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
                setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            }
            
            setOpaque(true);
            return this;
        }
<       public static void main(String[] args) {
        // Set system properties for better Chinese font rendering
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
          // Set Look and Feel to system default for better font support
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        cdb533a9fd40ec4b144b8d12ccd782ea62422375
        // DatabaseManager dbManager = new DatabaseManager();
        // try {
        //     dbManager.createTables(); // Ensure tables exist
        //     dbManager.populateDefaultExercisesIfNeeded(); // Populate if empty
        // } catch (SQLException e) {
        //     e.printStackTrace();
        //     JOptionPane.showMessageDialog(null, "資料庫初始化失敗: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        //     // Decide if the application should exit or continue without DB functionality
        // }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FitnessTrackerApp(); // Use new class name
            }
        });
    }
}