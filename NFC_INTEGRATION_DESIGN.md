# NFC Integration Design Document

## Overview

Integration of the UniversalNfcReader library into the FIVUCSAS client-apps (Kotlin Multiplatform) for reading NFC-based identity documents and cards.

## Supported Card Types

- Turkish eID (TC Kimlik Karti) - BAC authentication with MRZ
- e-Passports (ICAO 9303 TD3) - BAC authentication with MRZ
- Istanbulkart (transport card) - public data only
- MIFARE Classic 1K/4K - default key authentication
- MIFARE DESFire - public data
- MIFARE Ultralight / NTAG213/215/216
- NDEF formatted tags
- ISO 15693 (NfcV) tags
- FeliCa cards
- Generic NFC-A/NFC-B cards

## Architecture

Follows the existing hexagonal architecture with platform abstractions.

```
shared/commonMain/
  domain/model/NfcCardData.kt       -- Platform-independent data models
  platform/INfcService.kt           -- Interface (NfcScanState, MrzInputData)

shared/androidMain/
  di/PlatformModule.android.kt      -- Koin binding: INfcService -> AndroidNfcService

androidApp/
  data/nfc/
    AndroidNfcService.kt            -- INfcService implementation
    NfcCardReadingService.kt        -- Orchestrator (card detect -> reader select -> read)
    CardReaderFactory.kt            -- Factory for card-type-specific readers
    detector/
      CardDetector.kt               -- Interface for card type detection
      UniversalCardDetector.kt       -- AID probing, tech list analysis
    eid/
      BacAuthentication.kt          -- BAC key derivation & mutual auth (3DES)
      SecureMessaging.kt            -- Encrypted APDU channel (3DES + ISO 9797 MAC)
      EidApduHelper.kt             -- ICAO APDU command builder/parser
      Dg1Parser.kt                 -- DG1 (MRZ) ASN.1 parser
      Dg2Parser.kt                 -- DG2 (Photo) JPEG/JP2K extractor
      MrzParser.kt                 -- TD1/TD3 MRZ string parser
    reader/
      CardReader.kt                -- Base reader interface
      PassportNfcReader.kt          -- e-Passport reader (BAC + SM + DG1/DG2/SOD)
      TurkishEidReader.kt           -- Turkish eID reader (BAC + SM + DG1/DG2)
      MifareDesfireReader.kt        -- DESFire/Istanbulkart reader
      MifareClassicReader.kt        -- Mifare Classic with default keys
      MifareUltralightReader.kt     -- Ultralight/NTAG reader
      NdefReader.kt                 -- NDEF formatted tag reader
      GenericCardReader.kt          -- Fallback for unknown cards
    security/
      SecureByteArray.kt            -- Secure memory management
      SecureLogger.kt               -- PII-redacting logger
      sod/
        SodValidator.kt             -- CMS SignedData validator (BouncyCastle)
        HashVerifier.kt             -- DG hash verification
        LdsSecurityObjectParser.kt   -- ASN.1 LDS security object
        CscaCertificateStore.kt      -- CSCA trust anchor management
    model/
      CardData.kt                   -- Android-specific card data models (with Bitmap)
      CardType.kt                   -- Card type enum
      CardError.kt                  -- Error types
      AuthenticationData.kt         -- MRZ/PIN/Key auth credentials
      Result.kt                     -- Result monad
    util/
      Constants.kt                  -- AIDs, commands, timeouts
      Extensions.kt                 -- ByteArray extensions
  ui/screen/
    NfcReadScreen.kt               -- Compose UI (MRZ input + scan + results)
```

## NFC Read Flow

1. User opens NFC Reader screen
2. (Optional) User enters MRZ data: document number, date of birth, date of expiry
3. User taps "Scan with MRZ" or "Scan Any NFC Card"
4. App starts NFC foreground dispatch
5. User holds card against phone back
6. Card detected -> UniversalCardDetector identifies type
7. CardReaderFactory selects appropriate reader
8. If identity doc: BAC authentication -> Secure Messaging -> Read DG1/DG2/SOD
9. Result displayed: personal data, photo, security validation status

## Dependencies

- BouncyCastle (bcprov-jdk18on, bcpkix-jdk18on) - for SOD/CMS signature validation
- Android NFC APIs (android.nfc.tech.*)
- javax.crypto / java.security - for 3DES, SHA-1, DES

## Security Considerations

- All cryptographic key material wrapped in SecureByteArray (zeroed on close)
- Constant-time comparisons for all MAC/hash verification
- SecureRandom for all nonce generation
- PII auto-redacted in logs via SecureLogger
- Session keys cleared in finally blocks
- MRZ data cleared after authentication completes

## Platform Support

- **Android**: Full support (this integration)
- **iOS**: Future - would use Core NFC framework (NFCTagReaderSession)
- **Desktop**: Future - would use javax.smartcardio (PC/SC)
