# Build script for ACTIVESEGMENTATION — excludes test files
$ErrorActionPreference = "Continue"

$FIJI_DIR     = "C:\Users\prasa\Downloads\fiji-latest-win64-jdk\Fiji"
$JAVA_HOME    = "$FIJI_DIR\java\win64\zulu21.42.19-ca-jdk21.0.7-win_x64"
$JAVAC        = "$JAVA_HOME\bin\javac.exe"
$JAR_TOOL     = "$JAVA_HOME\bin\jar.exe"
$PROJECT_DIR  = "c:\Users\prasa\Downloads\ImageJ Active Segmentation platform - GSOC\ACTIVESEGMENTATION"
$SRC_DIR      = "$PROJECT_DIR\src"
$RES_DIR      = "$PROJECT_DIR\resources"
$JARS_DIR     = "$PROJECT_DIR\jars"
$OUT_DIR      = "$PROJECT_DIR\build_out"
$OUTPUT_JAR   = "$PROJECT_DIR\ACTIVESEGMENTATION.jar"

Write-Host "=== ACTIVESEGMENTATION Build Script ===" -ForegroundColor Cyan

# Build classpath with forward slashes
$projectJars = Get-ChildItem "$JARS_DIR" -Filter "*.jar" -Recurse | ForEach-Object { $_.FullName.Replace('\','/') }
$ijJar = (Get-ChildItem "$FIJI_DIR\jars" -Filter "ij-*.jar" | Select-Object -First 1).FullName.Replace('\','/')
$fxJars = Get-ChildItem "$FIJI_DIR\jars" -Filter "javafx*.jar" -Recurse | ForEach-Object { $_.FullName.Replace('\','/') }
$allCpItems = $projectJars + @($ijJar) + $fxJars
$classpath = ($allCpItems -join ";").Replace('\','/')

Write-Host "Classpath: $($allCpItems.Count) JARs"

# Clean
if (Test-Path $OUT_DIR) { Remove-Item $OUT_DIR -Recurse -Force }
New-Item $OUT_DIR -ItemType Directory -Force | Out-Null

# Find sources — EXCLUDE test directory
$javaFiles = Get-ChildItem "$SRC_DIR" -Filter "*.java" -Recurse | Where-Object { $_.FullName -notmatch "\\test\\" } | ForEach-Object { $_.FullName.Replace('\','/') }
Write-Host "Found $($javaFiles.Count) Java source files (excluding tests)"

# Write argfile
$argFile = "$PROJECT_DIR\javac_args.txt"
$outFwd = $OUT_DIR.Replace('\','/')
$srcFwd = $SRC_DIR.Replace('\','/')

$lines = @()
$lines += "-d"
$lines += """$outFwd"""
$lines += "-cp"
$lines += """$classpath"""
$lines += "-sourcepath"
$lines += """$srcFwd"""
$lines += "-Xlint:none"
$lines += "-encoding"
$lines += "UTF-8"
foreach ($f in $javaFiles) {
    $lines += """$f"""
}
[System.IO.File]::WriteAllLines($argFile, $lines, [System.Text.Encoding]::ASCII)

Write-Host "Argfile: $($lines.Count) lines"

# Compile
Write-Host "`nCompiling..." -ForegroundColor Yellow
& $JAVAC "@$argFile" 2>&1 | ForEach-Object { Write-Host $_ }

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n[OK] Compilation successful!" -ForegroundColor Green
    
    # Copy resources
    if (Test-Path $RES_DIR) {
        Copy-Item "$RES_DIR\*" $OUT_DIR -Recurse -Force -ErrorAction SilentlyContinue
    }
    Copy-Item "$PROJECT_DIR\plugins.config" $OUT_DIR -Force -ErrorAction SilentlyContinue

    # Create JAR
    Write-Host "Creating JAR..."
    & $JAR_TOOL cf $OUTPUT_JAR -C $OUT_DIR .
    
    $jarSize = [math]::Round((Get-Item $OUTPUT_JAR).Length / 1KB)
    Write-Host "[OK] JAR: $OUTPUT_JAR ($jarSize KB)" -ForegroundColor Green
    
    # Install to Fiji
    Copy-Item $OUTPUT_JAR "$FIJI_DIR\plugins\ACTIVESEGMENTATION.jar" -Force
    Write-Host "[OK] Installed to Fiji plugins!" -ForegroundColor Green
    
    Write-Host "`n=== READY TO RUN ===" -ForegroundColor Cyan
    Write-Host "Launch Fiji: $FIJI_DIR\ImageJ-win64.exe"
    Write-Host "Then go to: Plugins > Segmentation > Active Segmentation"
} else {
    Write-Host "`n[FAIL] Compilation failed." -ForegroundColor Red
}
