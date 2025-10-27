@echo off
REM FIVUCSAS Mobile App - Initialization Script for Windows
REM This script will initialize the Flutter project in this directory

echo ========================================
echo FIVUCSAS Mobile App Setup
echo ========================================
echo.

REM Check if Flutter is installed
where flutter >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Flutter is not installed or not in PATH
    echo Please install Flutter from: https://docs.flutter.dev/get-started/install/windows
    echo Then add C:\src\flutter\bin to your PATH
    pause
    exit /b 1
)

echo [INFO] Flutter found: 
flutter --version
echo.

REM Check Flutter doctor
echo [INFO] Running Flutter doctor...
flutter doctor
echo.

echo [INFO] Initializing Flutter project in current directory...
flutter create --org com.fivucsas --project-name fivucsas_mobile .
echo.

echo [INFO] Getting dependencies...
flutter pub get
echo.

echo [INFO] Creating project structure...

REM Create lib directories
if not exist "lib\core\constants" mkdir "lib\core\constants"
if not exist "lib\core\theme" mkdir "lib\core\theme"
if not exist "lib\core\utils" mkdir "lib\core\utils"
if not exist "lib\core\errors" mkdir "lib\core\errors"
if not exist "lib\core\network" mkdir "lib\core\network"
if not exist "lib\core\config" mkdir "lib\core\config"

if not exist "lib\features\auth\data\models" mkdir "lib\features\auth\data\models"
if not exist "lib\features\auth\data\datasources" mkdir "lib\features\auth\data\datasources"
if not exist "lib\features\auth\data\repositories" mkdir "lib\features\auth\data\repositories"
if not exist "lib\features\auth\domain\entities" mkdir "lib\features\auth\domain\entities"
if not exist "lib\features\auth\domain\repositories" mkdir "lib\features\auth\domain\repositories"
if not exist "lib\features\auth\domain\usecases" mkdir "lib\features\auth\domain\usecases"
if not exist "lib\features\auth\presentation\bloc" mkdir "lib\features\auth\presentation\bloc"
if not exist "lib\features\auth\presentation\pages" mkdir "lib\features\auth\presentation\pages"
if not exist "lib\features\auth\presentation\widgets" mkdir "lib\features\auth\presentation\widgets"

if not exist "lib\features\biometric\data\models" mkdir "lib\features\biometric\data\models"
if not exist "lib\features\biometric\data\datasources" mkdir "lib\features\biometric\data\datasources"
if not exist "lib\features\biometric\data\repositories" mkdir "lib\features\biometric\data\repositories"
if not exist "lib\features\biometric\data\services" mkdir "lib\features\biometric\data\services"
if not exist "lib\features\biometric\domain\entities" mkdir "lib\features\biometric\domain\entities"
if not exist "lib\features\biometric\domain\repositories" mkdir "lib\features\biometric\domain\repositories"
if not exist "lib\features\biometric\domain\usecases" mkdir "lib\features\biometric\domain\usecases"
if not exist "lib\features\biometric\presentation\bloc" mkdir "lib\features\biometric\presentation\bloc"
if not exist "lib\features\biometric\presentation\pages" mkdir "lib\features\biometric\presentation\pages"
if not exist "lib\features\biometric\presentation\widgets" mkdir "lib\features\biometric\presentation\widgets"

if not exist "lib\features\home\presentation\pages" mkdir "lib\features\home\presentation\pages"
if not exist "lib\features\home\presentation\widgets" mkdir "lib\features\home\presentation\widgets"

if not exist "lib\features\profile\data\models" mkdir "lib\features\profile\data\models"
if not exist "lib\features\profile\data\datasources" mkdir "lib\features\profile\data\datasources"
if not exist "lib\features\profile\data\repositories" mkdir "lib\features\profile\data\repositories"
if not exist "lib\features\profile\domain\entities" mkdir "lib\features\profile\domain\entities"
if not exist "lib\features\profile\domain\repositories" mkdir "lib\features\profile\domain\repositories"
if not exist "lib\features\profile\domain\usecases" mkdir "lib\features\profile\domain\usecases"
if not exist "lib\features\profile\presentation\bloc" mkdir "lib\features\profile\presentation\bloc"
if not exist "lib\features\profile\presentation\pages" mkdir "lib\features\profile\presentation\pages"
if not exist "lib\features\profile\presentation\widgets" mkdir "lib\features\profile\presentation\widgets"

if not exist "lib\shared\widgets" mkdir "lib\shared\widgets"

REM Create assets directories
if not exist "assets\images" mkdir "assets\images"
if not exist "assets\icons" mkdir "assets\icons"
if not exist "assets\animations" mkdir "assets\animations"
if not exist "assets\fonts" mkdir "assets\fonts"

echo [SUCCESS] Project structure created!
echo.

echo [INFO] Creating .env file from .env.example...
if not exist ".env" (
    copy .env.example .env
    echo [SUCCESS] .env file created! Please configure your API endpoints.
) else (
    echo [INFO] .env file already exists, skipping...
)
echo.

echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Review and configure .env file with your API endpoints
echo 2. Follow QUICKSTART.md for detailed setup instructions
echo 3. Follow FLUTTER_APP_GUIDE.md for implementation guide
echo 4. Run 'flutter run' to start the app
echo.
echo To check for any issues, run: flutter doctor
echo.
pause
