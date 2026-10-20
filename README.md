# Dead Horizon

Minecraft 1.20.1 (Fabric) için zombi kıyameti modu.

## Kurulum
1. [Fabric Loader](https://fabricmc.net/use/installer/) ile 1.20.1 profilini kur.
2. [Fabric API](https://modrinth.com/mod/fabric-api) (1.20.1 sürümü) dosyasını `mods` klasörüne koy.
3. Bu modun `.jar` dosyasını da `mods` klasörüne koy. En son derlenen `.jar`, GitHub'daki **Actions** sekmesinde en son çalışmanın altındaki `deadhorizon-jar` dosyasıdır.

## Şu an neler var
- **Yürüyen**: yavaş, dayanıklı, kalabalık gelir.
- **Koşucu**: hızlı ama zayıf, gece daha da hızlanır.
- **Tank**: dev, zırhlı, sert vurur, kapıları hızla kırar.
- **Özel zombiler** (Faz 4): **Çığlıkçı** seni görünce çığlık atar ve çevredeki tüm zombileri üstüne çeker; **Kusan** uzaktan zehirli asit tükürür; **Patlayan** ölünce zehirli bulut saçar; **Sürünen** yerde sürünür, 1 bloklu boşluklardan geçer ve ayağını yavaşlatır; **Asker Zombi** zırhlıdır ve ok/mermi hasarının yarısını yer.
- Hiçbiri güneşte yanmaz, gündüz de doğarlar.
- Her zorlukta kapı kırarlar.
- Ses sistemi: koşmak, blok kırmak ve saldırmak yakındaki zombileri çeker. Eğilerek hareket etmek sessizdir.

- **Enfeksiyon ve kanama** (Faz 2): zombi vuruşu enfeksiyon bulaştırabilir ve kanatabilir; tedavi edilmeyen enfeksiyon öldürür ve seni zombiye çevirir. Bandaj, ilk yardım çantası, ağrı kesici, antibiyotik, panzehir.
- **İşlevsel dekorasyon** (Faz 3): mutfak dolabı, buzdolabı, gardırop, ecza dolabı, kilitli kasa, çöp kutusu (içine atılanı yok eder), tahta sandık, çöp konteyneri; oturulabilen sandalye, tabure, koltuk; masa; açılıp kapanan masa/tavan/sokak lambası; kum torbası, dikenli tel (yavaşlatır ve keser), zombilerin kırmaya çalıştığı tahta barikat (tahtayla tamir edilir), trafik konisi, üstünden atlanamayan beton bariyer.
- **Gerçekçilik**:
  - **Susuzluk**: açlık barının üstünde su damlaları. Koşarken, çölde ve Nether'de daha hızlı azalır. Azalınca güçsüzleşir, yavaşlarsın; sıfırda can kaybedersin. Şişeyle doldurulan su kirlidir ve hasta edebilir; fırında, tütsüleyicide ya da kamp ateşinde kaynatınca **Temiz Su** olur. Süt, karpuz, çorbalar da susuzluğu giderir; ekmek ve kurutulmuş yosun susatır.
  - **Dayanıklılık**: koşmak ve vurmak yorar (nişangahın altındaki çubuk). Ağır zırh daha çok yorar. Tükenince bir süre koşamazsın ve vuruşların zayıflar.
  - **Kırık bacak**: yüksekten düşünce bacağın kırılabilir; topallarsın ve koşamazsın. **Atel** (çubuk + bandaj) ile düzelir.
  - **Zombi duyuları**: karanlıkta elinde ışık yokken seni daha zor fark ederler; kanaman varsa daha uzaktan bulurlar. Enfekte olanları biraz geç fark ederler.
  - **Kafadan vuruş**: ok ve mermiler kafaya gelirse iki kat hasar verir.
  - **Vücut ısısı**: biyom, yükseklik, gece, yağmur, suya girmek ve yakındaki ateş/fırın/lav vücut ısını değiştirir (hotbarın solundaki termometre). Deri kıyafet soğuğu keser ama sıcakta bunaltır. Çok üşürsen yavaşlarsın, donarsın; çok ısınırsan susuzluk hızlanır, sıcak çarpması can götürür.
  - **Uyku yorgunluğu**: uyumadan geçen her gün yorar. Uykusuzken gözlerin kapanır, kazman yavaşlar, dayanıklılığın geç dolar. Gece boyu yatakta uyumak tamamen dinlendirir; oturmak biraz dinlendirir. Kahve ve enerji içeceği uykunu açar.
  - **Yük ağırlığı**: çanta doldukça (özellikle bloklarla) yavaşlarsın ve koşmak daha çok yorar.
  - **Zombi yakalaması**: zombiler vurduğunda seni bir an yakalayıp durdurabilir. **Şok**: canın çok azalınca yavaşlar, güçsüzleşirsin.
  - **Yemek bozulması**: çiğ et 2, pişmiş et 4, ekmek 5 oyun günü sonra çürür (bilgi kutusunda tazelik yazar). Buzdolabı bozulmayı 4 kat yavaşlatır; konserve ve MRE hiç bozulmaz.
  - **Çiğ yemek**: çiğ et ve balık zehirleyebilir, özellikle tavuk.
  - **Islaklık**: yağmur ve su seni ıslatır, ıslakken daha çok üşürsün. Ateşin yanında ya da sıcakta kurursun.
  - **Yağmur Suyu Toplayıcı**: yağmurda dolan fıçı (tahtalar + kova). Şişeyle temiz su alınır.
  - **Soğuk algınlığı**: üşüyünce ya da sırılsıklamken hastalanabilirsin. Öksürük sesi zombileri çeker ve nefesini keser; ağrı kesici öksürüğü bastırır, gece boyu uyumak iyileştirir.
  - **Kırık kol**: Tank zombinin vuruşu kolunu kırabilir; vuruşların zayıflar. Atel ile düzelir.
  - **Sesler**: kapı, kapak ve çit kapısı açmak (eğilerek yarı ses), cam kırmak (eğilsen de) ve sert düşüş zombileri çeker.
  - **Kan**: vuruşlarda kan sıçrar (zombilerde koyu); canın azalınca ekran kenarları kızarır ve kalp atışını duyarsın.
  - Yeni eşyalar: Kahve, Temiz Su, Gazoz, Enerji İçeceği, Fasulye ve Çorba Konservesi, Enerji Barı, Askeri Tayın (MRE), Atel.
- **Silahlar**:
  - Ateşli: Tabanca (9mm, 15'lik), Pompalı (12 kalibre, 6 fişek, saçma), Dürbünlü Av Tüfeği (7.62, 5'lik, tek atışta ağır hasar), AK-47 (7.62, 30'luk, otomatik), M4 (5.56, 30'luk, otomatik).
  - Sağ tık ateş eder, otomatik modda basılı tutunca tarar. **Eğilmek nişan almaktır**: yakınlaştırır, sekmeyi ve dağılmayı azaltır. Koşarken ve zıplarken isabet düşer.
  - **R** doldurur, **B** atış modunu (otomatik/tek) değiştirir. Hotbarın sağında şarjör ve yedek mermi sayısı görünür.
  - Her atış silahı yıpratır; yıpranmış silah tutukluk yapabilir (R ile açılır). Kafaya isabet iki kat hasar. Mermiler camı kırar. Silah sesi çok uzaktan zombileri çeker.
  - Mermi: bakır + barut + demir parçacığından el yapımı üretilebilir. Silahlar üretilemez, sandıklarda, kasalarda ve asker zombilerde bulunur (yıpranmış ve az mermiyle).
  - Ayrıca MP5 (9mm, otomatik) ve Revolver (.357, 6'lık, güçlü).
  - **Keskin Nişancı Tüfeği** (7.62, 5'lik, tek atışta çoğu zombiyi öldürür, çok yavaş ve çok gürültülü, güçlü dürbün), **Uzi** (9mm, 32'lik, çok dağınık), **Çifte** (12g, iki fişek, yakında yıkıcı, hızlı dolar).
  - **Eklentiler**: Susturucu (sesi dörtte bire indirir), Uzatılmış Şarjör (+%50), Dürbün (uzun namlulularda güçlü yakınlaştırma ve isabet). Silahı sol ele al, eklentiyi sağ elle kullan.
  - **Atılabilirler**: El Bombası (2.5 sn sonra patlar), Molotof (çarptığı yeri yakar), Sesli Tuzak Bombası (öterek zombileri toplar, sonra patlar). Patlamalar varsayılan olarak blok kırmaz.
  - Yakın dövüş: Beyzbol Sopası, Çivili Sopa, Pala, Levye, Katana, Yangın Baltası. Katana, pala ve baltayla kritik vuruş (zıplayıp düşerken) zombinin kafasını keser; sopa ve levye zombiyi sersemletir.
- **Kıyamet olayları**:
  - **Gün sayacı**: her sabah ekranda "Gün N" yazar. Zombiler her gün biraz daha güçlenir (can +%3/gün, en fazla iki kat; hasar ve hız da artar).
  - **Kan Ayı**: her 7. gece gökyüzü kızarır, her 30 saniyede bir öfkeli zombi dalgası gelir.
  - **Gezgin sürüler**: 20-40 dakikada bir, birinin yakınında bir sürü belirip ona doğru yürür.
  - **Erzak düşürme**: iki günde bir öğlen telsizden koordinat gelir. Erzak sandığının yanında dumanlı işaret ateşi yanar ama zombiler de sesi duyar.
  - **Telsiz**: günü, Kan Ayına kaç gün kaldığını ve son erzağın yerini söyler.
- **Elektrik ve üs**:
  - **Jeneratör**: kömür (1 dk) ya da benzin bidonu (5 dk) yakar, 12 blok içindeki cihazlara elektrik verir. Gürültülüdür, 24 bloktan zombi çeker. Kapalı alanda çalışırsa egzoz gazı mide bulantısı ve halsizlik yapar.
  - **Güneş paneli**: sessiz ve bedava, ama sadece gündüz, açık gökyüzü altında ve yağmursuz havada.
  - **Elektrikli çit**: elektrik varken dokunan zombiyi çarpar ve geri fırlatır (oyuncuya yarım hasar). Üstünden atlanamaz.
  - **Projektör**: elektrik varken en güçlü ışığı verir.
  - **Buzdolabı** artık elektrik ister; elektriksiz buzdolabında yemek normal hızda bozulur.
  - **Silah tezgahı**: elinde aşınmış silah ya da yakın dövüş silahıyla kullan, bir demir külçe dayanıklılığın dörtte birini geri verir ve tutukluğu açar.
- **İnsanlar** (STALKER gibi):
  - **Haydutlar**: tabanca, çifte, AK, Uzi ya da palayla gezer. Oyuncuya ve stalkerlara ateş eder, siper mesafesini korur, seri atar, şarjör değiştirir, canı azalınca kaçar. Ölünce yıpranmış silahını, ruble ve mermi düşürebilir.
  - **Stalkerlar** (hayatta kalanlar): zombileri ve haydutları avlar, sana ancak saldırırsan döner.
  - **Tüccar**: sığınağında oturur. Ruble ile mermi, ilaç, anti-radyasyon, silah, dedektör satar; artefakt, silah, altın alır.
  - Zombiler insanlara da saldırır.
- **Bölge** (STALKER):
  - **Radyasyon**: radyoaktif varil ve moloz yakınında doz birikir. 30'da halsizlik, 60'ta hasar, 85'te ölüm. Anti-radyasyon iğnesi yarısını, votka biraz alır. Koruyucu tulumun her parçası dozu azaltır.
  - **Geiger sayacı**: üstündeyken radyasyonda tıklar; kullanınca dozu ve doz hızını gösterir.
  - **Anomaliler** (görünmez): Yakıcı tutuşturur, Elektro çarpar, Sıçratıcı havaya fırlatır. Sadece hafif bir titreşim belli eder. **Cıvata** at, anomali sende değil cıvatada patlasın. **Dedektör** elde öter ve anomalilerin sınırını gösterir.
  - **Artefaktlar** anomalilerin yanında zamanla oluşur: Denizanası (iyileştirir, kanamayı durdurur), Taş Çiçek (dayanıklılık), Ateş Topu (ısıtır, ateşe dayanıklılık). Kemerde (hızlı erişim çubuğu ya da sol el) çalışırlar ama radyoaktiftirler.
  - Yapılar: **haydut kampı**, **tüccar sığınağı** (korumalı), **radyoaktif alan** (kaza yapmış kamyon), **anomali alanı** (son stalkerın zulası).
- **Yakın dövüş** (Dying Light / Zomboid):
  - **Tekme** (V): zombiyi iter ve sersemletir. Eğilerek basarsan yerdeki zombinin kafasını ezer.
  - **Kaçınma** (sol Alt + yön): hızlı yana ya da geri sıçrama.
  - **Tırmanma**: 2 blok yüksek duvara zıplayıp boşluğa basılı tutunca üstüne tırmanırsın.
  - Hepsi dayanıklılık harcar.
- **Arayüz** (STALKER): üstte pusula, sağ altta can/dayanıklılık/radyasyon çubukları ve renkli durum rozetleri (sarı, turuncu, kırmızı), vurulunca nişangahın etrafında hasarın geldiği yönü gösteren kırmızı işaret. **PDA** (P): gün ve saat, Kan Ayına kalan gün, tüm vücut değerleri ve aktif durumlar.
- **Silah animasyonları**: eğilince silah ekranın ortasına, göz hizasına gelir (nişan). Koşarken namlu aşağı iner. Her atışta silah geri teper. Şarjör değiştirirken silah yana yatar, şarjör çıkar, yenisi takılır ve kurma kolu çekilir. Tutukluk olunca silah yan tutulur. Dürbünlü silahla nişan alınca dürbün görüntüsü açılır. Üçüncü şahısta silah iki elle omuza alınır. Her atışta silahın inip kalkması kaldırıldı.
- **Terk edilmiş binalar**: düzlük, orman, savan, tayga, kar ve çölde dağınık kasaba binaları çıkar. Hepsinin içinde zombi doğar, camların bir kısmı kırık, duvarlar yosunlu ve çatlak, lambalar sönük.
  - **Ev**: mutfak, buzdolabı, yatak odası, gardırop, ecza dolabı.
  - **Market**: raf koridorları, buzdolabı sırası, kasa. Konserve, içecek, sebze.
  - **Eczane**: duvar boyu ecza dolapları ve tezgah.
  - **Benzin istasyonu**: pompalar, sundurma, dükkan. Benzin bidonu, atıştırmalık, kömür.
  - **Polis karakolu**: nezarethane, kasa ve sandıklarda tabanca, pompalı, MP5, mermi, telsiz.
- **Yemek pişirme**: tencereyi yanan kamp ateşi, ateş, lav, magma ya da yanan fırın üstüne koy, kova ya da şişeyle su doldur.
  - Boş kaynatılan su temiz suya döner (3 şişe). Kirli suyu içmeden önce kaynat.
  - En fazla 4 malzeme: et ve sebze **Etli Güveç** (en doyurucu, su, dayanıklılık ve sıcaklık verir), iki sebze **Sebze Çorbası**, diğer her şey **Lapa** olur. Çürük et her şeyi lapaya çevirir.
  - Çorbalar kaseyle alınır, sıcak oldukları için vücudu ısıtır ve birkaç günde bozulur. Pişen yemeğin kokusu yakındaki zombileri çeker.
- **Yağmalama**: içinde loot olan dolaplar önce kısa bir süre "aranır" ve bu ses yakındaki zombileri çeker.

Ayarlar: `config/deadhorizon.json` (oyun ilk açıldığında oluşur).

Plan ve yol haritası: [docs/PLAN.md](docs/PLAN.md)

## Geliştirme
```
./gradlew build      # build/libs/ altında .jar üretir
./gradlew runClient  # geliştirme istemcisini açar
```
Dokular ve blok verileri `tools/gen_textures.py`, `tools/gen_icons.py` ve `tools/gen_blocks.py` ile üretilir (yer tutucu, ileride elle çizilecek).
