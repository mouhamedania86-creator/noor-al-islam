# 🕌 دليل بناء تطبيق نور الإسلام — بدون كمبيوتر!

> **الهدف:** بناء APK من التليفون فقط باستخدام GitHub Actions

---

## 📋 الخطوات (5-10 دقائق)

### الخطوة 1: إنشاء حساب GitHub (إذا ما عندكش)

1. افتح المتصفح في تليفونك
2. روح لـ **https://github.com/signup**
3. سجّل بإيميلك وكلمة مرور
4. فعّل الحساب من الإيميل

---

### الخطوة 2: إنشاء Repository جديد

1. روح لـ **https://github.com/new**
2. سمّي المشروع مثلاً: `noor-al-islam`
3. اختار **Public** (مهم باش Actions يشتغل مجاناً)
4. ❌ ما تضيفش README (نضيفه نحن)
5. اضغط **Create repository**

---

### الخطوة 3: رفع ملفات المشروع

**الطريقة الأسهل (من التليفون):**

1. في صفحة الـ Repository، اضغط **uploading an existing file**
2. افتح ملف ZIP (`noor_islam_github.zip`) اللي بعتهالك
3. **اسحب كل محتويات الـ ZIP** (لا الـ zip نفسه، بل الملفات داخله)
4. اكتب رسالة commit: "Initial commit"
5. اضغط **Commit changes**

**⚠️ مهم جداً:** ارفع **محتويات** المجلد `noor_islam/` مباشرة، بحيث يكون `app/` و `.github/` في الـ root.

```
noor-al-islam/  ← GitHub repo
├── .github/
│   └── workflows/
│       └── build.yml
├── app/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
└── ...
```

---

### الخطوة 4: إضافة GEMINI_API_KEY (اختياري لكن مهم)

بدون هذا المفتاح، شات IslamAI مش رح يشتغل. باقي التطبيق يشتغل عادي.

1. روح لـ **https://aistudio.google.com/apikey**
2. سجّل بـ Google account
3. اضغط **Create API Key**
4. انسخ المفتاح

ثم في GitHub:
1. روح للـ repository
2. اضغط **Settings** (في الأعلى)
3. في القائمة اليسرى: **Secrets and variables** → **Actions**
4. اضغط **New repository secret**
5. الاسم: `GEMINI_API_KEY`
6. القيمة: المفتاح اللي نسخته
7. اضغط **Add secret**

---

### الخطوة 5: تشغيل البناء

1. في الـ repository، اضغط على تبويب **Actions**
2. من القائمة اليسرى، اختار **🕌 Build Noor Al-Islam APK**
3. اضغط **Run workflow** (يمين)
4. اختار branch `main`
5. اضغط **Run workflow** (الأخضر)

---

### الخطوة 6: انتظر وانزّل APK ⏳

- البناء يأخذ **5-15 دقيقة** أول مرة
- تقدر تشوف التقدم مباشرة في Actions tab
- لما يخلص، اضغط على الـ run
- انزل لتحت في **Artifacts**
- اضغط على **noor-al-islam-debug-apk** باش ينزل ZIP فيه الـ APK

---

### الخطوة 7: تنصيب على تليفونك 📱

1. افتح ملف ZIP اللي نزلته
2. استخرج `app-debug.apk`
3. اضغط عليه باش ينصب
4. ⚠️ إذا طلب منك: **فعّل "تثبيت من مصادر غير معروفة"** لـ File Manager أو Chrome

---

## 🎉 مبروك! التطبيق على تليفونك

---

## ❓ مشاكل شائعة

### "Build failed: Could not find tools.jar"
→ Java issue. الـ workflow يستخدم Java 17. تأكد ما عدلتش.

### "Build failed: SDK location not found"
→ الـ action android-actions/setup-android يتعامل معاها. إذا فشلت، افتح issue.

### "App crashes on startup"
→ غالباً مشكلة في الـ .env. تأكد GEMINI_API_KEY صحيح.

### "App crashes when playing adhan"
→ ملف `adhan.mp3` مفقود من assets. عادي، التطبيق ما يطقّش — يستخدم صوت النظام fallback.

---

## 🔄 بناء نسخة جديدة

كل ما تدير push للكود في GitHub، البناء يشتغل تلقائياً. تنزّل APK جديد من Artifacts.

---

## 📤 نشر في Google Play (لاحقاً)

1. أنشئ حساب مطور: **https://play.google.com/console** ($25 مرة واحدة)
2. في الـ workflow، فعّل بناء Release APK (يحتاج keystore)
3. ارفع الـ AAB على Google Play Console
4. املأ معلومات التطبيق
5. انتظر المراجعة (3-7 أيام)

---

## 💬 مساعدة

إذا واجهتك مشكلة، ابعت لي screenshot من شاشة الخطأ في Actions، وأنا أساعدك.
