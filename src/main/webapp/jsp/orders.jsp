<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>Neue Order - myBrokerApp</title>
    <style>
        body { font-family: 'Segoe UI', Roboto, sans-serif; background: #0d1117; color: #e6edf3; margin: 0; padding: 20px; }
        a { color: #58a6ff; text-decoration: none; font-weight: 500; }
        a:hover { text-decoration: underline; }
        .container { max-width: 700px; margin: 0 auto; }
        .nav-top { margin-bottom: 16px; }
        .nav-top a { margin-right: 12px; }
        .title { font-size: 2em; color: #58a6ff; margin-bottom: 10px; }
        .subtitle { color: #8b949e; margin-bottom: 20px; }
        .card { background: rgba(255,255,255,0.06); backdrop-filter: blur(6px); border: 1px solid rgba(255,255,255,0.12); border-radius: 12px; padding: 18px 20px; box-shadow: 0 0 15px rgba(0,255,255,0.07); transition: transform 0.2s ease, box-shadow 0.2s ease; margin-bottom: 20px; }
        .card:hover { transform: translateY(-3px); box-shadow: 0 0 18px rgba(88,166,255,0.45); }
        label { display:block; margin-top: 10px; margin-bottom: 4px; }
        input[type=text], input[type=number] { width: 100%; padding: 8px 10px; border-radius: 8px; border: 1px solid #30363d; background:#0d1117; color:#e6edf3; }
        .radio-group { margin-top: 10px; }
        .btn { margin-top: 14px; padding: 8px 14px; border-radius: 8px; border: 1px solid #58a6ff; background:#58a6ff; color:#0d1117; font-weight: 500; cursor:pointer; }
        .btn:hover { background:#1f6feb; border-color:#1f6feb; }
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

    <div class="title">Neue Order</div>
    <div class="subtitle">Erfasse eine neue Market-Order im Alpaca Paper-Trading Konto.</div>

    <div class="card">
        <form method="post" action="${pageContext.request.contextPath}/orders">
            <label for="symbol">Symbol</label>
            <input type="text" id="symbol" name="symbol" value="AAPL" required>

            <label for="qty">Menge</label>
            <input type="number" id="qty" name="qty" value="1" min="1" required>

            <div class="radio-group">
                Side:
                <label><input type="radio" name="side" value="buy" checked> Buy</label>
                <label><input type="radio" name="side" value="sell"> Sell</label>
            </div>

            <button type="submit" class="btn">Order senden</button>
        </form>
    </div>
</div>
</body>
</html>
