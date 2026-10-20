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

### 2.4 Dünya ve yapılar
- Pasif hayvan sayısı azalır, gece zombi doğma oranı artar, gündüz de zombi doğar.
- Köyler: bir kısmı terk edilmiş/yıkık, kalan köylüler barikatlı.
- Dünyaya dağılmış ayrıntılar: kan lekesi, enkaz, terk edilmiş araçlar, çöp yığınları.

**Yapılar** (hepsinin içi döşeli ve yağmalanabilir, nadirlik seviyeli loot):
| Grup | Yapılar |
|---|---|
| Yerleşim | Terk edilmiş ev (3 tip), çiftlik evi + ahır, apartman (çok katlı), karavan parkı |
| Ticari | Market, eczane, silahçı, benzin istasyonu, nalbur, restoran |
| Kamu | Hastane, polis karakolu, okul, itfaiye, hapishane, kilise |
| Askeri | Askeri kontrol noktası, askeri üs/bunker, düşmüş helikopter, telsiz kulesi |
| Yol | Araç enkazlı otoyol parçası, köprü barikatı, otobüs durağı |
| İnsan | Hayatta kalan kampı (tüccarlı), haydut kampı (düşman NPC), karantina bölgesi |
| Şehir | Birkaç yapının yol ağıyla birleştiği küçük yıkık kasaba (nadir) |

### 2.5 Loot ve eşyalar
- **Yakın dövüş:** beyzbol sopası (çivili), levye, matchete, yangın baltası, keskin bıçak. Dayanıklılık sistemi.
- **Menzilli (tam silah sistemi):** yay/arbalet güçlendirmeleri; tabanca, revolver, pompalı, av tüfeği, keskin nişancı tüfeği, **taramalı/otomatik tüfek (AK, M4 tarzı)**, hafif makineli. Şarjör ve yeniden doldurma, ateş modları (tekli/seri/otomatik), geri tepme, nişan alma (ADS), mermi türleri ve mermi üretimi. Eklentiler: dürbün, susturucu (gürültüyü azaltır), uzatılmış şarjör. Silahlar gürültü yapar (zombi çeker).
- **Tıbbi:** bandaj, sargı (kanamayı durdurur), ilk yardım kiti, antibiyotik, antidot.
- **Yiyecek/içecek:** konserve, şişe su, kirli su (kaynatılmalı), enerji barı.
- **Hayatta kalma:** el feneri, barikat tahtası, dikenli tel, ses tuzağı (zombileri bir yere çeker), molotof.
- **Alet ve malzeme:** tamir kiti, koli bandı, hurda metal, çivi, elektronik parça, yakıt bidonu, akü, kumaş.
- **Ekipman:** sırt çantası (3 boy, ekstra envanter), çevik yelek, polis/asker zırhı, gaz maskesi (zehirli bulut ve Kusan'a karşı), gece görüş gözlüğü, telsiz.
- **Atılabilir:** el bombası, sis bombası, ses bombası (zombileri başka yöne çeker), molotof.
- **Tuzak ve savunma eşyaları:** kara mayını, ayı kapanı, sesli alarm teli.
- Yapılarda nadirlik seviyeli loot tabloları.

### 2.6 Hayatta kalma mekanikleri (her biri config ile kapatılabilir)
- **Susuzluk** barı ve su arıtma.
- **Kanama** (ağır vuruşta), **kırık bacak** (yüksekten düşme → yavaşlık, atel ile iyileşir).
- **Gürültü** seviyesi (koşma, silah, kapı → zombileri çeker; eğilerek yürümek sessiz).
- Gece görüşü kısıtlı; el feneri / meşale önemli.
- **Dayanıklılık**: koşmak ve vurmak yorar, ağır zırh daha çok yorar; tükenince koşamazsın ve güçsüzleşirsin.
- **Zombi duyuları**: karanlıkta zor fark edilirsin (elinde ışık yoksa), kanaman varsa kokunu uzaktan alırlar; kafaya isabet eden mermi/ok iki kat hasar.
- **Kirli su**: şişeyle doldurulan su hasta edebilir, kaynatılınca (fırın/kamp ateşi) temiz su olur.

### 2.7 Üs savunması
- Güçlendirilmiş kapılar ve barikat blokları (zombiler hasar verebilir, tamir edilebilir).
- Dikenli tel, sivri kazık tuzakları, alarm bloğu.

### 2.8 Ek mekanikler
- **Yağmalama:** dolaplar, araç bagajları, çöp kutuları "aranır" (kısa bekleme süresi), içerikleri ilk açılışta loot tablosundan oluşur.
- **Elektrik:** jeneratör (yakıtla çalışır, gürültü yapar) ve güneş paneli; kablolarla lamba, buzdolabı, projektör, alarm ve elektrikli çite güç verir.
- **Yemek pişirme ve su:** kamp ateşi/ocakta pişirme, yağmur suyu toplayıcı, su arıtıcı, yemekler bozulur (buzdolabı yavaşlatır).
- **Çalışma tezgahları:** Tezgah → Silah tezgahı → Kimya/ilaç tezgahı; ileri eşyalar ancak bunlarla yapılır.
- **Silah ve alet tamiri:** dayanıklılığı tamir kiti veya hurda ile yenile.
- **Beceri sistemi:** yakın dövüş, atış, ilk yardım, yağmalama, inşaat dalları; kullandıkça seviye atlar ve küçük bonuslar açılır.
- **Yorgunluk ve uyku:** ranza/yatak dinlendirir; uyurken sürü saldırırsa uyanırsın.
- **Hava ve sıcaklık (opsiyonel):** soğuk biyomlarda giysi ve ateş önemli.
- **İnsan NPC'ler:** tüccarlar (hurda/mermi ile takas), haydutlar (silahlı, üslere baskın), kurtarılabilir hayatta kalanlar.
- **Olaylar:** havadan erzak düşürme (dumanla işaretli, zombileri çeker), helikopter geçişi, radyo yayınları (yeni olayları duyurur).
- **Gizlilik:** karanlık ve eğilmek seni zor görünür yapar, zombi kanıyla "kamufle olma" (kısa süre fark edilmezsin).

### 2.9 İşlevsel dekorasyon (iç ve dış)
Her dekor bloğu bir işe yarar; yapıların içinde de doğal olarak bulunur.

**İç mekan**
| Blok | İşlevi |
|---|---|
| Buzdolabı | Depolama, içindeki yemek geç bozulur (elektrik ister) |
| Mutfak dolabı, gardırop, komodin, ecza dolabı | Depolama; yapılarda yağmalanabilir |
| Ocak / fırın | Yemek pişirme, kirli su kaynatma |
| Lavabo | Bağlıysa su verir, bardak/şişe doldurur |
| Küvet | Su deposu |
| Koltuk, sandalye, tabure | Oturulabilir |
| Masa, çalışma masası | Üzerine eşya koyulabilir (sergileme) |
| Ranza / yatak | Uyku, doğma noktası, yorgunluğu giderir |
| Silah dolabı / silah askısı | Silahları sergiler ve saklar |
| Raf | Eşyaları görünür şekilde sergiler |
| Kasa | Kilitlenebilir depolama |
| Masa lambası, tavan lambası | Işık (elektrikli olan daha parlak) |
| Televizyon, radyo | Radyo yayınlarını dinlersin (olay ve erzak duyuruları) |
| Çöp kutusu | İçine atılan eşyayı yok eder, yapılarda yağmalanabilir |
| Harita masası | Etraftaki yapıları haritada gösterir |
| Ecza tezgahı, silah tezgahı | Üretim istasyonları |

**Dış mekan**
| Blok | İşlevi |
|---|---|
| Kum torbası | Mermi geçirmez siper, zombiler kıramaz ama tırmanabilir |
| Dikenli tel, elektrikli çit | Geçeni yavaşlatır ve hasar verir |
| Barikat tahtası (pencere/kapı) | Zombiler vurdukça hasar alır, tamir edilir |
| Çelik kapı ve garaj kapısı | Güçlendirilmiş giriş, kilitlenebilir |
| Gözetleme kulesi parçaları | Yüksek atış noktası |
| Projektör | Geniş alanı aydınlatır, zombi doğmasını azaltır |
| Sokak lambası | Işık (elektrikli) |
| Jeneratör, güneş paneli, akü | Elektrik |
| Yakıt pompası | Benzin istasyonlarında yakıt verir |
| Yağmur toplayıcı, su kulesi | Su |
| Çöp konteyneri, posta kutusu | Yağmalanabilir depolama |
| Terk edilmiş araç (sedan, kamyonet, polis aracı) | Bagajı yağmalanır, parçalanarak hurda/akü/yakıt verir |
| Trafik konisi, beton bariyer, tabela | Yol bariyeri, zombileri yönlendirir |
| Kamp ateşi, mangal | Pişirme ve ısınma |
| Saksı, bahçe yatağı | Hızlı büyüyen sebze tarımı |

## 3. Aşamalı yol haritası

| Faz | İçerik | Sonuç |
|---|---|---|
| **0 – Altyapı** ✓ | Fabric projesi, GitHub repo, config, CI ile `.jar`. | Oyunda yüklenen mod. |
| **1 – Çekirdek zombiler** ✓ | Yürüyen, Koşucu, Tank; gündüz doğma, kapı kırma, ses algısı. | Daha tehlikeli zombiler. |
| **2 – Enfeksiyon + tıbbi eşyalar** ✓ | Enfeksiyon aşamaları, HUD, bandaj, antibiyotik, antidot, kanama. | Isırığın bir anlamı var. |
| **3 – Temel dekorasyon ve yağmalama** ✓ | Dolaplar, buzdolabı, raf, kasa, koltuk/sandalye, masa, lamba, çöp kutusu, kum torbası, barikat tahtası, dikenli tel; yağmalama sistemi. | Üs kurulabiliyor, yapılar doldurulabiliyor. |
| **4 – Özel zombiler** ✓ | Çığlıkçı, Kusan, Patlayan, Sürünen, Zırhlı; GeckoLib modelleri. | Çeşitli tehditler. |
| **5 – Silahlar** (✓ 7 ateşli silah, 5 mermi, 6 yakın dövüş, eklentiler, el bombası/molotof/tuzak bombası, kafa kesme; kalan: 3D modeller, nişan animasyonu) | Yakın dövüş, tam ateşli silah sistemi, mermi, eklentiler, atılabilirler. | Hazırlık ve ilerleme hissi. |
| **6 – Yapılar (1. dalga)** | Evler, market, eczane, benzin istasyonu, polis karakolu, araç enkazları. | Keşfetmeye değer dünya. |
| **7 – Hayatta kalma** (ilk kısmı öne çekildi ✓) | ✓ Susuzluk, ✓ dayanıklılık, ✓ kırık bacak, ✓ zombi duyuları ve kafadan vuruş, ✓ konserve/içecekler, ✓ vücut ısısı, ✓ uyku yorgunluğu, ✓ yük ağırlığı, ✓ zombi yakalaması ve şok, ✓ yemek bozulması ve buzdolabı, ✓ çiğ yemek zehirlenmesi, ✓ ıslaklık, ✓ yağmur suyu toplama; ✓ soğuk algınlığı ve öksürük, ✓ kırık kol, ✓ kapı/cam/düşüş sesleri, ✓ kan efektleri ve kalp atışı; kalan: yemek pişirme tarifleri. | Tam hayatta kalma. |
| **8 – Sürüler + olaylar** | Kıyamet gün sayacı, Kan Ayı, gezgin sürüler, erzak düşürme, radyo. | Sürekli artan baskı. |
| **9 – Elektrik ve üretim** | Jeneratör, güneş paneli, kablolar, elektrikli çit, projektör; çalışma tezgahları, tamir. | Gelişmiş üs. |
| **10 – Yapılar (2. dalga)** | Hastane, okul, hapishane, askeri üs, apartman, küçük kasaba. | Büyük keşif hedefleri. |
| **11 – İnsanlar** | Tüccarlar, haydutlar, hayatta kalan kampları; beceri sistemi. | Canlı dünya. |
| **12 – Boss + cila** | Alfa boss, ses efektleri, el çizimi dokular, denge, performans. | Yayına hazır sürüm. |

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
