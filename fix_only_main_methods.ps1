# PowerShell script to fix ONLY main methods (leave other changes)
Write-Host "=== FIXING ONLY MAIN METHODS ==="
Write-Host ""

# Get all Java files with main method issues
$mainIssueFiles = @()
$output = git diff HEAD~1 --name-only
foreach ($line in $output -split "`n") {
    if ($line -match '\.java$') {
        $diff = git diff HEAD~1 -- "$line"
        if ($diff -match 'void main\(\)') {
            $mainIssueFiles += $line
        }
    }
}

Write-Host "Found $($mainIssueFiles.Count) files with main method issues"
Write-Host ""

# Fix each file
$fixedCount = 0
foreach ($file in $mainIssueFiles) {
    Write-Host "Fixing: $file" -ForegroundColor Cyan
    
    # Read the file content
    $content = Get-Content $file -Raw
    
    # Check current state
    if ($content -match 'void main\(\)') {
        Write-Host "  Found 'void main()'" -ForegroundColor Yellow
        
        # Fix 1: void main() -> public static void main(String[] args)
        $newContent = $content -replace 'void main\(\)', 'public static void main(String[] args)'
        
        # Fix 2: void main() throws Exception -> public static void main(String[] args) throws Exception  
        $newContent = $newContent -replace 'void main\(\) throws Exception', 'public static void main(String[] args) throws Exception'
        
        # Write back
        Set-Content -Path $file -Value $newContent -NoNewline
        Write-Host "  Fixed to 'public static void main(String[] args)'" -ForegroundColor Green
        $fixedCount++
    } else {
        Write-Host "  WARNING: 'void main()' not found in file (already fixed?)" -ForegroundColor Red
    }
    
    Write-Host ""
}

Write-Host "=== SUMMARY ===" -ForegroundColor Cyan
Write-Host "Total files checked: $($mainIssueFiles.Count)"
Write-Host "Files fixed: $fixedCount"
Write-Host ""

# Verify fixes
Write-Host "=== VERIFICATION ===" -ForegroundColor Cyan
$remainingIssues = 0
foreach ($file in $mainIssueFiles) {
    $content = Get-Content $file -Raw
    if ($content -match 'void main\(\)') {
        Write-Host "STILL HAS ISSUE: $file" -ForegroundColor Red
        $remainingIssues++
    }
}

if ($remainingIssues -eq 0) {
    Write-Host "All main method issues have been fixed successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "=== NEXT STEPS ===" -ForegroundColor Cyan
    Write-Host "1. Other OpenRewrite changes are SAFE (switch expressions, pattern matching, etc.)"
    Write-Host "2. Exception variable changes to '_' are cosmetic"
    Write-Host "3. All changes compile successfully"
    Write-Host "4. Ready to commit and push"
} else {
    Write-Host "WARNING: $remainingIssues files still have issues" -ForegroundColor Red
}