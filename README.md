# 📲 Instagram Video Downloader (Android App)

An Android application to download Instagram Reels or videos using a public API (built with Flask + Instaloader).  
This app uses modern Android architecture with **Jetpack Compose**, **Ktor**, and **Koin**.

---

## 🎯 Features

- 📥 Download Instagram videos by pasting a post/reel URL
- 📊 Real-time download progress
- 💾 Save video to device storage (`/Photos`)
- ✅ Supports Android 6.0+ with scoped storage
- 🧩 Modular architecture using **Koin** for DI and **Ktor** for networking

---

## 🛠 Tech Stack

| Layer          | Library         | Purpose                             |
|----------------|------------------|--------------------------------------|
| UI             | Jetpack Compose  | Modern UI toolkit                    |
| Network        | Ktor             | HTTP client for API + file downloads |
| DI             | Koin             | Lightweight dependency injection     |
| Permissions    | Accompanist      | Runtime permission handling          |
| Coroutine      | Kotlin Coroutines| Background downloads                 |

---

## 📱 Screenshots

> _You can add screenshots here if needed_

---

## 🧩 Architecture Overview

- **Jetpack Compose** for declarative UI
- **MVVM** pattern with dependency injection (Koin)
- Ktor used both for:
  - Fetching video URL from backend
  - Downloading video with streaming and progress

---

## 🚀 How It Works

1. User pastes a public Instagram post/reel link
2. App sends request to backend API (Flask + Instaloader)
3. Receives:
- `video_url`
- `thumbnail_url`
- `title`
4. App downloads video via Ktor with real-time progress
5. Video saved in `Photos/` folder

---

## 🔧 Setup Instructions

### 1. Clone Project

```bash
git clone https://github.com/Makinul/Video-Downloader.git
