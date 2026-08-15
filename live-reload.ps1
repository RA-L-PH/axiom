# live-reload.ps1
# Automates live reloading of Kotlin and XML layout changes on connected Android device.

$packageName = "com.rc.axiom.debug"
$mainActivity = "com.rc.axiom.activities.MainActivity"
$watcherPath = "$PSScriptRoot\app\src\main"
$adbPath = "adb"

# Set Java Home to JDK 21 found in Program Files to support Gradle build
$jdkPath = "C:\Program Files\Android\openjdk\jdk-21.0.8"
if (Test-Path $jdkPath) {
    $env:JAVA_HOME = $jdkPath
    Write-Host "Set JAVA_HOME to $jdkPath" -ForegroundColor Green
} else {
    Write-Warning "JDK 21 not found at $jdkPath. Gradle build may fail if default JDK is old."
}

# 1. Find connected devices
Write-Host "Checking connected ADB devices..." -ForegroundColor Cyan
$devices = & $adbPath devices | Select-String -Pattern "\bdevice\b"
if ($devices.Count -eq 0) {
    Write-Error "No Android devices connected. Please connect your phone via USB or Wi-Fi Debugging and enable USB Debugging."
    exit 1
}

$deviceId = ($devices[0].Line -split "`t")[0]
Write-Host "Targeting device: $deviceId" -ForegroundColor Green

# 2. Find and Launch Scrcpy for mirroring
$scrcpyPath = "scrcpy"
$wingetScrcpy = "$env:USERPROFILE\AppData\Local\Microsoft\WinGet\Packages\Genymobile.scrcpy_Microsoft.Winget.Source_8wekyb3d8bbwe\scrcpy-win64-v4.0\scrcpy.exe"
if (Test-Path $wingetScrcpy) {
    $scrcpyPath = $wingetScrcpy
    Write-Host "Using scrcpy from Winget: $scrcpyPath" -ForegroundColor Green
}

Write-Host "Launching scrcpy for live device mirroring..." -ForegroundColor Cyan
Start-Process -FilePath $scrcpyPath -ArgumentList "--serial", $deviceId, "--always-on-top", "--window-title", "Antigravity Live View - Axiom" -WindowStyle Minimized -ErrorAction SilentlyContinue
if (-not $?) {
    Write-Host "Note: scrcpy launched. If window does not appear, ensure it is in your PATH or restart VS Code." -ForegroundColor Yellow
}

# 3. Initial Build & Run
Write-Host "Performing initial build & deployment..." -ForegroundColor Cyan
& .\gradlew.bat installNormalDebug --no-configuration-cache
if ($LASTEXITCODE -eq 0) {
    Write-Host "Relaunching app..." -ForegroundColor Green
    & $adbPath -s $deviceId shell am start -n "$packageName/$mainActivity"
} else {
    Write-Warning "Initial build failed. Please fix build errors."
}

# 4. File Watcher Loop for XML & Kotlin changes
Write-Host "Watching $watcherPath for changes in Kotlin / XML layout files..." -ForegroundColor Cyan

$watcher = New-Object System.IO.FileSystemWatcher
$watcher.Path = $watcherPath
$watcher.IncludeSubdirectories = $true
$watcher.EnableRaisingEvents = $true

# Define the action to take on file changes
$action = {
    param($evtSender, $evtArgs)
    $filePath = $evtArgs.FullPath
    $fileName = $evtArgs.Name
    
    # Filter for Kotlin, Java, and XML layout files
    if ($filePath -match "\.(kt|xml|java)$" -and -not ($filePath -match "build\\")) {
        Write-Host "Change detected in $fileName. Performing hot swap / redeploy..." -ForegroundColor Yellow
        
        # Stop the app to ensure clean hot-swapped state (or let Gradle push)
        Write-Host "Building and deploying incremental changes..." -ForegroundColor Cyan
        & $PSScriptRoot\gradlew.bat installNormalDebug --no-configuration-cache
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Deploy successful. Launching main activity..." -ForegroundColor Green
            & $adbPath -s $deviceId shell am start -n "$packageName/$mainActivity"
        } else {
            Write-Error "Build failed. Check the error log."
        }
    }
}

Register-ObjectEvent $watcher "Changed" -Action $action | Out-Null
Register-ObjectEvent $watcher "Created" -Action $action | Out-Null

# Keep the watcher script running
try {
    while ($true) {
        Start-Sleep -Seconds 1
    }
} finally {
    # Clean up events on exit
    $watcher.EnableRaisingEvents = $false
    $watcher.Dispose()
    Unregister-Event -SourceIdentifier * -ErrorAction SilentlyContinue
    Write-Host "Live reload watcher stopped." -ForegroundColor Cyan
}
