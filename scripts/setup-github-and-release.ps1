[CmdletBinding()]
param (
    [Parameter(Mandatory=False)]
    [string],
    
    [Parameter(Mandatory=False)]
    [string] = ""
)

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Lornshill Timetable GitHub & Release Setup" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# 1. Prompt for repo URL if not provided
if (-not ) {
     = git remote get-url origin 2>
    if () {
        Write-Host "Existing remote 'origin': " -ForegroundColor Green
         = Read-Host "Use this existing remote? (Y/n)"
        if ( -match "^[Nn]") {
             = Read-Host "Enter your GitHub repository URL (e.g. https://github.com/username/repo.git)"
        } else {
             = 
        }
    } else {
         = Read-Host "Enter your GitHub repository URL (e.g. https://github.com/username/repo.git)"
    }
}

if (-not ) {
    Write-Host "No repository URL provided. Aborting." -ForegroundColor Red
    exit 1
}

# 2. Configure Git remote
 = git remote | Select-String -Pattern "^origin$"
if () {
    git remote set-url origin 
    Write-Host "Updated remote 'origin' -> " -ForegroundColor Green
} else {
    git remote add origin 
    Write-Host "Added remote 'origin' -> " -ForegroundColor Green
}

# 3. Ensure branch is main and push
Write-Host "
Pushing main branch to GitHub..." -ForegroundColor Yellow
git branch -M main
git push -u origin main
if ( -ne 0) {
    Write-Host "Failed to push to GitHub. Please verify your credentials/SSH keys." -ForegroundColor Red
    exit 
}
Write-Host "Successfully pushed 'main' to !" -ForegroundColor Green

# 4. Optional tag for automated release
if (-not ) {
     = Read-Host "
Do you want to push a version tag now to trigger an automated GitHub Release? (y/N)"
    if ( -match "^[Yy]") {
         = Read-Host "Enter version tag (e.g. v1.0.0)"
    }
}

if () {
    Write-Host "
Creating and pushing tag ..." -ForegroundColor Yellow
    git tag 
    git push origin 
    Write-Host "Tag  pushed! GitHub Actions is now compiling the APK and creating your GitHub Release." -ForegroundColor Green
}

# 5. Extract GitHub user & repo for raw URL
if ( -match "github\.com[:/]([^/]+)/([^/.]+)") {
     = [1]
     = [2]
    Write-Host "
--------------------------------------------------" -ForegroundColor Cyan
    Write-Host "Your Live Configuration Raw URL:" -ForegroundColor White
    Write-Host "https://raw.githubusercontent.com///main/app-config.json" -ForegroundColor Magenta
    Write-Host "
Your GitHub Actions Builds:" -ForegroundColor White
    Write-Host "https://github.com///actions" -ForegroundColor Magenta
    Write-Host "
Your GitHub Releases:" -ForegroundColor White
    Write-Host "https://github.com///releases" -ForegroundColor Magenta
    Write-Host "--------------------------------------------------" -ForegroundColor Cyan
}

Write-Host "
Setup complete!" -ForegroundColor Green
