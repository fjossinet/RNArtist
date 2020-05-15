package io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.core.model.*

interface StructuralItem {
    fun getName():String?
    fun getType():String
    fun getLocation():Location
}

class SecondaryStructureItem(val drawing:SecondaryStructureDrawing): StructuralItem {
    override fun getName(): String? {
        return this.drawing.toString()
    }

    override fun getType(): String {
        return ""
    }

    override fun getLocation(): Location {
        return Location(1, drawing.length)
    }

}

class JunctionItem(val junction:JunctionCircle): StructuralItem {
    override fun getName(): String? {
        return this.junction.name
    }

    override fun getType(): String {
        var typeName: String = this.junction.type.name
        if (typeName.endsWith("Way")) {
            typeName = typeName.split("Way".toRegex()).toTypedArray()[0] + " Way Junction"
        } else if (typeName.endsWith("Loop")) {
            typeName = typeName.split("Loop".toRegex()).toTypedArray()[0] + " Loop"
        }
        return typeName
    }

    override fun getLocation(): Location {
        return this.junction.location
    }

}

class HelixItem(val helix:HelixLine): StructuralItem {
    override fun getName(): String? {
        return this.helix.name
    }

    override fun getType(): String {
        return "Helix"
    }

    override fun getLocation(): Location {
        return this.helix.location
    }

}

class TertiaryInteractionItem(val tertiaryInteraction:TertiaryInteractionLine): StructuralItem {
    override fun getName(): String? {
        return this.tertiaryInteraction.toString()
    }

    override fun getType(): String {
        return "Tertiary Interaction"
    }

    override fun getLocation(): Location {
        return this.tertiaryInteraction.location
    }

}

class SecondaryInteractionItem(val secondaryInteraction:SecondaryInteractionLine): StructuralItem {
    override fun getName(): String? {
        return this.secondaryInteraction.toString()
    }

    override fun getType(): String {
        return "Secondary Interaction"
    }

    override fun getLocation(): Location {
        return this.secondaryInteraction.location
    }

}

class ResidueItem(val residue:ResidueCircle): StructuralItem {
    override fun getName(): String? {
        return this.residue.label.toString()
    }

    override fun getType(): String {
        return "Residue"
    }

    override fun getLocation(): Location {
        return Location(pos=this.residue.absPos)
    }

}