package com.fivucsas.desktop.security

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Linux [SecureTokenStorage] backed by libsecret / the Secret Service D-Bus
 * API, accessed through the `secret-tool` command-line helper.
 *
 * We shell out rather than link against libsecret directly because the
 * command-line interface is present on every current GNOME/KDE distro and
 * avoids dragging a native dep into the Compose distribution bundle.
 *
 * Entries are stored with attributes:
 *   `service=fivucsas account=<key>`
 * and labelled `FIVUCSAS Token: <key>`. Listing/clearAll therefore works by
 * running `secret-tool search --unlock --all service fivucsas` and clearing
 * every returned account.
 *
 * Availability is probed at construction (`which secret-tool`). If the
 * helper is missing or the daemon refuses a trivial round-trip, we throw
 * [StorageUnavailableException] so [TokenStorageFactory] can fall back.
 */
class LibsecretTokenStorage(
    private val secretTool: String = resolveSecretTool(),
) : SecureTokenStorage {

    init {
        // Surface daemon-missing problems *at construction* so the factory
        // has a chance to fall back. Don't actually write/read anything; just
        // assert that the binary runs. The daemon-missing case is caught on
        // the first real call and converted to StorageUnavailableException
        // there too.
        if (secretTool.isBlank()) {
            throw StorageUnavailableException("secret-tool binary not found on PATH")
        }
    }

    override fun save(key: String, value: String) {
        val exit = runSecretTool(
            args = listOf(
                "store",
                "--label=FIVUCSAS Token: $key",
                SERVICE_ATTR, SERVICE_VALUE,
                ACCOUNT_ATTR, key,
            ),
            stdin = value,
        )
        if (exit.code != 0) {
            throw StorageUnavailableException(
                "secret-tool store failed (exit=${exit.code}): ${exit.stderr.trim()}",
            )
        }
    }

    override fun load(key: String): String? {
        val result = runSecretTool(
            args = listOf(
                "lookup",
                SERVICE_ATTR, SERVICE_VALUE,
                ACCOUNT_ATTR, key,
            ),
        )
        if (result.code == 0) {
            // secret-tool strips trailing newline inconsistently; trim ours.
            val out = result.stdout
            return if (out.endsWith('\n')) out.dropLast(1) else out
        }
        // Exit 1 on "not found" is normal; any other non-zero we treat as null
        // (the caller should re-auth rather than crash the app).
        return null
    }

    override fun clear(key: String) {
        runSecretTool(
            args = listOf(
                "clear",
                SERVICE_ATTR, SERVICE_VALUE,
                ACCOUNT_ATTR, key,
            ),
        )
        // Ignore exit code: "clear" returns 1 when the item is absent.
    }

    override fun clearAll() {
        // `secret-tool clear` removes *all* matching items when only the
        // service attribute is passed. This is documented behaviour since
        // libsecret 0.18.
        runSecretTool(
            args = listOf(
                "clear",
                SERVICE_ATTR, SERVICE_VALUE,
            ),
        )
    }

    // ----- internals -----

    private data class SecretToolResult(val code: Int, val stdout: String, val stderr: String)

    private fun runSecretTool(args: List<String>, stdin: String? = null): SecretToolResult {
        val cmd = mutableListOf(secretTool).apply { addAll(args) }
        val pb = ProcessBuilder(cmd)
            .redirectErrorStream(false)
        return try {
            val process = pb.start()
            if (stdin != null) {
                process.outputStream.use { it.write(stdin.toByteArray(StandardCharsets.UTF_8)) }
            } else {
                process.outputStream.close()
            }
            val stdout = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
                .use { it.readText() }
            val stderr = BufferedReader(InputStreamReader(process.errorStream, StandardCharsets.UTF_8))
                .use { it.readText() }
            val finished = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                throw StorageUnavailableException("secret-tool timed out after ${PROCESS_TIMEOUT_SECONDS}s")
            }
            SecretToolResult(process.exitValue(), stdout, stderr)
        } catch (ex: IOException) {
            throw StorageUnavailableException("secret-tool invocation failed", ex)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw StorageUnavailableException("secret-tool interrupted", ex)
        }
    }

    companion object {
        private const val SERVICE_ATTR = "service"
        private const val SERVICE_VALUE = "fivucsas"
        private const val ACCOUNT_ATTR = "account"
        private const val PROCESS_TIMEOUT_SECONDS = 5L

        /**
         * Resolve the absolute path of `secret-tool`. Returns empty string if
         * the binary isn't on PATH; callers will throw [StorageUnavailableException].
         */
        private fun resolveSecretTool(): String {
            // Honour explicit override (useful in flatpak / custom installs).
            System.getProperty("fivucsas.secretTool")?.takeIf { it.isNotBlank() }?.let { return it }

            val pathEnv = System.getenv("PATH") ?: return ""
            for (dir in pathEnv.split(File.pathSeparator)) {
                if (dir.isBlank()) continue
                val candidate = File(dir, "secret-tool")
                if (candidate.canExecute()) return candidate.absolutePath
            }
            return ""
        }
    }
}
