document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("value-analysis-form");
    const symbolInput = document.getElementById("symbol-input");
    const errorBox = document.getElementById("va-error");
    const loader = document.getElementById("va-loader");
    const content = document.getElementById("va-content");

    const fundamentalsBody = document.getElementById("va-fundamentals-body");
    const grahamBody = document.getElementById("va-graham-body");
    const buffettBody = document.getElementById("va-buffett-body");
    const greenblattBody = document.getElementById("va-greenblatt-body");
    const mungerBody = document.getElementById("va-munger-body");
    const lynchBody = document.getElementById("va-lynch-body");
    const schlossBody = document.getElementById("va-schloss-body");
    const davisBody = document.getElementById("va-davis-body");
    const templetonBody = document.getElementById("va-templeton-body");
    const klarmanBody = document.getElementById("va-klarman-body");
    const scoreBody = document.getElementById("va-score-body");

    function setError(msg) {
        if (!msg) {
            errorBox.textContent = "";
            errorBox.classList.add("hidden");
        } else {
            errorBox.textContent = msg;
            errorBox.classList.remove("hidden");
        }
    }

    function setLoading(isLoading) {
        if (isLoading) {
            loader.classList.remove("hidden");
            content.classList.add("hidden");
        } else {
            loader.classList.add("hidden");
        }
    }

    function setContentVisible(visible) {
        if (visible) {
            content.classList.remove("hidden");
        } else {
            content.classList.add("hidden");
        }
    }

    async function fetchValueAnalysis(symbol) {
        setError(null);
        setLoading(true);
        setContentVisible(false);

        try {
            const url = `${VA_API_URL}?symbol=${encodeURIComponent(symbol)}&format=json`;
            const response = await fetch(url, {
                headers: {
                    "Accept": "application/json"
                }
            });

            const data = await response.json();

            if (!response.ok) {
                const msg = data && data.error ? data.error : `HTTP ${response.status}`;
                throw new Error(msg);
            }

            renderValueAnalysis(data);
            setLoading(false);
            setContentVisible(true);

        } catch (e) {
            console.error("Value-Analysis error", e);
            setLoading(false);
            setContentVisible(false);
            setError("Fehler beim Laden der Value-Analyse: " + e.message);
        }
    }

    function renderValueAnalysis(data) {
        const fundamentals = data.fundamentals || {};
        const graham = data.graham || {};
        const buffett = data.buffett || {};
        const greenblatt = data.greenblatt || {};
        const munger = data.munger || {};
        const lynch = data.lynch || {};
        const schloss = data.schloss || {};
        const davis = data.davis || {};
        const templeton = data.templeton || {};
        const klarman = data.klarman || {};
        const score = data.value || {};

        const symbol = data.symbol || fundamentals.symbol || "";

        // Fundamentals
        fundamentalsBody.innerHTML = `
            <ul class="va-list">
                <li><span>Symbol:</span><strong>${escapeHtml(symbol)}</strong></li>
                <li><span>Preis:</span><strong>${formatNumber(fundamentals.price)}</strong></li>
                <li><span>EPS:</span><strong>${formatNumber(fundamentals.eps)}</strong></li>
                <li><span>Buchwert je Aktie:</span><strong>${formatNumber(fundamentals.book_value)}</strong></li>
                <li><span>ROE:</span><strong>${formatPercent(fundamentals.roe)}</strong></li>
                <li><span>Dividendenrendite:</span><strong>${formatPercent(fundamentals.dividend_yield)}</strong></li>
                <li><span>Debt/Equity:</span><strong>${formatNumber(fundamentals.debt_to_equity)}</strong></li>
                <li><span>EPS-Wachstum (5Y):</span><strong>${formatPercent(fundamentals.earnings_growth_5y)}</strong></li>
                <li><span>Marktkapitalisierung:</span><strong>${formatBillions(fundamentals.market_cap)}</strong></li>
                <li><span>Sektor:</span><strong>${escapeHtml(fundamentals.sector || "")}</strong></li>
                <li><span>Industrie:</span><strong>${escapeHtml(fundamentals.industry || "")}</strong></li>
                <li><span>Quelle:</span><strong>${escapeHtml(fundamentals.source || "")}</strong></li>
            </ul>
        `;

        // Graham
        grahamBody.innerHTML = `
            <ul class="va-list">
                <li><span>Intrinsic Value:</span><strong>${formatNumber(graham.intrinsic_value)}</strong></li>
                <li><span>Margin of Safety:</span><strong>${formatPercent(graham.margin_of_safety)}</strong></li>
                <li><span>Unterbewertet:</span><strong>${formatBoolean(graham.is_undervalued)}</strong></li>
            </ul>
        `;

        // Buffett
        buffettBody.innerHTML = `
            <ul class="va-list">
                <li><span>ROE:</span><strong>${formatPercent(buffett.roe)}</strong></li>
                <li><span>Debt/Equity:</span><strong>${formatNumber(buffett.debt_to_equity)}</strong></li>
                <li><span>EPS-Wachstum (5Y):</span><strong>${formatPercent(buffett.earnings_growth_5y)}</strong></li>
                <li><span>Quality Score:</span><strong>${formatPercent(buffett.quality_score)}</strong></li>
                <li><span>Hohe Qualität:</span><strong>${formatBoolean(buffett.is_high_quality)}</strong></li>
            </ul>
        `;

        // Greenblatt
        greenblattBody.innerHTML = `
            <ul class="va-list">
                <li><span>Earnings Yield:</span><strong>${formatPercent(greenblatt.earnings_yield)}</strong></li>
                <li><span>Return on Capital:</span><strong>${formatPercent(greenblatt.return_on_capital)}</strong></li>
                <li><span>Magic Score:</span><strong>${formatPercent(greenblatt.magic_score)}</strong></li>
            </ul>
        `;

        // Munger
        mungerBody.innerHTML = `
            <ul class="va-list">
                <li><span>ROE:</span><strong>${formatPercent(munger.roe)}</strong></li>
                <li><span>Debt/Equity:</span><strong>${formatNumber(munger.debt_to_equity)}</strong></li>
                <li><span>EPS-Wachstum (5Y):</span><strong>${formatPercent(munger.earnings_growth_5y)}</strong></li>
                <li><span>Quality Score:</span><strong>${formatPercent(munger.quality_score)}</strong></li>
                <li><span>Hohe Qualität:</span><strong>${formatBoolean(munger.is_high_quality)}</strong></li>
            </ul>
        `;

        // Lynch – Growth at a Reasonable Price
        lynchBody.innerHTML = `
            <ul class="va-list">
                <li><span>EPS:</span><strong>${formatNumber(lynch.eps)}</strong></li>
                <li><span>Wachstums-Geschwindigkeit:</span><strong>${lynch.growth_pct != null ? formatPercent(lynch.growth_pct) : "-"}</strong></li>
                <li><span>PE:</span><strong>${formatNumber(lynch.pe)}</strong></li>
                <li><span>PEG:</span><strong>${lynch.peg != null ? formatNumber(lynch.peg) : "-"}</strong></li>
                <li><span>Score:</span><strong>${formatPercent(lynch.score)}</strong></li>
            </ul>
        `;

        // Schloss – Deep Value (Price/Book-Fokus)
        schlossBody.innerHTML = `
            <ul class="va-list">
                <li><span>Preis:</span><strong>${formatNumber(schloss.price)}</strong></li>
                <li><span>Buchwert je Aktie:</span><strong>${formatNumber(schloss.book_value)}</strong></li>
                <li><span>Price/Book:</span><strong>${formatNumber(schloss.price_to_book)}</strong></li>
                <li><span>Debt/Equity:</span><strong>${formatNumber(schloss.debt_to_equity)}</strong></li>
                <li><span>Score:</span><strong>${formatPercent(schloss.score)}</strong></li>
            </ul>
        `;

        // Davis – ähnlich Deep Value / defensiver Fokus
        davisBody.innerHTML = `
            <ul class="va-list">
                <li><span>Preis:</span><strong>${formatNumber(davis.price)}</strong></li>
                <li><span>Buchwert je Aktie:</span><strong>${formatNumber(davis.book_value)}</strong></li>
                <li><span>Price/Book:</span><strong>${formatNumber(davis.price_to_book)}</strong></li>
                <li><span>Debt/Equity:</span><strong>${formatNumber(davis.debt_to_equity)}</strong></li>
                <li><span>Score:</span><strong>${formatPercent(davis.score)}</strong></li>
            </ul>
        `;

        // Templeton – Contrarian Value
        templetonBody.innerHTML = `
            <ul class="va-list">
                <li><span>Preis:</span><strong>${formatNumber(templeton.price)}</strong></li>
                <li><span>EPS:</span><strong>${formatNumber(templeton.eps)}</strong></li>
                <li><span>PE:</span><strong>${formatNumber(templeton.pe)}</strong></li>
                <li><span>Price/Book:</span><strong>${formatNumber(templeton.price_to_book)}</strong></li>
                <li><span>Rel. PE vs Markt (15x):</span><strong>${formatNumber(templeton.relative_pe_vs_market15)}</strong></li>
                <li><span>Score:</span><strong>${formatPercent(templeton.score)}</strong></li>
            </ul>
        `;

        // Klarman – Margin of Safety / Discount zum Inneren Wert
        klarmanBody.innerHTML = `
            <ul class="va-list">
                <li><span>Intrinsic Value:</span><strong>${formatNumber(klarman.intrinsic_value)}</strong></li>
                <li><span>Preis:</span><strong>${formatNumber(klarman.price)}</strong></li>
                <li><span>Margin of Safety:</span><strong>${formatPercent(klarman.margin_of_safety)}</strong></li>
                <li><span>Debt/Equity:</span><strong>${formatNumber(klarman.debt_to_equity)}</strong></li>
                <li><span>Score:</span><strong>${formatPercent(klarman.score)}</strong></li>
            </ul>
        `;

        // Value-Score gesamt (NEU – korrekt gemappt)
        const valueScore = typeof score.score === "number" ? score.score : null;
        const valueLevel = score.rating || "";

        scoreBody.innerHTML = `
            <div class="va-score-main">
                <div class="va-score-value">
                    ${valueScore !== null ? (valueScore * 100).toFixed(1) + " %" : "-"}
                </div>
                <div class="va-score-level">${escapeHtml(valueLevel)}</div>
            </div>
            <p class="va-score-note">
                Der Value-Score kombiniert Margin-of-Safety, Qualitätskennzahlen und Ertragskraft zu einem Gesamtbild.
            </p>
        `;
    }

    function formatNumber(v) {
        if (v === null || v === undefined || isNaN(v)) return "-";
        return Number(v).toFixed(2);
    }

    function formatPercent(v) {
        if (v === null || v === undefined || isNaN(v)) return "-";
        return (Number(v) * 100).toFixed(1) + " %";
    }

    function formatBillions(v) {
        if (v === null || v === undefined || isNaN(v)) return "-";
        const num = Number(v);
        if (Math.abs(num) >= 1e9) {
            return (num / 1e9).toFixed(1) + " Mrd.";
        }
        if (Math.abs(num) >= 1e6) {
            return (num / 1e6).toFixed(1) + " Mio.";
        }
        return num.toFixed(0);
    }

    function formatBoolean(v) {
        if (v === true) return "Ja";
        if (v === false) return "Nein";
        return "-";
    }

    function escapeHtml(str) {
        if (str == null) return "";
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    form.addEventListener("submit", (e) => {
        e.preventDefault();
        const symbol = symbolInput.value.trim();
        if (!symbol) {
            setError("Bitte ein Ticker-Symbol eingeben.");
            return;
        }
        fetchValueAnalysis(symbol);
    });

    // Initiales Symbol beim Laden analysieren
    if (typeof VA_INITIAL_SYMBOL === "string" && VA_INITIAL_SYMBOL.trim().length > 0) {
        symbolInput.value = VA_INITIAL_SYMBOL.trim();
        fetchValueAnalysis(VA_INITIAL_SYMBOL.trim());
    }
});
