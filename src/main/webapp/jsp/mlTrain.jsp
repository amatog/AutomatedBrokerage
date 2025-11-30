<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>ML-Modelltraining</title>
</head>
<body>
<h1>ML-Modelltraining für Portfolio-Risiko</h1>

<p>Trainierte Portfolios: ${numPortfolios}</p>
<p>Horizont (Tage): ${horizonDays}</p>
<p>HTTP-Status vom ML-Service: ${statusCode}</p>

<h2>Roh-Resultat</h2>
<pre>${resultJson}</pre>

<p>
    <a href="${pageContext.request.contextPath}/ml-risk">Zur ML-Risiko-Ansicht</a>
</p>
</body>
</html>
