FROM openjdk:8-jdk-alpine

WORKDIR /app

# Gerekli paketleri yükle
RUN apk add --no-cache curl wget findutils grep

# Veritabanı dizinini oluştur
RUN mkdir -p /app/data /app/classes

# Veritabanı dosyası için VOLUME tanımla
VOLUME ["/app/data"]

# Start.sh dosyasını kopyala
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

# Tüm proje dosyalarını kopyala
COPY . /app/

# Uygulamayı çalıştır
CMD ["/bin/sh", "/app/start.sh"] 