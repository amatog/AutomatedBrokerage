document.addEventListener("DOMContentLoaded", function () {
    const dataEl = document.getElementById("pa-data");
    if (!dataEl) {
        return;
    }

    const parseJson = (str, fallback) => {
        try {
            return JSON.parse(str);
        } catch (e) {
            console.warn("Konnte JSON aus data-Attribut nicht parsen:", e);
            return fallback;
        }
    };

    // Daten aus den data-* Attributen lesen
    const positionLabels = parseJson(dataEl.dataset.positionLabels || "[]", []);
    const positionValues = parseJson(dataEl.dataset.positionValues || "[]", []);
    const positionSectors = parseJson(dataEl.dataset.positionSectors || "[]", []);
    const sectorLabels = parseJson(dataEl.dataset.sectorLabels || "[]", []);
    const sectorValues = parseJson(dataEl.dataset.sectorValues || "[]", []);
    const performanceLabels = parseJson(dataEl.dataset.performanceLabels || "[]", []);
    const performanceValues = parseJson(dataEl.dataset.performanceValues || "[]", []);
    const riskScore = parseFloat(dataEl.dataset.riskScore || "0");

    // ----------------------------
    // PIE – Portfolio nach Titeln
    // ----------------------------
    (function () {
        const ctx = document.getElementById("positionsPieChart");
        if (!ctx || typeof Chart === "undefined") return;

        if (!positionLabels.length || !positionValues.length) {
            console.warn("Keine Positionsdaten für Pie-Chart vorhanden.");
            return;
        }

        new Chart(ctx, {
            type: "pie",
            data: {
                labels: positionLabels,
                datasets: [{
                    data: positionValues
                }]
            },
            options: {
                plugins: {
                    legend: {position: "bottom"}
                }
            }
        });
    })();

    // BAR – Sektorallokation (mit graceful Fallback)
    (function () {
        const ctx = document.getElementById("sectorBarChart");
        const msgEl = document.getElementById("sectorChartMessage");
        if (!ctx || typeof Chart === "undefined") return;

        let labels = sectorLabels;
        let values = sectorValues;

        // Prüfen: keine Sektoren, oder nur 1 Sektor und der heißt "unknown" oder "us_equity"
        const onlyUnknown =
            !labels ||
            labels.length === 0 ||
            (labels.length === 1 &&
                (labels[0].toLowerCase() === "unknown" || labels[0].toLowerCase() === "us_equity"));

        if (onlyUnknown) {
            console.warn("Keine sinnvollen Sektordaten – Sektorkachel zeigt Hinweis statt Chart.");
            if (msgEl) {
                msgEl.textContent = "Sektor-Analyse ist aktuell nicht verfügbar (Marktdaten-Service).";
                msgEl.style.display = "block";
            }
            // Chart gar nicht rendern
            return;
        }

        if (msgEl) {
            msgEl.style.display = "none";
        }

        new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [{
                    data: values
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: (value) => value + " %"
                        }
                    }
                },
                plugins: {
                    legend: {display: false}
                }
            }
        });
    })();


    // ----------------------------
    // LINE – Portfolio-Entwicklung (nur echte Daten)
    // ----------------------------
    (function () {
        const ctx = document.getElementById("performanceLineChart");
        if (!ctx || typeof Chart === "undefined") return;

        if (!performanceLabels.length || !performanceValues.length) {
            console.warn("Keine Performance-Daten vorhanden – Line-Chart wird nicht gerendert.");
            return;
        }

        new Chart(ctx, {
            type: "line",
            data: {
                labels: performanceLabels,
                datasets: [{
                    data: performanceValues,
                    tension: 0.35,
                    fill: false
                }]
            },
            options: {
                plugins: {
                    legend: {display: false}
                }
            }
        });
    })();

    // ----------------------------
    // APEXCHARTS – Risiko-Gauge
    // ----------------------------
    (function () {
        const el = document.querySelector("#riskGaugeChart");
        if (!el || typeof ApexCharts === "undefined") return;

        const gaugeValue = Math.max(0, Math.min(100, isNaN(riskScore) ? 0 : riskScore));

        const options = {
            chart: {
                type: "radialBar",
                height: 260,
                toolbar: {show: false},
                sparkline: {enabled: true}
            },
            series: [gaugeValue],
            labels: ["Konzentrationsgrad"],
            plotOptions: {
                radialBar: {
                    startAngle: -110,
                    endAngle: 110,
                    hollow: {size: "60%"},
                    track: {
                        background: "#e5e7eb",
                        strokeWidth: "100%"
                    },
                    dataLabels: {
                        name: {show: false},
                        value: {show: false}
                    }
                }
            },
            fill: {
                type: "gradient",
                gradient: {
                    shade: "light",
                    type: "horizontal",
                    stops: [0, 50, 100],
                    colorStops: [
                        {offset: 0, color: "#22c55e", opacity: 1},
                        {offset: 50, color: "#facc15", opacity: 1},
                        {offset: 100, color: "#ef4444", opacity: 1}
                    ]
                }
            }
        };

        const chart = new ApexCharts(el, options);
        chart.render();

        const riskEl = document.getElementById("riskScoreValue");
        if (riskEl) {
            riskEl.textContent = gaugeValue.toFixed(1) + " %";
        }
    })();
});
