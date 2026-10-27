# Android deneme sürümünü kurma

[KlarLernen.apk dosyasını indir](https://github.com/mahirozcan63-crypto/klarmathe-android/releases/download/debug-40584758699-2/KlarLernen.apk).

1. Bu bağlantıyı Android telefonda aç ve APK'yı indir.
2. Dosyalar > İndirilenler bölümünden KlarLernen.apk dosyasına dokun.
3. Android kurulum izni isterse indirmeyi açtığın uygulama için geçici kurulum izni ver. Kurulumdan sonra bu izni kapatabilirsin.
4. Uygulamada açıklama dilini ve günlük hedefini seçip Speichern düğmesine bas. Startcheck ile altı soruluk değerlendirmeyi başlatabilir veya onu atlayabilirsin.
5. Heute üben günlük çalışmayı açar. Yanlışlardan sonra ipucu isteyebilir, benzer soruları Wiederholen bölümünde çalışabilirsin. Fortschritt ekranında gerçek kayıtlarını görürsün.

Android 8.0 ve üzeri gerekir. Bu, Play Store'da yayımlanmış üretim sürümü değildir; debug imzalı cihaz deneme sürümüdür. Google Play geliştirici hesabı veya uygulamada ödeme gerekmez.

## İçerik ve veriler

8 derste toplam 134 özgün temel soru bulunur. Sorular Almanca, açıklamalar isteğe bağlı olarak Türkçe desteklidir. İçeriklerin öğretmen incelemesi ve bütün NRW müfredatıyla eşleştirilmesi henüz yapılmadı.

Uygulama çevrimdışıdır. Canlı yapay zekâ, bulut hesabı, cihazlar arası eşitleme, video servisi ve ödeme bağlantısı bulunmaz. Pro satın alınamaz; mevcut örnek içerik bu yerel sürümde ücretsizdir.

İlerleme yalnızca cihazda saklanır. Profil > Daten exportieren ile yedek alabilirsin. Uygulamayı kaldırmak veya cihaz verilerini silmek yerel ilerlemeyi kaldırır. Veriler uygulamadan dışa aktarılmadıkça başka bir cihaza otomatik taşınmaz.

## Güncelleme

GitHub'daki yeni derlemeler farklı debug imzasıyla oluşabilir. Böyle bir güncelleme mevcut uygulamanın üzerine kurulamayabilir. Her güncellemeden önce verilerini dışa aktar. Kalıcı üretim imzası ve Play Store dağıtımı ayrıca kurulmalıdır.

## Gerçekten yapılan kontroller

27 Ekim 2026 tarihli derlemede 9 yerel matematik kontrolü ve 11 JUnit testi geçti; Android lint, APK derleme ve imza doğrulaması başarılı oldu. JUnit testi 134 sorunun cevap anahtarlarını, eşdeğer denklem adımlarını, yardım kaydını, tekrarları ve durumun yeniden yüklenmesini kapsar.

Android 16 emülatöründe 360 × 800 dp telefon görünümünde ana akış geçti: profil, altı soruluk değerlendirme, yanlış yanıttan sonra yeniden deneme, ipucu, matematik klavyesi, uygulama yeniden açılınca taslağın korunması, benzer soruyla tekrar, ilerleme ve beş soruluk yardımsız deneme. Android 16 sistem geri hareketi de kontrol edildi. Fiziksel telefon testi, dışa aktarma/geri yükleme dosya seçicisi ve paylaşım ekranı henüz ayrıca denenmedi.

[Derleme kaydı](https://github.com/mahirozcan63-crypto/klarmathe-android/actions/runs/40584758699)

APK SHA-256: `2e706abfd1e0b20602bc92f0ae4f607f95dada10310b04d9d30f378192a219f2`.
