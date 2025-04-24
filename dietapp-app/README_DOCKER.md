# Diet Planner Uygulaması Docker Rehberi

Bu rehber, Diet Planner uygulamasını Docker kullanarak nasıl çalıştıracağınızı açıklamaktadır.

## Gereksinimler

- Docker
- Docker Compose

## Kurulum ve Çalıştırma

1. Bu repoyu klonlayın veya indirin
2. Proje dizininde aşağıdaki komutu çalıştırın:

```bash
docker-compose up -d
```

Bu komut:
- Docker imajını oluşturacak
- Uygulamayı bir konteyner içinde çalıştıracak
- SQLite veritabanı için ./data dizinini bağlayacak

## Veritabanı Hakkında

SQLite veritabanı dosyası (`dietplanner.db`) konteyner içinde `/app/data` dizininde oluşturulur ve host makinenizdeki `./data` dizinine bağlanır. Bu sayede konteyner silinse bile verileriniz kaybolmaz.

## Nasıl Çalışır

1. Uygulama, Docker ortamını algılar ve veritabanı bağlantısını `/app/data/dietplanner.db` olarak yapılandırır
2. Uygulama, gerekirse veri dizinini otomatik olarak oluşturur
3. Tüm veritabanı işlemleri bu Docker volume'u üzerinden gerçekleştirilir

## Konteyner Yönetimi

- Konteyner'ı durdurmak için: `docker-compose stop`
- Konteyner'ı kaldırmak için: `docker-compose down`
- Logları görüntülemek için: `docker-compose logs -f`

## Sorun Giderme

1. Veritabanı bağlantı hataları:
   - `./data` dizininin yazılabilir olduğundan emin olun
   - Konteyner loglarını kontrol edin: `docker-compose logs -f`

2. İmaj oluşturma sorunları:
   - Maven bağımlılıklarının çözümlendiğinden emin olun
   - Docker imajını yeniden oluşturun: `docker-compose build --no-cache` 