package nl.jixxed.eliteodysseymaterials;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import nl.jixxed.eliteodysseymaterials.constants.AppConstants;
import nl.jixxed.eliteodysseymaterials.constants.OsConstants;
import nl.jixxed.eliteodysseymaterials.constants.PreferenceConstants;
import nl.jixxed.eliteodysseymaterials.domain.ApplicationState;
import nl.jixxed.eliteodysseymaterials.enums.FontSize;
import nl.jixxed.eliteodysseymaterials.enums.StoragePool;
import nl.jixxed.eliteodysseymaterials.helper.FileHelper;
import nl.jixxed.eliteodysseymaterials.parser.FileProcessor;
import nl.jixxed.eliteodysseymaterials.service.LocaleService;
import nl.jixxed.eliteodysseymaterials.service.MaterialTrackingService;
import nl.jixxed.eliteodysseymaterials.service.PreferencesService;
import nl.jixxed.eliteodysseymaterials.service.event.*;
import nl.jixxed.eliteodysseymaterials.templates.ApplicationLayout;
import nl.jixxed.eliteodysseymaterials.templates.StartDialog;
import nl.jixxed.eliteodysseymaterials.trade.MarketPlaceClient;
import nl.jixxed.eliteodysseymaterials.watchdog.JournalWatcher;
import nl.jixxed.eliteodysseymaterials.watchdog.TimeStampedGameStateWatcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
public class Main extends Application {

    public static final ApplicationState APPLICATION_STATE = ApplicationState.getInstance();

    private ApplicationLayout applicationLayout;
    private TimeStampedGameStateWatcher timeStampedShipLockerWatcher;
    private TimeStampedGameStateWatcher timeStampedBackPackWatcher;
    private final JournalWatcher journalWatcher = new JournalWatcher();
    private Stage primaryStage;

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    @Override
    public void start(final Stage primaryStage) {
        try {
            PreferencesService.setPreference(PreferenceConstants.APP_SETTINGS_VERSION, System.getProperty("app.version"));
            final boolean whatsNewSeen = PreferencesService.getPreference(PreferenceConstants.WHATS_NEW_VERSION, "").equals(PreferencesService.getPreference(PreferenceConstants.APP_SETTINGS_VERSION, "0"));
            if (!whatsNewSeen || !PreferencesService.getPreference(PreferenceConstants.POLICY_ACCEPT_VERSION, "").equals(StartDialog.POLICY_LEVEL_REQUIRED)) {
                final Stage policyStage = new Stage();

                final Scene policyScene = new Scene(new StartDialog(policyStage), 640, 480);
                policyStage.initModality(Modality.APPLICATION_MODAL);
                final JMetro jMetro = new JMetro(Style.DARK);
                jMetro.setScene(policyScene);
                policyScene.getStylesheets().add(getClass().getResource("/nl/jixxed/eliteodysseymaterials/style/style.css").toExternalForm());
                policyStage.setScene(policyScene);
                policyStage.titleProperty().set("What's new & privacy policy");
                policyStage.showAndWait();
                if (!PreferencesService.getPreference(PreferenceConstants.POLICY_ACCEPT_VERSION, "").equals(StartDialog.POLICY_LEVEL_REQUIRED)) {
                    System.exit(0);
                } else {
                    PreferencesService.setPreference(PreferenceConstants.POLICY_ACCEPT_VERSION, StartDialog.POLICY_LEVEL_REQUIRED);
                }
            }
            MaterialTrackingService.initialize();
            this.applicationLayout = new ApplicationLayout(this);
            this.primaryStage = primaryStage;
            primaryStage.setTitle(AppConstants.APP_TITLE);
            primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream(AppConstants.APP_ICON_PATH)));
            final File watchedFolder = new File(PreferencesService.getPreference(PreferenceConstants.JOURNAL_FOLDER, OsConstants.DEFAULT_WATCHED_FOLDER));

            this.timeStampedShipLockerWatcher = new TimeStampedGameStateWatcher(watchedFolder, FileProcessor::processShipLockerBackpack, AppConstants.SHIPLOCKER_FILE, StoragePool.SHIPLOCKER);
            this.timeStampedBackPackWatcher = new TimeStampedGameStateWatcher(watchedFolder, FileProcessor::processShipLockerBackpack, AppConstants.BACKPACK_FILE, StoragePool.BACKPACK);

            this.journalWatcher.watch(watchedFolder, FileProcessor::processJournal, FileProcessor::resetAndProcessJournal);

            EventService.addListener(this, WatchedFolderChangedEvent.class, event -> reset(new File(event.getPath())));
            EventService.addListener(this, CommanderSelectedEvent.class, event -> reset(this.journalWatcher.getWatchedFolder()));


            final Scene scene = new Scene(this.applicationLayout, PreferencesService.getPreference(PreferenceConstants.APP_WIDTH, 800D), PreferencesService.getPreference(PreferenceConstants.APP_HEIGHT, 600D));

            scene.widthProperty().addListener((observable, oldValue, newValue) -> setPreferenceIfNotMaximized(primaryStage, PreferenceConstants.APP_WIDTH, Math.max((Double) newValue, 175.0D)));
            scene.heightProperty().addListener((observable, oldValue, newValue) -> setPreferenceIfNotMaximized(primaryStage, PreferenceConstants.APP_HEIGHT, Math.max((Double) newValue, 175.0D)));

            primaryStage.xProperty().addListener((observable, oldValue, newValue) -> setPreferenceIfNotMaximized(primaryStage, PreferenceConstants.APP_X, Math.max((Double) newValue, -8.0D)));
            primaryStage.yProperty().addListener((observable, oldValue, newValue) -> setPreferenceIfNotMaximized(primaryStage, PreferenceConstants.APP_Y, Math.max((Double) newValue, -8.0D)));
            primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> PreferencesService.setPreference(PreferenceConstants.APP_MAXIMIZED, newValue));

            primaryStage.setX(PreferencesService.getPreference(PreferenceConstants.APP_X, 0D));
            primaryStage.setY(PreferencesService.getPreference(PreferenceConstants.APP_Y, 0D));
            primaryStage.setMaximized(PreferencesService.getPreference(PreferenceConstants.APP_MAXIMIZED, Boolean.FALSE));
            final JMetro jMetro = new JMetro(Style.DARK);
            jMetro.setScene(scene);
            scene.getStylesheets().add(getClass().getResource("/nl/jixxed/eliteodysseymaterials/style/style.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/notificationpopup.css").toExternalForm());
            final File customCss = new File(OsConstants.CUSTOM_CSS);
            if (OsConstants.OLD_CUSTOM_CSS != null) {
                final File oldCustomCss = new File(OsConstants.OLD_CUSTOM_CSS);
                if (!customCss.exists() && oldCustomCss.exists()) {
                    customCss.createNewFile();
                    FileHelper.copyFileContents(oldCustomCss, customCss);
                }
            }
            if (customCss.exists()) {
                importCustomCss(scene, customCss);
            }
            EventService.addListener(this, FontSizeEvent.class, fontSizeEvent -> {
                this.applicationLayout.styleProperty().set("-fx-font-size: " + fontSizeEvent.getFontSize() + "px");
                EventService.publish(new AfterFontSizeSetEvent(fontSizeEvent.getFontSize()));
            });
            this.applicationLayout.styleProperty().set("-fx-font-size: " + FontSize.valueOf(PreferencesService.getPreference(PreferenceConstants.TEXTSIZE, "NORMAL")).getSize() + "px");
            EventService.addListener(this, SaveWishlistEvent.class, event -> {
                final FileChooser fileChooser = new FileChooser();

                //Set extension filter for text files
                final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
                fileChooser.getExtensionFilters().add(extFilter);

                //Show save file dialog
                final File file = fileChooser.showSaveDialog(primaryStage);

                if (file != null) {
                    saveTextToFile(event.getText(), file);
                }
            });
            primaryStage.setScene(scene);
            primaryStage.show();
            EventService.publish(new ApplicationLifeCycleEvent());
            primaryStage.setOnCloseRequest(event -> {
                if (MarketPlaceClient.getInstance() != null) {
                    MarketPlaceClient.getInstance().close();
                }
                MaterialTrackingService.close();
                this.timeStampedShipLockerWatcher.stop();
                this.timeStampedBackPackWatcher.stop();
                this.journalWatcher.stop();
            });
        } catch (final Exception ex) {
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(800, 800);
            Platform.runLater(() -> alert.setResizable(false));
            alert.setTitle("Application Error");
            alert.setHeaderText("Please contact the developer with the following information");
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);
            ex.printStackTrace(printWriter);
            alert.setContentText(stringWriter.toString());
            alert.showAndWait();
        }
    }

    private void importCustomCss(final Scene scene, final File customCss) {
        try {
            scene.getStylesheets().add(customCss.toURI().toURL().toExternalForm());
        } catch (final IOException e) {
            log.error("Error loading stylesheet", e);
        }
    }

    private void reset(final File watchedFolder) {
        APPLICATION_STATE.resetEngineerStates();
        APPLICATION_STATE.resetShipLockerCounts();
        APPLICATION_STATE.resetBackPackCounts();
        APPLICATION_STATE.resetCommanders();
        EventService.publish(new CommanderResetEvent());
        this.timeStampedShipLockerWatcher.stop();
        this.timeStampedBackPackWatcher.stop();
        this.journalWatcher.stop();
        this.timeStampedShipLockerWatcher = new TimeStampedGameStateWatcher(watchedFolder, FileProcessor::processShipLockerBackpack, AppConstants.SHIPLOCKER_FILE, StoragePool.SHIPLOCKER);
        this.timeStampedBackPackWatcher = new TimeStampedGameStateWatcher(watchedFolder, FileProcessor::processShipLockerBackpack, AppConstants.BACKPACK_FILE, StoragePool.BACKPACK);

        this.journalWatcher.watch(watchedFolder, FileProcessor::processJournal, FileProcessor::resetAndProcessJournal);
    }

    private void setPreferenceIfNotMaximized(final Stage primaryStage, final String setting, final Double value) {
        // x y are processed before maximized, so excluding setting it if it's -8
        if (!primaryStage.isMaximized() && !Double.valueOf(-8.0D).equals(value)) {
            PreferencesService.setPreference(setting, value);
        }
    }

    private void saveTextToFile(final String content, final File file) {
        try {
            final PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (final IOException ex) {
            log.error("Failed to write to file");
        }
    }

    public static void main(final String[] args) {
        LocaleService.setCurrentLocale(LocaleService.getCurrentLocale());
        launch(args);

    }
}
