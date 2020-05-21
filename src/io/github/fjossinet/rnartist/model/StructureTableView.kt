package io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.core.model.*

interface StructuralItem {
    val secondaryStructureElement:SecondaryStructureElement?
    fun getName():String?
    fun getType():String
    fun getLocation():Location
}

abstract class AbstractStructuralItem(secondaryStructureElement:SecondaryStructureElement?):StructuralItem {
    override val secondaryStructureElement = secondaryStructureElement

    override fun getType(): String {
        this.secondaryStructureElement?.let {
            return it.getType()
        }
        return ""
    }
}

class SecondaryStructureItem(val drawing:SecondaryStructureDrawing): AbstractStructuralItem(null) {
    override fun getName(): String? {
        return this.drawing.toString()
    }

    override fun getLocation(): Location {
        return Location(1, drawing.length)
    }

}

class JunctionItem(val junction:JunctionCircle): AbstractStructuralItem(junction) {

    private val locationWithoutSecondaries:Location

    init {
        this.locationWithoutSecondaries = junction.locationWithoutSecondaries //to compute it only once, and not with each call to the function getLocation in this class
    }
    override fun getName(): String? {
        return this.junction.name
    }

    override fun getLocation(): Location {
        //return this.locationWithoutSecondaries
        return this.junction.location
    }

}

class HelixItem(val helix:HelixLine): AbstractStructuralItem(helix) {
    override fun getName(): String? {
        return this.helix.name
    }

    override fun getLocation(): Location {
        return this.helix.location
    }

}

class TertiaryInteractionItem(val tertiaryInteraction:TertiaryInteractionLine): AbstractStructuralItem(tertiaryInteraction) {
    override fun getName(): String? {
        return this.tertiaryInteraction.toString()
    }

    override fun getLocation(): Location {
        return this.tertiaryInteraction.location
    }

}

class SecondaryInteractionItem(val secondaryInteraction:SecondaryInteractionLine): AbstractStructuralItem(secondaryInteraction) {
    override fun getName(): String? {
        return this.secondaryInteraction.toString()
    }

    override fun getLocation(): Location {
        return this.secondaryInteraction.location
    }

}

class ResidueItem(val residue:ResidueCircle): AbstractStructuralItem(residue) {
    override fun getName(): String? {
        return this.residue.label.toString()
    }

    override fun getLocation(): Location {
        return Location(pos=this.residue.absPos)
    }

}