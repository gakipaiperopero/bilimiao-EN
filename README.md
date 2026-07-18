<p align="center">
<img width="100px" src="https://10miaomiao.cn/icons/bilimiao_new.png"/>
</p>

<div align="center">

# bilimiao-EN (BiliMiao)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/gakipaiperopero/bilimiao-EN)](https://github.com/gakipaiperopero/bilimiao-EN/releases) ![GitHub All Releases](https://img.shields.io/github/downloads/gakipaiperopero/bilimiao-EN/total) ![GitHub stars](https://img.shields.io/github/stars/gakipaiperopero/bilimiao-EN?style=flat) ![GitHub forks](https://img.shields.io/github/forks/gakipaiperopero/bilimiao-EN)

**An English-friendly fork of [bilimiao2](https://github.com/10miaomiao/bilimiao2) — a third-party Bilibili client for Android & Desktop.**

</div>

---

## About This Fork

This is a fork of the excellent [bilimiao2](https://github.com/10miaomiao/bilimiao2) project by [10miaomiao](https://github.com/10miaomiao).

### Differences from the original

- **English translations** — All Chinese UI text and code comments have been translated to English for international contributors
- **Desktop improvements** — Fixed no-sound issues in the desktop VLC player; added FFmpeg-based DASH stream muxing (video.m4s + audio.m4s → single video.mkv) during downloads
- **Different download paths** — Android: `/storage/emulated/0/Download/bilimiao_EN/`; Desktop: `~/Downloads/bilimiao/`
- **No F-Droid release** — This fork does not publish to F-Droid. Only GitHub Releases are available.

### Credits

- **Original author**: [10miaomiao](https://github.com/10miaomiao) — [bilimiao2](https://github.com/10miaomiao/bilimiao2)
- This project is maintained separately and is not affiliated with the original author.

## Download

Grab the latest APK or desktop package from [GitHub Releases](https://github.com/gakipaiperopero/bilimiao-EN/releases).

### Desktop Usage

**Pre-built package:** currently available for Linux x64 only. Windows and macOS users must [build from source](#building-from-source).

#### Linux (x64)

1. Download `bilimiao-*-linux-x64.tar.gz` from [Releases](https://github.com/gakipaiperopero/bilimiao-EN/releases)
2. Extract: `tar -xzf bilimiao-*-linux-x64.tar.gz`
3. Run: `./bilimiao/bin/bilimiao`

#### Windows / macOS

No pre-built packages available yet. See [Building from Source](#building-from-source) below.

#### Requirements (all platforms)

- **VLC 3.0+** — required for video playback
- **FFmpeg** (optional) — enables automatic DASH stream muxing during downloads (video+audio merged into a single MKV file)

### Building from Source

**Prerequisites:** Java 17+, Android SDK (for APK build), and VLC development headers.

```bash
# Build desktop app (runs on current OS)
./gradlew :desktop-app:run

# Package desktop distribution for current OS
./gradlew :desktop-app:packageDistributionForCurrentOS

# Build Android APK (full flavor)
./gradlew assembleFullDebug

# Build Android APK (FOSS flavor)
./gradlew assembleFossDebug
```

On **Windows**, you can produce an `.msi` installer with `./gradlew :desktop-app:packageMsi`.  
On **macOS**, a `.dmg` package with `./gradlew :desktop-app:packageDmg`.

### Android Usage

Install the APK directly. Two flavors are available:
- `app-full-debug.apk` — includes Baidu stats, Geetest CAPTCHA, and AV1 decoder
- `app-foss-debug.apk` — no proprietary dependencies

## License

This project is licensed under the **GNU General Public License v3.0** — see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [10miaomiao / bilimiao2](https://github.com/10miaomiao/bilimiao2) — the original project this fork is based on
- [bilibili-API-collect](https://github.com/SocialSisterYi/bilibili-API-collect)
- [BiliRoaming](https://github.com/yujincheng08/BiliRoaming)
- [Kodein-DI](https://github.com/Kodein-Framework/Kodein-DI)
- [Splitties](https://github.com/LouisCAD/Splitties)
- [okhttp](https://github.com/square/okhttp)
- [glide](https://github.com/bumptech/glide)
- [BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)
- [ModernAndroidPreferences](https://github.com/Maxr1998/ModernAndroidPreferences)
- [NumberPickerView](https://github.com/Carbs0126/NumberPickerView)
- [ShadowLayout](https://github.com/lihangleo2/ShadowLayout)
- [GSYVideoPlayer](https://github.com/CarGuo/GSYVideoPlayer)
- [DanmakuFlameMaster](https://github.com/bilibili/DanmakuFlameMaster)
- [mojito](https://github.com/mikaelzero/mojito)
- [DialogX](https://github.com/kongzue/DialogX)
- [scale](https://github.com/jvziyaoyao/scale)
