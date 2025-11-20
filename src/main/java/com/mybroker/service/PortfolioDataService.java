package com.mybroker.service;

import com.mybroker.model.Position;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PortfolioDataService {

    /**
     * TODO: Hier deine echte Anbindung einbauen (Alpaca, DB, Swissquote-API etc.)
     */
    public List<Position> loadCurrentPositions(String userId) {
        // DEMO-Daten
        List<Position> positions = new ArrayList<>();

        positions.add(new Position(
                "AAPL",
                "Apple Inc.",
                "Technology",
                new BigDecimal("50"),
                new BigDecimal("9000"),
                new BigDecimal("800"),
                new BigDecimal("0.23")
        ));

        positions.add(new Position(
                "MSFT",
                "Microsoft Corp.",
                "Technology",
                new BigDecimal("30"),
                new BigDecimal("7500"),
                new BigDecimal("600"),
                new BigDecimal("0.20")
        ));

        positions.add(new Position(
                "JNJ",
                "Johnson & Johnson",
                "Healthcare",
                new BigDecimal("40"),
                new BigDecimal("6000"),
                new BigDecimal("200"),
                new BigDecimal("0.12")
        ));

        positions.add(new Position(
                "XOM",
                "Exxon Mobil",
                "Energy",
                new BigDecimal("80"),
                new BigDecimal("5000"),
                new BigDecimal("-150"),
                new BigDecimal("0.30")
        ));

        return positions;
    }
}

