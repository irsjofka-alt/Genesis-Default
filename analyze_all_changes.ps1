# PowerShell script to analyze ALL OpenRewrite changes
Write-Host "=== COMPREHENSIVE OPENREWRITE ANALYSIS ==="
Write-Host ""

# Get all Java files changed
$javaFiles = @()
$output = git diff HEAD~1 --name-only
foreach ($line in $output -split "`n") {
    if ($line -match '\.java$') {
        $javaFiles += $line
    }
}

Write-Host "Total Java files changed: $($javaFiles.Count)"
Write-Host ""

# Categories
$mainMethodChanges = @()
$exceptionVarChanges = @()
$otherChanges = @()
$potentialLogicIssues = @()

$counter = 0
foreach ($file in $javaFiles) {
    $counter++
    Write-Host "Analyzing file $counter/$($javaFiles.Count) : $file" -ForegroundColor Cyan
    
    $diff = git diff HEAD~1 -- "$file"
    
    # Check for main method changes
    if ($diff -match 'void main\(\)') {
        $mainMethodChanges += $file
        Write-Host "  ⚠ Main method changed" -ForegroundColor Yellow
    }
    
    # Check for exception variable changes
    $exceptionCount = [regex]::Matches($diff, 'catch.*final.*Exception.*_').Count
    $exceptionCount += [regex]::Matches($diff, 'catch.*final.*InterruptedException.*_').Count
    if ($exceptionCount -gt 0) {
        $exceptionVarChanges += "$file ($exceptionCount changes)"
        Write-Host "  ⚠ $exceptionCount exception variables changed" -ForegroundColor Yellow
    }
    
    # Check for other changes (excluding main and exception)
    $totalChanges = ($diff | Select-String -Pattern '^[+-]' | Measure-Object).Count
    $mainAndExceptionChanges = ($diff | Select-String -Pattern 'void main\(\)|catch.*final.*Exception.*_|catch.*final.*InterruptedException.*_' | Measure-Object).Count
    
    if ($totalChanges -gt $mainAndExceptionChanges) {
        $otherChanges += "$file ($($totalChanges - $mainAndExceptionChanges) other changes)"
        Write-Host "  ❗ $($totalChanges - $mainAndExceptionChanges) other changes found!" -ForegroundColor Red
        
        # Get the actual other changes
        $otherDiff = $diff | Select-String -Pattern '^[+-]' | Where-Object { $_ -notmatch 'void main\(\)' -and $_ -notmatch 'catch.*final.*Exception.*_' -and $_ -notmatch 'catch.*final.*InterruptedException.*_' }
        $potentialLogicIssues += "$file : $($otherDiff -join '; ')"
    }
    
    Write-Host ""
}

# Print summary
Write-Host "=== SUMMARY ===" -ForegroundColor Cyan
Write-Host ""

Write-Host "MAIN METHOD CHANGES ($($mainMethodChanges.Count)):" -ForegroundColor Red
foreach ($file in $mainMethodChanges) {
    Write-Host "  $file" -ForegroundColor Red
}

Write-Host ""
Write-Host "EXCEPTION VARIABLE CHANGES ($($exceptionVarChanges.Count)):" -ForegroundColor Yellow
Write-Host "  (First 10 files shown)"
for ($i = 0; $i -lt [Math]::Min(10, $exceptionVarChanges.Count); $i++) {
    Write-Host "  $($exceptionVarChanges[$i])" -ForegroundColor Yellow
}
if ($exceptionVarChanges.Count -gt 10) {
    Write-Host "  ... and $($exceptionVarChanges.Count - 10) more" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "OTHER CHANGES ($($otherChanges.Count)):" -ForegroundColor Magenta
if ($otherChanges.Count -gt 0) {
    Write-Host "  WARNING: Found files with changes beyond main/exception!" -ForegroundColor Red
    foreach ($change in $otherChanges) {
        Write-Host "  $change" -ForegroundColor Magenta
    }
    
    Write-Host ""
    Write-Host "POTENTIAL LOGIC ISSUES:" -ForegroundColor Red
    foreach ($issue in $potentialLogicIssues) {
        Write-Host "  $issue" -ForegroundColor Red
    }
} else {
    Write-Host "  No other changes found (good!)" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== RECOMMENDATIONS ===" -ForegroundColor Cyan
if ($mainMethodChanges.Count -gt 0) {
    Write-Host "1. Fix $($mainMethodChanges.Count) main method changes FIRST" -ForegroundColor Red
}
if ($otherChanges.Count -gt 0) {
    Write-Host "2. Review $($otherChanges.Count) files with other changes (potential logic issues)" -ForegroundColor Red
}
Write-Host "3. Exception variable changes ($($exceptionVarChanges.Count)) are safe (cosmetic)" -ForegroundColor Green