package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.io.randomColor
import io.github.fjossinet.rnartist.core.model.*
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.SVGPath

class StructuralDomainColorPicker(mediator: Mediator) : RNArtistColorPicker(mediator) {

    override var behaviors = mapOf(
        Pair("Any") {e:DrawingElement -> (e is HelixDrawing || e is JunctionDrawing || e is SingleStrandDrawing) && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Helices") { e: DrawingElement -> e is HelixDrawing && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Strands") { e: DrawingElement -> e is SingleStrandDrawing && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Junctions") { e: DrawingElement -> e is JunctionDrawing && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Apical Loops") { e: DrawingElement -> e is JunctionDrawing && e.junctionType == JunctionType.ApicalLoop && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Inner Loops") { e: DrawingElement -> e is JunctionDrawing && e.junctionType == JunctionType.InnerLoop && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("3-Way") { e: DrawingElement -> e is JunctionDrawing && e.junctionType == JunctionType.ThreeWay && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("4-Way") { e: DrawingElement -> e is JunctionDrawing && e.junctionType == JunctionType.FourWay && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true}
    )

    init {
        val labels = listOf("Any", "Helices", "Strands", "Junctions", "Apical Loops", "Inner Loops", "3-Way", "4-Way")
        this.targetsComboBox.items.addAll(labels)
        this.targetsComboBox.value = labels.first()

        var c = Circle(0.0, 0.0, 10.0)
        var restricted2SelectionPath = SVGPath()
        restricted2SelectionPath.content = "M165,330c63.411,0,115-51.589,115-115c0-29.771-11.373-56.936-30-77.379V85c0-46.869-38.131-85-85-85 S80.001,38.131,80.001,85v52.619C61.373,158.064,50,185.229,50,215C50,278.411,101.589,330,165,330z M180,219.986V240 c0,8.284-6.716,15-15,15s-15-6.716-15-15v-20.014c-6.068-4.565-10-11.824-10-19.986c0-13.785,11.215-25,25-25s25,11.215,25,25 C190,208.162,186.068,215.421,180,219.986z M110.001,85c0-30.327,24.673-55,54.999-55c30.327,0,55,24.673,55,55v29.029 C203.652,105.088,184.91,100,165,100c-19.909,0-38.651,5.088-54.999,14.028V85z"
        restricted2SelectionPath.fill = if (restrictedToSelection) Color.DARKORANGE else Color.LIGHTGRAY
        restricted2SelectionPath.scaleX = 0.07
        restricted2SelectionPath.scaleY = 0.07
        val useSelection = Button(null, restricted2SelectionPath)
        useSelection.onMouseClicked = EventHandler {
            restrictedToSelection = !restrictedToSelection
            restricted2SelectionPath.fill = if (restrictedToSelection) Color.DARKORANGE else Color.LIGHTGRAY
        }
        useSelection.background = Background.EMPTY
        useSelection.layoutX = 0.0
        useSelection.layoutY = 0.0
        useSelection.shape = c
        useSelection.setMinSize(20.0, 20.0)
        useSelection.setMaxSize(20.0, 20.0)
        colorWheelGroup.children.add(useSelection)

        c = Circle(0.0, 0.0, 10.0)
        var randomColorsPath = SVGPath()
        randomColorsPath.content = "m -223.19833,169.13545 c -0.78711,0.0523 -1.56517,0.18787 -2.31719,0.42354 l -53.02216,16.17069 c -4.88163,1.49153 -7.73299,6.55835 -6.47823,11.51142 l 6.17921,55.6633 c 0.28607,2.53218 1.56825,4.84585 3.56312,6.4285 l 39.2184,31.19527 c 0.16955,0.14664 0.34434,0.28803 0.52304,0.42355 0.24876,0.19506 0.50668,0.37901 0.77179,0.54855 0.26643,0.17087 0.54071,0.32928 0.82284,0.4733 0.26774,0.13812 0.54333,0.26184 0.82286,0.37378 l 0.199,0.0766 c 0.19639,0.0766 0.39669,0.13878 0.59831,0.199 0.18133,0.0523 0.36331,0.10208 0.54857,0.14926 l 0.12766,0.0262 0.0523,0 c 0.21406,0.0523 0.43008,0.089 0.64807,0.12765 l 0.62253,0.0766 0.14925,0 c 0.18983,0.0131 0.38164,0.0263 0.57278,0.0263 0.26643,-0.006 0.53221,-0.0263 0.79733,-0.0523 0.31748,-0.0263 0.63431,-0.0655 0.94656,-0.12766 0.3188,-0.0655 0.63498,-0.1342 0.94657,-0.22452 0.89813,-0.25531 1.75476,-0.64152 2.54154,-1.14558 l 49.30952,-27.68211 c 2.82726,-1.5911 4.65638,-4.50831 4.85872,-7.74909 l 3.6128,-59.60003 c 0.14533,-2.47928 -0.67614,-4.91817 -2.29218,-6.80214 -0.0655,-0.0766 -0.13159,-0.15056 -0.19901,-0.22454 -0.36724,-0.398 -0.76674,-0.76543 -1.19539,-1.09588 -0.0766,-0.0655 -0.14925,-0.11456 -0.22452,-0.17478 l -0.14926,-0.10209 c -0.20948,-0.15842 -0.42615,-0.30767 -0.64807,-0.44907 -0.18722,-0.10208 -0.37902,-0.20554 -0.57278,-0.29849 -0.0891,-0.0392 -0.18264,-0.0891 -0.27428,-0.12766 -0.19639,-0.10208 -0.39539,-0.18983 -0.59832,-0.27428 l -0.0766,-0.0263 c -0.13158,-0.0392 -0.26512,-0.0891 -0.398,-0.12766 l -46.21995,-17.04276 c -1.20304,-0.44121 -2.48255,-0.63629 -3.76238,-0.57278 z m -1.9339,9.41911 c 4.15069,-0.58391 8.2244,0.39539 9.09446,2.19263 0.8688,1.79442 -1.80424,3.72625 -5.95508,4.31049 -4.1507,0.58457 -8.20086,-0.398 -9.06959,-2.19256 -0.87004,-1.79684 1.77937,-3.72626 5.93021,-4.31056 z m -26.68554,7.52472 c 4.39447,-0.66077 8.69672,0.45822 9.61776,2.49163 0.91979,2.03147 -1.90938,4.22291 -6.30386,4.88368 -4.3946,0.66083 -8.69795,-0.4602 -9.61775,-2.49172 -0.92111,-2.03414 1.90937,-4.22277 6.30385,-4.88359 z m 53.62021,1.07165 c 4.63826,0.63563 7.59815,2.73169 6.62775,4.68415 -0.9721,1.95506 -5.50274,3.02718 -10.14099,2.39201 -4.6384,-0.63562 -7.62447,-2.75407 -6.65277,-4.70921 0.97086,-1.95237 5.52775,-3.00218 10.16601,-2.36695 z m -29.97451,0.29851 c 5.12588,-0.63563 10.13818,0.43597 11.21233,2.39195 1.07291,1.95251 -2.1995,4.04904 -7.32543,4.68434 -5.12589,0.63562 -10.16442,-0.4399 -11.23721,-2.39202 -1.07415,-1.95513 2.22451,-4.04898 7.35031,-4.68427 z m 1.84385,9.71739 c 3.74104,0.13419 6.86767,1.18131 7.6991,2.7658 1.10603,2.11041 -2.28625,4.37164 -7.57458,5.05793 -5.28843,0.68636 -10.48009,-0.45562 -11.58612,-2.56635 -1.10859,-2.11321 2.28623,-4.39663 7.57459,-5.08299 1.32205,-0.17086 2.63997,-0.2193 3.88701,-0.17478 z m -44.424,9.10552 c 1.86853,0.11456 4.11836,2.11905 5.70574,5.30721 2.1172,4.25098 2.2296,9.02534 0.24875,10.63927 -1.97778,1.61186 -5.28357,-0.5335 -7.40007,-4.78397 -2.11721,-4.25078 -2.22705,-9.00248 -0.24875,-10.61434 0.49489,-0.40324 1.0716,-0.58784 1.69447,-0.54855 z m 86.79214,4.69502 c 0.85605,-0.11456 1.69316,-0.0392 2.44177,0.32403 2.9904,1.44107 3.56449,6.44315 1.29574,11.16258 -2.26888,4.71923 -6.5276,7.37113 -9.51813,5.93006 -2.99451,-1.44308 -3.58937,-6.41828 -1.32055,-11.1377 1.70161,-3.53949 4.53202,-5.91893 7.10117,-6.27885 z m -65.88729,7.19006 c 2.02916,0.12764 4.50517,2.24323 6.22909,5.60629 2.29835,4.4839 2.42475,9.48501 0.27428,11.18746 -2.14784,1.70029 -5.77434,-0.54856 -8.07293,-5.03311 -2.29795,-4.48404 -2.42181,-9.51218 -0.27429,-11.21247 0.53678,-0.42614 1.16731,-0.59046 1.84385,-0.54856 z m 38.82805,4.09703 c 0.85597,-0.11456 1.69322,-0.0392 2.44182,0.32403 2.99041,1.44106 3.56437,6.44315 1.29561,11.1625 -2.26888,4.7193 -6.52746,7.37127 -9.51798,5.93021 -2.99453,-1.4431 -3.5645,-6.41841 -1.29569,-11.13765 1.70161,-3.53949 4.50701,-5.91913 7.07624,-6.27897 z m -57.2413,2.10718 c 1.7078,0.12766 3.78162,2.16043 5.2325,5.40691 1.93483,4.32862 2.0344,9.17027 0.22453,10.8138 -1.80771,1.6413 -4.84262,-0.52958 -6.77727,-4.85873 -1.93503,-4.32862 -2.03197,-9.17243 -0.22452,-10.8138 0.45299,-0.4111 0.97589,-0.58916 1.54476,-0.54857 z m 19.68399,13.38015 c 2.02916,0.12765 4.48015,2.24309 6.20421,5.60617 2.29834,4.48389 2.42475,9.48513 0.27427,11.18758 -2.14784,1.70029 -5.74945,-0.54856 -8.04799,-5.03325 -2.29796,-4.48402 -2.42187,-9.51203 -0.27428,-11.21233 0.53678,-0.42615 1.16724,-0.59045 1.84379,-0.54855 z m 64.0185,4.22153 c 0.85604,-0.11456 1.69329,-0.0131 2.4419,0.34957 2.9904,1.44106 3.56436,6.41826 1.29562,11.13763 -2.2689,4.7193 -6.52748,7.39601 -9.51801,5.95494 -2.99452,-1.44309 -3.5895,-6.44327 -1.32067,-11.16251 1.70172,-3.53948 4.532,-5.919 7.10116,-6.27898 z m -81.48494,0.76157 c 1.60079,0.10209 3.54859,1.85426 4.90847,4.6346 1.8138,3.70687 1.89597,7.86149 0.19901,9.26885 -1.69439,1.40558 -4.54039,-0.45431 -6.35373,-4.16104 -1.81274,-3.70682 -1.89366,-7.86353 -0.199,-9.26899 0.42485,-0.35218 0.9109,-0.50797 1.44512,-0.47328 z m 55.32278,11.57198 c 0.85596,-0.11455 1.69322,-0.0131 2.44183,0.34956 2.99039,1.441 3.58924,6.44315 1.32048,11.16252 -2.26889,4.7193 -6.55234,7.37112 -9.54287,5.93006 -2.99452,-1.44309 -3.56463,-6.44328 -1.2958,-11.16251 1.70173,-3.53962 4.50713,-5.91913 7.07636,-6.27897 z m -36.66037,2.10719 c 2.02916,0.12765 4.50511,2.24323 6.22909,5.60617 2.29834,4.4839 2.39987,9.48513 0.24876,11.18752 -2.14779,1.70028 -5.74941,-0.54857 -8.048,-5.03305 -2.29795,-4.48416 -2.42181,-9.4873 -0.2743,-11.18759 0.53745,-0.42615 1.16732,-0.61468 1.84379,-0.57279 z"
        randomColorsPath.fill = Color.LIGHTGRAY
        randomColorsPath.scaleX = 0.17
        randomColorsPath.scaleY = 0.17
        val randomColors = Button(null, randomColorsPath)
        randomColors.onMousePressed = EventHandler {
            randomColorsPath.fill = Color.DARKORANGE
        }
        randomColors.onMouseReleased = EventHandler {
            randomColorsPath.fill = Color.LIGHTGRAY
            val t = Theme()
            mediator.drawingDisplayed.get()?.let { drawing ->

                if (this.targetsComboBox.value in listOf("Any", "Helices"))
                    drawing.drawing.allHelices.forEach {
                        var color = getHTMLColorString(randomColor())
                        t.setConfigurationFor(selection = { e: DrawingElement -> e is HelixDrawing && e.location.start == it.location.start && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true }, ThemeParameter.color) {
                            color
                        }
                    }

                if (this.targetsComboBox.value in listOf("Any", "Junctions", "Apical Loops", "Inner Loops", "3-Way", "4-Way")) {
                    drawing.drawing.allJunctions.forEach {
                        var color = getHTMLColorString(randomColor())
                        val fine = this.targetsComboBox.value.equals("Any")
                                || this.targetsComboBox.value.equals("Junctions")
                                || this.targetsComboBox.value.equals("Apical Loops") && it.junctionType == JunctionType.ApicalLoop
                                || this.targetsComboBox.value.equals("Inner Loops") && it.junctionType == JunctionType.InnerLoop
                                || this.targetsComboBox.value.equals("3-Way") && it.junctionType == JunctionType.ThreeWay
                                || this.targetsComboBox.value.equals("4-Way") && it.junctionType == JunctionType.FourWay
                        t.setConfigurationFor(selection = { e: DrawingElement ->
                            e is JunctionDrawing && e.location.start == it.location.start && fine && if (this.restrictedToSelection) e.pathToStructuralDomain()
                                .any { mediator.canvas2D.getSelection().contains(it) } else true
                        }, ThemeParameter.color) {
                            color
                        }
                    }
                }

                if (this.targetsComboBox.value in listOf("Any", "Strands"))
                    drawing.drawing.singleStrands.forEach {
                        var color = getHTMLColorString(randomColor())
                        t.setConfigurationFor(selection = { e: DrawingElement -> e is SingleStrandDrawing && e.location.start == it.location.start && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true }, ThemeParameter.color) {
                            color
                        }
                    }

                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
                mediator.canvas2D.repaint()

            }
        }
        randomColors.background = Background.EMPTY
        randomColors.shape = c
        randomColors.layoutX = 0.0
        randomColors.layoutY = 150.0
        randomColors.setMinSize(20.0, 20.0)
        randomColors.setMaxSize(20.0, 20.0)
        colorWheelGroup.children.add(randomColors)
    }

}