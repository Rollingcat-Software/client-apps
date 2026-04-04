package com.fivucsas.shared.platform

/**
 * File Saver Interface (UI Port)
 *
 * Platform abstraction for saving files to the local filesystem.
 * Each platform implements this differently:
 * - Android: writes to Downloads directory
 * - Desktop: opens JFileChooser save dialog
 * - iOS: presents UIActivityViewController (share sheet)
 *
 * Design Principles Applied:
 * - Dependency Inversion Principle (DIP): Domain depends on abstraction
 * - Interface Segregation Principle (ISP): Focused interface for file saving
 * - Single Responsibility Principle (SRP): Only handles file save operations
 *
 * Hexagonal Architecture Role: PORT (Primary/Driving Port)
 */
interface IFileSaver {

    /**
     * Save text content to a file.
     *
     * @param content The text content to save
     * @param suggestedFileName Suggested file name (e.g., "audit_log_2026-04-04.csv")
     * @param mimeType MIME type of the content (e.g., "text/csv")
     * @return Result with the saved file path on success, or an error
     */
    suspend fun saveTextFile(
        content: String,
        suggestedFileName: String,
        mimeType: String = "text/csv"
    ): Result<String>
}
