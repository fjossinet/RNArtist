import io.github.fjossinet.rnartist.core.*

rnartist {
    ss {
        bn {
            seq = "GCGAAAAAUCGC"
            value =
                "((((....))))"
        }
    }
    data {
        "1" to 200.7
        "2" to 192.3
        "3" to 143.6
        "4" to 34.8
        "5" to 4.5
        "6" to 234.9
        "7" to 12.3
        "8" to 56.8
        "9" to 59.8
        "10" to 140.5
        "11" to 0.2
        "12" to 345.8
    }
    theme {
        details {
            value = 2
        }

        show {
            type = "Y R"
        }

        color {
            type = "Y"
            value = "white"
            to = "red"
        }

        color {
            type = "R"
            value = "white"
            to = "green"
            data gt 50.0
        }

    }
}