<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>Order Ergebnis - myBrokerApp</title>
    <style>
        body { font-family: 'Segoe UI', Roboto, sans-serif; background: #0d1117; color: #e6edf3; margin: 0; padding: 20px; }
        a { color: #58a6ff; text-decoration: none; font-weight: 500; }
        a:hover { text-decoration: underline; }
        .container { max-width: 800px; margin: 0 auto; }
        .nav-top { margin-bottom: 16px; }
        .nav-top a { margin-right: 12px; }
        .title { font-size: 2em; color: #58a6ff; margin-bottom: 10px; }
        .subtitle { color: #8b949e; margin-bottom: 20px; }
        .card { background: rgba(255,255,255,0.06); backdrop-filter: blur(6px); border: 1px solid rgba(255,255,255,0.12); border-radius: 12px; padding: 18px 20px; box-shadow: 0 0 15px rgba(0,255,255,0.07); transition: transform 0.2s ease, box-shadow 0.2s ease; margin-bottom: 20px; }
        .card:hover { transform: translateY(-3px); box-shadow: 0 0 18px rgba(88,166,255,0.45); }
        table { width: 100%; border-collapse: collapse; margin-top: 8px; }
        th, td { padding: 8px 10px; text-align: left; font-size: 0.9em; }
        th { background: #161b22; color: #79c0ff; }
        tr:nth-child(even) { background: #161b22; }
        tr:nth-child(odd) { background: #0f141a; }
    </style>
</head>
<body>
<div class="container">
    <div class="nav-top">
        <a href="${pageContext.request.contextPath}/jsp/index.jsp">Home</a>
        <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
        <a href="${pageContext.request.contextPath}/status">Account Status</a>
        <a href="${pageContext.request.contextPath}/positions">Positionen</a>
    </div>

    <div class="title">Order Ergebnis</div>
    <div class="subtitle">Zusammenfassung der gesendeten Order</div>

    <div class="card">
        <h2>Order Details</h2>
        <table>
            <tr><th>Order-ID</th><td>${order.id}</td></tr>
            <tr><th>Status</th><td>${order.status}</td></tr>
            <tr><th>Symbol</th><td>${order.symbol}</td></tr>
            <tr><th>Angeforderte Menge</th><td>${order.requestedQty}</td></tr>
            <tr><th>Gefüllte Menge</th><td>${order.filledQty}</td></tr>
            <tr><th>Order-Typ</th><td>${order.type}</td></tr>
            <tr><th>Time-in-Force</th><td>${order.tif}</td></tr>
            <tr><th>Erstellt</th><td>${order.createdAt}</td></tr>
        </table>
    </div>

    <div class="card">
        <h2>Rohes JSON (Debug)</h2>
        <pre style="white-space: pre-wrap; font-size: 0.85em; color:#c9d1d9;">${rawJson}</pre>
    </div>

    <div>
        <a href="${pageContext.request.contextPath}/orders">Weitere Order erstellen</a><br>
        <a href="${pageContext.request.contextPath}/positions">Positionen anzeigen</a><br>
        <a href="${pageContext.request.contextPath}/dashboard">Zum Dashboard</a>
    </div>
</div>
</body>
</html>
