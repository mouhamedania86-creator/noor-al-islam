# 🔐 دليل إضافة Release Signing في GitHub Actions

> **اتبع هذا الدليل فقط لما تكون جاهز تنشر في Google Play Store**

---

## الخطوة 1: توليد Keystore محلياً (مرة واحدة)

على كمبيوتر أو تليفون فيه Termux:

```bash
bash scripts/generate_keystore.sh
```

هذا يولّد ملف `my-upload-key.jks` ويطلب منك 4 معلومات سرية.

---

## الخطوة 2: إضافة 4 Secrets في GitHub

روح للـ repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

### Secret 1: KEYSTORE_BASE64

```bash
# في التيرمينال، بعد ما تولّد الـ keystore:
base64 -w 0 my-upload-key.jks
```

انسخ المخرج كله (نص طويل) والصقه كقيمة.

### Secret 2: KEY_ALIAS
```
upload
```

### Secret 3: KEY_STORE_PASSWORD
نفس كلمة السر اللي دخلتها للـ keystore.

### Secret 4: KEY_PASSWORD
نفس كلمة السر اللي دخلتها للمفتاح (ممكن تكون نفس STORE).

---

## الخطوة 3: فعّل Release build

عدّل ملف `.github/workflows/build.yml` وغيّر:

```yaml
- name: 🏗️ بناء Release APK
  if: ${{ env.SHOULD_BUILD_RELEASE == 'true' }}
```

إلى:

```yaml
- name: 🏗️ بناء Release APK
  if: ${{ env.SHOULD_BUILD_RELEASE == 'true' }}
  env:
    KEYSTORE_PATH: ${{ github.workspace }}/my-upload-key.jks
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
    STORE_PASSWORD: ${{ secrets.KEY_STORE_PASSWORD }}
```

وأضف خطوة قبله تفك تشفير الـ keystore:

```yaml
- name: 🔓 فك تشفير Keystore
  if: ${{ env.SHOULD_BUILD_RELEASE == 'true' }}
  run: |
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > my-upload-key.jks
```

---

## الخطوة 4: Build AAB للنشر

بدل `assembleRelease` استخدم `bundleRelease`:

```yaml
- name: 🏗️ بناء Release AAB
  run: ./gradlew bundleRelease --no-daemon
```

الـ AAB هو اللي Google Play يطلبه. الـ APK للتثبيت اليدوي.

---

## ⚠️ تنبيهات مهمة

- **ما ترفعش ملف `.jks` في Git!** (الـ .gitignore يمنع ذا)
- **احفظ نسخة من الـ keystore في مكان آمن جداً** (Google Drive مشفّر، USB...)
- **إذا فقدت الـ keystore، ما تقدرش تنشر تحديثات جديدة!**
