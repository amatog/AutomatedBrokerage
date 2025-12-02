<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>Portfolio-Analyse mit KI</title>

    <!-- Tailwind CSS (Utility-First Framework) -->
    <script src="https://cdn.tailwindcss.com"></script>

    <!-- Chart.js (für Pie + Bar + Line) -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <!-- ApexCharts (für Risk-Gauge) -->
    <script src="https://cdn.jsdelivr.net/npm/apexcharts"></script>

    <!-- Alpine.js (für kleine Interaktionen) -->
    <script defer src="https://unpkg.com/alpinejs@3.x.x/dist/cdn.min.js"></script>

    <!-- eigenes CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/portfolio-analysis.css">
</head>
<body class="bg-slate-100 text-slate-900 min-h-screen flex flex-col">

<!-- HEADER -->
<header class="bg-slate-900 text-slate-50 shadow-lg">
    <div class="max-w-6xl mx-auto px-5 py-4 flex flex-col md:flex-row md:items-center md:justify-between gap-2">
        <div>
            <h1 class="text-xl font-semibold">Portfolio-Analyse</h1>
            <p class="text-sm text-slate-400">
                Übersicht über Positionen, Sektoren &amp; KI-Einschätzungen.
            </p>
        </div>
        <span class="inline-flex items-center rounded-full border border-sky-500/60 bg-sky-500/10 px-3 py-1 text-xs text-sky-200">
            <span class="mr-2 inline-block h-2 w-2 rounded-full bg-emerald-400"></span>
            KI-gestützte Analyse aktiv
        </span>
    </div>
</header>

<main class="flex-1 max-w-6xl mx-auto px-5 py-6 space-y-6">

    <!-- KPIs -->
    <section class="grid gap-4 md:grid-cols-3">
        <!-- Gesamtmarktwert -->
        <div class="pa-card">
            <div class="flex items-baseline justify-between mb-2">
                <div>
                    <div class="text-sm font-medium text-slate-700">Gesamtmarktwert</div>
                    <div class="text-xs text-slate-400">Summe aller Positionen</div>
                </div>
                <span class="rounded-full border border-slate-200 bg-slate-50 px-2 py-0.5 text-[10px] text-slate-500">
                    Live
                </span>
            </div>
            <div class="text-2xl font-semibold">
                <c:out value="${analysis.totalMarketValue}" default="–"/> CHF
            </div>
        </div>

        <!-- Anzahl Positionen -->
        <div class="pa-card">
            <div class="flex items-baseline justify-between mb-2">
                <div>
                    <div class="text-sm font-medium text-slate-700">Anzahl Positionen</div>
                    <div class="text-xs text-slate-400">Aktive Titel</div>
                </div>
                <span class="rounded-full border border-slate-200 bg-slate-50 px-2 py-0.5 text-[10px] text-slate-500">
                    Portfolio
                </span>
            </div>
            <div class="text-2xl font-semibold">
                <c:choose>
                    <c:when test="${not empty positions}">
                        ${fn:length(positions)}
                    </c:when>
                    <c:otherwise>0</c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Größte Position -->
        <div class="pa-card">
            <div class="flex items-baseline justify-between mb-2">
                <div>
                    <div class="text-sm font-medium text-slate-700">Größte Position</div>
                    <div class="text-xs text-slate-400">Konzentrationsrisiko</div>
                </div>
                <span class="rounded-full border border-slate-200 bg-slate-50 px-2 py-0.5 text-[10px] text-slate-500">
                    Max. Gewicht
                </span>
            </div>
            <div class="text-xl font-semibold">
                <c:out value="${analysis.topPositionSymbol}" default="–"/>
            </div>
            <div class="text-xs text-slate-500">
                Anteil: <c:out value="${analysis.topPositionWeight}" default="–"/> %
            </div>
        </div>
    </section>

    <!-- Charts: Allokation + Sektoren -->
    <section class="grid gap-4 md:grid-cols-2">
        <!-- Portfolio nach Titeln (Pie) -->
        <div class="pa-card">
            <div class="flex items-baseline justify-between mb-2">
                <div>
                    <div class="text-sm font-medium text-slate-700">Portfolio nach Titeln</div>
                    <div class="text-xs text-slate-400">Marktwert je Symbol (Kuchendiagramm)</div>
                </div>
                <span class="inline-flex items-center rounded-full bg-sky-50 px-2 py-0.5 text-[11px] text-sky-700">
                    <span class="mr-1 inline-block h-2 w-2 rounded-full bg-sky-500"></span>
                    Allokation
                </span>
            </div>
            <canvas id="positionsPieChart" height="200"></canvas>
        </div>

        <!-- Sektorallokation (Bar) -->
        <div class="pa-card">
            <div class="flex items-baseline justify-between mb-2">
                <div>
                    <div class="text-sm font-medium text-slate-700">Sektorallokation</div>
                    <div class="text-xs text-slate-400">Gewichtung der Sektoren</div>
                </div>
                <span class="inline-flex items-center rounded-full bg-indigo-50 px-2 py-0.5 text-[11px] text-indigo-700">
                    <span class="mr-1 inline-block h-2 w-2 rounded-full bg-indigo-500"></span>
                    Sektoren
                </span>
            </div>
            <canvas id="sectorBarChart" height="200"></canvas>

            <div class="mt-3 space-y-1 text-xs text-slate-500">
                <p>
                    <span class="font-semibold text-slate-700">Diversifikation:</span>
                    <c:out value="${analysis.diversificationComment}" default="—"/>
                </p>
                <p>
                    <span class="font-semibold text-slate-700">Tech-Anteil:</span>
                    <c:out value="${analysis.techWeight}" default="—"/> %
                </p>
            </div>
        </div>
    </section>

    <!-- Charts: Performance + Risiko-Gauge -->
    <section class="grid gap-4 md:grid-cols-2">
        <!-- Performance (Line - Demo) -->
        <div class="pa-card">
            <div class="flex items-baseline justify-between mb-2">
                <div>
                    <div class="text-sm font-medium text-slate-700">Portfolio-Entwicklung</div>
                    <div class="text-xs text-slate-400">
                        Demo-Zeitreihe – später durch echte Historie ersetzbar.
                    </div>
                </div>
                <span class="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-[11px] text-emerald-700">
                    <span class="mr-1 inline-block h-2 w-2 rounded-full bg-emerald-500"></span>
                    Performance
                </span>
            </div>
            <canvas id="performanceLineChart" height="200"></canvas>
        </div>

        <!-- Risiko (ApexCharts Gauge) -->
        <div class="pa-card">
            <div class="flex items-baseline justify-between mb-2">
                <div>
                    <div class="text-sm font-medium text-slate-700">Risikoprofil</div>
                    <div class="text-xs text-slate-400">
                        Basierend auf der größten Einzelposition.
                    </div>
                </div>
                <span class="inline-flex items-center rounded-full bg-rose-50 px-2 py-0.5 text-[11px] text-rose-700">
                    <span class="mr-1 inline-block h-2 w-2 rounded-full bg-rose-500"></span>
                    Risiko
                </span>
            </div>

            <div class="pa-risk-score-wrapper mt-2">
                <div id="riskGaugeChart" class="w-full h-48"></div>
                <div class="pa-risk-score-value">
                    <div class="text-[11px] text-slate-500">Konzentrationsgrad</div>
                    <strong id="riskScoreValue">–</strong>
                    <div class="text-[11px] text-slate-500">Max. Einzelposition</div>
                </div>
            </div>

            <div class="mt-3 space-y-1 text-xs text-slate-500">
                <p>
                    <span class="font-semibold text-slate-700">Risiko-Einschätzung:</span>
                    <c:out value="${analysis.riskComment}" default="—"/>
                </p>
                <p>
                    <span class="font-semibold text-slate-700">Volatilität:</span>
                    <c:out value="${analysis.volatilityComment}" default="—"/>
                </p>
            </div>
        </div>
    </section>

    <!-- Tabelle + KI-Assistent -->
    <section class="grid gap-4 md:grid-cols-2">

        <!-- Positionstabelle -->
        <div class="pa-card">
            <div class="mb-2 flex items-baseline justify-between">
                <div>
                    <div class="text-sm font-medium text-slate-700">Offene Positionen</div>
                    <div class="text-xs text-slate-400">Detailansicht aller Titel</div>
                </div>
            </div>

            <c:if test="${empty positions}">
                <p class="text-xs text-slate-500">Keine Positionen vorhanden.</p>
            </c:if>

            <c:if test="${not empty positions}">
                <div class="overflow-x-auto">
                    <table class="min-w-full text-xs">
                        <thead class="border-b border-slate-200 bg-slate-50 text-slate-500">
                        <tr>
                            <th class="px-2 py-1 text-left font-semibold uppercase tracking-wide">Symbol</th>
                            <th class="px-2 py-1 text-left font-semibold uppercase tracking-wide">Name</th>
                            <th class="px-2 py-1 text-left font-semibold uppercase tracking-wide">Sektor</th>
                            <th class="px-2 py-1 text-right font-semibold uppercase tracking-wide">Stück</th>
                            <th class="px-2 py-1 text-right font-semibold uppercase tracking-wide">Marktwert</th>
                            <th class="px-2 py-1 text-right font-semibold uppercase tracking-wide">unreal. P&amp;L</th>
                        </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-100">
                        <c:forEach var="p" items="${positions}">
                            <tr class="hover:bg-slate-50">
                                <td class="px-2 py-1 align-middle font-medium text-slate-800">${p.symbol}</td>
                                <td class="px-2 py-1 align-middle text-slate-700">${p.name}</td>
                                <td class="px-2 py-1 align-middle text-slate-500">${p.sector}</td>
                                <td class="px-2 py-1 align-middle text-right">
                                    <c:out value="${p.quantity}" default="—"/>
                                </td>
                                <td class="px-2 py-1 align-middle text-right">
                                    <c:out value="${p.marketValue}" default="—"/>
                                </td>
                                <td class="px-2 py-1 align-middle text-right">
                                    <c:choose>
                                        <c:when test="${p.unrealizedPnl < 0}">
                                            <span class="text-rose-600">${p.unrealizedPnl}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-emerald-600">${p.unrealizedPnl}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
        </div>

        <!-- KI-Assistent (Alpine.js) -->
        <div class="pa-card" x-data="{ showMeta: false }">
            <div class="mb-2 flex items-baseline justify-between">
                <div>
                    <div class="text-sm font-medium text-slate-700">Smart Trading Assistant</div>
                    <div class="text-xs text-slate-400">KI-Analyse deines Portfolios</div>
                </div>

                <button
                        type="button"
                        class="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] text-slate-600 hover:bg-slate-100"
                        @click="showMeta = !showMeta">
                    <span x-show="!showMeta">Details einblenden</span>
                    <span x-show="showMeta">Details ausblenden</span>
                </button>
            </div>

            <div class="text-xs leading-relaxed text-slate-800">
                <c:out value="${aiContent}" default="Keine KI-Analyse verfügbar."/>
            </div>

            <div x-show="showMeta" x-transition
                 class="mt-3 border-t border-dashed border-slate-200 pt-2 text-[11px] text-slate-500 space-y-1">
                <p>
                    <span class="font-semibold text-slate-700">Modell:</span>
                    <c:out value="${aiModel}" default="—"/>
                </p>
                <p>
                    <span class="font-semibold text-slate-700">Tokens gesamt:</span>
                    <c:out value="${aiTotalTokens}" default="—"/>
                    &nbsp;|&nbsp;
                    <span class="font-semibold text-slate-700">Antwort-Tokens:</span>
                    <c:out value="${aiCompletionTokens}" default="—"/>
                </p>
                <p>
                    <span class="font-semibold text-slate-700">Service-Tier:</span>
                    <c:out value="${aiServiceTier}" default="—"/>
                </p>
            </div>
        </div>
    </section>

    <!-- Zurück zum Dashboard -->
    <a href="${pageContext.request.contextPath}/dashboard"
       class="inline-flex items-center text-xs text-sky-700 hover:text-sky-900">
        <span class="mr-1">&larr;</span> Zurück zum Dashboard
    </a>

</main>

<!-- Daten-Container für JS (ohne Inline-JS-Logik) -->
<div id="pa-data"
     style="display:none"
     data-position-labels='[
        <c:forEach items="${positions}" var="p" varStatus="st">
            "${fn:escapeXml(p.symbol)}"<c:if test="${!st.last}">,</c:if>
        </c:forEach>
     ]'
     data-position-values='[
        <c:forEach items="${positions}" var="p" varStatus="st">
            ${p.marketValue != null ? p.marketValue : 0}<c:if test="${!st.last}">,</c:if>
        </c:forEach>
     ]'
     data-sector-labels='[
        <c:forEach items="${analysis.sectorWeights}" var="entry" varStatus="st">
            "${fn:escapeXml(entry.key)}"<c:if test="${!st.last}">,</c:if>
        </c:forEach>
     ]'
     data-sector-values='[
        <c:forEach items="${analysis.sectorWeights}" var="entry" varStatus="st">
            ${entry.value != null ? entry.value : 0}<c:if test="${!st.last}">,</c:if>
        </c:forEach>
     ]'
     data-risk-score='${empty analysis.topPositionWeight ? 0 : analysis.topPositionWeight}'>
</div>

<!-- eigenes JS -->
<script src="${pageContext.request.contextPath}/js/portfolio-analysis.js"></script>

</body>
</html>
