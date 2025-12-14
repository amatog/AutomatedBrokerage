<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String initialSymbol = (String) request.getAttribute("initialSymbol");
    if (initialSymbol == null) {
        initialSymbol = "";
    }
%>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>Value-Analyse</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/value-analysis.css">
</head>
<body>
<div class="nav-top">
    <a href="<%= contextPath %>/dashboard">Dashboard</a>
    <a href="<%= contextPath %>/status">Account Status</a>
    <a href="<%= contextPath %>/positions">Positionen</a>
    <a href="<%= contextPath %>/orders">Neue Order</a>
    <a href="<%= contextPath %>/value-analysis">Value-Analyse</a>
</div>

<div class="value-analysis-page">

    <header class="va-header">
        <h1>Value-Analyse</h1>
        <p>Fundamentalanalyse basierend auf Graham, Buffett, Greenblatt und weiteren Investoren sowie einem kombinierten
            Value-Score.</p>
    </header>

    <section class="va-search">
        <form id="value-analysis-form">
            <label for="symbol-input">Ticker-Symbol:</label>
            <input type="text" id="symbol-input" name="symbol"
                   placeholder="z.B. AAPL, MSFT, NESN.SW"
                   value="<%= initialSymbol %>">
            <button type="submit">Analysieren</button>
        </form>
        <div id="va-error" class="va-error hidden"></div>
    </section>

    <section id="va-loader" class="va-loader hidden">
        Bitte warten, Daten werden geladen ...
    </section>

    <section id="va-content" class="va-content hidden">
        <div class="va-grid">

            <div class="va-card" id="va-fundamentals-card">
                <h2>Fundamentaldaten</h2>
                <div id="va-fundamentals-body"></div>
            </div>

            <div class="va-card" id="va-graham-card">
                <h2>Graham</h2>
                <div id="va-graham-body"></div>
            </div>

            <div class="va-card" id="va-buffett-card">
                <h2>Buffett</h2>
                <div id="va-buffett-body"></div>
            </div>

            <div class="va-card" id="va-greenblatt-card">
                <h2>Greenblatt</h2>
                <div id="va-greenblatt-body"></div>
            </div>

            <div class="va-card" id="va-munger-card">
                <h2>Munger</h2>
                <div id="va-munger-body"></div>
            </div>

            <div class="va-card" id="va-lynch-card">
                <h2>Lynch</h2>
                <div id="va-lynch-body"></div>
            </div>

            <div class="va-card" id="va-schloss-card">
                <h2>Schloss</h2>
                <div id="va-schloss-body"></div>
            </div>

            <div class="va-card" id="va-davis-card">
                <h2>Davis</h2>
                <div id="va-davis-body"></div>
            </div>

            <div class="va-card" id="va-templeton-card">
                <h2>Templeton</h2>
                <div id="va-templeton-body"></div>
            </div>

            <div class="va-card" id="va-klarman-card">
                <h2>Klarman</h2>
                <div id="va-klarman-body"></div>
            </div>

            <div class="va-card va-card-score" id="va-score-card">
                <h2>Value-Score</h2>
                <div id="va-score-body"></div>
            </div>

        </div>
    </section>

</div>

<script>
    const VA_CONTEXT_PATH = '<%= contextPath %>';
    const VA_API_URL = VA_CONTEXT_PATH + '/value-analysis';
    const VA_INITIAL_SYMBOL = '<%= initialSymbol %>';
</script>
<script src="<%= contextPath %>/js/value-analysis.js"></script>
</body>
</html>
