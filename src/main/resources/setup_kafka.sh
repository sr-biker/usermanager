openssl pkcs12 -export -inkey service.key -in service.cert -out client.keystore.p12 -name service_key
keytool -import -file ca.pem -alias CA -keystore client.truststore.jks