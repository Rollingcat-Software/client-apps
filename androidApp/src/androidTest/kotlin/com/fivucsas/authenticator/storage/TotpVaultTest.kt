package com.fivucsas.authenticator.storage

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fivucsas.authenticator.totp.TotpAccount
import com.fivucsas.authenticator.totp.TotpAlgorithm
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class TotpVaultTest {

    private lateinit var vault: TotpVault

    @Before
    fun setUp() {
        vault = TotpVault(ApplicationProvider.getApplicationContext())
        vault.clear()
    }

    @After
    fun tearDown() {
        vault.clear()
    }

    private fun sample(id: String, issuer: String = "Acme", account: String = "alice") =
        TotpAccount(
            id = id,
            issuer = issuer,
            accountName = account,
            secretBase32 = "JBSWY3DPEHPK3PXP",
            algorithm = TotpAlgorithm.SHA1.canonical,
            digits = 6,
            period = 30,
            createdAt = 1L
        )

    @Test
    fun add_and_round_trip_preserves_account() {
        val account = sample("id-1")
        vault.add(account)
        val loaded = vault.getById("id-1")
        assertNotNull(loaded)
        assertEquals(account, loaded)
    }

    @Test
    fun delete_removes_account() {
        vault.add(sample("id-a"))
        vault.add(sample("id-b", issuer = "Other"))
        vault.delete("id-a")
        assertEquals(1, vault.getAll().size)
        assertNull(vault.getById("id-a"))
        assertNotNull(vault.getById("id-b"))
    }

    @Test
    fun rename_updates_issuer_and_account_name() {
        vault.add(sample("id-1"))
        vault.rename("id-1", "NewIssuer", "newAccount")
        val updated = vault.getById("id-1")
        assertNotNull(updated)
        assertEquals("NewIssuer", updated.issuer)
        assertEquals("newAccount", updated.accountName)
    }

    @Test
    fun add_with_duplicate_id_replaces_existing() {
        vault.add(sample("id-1", issuer = "First"))
        vault.add(sample("id-1", issuer = "Second"))
        assertEquals(1, vault.getAll().size)
        assertEquals("Second", vault.getById("id-1")?.issuer)
    }

    @Test
    fun encrypted_storage_persists_across_instances() {
        vault.add(sample("id-persist"))
        val second = TotpVault(ApplicationProvider.getApplicationContext())
        val loaded = second.getById("id-persist")
        assertNotNull(loaded)
        assertTrue(loaded.secretBase32.isNotBlank())
    }
}
