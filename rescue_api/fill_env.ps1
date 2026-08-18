<#
Interactive script to populate the .env file for rescue_api.
This script prompts for sensitive values locally (no secrets are sent anywhere).
Run in PowerShell on the machine where you want to store the secrets.

Usage:
  Open PowerShell in the folder `rescue_api` and run:
    ./fill_env.ps1

The script will overwrite the existing `.env` file after confirmation.
#>

function Read-Secret([string]$prompt) {
    $secure = Read-Host -AsSecureString -Prompt $prompt
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}

Write-Host "This will create/overwrite .env in the current directory (rescue_api)."
$confirm = Read-Host "Proceed? (y/N)"
if ($confirm -ne 'y' -and $confirm -ne 'Y') {
    Write-Host "Aborted. No changes made."; exit 0
}

# Read values (defaults shown where appropriate)
$dbUrl = Read-Host "DB_URL (enter for default 'jdbc:mysql://localhost:3306/rescue?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC')"
if ([string]::IsNullOrEmpty($dbUrl)) { $dbUrl = "jdbc:mysql://localhost:3306/rescue?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC" }
$dbUser = Read-Host "DB_USERNAME (default 'root')"
if ([string]::IsNullOrEmpty($dbUser)) { $dbUser = "root" }
$dbPass = Read-Secret "DB_PASSWORD (input hidden)"

$jwt = Read-Secret "JWT_SECRET (input hidden)"

$tencent = Read-Host "TENCENT_MAP_KEY (leave blank if none)"

$aliyunId = Read-Host "ALIYUN_OSS_ACCESS_KEY_ID (leave blank if none)"
$aliyunSecret = if (-not [string]::IsNullOrEmpty($aliyunId)) { Read-Secret "ALIYUN_OSS_ACCESS_KEY_SECRET (input hidden)" } else { '' }
$aliyunBucket = Read-Host "ALIYUN_OSS_BUCKET_NAME (default 'zhichonggang')"
if ([string]::IsNullOrEmpty($aliyunBucket)) { $aliyunBucket = 'zhichonggang' }
$aliyunEndpoint = Read-Host "ALIYUN_OSS_ENDPOINT (default 'https://oss-cn-beijing.aliyuncs.com')"
if ([string]::IsNullOrEmpty($aliyunEndpoint)) { $aliyunEndpoint = 'https://oss-cn-beijing.aliyuncs.com' }

$aliyunVoiceId = Read-Host "ALIYUN_VOICE_ACCESS_KEY_ID (leave blank if none)"
$aliyunVoiceSecret = if (-not [string]::IsNullOrEmpty($aliyunVoiceId)) { Read-Secret "ALIYUN_VOICE_ACCESS_KEY_SECRET (input hidden)" } else { '' }
$aliyunVoiceTts = Read-Host "ALIYUN_VOICE_TTS_CODE (default 'TTS_328715209')"
if ([string]::IsNullOrEmpty($aliyunVoiceTts)) { $aliyunVoiceTts = 'TTS_328715209' }
$aliyunVoiceCalled = Read-Host "ALIYUN_VOICE_CALLED_NUMBER (leave blank if none)"

$wechatUserAppid = Read-Host "WECHAT_USER_APPID (leave blank if none)"
$wechatUserSecret = if (-not [string]::IsNullOrEmpty($wechatUserAppid)) { Read-Secret "WECHAT_USER_SECRET (input hidden)" } else { '' }
$wechatShifuAppid = Read-Host "WECHAT_SHIFU_APPID (leave blank if none)"
$wechatShifuSecret = if (-not [string]::IsNullOrEmpty($wechatShifuAppid)) { Read-Secret "WECHAT_SHIFU_SECRET (input hidden)" } else { '' }

$content = @()
$content += "# Local development .env (DO NOT commit real secrets)"
$content += "# Generated on $(Get-Date -Format o)"
$content += ""
$content += "DB_URL=$dbUrl"
$content += "DB_USERNAME=$dbUser"
$content += "DB_PASSWORD=$dbPass"
$content += ""
$content += "JWT_SECRET=$jwt"
$content += ""
$content += "TENCENT_MAP_KEY=$tencent"
$content += ""
$content += "ALIYUN_OSS_ACCESS_KEY_ID=$aliyunId"
$content += "ALIYUN_OSS_ACCESS_KEY_SECRET=$aliyunSecret"
$content += "ALIYUN_OSS_BUCKET_NAME=$aliyunBucket"
$content += "ALIYUN_OSS_ENDPOINT=$aliyunEndpoint"
$content += ""
$content += "ALIYUN_VOICE_ACCESS_KEY_ID=$aliyunVoiceId"
$content += "ALIYUN_VOICE_ACCESS_KEY_SECRET=$aliyunVoiceSecret"
$content += "ALIYUN_VOICE_TTS_CODE=$aliyunVoiceTts"
$content += "ALIYUN_VOICE_CALLED_NUMBER=$aliyunVoiceCalled"
$content += ""
$content += "WECHAT_USER_APPID=$wechatUserAppid"
$content += "WECHAT_USER_SECRET=$wechatUserSecret"
$content += "WECHAT_SHIFU_APPID=$wechatShifuAppid"
$content += "WECHAT_SHIFU_SECRET=$wechatShifuSecret"

Write-Host "\nAbout to write .env with provided values (sensitive values hidden in console)."
$ok = Read-Host "Confirm write .env? (y/N)"
if ($ok -ne 'y' -and $ok -ne 'Y') { Write-Host "Aborted. No file written."; exit 0 }

Set-Content -Path .\.env -Value ($content -join "`n") -Encoding UTF8
Write-Host "Wrote .env to $(Get-Location)\.env"
Write-Host "Reminder: .env is in .gitignore; do NOT commit this file."
