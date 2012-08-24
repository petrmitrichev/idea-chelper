package net.egork.chelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import net.egork.chelper.task.StreamConfiguration;
import net.egork.chelper.task.Task;
import net.egork.chelper.task.TestType;
import net.egork.chelper.util.FileCreator;
import net.egork.chelper.util.FileUtilities;
import net.egork.chelper.util.Provider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Egor Kulikov (egorku@yandex-team.ru)
 */
public class TaskConfigurationPanel extends JPanel {
    private JPanel basic;
    private JPanel advanced;
    private Task task;
    private Project project;

    //basic
    private JTextField name;
    private JComboBox testType;
    private JComboBox inputType;
    private JTextField inputFileName;
    private JComboBox outputType;
    private JTextField outputFileName;
    private JButton tests;

    //advanced
    private DirectorySelector location;
    private JTextField vmArgs;
    private JTextField mainClass;
    private SelectOrCreateClass taskClass;
    private SelectOrCreateClass checkerClass;
    private JTextField checkerParameters;
    private JButton testClasses;
    private JTextField date;
    private JTextField contestName;
    private JCheckBox truncate;

    public TaskConfigurationPanel(final Task task, boolean firstEdit, final Project project, final SizeChangeListener listener, JPanel buttonPanel) {
        super(new BorderLayout(5, 5));
        this.task = task;
        this.project = project;
        basic = new JPanel(new VerticalFlowLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width = 250;
                return size;
            }
        };
        basic.add(new JLabel("Name:"));
        name = new JTextField(task.name);
        name.setEnabled(firstEdit);
        basic.add(name);
        basic.add(new JLabel("Test type:"));
        testType = new JComboBox(TestType.values());
        testType.setSelectedItem(task.testType);
        basic.add(testType);
        basic.add(new JLabel("Input:"));
        inputType = new JComboBox(StreamConfiguration.StreamType.values());
        inputType.setSelectedItem(task.input.type);
        inputType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                inputFileName.setVisible(inputType.getSelectedItem() == StreamConfiguration.StreamType.CUSTOM);
                if (listener != null)
                    listener.onSizeChanged();
            }
        });
        basic.add(inputType);
        inputFileName = new JTextField(task.input.type == StreamConfiguration.StreamType.CUSTOM ? task.input.fileName :
                "input.txt");
        inputFileName.setVisible(task.input.type == StreamConfiguration.StreamType.CUSTOM);
        basic.add(inputFileName);
        basic.add(new JLabel("Output:"));
        outputType = new JComboBox(StreamConfiguration.StreamType.values());
        outputType.setSelectedItem(task.output.type);
        outputType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outputFileName.setVisible(outputType.getSelectedItem() == StreamConfiguration.StreamType.CUSTOM);
                if (listener != null)
                    listener.onSizeChanged();
            }
        });
        basic.add(outputType);
        outputFileName = new JTextField(task.output.type == StreamConfiguration.StreamType.CUSTOM ?
                task.output.fileName : "output.txt");
        outputFileName.setVisible(task.output.type == StreamConfiguration.StreamType.CUSTOM);
        basic.add(outputFileName);
        tests = new JButton("Edit tests");
        tests.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaskConfigurationPanel.this.task = TaskConfigurationPanel.this.task.setTests(
                        EditTestsDialog.editTests(TaskConfigurationPanel.this.task.tests, TaskConfigurationPanel.this.project));
                name.setText(name.getText());
            }
        });
        basic.add(tests);
        if (buttonPanel != null)
            basic.add(buttonPanel);
        JPanel leftAdvanced = new JPanel(new VerticalFlowLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width = 250;
                return size;
            }
        };
        leftAdvanced.add(new JLabel("Location:"));
        location = new DirectorySelector(project, task.location);
        location.setEnabled(firstEdit);
        leftAdvanced.add(location);
        leftAdvanced.add(new JLabel("Main class name:"));
        mainClass = new JTextField(task.mainClass);
        leftAdvanced.add(mainClass);
        leftAdvanced.add(new JLabel("Task class:"));
        Provider<String> locationProvider = new Provider<String>() {
            public String provide() {
                return location.getText();
            }
        };
        taskClass = new SelectOrCreateClass(task.taskClass, project, locationProvider, new FileCreator() {
            public String createFile(Project project, String path, String name) {
                return FileUtilities.createTaskClass(project, path, name);
            }

            public boolean isValid(String name) {
                return FileUtilities.isValidClassName(name);
            }
        });
        leftAdvanced.add(taskClass);
        leftAdvanced.add(new JLabel("Checker class:"));
        checkerClass = new SelectOrCreateClass(task.checkerClass, project, locationProvider, new FileCreator() {
            public String createFile(Project project, String path, String name) {
                return FileUtilities.createCheckerClass(project, path, name);
            }

            public boolean isValid(String name) {
                return FileUtilities.isValidClassName(name);
            }
        });
        leftAdvanced.add(checkerClass);
        leftAdvanced.add(new JLabel("Checker parameters:"));
        checkerParameters = new JTextField(task.checkerParameters);
        leftAdvanced.add(checkerParameters);
        JPanel rightAdvanced = new JPanel(new VerticalFlowLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width = 250;
                return size;
            }
        };
        rightAdvanced.add(new JLabel("VM arguments:"));
        vmArgs = new JTextField(task.vmArgs);
        rightAdvanced.add(vmArgs);
        testClasses = new JButton("Test classes");
        testClasses.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaskConfigurationPanel.this.task = TaskConfigurationPanel.this.task.setTestClasses(
                        TestClassesDialog.showDialog(TaskConfigurationPanel.this.task.testClasses, project,
                        TaskConfigurationPanel.this.task));
                name.setText(name.getText());
            }
        });
        rightAdvanced.add(testClasses);
        rightAdvanced.add(new JLabel("Date:"));
        date = new JTextField(task.date);
        rightAdvanced.add(date);
        rightAdvanced.add(new JLabel("Contest name:"));
        contestName = new JTextField(task.contestName);
        rightAdvanced.add(contestName);
        truncate = new JCheckBox("Truncate long tests", task.truncate);
        rightAdvanced.add(truncate);
        advanced = new JPanel(new GridLayout(1, 2, 5, 5));
        advanced.add(leftAdvanced);
        advanced.add(rightAdvanced);
        JPanel advancedWrapper = new JPanel(new BorderLayout());
        advancedWrapper.add(advanced, BorderLayout.WEST);
        add(basic, BorderLayout.WEST);
        add(advancedWrapper, BorderLayout.CENTER);
    }

    public void setAdvancedVisibility(boolean visibility) {
        advanced.setVisible(visibility);
    }

    public boolean isAdvancedVisible() {
        return advanced.isVisible();
    }

    public Task getTask() {
        return task = new Task(name.getText(), (TestType)testType.getSelectedItem(),
                new StreamConfiguration((StreamConfiguration.StreamType) inputType.getSelectedItem(), inputFileName.getText()),
                new StreamConfiguration((StreamConfiguration.StreamType) outputType.getSelectedItem(), outputFileName.getText()),
                task.tests, location.getText(), vmArgs.getText(), mainClass.getText(),
                taskClass.getText(), checkerClass.getText(), checkerParameters.getText(), task.testClasses,
                date.getText(), contestName.getText(), truncate.isSelected(), task.inputClass, task.outputClass);
    }

    public interface SizeChangeListener {
        public void onSizeChanged();
    }

    public JTextField getNameField() {
        return name;
    }
}
