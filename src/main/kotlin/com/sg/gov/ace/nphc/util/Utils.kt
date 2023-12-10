package com.sg.gov.ace.nphc.util

import java.util.UUID

class Utils {

    companion object {
        fun generateUUID() = UUID.randomUUID().toString().replace("-","")
    }

}