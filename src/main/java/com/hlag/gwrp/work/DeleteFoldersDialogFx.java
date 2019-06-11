package com.hlag.gwrp.work;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DeleteFoldersDialogFx {
	static Stream<File> showDialog(final Stage stage,
			final Collection<File> folders,
			final HostServices hostServices,
			final Consumer<Collection<File>> folderConsumer,
			final boolean alwaysOnTop) {
		final VBox pane = new VBox();
		pane.setPadding(new Insets(15));
		pane.setMaxWidth(Double.MAX_VALUE);
		pane.setSpacing(5);

		final Map<CheckBox, File> checkboxes = new LinkedHashMap<>();
		for (final File folder : folders) {
			final CheckBox box = new CheckBox(folder.getName());
			box.setMaxWidth(Double.MAX_VALUE);
			box.setTooltip(new Tooltip(getTooltipText(folder)));
			pane.getChildren().add(box);
			checkboxes.put(box, folder);

			box.setOnMouseClicked(me -> {
				if (me.getClickCount() == 2) {
					hostServices.showDocument(folder.getAbsolutePath());
				}
			});
		}

		final Button button = new Button("Delete");
		button.setDefaultButton(true);
		button.setMaxWidth(Double.MAX_VALUE);
		// pane.add(button, 0, index);
		pane.getChildren().add(button);

		button.setOnAction(e -> {
			final List<File> foldersToDelete = checkboxes//
					.entrySet()
					.stream()
					.filter(entry -> entry.getKey().isSelected())
					.map(entry -> entry.getValue())
					.collect(Collectors.toList());
			folderConsumer.accept(foldersToDelete);
			Platform.exit();
		});

		final Scene scene = new Scene(pane);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setAlwaysOnTop(alwaysOnTop);
		stage.setTitle("Delete folders");
		stage.setMinWidth(220);
		stage.setMinHeight(150);
		stage.show();

		return null;
	}

	private static String getTooltipText(final File folder) {
		final StringBuilder sb = new StringBuilder();
		final File[] files = folder.listFiles();
		if (files.length == 0) {
			sb.append("empty");
		} else if (files.length == 1) {
			sb.append(files.length).append(" item:\n");
		} else {
			sb.append(files.length).append(" items:\n");
		}

		int count = 0;
		for (final File file : files) {
			if (file.isDirectory()) {
				sb.append(" + ");
			} else {
				sb.append(" - ");
			}
			sb.append(file.getName()).append("\n");
			if (++count > 14) {
				break;
			}
		}
		if (count < files.length) {
			sb.append(" ... and ").append(files.length - count).append(" more");
		}
		return sb.toString();
	}
}
