package net.egork.chelper.actions;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.egork.chelper.Utilities;
import net.egork.chelper.configurations.TaskConfiguration;
import net.egork.chelper.configurations.TopCoderConfiguration;
import net.egork.chelper.task.Task;
import net.egork.chelper.task.TopCoderTask;

import javax.swing.JOptionPane;
import java.io.IOException;

/**
 * @author Egor Kulikov (kulikov@devexperts.com)
 */
public class DeleteTaskAction extends AnAction {
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
		int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete current configuration?");
		if (result != JOptionPane.OK_OPTION)
			return;
		if (configuration instanceof TaskConfiguration) {
			String archiveDir = Utilities.getData(project).archive;
			final VirtualFile directory = Utilities.getFile(project, archiveDir);
			if (directory == null)
				return;
			final Task task = ((TaskConfiguration) configuration).getConfiguration();
			ApplicationManager.getApplication().runWriteAction(new Runnable() {
				public void run() {
					try {
						VirtualFile mainFile = Utilities.getFile(project, task.location + "/" + task.name + ".java");
						if (mainFile == null)
							return;
						mainFile.delete(this);
						VirtualFile checkerFile = Utilities.getFile(project, task.location + "/" + task.name + "Checker.java");
						if (checkerFile == null)
							return;
						checkerFile.delete(this);
						manager.removeConfiguration(manager.getSelectedConfiguration());
						RunConfiguration[] allConfigurations = manager.getAllConfigurations();
						if (allConfigurations.length != 0) {
							manager.setActiveConfiguration(new RunnerAndConfigurationSettingsImpl(manager, allConfigurations[0],
								false));
						}
					} catch (IOException ignored) {
					}
				}
			});
		}
		if (configuration instanceof TopCoderConfiguration) {
			String archiveDir = Utilities.getData(project).archive;
			final VirtualFile directory = Utilities.getFile(project, archiveDir);
			if (directory == null)
				return;
			final TopCoderTask task = ((TopCoderConfiguration) configuration).getConfiguration();
			ApplicationManager.getApplication().runWriteAction(new Runnable() {
				public void run() {
					try {
						VirtualFile mainFile = Utilities.getFile(project, Utilities.getData(project).defaultDir
							+ "/" + task.name + ".java");
						if (mainFile == null)
							return;
						mainFile.delete(this);
						VirtualFile topcoderFile = Utilities.getFile(project, Utilities.getData(project).topcoderDir
							+ "/" + task.name + ".java");
						if (topcoderFile != null)
							topcoderFile.delete(this);
						manager.removeConfiguration(manager.getSelectedConfiguration());
						RunConfiguration[] allConfigurations = manager.getAllConfigurations();
						if (allConfigurations.length != 0) {
							manager.setActiveConfiguration(new RunnerAndConfigurationSettingsImpl(manager, allConfigurations[0],
								false));
						}
					} catch (IOException ignored) {
					}
				}
			});
		}
	}
}
