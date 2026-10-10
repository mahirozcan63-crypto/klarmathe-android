[CmdletBinding()]
param([switch]$CheckOnly)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$jdkCandidates = @(
  $env:JAVA_HOME,
  (Join-Path $env:ProgramFiles 'Android\Android Studio\jbr'),
  (Join-Path $env:LOCALAPPDATA 'Programs\Android Studio\jbr')
)
$jdk = $jdkCandidates | Where-Object { $_ -and (Test-Path -LiteralPath (Join-Path $_ 'bin\javac.exe')) } | Select-Object -First 1
if (-not $jdk) { throw 'JDK bulunamadi. Android Studio kurulumu bittikten sonra APK-OLUSTUR.cmd dosyasini tekrar acin: https://developer.android.com/studio' }
$env:JAVA_HOME = $jdk
$env:PATH = (Join-Path $jdk 'bin') + ';' + $env:PATH
$sdkCandidates = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, (Join-Path $env:LOCALAPPDATA 'Android\Sdk'))
$sdk = $sdkCandidates | Where-Object { $_ -and (Test-Path -LiteralPath (Join-Path $_ 'platforms\android-36\android.jar')) } | Select-Object -First 1
if (-not $sdk) { throw 'Android SDK Platform 36 eksik. Android Studio > SDK Manager > SDK Platforms icinde Android 16 / API 36 secin.' }
if (-not (Test-Path -LiteralPath (Join-Path $sdk 'build-tools\35.0.0\apksigner.bat'))) { throw 'SDK Build-Tools 35.0.0 eksik. SDK Manager > SDK Tools > Show Package Details icinden yukleyin.' }
$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk
if ($CheckOnly) { Write-Output 'JDK ve Android SDK bulundu.'; exit 0 }
$toolRoot = Join-Path $PSScriptRoot '.build-tools'
$gradleRoot = Join-Path $toolRoot 'gradle-8.13'
$gradleCmd = Join-Path $gradleRoot 'bin\gradle.bat'
if (-not (Test-Path -LiteralPath $gradleCmd)) {
  New-Item -ItemType Directory -Path $toolRoot -Force | Out-Null
  $archive = Join-Path $toolRoot 'gradle-8.13-bin.zip'
  Write-Output 'Ucretsiz Gradle 8.13 indiriliyor...'
  [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
  Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.13-bin.zip' -OutFile $archive -UseBasicParsing
  $hash = (Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant()
  if ($hash -ne '20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78') { throw 'Gradle indirmesi dogrulanamadi. Derleme durduruldu.' }
  Expand-Archive -LiteralPath $archive -DestinationPath $toolRoot -Force
}
$mathClasses = Join-Path $PSScriptRoot 'build\math-checks'
New-Item -ItemType Directory -Path $mathClasses -Force | Out-Null
& (Join-Path $jdk 'bin\javac.exe') -encoding UTF-8 -d $mathClasses 'app\src\main\java\de\klarlernen\app\SafeMath.java' 'tests\SafeMathTest.java'
if ($LASTEXITCODE -ne 0) { throw 'Matematik motoru derlenemedi.' }
& (Join-Path $jdk 'bin\java.exe') -cp $mathClasses de.klarlernen.app.SafeMathTest
if ($LASTEXITCODE -ne 0) { throw 'Matematik kontrolleri gecmedi.' }
& $gradleCmd --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
if ($LASTEXITCODE -ne 0) { throw 'Android derlemesi tamamlanmadi. Penceredeki ilk hatayi iletin.' }
$apk = Join-Path $PSScriptRoot 'app\build\outputs\apk\debug\app-debug.apk'
& (Join-Path $sdk 'build-tools\35.0.0\apksigner.bat') verify --verbose $apk
if ($LASTEXITCODE -ne 0) { throw 'APK imzasi dogrulanamadi.' }
$delivery = Join-Path $PSScriptRoot 'delivery'
New-Item -ItemType Directory -Path $delivery -Force | Out-Null
Copy-Item -LiteralPath $apk -Destination (Join-Path $delivery 'KlarLernen.apk') -Force
Write-Output ('APK hazir: ' + (Join-Path $delivery 'KlarLernen.apk'))
Write-Output 'Bu cihaz deneme surumudur; Play Store yayini degildir.'
