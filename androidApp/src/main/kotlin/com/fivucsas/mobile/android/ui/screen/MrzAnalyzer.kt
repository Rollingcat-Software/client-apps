package com.fivucsas.mobile.android.ui.screen

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * MRZ document format type.
 */
enum class MrzFormat {
    TD1,  // ID cards: 3 lines × 30 characters
    TD3,  // Passports: 2 lines × 44 characters
    UNKNOWN
}

/**
 * Parsed MRZ data from OCR scan.
 *
 * Ported from practice-and-test/UniversalNfcReader.
 */
data class ScannedMrzData(
    val documentNumber: String,
    val dateOfBirth: String,
    val dateOfExpiry: String,
    val confidence: Float = 0f,
    val checksumValid: Boolean = false,
    val format: MrzFormat = MrzFormat.UNKNOWN,
    val surname: String = "",
    val givenNames: String = "",
    val nationality: String = "",
    val sex: String = ""
)

/**
 * MRZ Scanner using ML Kit Text Recognition.
 *
 * Supports both TD1 (ID cards) and TD3 (Passports) formats per ICAO Doc 9303.
 */
class MrzAnalyzer(
    private val onMrzDetected: (ScannedMrzData) -> Unit,
    private val onError: (String) -> Unit,
    private val onTextDetected: ((String) -> Unit)? = null
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "MrzAnalyzer"
    }

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var isProcessing = false
    private var lastSuccessTime = 0L

    private val scanDebounceMs = 1500L
    private val mrzCharRegex = Regex("^[A-Z0-9<]+$")

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()

        if (isProcessing || (currentTime - lastSuccessTime < scanDebounceMs)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        isProcessing = true

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text

                val mrzLikeText = extractMrzLikeText(fullText)
                if (mrzLikeText.isNotEmpty()) {
                    onTextDetected?.invoke(mrzLikeText)
                }

                val mrzData = parseMrz(fullText)
                if (mrzData != null) {
                    lastSuccessTime = System.currentTimeMillis()
                    onMrzDetected(mrzData)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "MRZ OCR failed", e)
                onError("OCR failed: ${e.message}")
            }
            .addOnCompleteListener {
                isProcessing = false
                imageProxy.close()
            }
    }

    private fun extractMrzLikeText(text: String): String {
        val lines = text.uppercase()
            .split("\n")
            .map { line -> line.replace(" ", "").trim() }
            .filter { line ->
                line.length >= 15 &&
                        (line.contains("<") || line.any { it.isDigit() }) &&
                        line.count { it.isLetterOrDigit() || it == '<' } > line.length * 0.7
            }
            .take(3)

        return lines.joinToString("")
    }

    private fun parseMrz(text: String): ScannedMrzData? {
        val lines = text.uppercase()
            .split("\n")
            .map { line -> line.replace(" ", "").trim() }
            .filter { it.length >= 20 }

        val td1Result = parseTd1(lines)
        if (td1Result != null) return td1Result

        val td3Result = parseTd3(lines)
        if (td3Result != null) return td3Result

        return findMrzPatterns(text)
    }

    private fun parseTd1(lines: List<String>): ScannedMrzData? {
        if (lines.size < 2) return null

        val mrzLines = lines.mapNotNull { line -> normalizeMrzLine(line) }
            .filter { line -> line.length >= 28 && mrzCharRegex.matches(line) }

        if (mrzLines.size < 2) return null

        val line1Candidates = mrzLines.filter { isTd1Line1(it) }
        val line2Candidates = mrzLines.filter { isTd1Line2(it) }

        for (line1 in line1Candidates) {
            for (line2 in line2Candidates) {
                val docNum = extractTd1DocumentNumber(line1)
                val dob = extractTd1DateOfBirth(line2)
                val doe = extractTd1DateOfExpiry(line2)

                if (docNum != null && dob != null && doe != null) {
                    val checksumValid = validateTd1CheckDigits(line1, line2)
                    if (checksumValid) {
                        val nationality = if (line2.length >= 18) {
                            line2.substring(15, 18).replace("<", "")
                        } else ""
                        val sex = if (line2.length > 7) line2[7].toString() else ""

                        return ScannedMrzData(
                            documentNumber = docNum,
                            dateOfBirth = dob,
                            dateOfExpiry = doe,
                            confidence = 0.98f,
                            checksumValid = true,
                            format = MrzFormat.TD1,
                            nationality = nationality,
                            sex = sex
                        )
                    }
                }
            }
        }

        for (line1 in line1Candidates) {
            for (line2 in line2Candidates) {
                val docNum = extractTd1DocumentNumber(line1)
                val dob = extractTd1DateOfBirth(line2)
                val doe = extractTd1DateOfExpiry(line2)

                if (docNum != null && dob != null && doe != null) {
                    val nationality = if (line2.length >= 18) {
                        line2.substring(15, 18).replace("<", "")
                    } else ""
                    val sex = if (line2.length > 7) line2[7].toString() else ""

                    return ScannedMrzData(
                        documentNumber = docNum,
                        dateOfBirth = dob,
                        dateOfExpiry = doe,
                        confidence = 0.7f,
                        checksumValid = false,
                        format = MrzFormat.TD1,
                        nationality = nationality,
                        sex = sex
                    )
                }
            }
        }

        return null
    }

    private fun normalizeMrzLine(line: String): String? {
        var normalized = line.uppercase()
            .replace(" ", "")
            .replace("«", "<")
            .replace("‹", "<")
            .replace("›", "<")
            .replace("|", "I")
            .trim()

        val validChars = normalized.count { it.isLetterOrDigit() || it == '<' }
        if (validChars < normalized.length * 0.85) return null

        normalized = normalized.filter { it.isLetterOrDigit() || it == '<' }

        return if (normalized.length >= 25) normalized else null
    }

    private fun isTd1Line1(line: String): Boolean {
        if (line.length < 15) return false

        val startsWithType = line.matches(Regex("^[IAC][<A-Z][A-Z]{3}.*"))
        val hasDocNumber = if (line.length >= 14) {
            val docPart = line.substring(5, minOf(14, line.length))
            docPart.any { it.isLetterOrDigit() && it != '<' }
        } else false
        val startsWithDate = line.take(6).all { it.isDigit() }

        return startsWithType && hasDocNumber && !startsWithDate
    }

    private fun isTd1Line2(line: String): Boolean {
        if (line.length < 18) return false

        val potentialDob = extractDateWithOcrCorrection(line, 0, 6)
        val potentialDoe = extractDateWithOcrCorrection(line, 8, 14)

        val sexChar = if (line.length > 7) line[7] else ' '
        val validSex = sexChar in listOf('M', 'F', '<', 'X', 'H', 'W')

        val hasNationality = if (line.length >= 18) {
            val natPart = line.substring(15, 18)
            natPart.all { it.isLetter() || it == '<' }
        } else false

        return potentialDob != null && potentialDoe != null && (validSex || hasNationality)
    }

    private fun extractTd1DocumentNumber(line: String): String? {
        if (line.length < 14) return null
        if (!isTd1Line1(line)) return null

        val docNumRaw = line.substring(5, minOf(14, line.length))
        val docNum = docNumRaw.trimEnd('<').replace("<", "")
        if (docNum.isEmpty()) return null

        return if (docNum.length in 1..9 && docNum.all { it.isLetterOrDigit() }) docNum else null
    }

    private fun extractTd1DateOfBirth(line: String): String? {
        return extractDateWithOcrCorrection(line, 0, 6)
    }

    private fun extractTd1DateOfExpiry(line: String): String? {
        return extractDateWithOcrCorrection(line, 8, 14)
    }

    private fun parseTd3(lines: List<String>): ScannedMrzData? {
        val mrzLines = lines.mapNotNull { line -> normalizeMrzLine(line) }
            .filter { line -> line.length >= 40 && mrzCharRegex.matches(line) }

        val line1Candidates = mrzLines.filter { isTd3Line1(it) }
        val line2Candidates = mrzLines.filter { isTd3Line2(it) }

        for (line1 in line1Candidates) {
            for (line2 in line2Candidates) {
                val result = extractTd3Data(line1, line2, validateChecksum = true)
                if (result != null) return result
            }
        }

        for (line1 in line1Candidates) {
            for (line2 in line2Candidates) {
                val result = extractTd3Data(line1, line2, validateChecksum = false)
                if (result != null) return result
            }
        }

        for (line in mrzLines) {
            if (line.length >= 88) {
                val line1 = line.substring(0, 44)
                val line2 = line.substring(44, 88)
                val result = extractTd3Data(line1, line2, validateChecksum = false)
                if (result != null) return result
            }
        }

        return null
    }

    private fun isTd3Line1(line: String): Boolean {
        if (line.length < 40) return false

        val startsWithPassportType = line.matches(Regex("^P[<A-Z0][A-Z]{3}.*"))
        val hasNameSeparator = line.contains("<<")
        val notStartWithDigits = !line[0].isDigit()

        return startsWithPassportType && hasNameSeparator && notStartWithDigits
    }

    private fun isTd3Line2(line: String): Boolean {
        if (line.length < 40) return false

        val hasDocNumPattern = line.substring(0, 9).any { it.isLetterOrDigit() && it != '<' }
        val potentialDob = extractDateWithOcrCorrection(line, 13, 19)
        val potentialDoe = extractDateWithOcrCorrection(line, 21, 27)

        return hasDocNumPattern && potentialDob != null && potentialDoe != null
    }

    private fun extractTd3Data(line1: String, line2: String, validateChecksum: Boolean): ScannedMrzData? {
        if (line1.length < 44 || line2.length < 44) return null

        val namesField = line1.substring(5, 44)
        val nameParts = namesField.split("<<")
        val surname = nameParts.getOrNull(0)?.replace("<", " ")?.trim() ?: ""
        val givenNames = nameParts.getOrNull(1)?.replace("<", " ")?.trim() ?: ""

        val docNumRaw = line2.substring(0, 9).trimEnd('<').replace("<", "")
        val docNum = if (docNumRaw.isNotEmpty() && docNumRaw.length <= 9) docNumRaw else return null

        val docNumCheck = line2[9]
        val nationality = line2.substring(10, 13).replace("<", "")
        val dob = extractDateWithOcrCorrection(line2, 13, 19) ?: return null
        val dobCheck = line2[19]
        val sex = when (line2[20]) {
            'M', 'm' -> "M"
            'F', 'f' -> "F"
            else -> "<"
        }
        val doe = extractDateWithOcrCorrection(line2, 21, 27) ?: return null
        val doeCheck = line2[27]

        if (validateChecksum) {
            val checksumValid = validateTd3CheckDigits(
                line2.substring(0, 9), docNumCheck, dob, dobCheck, doe, doeCheck
            )
            if (!checksumValid) return null
        }

        return ScannedMrzData(
            documentNumber = docNum,
            dateOfBirth = dob,
            dateOfExpiry = doe,
            confidence = if (validateChecksum) 0.95f else 0.75f,
            checksumValid = validateChecksum,
            format = MrzFormat.TD3,
            surname = surname,
            givenNames = givenNames,
            nationality = nationality,
            sex = sex
        )
    }

    private fun validateTd3CheckDigits(
        docNum: String,
        docNumCheck: Char,
        dob: String,
        dobCheck: Char,
        doe: String,
        doeCheck: Char
    ): Boolean {
        val docNumPadded = docNum.padEnd(9, '<')
        val docNumValid = verifyCheckDigit(docNumPadded, docNumCheck)
        val dobValid = verifyCheckDigit(dob, dobCheck)
        val doeValid = verifyCheckDigit(doe, doeCheck)
        return docNumValid && dobValid && doeValid
    }

    private fun findMrzPatterns(text: String): ScannedMrzData? {
        val cleanText = text.uppercase().replace(" ", "")

        val mrzLikeLines = cleanText.split("\n")
            .map { it.replace(" ", "") }
            .filter { line ->
                line.length >= 25 && line.contains("<") && line.count { it == '<' } >= 3
            }

        for (line in mrzLikeLines) {
            val typeMatch = Regex("^[IAC]<[A-Z]{3}").find(line)
            if (typeMatch != null) {
                if (line.length >= 14) {
                    val docNum = line.substring(5, 14).trimEnd('<').replace("<", "")
                    if (docNum.isNotEmpty() && docNum.length <= 9) {
                        val dates = findDatesInText(cleanText)
                        if (dates.size >= 2) {
                            return ScannedMrzData(
                                documentNumber = docNum,
                                dateOfBirth = dates[0],
                                dateOfExpiry = dates[1],
                                confidence = 0.6f
                            )
                        }
                    }
                }
            }
        }

        return null
    }

    private fun findDatesInText(text: String): List<String> {
        val correctedText = text.map { char ->
            when (char) {
                'O', 'Q', 'D' -> '0'
                'I', 'L' -> '1'
                'S' -> '5'
                'B' -> '8'
                else -> char
            }
        }.joinToString("")

        val dateRegex = Regex("\\d{6}")
        return dateRegex.findAll(correctedText)
            .map { it.value }
            .filter { isValidMrzDate(it) }
            .toList()
    }

    private fun extractDateWithOcrCorrection(text: String, start: Int, end: Int): String? {
        if (text.length < end) return null

        val rawDate = text.substring(start, minOf(end, text.length))

        val corrected = rawDate.map { char ->
            when (char) {
                'O', 'o', 'Q', 'D' -> '0'
                'I', 'i', 'l', 'L', '|', '!' -> '1'
                'Z', 'z' -> '2'
                'E' -> '3'
                'A', 'h' -> '4'
                'S', 's' -> '5'
                'G', 'b' -> '6'
                'T' -> '7'
                'B' -> '8'
                'g', 'q' -> '9'
                else -> char
            }
        }.joinToString("")

        val dateStr = corrected.filter { it.isDigit() }.take(6)

        return if (dateStr.length == 6 && isValidMrzDate(dateStr)) dateStr else null
    }

    private fun isValidMrzDate(date: String): Boolean {
        if (date.length != 6) return false
        return try {
            val month = date.substring(2, 4).toInt()
            val day = date.substring(4, 6).toInt()
            month in 1..12 && day in 1..31
        } catch (e: Exception) {
            false
        }
    }

    private val checkDigitWeights = intArrayOf(7, 3, 1)

    private fun getMrzCharValue(char: Char): Int {
        return when {
            char.isDigit() -> char.digitToInt()
            char.isLetter() -> char.uppercaseChar() - 'A' + 10
            char == '<' -> 0
            else -> 0
        }
    }

    private fun calculateCheckDigit(data: String): Int {
        var sum = 0
        data.forEachIndexed { index, char ->
            val value = getMrzCharValue(char)
            val weight = checkDigitWeights[index % 3]
            sum += value * weight
        }
        return sum % 10
    }

    private fun verifyCheckDigit(data: String, checkDigit: Char): Boolean {
        if (!checkDigit.isDigit()) return false
        val expected = calculateCheckDigit(data)
        val actual = checkDigit.digitToInt()
        return expected == actual
    }

    private fun validateTd1CheckDigits(line1: String, line2: String): Boolean {
        if (line1.length < 15 || line2.length < 15) return false

        val docNum = line1.substring(5, 14)
        val docNumCheck = line1[14]
        val docNumValid = verifyCheckDigit(docNum, docNumCheck)

        val dob = line2.substring(0, 6)
        val dobCheck = line2[6]
        val dobValid = verifyCheckDigit(dob, dobCheck)

        val doe = line2.substring(8, 14)
        val doeCheck = line2[14]
        val doeValid = verifyCheckDigit(doe, doeCheck)

        return docNumValid && dobValid && doeValid
    }

    fun close() {
        recognizer.close()
    }
}
