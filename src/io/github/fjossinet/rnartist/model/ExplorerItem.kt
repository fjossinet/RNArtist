package io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.core.model.*

interface ExplorerItem {
    val name:String
    var location:String
    var color:String?
    var charColor:String?
    var lineWidth:String?
    var lineShift:String?
    var opacity:String?
    val secondaryStructureElement:SecondaryStructureElement?

    fun setDrawingConfigurationParameter(param:String, value:String?)

}

abstract class AbstractExplorerItem(name:String, secondaryStructureElement:SecondaryStructureElement? = null):ExplorerItem {
    override val name = name
    override var location = ""
    override var color:String? = ""
    override var charColor:String? = ""
    override var lineWidth:String? = ""
    override var lineShift:String? = ""
    override var opacity:String? = ""
    override val secondaryStructureElement = secondaryStructureElement
}

class SecondaryStructureItem(val drawing:SecondaryStructureDrawing): AbstractExplorerItem("Full 2D") {

    override var location = Location(1, drawing.length).toString()

    override var color:String? = ""
        get() {
            return this.drawing.drawingConfiguration.params.getOrDefault(DrawingConfigurationParameter.Color.toString(),"")
        }

    override var charColor:String? = ""
        get() {
            return this.drawing.drawingConfiguration.params.getOrDefault(DrawingConfigurationParameter.CharColor.toString(),"")
        }

    override var lineWidth: String? = ""
        get() {
            return this.drawing.drawingConfiguration.params.getOrDefault(DrawingConfigurationParameter.LineWidth.toString(),"")
        }

    override var lineShift: String? = ""
        get() {
            return this.drawing.drawingConfiguration.params.getOrDefault(DrawingConfigurationParameter.LineShift.toString(),"")
        }

    override var opacity: String? = ""
        get() {
            return this.drawing.drawingConfiguration.params.getOrDefault(DrawingConfigurationParameter.Opacity.toString(),"")
        }

    override fun setDrawingConfigurationParameter(param:String, value:String?) {
        if (value == null)
            this.drawing.drawingConfiguration.params[param] = RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString())!![param.toString()]!! //we restore the default value
        else
            this.drawing.drawingConfiguration.params[param] = value
    }

}

abstract class StructuralItem(name:String, secondaryStructureElement:SecondaryStructureElement):AbstractExplorerItem(name, secondaryStructureElement) {

    override val name = name

    override var location = this.secondaryStructureElement!!.location.description

    override var color:String? = ""
        get():String? = this.secondaryStructureElement!!.drawingConfiguration.params.getOrDefault(DrawingConfigurationParameter.Color.toString(),"")

    override var charColor:String? = ""
        get():String? = this.secondaryStructureElement!!.drawingConfiguration.params.getOrDefault(DrawingConfigurationParameter.CharColor.toString(),"")

    override var lineWidth:String? = ""
        get():String? = this.secondaryStructureElement!!.drawingConfiguration.params.getOrDefault(DrawingConfigurationParameter.LineWidth.toString(),"")

    override var opacity: String? = ""
        get():String? = this.secondaryStructureElement!!.drawingConfiguration.params.getOrDefault(DrawingConfigurationParameter.Opacity.toString(),"")

    override fun setDrawingConfigurationParameter(param:String, value:String?) {
        if (value == null)
            this.secondaryStructureElement!!.drawingConfiguration.params.remove(param)
        else
            this.secondaryStructureElement!!.drawingConfiguration.params[param.toString()] = value
    }
}
class GroupOfStructuralElements(name:String) : AbstractExplorerItem(name) {
    override var location = ""

    override fun setDrawingConfigurationParameter(param: String, value: String?) {
    }
}

class SingleStrandItem(val ss:SingleStrandDrawing): StructuralItem(ss.name, ss) {

}

class JunctionItem(val junction:JunctionDrawing): StructuralItem(junction.name, junction) {

    private val locationWithoutSecondaries:Location

    init {
        this.locationWithoutSecondaries = junction.locationWithoutSecondaries //to compute it only once, and not with each call to the function getLocation in this class
    }

}

class PknotItem(val pknot:PKnotDrawing): StructuralItem(pknot.name, pknot) {

}

class HelixItem(val helix:HelixDrawing): StructuralItem(helix.name, helix) {

}

class TertiaryInteractionItem(val tertiaryInteraction:TertiaryInteractionDrawing): StructuralItem(tertiaryInteraction.toString(), tertiaryInteraction) {

}

class SecondaryInteractionItem(val secondaryInteraction:SecondaryInteractionDrawing): StructuralItem(secondaryInteraction.toString(), secondaryInteraction) {

}

class PhosphodiesterItem(val phosphodiesterBondLine: PhosphodiesterBondDrawing): StructuralItem("Phosphodiester Bond", phosphodiesterBondLine) {
    override var charColor: String? = null
        get() = null

}

class ResidueItem(val residue:ResidueDrawing): StructuralItem(residue.name, residue) {

    override var lineShift: String? = null
        get() = null
}

class LWSymbolItem(val symbol:LWSymbolDrawing): StructuralItem(symbol.toString(), symbol) {

    override var charColor: String? = null
        get() = null

    override var lineShift: String? = null
        get() = null

}