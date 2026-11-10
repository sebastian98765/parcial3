package com.smarthome.ui;

import com.smarthome.facade.SmartHomeFacade;
import com.smarthome.model.Controllable;
import com.smarthome.model.Device;
import com.smarthome.model.DeviceType;
import com.smarthome.model.DoorLock;
import com.smarthome.model.Light;
import com.smarthome.model.Room;
import com.smarthome.model.Thermostat;
import com.smarthome.strategy.AdaptiveLightingStrategy;
import com.smarthome.strategy.ClimateStrategy;
import com.smarthome.strategy.ComfortClimateStrategy;
import com.smarthome.strategy.EcoClimateStrategy;
import com.smarthome.strategy.FullLightingStrategy;
import com.smarthome.strategy.LightingStrategy;
import com.smarthome.strategy.NightClimateStrategy;
import com.smarthome.strategy.RelaxLightingStrategy;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.util.function.Function;

public class MainController {

    @FXML private Button undoButton;
    @FXML private Button redoButton;
    @FXML private ChoiceBox<SmartHomeFacade.Scene> sceneBox;
    @FXML private CheckBox automationCheck;
    @FXML private Spinner<Double> outsideSpinner;
    @FXML private Label environmentLabel;

    @FXML private TreeView<Controllable> homeTree;
    @FXML private ChoiceBox<DeviceType> newTypeBox;
    @FXML private ChoiceBox<Room> newRoomBox;
    @FXML private TextField newNameField;
    @FXML private TextField newRoomField;

    @FXML private Label titleLabel;
    @FXML private Label stateLabel;
    @FXML private Label detailLabel;
    @FXML private Button powerButton;
    @FXML private Button faultButton;
    @FXML private Button suggestButton;
    @FXML private VBox lightControls;
    @FXML private Slider brightnessSlider;
    @FXML private Label brightnessValue;
    @FXML private ChoiceBox<LightingStrategy> lightingStrategyBox;
    @FXML private VBox thermostatControls;
    @FXML private Slider temperatureSlider;
    @FXML private Label temperatureValue;
    @FXML private ChoiceBox<ClimateStrategy> climateStrategyBox;

    @FXML private ListView<String> logList;

    private final SmartHomeFacade facade = new SmartHomeFacade();
    private int structureSignature = -1;
    private boolean updating;

    @FXML
    private void initialize() {
        sceneBox.getItems().setAll(SmartHomeFacade.Scene.values());
        sceneBox.getSelectionModel().selectFirst();

        newTypeBox.getItems().setAll(DeviceType.values());
        newTypeBox.getSelectionModel().selectFirst();

        lightingStrategyBox.getItems().setAll(new AdaptiveLightingStrategy(),
                new FullLightingStrategy(), new RelaxLightingStrategy());
        lightingStrategyBox.setConverter(new StringConverter<LightingStrategy>() {
            @Override
            public String toString(LightingStrategy strategy) {
                return strategy == null ? "" : strategy.name();
            }

            @Override
            public LightingStrategy fromString(String text) {
                return null;
            }
        });
        lightingStrategyBox.getSelectionModel().selectedItemProperty().addListener((o, old, chosen) -> {
            if (!updating && chosen != null && selected() instanceof Light light
                    && !light.getStrategy().name().equals(chosen.name())) {
                facade.setLightingStrategy(light, chosen);
            }
        });

        climateStrategyBox.getItems().setAll(new ComfortClimateStrategy(),
                new EcoClimateStrategy(), new NightClimateStrategy());
        climateStrategyBox.setConverter(new StringConverter<ClimateStrategy>() {
            @Override
            public String toString(ClimateStrategy strategy) {
                return strategy == null ? "" : strategy.name();
            }

            @Override
            public ClimateStrategy fromString(String text) {
                return null;
            }
        });
        climateStrategyBox.getSelectionModel().selectedItemProperty().addListener((o, old, chosen) -> {
            if (!updating && chosen != null && selected() instanceof Thermostat thermostat
                    && !thermostat.getStrategy().name().equals(chosen.name())) {
                facade.setClimateStrategy(thermostat, chosen);
            }
        });

        // Un comando por gesto: se confirma al soltar el slider o al hacer clic en la barra.
        brightnessSlider.valueProperty().addListener((o, old, now) -> {
            brightnessValue.setText((int) Math.round(now.doubleValue()) + "%");
            if (!updating && !brightnessSlider.isValueChanging()) {
                commitBrightness();
            }
        });
        brightnessSlider.valueChangingProperty().addListener((o, was, isChanging) -> {
            if (!updating && !isChanging) {
                commitBrightness();
            }
        });
        temperatureSlider.valueProperty().addListener((o, old, now) -> {
            temperatureValue.setText(String.format("%.1f °C", now.doubleValue()));
            if (!updating && !temperatureSlider.isValueChanging()) {
                commitTemperature();
            }
        });
        temperatureSlider.valueChangingProperty().addListener((o, was, isChanging) -> {
            if (!updating && !isChanging) {
                commitTemperature();
            }
        });

        outsideSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                10.0, 40.0, facade.getEnvironment().getOutsideTemperature(), 0.5));
        outsideSpinner.valueProperty().addListener((o, old, now) -> {
            if (now != null) {
                facade.getEnvironment().setOutsideTemperature(now);
                refreshDetails();
            }
        });

        homeTree.setCellFactory(tree -> new TreeCell<Controllable>() {
            @Override
            protected void updateItem(Controllable item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : describeNode(item));
            }
        });
        homeTree.getSelectionModel().selectedItemProperty().addListener((o, old, now) -> refreshDetails());

        facade.addChangeListener(this::refreshAll);
        refreshAll();
        homeTree.getSelectionModel().selectFirst();
    }

    // ---------- Manejadores de la vista ----------

    @FXML
    private void onUndo() {
        facade.undo();
    }

    @FXML
    private void onRedo() {
        facade.redo();
    }

    @FXML
    private void onRunScene() {
        SmartHomeFacade.Scene scene = sceneBox.getValue();
        if (scene != null) {
            facade.runScene(scene);
        }
    }

    @FXML
    private void onAutomationToggled() {
        facade.setAutomationEnabled(automationCheck.isSelected());
    }

    @FXML
    private void onTogglePower() {
        Controllable target = selected();
        if (target != null) {
            facade.togglePower(target);
        }
    }

    @FXML
    private void onToggleFault() {
        if (selected() instanceof Device device) {
            facade.toggleFault(device);
        }
    }

    @FXML
    private void onApplySuggestion() {
        if (selected() instanceof Device device) {
            facade.applySuggestion(device);
        }
    }

    @FXML
    private void onAddDevice() {
        Room room = newRoomBox.getValue();
        DeviceType type = newTypeBox.getValue();
        if (room == null || type == null) {
            return;
        }
        facade.addDevice(type, newNameField.getText(), room);
        newNameField.clear();
    }

    @FXML
    private void onAddRoom() {
        facade.addRoom(newRoomField.getText());
        newRoomField.clear();
    }

    private void commitBrightness() {
        if (selected() instanceof Light light) {
            int value = (int) Math.round(brightnessSlider.getValue());
            if (value != light.getBrightness()) {
                facade.setBrightness(light, value);
            }
        }
    }

    private void commitTemperature() {
        if (selected() instanceof Thermostat thermostat) {
            double value = Math.round(temperatureSlider.getValue() * 2) / 2.0;
            if (value != thermostat.getTargetTemperature()) {
                facade.setTemperature(thermostat, value);
            }
        }
    }

    // ---------- Actualización de la vista ----------

    private Controllable selected() {
        TreeItem<Controllable> item = homeTree.getSelectionModel().getSelectedItem();
        return item == null ? null : item.getValue();
    }

    private void refreshAll() {
        int signature = facade.devices().size() + 1000 * facade.getHome().rooms().size();
        if (signature != structureSignature) {
            structureSignature = signature;
            rebuildTree();
        }
        homeTree.refresh();
        refreshDetails();
        logList.getItems().setAll(facade.activityLog());
        undoButton.setDisable(!facade.canUndo());
        redoButton.setDisable(!facade.canRedo());
        environmentLabel.setText(String.format("Hora: %02d:00 (%s)", LocalTime.now().getHour(),
                facade.getEnvironment().isDark() ? "noche" : "día"));
    }

    private void rebuildTree() {
        Controllable previous = selected();
        homeTree.setRoot(buildItem(facade.getHome()));
        if (previous != null) {
            selectValue(homeTree.getRoot(), previous);
        }
        Room chosenRoom = newRoomBox.getValue();
        newRoomBox.getItems().setAll(facade.getHome().rooms());
        if (chosenRoom != null && newRoomBox.getItems().contains(chosenRoom)) {
            newRoomBox.setValue(chosenRoom);
        } else {
            newRoomBox.getSelectionModel().selectFirst();
        }
    }

    private TreeItem<Controllable> buildItem(Controllable node) {
        TreeItem<Controllable> item = new TreeItem<>(node);
        item.setExpanded(true);
        if (node instanceof Room room) {
            for (Controllable child : room.getChildren()) {
                item.getChildren().add(buildItem(child));
            }
        }
        return item;
    }

    private boolean selectValue(TreeItem<Controllable> item, Controllable value) {
        if (item.getValue() == value) {
            homeTree.getSelectionModel().select(item);
            return true;
        }
        for (TreeItem<Controllable> child : item.getChildren()) {
            if (selectValue(child, value)) {
                return true;
            }
        }
        return false;
    }

    private static String describeNode(Controllable node) {
        if (node instanceof Device device) {
            return device.getName() + " — " + device.getState().name();
        }
        long on = node.devices().stream().filter(Device::isOn).count();
        return node.getName() + " (" + on + "/" + node.devices().size() + " encendidos)";
    }

    private void refreshDetails() {
        Controllable node = selected();
        updating = true;
        try {
            show(lightControls, false);
            show(thermostatControls, false);
            show(suggestButton, false);
            show(faultButton, false);
            show(powerButton, node != null);
            if (node == null) {
                titleLabel.setText("Selecciona un elemento");
                stateLabel.setText("");
                detailLabel.setText("");
                return;
            }
            titleLabel.setText(node.getName());
            if (node instanceof Device device) {
                showDevice(device);
            } else {
                long on = node.devices().stream().filter(Device::isOn).count();
                stateLabel.setText(on + " de " + node.devices().size() + " dispositivos encendidos");
                detailLabel.setText(node.devices().isEmpty()
                        ? "Esta habitación aún no tiene dispositivos."
                        : "Controla todos los dispositivos de la habitación a la vez.");
                powerButton.setText(on > 0 ? "Apagar todo" : "Encender todo");
            }
        } finally {
            updating = false;
        }
    }

    private void showDevice(Device device) {
        titleLabel.setText(device.getName() + " · " + device.getType().getLabel());
        stateLabel.setText("Estado: " + device.getState().name());
        detailLabel.setText(device.describe());
        show(faultButton, true);
        faultButton.setText(device.isFaulty() ? "Reparar" : "Simular falla");
        if (device instanceof DoorLock) {
            powerButton.setText(device.isOn() ? "Liberar" : "Asegurar");
        } else {
            powerButton.setText(device.isOn() ? "Apagar" : "Encender");
        }
        if (device instanceof Light light) {
            show(lightControls, true);
            show(suggestButton, true);
            brightnessSlider.setValue(light.getBrightness());
            brightnessValue.setText(light.getBrightness() + "%");
            selectByName(lightingStrategyBox, light.getStrategy().name(), LightingStrategy::name);
            suggestButton.setText("Aplicar brillo sugerido ("
                    + light.suggestedBrightness(facade.getEnvironment().currentHour()) + "%)");
        } else if (device instanceof Thermostat thermostat) {
            show(thermostatControls, true);
            show(suggestButton, true);
            temperatureSlider.setValue(thermostat.getTargetTemperature());
            temperatureValue.setText(String.format("%.1f °C", thermostat.getTargetTemperature()));
            selectByName(climateStrategyBox, thermostat.getStrategy().name(), ClimateStrategy::name);
            suggestButton.setText(String.format("Aplicar temperatura sugerida (%.1f °C)",
                    thermostat.suggestedTemperature(facade.getEnvironment().currentHour(),
                            facade.getEnvironment().getOutsideTemperature())));
        }
    }

    private static <T> void selectByName(ChoiceBox<T> box, String name, Function<T, String> nameOf) {
        for (T item : box.getItems()) {
            if (nameOf.apply(item).equals(name)) {
                box.getSelectionModel().select(item);
            }
        }
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
