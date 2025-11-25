<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>Portfolio-Analyse mit KI</title>
    <style>
        body { font-family: Arial, sans-serif; background: #0f172a; color: #e5e7eb; margin: 0; padding: 0; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
        h1 { color: #f97316; }
        .card { background: #020617; border-radius: 16px; padding: 20px; margin-bottom: 20px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); grid-gap: 20px; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { padding: 8px 6px; text-align: left; font-size: 14px; }
        th { border-bottom: 1px solid #1f2937; color: #9ca3af; }
        tr:nth-child(even) { background-color: #020617; }
        tr:nth-child(odd) { background-color: #020617; }
        pre { white-space: pre-wrap; background: #020617; padding: 12px; border-radius: 12px; font-size: 14px; }
        a { color: #38bdf8; text-decoration: none; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>
<div class="container">
    <div class="nav-top" style="margin-bottom: 12px;">
        <a href="${pageContext.request.contextPath}/jsp/index.jsp">Home</a> |
        <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
    </div>

    <h1>Portfolio-Analyse mit KI</h1>
    <p>Die KI erklärt dir dein aktuelles Portfolio, ohne konkrete Anlageempfehlungen zu geben.</p>

    <div class="grid">
        <div class="card">
            <h2>Gesamtübersicht</h2>
            <p><strong>Gesamtwert:</strong> ${analysis.totalMarketValue}</p>
            <p><strong>Tech-Gewichtung:</strong> ${analysis.techWeight} %</p>
            <p><strong>Größte Position:</strong> ${analysis.topPositionSymbol} (${analysis.topPositionWeight} %)</p>
        </div>
        <div class="card">
            <h2>Risiko & Diversifikation</h2>
            <p><strong>Risiko-Einschätzung:</strong><br/>${analysis.riskComment}</p>
            <p><strong>Volatilität:</strong><br/>${analysis.volatilityComment}</p>
            <p><strong>Diversifikation:</strong><br/>${analysis.diversificationComment}</p>
        </div>
    </div>

    <div class="card">
        <h2>Sektorverteilung</h2>
        <table>
            <thead><tr><th>Sektor</th><th>Gewichtung</th></tr></thead>
            <tbody>
            <c:forEach var="entry" items="${analysis.sectorWeights}">
                <tr>
                    <td>${entry.key}</td>
                    <td>${entry.value} %</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <div class="card">
        <h2>Einzelpositionen</h2>
        <table>
            <thead><tr><th>Symbol</th><th>Name</th><th>Sektor</th><th>Marktwert</th><th>Offener P/L</th><th>Volatilität</th></tr></thead>
            <tbody>
            <c:forEach var="p" items="${positions}">
                <tr>
                    <td>${p.symbol}</td>
                    <td>${p.name}</td>
                    <td>${p.sector}</td>
                    <td>${p.marketValue}</td>
                    <td>${p.unrealizedPnl}</td>
                    <td>${p.volatility}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <div class="card">
        <h2>KI-Erklärung (keine Anlageberatung)</h2>
        <p>${aiContent}</p>
        <hr style="border: 0; border-top: 1px solid #1f2937; margin: 12px 0;"/>
        <p style="font-size: 0.85em; color: #8b949e;">
            <strong>Modell:</strong> ${aiModel}<br/>
            <strong>Tokens gesamt:</strong> ${aiTotalTokens}<br/>
            <strong>Tokens Antwort:</strong> ${aiCompletionTokens}<br/>
            <strong>Service-Tier:</strong> ${aiServiceTier}
        </p>
    </div>

    <p><a href="${pageContext.request.contextPath}/dashboard">Zurück zum Dashboard</a></p>
</div>
</body>
</html>
