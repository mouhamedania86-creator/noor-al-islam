# 🕌 نور الإسلام — Noor Al-Islam

> **تطبيق إسلامي شامل**: قرآن، أذان، أذكار، قبلة، راديو، أسماء الله الحسنى، مساعد IslamAI الذكي

[![Build APK](https://img.shields.io/badge/Build%20APK-GitHub%20Actions-blue?logo=github-actions)](https://github.com/features/actions)
[![Platform](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://www.android.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(7.0)-orange)]()
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue)]()

---

## ⚡ بناء التطبيق من التليفون (5 دقايق)

### 1️⃣ أنشئ حساب GitHub
- روح **https://github.com/signup**
- سجّل وفعّل الإيميل

### 2️⃣ أنشئ Repository جديد
- **https://github.com/new**
- سمّيه `noor-al-islam`
- Public
- ❌ ما تضيفش README

### 3️⃣ ارفع الملفات
- في الـ repo الجديد، اضغط **uploading an existing file**
- اسحب **محتويات** مجلد `noor_islam/` (لا الـ zip، بل الملفات داخله)
- اضغط **Commit changes**

### 4️⃣ شغّل البناء
- روح لتبويب **Actions**
- اختار **🕌 Build Noor Al-Islam APK**
- اضغط **Run workflow**
- انتظر 5-15 دقيقة

### 5️⃣ نزّل APK
- في آخر الـ run، انزل لـ **Artifacts**
- اضغط **noor-al-islam-debug-apk**
- افتح الـ ZIP واستخرج `app-debug.apk`
- اضغط عليه باش ينصب على تليفونك

---

## 📋 لازم تضيفه في GitHub Secrets (اختياري)

| Secret | منين تجيبه |
|--------|-----------|
| `GEMINI_API_KEY` | https://aistudio.google.com/apikey |

بدون المفتاح: التطبيق يشتغل كامل، بس شات IslamAI ما يردّش.

---

## 📖 أدلة مفصّلة

- 📘 **[BUILD_GUIDE_AR.md](BUILD_GUIDE_AR.md)** — الدليل الكامل خطوة بخطوة (عربي)
- 🔐 **[scripts/setup_release_secrets.md](scripts/setup_release_secrets.md)** — للنشر في Google Play

---

## 🛠️ مميزات التطبيق

- 📖 **القرآن الكريم** كامل (114 سورة) مع ترجمات EN/FR
- 🕌 **مواقيت الصلاة** حسب موقعك (58 ولاية جزائرية + مدن عالمية)
- 🤲 **الأذكار والأدعية** (صباح، مساء، نوم، استيقاظ...)
- 📿 **السبحة الإلكترونية** (5 أذكار)
- 🕋 **اتجاه القبلة** بالبوصلة
- 📻 **إذاعات قرآنية** (9 محطات حية)
- 🤖 **IslamAI** — مساعد ذكي للفتاوى والأسئلة الشرعية
- 🎓 **اختبار إسلامي** (15 سؤال لكل جلسة)
- 📅 **التقويم الهجري**
- 🕯️ **الرقية الشرعية** (offline)
- 📖 **أسماء الله الحسنى** (99 اسم)
- 📚 **مكتبة** (تجويد، فقه، خطب، حج، ختمة)

---

## 🏗️ التقنيات

- **اللغة:** Kotlin 2.2.10
- **UI:** Jetpack Compose + Material 3
- **DB:** Room
- **Network:** Retrofit + OkHttp + Moshi
- **AI:** Firebase AI + Gemini
- **Audio:** Foreground Services + MediaPlayer
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36 (Android 16)

---

## 📞 دعم

إذا واجهتك أي مشكلة في البناء، افتح **Issue** في GitHub أو ابعت screenshot من خطأ Actions.
