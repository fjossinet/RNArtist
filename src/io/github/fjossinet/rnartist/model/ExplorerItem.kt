package io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.core.model.*

interface ExplorerItem {
    val name:String
    var value:String
    val secondaryStructureElement:SecondaryStructureElement?
}

abstract class AbstractExplorerItem(name:String, secondaryStructureElement:SecondaryStructureElement? = null):ExplorerItem {
    override val name = name
    override var value = ""
    override val secondaryStructureElement = secondaryStructureElement
}

abstract class StructuralItem(name:String, secondaryStructureElement:SecondaryStructureElement):AbstractExplorerItem(name, secondaryStructureElement) {
    override val name = name
    override var value = this.secondaryStructureElement!!.location.description
}

class ThemeItem(): AbstractExplorerItem("Theme") {

}

class ThemeParameterItem(key:String, value:String): AbstractExplorerItem("Theme") {
    override val name = key
    override var value = value
}

class GroupOfStructuralElements(name:String) : AbstractExplorerItem(name) {
    override var value = ""
}
class SecondaryStructureItem(val drawing:SecondaryStructureDrawing): AbstractExplorerItem("Full 2D") {

    override var value = Location(1, drawing.length).toString()

}

class JunctionItem(val junction:JunctionCircle): StructuralItem(junction.name, junction) {

    private val locationWithoutSecondaries:Location

    init {
        this.locationWithoutSecondaries = junction.locationWithoutSecondaries //to compute it only once, and not with each call to the function getLocation in this class
    }

}

class HelixItem(val helix:HelixLine): StructuralItem(helix.name, helix) {

}

class TertiaryInteractionItem(val tertiaryInteraction:TertiaryInteractionLine): StructuralItem(tertiaryInteraction.toString(), tertiaryInteraction) {


}

class SecondaryInteractionItem(val secondaryInteraction:SecondaryInteractionLine): StructuralItem(secondaryInteraction.toString(), secondaryInteraction) {


}

class ResidueItem(val residue:ResidueCircle): StructuralItem(residue.label.toString(), residue) {

}