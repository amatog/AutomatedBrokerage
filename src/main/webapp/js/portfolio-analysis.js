document.addEventListener("DOMContentLoaded", function () {
    const dataEl = document.getElementById("pa-data");
    if (!dataEl) {
        return;
    }

    // Daten aus data-* Attributen lesen
    const parseJson = (str, fallback) => {
        try {
            return JSON.parse(str);
        } catch (e) {
            return fallback;
        }
    };

    const positionLabels = parseJson(dataEl.dataset.positionLabels || "[]", []);
    const positionValues = parseJson(dataEl.dataset.positionValues || "[]", []);
    const sectorLabels = parseJson(dataEl.dataset.sectorLabels || "[]", []);
    const sectorValues = parseJson(dataEl.dataset.sectorValues || "[]", []);
    const riskScore = parseFloat(dataEl.dataset.riskScore || "0");

    // ----------------------------
    // PIE – Portfolio nach Titeln
    // ----------------------------
    (function () {
        const ctx = document.getElementById("positionsPieChart");
        if (!ctx || typeof Chart === "undefined") return;

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

    // ----------------------------
    // BAR – Sektorallokation
    // ----------------------------
    (function () {
        const ctx = document.getElementById("sectorBarChart");
        if (!ctx || typeof Chart === "undefined") return;

        new Chart(ctx, {
            type: "bar",
            data: {
                labels: sectorLabels,
                datasets: [{
                    data: sectorValues
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
    // LINE – Demo-Performance
    // ----------------------------
    (function () {
        const ctx = document.getElementById("performanceLineChart");
        if (!ctx || typeof Chart === "undefined") return;

        new Chart(ctx, {
            type: "line",
            data: {
                labels: ["T-6", "T-5", "T-4", "T-3", "T-2", "T-1", "Heute"],
                datasets: [{
                    data: [100, 101.5, 99.8, 103.2, 104.1, 102.7, 105.0],
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

        const gaugeValue = Math.max(0, Math.min(100, riskScore));

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
