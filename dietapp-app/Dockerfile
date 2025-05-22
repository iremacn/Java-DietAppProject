FROM openjdk:8-jdk-alpine

WORKDIR /app

# Tüm proje dosyalarını kopyala
COPY . /app/

# Gerekli paketleri yükle
RUN apk add --no-cache curl wget findutils grep

# Veritabanı dizinini oluştur
RUN mkdir -p /app/data /app/classes

# Start.sh dosyasını kopyala ve çalıştırılabilir yap
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh && sed -i 's/\r$//' /app/start.sh

# Veritabanı dosyası için VOLUME tanımla
VOLUME ["/app/data"]

# Uygulamayı çalıştır
CMD ["/bin/sh", "/app/start.sh"] 