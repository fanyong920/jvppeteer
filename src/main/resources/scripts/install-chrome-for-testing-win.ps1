param(
    #下载链接
    [string]$url,

    #下载的压缩包的存放路径，不包括压缩包名称，例如C:\Users\fanyong\Desktop
    [string]$savePath,

    #压缩包名称，例如chrome-win64
    [string]$archive,

    #执行名称，例如chrome.exe chrome chromium
    [string]$executableName
  )
$ErrorActionPreference = 'Stop'
$array = @("firefox.exe")
if ($array -contains $executableName) {
    #是火狐浏览器
    Write-Host "Downloading firefox browser"
    $wc = New-Object net.webclient
    #下载
    $wc.Downloadfile($url, "$savePath\$archive.exe")
    Write-Host "install firefox browser"
    #设置环境变量
    $env:__compat_layer = 'RunAsInvoker'
    #安装exe文件
    $process = Start-Process -FilePath "$savePath\$archive.exe" -ArgumentList "/ExtractDir=$savePath" -NoNewWindow -Wait -PassThru
    #删除压缩包
    Remove-Item "$savePath\$archive.exe"
    if (Test-Path "$savePath\core\$executableName") {
            (Get-Item "$savePath\core\$executableName").VersionInfo
        }  else {
            Write-Host "ERROR: failed to install firefox"br
            exit 1
        }
}else{
    Write-Host "Downloading chrome browser"
    $wc = New-Object net.webclient
    #下载
    $wc.Downloadfile($url, "$savePath\$archive.zip")
    Write-Host "Unzipping Chrome Browser"

    #解压文件
    Expand-Archive -LiteralPath "$savePath\$archive.zip" -DestinationPath "$savePath"
    #删除压缩包
    Remove-Item "$savePath\$archive.zip"
    #测试
    if (Test-Path "$savePath\$archive\$executableName") {
        (Get-Item "$savePath\$archive\$executableName").VersionInfo
    }  else {
        Write-Host "ERROR: failed to install Google Chrome Beta"
        exit 1
    }
}


