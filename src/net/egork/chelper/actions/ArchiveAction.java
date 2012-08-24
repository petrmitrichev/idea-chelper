package net.egork.chelper.actions;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import net.egork.chelper.configurations.TaskConfiguration;
import net.egork.chelper.util.CodeGenerationUtilities;
import net.egork.chelper.util.FileUtilities;
import net.egork.chelper.util.Utilities;
import net.egork.chelper.configurations.TopCoderConfiguration;
import net.egork.chelper.task.Task;
import net.egork.chelper.task.TopCoderTask;

import java.io.IOException;

/**
 * @author Egor Kulikov (kulikov@devexperts.com)
 */
public class ArchiveAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		if (!Utilities.isEligible(e.getDataContext()))
			return;
		final Project project = Utilities.getProject(e.getDataContext());
		final RunManagerImpl manager = RunManagerImpl.getInstanceImpl(project);
		RunnerAndConfigurationSettings selectedConfiguration =
			manager.getSelectedConfiguration();
		if (selectedConfiguration == null)
			return;
		RunConfiguration configuration = selectedConfiguration.getConfiguration();
		if (configuration instanceof TaskConfiguration) {
            final Task task = ((TaskConfiguration) configuration).getConfiguration();
            String archiveDir = Utilities.getData(project).archiveDirectory;
            String dateAndContest = getDateAndContest(task);
			final VirtualFile directory = FileUtilities.createDirectoryIfMissing(project, archiveDir + "/" + dateAndContest);
			if (directory == null)
				return;
			CodeGenerationUtilities.createUnitTest(task, project);
			ApplicationManager.getApplication().runWriteAction(new Runnable() {
				public void run() {
					try {
                        PsiElement main = JavaPsiFacade.getInstance(project).findClass(task.taskClass);
                        VirtualFile mainFile = main == null ? null : main.getContainingFile() == null ? null : main.getContainingFile().getVirtualFile();
						if (mainFile != null) {
    						VfsUtil.copyFile(this, mainFile, directory);
	    					mainFile.delete(this);
                        }
                        PsiElement checker = JavaPsiFacade.getInstance(project).findClass(task.checkerClass);
						VirtualFile checkerFile = checker == null ? null : checker.getContainingFile() == null ? null : checker.getContainingFile().getVirtualFile();
						if (checkerFile != null && mainFile != null && checkerFile.getParent().equals(mainFile.getParent())) {
                            VfsUtil.copyFile(this, checkerFile, directory);
                            checkerFile.delete(this);
                        }
                        for (String testClass : task.testClasses) {
                            PsiElement test = JavaPsiFacade.getInstance(project).findClass(testClass);
                            VirtualFile testFile = test == null ? null : test.getContainingFile() == null ? null : test.getContainingFile().getVirtualFile();
                            if (testFile != null) {
                                VfsUtil.copyFile(this, testFile, directory);
                                testFile.delete(this);
                            }
                        }
                        VirtualFile taskFile = FileUtilities.getFile(project, task.location + "/" + task.name + ".task");
                        if (taskFile != null) {
                            VfsUtil.copyFile(this, taskFile, directory);
                            taskFile.delete(this);
                        }
						manager.removeConfiguration(manager.getSelectedConfiguration());
						setOtherConfiguration(manager);
					} catch (IOException e) {
                        throw new RuntimeException(e);
					}
				}
			});
		}
		if (configuration instanceof TopCoderConfiguration) {
			String archiveDir = Utilities.getData(project).archiveDirectory;
			final VirtualFile directory = FileUtilities.createDirectoryIfMissing(project, archiveDir);
			if (directory == null)
				return;
			final TopCoderTask task = ((TopCoderConfiguration) configuration).getConfiguration();
			CodeGenerationUtilities.createUnitTest(task);
			ApplicationManager.getApplication().runWriteAction(new Runnable() {
				public void run() {
					try {
						VirtualFile mainFile = FileUtilities.getFile(project, Utilities.getData(project).defaultDirectory
							+ "/" + task.name + ".java");
						if (mainFile == null)
							return;
						VfsUtil.copyFile(this, mainFile, directory);
						mainFile.delete(this);
						VirtualFile topcoderFile = FileUtilities.getFile(project, Utilities.getData(project).outputDirectory
							+ "/" + task.name + ".java");
						if (topcoderFile != null)
							topcoderFile.delete(this);
						manager.removeConfiguration(manager.getSelectedConfiguration());
						setOtherConfiguration(manager);
					} catch (IOException ignored) {
					}
				}
			});
		}
	}

    private String getDateAndContest(Task task) {
        String yearAndMonth = task.date;
        int position = yearAndMonth.indexOf('.');
        if (position != -1)
            position = yearAndMonth.indexOf('.', position + 1);
        if (position != -1)
            yearAndMonth = yearAndMonth.substring(0, position);
        return canonize(yearAndMonth) + "/" + canonize(task.date + " - " + (task.contestName.length() == 0 ? "unsorted" : task.contestName));
    }

    private String canonize(String filename) {
        return filename.replaceAll("[/\\\\?%*:|\"<>]", "-");
    }

    public static void setOtherConfiguration(RunManagerImpl manager) {
		RunConfiguration[] allConfigurations = manager.getAllConfigurations();
		for (RunConfiguration configuration : allConfigurations) {
			if (configuration instanceof TaskConfiguration || configuration instanceof TopCoderConfiguration) {
				manager.setActiveConfiguration(new RunnerAndConfigurationSettingsImpl(manager, configuration, false));
				return;
			}
		}
	}
}
