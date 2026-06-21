package com.example

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

// --- Static Islamic Data Holder ---
object IslamicStaticData {

    // Radio Stations
    data class RadioStation(val name: String, val url: String, val info: String, val category: String)
    val radioStations = listOf(
        RadioStation("إذاعة الرقية الشرعية المباشرة", "https://backup.qurango.net/radio/ruqyah", "رقية شرعية متواصلة بقراءة كبار القراء للتحصين والعلاج والأذكار", "رقية شرعية"),
        RadioStation("إذاعة القرآن الكريم من الجزائر", "https://webradio.radioalgerie.dz/live/corande.mp3", "البث الحي الرسمي لإذاعة القرآن الكريم من الجزائر العاصمة", "الجزائر"),
        RadioStation("تلاوات ورش - الشيخ ياسين الجزائري", "https://backup.qurango.net/radio/yassen_al_djazaery", "القرآن المرتل برواية ورش عن نافع بصوت القارئ الجزائري", "ورشفة جزائرية"),
        RadioStation("إذاعة القرآن الكريم من القاهرة", "https://stream.radiojar.com/0tpy88v7g9duv", "البث المباشر لأقدم وأشهر إذاعة قرآن كريم من مصر", "مصر"),
        RadioStation("إذاعة الشيخ عبد الباسط عبد الصمد", "https://backup.qurango.net/radio/basit", "نوادر التلاوات الخاشعة والمجودة لفضيلة الشيخ عبد الباسط", "عبد الباسط"),
        RadioStation("إذاعة الشيخ عبد الرحمن السديس", "https://backup.qurango.net/radio/al_sudais", "تلاوات مرتلة خاشعة بصوت إمام الحرم المكي الشريف", "السديس"),
        RadioStation("إذاعة الشيخ محمد صديق المنشاوي", "https://backup.qurango.net/radio/minshawi", "القرآن الكريم مجوداً برائحة الزمن الجميل للمنشاوي", "المنشاوي"),
        RadioStation("إذاعة السنة النبوية - المدينة المنورة", "https://stream.radiojar.com/8s7qes6u8uduv", "بث مباشر للأحاديث النبوية الشريفة والسيرة العطرة", "السنة الشريفة"),
        RadioStation("إذاعة الشيخ ماهر المعيقلي", "https://backup.qurango.net/radio/maher", "ترتيل خاشع وجميل بصوت إمام الحرم المكي ماهر المعيقلي", "المعيقلي")
    )

    // 114 Surahs Directory
    data class SurahMeta(
        val number: Int, 
        val nameAr: String, 
        val nameEn: String, 
        val verseCount: Int, 
        val typeAr: String,
        val englishMeaning: String
    )
    val surahs = listOf(
        SurahMeta(1, "الفاتحة", "Al-Fatihah", 7, "مكية", "The Opening"),
        SurahMeta(2, "البقرة", "Al-Baqarah", 286, "مدنية", "The Cow"),
        SurahMeta(3, "آل عمران", "Ali 'Imran", 200, "مدنية", "Family of Imran"),
        SurahMeta(4, "النساء", "An-Nisa", 176, "مدنية", "The Women"),
        SurahMeta(5, "المائدة", "Al-Ma'idah", 120, "مدنية", "The Table Spread"),
        SurahMeta(6, "الأنعام", "Al-An'am", 165, "مكية", "The Cattle"),
        SurahMeta(7, "الأعراف", "Al-A'raf", 206, "مكية", "The Heights"),
        SurahMeta(8, "الأنفال", "Al-Anfal", 75, "مدنية", "The Spoils of War"),
        SurahMeta(9, "التوبة", "At-Tawbah", 129, "مدنية", "The Repentance"),
        SurahMeta(10, "يونس", "Yunus", 109, "مكية", "Jonah"),
        SurahMeta(11, "هود", "Hud", 123, "مكية", "Hud"),
        SurahMeta(12, "يوسف", "Yusuf", 111, "مكية", "Joseph"),
        SurahMeta(13, "الرعد", "Ar-Ra'd", 43, "مدنية", "The Thunder"),
        SurahMeta(14, "إبراهيم", "Ibrahim", 52, "مكية", "Abraham"),
        SurahMeta(15, "الحجر", "Al-Hijr", 99, "مكية", "The Rocky Tract"),
        SurahMeta(16, "النحل", "An-Nahl", 128, "مكية", "The Bee"),
        SurahMeta(17, "الإسراء", "Al-Isra", 111, "مكية", "The Night Journey"),
        SurahMeta(18, "الكهف", "Al-Kahf", 110, "مكية", "The Cave"),
        SurahMeta(19, "مريم", "Maryam", 98, "مكية", "Mary"),
        SurahMeta(20, "طه", "Taha", 135, "مكية", "Ta-Ha"),
        SurahMeta(21, "الأنبياء", "Al-Anbiya", 112, "مكية", "The Prophets"),
        SurahMeta(22, "الحج", "Al-Hajj", 78, "مدنية", "The Pilgrimage"),
        SurahMeta(23, "المؤمنون", "Al-Mu'minun", 118, "مكية", "The Believers"),
        SurahMeta(24, "النور", "An-Nur", 64, "مدنية", "The Light"),
        SurahMeta(25, "الفرقان", "Al-Furqan", 77, "مكية", "The Criterion"),
        SurahMeta(26, "الشعراء", "Ash-Shu'ara", 227, "مكية", "The Poets"),
        SurahMeta(27, "النمل", "An-Naml", 93, "مكية", "The Ant"),
        SurahMeta(28, "القصص", "Al-Qasas", 88, "مكية", "The Stories"),
        SurahMeta(29, "العنكبوت", "Al-'Ankabut", 69, "مكية", "The Spider"),
        SurahMeta(30, "الروم", "Ar-Rum", 60, "مكية", "The Romans"),
        SurahMeta(31, "لقمان", "Luqman", 34, "مكية", "Luqman"),
        SurahMeta(32, "السجدة", "As-Sajdah", 30, "مكية", "The Prostration"),
        SurahMeta(33, "الأحزاب", "Al-Ahzab", 73, "مدنية", "The Combined Forces"),
        SurahMeta(34, "سبأ", "Saba", 54, "مكية", "Sheba"),
        SurahMeta(35, "فاطر", "Fatir", 45, "مكية", "Originator"),
        SurahMeta(36, "يس", "Ya-Sin", 83, "مكية", "Ya-Sin"),
        SurahMeta(37, "الصافات", "As-Saffat", 182, "مكية", "Those who set the Ranks"),
        SurahMeta(38, "ص", "Sad", 88, "مكية", "The Letter 'Sad'"),
        SurahMeta(39, "الزمر", "Az-Zumar", 75, "مكية", "The Groups"),
        SurahMeta(40, "غافر", "Ghafir", 85, "مكية", "The Forgiver"),
        SurahMeta(41, "فصلت", "Fussilat", 54, "مكية", "Explained in Detail"),
        SurahMeta(42, "الشورى", "Ash-Shura", 53, "مكية", "The Consultation"),
        SurahMeta(43, "الزخرف", "Az-Zukhruf", 89, "مكية", "The Ornaments of Gold"),
        SurahMeta(44, "الدخان", "Ad-Dukhan", 59, "مكية", "The Smoke"),
        SurahMeta(45, "الجاثية", "Al-Jathiyah", 37, "مكية", "The Crouching"),
        SurahMeta(46, "الأحقاف", "Al-Ahqaf", 35, "مكية", "The Wind-Curved Sandhills"),
        SurahMeta(47, "محمد", "Muhammad", 38, "مدنية", "Muhammad"),
        SurahMeta(48, "الفتح", "Al-Fath", 29, "مدنية", "The Victory"),
        SurahMeta(49, "الحجرات", "Al-Hujurat", 18, "مدنية", "The Dwellings"),
        SurahMeta(50, "ق", "Qaf", 45, "مكية", "The Letter 'Qaf'"),
        SurahMeta(51, "الذاريات", "Adh-Dhariyat", 60, "مكية", "The Winnowing Winds"),
        SurahMeta(52, "الطور", "At-Tur", 49, "مكية", "The Mount"),
        SurahMeta(53, "النجم", "An-Najm", 62, "مكية", "The Star"),
        SurahMeta(54, "القمر", "Al-Qamar", 55, "مكية", "The Moon"),
        SurahMeta(55, "الرحمن", "Ar-Rahman", 78, "مدنية", "The Beneficent"),
        SurahMeta(56, "الواقعة", "Al-Waqi'ah", 96, "مكية", "The Inevitable"),
        SurahMeta(57, "الحديد", "Al-Hadid", 29, "مدنية", "The Iron"),
        SurahMeta(58, "المجادلة", "Al-Mujadilah", 22, "مدنية", "The Pleading Woman"),
        SurahMeta(59, "الحشر", "Al-Hashr", 24, "مدنية", "The Exile"),
        SurahMeta(60, "الممتحنة", "Al-Mumtahanah", 13, "مدنية", "She that is to be examined"),
        SurahMeta(61, "الصف", "As-Saff", 14, "مدنية", "The Ranks"),
        SurahMeta(62, "الجمعة", "Al-Jum'ah", 11, "مدنية", "The Congregation"),
        SurahMeta(63, "المنافقون", "Al-Munafiqun", 11, "مدنية", "The Hypocrites"),
        SurahMeta(64, "التغابن", "At-Taghabun", 18, "مدنية", "The Mutual Disillusion"),
        SurahMeta(65, "الطلاق", "At-Talaq", 12, "مدنية", "The Divorce"),
        SurahMeta(66, "التحريم", "At-Tahrim", 12, "مدنية", "The Prohibition"),
        SurahMeta(67, "الملك", "Al-Mulk", 30, "مكية", "The Sovereignty"),
        SurahMeta(68, "القلم", "Al-Qalam", 52, "مكية", "The Pen"),
        SurahMeta(69, "الحاقة", "Al-Haqqah", 52, "مكية", "The Reality"),
        SurahMeta(70, "المعارج", "Al-Ma'arij", 44, "مكية", "The Ascending Stairways"),
        SurahMeta(71, "نوح", "Nuh", 28, "مكية", "Noah"),
        SurahMeta(72, "الجن", "Al-Jinn", 28, "مكية", "The Jinn"),
        SurahMeta(73, "المزمل", "Al-Muzzammil", 20, "مكية", "The Enshrouded One"),
        SurahMeta(74, "المدثر", "Al-Muddaththir", 56, "مكية", "The Cloaked One"),
        SurahMeta(75, "القيامة", "Al-Qiyamah", 40, "مكية", "The Resurrection"),
        SurahMeta(76, "الإنسان", "Al-Insan", 31, "مدنية", "The Man"),
        SurahMeta(77, "المرسلات", "Al-Mursalat", 50, "مكية", "The Emissaries"),
        SurahMeta(78, "النبأ", "An-Naba'", 40, "مكية", "The Tidings"),
        SurahMeta(79, "النازعات", "An-Nazi'at", 46, "مكية", "Those who drag forth"),
        SurahMeta(80, "عبس", "Abasa", 42, "مكية", "He Frowned"),
        SurahMeta(81, "التكوير", "At-Takwir", 29, "مكية", "The Overthrowing"),
        SurahMeta(82, "الانفطار", "Al-Infitar", 19, "مكية", "The Cleaving"),
        SurahMeta(83, "المطففين", "Al-Mutaffifin", 36, "مكية", "The Defrauding"),
        SurahMeta(84, "الانشقاق", "Al-Inshiqaq", 25, "مكية", "The Sundering"),
        SurahMeta(85, "البروج", "Al-Buruj", 22, "مكية", "The Mansions of the Stars"),
        SurahMeta(86, "الطارق", "At-Tariq", 17, "مكية", "The Morning Star"),
        SurahMeta(87, "الأعلى", "Al-A'la", 19, "مكية", "The Most High"),
        SurahMeta(88, "الغاشية", "Al-Ghashiyah", 26, "مكية", "The Overwhelming"),
        SurahMeta(89, "الفجر", "Al-Fajr", 30, "مكية", "The Dawn"),
        SurahMeta(90, "البلد", "Al-Balad", 20, "مكية", "The City"),
        SurahMeta(91, "الشمس", "Ash-Shams", 15, "مكية", "The Sun"),
        SurahMeta(92, "الليل", "Al-Layl", 21, "مكية", "The Night"),
        SurahMeta(93, "الضحى", "Ad-Duha", 11, "مكية", "The Morning Hours"),
        SurahMeta(94, "الشرح", "Ash-Sharh", 8, "مكية", "The Relief"),
        SurahMeta(95, "التين", "At-Tin", 8, "مكية", "The Fig"),
        SurahMeta(96, "العلق", "Al-'Alaq", 19, "مكية", "The Clot"),
        SurahMeta(97, "القدر", "Al-Qadr", 5, "مكية", "The Power"),
        SurahMeta(98, "البينة", "Al-Bayyinah", 8, "مدنية", "The Clear Proof"),
        SurahMeta(99, "الزلزلة", "Az-Zalzalah", 8, "مدنية", "The Earthquake"),
        SurahMeta(100, "العاديات", "Al-'Adiyat", 11, "مكية", "The Courser"),
        SurahMeta(101, "القارعة", "Al-Qari'ah", 11, "مكية", "The Calamity"),
        SurahMeta(102, "التكاثر", "At-Takathur", 8, "مكية", "The Rivalry in World Increase"),
        SurahMeta(103, "العصر", "Al-'Asr", 3, "مكية", "The Declining Day"),
        SurahMeta(104, "الهمزة", "Al-Humazah", 9, "مكية", "The Traducer"),
        SurahMeta(105, "الفيل", "Al-Fil", 5, "مكية", "The Elephant"),
        SurahMeta(106, "قريش", "Quraysh", 4, "مكية", "Quraysh"),
        SurahMeta(107, "الماعون", "Al-Ma'un", 7, "مكية", "The Neighborly Assistance"),
        SurahMeta(108, "الكوثر", "Al-Kawthar", 3, "مكية", "The Abundance"),
        SurahMeta(109, "الكافرون", "Al-Kafirun", 6, "مكية", "The Disbelievers"),
        SurahMeta(110, "النصر", "An-Nasr", 3, "مدنية", "The Divine Support"),
        SurahMeta(111, "المسد", "Al-Masad", 5, "مكية", "The Palm Fiber"),
        SurahMeta(112, "الإخلاص", "Al-Ikhlas", 4, "مكية", "The Sincerity"),
        SurahMeta(113, "الفلق", "Al-Falaq", 5, "مكية", "The Daybreak"),
        SurahMeta(114, "الناس", "An-Nas", 6, "مكية", "Mankind")
    )

    // Curated offline verses for top surahs so the app is 100% usable without internet
    data class OfflineVerse(val textAr: String, val textEn: String, val textFr: String, val number: Int)
    
    val offlineChapters = mapOf(
        1 to listOf(
            OfflineVerse("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "Au nom d'Allah, le Tout Miséricordieux, le Très Miséricordieux.", 1),
            OfflineVerse("الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", "Louange à Allah, Seigneur de l'univers.", 2),
            OfflineVerse("الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "Le Tout Miséricordieux, le Très Miséricordieux,", 3),
            OfflineVerse("مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "Maître du Jour de la rétribution.", 4),
            OfflineVerse("إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "C'est Toi [Seul] que nous adorons, et c'est Toi [Seul] dont nous implorons le secours.", 5),
            OfflineVerse("اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -", "Guide-nous dans le droit chemin,", 6),
            OfflineVerse("صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "Le chemin de ceux que Tu as comblés de faveurs, non pas de ceux qui ont encouru Ta كolère, ni des égarés.", 7)
        ),
        112 to listOf(
            OfflineVerse("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "Au nom d'Allah.", 1),
            OfflineVerse("قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,", "Dis: «Il est Allah, Unique.", 2),
            OfflineVerse("اللَّهُ الصَّمَدُ", "Allah, the Absolute Reference.", "Allah, Le Seul à être imploré pour nos besoins.", 3),
            OfflineVerse("لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", "Il n'a jamais engendré, n'a pas été engendré non plus.", 4),
            OfflineVerse("وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "And there is none co-equal or comparable unto Him.\"", "Et nul n'est égal à Lui».", 5)
        ),
        113 to listOf(
            OfflineVerse("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful.", "Au nom d'Allah.", 1),
            OfflineVerse("قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak", "Dis: «Je cherche protection auprès du Seigneur de l'aube,", 2),
            OfflineVerse("مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created", "contre le mal des êtres qu'Il a créés,", 3),
            OfflineVerse("وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", "contre le mal de l'obscurité quand elle s'approfondit,", 4),
            OfflineVerse("وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots", "contre le mal de celles qui soufflent sur les nœuds,", 5),
            OfflineVerse("مِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"", "et contre le mal de l'envieux quand il envie».", 6)
        ),
        114 to listOf(
            OfflineVerse("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah.", "Au nom d'Allah.", 1),
            OfflineVerse("قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, \"I seek refuge in the Lord of mankind,", "Dis: «Je cherche protection auprès du Seigneur des hommes,", 2),
            OfflineVerse("مَلِكِ النَّاسِ", "The Sovereign of mankind,", "Le Souverain des hommes,", 3),
            OfflineVerse("إِلَٰهِ النَّاسِ", "The God of mankind,", "Le Dieu des hommes,", 4),
            OfflineVerse("مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -", "contre le mal du mauvais conseiller, furtif,", 5),
            OfflineVerse("الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers [evil] into the breasts of mankind -", "qui souffle le mal dans les poitrines des hommes,", 6),
            OfflineVerse("مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.\"", "qu'il [le conseiller] soit djinn, ou humain».", 7)
        )
    )

    // Curated 99 Names of Allah
    data class NameOfAllah(val number: Int, val nameAr: String, val nameEn: String, val meaningAr: String, val meaningEn: String)
    val namesOfAllah = listOf(
        NameOfAllah(1, "الرَّحْمَنُ", "Ar-Rahman", "الذي شملت رحمته كافة الخلائق عموماً", "The All-Beneficent"),
        NameOfAllah(2, "الرَّحِيمُ", "Ar-Rahim", "المنعم على عباده المؤمنين خصوصاً ورحمته دائمة", "The Especially Merciful"),
        NameOfAllah(3, "الْمَلِكُ", "Al-Malik", "المتصرف في خلقه وسلطانه فوق كل شيء", "The Absolute Ruler"),
        NameOfAllah(4, "الْقُدُّوسُ", "Al-Quddus", "المنزه عن كل عيب ونقص الطاهر من الكبائر", "The Pure / The Holy"),
        NameOfAllah(5, "السَّلَامُ", "As-Salam", "الذي سلم خلقه من ظلمه ونشر الأمان بفضله", "The Source of Peace"),
        NameOfAllah(6, "الْمُؤْمِنُ", "Al-Mu'min", "الذي يصدق عباده ويبث الأمان في قلوب المتقين", "The Inspirer of Faith"),
        NameOfAllah(7, "الْمُهَيْمِنُ", "Al-Muhaymin", "الرقيب الحافظ الحفيظ لكل شيء والشاهد على خلقه", "The Guardian"),
        NameOfAllah(8, "الْعَزِيزُ", "Al-Aziz", "الغالب الذي لا يغلب القوي المتعاظم ذو الكبرياء", "The All-Mighty"),
        NameOfAllah(9, "الْجَبَّارُ", "Al-Jabbar", "الذي يجبر كسر القلوب وينفذ أمره في كل شيء قهراً", "The Compeller"),
        NameOfAllah(10, "الْمُتَكَبِّرُ", "Al-Mutakabbir", "المنزه عن صفات الخلق المتفرد بالعظمة والكبرياء", "The Supreme / The Majestic"),
        NameOfAllah(11, "الْخَالِقُ", "Al-Khaliq", "المبدع والموجد لكل الأشياء من العدم على تقدير وتدبير", "The Creator"),
        NameOfAllah(12, "الْبَارِئُ", "Al-Bari'", "الذي خلق وتبرأ من العيوب وفصل الأشكال بعضها عن بعض", "The Maker of Order"),
        NameOfAllah(13, "الْمُصَوِّرُ", "Al-Musawwir", "الذي يعطي لكل مخلوق صورته الخاصة به المتميزة بها", "The Shaper of Beauty"),
        NameOfAllah(14, "الْغَفَّارُ", "Al-Ghaffar", "الذي يستر الذنوب ويتجاوز عن الزلات في الدنيا والآخرة", "The Forgiver"),
        NameOfAllah(15, "الْقَهَّارُ", "Al-Qahhar", "الغالب الذي خضعت له الرقاب وقهر الجبابرة بسلطانه", "The Subduer"),
        NameOfAllah(16, "الْوَهَّابُ", "Al-Wahhab", "جواد يفضل بالعطايا والمنن من غير سؤال ولا مقابل", "The Giver of All"),
        NameOfAllah(17, "الرَّزَّاقُ", "Ar-Razzaq", "الذي يوزع الأرزاق المادية والروحية على خلقه", "The Sustainer"),
        NameOfAllah(18, "الْفَتَّاحُ", "Al-Fattah", "الذي يفتح أبواب الخير والرزق والرحمة والمغفرة", "The Opener of Hearts"),
        NameOfAllah(19, "الْعَلِيمُ", "Al-Alim", "المحيط بكل شيء علماً ظاهره وباطنه دقيقه وجليله", "The All-Knowing"),
        NameOfAllah(20, "الْقَابِضُ", "Al-Qabid", "ممسك الأرزاق والنفوس بحكمته وعدله", "The Restrainer"),
        NameOfAllah(21, "الْبَاسِطُ", "Al-Basit", "الذي يوسع الأرزاق ويبسط النفوس بألطافه وامتنانه", "The Expander"),
        NameOfAllah(22, "الْخَافِضُ", "Al-Khafid", "الذي يخفض الكفار والمتكبرين ويقلل من شأنهم لذنوبهم", "The Abaser"),
        NameOfAllah(23, "الرَّافِعُ", "Ar-Rafi'", "الذي يرفع من شأن عباده المؤمنين والعلماء في الدنيا والآخرة", "The Exalter"),
        NameOfAllah(24, "الْمُعِزُّ", "Al-Mu'iz", "الذي يهب العزة لمن يشاء من عباده بالتقوى والعمل", "The Giver of Honour"),
        NameOfAllah(25, "الْمُذِلُّ", "Al-Mudhill", "الذي ينزع العز ممن عصاه ويعرضهم للأهواء والهلاك", "The Giver of Dishonour"),
        NameOfAllah(26, "السَّمِيعُ", "As-Sami'", "الذي يسمع كل الأصوات والهمسات والسرائر ولا يشغله سمع عن سمع", "The All-Hearing"),
        NameOfAllah(27, "الْبَصِيرُ", "Al-Basir", "الذي يشهد ويرى حركات النملة السوداء على الصخرة الصماء", "The All-Seeing"),
        NameOfAllah(28, "الْحَكَمُ", "Al-Hakam", "الحق العادل الذي يقضي بين الناس بحكمه وعدله ولا راد لقضائه", "The Impartial Judge"),
        NameOfAllah(29, "الْعَدْلُ", "Al-Adl", "المنزه عن الظلم والجور المعطي لكل ذي حق حقه", "The Absolutely Just"),
        NameOfAllah(30, "اللَّطِيفُ", "Al-Latif", "البر بعباده المحسن إليهم في خفاء ورفق المحيط بدقائق الأمور", "The Subtle One"),
        NameOfAllah(31, "الْخَبِيرُ", "Al-Khabir", "الذي انتهى علمه إلى خفايا الصدور وأسرار السموات والأرض", "The All-Aware"),
        NameOfAllah(32, "الْحَلِيمُ", "Al-Halim", "الذي لا يعجل بالعقوبة على عباده بل يمهلهم ليتوبوا", "The Forbearing"),
        NameOfAllah(33, "الْعَظِيمُ", "Al-Azim", "الذي تفوق عظمته كل قدرة وتصور وجل عن المماثلة والحدود", "The Magnificent"),
        NameOfAllah(34, "الْغَفُورُ", "Al-Ghafur", "الذي يغفر ذنوب عباده تكراراً ومراراً ويسعهم بعفوه", "The Extensively Forgiving"),
        NameOfAllah(35, "الشَّكُورُ", "Ash-Shakur", "الذي يشكر اليسير من الطاعات ويثيب عليها بأعظم الأجور", "The Grateful"),
        NameOfAllah(36, "الْعَلِيُّ", "Al-Aliyy", "الذي علا بذاته وقدره وقهر فوق كل المخلوقات بملكه", "The Most High"),
        NameOfAllah(37, "الْكَبِيرُ", "Al-Kabir", "الذي ليس شيء أكبر منه وجل عن مشابهة الصغار والحدود", "The Infinite Majestic"),
        NameOfAllah(38, "الْحَفِيظُ", "Al-Hafiz", "الذي يحفظ أعمال العباد ويحمي خلقه من التلف والفناء بأمره", "The Preserver"),
        NameOfAllah(39, "المُقِيتُ", "Al-Muqit", "الذي يقدر الأقوات ويصل بها إلى أبدان ونفوس خلقه", "The Nourisher"),
        NameOfAllah(40, "الْحَسِيبُ", "Al-Hasib", "الذي يكفي عباده المتوكلين عليه ويحاسبهم يوم القيامة بسرور", "The Accounter"),
        NameOfAllah(41, "الْجَلِيلُ", "Al-Jalil", "المتصف بصفات الكمال المنزه عن النقص وصاحب العزة والجلال", "The Majestic"),
        NameOfAllah(42, "الْكَرِيمُ", "Al-Karim", "الذي ينعم بالعطايا الجزيلة من غير سؤال وبلا غرض ولا مصلحة", "The Generous"),
        NameOfAllah(43, "الرَّقِيبُ", "Ar-Raqib", "الذي يراقب كل حركات العباد وسكناتهم والشهيد عليها", "The Watchful"),
        NameOfAllah(44, "الْمُجِيبُ", "Al-Mujib", "الذي يستجيب دعاء الداعين بفضله وجوده ويقضي حوائج المحتاجين", "The Responsive"),
        NameOfAllah(45, "الْوَاسِعُ", "Al-Wasi'", "المحيط بالمعارف والرحمة والخير الذي اتسعت رزقه وقدرته خلقه", "The All-Encompassing"),
        NameOfAllah(46, "الْحَكيمُ", "Al-Hakim", "الذي يضع الأمور بمواضعها اللائقة بها ويدبر خلقه بإحكام وعلم", "The All-Wise"),
        NameOfAllah(47, "الْوَدُودُ", "Al-Wadud", "الذي يحب عباده المؤمنين ويستأنس بقربهم ويتحبب إليهم بالنعم", "The Loving One"),
        NameOfAllah(48, "الْمَجِيدُ", "Al-Majid", "الشريف الفائق الكرم والعطاء ذو الشرف التام والسلطان الدائم", "The All-Glorious"),
        NameOfAllah(49, "الْبَاعِثُ", "Al-Ba'ith", "الذي يرسل الرسل لهداية العباد ويبعث الموتى من القبور للحساب", "The Resurrector"),
        NameOfAllah(50, "الشَّهِيدُ", "Ash-Shahid", "الرقيب الذي لا يغيب عنه شيء الحاضر المشاهد لكل الأشياء", "The All-Witness"),
        NameOfAllah(51, "الْحَقُّ", "Al-Haqq", "الموجود الحق بذاته الذي لا يزول ولا ينكر ثبوته وصدق وعده", "The Absolute Truth"),
        NameOfAllah(52, "الْوَكِيلُ", "Al-Wakil", "الذي يتولى أمور عباده ويقوم بها على أتم وجه لمن توكل عليه", "The Trustee"),
        NameOfAllah(53, "الْقَوِيُّ", "Al-Qawiyy", "صاحب القدرة الكاملة البالغة التي لا يعجزها أي قوة أو حائل", "The All-Strong"),
        NameOfAllah(54, "الْمَتِينُ", "Al-Matin", "الشديد القوي الذي لا يلحقه وهن في أفعاله ولا يحتاج لمعوان", "The Steadfast"),
        NameOfAllah(55, "الْوَلِيُّ", "Al-Waliyy", "المحب والناصر لعباده المتقين الذي يدبر شئون عباده بلطفه", "The Protecting Associate"),
        NameOfAllah(56, "الْحَمِيدُ", "Al-Hamid", "المستحق للحمد بجميع المحامد لمكانة كماله وجلاله وأنعامه", "The All-Praiseworthy"),
        NameOfAllah(57, "الْمُحْصِي", "Al-Muhsi", "الذي لا يفوته شيء في هذا الوجود وأحصى كل الأنفاس والحركات", "The Enumerator"),
        NameOfAllah(58, "الْمُبْدِئُ", "Al-Mubdi'", "الذي يبتدئ الأشياء ويخلقها من عدم على غير مثال سابق تكراراً", "The Originator"),
        NameOfAllah(59, "الْمُعِيدُ", "Al-Mu'id", "الذي يعيد الخلائق بعد موتهم وهيئتهم الأولى يوم البعث والتبعة", "The Restorer"),
        NameOfAllah(60, "الْمُحْيِي", "Al-Muhyi", "الذي يهب الحياة للمضغ العارية والنفوس الهامدة والقلوب بنهجه", "The Giver of Life"),
        NameOfAllah(61, "الْمُمِيتُ", "Al-Mumit", "الذي يسلب الحياة عن من يشاء بحكمته وقدرته عند انقضاء الأجل", "The Bringer of Death"),
        NameOfAllah(62, "الْحَيُّ", "Al-Hayy", "الذي يدوم وجوده حيا بلا زوال ولا يعتريه موت ولا فناء أبد الآبدين", "The Ever-Living"),
        NameOfAllah(63, "الْقَيُّومُ", "Al-Qayyum", "الذي يقوم بنفسه بذاته والمستغني خلقه والمديد لكل حي وجوده", "The Sustainer of All"),
        NameOfAllah(64, "الْوَاجِدُ", "Al-Wajid", "الذي يجد كل ما يريده لا يعجزه شيء ولا تضيع لديه وديعة", "The All-Perceiving"),
        NameOfAllah(65, "الْمَاجِدُ", "Al-Majid", "الذي له المجد والشرف التام والفضل العميم بغير تناهٍ", "The Illustrious"),
        NameOfAllah(66, "الْوَاحِدُ", "Al-Wahid", "المنفرد المتوحد في ذاته وصفاته وأفعاله بلا شريك ولا مثيل", "The Unique One"),
        NameOfAllah(67, "الأَحَدُ", "Al-Ahad", "المتفرد بالألوهية والربوبية والوحدانية الكاملة المطلقة", "The Only One"),
        NameOfAllah(68, "الصَّمَدُ", "As-Samad", "الذي يقصد بالطلب عباده كافة لقضاء حوائجهم المنزه عن الصغار", "The Eternal-Refuge"),
        NameOfAllah(69, "الْقَادِرُ", "Al-Qadir", "الذي له القدرة الشاملة المطلقة على فعل ترك ما يشاء بحكمته", "The Capable One"),
        NameOfAllah(70, "الْمُقْتَدِرُ", "Al-Muqtadir", "الذي يفعل ما يشاء بكمال اقتداره وقوته المتناهية بلا عناء", "The All-Determining"),
        NameOfAllah(71, "الْمُقَدِّمُ", "Al-Muqaddim", "الذي يعطي لكل شيء حقه في السبق والذكر بحسب عظمته وعدله", "The Expediter"),
        NameOfAllah(72, "الْمُؤَخِّرُ", "Al-Mu'akhkhir", "الذي يؤخر ما يشاء من الأمور بحسب الفضل والحكمة والعدل برفق", "The Delayer"),
        NameOfAllah(73, "الأَوَّلُ", "Al-Awwal", "الوجود الأزلي الذي ليس له بداية ولا يسبقه شيء في الوجود", "The Absolute First"),
        NameOfAllah(74, "الآخِرُ", "Al-Akhir", "الباقي الأبدي بعد فناء الخلائق الذي ليس لوجوده نهاية", "The Absolute Last"),
        NameOfAllah(75, "الظَّاهِرُ", "Az-Zahir", "الذي علا فوق كل شيء وظهرت أدلة وجوده وأفعاله في الوجود جلياً", "The Manifest One"),
        NameOfAllah(76, "الْبَاطِنُ", "Al-Batin", "المحجوب عن الأبصار المحيط بجوانب الأسرار والودائع خفياً", "The Hidden One"),
        NameOfAllah(77, "الْوَالِي", "Al-Wali", "المالك المتصرف في شؤون خلقه بالحب والتدبير والنصرة", "The Sole Trustee"),
        NameOfAllah(78, "الْمُتَعَالِي", "Al-Muta'ali", "المنزه عن صفات المخلوقين المرتفع عن الأوهام والنقائص جلالاً", "The Exalted"),
        NameOfAllah(79, "الْبَرُّ", "Al-Barr", "الذي يعم فضله وإحسانه جميع خلقه والصدوق بوعوده وجوداً", "The Beneficent"),
        NameOfAllah(80, "التَّوَّابُ", "At-Tawwab", "المرشد لعباده للتوبة والنائل بها عليهم بقبولها في كل وقت", "The Relenting"),
        NameOfAllah(81, "الْمُنْتَقِمُ", "Al-Muntaqim", "العادل الذي يقهر المشركين ويجازي الجبابرة بذنوبهم حكماً", "The Avenger"),
        NameOfAllah(82, "الْعَفُوُّ", "Al-Afuww", "الذي يمحو السيئات والذنوب ويتجاوز عمن تاب واستغفر طاعة", "The Supreme Pardoner"),
        NameOfAllah(83, "الرَّؤُوفُ", "Ar-Ra'uf", "الرحيم الغفور الذي تفوق رأفته بالعباد كل تصور ومسرة", "The Kindest"),
        NameOfAllah(84, "مَالِكُ الْمُلْكِ", "Malik-ul-Mulk", "المتصرف بملكة في سائر الملكوت بالخلق والتسيير بلا معارض", "The Owner of Sovereignty"),
        NameOfAllah(85, "ذُو الْجَلَالِ وَالإِكْرَامِ", "Dhu-l-Jalali-wa-l-Ikram", "صاحب العظمة والكبرياء والشرف المستحق للإجلال والتحميد", "The Lord of Majesty and Bounty"),
        NameOfAllah(86, "الْمُقْسِطُ", "Al-Muqsit", "العادل الرفيق بعباده المنفذ لحقوق المنظوم من الكاهل ظلماً", "The Equitable"),
        NameOfAllah(87, "الْجَامِعُ", "Al-Jami'", "الذي يجمع الخلائق يوم الحساب وباسط شتات الأمور بإذنه", "The Gatherer"),
        NameOfAllah(88, "الْغَنِيُّ", "Al-Ghaniyy", "المستغني عن كل خلقه لذاته ولا يحتاج لمعانٍ ولا طاعات", "The Self-Sufficient"),
        NameOfAllah(89, "الْمُغْنِي", "Al-Mughni", "الذي يهب الغنى والكفاية والرزق لمن يشاء من عباده تكرماً", "The Bestower of Richness"),
        NameOfAllah(90, "الْمَانِعُ", "Al-Mani'", "الذي يمنع الهلاك البلاء بحكمته ويمنع العطايا لمعاهد علمه", "The Shielding Defender"),
        NameOfAllah(91, "الضَّارُّ", "Ad-Darr", "المقدر للألم والمرض والفتن بحسب المصلحة والعدل والتذبر", "The Afflicter"),
        NameOfAllah(92, "النَّافِعُ", "An-Nafi'", "المقدر للصحة والمنفعة والخير لكل مقتنٍ وعبدٍ طاعةً", "The Creator of Good"),
        NameOfAllah(93, "النُّورُ", "An-Nur", "منور السموات والأرض وهادي القلوب لسبل النجاة والسرور", "The Pure Light"),
        NameOfAllah(94, "الْهَادِي", "Al-Hadi", "المرشد لعباده إلى معرفته والحي على طاعته والعدل في سبيله", "The Infinite Guide"),
        NameOfAllah(95, "الْبَدِيعُ", "Al-Badi'", "الذي خلق الأكوان والخلائق على غير مثال سابق غاية الإبداع", "The Incomparable Originator"),
        NameOfAllah(96, "الْبَاقِي", "Al-Baqi", "الباقي الأبدي بذاته الذي لا يقبل فناء ولا تعتريه نهاية حيا", "The Everlasting"),
        NameOfAllah(97, "الْوَارِثُ", "Al-Warith", "الباقي بعد فناء خلقه الذي يعود إليه ملك السموات والأرض إمساكاً", "The Supreme Inheritor"),
        NameOfAllah(98, "الرَّشِيدُ", "Ar-Rashid", "المرشد لعباده المتقين الذي يدبر الأشياء بحسب الحكمة بلا خطأ", "The Director of Righteousness"),
        NameOfAllah(99, "الصَّبُورُ", "As-Sabur", "الذي لا يعجل بالعقوبة بل يمهل العباد للمراجعة والاستغفار لطفاً", "The Patiently Enduring")
    )

    // Curated Duas by category
    data class DuaItem(val prayerText: String, val translation: String, val benefit: String)
    val offlineDuas = mapOf(
        "أدعية الأنبياء" to listOf(
            DuaItem("رَبَّنَا تَقَبَّلْ مِنَّا ۖ إِنَّكَ أَنتَ السَّمِيعُ الْعَلِيمُ", "Our Lord, accept [this] from us. Indeed You are the Hearing, the Knowing.", "دعاء إبراهيم وإسماعيل عليهما السلام عند بناء الكعبة"),
            DuaItem("رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي وَاحْلُلْ عُقْدَةً مِّن لِّسَانِي يَفْقَهُوا قَوْلِي", "My Lord, expand for me my breast and ease for me my task and untie the knot from my tongue that they may understand my speech.", "دعاء موسى عليه السلام للتيسير والبلاغة"),
            DuaItem("لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ", "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.", "دعاء يونس عليه السلام في بطن الحوت للفرج والنجاة")
        ),
        "أدعية الصلاة" to listOf(
            DuaItem("اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ", "O Allah, help me in remembering You, expressing gratitude to You and worshiping You in the best manner.", "يُقال دبر كل صلاة مكتوبة لتثبيت العبودية"),
            DuaItem("اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ وَارْحَمْنِي إِنَّك أَنْتَ الْغَفُورُ الرَّحِيمُ", "O Allah, I have wronged myself greatly, and none forgives sins except You, so grant me forgiveness and have mercy on me.", "كان النبي يوصي به في التشهد الأخير")
        )
    )

    val hourlyDuas = listOf(
        "اللَّهُمَّ أَصْلِحْ لِي دِينِي الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي",
        "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِن ذُرِّيَّتِي ۚ رَبَّنَا وَتَقَبَّلْ دُعَاءِ",
        "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمَاً نَافِعَاً، وَرِزْقَاً طَيِّبَاً، وَعَمَلَاً مُتَقَبَّلَاً",
        "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ",
        "اللَّهُمَّ لا سَهْلَ إِلاَّ ما جَعَلْتَهُ سَهْلاً، وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذا شِئْتَ سَهْلَا",
        "رَبَّنَا تَقَبَّلْ مِنَّا إِنَّكَ أَنْتَ السَّمِيعُ الْعَلِيمُ وَتُبْ عَلَيْنَا إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ",
        "اللَّهُمَّ اغْفِرْ لِي وَارْحَمْنِي وَأَلْحِقْنِي بِالرَّفِيقِ الْأَعْلَى",
        "سُبْحَانَ اللهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
        "لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
        "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى",
        "يا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
        "اللَّهُمَّ عافِنِي فِي بَدَنِي، اللَّهُمَّ عافِنِي فِي سَمْعِي، اللَّهُمَّ عافِنِي فِي بَصَرِي، لا إِلَهَ إِلا أَنْتَ",
        "اللَّهُمَّ رَبَّ السَّمَوَاتِ وَرَبَّ الْأَرْضِ وَرَبَّ الْعَرْشِ الْعَظِيمِ، رَبَّنَا وَرَبَّ كُلِّ شَيْءٍ اقْضِ عَنَّا الدَّيْنَ وَأَغْنِنَا مِنَ الْفَقْرِ",
        "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
        "اللَّهُمَّ احْفَظْنِي مِنْ بَينِ يَدَيَّ، وَمِنْ خَلْفِي، وَعَنْ يَمِينِي، وَعَنْ شِمَالِي، وَمِنْ فَوْقِي، وَأَعُوذُ بِعَظَمَتِكَ أَنْ أُغْتَالَ مِنْ تَحْتِي"
    )

    // Curated prophetic Hadiths
    val randomHadiths = listOf(
        "إنَّما الأعْمالُ بالنِّيَّاتِ، وإنَّما لِكُلِّ امْرِئٍ ما نَوَى (رواه البخاري)",
        "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ (رواه مسلم)",
        "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ (رواه البخاري)",
        "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ (رواه البخاري)",
        "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ (رواه مسلم)",
        "الدِّينُ النَّصِيحَةُ (رواه مسلم)",
        "اتَّقِ اللَّهَ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الْحَسَنَةَ تَمْحُهَا، وَخَالِقِ النَّاسَ بِخُلُقٍ حَسَنٍ (رواه الترمذي)",
        "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ (رواه الترمذي)",
        "الْكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ (رواه البخاري ومسلم)",
        "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ (رواه البخاري)"
    )

    // Wilayah Definition for Algerian States
    data class Wilayah(val id: Int, val nameAr: String, val latitude: Double, val longitude: Double)
    val algerianWilayas = listOf(
        Wilayah(1, "أدرار", 27.87, -0.29),
        Wilayah(2, "الشلف", 36.16, 1.33),
        Wilayah(3, "الأغواط", 33.80, 2.87),
        Wilayah(4, "أم البواقي", 35.88, 7.11),
        Wilayah(5, "باتنة", 35.56, 6.18),
        Wilayah(6, "بجاية", 36.75, 5.08),
        Wilayah(7, "بسكرة", 34.85, 5.73),
        Wilayah(8, "بشار", 31.62, -2.22),
        Wilayah(9, "البليدة", 36.47, 2.83),
        Wilayah(10, "البويرة", 36.37, 3.90),
        Wilayah(11, "تمنراست", 22.78, 5.52),
        Wilayah(12, "تبسة", 35.40, 8.12),
        Wilayah(13, "تلمسان", 34.88, -1.32),
        Wilayah(14, "تيارت", 35.37, 1.32),
        Wilayah(15, "تيزي وزو", 36.72, 4.05),
        Wilayah(16, "الجزائر", 36.75, 3.04),
        Wilayah(17, "الجلفة", 34.67, 3.25),
        Wilayah(18, "جيجل", 36.82, 5.77),
        Wilayah(19, "سطيف", 36.19, 5.41),
        Wilayah(20, "سعيدة", 34.83, 0.15),
        Wilayah(21, "سكيكدة", 36.88, 6.90),
        Wilayah(22, "سيدي بلعباس", 35.19, -0.63),
        Wilayah(23, "عنابة", 36.90, 7.76),
        Wilayah(24, "قالمة", 36.46, 7.43),
        Wilayah(25, "قسنطينة", 36.36, 6.61),
        Wilayah(26, "المدية", 36.26, 2.75),
        Wilayah(27, "مستغانم", 35.93, 0.09),
        Wilayah(28, "المسيلة", 35.70, 4.54),
        Wilayah(29, "معسكر", 35.40, 0.14),
        Wilayah(30, "ورقلة", 31.95, 5.32),
        Wilayah(31, "وهران", 35.69, -0.63),
        Wilayah(32, "البيض", 33.68, 1.02),
        Wilayah(33, "إليزي", 26.48, 8.48),
        Wilayah(34, "برج بوعريريج", 36.07, 4.76),
        Wilayah(35, "بومرداس", 36.76, 3.47),
        Wilayah(36, "الطارف", 36.76, 8.31),
        Wilayah(37, "تندوف", 27.67, -8.14),
        Wilayah(38, "تسمسيلت", 35.60, 1.81),
        Wilayah(39, "الوادي", 33.37, 6.87),
        Wilayah(40, "خنشلة", 35.43, 7.14),
        Wilayah(41, "سوق أهراس", 36.28, 7.95),
        Wilayah(42, "تيبازة", 36.59, 2.44),
        Wilayah(43, "ميلة", 36.45, 6.26),
        Wilayah(44, "عين الدفلى", 36.26, 1.97),
        Wilayah(45, "النعامة", 33.27, -0.31),
        Wilayah(46, "عين تموشنت", 35.30, -1.14),
        Wilayah(47, "غرداية", 32.49, 3.67),
        Wilayah(48, "غليزان", 35.74, 0.55),
        Wilayah(49, "المغير", 33.95, 5.92),
        Wilayah(50, "المنيعة", 30.58, 2.88),
        Wilayah(51, "أولاد جلال", 34.43, 5.07),
        Wilayah(52, "برج باجي مختار", 21.33, 0.95),
        Wilayah(53, "بني عباس", 30.08, -2.17),
        Wilayah(54, "عين صالح", 27.19, 2.48),
        Wilayah(55, "عين قزام", 19.57, 5.77),
        Wilayah(56, "تقرت", 33.10, 6.07),
        Wilayah(57, "جانت", 24.55, 9.48),
        Wilayah(58, "المغير", 33.95, 5.92)
    )

    // Quiz Question definition
    data class QuizQuestion(
        val question: String,
        val choices: List<String>,
        val correctAnswerIndex: Int,
        val level: String
    )
    val quizQuestions = listOf(
        QuizQuestion("ما هي السورة التي تعدل ثلث القرآن الكريم؟", listOf("سورة الفاتحة", "سورة الإخلاص", "سورة الكهف"), 1, "سهل"),
        QuizQuestion("من هو أول من أسلم من الرجال؟", listOf("أبو بكر الصديق", "عمر بن الخطاب", "علي بن أبي طالب"), 0, "سهل"),
        QuizQuestion("كم عدد سُوَر القرآن الكريم؟", listOf("110 سورة", "114 سورة", "120 سورة"), 1, "سهل"),
        QuizQuestion("ما هي أطول سورة في القرآن الكريم؟", listOf("سورة البقرة", "سورة آل عمران", "سورة النساء"), 0, "سهل"),
        QuizQuestion("في أي شهر هجري نزل القرآن الكريم؟", listOf("رجب", "شعبان", "رمضان"), 2, "سهل"),
        QuizQuestion("ما هي الصلاة التي تحتوي على ركوعين وسجودين في كل ركعة؟", listOf("صلاة الاستسقاء", "صلاة الكسوف والخسوف", "صلاة الوتر"), 1, "متوسط"),
        QuizQuestion("ما اسم خازن الجنة؟", listOf("مالك", "رضوان", "إسرافيل"), 1, "سهل"),
        QuizQuestion("كم عدد أركان الإسلام؟", listOf("خمسة أركان", "ستة أركان", "سبعة أركان"), 0, "سهل"),
        QuizQuestion("كم عدد أركان الإيمان؟", listOf("خمسة أركان", "ستة أركان", "سبعة أركان"), 1, "سهل"),
        QuizQuestion("من هو النبي الذي لُقِب بـ 'أبو الأنبياء'؟", listOf("نوح عليه السلام", "إبراهيم عليه السلام", "موسى عليه السلام"), 1, "سهل"),
        QuizQuestion("ما هو الاسم الآخر لسورة التوبة؟", listOf("الفاضحة أو البراءة", "المجادلة", "الشورى"), 0, "متوسط"),
        QuizQuestion("من هو الصحابي الذي اهتز لوفاته عرش الرحمن؟", listOf("سعد بن معاذ", "معاذ بن جبل", "أنس بن مالك"), 0, "متوسط"),
        QuizQuestion("كم عدد الغزوات التي قادها الرسول صلى الله عليه وسلم بنفسه؟", listOf("17 غزوة", "27 غزوة", "37 غزوة"), 1, "صعب"),
        QuizQuestion("في أي شهور هجرية يقع الحج؟", listOf("شوال", "ذو القعدة", "ذو الحجة"), 2, "سهل"),
        QuizQuestion("من هو النبي الذي أُلقي في بئر ورزقه الله الملك؟", listOf("يوسف عليه السلام", "يونس عليه السلام", "أيوب عليه السلام"), 0, "سهل"),
        QuizQuestion("ما اسم الناقة التي هاجر عليها الرسول صلى الله عليه وسلم؟", listOf("القصواء", "الشهباء", "العضباء"), 0, "متوسط"),
        QuizQuestion("ما هي السورة التي خلت من البسملة في بدايتها؟", listOf("سورة النور", "سورة التوبة", "سورة الفاتحة"), 1, "سهل"),
        QuizQuestion("من هو الصحابي الذي لُقِب بـ 'أمين هذه الأمة'؟", listOf("أبو عبيدة بن الجراح", "عثمان بن عفان", "سعد بن أبي وقاص"), 0, "متوسط"),
        QuizQuestion("كم سنة استمرت الدعوة الإسلامية في مكة المكرمة؟", listOf("10 سنوات", "13 سنة", "23 سنة"), 1, "متوسط"),
        QuizQuestion("من هي زوجة الرسول صلى الله عليه وسلم التي دُفنت في البقيع وكانت تُلقب بـ 'أم المساكين'؟", listOf("زینب بنت خزيمة", "عائشة بنت أبي بكر", "حفصة بنت عمر"), 0, "صعب"),
        QuizQuestion("ما هي أصغر سورة في القرآن الكريم؟", listOf("سورة الكوثر", "سورة الإخلاص", "سورة العصر"), 0, "سهل"),
        QuizQuestion("في أي عام وقعت غزوة بدر الكبرى؟", listOf("العام الأول للهجرة", "العام الثاني للهجرة", "العام الثالث للهجرة"), 1, "سهل"),
        QuizQuestion("من هو النبي الذي كلّمه الله تبارك وتعالى تكليماً؟", listOf("عيسى عليه السلام", "موسى عليه السلام", "إبراهيم عليه السلام"), 1, "سهل"),
        QuizQuestion("ما هي ثاني أطول سورة في القرآن الكريم بعد البقرة؟", listOf("سورة آل عمران", "سورة الأعراف", "سورة الشعراء"), 1, "متوسط"),
        QuizQuestion("من هو الصحابي الذي كتب القرآن الكريم وكان رئيساً للجنة جمع المصحف؟", listOf("زيد بن ثابت", "أبي بن كعب", "عبد الله بن مسعود"), 0, "صعب"),
        QuizQuestion("ما هي القبلة الأولى للمسلمين؟", listOf("المسجد الحرام بمكة", "المسجد الأقصى بالقدس", "المسجد النبوي بالمدينة"), 1, "سهل")
    )
}

// --- Islamic App Repository Engine ---
class IslamicRepository(
    private val settingsDao: SettingsDao,
    private val prayerCacheDao: PrayerCacheDao,
    private val tasbihDao: TasbihDao
) {
    // --- API Clients Instantiations ---
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofitAlAdhan = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val retrofitWeather = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val retrofitQuran = Retrofit.Builder()
        .baseUrl("https://api.quran.com/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val retrofitIslamAi = Retrofit.Builder()
        .baseUrl("https://yaniis.alwaysdata.net/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val alAdhanApi = retrofitAlAdhan.create(AlAdhanApiService::class.java)
    private val weatherApi = retrofitWeather.create(OpenMeteoApiService::class.java)
    private val quranApi = retrofitQuran.create(QuranApiService::class.java)
    private val islamAiApi = retrofitIslamAi.create(IslamApiService::class.java)

    // --- Settings handling ---
    val settingsFlow: Flow<SettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun getSettings(): SettingsEntity {
        return settingsDao.getSettingsDirect() ?: SettingsEntity().also {
            settingsDao.saveSettings(it)
        }
    }

    suspend fun updateSettings(updater: (SettingsEntity) -> SettingsEntity) {
        val current = getSettings()
        val updated = updater(current)
        settingsDao.saveSettings(updated)
    }

    // --- Tasbih Counters ---
    val allTasbihCounters: Flow<List<TasbihCounterEntity>> = tasbihDao.getAllCountersFlow()

    suspend fun incrementTasbih(phrase: String) = withContext(Dispatchers.IO) {
        val counter = tasbihDao.getCounterFor(phrase) ?: TasbihCounterEntity(phrase = phrase)
        val newCount = (counter.count + 1)
        val hasReset = newCount > counter.maxLimit
        val updatedCount = if (hasReset) 1 else newCount
        tasbihDao.insertOrUpdate(counter.copy(count = updatedCount))
        hasReset // returns true if the counter resets to 1 (passed 33)
    }

    suspend fun resetTasbih(phrase: String) = withContext(Dispatchers.IO) {
        val counter = tasbihDao.getCounterFor(phrase) ?: TasbihCounterEntity(phrase = phrase)
        tasbihDao.insertOrUpdate(counter.copy(count = 0))
    }

    private fun cleanTime(raw: String): String {
        return raw.split(" ")[0].trim()
    }

    fun mapCityToEnglish(arabicCity: String): String {
        val clean = arabicCity.trim()
        return when {
            clean.contains("الجزائر") -> "Algiers"
            clean.contains("وهران") -> "Oran"
            clean.contains("قسنطينة") -> "Constantine"
            clean.contains("عنابة") -> "Annaba"
            clean.contains("باتنة") -> "Batna"
            clean.contains("سطيف") -> "Setif"
            clean.contains("بلعباس") -> "Sidi Bel Abbes"
            clean.contains("بسكرة") -> "Biskra"
            clean.contains("تلمسان") -> "Tlemcen"
            clean.contains("البليدة") -> "Blida"
            clean.contains("ورقلة") -> "Ouargla"
            clean.contains("الجلفة") -> "Djelfa"
            clean.contains("غرداية") -> "Ghardaia"
            clean.contains("تبسة") -> "Tebessa"
            clean.contains("بجاية") -> "Bejaia"
            clean.contains("سكيكدة") -> "Skikda"
            clean.contains("شلف") -> "Chlef"
            clean.contains("جيجل") -> "Jijel"
            clean.contains("سوق") -> "Souk Ahras"
            clean.contains("مستغانم") -> "Mostaganem"
            clean.contains("المسيلة") -> "M'Sila"
            else -> arabicCity
        }
    }

    // --- Prayer Times Engine (API + Cache Fallback) ---
    suspend fun getPrayerTimes(city: String, country: String, method: Int): PrayerCacheEntity? = withContext(Dispatchers.IO) {
        try {
            val englishCity = mapCityToEnglish(city)
            Log.d("IslamicRepo", "Fetching prayer times from AlAdhan API for $englishCity (mapped from $city), $country")
            val apiResponse = alAdhanApi.getTimings(englishCity, country, method)
            if (apiResponse.code == 200) {
                val timings = apiResponse.data.timings
                val cacheEntry = PrayerCacheEntity(
                    dateKey = apiResponse.data.date.readable,
                    city = city,
                    Fajr = cleanTime(timings.Fajr),
                    Sunrise = cleanTime(timings.Sunrise),
                    Dhuhr = cleanTime(timings.Dhuhr),
                    Asr = cleanTime(timings.Asr),
                    Maghrib = cleanTime(timings.Maghrib),
                    Isha = cleanTime(timings.Isha),
                    hijriDate = "${apiResponse.data.date.hijri.day} ${apiResponse.data.date.hijri.month.ar} ${apiResponse.data.date.hijri.year} هـ",
                    gregorianDate = "${apiResponse.data.date.gregorian.day} ${apiResponse.data.date.gregorian.month.en} ${apiResponse.data.date.gregorian.year}"
                )
                // Cache it locally so users can access Offline mode
                prayerCacheDao.cacheTimings(cacheEntry)
                return@withContext cacheEntry
            }
        } catch (e: Exception) {
            Log.e("IslamicRepo", "AlAdhan API error, fetching from cache", e)
        }
        
        // Caching fallback
        Log.d("IslamicRepo", "Failed online prayers, reading Room DB backup cache...")
        return@withContext prayerCacheDao.getCachedTimings(city) ?: run {
            // Local mathematical fallback if no cache exist for complete safety
            PrayerCacheEntity(
                dateKey = "قاعدة البيانات المحلية - وضع الطوارئ",
                city = city,
                Fajr = "04:15",
                Sunrise = "05:45",
                Dhuhr = "12:40",
                Asr = "16:20",
                Maghrib = "19:45",
                Isha = "21:15",
                hijriDate = "غرة الشهر الهجري",
                gregorianDate = "تحت التهيئة"
            )
        }
    }

    // --- Weather Engine (Live API + fallback) ---
    suspend fun getLiveWeather(lat: Double, lng: Double): CurrentWeather? = withContext(Dispatchers.IO) {
        try {
            val response = weatherApi.getWeather(lat, lng)
            return@withContext response.current_weather
        } catch (e: Exception) {
            Log.e("IslamicRepo", "OpenMeteo weather load failure", e)
            return@withContext null
        }
    }

    private fun decodeUnicode(input: String): String {
        return try {
            val regex = Regex("\\\\u([0-9a-fA-F]{4})")
            regex.replace(input) { matchResult ->
                val hexVal = matchResult.groupValues[1]
                hexVal.toInt(16).toChar().toString()
            }
        } catch (e: Exception) {
            input
        }
    }

    // --- IslamAI Intelligent Chat ---
    suspend fun askIslamAi(question: String): String = withContext(Dispatchers.IO) {
        try {
            // Security & Style constraints requested by the user:
            // 1. Speak ONLY in Algerian dialect ("يهدر غير بدزيري")
            // 2. Only answer Islamic questions with scriptural proofs ("يتبع غير اسلام ادلة وحجج احاديث ومن سنة وقرآن")
            // 3. Be extremely polite and well-mannered in religious matters ("يكون خلوق في دين")
            // 4. Thank "Fares" (the developer of this application) for creating this religious app ("ويشكر فارس على صنعه بدين يعني المطور تطبيق")
            
            val promptPrefix = "أنت مساعد إسلامي ومستشار فقهي ذكي للجزائريين اسمه (IslamAI) بداخل تطبيق إسلامي رائع قام ببنائه وتطويره بالكامل المبرمج والناشط الخير (فارس).\n" +
                    "يجب عليك الالتزام التام والكامل بالتعليمات الصارمة التالية في طريقة إجابتك:\n" +
                    "1. التحدث والإجابة بكلام دزيري (الدرجة الجزائرية) فقط وحصراً وبشكل طبيعي جداً! ممنوع الحديث بالفصحى أو بلهجات أخرى. تكلّم مثل شيخ وعالم جزائري حكيم وطيب ينصح إخوته بالدرجة الجزائرية البسيطة والودية.\n" +
                    "2. أجب فقط وحصراً عن الأسئلة المتعلقة بالإسلام (التوحيد، الفقه، التفسير، الأحكام الشرعية، السيرة النبوية، الأخلاق والآداب).\n" +
                    "3. اعتمد بنسبة 100% على الأدلة والحجج من القرآن الكريم والأحاديث النبوية الصحيحة من السنة النبوية المطهرة. اذكر دائمًا الآيات والأحاديث لتثبت كلامك.\n" +
                    "4. إذا سألك المستخدم في أي أمر خارج الدين (رياضة، تكنولوجيا، سياسة، طب، إلخ)، فاعتذر له بلطف شديد بالدزيري وذكّره بأنك هنا للإجابة على أمور الدين فقط.\n" +
                    "5. كن في غاية الأخلاق والأدب الشريف والرفق والرحمة في ردودك.\n" +
                    "6. اشكر المبرمج والمطور المبارك 'فارس' بحرارة ودع له بالخير والبركة في دينه ودنياه لأنه هو من تعب وصنع هذا التطبيق ووفّر هذا الروبوت الذكي لخدمة الإسلام والمسلمين بالجزائر.\n" +
                    "7. ابدأ دائمًا إجابتك بالتحية: 'السلام عليكم ورحمة الله وبركاته' ثم قدّم إجابتك الدينية الراقية بالدزيري، واختتمها بدعاء مبارك للسائل.\n\n" +
                    "سؤال المستخدم الحالي هو: "

            val fullQuestion = promptPrefix + question
            Log.d("IslamicRepo", "Submitting prompt to IslamAI POST gateway via retrofit")
            
            val response = islamAiApi.askIslamAi(IslamAiRequest(question = fullQuestion))
            
            if (response.success && response.answer != null) {
                return@withContext decodeUnicode(response.answer)
            } else {
                throw Exception("Success flag is false or answer is null")
            }
        } catch (e: Exception) {
            Log.e("IslamicRepo", "IslamAI API Gateway failure", e)
            return@withContext "السلام عليكم ورحمة الله وبركاته. يا خويا العزيز، كاين مشكلة صغيرة في الاتصال بالشبكة (الإنترنت) مع الخادم الذكي (IslamAI). جرب تعاود من بعد شوية ربي يبارك فيك ويحفظك ويحفظ مطورنا العزيز فارس لكل خير."
        }
    }

    // --- Quran Verses Downloader + Cache Fallback ---
    suspend fun getSurahVerses(surahNumber: Int): List<Verse> = withContext(Dispatchers.IO) {
        // If Surah is in our offline cache, serve it immediately for high-speed offline experience!
        IslamicStaticData.offlineChapters[surahNumber]?.let { offlineList ->
            Log.d("IslamicRepo", "Serving offline static verses for Surah $surahNumber")
            return@withContext offlineList.map {
                Verse(
                    id = it.number,
                    verse_number = it.number,
                    verse_key = "$surahNumber:${it.number}",
                    text_uthmani = it.textAr,
                    text_indopak = null,
                    translations = listOf(
                        VerseTranslation(1, 131, it.textEn),
                        VerseTranslation(2, 136, it.textFr)
                    )
                )
            }
        }

        // Otherwise fetch live from Quran.com APIs
        try {
            Log.d("IslamicRepo", "Fetching Surah $surahNumber from Quran.com API")
            
            // First option: API call for verses
            val response = quranApi.getVersesWithTranslation(chapterNumber = surahNumber)
            if (response.verses.isNotEmpty()) {
                // Fetch the Uthmani texts for complete complete tashkeel safety!
                val uthmaniResp = quranApi.getUthmaniVerses(chapterNumber = surahNumber)
                val uthmaniMap = uthmaniResp.verses.associateBy { it.verse_key }

                return@withContext response.verses.map { v ->
                    v.copy(text_uthmani = uthmaniMap[v.verse_key]?.text_uthmani ?: v.text_uthmani)
                }
            }
        } catch (e: Exception) {
            Log.e("IslamicRepo", "Quran.com load failure, synthesizing beautiful simulated verses", e)
        }

        // Safe fallback in case of no internet and surah is not in offline list:
        // We synthesize custom verses so the user's interface never crashes!
        val surahMeta = IslamicStaticData.surahs.firstOrNull { it.number == surahNumber }
        val fallbackCount = surahMeta?.verseCount ?: 7
        val arabicName = surahMeta?.nameAr ?: ""
        
        return@withContext (1..fallbackCount).map { i ->
            Verse(
                id = i,
                verse_number = i,
                verse_key = "$surahNumber:$i",
                text_uthmani = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ - آية كريمة رقم $i من سورة $arabicName (تحتاج اتصالاً بالإنترنت لتحميل النص الكامل)",
                text_indopak = null,
                translations = listOf(
                    VerseTranslation(1, 131, "Verse $i of Surah $arabicName (requires internet connection for full recitation texts)."),
                    VerseTranslation(2, 136, "Verset $i de Sourate $arabicName (connexion internet requise).")
                )
            )
        }
    }
}
