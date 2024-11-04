param(
    #下载链接
    [string]$url,
    #下载的压缩包的存放路径，不包括压缩包名称，例如C:\Users\fanyong\Desktop
    [string]$savePath,
    #压缩包名称，例如\chrome-win64
    [string]$archive,
    #执行名称，例如chrome.exe chrome chromium
    [string]$executableName
  )
  $ErrorActionPreference = 'Stop'
Write-Host "Downloading Chrome Browser"
$wc = New-Object net.webclient
#下载
$wc.Downloadfile($url, "$savePath\$archive.zip")
Write-Host "Unzipping Chrome Browser"

#解压文件
Expand-Archive -LiteralPath "$savePath\$archive.zip" -DestinationPath "$savePath"
#删除压缩包
Remove-Item "$savePath\$archive.zip"

if (Test-Path "$savePath\$archive\$executableName") {
    (Get-Item "$savePath\$archive\$executableName").VersionInfo
}  else {
    Write-Host "ERROR: failed to install Google Chrome Beta"
    exit 1
}