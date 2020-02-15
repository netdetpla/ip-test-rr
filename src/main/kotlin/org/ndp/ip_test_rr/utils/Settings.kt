package org.ndp.ip_test_rr.utils

import java.io.FileReader
import java.util.*


object Settings {
    val setting = Properties()

    init {
        val inFile = FileReader("settings.properties")
        setting.load(inFile)
        inFile.close()
    }

}