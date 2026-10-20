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
- **Yağmalama**: içinde loot olan dolaplar önce kısa bir süre "aranır" ve bu ses yakındaki zombileri çeker.

Ayarlar: `config/deadhorizon.json` (oyun ilk açıldığında oluşur).

Plan ve yol haritası: [docs/PLAN.md](docs/PLAN.md)

## Geliştirme
```
./gradlew build      # build/libs/ altında .jar üretir
./gradlew runClient  # geliştirme istemcisini açar
```
Dokular ve blok verileri `tools/gen_textures.py`, `tools/gen_icons.py` ve `tools/gen_blocks.py` ile üretilir (yer tutucu, ileride elle çizilecek).
