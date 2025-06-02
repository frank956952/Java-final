import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.time.DayOfWeek; // Added for WeeklyPlan
import java.util.Map; // Added for WeeklyPlan
import java.util.ArrayList; // Added for WeeklyPlan display


public class FitnessTrackerApp { // Renamed from main to FitnessTrackerApp

    private JFrame frame;
    private JComboBox<BodyPart> bodyPartComboBox;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JList<Exercise> availableExercisesList;
    private DefaultListModel<Exercise> availableExercisesListModel;
    private JList<WorkoutExercise> selectedExercisesList;
    private DefaultListModel<WorkoutExercise> selectedExercisesListModel;
    // private JTextField setsField;
    // private JTextField repsField;
    // private JTextField weightField;
    private JComboBox<Integer> setsComboBox;
    private JComboBox<Integer> repsComboBox;
    private JComboBox<Double> weightComboBox;

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
    }

    private void createAndShowGUI() {
        frame = new JFrame("健身追蹤器");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Panel for recording workouts
        JPanel recordWorkoutPanel = createRecordWorkoutPanel();
        tabbedPane.addTab("記錄健身課程", recordWorkoutPanel);

        // Panel for weekly plan
        JPanel weeklyPlanPanel = createWeeklyPlanPanel(); // Call new method
        tabbedPane.addTab("每週計劃", weeklyPlanPanel);

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel createRecordWorkoutPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Panel: Body part, Start time, End time
        // Changed from BoxLayout with nested JPanels to a single FlowLayout for better component flow
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); // hgap=10, vgap=5

        topPanel.add(new JLabel("選擇部位:"));
        bodyPartComboBox = new JComboBox<>(BodyPart.values());
        bodyPartComboBox.setSelectedItem(BodyPart.CHEST); // Default selection
        // Add ActionListener to bodyPartComboBox
        bodyPartComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAvailableExercises();
            }
        });
        topPanel.add(bodyPartComboBox);

        topPanel.add(new JLabel("開始時間 (YYYY-MM-DD HH:MM):"));
        startTimeField = new JTextField("2025-05-20 09:48", 16); // Adjusted column width for date-time
        topPanel.add(startTimeField);

        topPanel.add(new JLabel("結束時間 (YYYY-MM-DD HH:MM):"));
        endTimeField = new JTextField("2025-05-20 10:48", 16); // Adjusted column width for date-time
        topPanel.add(endTimeField);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center Panel: Available Exercises, Add/Remove Buttons, Selected Exercises
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Available Exercises
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.add(new JLabel("可用練習:", SwingConstants.CENTER), BorderLayout.NORTH);
        availableExercisesListModel = new DefaultListModel<>();
        availableExercisesList = new JList<>(availableExercisesListModel);
        availableExercisesList.setCellRenderer(new ExerciseListCellRenderer()); // Set custom renderer
        availablePanel.add(new JScrollPane(availableExercisesList), BorderLayout.CENTER);
        centerPanel.add(availablePanel, gbc);

        // Add/Remove Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JButton addExerciseButton = new JButton("添加練習 >>");
        JButton removeExerciseButton = new JButton("<< 移除練習");

        addExerciseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSelectedExercise();
            }
        });
        
        Dimension buttonSize = new Dimension(120, 30);
        addExerciseButton.setPreferredSize(buttonSize);
        addExerciseButton.setMinimumSize(buttonSize);
        addExerciseButton.setMaximumSize(buttonSize);
        removeExerciseButton.setPreferredSize(buttonSize);
        removeExerciseButton.setMinimumSize(buttonSize);
        removeExerciseButton.setMaximumSize(buttonSize);

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(addExerciseButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(removeExerciseButton);
        buttonPanel.add(Box.createVerticalGlue());

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        centerPanel.add(buttonPanel, gbc);

        // Selected Exercises
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel selectedPanel = new JPanel(new BorderLayout());
        selectedPanel.add(new JLabel("已選練習:", SwingConstants.CENTER), BorderLayout.NORTH);
        selectedExercisesListModel = new DefaultListModel<>();
        selectedExercisesList = new JList<>(selectedExercisesListModel);
        selectedExercisesList.setCellRenderer(new WorkoutExerciseListCellRenderer()); // Set custom renderer
        selectedPanel.add(new JScrollPane(selectedExercisesList), BorderLayout.CENTER);
        centerPanel.add(selectedPanel, gbc);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom Panel: Sets, Reps, Weight, Record Button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JPanel exerciseDetailsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exerciseDetailsPanel.add(new JLabel("組數:"));
        // setsField = new JTextField(5);
        // exerciseDetailsPanel.add(setsField);
        Integer[] setValues = new Integer[10];
        for (int i = 0; i < 10; i++) setValues[i] = i + 1;
        setsComboBox = new JComboBox<>(setValues);
        exerciseDetailsPanel.add(setsComboBox);

        exerciseDetailsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        exerciseDetailsPanel.add(new JLabel("次數:"));
        // repsField = new JTextField(5);
        // exerciseDetailsPanel.add(repsField);
        Integer[] repValues = new Integer[20];
        for (int i = 0; i < 20; i++) repValues[i] = i + 1;
        repsComboBox = new JComboBox<>(repValues);
        exerciseDetailsPanel.add(repsComboBox);

        exerciseDetailsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        exerciseDetailsPanel.add(new JLabel("重量 (kg):"));
        // weightField = new JTextField(5);
        // exerciseDetailsPanel.add(weightField);
        Double[] weightValues = new Double[41]; // 0.0 to 100.0 in 2.5 increments
        for (int i = 0; i <= 40; i++) weightValues[i] = i * 2.5;
        weightComboBox = new JComboBox<>(weightValues);
        exerciseDetailsPanel.add(weightComboBox);

        bottomPanel.add(exerciseDetailsPanel);

        JPanel recordButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton recordSessionButton = new JButton("記錄健身課程");
        recordButtonPanel.add(recordSessionButton);
        bottomPanel.add(recordButtonPanel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Initial population of available exercises
        updateAvailableExercises();

        return mainPanel;
    }

    private JPanel createWeeklyPlanPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel for day selection
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topControlPanel.add(new JLabel("選擇星期:"));
        dayOfWeekComboBox = new JComboBox<>(DayOfWeek.values());
        topControlPanel.add(dayOfWeekComboBox);
        dayOfWeekComboBox.addActionListener(e -> updateDailyPlanView());
        mainPanel.add(topControlPanel, BorderLayout.NORTH);

        // Center panel with two sides: exercise selection and plan view
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Left side: Exercise selection for the plan
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("添加運動到計劃"));

        JPanel planExerciseSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        planExerciseSelectionPanel.add(new JLabel("身體部位:"));
        planBodyPartComboBox = new JComboBox<>(BodyPart.values());
        planExerciseSelectionPanel.add(planBodyPartComboBox);
        planBodyPartComboBox.addActionListener(e -> updatePlanAvailableExercises());

        leftPanel.add(planExerciseSelectionPanel, BorderLayout.NORTH);

        planAvailableExercisesListModel = new DefaultListModel<>();
        planAvailableExercisesList = new JList<>(planAvailableExercisesListModel);
        planAvailableExercisesList.setCellRenderer(new ExerciseListCellRenderer());
        leftPanel.add(new JScrollPane(planAvailableExercisesList), BorderLayout.CENTER);

        JButton addExerciseToPlanButton = new JButton("添加到計劃");
        addExerciseToPlanButton.addActionListener(e -> addExerciseToWeeklyPlan());
        leftPanel.add(addExerciseToPlanButton, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(leftPanel);

        // Right side: Display of the plan for the selected day
        JPanel rightPanel = new JPanel(new BorderLayout(5,5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("當日計劃"));

        dailyPlanListModel = new DefaultListModel<>();
        dailyPlanList = new JList<>(dailyPlanListModel);
        rightPanel.add(new JScrollPane(dailyPlanList), BorderLayout.CENTER);

        JButton removeExerciseFromPlanButton = new JButton("從計劃中移除選定項");
        removeExerciseFromPlanButton.addActionListener(e -> removeExerciseFromWeeklyPlan());
        rightPanel.add(removeExerciseFromPlanButton, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);

        // Initial population
        updatePlanAvailableExercises();
        updateDailyPlanView();

        return mainPanel;
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
    }

    // Custom ListCellRenderer for Exercise objects
    private static class ExerciseListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Exercise) {
                setText(((Exercise) value).getName());
            }
            return this;
        }
    }

    // Custom ListCellRenderer for WorkoutExercise objects
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
            return this;
        }
    }

    public static void main(String[] args) {
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