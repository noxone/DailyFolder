package com.hlag.gwrp.work;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

/**
 * Utility class generating the simple folder deletion dialog.
 *
 * @author neumaol
 *
 */
public final class DeleteFoldersDialogFx {
	/** Space between buttons */
	private static final int BUTTON_PANE_SPACING = 5;

	/** Space around buttons */
	private static final int BUTTON_PANE_PADDING = 15;

	/** Minimum window height */
	private static final int WINDOW_MIN_HEIGHT = 150;

	/** Minimum window width */
	private static final int WINDOW_MIN_WIDTH = 220;

	/** the maximum number of files to be shown in the tooltip of a folder */
	private static final int LIMIT_OF_FILES_IN_TOOLTIP = 15;

	/** private constructor to prevent instantiation */
	private DeleteFoldersDialogFx() {
		throw new RuntimeException();
	}

	/**
	 * Shows a dialog to the user asking to delete folders.
	 *
	 * @param stage          the stage to be used for the dialog
	 * @param folders        the folders to be presented to the user
	 * @param hostServices   an object to interact with the operating system
	 * @param folderConsumer an action to be performed for the selected folders
	 * @param alwaysOnTop    whether the window should always be on top of other
	 *                       windows
	 */
	static void showDialog(final Stage stage,
			final Collection<Path> folders,
			final HostServices hostServices,
			final Consumer<Collection<Path>> folderConsumer,
			final boolean alwaysOnTop) {
		final VBox pane = new VBox();
		pane.setPadding(new Insets(BUTTON_PANE_PADDING));
		pane.setMaxWidth(Double.MAX_VALUE);
		pane.setSpacing(BUTTON_PANE_SPACING);

		final Map<CheckBox, Path> checkboxes = new LinkedHashMap<>();
		for (final Path folder : folders) {
			final CheckBox box = new CheckBox(folder.getFileName().toString());
			box.setMaxWidth(Double.MAX_VALUE);
			box.setTooltip(new Tooltip(createTooltipText(folder)));
			pane.getChildren().add(box);
			checkboxes.put(box, folder);

			box.setOnMouseClicked(me -> {
				if (me.getClickCount() == 2) {
					hostServices.showDocument(folder.toAbsolutePath().toString());
				}
			});
		}

		final Button button = new Button("Delete");
		button.setDefaultButton(true);
		button.setMaxWidth(Double.MAX_VALUE);
		pane.getChildren().add(button);

		button.setOnAction(e -> {
			final List<Path> foldersToDelete = checkboxes//
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
		stage.setMinWidth(WINDOW_MIN_WIDTH);
		stage.setMinHeight(WINDOW_MIN_HEIGHT);
		stage.show();
	}

	/**
	 * Create tool tip text for the given folder
	 *
	 * @param folder the folder to create the tooltip text for
	 * @return the generated tooltip text
	 */
	private static String createTooltipText(final Path folder) {
		final List<Path> files;
		try (Stream<Path> paths = Files.list(folder)) {
			files = paths.limit(LIMIT_OF_FILES_IN_TOOLTIP + 1).collect(toList());
		} catch (final IOException ignore) {
			return "Unknown content";
		}

		if (files.isEmpty()) {
			return "empty";
		}

		final StringBuilder tooltipText = new StringBuilder();
		tooltipText.append("Content:\n");
		tooltipText.append(files.subList(0, Math.min(files.size(), LIMIT_OF_FILES_IN_TOOLTIP))
				.stream()
				.map(path -> (Files.isDirectory(path) ? "+" : "-") + " " + path.getFileName().toString())
				.collect(joining("\n")));
		if (files.size() > LIMIT_OF_FILES_IN_TOOLTIP) {
			tooltipText.append("\n... and even more");
		}
		return tooltipText.toString();
	}
}
