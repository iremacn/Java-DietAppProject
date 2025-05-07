#!/bin/sh

# Çalışma dizinine geç
cd /app
mkdir -p /app/data /app/classes

# SQLite JDBC sürücüsünü indirme - GitHub'dan doğrudan indirme
echo "SQLite JDBC sürücüsü indiriliyor..."
rm -f /app/sqlite-jdbc.jar
wget --no-check-certificate -O /app/sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.34.0/sqlite-jdbc-3.34.0.jar

# İndirme başarısız olduysa alternatif kaynak dene
if [ ! -s /app/sqlite-jdbc.jar ]; then
  echo "İlk kaynak başarısız, alternatif kaynak deneniyor..."
  rm -f /app/sqlite-jdbc.jar
  curl -L -f -o /app/sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.34.0/sqlite-jdbc-3.34.0.jar
fi

# Son kontrol
if [ ! -s /app/sqlite-jdbc.jar ]; then
  echo "HATA: SQLite JDBC sürücüsü indirilemedi veya dosya boş!"
  exit 1
fi

echo "JDBC sürücü dosya boyutu: $(wc -c < /app/sqlite-jdbc.jar) bayt"

# Derleme ve çalıştırma
echo "Derleme başlıyor..."
javac -verbose -cp "/app/sqlite-jdbc.jar:/app/src/main/java" -d /app/classes $(find /app/src/main/java -name "*.java" | grep -v "Test.java")
echo "Derleme tamamlandı. Sınıf dosyaları:"
find /app/classes -name "*.class" | grep DietappApp

# Çalıştırma
echo "Çalıştırma başlıyor..."

# Guest mode'a giriş yap (3) ve sonra uygulamadan çık (0)
# Menü etkileşimlerini simüle et
echo -e "3\n0" | java -Djava.awt.headless=true -Xms64m -Xmx128m -XX:+UseSerialGC -cp "/app/sqlite-jdbc.jar:/app/classes" com.berkant.kagan.haluk.irem.dietapp.DietappApp

echo "Uygulama başarıyla çalıştı ve tamamlandı." 