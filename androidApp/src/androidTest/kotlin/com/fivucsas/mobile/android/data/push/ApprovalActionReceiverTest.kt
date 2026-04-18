package com.fivucsas.mobile.android.data.push

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fivucsas.shared.domain.repository.NfcApprovalRepository
import com.fivucsas.shared.presentation.state.ApprovalDecision
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Instrumented unit tests for ApprovalActionReceiver.
 *
 * Koin is started with a tiny module that provides a FakeNfcApprovalRepository
 * so the receiver's network call is captured rather than executed. We then
 * invoke onReceive directly (no real IntentFilter wiring) and verify:
 *   - Allow → repository called with ApprovalDecision.ALLOW for the given sid
 *   - Deny  → repository called with ApprovalDecision.DENY
 *   - Unknown decision string → repository NOT called
 *   - Missing session id → repository NOT called
 *
 * The receiver kicks work onto Dispatchers.IO via goAsync(); we busy-wait on
 * the fake's recorded-call list with a short timeout to avoid flaky races.
 */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ApprovalActionReceiverTest {

    private lateinit var context: Context
    private lateinit var repository: FakeNfcApprovalRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = FakeNfcApprovalRepository()

        // FIVUCSASApplication already started Koin during test-app launch with
        // the real network-backed NfcApprovalRepository. Restart with just the
        // fake so the receiver's KoinComponent lookup picks up our test double.
        if (GlobalContext.getKoinApplicationOrNull() != null) {
            stopKoin()
        }
        startKoin {
            modules(
                module {
                    single<NfcApprovalRepository> { repository }
                }
            )
        }
    }

    @After
    fun tearDown() {
        if (GlobalContext.getKoinApplicationOrNull() != null) {
            stopKoin()
        }
    }

    @Test
    fun onReceive_allowDecision_postsAllowToRepository() {
        val intent = buildIntent(
            sessionId = "sess-allow-1",
            decision = ApprovalActionReceiver.DECISION_ALLOW,
            notificationId = 42
        )

        ApprovalActionReceiver().onReceive(context, intent)

        val recorded = waitForCall()
        assertEquals(1, recorded.size, "Expected exactly one repository call")
        assertEquals("sess-allow-1" to ApprovalDecision.ALLOW, recorded.first())
    }

    @Test
    fun onReceive_denyDecision_postsDenyToRepository() {
        val intent = buildIntent(
            sessionId = "sess-deny-1",
            decision = ApprovalActionReceiver.DECISION_DENY,
            notificationId = 7
        )

        ApprovalActionReceiver().onReceive(context, intent)

        val recorded = waitForCall()
        assertEquals(1, recorded.size)
        assertEquals("sess-deny-1" to ApprovalDecision.DENY, recorded.first())
    }

    @Test
    fun onReceive_unknownDecision_doesNotCallRepository() {
        val intent = buildIntent(
            sessionId = "sess-x",
            decision = "maybe",
            notificationId = 1
        )

        ApprovalActionReceiver().onReceive(context, intent)

        // Give any async path a short window to run — it must NOT fire.
        sleepQuiet(500)
        assertTrue(repository.callsSnapshot().isEmpty(), "Repository must not be called for unknown decision")
    }

    @Test
    fun onReceive_missingSessionId_doesNotCallRepository() {
        val intent = Intent().apply {
            putExtra(ApprovalActionReceiver.EXTRA_DECISION, ApprovalActionReceiver.DECISION_ALLOW)
            putExtra(ApprovalActionReceiver.EXTRA_NOTIFICATION_ID, 99)
        }

        ApprovalActionReceiver().onReceive(context, intent)

        sleepQuiet(500)
        assertTrue(repository.callsSnapshot().isEmpty(), "Repository must not be called without session id")
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun buildIntent(sessionId: String, decision: String, notificationId: Int): Intent =
        Intent().apply {
            putExtra(ApprovalActionReceiver.EXTRA_SESSION_ID, sessionId)
            putExtra(ApprovalActionReceiver.EXTRA_DECISION, decision)
            putExtra(ApprovalActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }

    /**
     * Polls up to 2 seconds for the fake repo to record at least one call,
     * because the receiver dispatches onto Dispatchers.IO via goAsync().
     */
    private fun waitForCall(timeoutMs: Long = 2_000): List<Pair<String, ApprovalDecision>> = runBlocking {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val snap = repository.callsSnapshot()
            if (snap.isNotEmpty()) return@runBlocking snap
            delay(25)
        }
        repository.callsSnapshot()
    }

    private fun sleepQuiet(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (_: InterruptedException) {
            // ignore
        }
    }
}

/**
 * Thread-safe fake repo. The receiver dispatches on Dispatchers.IO; the test
 * reads from the main thread, so we guard the list with a Mutex.
 */
private class FakeNfcApprovalRepository : NfcApprovalRepository {
    private val lock = Mutex()
    private val calls: MutableList<Pair<String, ApprovalDecision>> = mutableListOf()

    override suspend fun submitDecision(
        sessionId: String,
        decision: ApprovalDecision
    ): Result<Unit> {
        lock.withLock { calls.add(sessionId to decision) }
        return Result.success(Unit)
    }

    fun callsSnapshot(): List<Pair<String, ApprovalDecision>> = runBlocking {
        lock.withLock { calls.toList() }
    }
}
