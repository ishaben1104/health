package com.example.health

import android.text.InputFilter
import android.text.Spanned

class FixedTimeFormatInputFilter : InputFilter {
    private val timeRegex = Regex("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val newText = dest?.subSequence(0, dstart).toString() +
                source?.subSequence(start, end) +
                dest?.subSequence(dend, dest.length)

        return if (newText.matches(timeRegex)) {
            null // Accept the input
        } else {
            ""   // Reject the input
        }
    }
}