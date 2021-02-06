package de.dde.snes.da.gui.settings

import de.dde.snes.da.gui.table.ActionId
import de.dde.snes.da.settings.Settings
import de.dde.snes.da.translate
import de.dde.snes.da.util.GridPane
import de.dde.snes.da.util.VBox
import de.dde.snes.da.util.children
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.util.*

class SettingsDialog(
        val settings: Settings,
        val actions: Set<ActionId>
) {
    private val inputMap = mutableMapOf<KeyCombination, ActionId>()
    private val inputMapReverse = mutableMapOf<ActionId, MutableSet<KeyCombination>>()

    private val actionLabels = mutableSetOf<ActionLabel>()

    private val stage = Stage(StageStyle.UTILITY)
    private val root = BorderPane()
    private val scene = Scene(root)

    private val cmbLanguage = ComboBox<Locale>()
    private val slNumProjects = Spinner<Int>(0, 100, 0)

    private val editingAction = SimpleObjectProperty<ActionLabel>(this, "editingAction", null)
    private val updateActionLabels = SimpleBooleanProperty(this, "updateActionLabels", false)

    init {
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.title = translate("de.dde.snes.da.mnu.file.settings")
        stage.scene = scene

        val tabPane = buildTabPane()
        actions.forEach { inputMapReverse[it] = mutableSetOf() }

        scene.setOnKeyPressed {  keyPressed(it) }

        val btns = ButtonBar()
        val btnSave = Button(translate("de.dde.snes.da.save"))
        val btnCancel = Button(translate("de.dde.snes.da.cancel"))

        btns.buttons.addAll(btnSave, btnCancel)
        ButtonBar.setButtonData(btnSave, ButtonBar.ButtonData.OK_DONE)
        ButtonBar.setButtonData(btnCancel, ButtonBar.ButtonData.CANCEL_CLOSE)

        btnCancel.setOnAction { stage.hide() }
        btnSave.setOnAction {
            settings.language = cmbLanguage.value
            settings.lastProjectsCount = slNumProjects.value.toInt()
            settings.inputMap = inputMap
            stage.hide()
        }

        root.bottom = btns

        root.center = tabPane
    }

    private fun buildTabPane(): TabPane {
        val t = TabPane()
        t.side = Side.LEFT

        val tabGeneral = Tab(translate("de.dde.snes.da.settings.general"))
        tabGeneral.content = VBox {
            cmbLanguage.items.addAll(Locale.GERMAN, Locale.ENGLISH)
            cmbLanguage.selectionModel.select(settings.language)

            children(
                    HBox(Label(translate("de.dde.snes.da.settings.locale")), cmbLanguage),
                    HBox(Label(translate("de.dde.snes.da.settings.numProjects")), slNumProjects)
            )
        }

        val tabInput = Tab(translate("de.dde.snes.da.settings.input"))
        tabInput.content = GridPane {
            for (action in actions) {
                rowConstraints.add(RowConstraints((-1).toDouble()))
                add(Label(translate("de.dde.snes.da.action.$action")), 0, rowCount)

                val lbl = ActionLabel(action, Label())
                actionLabels.add(lbl)
                add(lbl.label, 1, rowCount)
            }
        }

        t.tabs.addAll(tabGeneral, tabInput)

        return t
    }

    fun show() {
        cmbLanguage.selectionModel.select(settings.language)
        slNumProjects.valueFactory.value = settings.lastProjectsCount

        inputMap.clear()
        inputMap.putAll(settings.inputMap)
        inputMapReverse.values.forEach { it.clear() }
        inputMap.forEach { (key, action) -> inputMapReverse[action]?.add(key) }

        updateActionLabels()

        stage.show()
    }

    private fun keyPressed(e: KeyEvent) {
        if (e.code == KeyCode.ESCAPE) {
            editingAction.value = null
        } else if (e.code != KeyCode.UNDEFINED && !e.code.isModifierKey) {
            editingAction.value?.let {
                fun Boolean.toModifier() = if (this) KeyCombination.ModifierValue.DOWN else KeyCombination.ModifierValue.UP
                val combination = KeyCodeCombination(
                        e.code,
                        e.isShiftDown.toModifier(),
                        e.isControlDown.toModifier(),
                        e.isAltDown.toModifier(),
                        e.isMetaDown.toModifier(),
                        KeyCombination.ModifierValue.UP
                )

                inputMapReverse[inputMap[combination]]?.remove(combination)

                for (comb in inputMapReverse[it.action]?: emptySet()) {
                    inputMap.remove(comb)
                }
                inputMapReverse[it.action]?.clear()

                inputMap[combination] = it.action
                inputMapReverse[it.action]?.add(combination)

                updateActionLabels()
                editingAction.value = null
            }
        }
    }

    private fun updateActionLabels() {
        updateActionLabels.set(!updateActionLabels.get())
    }

    private inner class ActionLabel(val action: ActionId, val label: Label) {
        init {
            label.textProperty().bind(Bindings.createStringBinding({
                when {
                    editingAction.get() == this -> "..."
                    inputMapReverse[action]?.isEmpty() != false -> "unmapped"
                    else -> inputMapReverse[action]?.joinToString(", ")?: ""
                }
            }, editingAction, updateActionLabels))

            label.setOnMousePressed { if (it.clickCount > 1) editingAction.value = this }
        }
    }
}