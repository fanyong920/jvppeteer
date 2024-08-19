param(
    #下载链接
    [string]$url,
    #下载的压缩包的存放路径，不包括压缩包名称，例如C:\Users\fanyong\Desktop
    [string]$savePath,
    #压缩包名称，例如\chrome-win64.zip
    [string]$archiveName
  )
  $ErrorActionPreference = 'Stop'
Write-Host "Downloading Chrome-for-testing"
$wc = New-Object net.webclient
#下载
$wc.Downloadfile($url, "$savePath$archiveName")
Write-Host "Unziping Chrome-for-testing"

#解压文件
Expand-Archive -LiteralPath "$savePath$archiveName" -DestinationPath "$savePath"
#删除压缩包
Remove-Item "$savePath$archiveName"
$LastIndexOf = $archiveName.LastIndexOf(".")
$removeName = $archiveName.Remove($LastIndexOf,4);
$suffix = "$removeName\chrome.exe"
if (Test-Path "$savePath$suffix") {
    (Get-Item "$savePath$suffix").VersionInfo
}  else {
    Write-Host "ERROR: failed to install Google Chrome Beta"
    exit 1
}