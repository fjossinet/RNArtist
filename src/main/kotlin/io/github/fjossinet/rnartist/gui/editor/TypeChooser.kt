package io.github.fjossinet.rnartist.gui.editor

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.control.skin.ComboBoxListViewSkin
import javafx.scene.text.Text
import javafx.util.Callback
import java.util.stream.Collectors

class TypeChooser(val editor:Script, text: Text, types:List<String>? = null): ComboBox<TypeChooser.TypeItem>() {

    private var types = listOf(
        "A", "a", "U", "u", "G", "g", "C", "c", "R", "r", "Y", "y", "N", "n", "X", "x",
        "helix", "single_strand", "junction", "pknot",
        "secondary_interaction", "tertiary_interaction",
        "phosphodiester_bond", "interaction_symbol")

    init {
        types?.let {
            this.types = types.toList()
        }
        this.minHeight = 30.0
        this.prefHeight = 30.0

        buttonCell = object : ListCell<TypeItem>() {

            override fun updateItem(item: TypeItem?, empty: Boolean) {
                super.updateItem(item, empty)
            }
        }

        val items = FXCollections.observableArrayList<TypeItem>()
        val previousTypes = text.text.replace("\"", "").split(" ")
        for (type in this.types) {
            val item = TypeItem(type)
            if (type in previousTypes)
                item.setSelected(true)
            items.add(item)
        }
        this.items = items

        this.value = TypeItem(getSelection())

        cellFactory = object : Callback<ListView<TypeItem>, ListCell<TypeItem>> {
            override fun call(param: ListView<TypeItem>): ListCell<TypeItem> {
                return object : ListCell<TypeItem>() {
                    private val cb = CheckBox()
                    private var booleanProperty: BooleanProperty? = null
                    override fun updateItem(item: TypeItem?, empty: Boolean) {
                        super.updateItem(item, empty)
                        if (!empty && item != null) {
                            if (booleanProperty != null) {
                                cb.selectedProperty().unbindBidirectional(booleanProperty)
                            }
                            booleanProperty = item.selectedProperty()
                            cb.selectedProperty().bindBidirectional(booleanProperty)
                            graphic = cb
                            setText(item.getType() + "");
                        } else {
                            graphic = null
                            setText(null)
                        }
                    }

                    init {
                        cb.onAction = EventHandler { e: ActionEvent? ->
                            listView.getSelectionModel().select(item)
                            buttonCell.setText(getSelection())
                        }
                    }
                }
            }
        }

        this.onShowing = EventHandler {
            buttonCell.setText(getSelection())
        }

    }

    fun getSelection():String {
        return getItems().stream().filter { i -> i.isSelected() }
            .map { i -> i.toString() + "" }.collect(Collectors.joining(" "))
    }

    override fun createDefaultSkin(): Skin<*>? {
        return object : ComboBoxListViewSkin<TypeItem>(this) {

            init {
                this.isHideOnClick = false
            }

        }
    }

    class TypeItem(type:String) {
        private val type = SimpleStringProperty()
        private val selected = SimpleBooleanProperty()

       init {
           setType(type)
       }

        fun getType(): String {
            return type.get()
        }

        fun typeProperty(): StringProperty? {
            return type
        }

        fun setType(type: String) {
            this.type.set(type)
        }

        fun isSelected(): Boolean {
            return selected.get()
        }

        fun selectedProperty(): BooleanProperty? {
            return selected
        }

        fun setSelected(selected: Boolean) {
            this.selected.set(selected)
        }

        override fun toString(): String {
            return getType()
        }
    }

    override fun getBaselineOffset(): Double {
        return 20.0
    }
}