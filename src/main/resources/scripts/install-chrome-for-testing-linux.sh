#!/usr/bin/env bash
set -e
set -x
if [[ $(arch) == "aarch64" ]]; then
  echo "ERROR: not supported on Linux Arm64"
  exit 1
fi

if [[ ! -f "/etc/os-release" ]]; then
  echo "ERROR: cannot install on unknown linux distribution (/etc/os-release is missing)"
  exit 1
fi

## 1. make sure to remove old beta if any.
#if dpkg --get-selections | grep -q "^google-chrome-beta[[:space:]]*install$" >/dev/null; then
#  apt-get remove -y google-chrome-beta
#fi

# 2. Update apt lists (needed to install curl and chrome dependencies)
# apt-get update

# 3. Install curl to download chrome
if ! command -v curl >/dev/null; then
  if [[ -f /etc/debian_version ]]; then
    apt-get install -y curl
  elif [[ -f /etc/centos-release ]]; then
    yum -y install curl
  else
    echo "Unknown Linux distribution."
  fi
fi

# 4. download chrome beta from dl.google.com and install it.
cd "$1" #$1是安装目录
curl -O -L "$2" # $2是一个url，代表浏览器的下载地址
if [[ "$4" == "firefox" ]];then #是火狐浏览器
  if ! command -v tar >/dev/null; then
    if [[ -f /etc/debian_version ]]; then
      apt-get install -y tar
    elif [[ -f /etc/centos-release ]]; then
      yum -y install tar
    else
      echo "Unknown Linux distribution."
    fi
  fi
  tar -jxvf ./"$3.tar.bz2"
  rm -rf ./"$3.tar.bz2"
  ./"$4"/"$4" --version
else #是谷歌浏览器
  unzip ./"$3.zip" #解压 $3例如 chrome-linux64
  rm -rf ./"$3.zip"
  # 安装依赖
  if [[ -f /etc/debian_version ]]; then
      apt-get install -y ca-certificates fonts-liberation libasound2 libatk-bridge2.0-0 libatk1.0-0 libc6 libcairo2 libcups2 libdbus-1-3 libexpat1 libfontconfig1 libgbm1 libgcc1 libglib2.0-0 libgtk-3-0 libnspr4 libnss3 libpango-1.0-0 libpangocairo-1.0-0 libstdc++6 libx11-6 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 libxdamage1 libxext6 libxfixes3 libxi6 libxrandr2 libxrender1 libxss1 libxtst6 lsb-release wget xdg-utils
    elif [[ -f /etc/centos-release ]]; then
      yum install -y alsa-lib.x86_64 atk.x86_64 cups-libs.x86_64 gtk3.x86_64 ipa-gothic-fonts libXcomposite.x86_64 libXcursor.x86_64 libXdamage.x86_64 libXext.x86_64 libXi.x86_64 libXrandr.x86_64 libXScrnSaver.x86_64 libXtst.x86_64 pango.x86_64 xorg-x11-fonts-100dpi xorg-x11-fonts-75dpi xorg-x11-fonts-cyrillic xorg-x11-fonts-misc xorg-x11-fonts-Type1 xorg-x11-utils
    else
      echo "Unknown Linux distribution."
    fi
    ./"$3"/"$4" --version
fi




