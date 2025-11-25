<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>myBrokerApp Dashboard</title>
    <style>
        body { font-family: 'Segoe UI', Roboto, sans-serif; background: #0d1117; color: #e6edf3; margin: 0; padding: 20px; }
        h1, h2 { color: #58a6ff; }
        a { color: #58a6ff; text-decoration: none; font-weight: 500; }
        a:hover { text-decoration: underline; }
        .header { display:flex; align-items:center; justify-content:space-between; margin-bottom:20px; }
        .header-title { font-size: 1.8em; }
        .header-links a { margin-left: 12px; }
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 24px; }
        .card { background: rgba(255,255,255,0.06); backdrop-filter: blur(6px); border: 1px solid rgba(255,255,255,0.12); border-radius: 12px; padding: 18px 20px; box-shadow: 0 0 15px rgba(0,255,255,0.07); transition: transform 0.2s ease, box-shadow 0.2s ease; }
        .card:hover { transform: translateY(-3px); box-shadow: 0 0 18px rgba(88,166,255,0.45); }
        table { width: 100%; border-collapse: collapse; margin-top: 8px; }
        th, td { padding: 8px 10px; text-align: left; font-size: 0.9em; }
        th { background: #161b22; color: #79c0ff; }
        tr:nth-child(even) { background: #161b22; }
        tr:nth-child(odd) { background: #0f141a; }
        .tag { padding: 4px 8px; border-radius: 6px; font-size: 0.8em; text-transform: uppercase; font-weight: 600; }
        .tag-buy { background:#238636; color:#ffffff; }
        .tag-sell { background:#da3633; color:#ffffff; }
        .chat-card { display:flex; flex-direction:column; max-height: 480px; }
        #chat-window { flex: 1; overflow-y: auto; border-radius: 8px; padding: 8px; background: rgba(15, 23, 42, 0.8); margin-bottom: 10px; font-size: 0.9em; }
        .chat-message { padding: 6px 10px; border-radius: 8px; margin-bottom: 6px; white-space: pre-wrap; }
        .chat-message.user { background: #1d4ed8; align-self: flex-end; }
        .chat-message.assistant { background: #111827; align-self: flex-start; }
        #chat-form { display:flex; gap:8px; }
        #chat-input { flex: 1; padding: 8px; border-radius: 8px; border: none; outline: none; background: #161b22; color: #e6edf3; }
        #chat-form button { padding: 8px 14px; border-radius: 8px; border: none; cursor: pointer; background: #238636; color: #ffffff; font-weight: 600; }
        #chat-form button:hover { background: #2ea043; }
    </style>
</head>
<body>
<div class="header">
    <div class="header-title">myBrokerApp Dashboard</div>
    <div class="header-links">
        <a href="${pageContext.request.contextPath}/">Home</a>
        <a href="${pageContext.request.contextPath}/orders">Neue Order</a>
        <a href="${pageContext.request.contextPath}/positions">Positionen</a>
    </div>
</div>

<div class="grid">
    <div class="card">
        <h2>Konto</h2>
        <p><strong>Cash:</strong> ${cash}</p>
        <p><strong>Portfolio-Wert:</strong> ${portfolioValue}</p>
    </div>

    <div class="card">
        <h2>Marktindikatoren</h2>
        <p><strong>NASDAQ (QQQ):</strong> ${markets.nasdaqPrice} <small>(Letzter Trade: ${markets.nasdaqTime})</small></p>
        <p><strong>Dow Jones (DIA):</strong> ${markets.dowPrice} <small>(Letzter Trade: ${markets.dowTime})</small></p>
    </div>

    <div class="card">
        <h2>Offene Orders</h2>
        <c:choose>
            <c:when test="${empty openOrders}">
                <p>Keine offenen Orders.</p>
            </c:when>
            <c:otherwise>
                <table>
                    <tr><th>Symbol</th><th>Side</th><th>Menge</th><th>Status</th><th>Erstellt</th></tr>
                    <c:forEach var="order" items="${openOrders}">
                        <tr>
                            <td>${order.symbol}</td>
                            <td><span class="tag ${order.side eq 'buy' ? 'tag-buy' : 'tag-sell'}">${order.side}</span></td>
                            <td>${order.qty}</td>
                            <td>${order.status}</td>
                            <td><small>${order.createdAt}</small></td>
                        </tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="card">
        <h2>Letzte Transaktionen (Fills)</h2>
        <c:choose>
            <c:when test="${empty fills}">
                <p>Keine Transaktionen gefunden.</p>
            </c:when>
            <c:otherwise>
                <table>
                    <tr><th>Symbol</th><th>Menge</th><th>Preis</th><th>Side</th><th>Ausgeführt</th></tr>
                    <c:forEach var="fill" items="${fills}">
                        <tr>
                            <td>${fill.symbol}</td>
                            <td>${fill.qty}</td>
                            <td>${fill.price}</td>
                            <td><span class="tag ${fill.side eq 'buy' ? 'tag-buy' : 'tag-sell'}">${fill.side}</span></td>
                            <td><small>${fill.timestamp}</small></td>
                        </tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="card chat-card">
        <h2>Smart Trading Assistant</h2>
        <div id="chat-window"></div>
        <form id="chat-form" action="ai-chat" method="post">
            <input id="chat-input" name="message" placeholder="Frage die KI ..." required />
            <button type="submit">Senden</button>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/ai-chat.js"></script>
</body>
</html>
