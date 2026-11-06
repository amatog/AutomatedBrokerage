# Lokale Sicherheitskonfiguration

`/orders` und `/ml-train` sind durch Servlet-Container-Authentifizierung geschützt. Im Tomcat-Realm müssen Benutzer mit den Rollen `trader` beziehungsweise `ml-admin` eingerichtet werden. Ohne gültige Anmeldung/Rolle bleiben die Aktionen gesperrt. Es wird weiterhin genau das Alpaca-Konto des Servers verwendet; diese Rollen sind für dessen berechtigte Betreiber gedacht, nicht für eine Anwendung mit getrennten Kundenkonten.

Die Formulare unter `/orders` und `/ml-train` liefern Sitzungstoken für POST-Anfragen. GET auf `/ml-train` zeigt nur noch das Formular. Ein Orderformular ist nur einmal verwendbar; bei Timeout zuerst den Brokerstatus prüfen, bevor eine neue Order erfasst wird. Der gesendete `client_order_id` entspricht dem Formularbezeichner.

Für Orders sind zusätzlich erforderlich:

- `ALPACA_API_KEY` und `ALPACA_API_SECRET`.
- Paper: `ALPACA_TRADING_MODE=paper`, `ALPACA_BASE_URL=https://paper-api.alpaca.markets`.
- Live: `ALPACA_TRADING_MODE=live`, `ALPACA_BASE_URL=https://api.alpaca.markets`.

Keine Standardwerte aktivieren den Handel. Der Modus wird auf dem Formular angezeigt. Für ein extern erreichbares Deployment müssen HTTPS und die Tomcat-Benutzerverwaltung eingerichtet sein; BASIC-Anmeldedaten dürfen nur über TLS übertragen werden.

Für Trainingsaufrufe denselben zufälligen `ML_ADMIN_TOKEN` im Java- und Python-Dienst konfigurieren. Er wird serverseitig als Bearer-Token übertragen. Der Trainingsaufruf ist synchron und auf fünf Minuten Lesezeit begrenzt; ein Timeout beendet keinen bereits laufenden Python-Job. Parallelaufrufe werden vom Python-Dienst abgewiesen.

Der ML-Dienst liefert derzeit einen ausdrücklich heuristischen Risikoscore. Nicht vorhandene Trendinferenz erzeugt HTTP 501 und wird im Dashboard als fehlende Trenddaten behandelt. Short-Positionen werden vom Risikodienst nicht unterstützt.

Tests: `mvn test` (7 Regressionstests; kein echter Brokeraufruf).
