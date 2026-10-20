# Dead Horizon

Minecraft 1.20.1 (Fabric) için zombi kıyameti modu.

## Kurulum
1. [Fabric Loader](https://fabricmc.net/use/installer/) ile 1.20.1 profilini kur.
2. [Fabric API](https://modrinth.com/mod/fabric-api) (1.20.1 sürümü) dosyasını `mods` klasörüne koy.
3. Bu modun `.jar` dosyasını da `mods` klasörüne koy. En son derlenen `.jar`, GitHub'daki **Actions** sekmesinde en son çalışmanın altındaki `deadhorizon-jar` dosyasıdır.

## Şu an neler var (Faz 1)
- **Yürüyen**: yavaş, dayanıklı, kalabalık gelir.
- **Koşucu**: hızlı ama zayıf, gece daha da hızlanır.
- **Tank**: dev, zırhlı, sert vurur, kapıları hızla kırar.
- Hiçbiri güneşte yanmaz, gündüz de doğarlar.
- Her zorlukta kapı kırarlar.
- Ses sistemi: koşmak, blok kırmak ve saldırmak yakındaki zombileri çeker. Eğilerek hareket etmek sessizdir.

Ayarlar: `config/deadhorizon.json` (oyun ilk açıldığında oluşur).

Plan ve yol haritası: [docs/PLAN.md](docs/PLAN.md)

## Geliştirme
```
./gradlew build      # build/libs/ altında .jar üretir
./gradlew runClient  # geliştirme istemcisini açar
```
Dokular `tools/gen_textures.py` ile üretilir (yer tutucu, ileride elle çizilecek).
