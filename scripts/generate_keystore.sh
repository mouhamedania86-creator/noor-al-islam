#!/bin/bash
# ============================================
# سكريبت توليد Release Keystore
# استخدمه فقط لما تكون جاهز تنشر في Google Play
# شغّل مرة واحدة واحفظ ملف JKS في مكان آمن!
# ============================================

set -e

KEYSTORE_NAME="my-upload-key.jks"
ALIAS="upload"
VALIDITY=10000  # ~27 سنة

echo "🔐 توليد Release Keystore..."
echo ""

# Prompt for passwords
read -p "أدخل كلمة سر الـ keystore (ستحتاجها لاحقاً): " -s STORE_PASS
echo ""
read -p "أدخل كلمة سر المفتاح (يمكن أن تكون نفسها): " -s KEY_PASS
echo ""
read -p "أدخل اسمك (Common Name): " CN
echo ""

# Generate the keystore
keytool -genkey -v \
  -keystore "$KEYSTORE_NAME" \
  -keyalg RSA \
  -keysize 2048 \
  -validity $VALIDITY \
  -alias "$ALIAS" \
  -keypass "$KEY_PASS" \
  -storepass "$STORE_PASS" \
  -dname "CN=$CN, OU=Mobile, O=NoorAlIslam, L=Algiers, ST=Algiers, C=DZ"

echo ""
echo "✅ تم توليد $KEYSTORE_NAME بنجاح!"
echo ""
echo "📋 الخطوات التالية:"
echo "1. أضف 3 secrets في GitHub (Settings → Secrets → Actions):"
echo "   - KEYSTORE_BASE64: محتوى $(base64 -w 0 $KEYSTORE_NAME)"
echo "   - KEY_ALIAS: $ALIAS"
echo "   - KEY_STORE_PASSWORD: $STORE_PASS"
echo "   - KEY_PASSWORD: $KEY_PASS"
echo ""
echo "2. احفظ ملف $KEYSTORE_NAME في مكان آمن (لن تستطيع استرجاعه!)"
echo ""
echo "⚠️ إذا فقدت الـ keystore، لن تستطيع تحديث التطبيق في Google Play!"
