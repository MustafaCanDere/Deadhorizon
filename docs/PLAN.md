# Zombi Kıyamet Modu – Plan (Minecraft 1.20.1)

Çalışma adı: **Dead Horizon** (mod id: `deadhorizon`) — istersen değiştiririz.

## 1. Teknik seçimler

| Konu | Seçim | Neden |
|---|---|---|
| Mod yükleyici | **Fabric 1.20.1** (Fabric Loader + Fabric API) | Kullanıcı seçimi. Hafif, hızlı açılır, Sodium/Iris gibi performans modlarıyla uyumlu. |
| Dil / JDK | Java 17 | 1.20.1'in zorunlu sürümü. |
| Build | Gradle + Fabric Loom, Yarn eşlemeleri | Fabric'in standart araç zinciri. |
| Oyuncu verisi | Cardinal Components API | Enfeksiyon, susuzluk, kanama gibi oyuncuya bağlı veriler için. |
| Modeller / animasyon | Vanilla zombi modeli + özel dokular (Faz 1), GeckoLib 4 Fabric (Faz 3+, özel zombiler ve silah animasyonları için) | Başta bağımlılıksız başlayıp, gerektiğinde animasyon eklemek. |
| Ayarlar | JSON config (`config/deadhorizon.json`), ileride Mod Menu ekranı | Her özellik açılıp kapatılabilir, zorluk ayarlanabilir. |
| Veri | Datapack tabanlı loot table, tarif, etiketler | Sonradan kod değiştirmeden ayar. |

Oyuncunun kurması gerekenler: Fabric Loader, Fabric API ve (Faz 3'ten itibaren) GeckoLib. Cardinal Components mod `.jar`'ının içine gömülür.

## 2. Özellik kapsamı

### 2.1 Zombi çeşitleri
| Zombi | Davranış |
|---|---|
| **Yürüyen (Walker)** | Yavaş, kalabalık, gün ışığında yanmaz. Temel tehdit. |
| **Koşucu (Runner)** | Hızlı, düşük can, gece daha agresif. |
| **Tank (Brute)** | Çok yüksek can, geri itme, kapı/blok kırar. |
| **Çığlıkçı (Screamer)** | Oyuncuyu görünce bağırır, çevredeki zombileri çeker. |
| **Kusan (Spitter)** | Uzaktan asit atar, zehir/bulantı verir. |
| **Patlayan (Bloater)** | Ölünce zehirli bulut / patlama. |
| **Sürünen (Crawler)** | Alçak, 1 bloklu boşluklardan geçer, ayağı yaralar. |
| **Zırhlı (Riot/Soldier)** | Zırhlı, ok/silah hasarına dirençli, iyi loot düşürür. |
| **Boğulmuş/Mağara varyantları** | Biyoma göre ortaya çıkan çeşitler. |
| **Boss: Alfa / Abomination** | Nadir, kıyamet seviyesi yükselince ortaya çıkar. |

Ortak AI: kapı kırma, ses/ışık algısı (ateş, koşma, patlama sesi zombileri çeker), tırmanma/üst üste binme (opsiyonel), sürü halinde takip.

### 2.2 Enfeksiyon sistemi
- Zombi saldırısında % şansla **enfeksiyon** (ayarlanabilir).
- Aşamalar: Kuluçka → Ateş/halsizlik → Görüş bozulması → Ölüm ve oyuncunun yerine zombi doğması.
- Tedavi: **Antibiyotik** (yavaşlatır), **Antidot/Aşı** (iyileştirir; nadir loot veya üretim).
- Köylüler de enfekte olup zombi köylüye dönüşebilir.
- HUD'da küçük enfeksiyon göstergesi.

### 2.3 Sürüler (Horde)
- Her X günde bir **Kan Ayı gecesi**: büyük sürü oyuncunun üssüne yürür.
- Gün sayısıyla artan zorluk ("Kıyamet Günü" sayacı).
- Gürültü sistemine bağlı gezgin sürüler.
- Sürü öncesi uyarı (mesaj + ses + kırmızı gökyüzü).

### 2.4 Dünya değişiklikleri
- Pasif hayvan sayısı azalır, gece zombi doğma oranı artar, gündüz de zombi doğar.
- **Yıkık yapılar** (yapı / datapack): terk edilmiş evler, karakol, hastane, askeri kamp, benzin istasyonu, kaza yapmış araç enkazları.
- Köyler: bir kısmı terk edilmiş/yıkık, kalan köylüler barikatlı.
- Opsiyonel: yeni dünyada "dağınık" bloklar (kan lekesi, enkaz, terk edilmiş sandıklar).

### 2.5 Loot ve eşyalar
- **Yakın dövüş:** beyzbol sopası (çivili), levye, matchete, yangın baltası, keskin bıçak. Dayanıklılık sistemi.
- **Menzilli (tam silah sistemi):** yay/arbalet güçlendirmeleri; tabanca, revolver, pompalı, av tüfeği, keskin nişancı tüfeği, **taramalı/otomatik tüfek (AK, M4 tarzı)**, hafif makineli. Şarjör ve yeniden doldurma, ateş modları (tekli/seri/otomatik), geri tepme, nişan alma (ADS), mermi türleri ve mermi üretimi. Eklentiler: dürbün, susturucu (gürültüyü azaltır), uzatılmış şarjör. Silahlar gürültü yapar (zombi çeker).
- **Tıbbi:** bandaj, sargı (kanamayı durdurur), ilk yardım kiti, antibiyotik, antidot.
- **Yiyecek/içecek:** konserve, şişe su, kirli su (kaynatılmalı), enerji barı.
- **Hayatta kalma:** el feneri, barikat tahtası, dikenli tel, ses tuzağı (zombileri bir yere çeker), molotof.
- Yapılarda nadirlik seviyeli loot tabloları.

### 2.6 Hayatta kalma mekanikleri (her biri config ile kapatılabilir)
- **Susuzluk** barı ve su arıtma.
- **Kanama** (ağır vuruşta), **kırık bacak** (yüksekten düşme → yavaşlık, atel ile iyileşir).
- **Gürültü** seviyesi (koşma, silah, kapı → zombileri çeker; eğilerek yürümek sessiz).
- Gece görüşü kısıtlı; el feneri / meşale önemli.

### 2.7 Üs savunması
- Güçlendirilmiş kapılar ve barikat blokları (zombiler hasar verebilir, tamir edilebilir).
- Dikenli tel, sivri kazık tuzakları, alarm bloğu.

## 3. Aşamalı yol haritası

| Faz | İçerik | Sonuç |
|---|---|---|
| **0 – Altyapı** | Fabric örnek mod projesi (Loom), GitHub repo, config dosyası, CI ile `.jar` build. | Boş ama oyunda yüklenen mod. |
| **1 – Çekirdek zombiler** | Walker, Runner, Brute; gündüz doğma, kapı kırma, ses algısı; temel dokular. | Oynanabilir "daha tehlikeli zombiler". |
| **2 – Enfeksiyon + tıbbi eşyalar** | Enfeksiyon capability'si, aşamalar, HUD, bandaj/antibiyotik/antidot. | Zombi ısırığının bir anlamı var. |
| **3 – Özel zombiler** | Screamer, Spitter, Bloater, Crawler, Soldier; GeckoLib modelleri. | Çeşitli tehditler. |
| **4 – Sürüler + zorluk ölçeği** | Kıyamet gün sayacı, Kan Ayı gecesi, gezgin sürüler. | Sürekli artan baskı. |
| **5 – Silahlar ve loot** | Yakın dövüş, tam ateşli silah sistemi (taramalı tüfekler, şarjör, geri tepme, nişan, eklentiler), mermi, loot tabloları. | Hazırlık ve ilerleme hissi. |
| **6 – Dünya / yapılar** | Yıkık yapılar, terk edilmiş köyler, enkazlar. | Kıyamet atmosferi. |
| **7 – Hayatta kalma** | Susuzluk, kanama, kırık bacak, gürültü barı. | Tam hayatta kalma deneyimi. |
| **8 – Üs savunması + Boss** | Barikatlar, tuzaklar, Alfa boss. | Oyun sonu içeriği. |
| **9 – Cila** | Ses efektleri, dil dosyaları (TR + EN), denge, performans. | Yayına hazır sürüm. |

Her faz sonunda indirilebilir bir `.jar` ve kısa test notları gelir.

## 4. Proje yapısı

```
deadhorizon/
├─ build.gradle (Fabric Loom), settings.gradle, gradle.properties
├─ .github/workflows/build.yml          # her push'ta .jar üretir
└─ src/main/
   ├─ java/com/deadhorizon/
   │  ├─ DeadHorizon.java               # ModInitializer (ortak giriş) + DeadHorizonClient.java
   │  ├─ config/                        # ModConfig
   │  ├─ registry/                      # ModEntities, ModItems, ModBlocks, ModSounds, ModEffects
   │  ├─ entity/zombie/                 # her zombi türü + ortak BaseZombie
   │  ├─ entity/ai/                     # ses algısı, kapı kırma, sürü hedefleri
   │  ├─ infection/                     # Cardinal component, aşamalar, tedavi
   │  ├─ horde/                         # gün sayacı, Kan Ayı, sürü doğurucu
   │  ├─ survival/                      # susuzluk, kanama, kırık bacak, gürültü
   │  ├─ item/                          # silahlar, tıbbi, yiyecek
   │  ├─ block/                         # barikat, tuzaklar
   │  ├─ worldgen/                      # yapılar, doğma ayarları
   │  ├─ network/                       # istemci-sunucu paketleri (HUD verisi)
   │  └─ client/                        # renderer, model, HUD overlay
   └─ resources/
      ├─ fabric.mod.json
      ├─ assets/deadhorizon/            # dokular, modeller, sesler, lang (tr_tr, en_us)
      └─ data/deadhorizon/              # loot_tables, recipes, tags, worldgen, structures
```

## 5. Netleştirilmesi gerekenler (varsayılanlar parantez içinde)
1. **Mod yükleyici:** ✓ Karar verildi: Fabric.
2. **Ateşli silahlar:** ✓ Karar verildi: tam sistem, yakın dövüş + menzilli + taramalı tüfekler.
3. **Zorluk:** "hardcore" mu, "dengeli" mi? (Dengeli, config ile ayarlanabilir)
4. **Tek oyunculu mu, sunucuda da mı?** (İkisi de, sunucu uyumlu yazılır)
5. **Repo:** Henüz yok. Kullanıcı `deadhorizon` adında boş bir GitHub reposu açacak; `.jar` dosyaları GitHub Actions ile orada derlenecek.
