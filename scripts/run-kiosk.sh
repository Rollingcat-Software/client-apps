#!/usr/bin/env bash
# FIVUCSAS Desktop Kiosk Mode Launcher
#
# Usage:
#   ./scripts/run-kiosk.sh                          # default admin password
#   ./scripts/run-kiosk.sh --kiosk-password=MyPass   # custom exit password
#
# Starts the desktop app in fullscreen kiosk mode for enrollment stations.
# The window is undecorated (no title bar) and fullscreen.
# Closing requires entering the admin password.

set -euo pipefail
cd "$(dirname "$0")/.."

EXTRA_ARGS=("$@")

echo "=== FIVUCSAS Desktop Kiosk Mode ==="
echo "Starting enrollment station..."
echo ""
echo "To exit kiosk mode, use Alt+F4 or window close and enter the admin password."
echo "Default password: fivucsas-admin (override with --kiosk-password=<password>)"
echo ""

exec ./gradlew :desktopApp:run --args="--kiosk ${EXTRA_ARGS[*]:-}" --no-daemon
