package io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.core.model.*


interface ExplorerItem {
    val name:String
    var location:String
    var color:String
    var lineWidth:String
    var lineShift:String
    var opacity:String
    var fullDetails:String
    val drawingElement:DrawingElement?
    val residues:List<ResidueDrawing>

    fun applyTheme(theme: Theme)
    fun applyAdvancedTheme(theme: AdvancedTheme)

}

abstract class AbstractExplorerItem(name:String, drawingElement:DrawingElement? = null):ExplorerItem {

    override val name = name
    override var location = ""
    override var color:String = ""
    override var lineWidth:String = ""
    override var lineShift:String = ""
    override var opacity:String = ""
    override var fullDetails:String = ""
    override val drawingElement:DrawingElement? = drawingElement

}

class SecondaryStructureItem(private val drawing:SecondaryStructureDrawing): AbstractExplorerItem("2D") {

    override var location = Location(1, drawing.length).toString()

    override var color:String = ""
        get () = ""
    override var lineWidth:String = ""
        get () = ""
    override var lineShift:String = ""
        get () = ""
    override var opacity: String = ""
        get () = ""
    override var fullDetails: String = ""
        get () = ""

    override val residues:List<ResidueDrawing>
        get() =this.drawing.residues

    override fun applyTheme(theme: Theme) {
        theme.configurations[SecondaryStructureType.Full2D.toString()]?.let { configuration ->
            configuration.keys.forEach {
                when(it) {
                    DrawingConfigurationParameter.color.toString() -> this.color = configuration[it]!!
                    DrawingConfigurationParameter.linewidth.toString() -> this.lineWidth = configuration[it]!!
                    DrawingConfigurationParameter.lineshift.toString() -> this.lineShift = configuration[it]!!
                    DrawingConfigurationParameter.opacity.toString() -> this.opacity = configuration[it]!!
                    DrawingConfigurationParameter.fulldetails.toString() -> this.fullDetails = configuration[it]!!
                }
            }
        }
    }

    override fun applyAdvancedTheme(theme: AdvancedTheme) {
        //not a drawing element so....
    }

}

abstract class StructuralItem(name:String, drawingElement:DrawingElement):AbstractExplorerItem(name, drawingElement) {

    override val name = name
    override var location = drawingElement.location.description

    override var color:String = getHTMLColorString(drawingElement.drawingConfiguration.color)
        set(value) {
            field = value
            this.drawingElement!!.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] = value
        }

    override var lineWidth:String = drawingElement.drawingConfiguration.lineWidth.toString()
        set(value) {
            field = value
            this.drawingElement!!.drawingConfiguration.params[DrawingConfigurationParameter.linewidth.toString()] =  value
        }

    override var fullDetails:String = drawingElement.drawingConfiguration.fullDetails.toString()
        set(value) {
            field = value
            this.drawingElement!!.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =  value
        }

    override var lineShift:String = drawingElement.drawingConfiguration.lineShift.toString()
        set(value) {
            field = value
            this.drawingElement!!.drawingConfiguration.params[DrawingConfigurationParameter.lineshift.toString()] =  value
        }

    override var opacity: String = drawingElement.drawingConfiguration.opacity.toString()

        set(value) {
            field = value
            this.drawingElement!!.drawingConfiguration.params[DrawingConfigurationParameter.opacity.toString()] =  value
        }

    override val residues:List<ResidueDrawing>
        get() =this.drawingElement!!.residues

    override fun applyTheme(theme: Theme) {
        theme.configurations[this.drawingElement!!.type.toString()]?.let { configuration ->
            configuration.keys.forEach {
                when(it) {
                    DrawingConfigurationParameter.color.toString() -> this.color = configuration[it]!!
                    DrawingConfigurationParameter.linewidth.toString() -> this.lineWidth = configuration[it]!!
                    DrawingConfigurationParameter.lineshift.toString() -> this.lineShift = configuration[it]!!
                    DrawingConfigurationParameter.opacity.toString() -> this.opacity = configuration[it]!!
                    DrawingConfigurationParameter.fulldetails.toString() -> this.fullDetails = configuration[it]!!
                }
            }
        }
    }

    override fun applyAdvancedTheme(theme: AdvancedTheme) {
        theme.configurations.entries.forEach { entry ->
            if (entry.key(this.drawingElement!!)) {
                when(entry.value.first) {
                    DrawingConfigurationParameter.color.toString() -> this.color = entry.value.second
                    DrawingConfigurationParameter.linewidth.toString() -> this.lineWidth = entry.value.second
                    DrawingConfigurationParameter.lineshift.toString() -> this.lineShift = entry.value.second
                    DrawingConfigurationParameter.opacity.toString() -> this.opacity = entry.value.second
                    DrawingConfigurationParameter.fulldetails.toString() -> this.fullDetails = entry.value.second
                }
            }
        }
    }
}
class GroupOfStructuralElements(name:String) : AbstractExplorerItem(name) {

    val children = mutableListOf<ExplorerItem>()

    override var color:String
        set(value) {
            children.forEach { it.color = value }
        }
        get () = ""
    override var lineWidth:String
        set(value) {
            children.forEach { it.lineWidth = value }
        }
        get () = ""
    override var lineShift:String
        set(value) {
            children.forEach { it.lineShift = value }
        }
        get () = ""
    override var opacity: String
        set(value) {
            children.forEach { it.opacity = value }
        }
        get () = ""
    override var fullDetails: String
        set(value) {
            children.forEach { it.fullDetails = value }
        }
        get () = ""

    override val residues:List<ResidueDrawing>
        get() = this.children.flatMap { it.residues }

    override fun applyTheme(theme: Theme) {
        children.forEach { it.applyTheme(theme) }
    }

    override fun applyAdvancedTheme(theme: AdvancedTheme) {
        children.forEach { it.applyAdvancedTheme(theme) }
    }
}

class SingleStrandItem(private val ss:SingleStrandDrawing): StructuralItem("${ss.name} [${ss.location}]", ss) {
    override var lineShift: String = ""
        get() = ""

}

class JunctionItem(val junction:JunctionDrawing): StructuralItem("${junction.junctionType.name} ${junction.name} [${junction.location}]", junction) {
    override var lineShift: String = ""
        get() = ""
}

class PknotItem(private val pknot:PKnotDrawing): StructuralItem("${pknot.name} [${pknot.location}]", pknot) {
    override var lineShift: String = ""
        get() = ""
}

class HelixItem(val helix:HelixDrawing): StructuralItem("${helix.name} [${helix.location}]", helix) {
    override var lineShift: String = ""
        get() = ""
}

class TertiaryInteractionItem(private val tertiaryInteraction:TertiaryInteractionDrawing): StructuralItem("${tertiaryInteraction.name} [${tertiaryInteraction.location}]", tertiaryInteraction)

class SecondaryInteractionItem(private val secondaryInteraction:SecondaryInteractionDrawing): StructuralItem("${secondaryInteraction.name} [${secondaryInteraction.location}]", secondaryInteraction)

class PhosphodiesterItem(private val phosphodiesterBondLine: PhosphodiesterBondDrawing): StructuralItem("${phosphodiesterBondLine.start}-${phosphodiesterBondLine.end}", phosphodiesterBondLine)

class InteractionSymbolItem(private val interactionSymbol:InteractionSymbolDrawing): StructuralItem("Symbol", interactionSymbol) {
    override var lineShift: String = ""
        get() = ""
}

class ResidueItem(val residue:ResidueDrawing): StructuralItem("${residue.name}${residue.location.start}", residue) {

    override var lineShift: String = ""
        get() = ""
}

class ResidueLetterItem(private val residueLetter:ResidueLetterDrawing): StructuralItem("${residueLetter.name}", residueLetter) {

    override var lineShift: String = ""
        get() = ""

    override var lineWidth: String = ""
        get() = ""
}

class DefaultSymbolItem(private val symbol:LWSymbolDrawing): StructuralItem("Default Symbol", symbol) {

    override var lineShift: String = ""
        get() = ""

}

class LWSymbolItem(private val symbol:LWSymbolDrawing): StructuralItem(symbol.toString(), symbol) {

    override var lineShift: String = ""
        get() = ""

}