package com.fivucsas.shared.attestation

/*
 * TODO(attested-puzzle-capture): Android capture side of the attested puzzle
 * binding. NOT implemented yet: the app currently has no server-session puzzle,
 * no face-mesh landmarks and no on-device face embedding (see identity-core-api
 * docs/ATTESTED_PUZZLE_BINDING.md §9). What exists today:
 *
 *   - FaceLivenessScreen / LivenessViewModel: a local challenge list advanced by
 *     the UI and one JPEG uploaded to passive liveness (not usable for binding);
 *   - CameraX + ML Kit face detection (no MediaPipe face mesh, no Facenet,
 *     no ONNX Runtime / TFLite).
 *
 * Implemented and ready for the capture UI to call:
 *
 *   - PuzzleSessionApi.create / submitChallenge / verifyStep (wire contract),
 *   - PuzzleTraceDigest (bio-identical canonical digest, golden-vector tested),
 *   - AttestedBindingPayload (identity-identical, golden-vector tested),
 *   - AttestedPuzzleBinder (sign + assemble the MFA step data),
 *   - AttestedKeyManager (androidMain; AndroidKeyStore TEE/StrongBox key with
 *     setAttestationChallenge, one signature, then deleted).
 *
 * Capture design (to build):
 *
 *   1. CREATE via PuzzleSessionApi.create(sessionToken); require
 *      trace_contract == "v2" and an attestation_challenge.
 *   2. CameraX ImageAnalysis (front camera, YUV -> RGB, no mirroring of the
 *      analysed frame) feeding MediaPipe Tasks FaceLandmarker (478 points,
 *      LIVE_STREAM) and HandLandmarker for hand challenges, sampled at <= 25 fps.
 *   3. Port web PuzzleTraceRecorder: 32 landmarks in the server's
 *      trace_face_indices order, ints in 1e-4 units (x, y normalised to the frame,
 *      z as MediaPipe gives it), rolling 4 s window, identical-frame dedupe, the
 *      15 ms minimum frame interval, the return-to-centre tail for turn/look.
 *   4. Port web BiometricPuzzle gates (baseline-relative, 1 s warm-up, >= 2
 *      reversals for nod/shake). Validate against the shared golden contract traces
 *      (web puzzleContract.test.ts / bio test_puzzle_contract.py) so that a client
 *      "completed" always implies a server accept.
 *   5. SUBMIT each completed challenge; keep (action, trace) only for
 *      verified:true responses, in issued order -> AcceptedPuzzleChallenge list.
 *   6. Pick the best frontal frame of the same session; compute Facenet512 with
 *      ONNX Runtime Android using the SAME FP16 model and preprocessing as the web
 *      (eye-aligned crop, aspect-preserving resize, centre pad 160x160, BGR, [0,1],
 *      L2-normalise); record its trace timestamp + 96 ints as the BindingFrame.
 *   7. Widen the FloatArray embedding to List<Double> ONCE, then
 *      AttestedPuzzleBinder.buildStepData(...) and PuzzleSessionApi.verifyStep(...).
 *
 * The camera frames, landmarks and embedding never leave the device except as the
 * v2 traces and the 512-d vector the web client already sends.
 */
internal object AttestedPuzzleCaptureDesignNote
