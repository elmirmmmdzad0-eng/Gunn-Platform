package com.tripgen.api;

import java.text.Normalizer;
import java.util.Locale;

final class ConcretePlaceCatalog {

    private ConcretePlaceCatalog() {
    }

    static String hiddenGemsBlock(TripRequestContext context) {
        PlaceSet places = resolve(context.getDestination());
        String tip = switch (context.getLanguageCode()) {
            case "ru" -> "Local tip";
            case "az" -> "Yerli meslehet";
            default -> "Local tip";
        };

        return """
                HIDDEN_GEMS:
                1. %s - %s: go before the main evening rush and ask staff for the most seasonal item.
                2. %s - %s: reserve or arrive early because the room is small and fills quickly.
                3. %s - %s: save it for golden hour and use the nearby side streets for quieter photos.
                """.formatted(
                places.hiddenGems()[0], tip,
                places.hiddenGems()[1], tip,
                places.hiddenGems()[2], tip
        ).trim();
    }

    static String touristInfrastructureBlock(TripRequestContext context) {
        PlaceSet places = resolve(context.getDestination());
        String hotels = String.join(", ", hotelsForBudget(places, context.getBudgetType()));

        return """
                TOURIST_INFRASTRUCTURE:
                Real hotels: %s
                Currency exchange: %s
                Fee-friendly ATMs: %s
                Shopping and daily essentials: %s
                """.formatted(
                hotels,
                String.join(", ", places.currencyExchange()),
                String.join(", ", places.atms()),
                String.join(", ", places.shopping())
        ).trim();
    }

    static String mapPointsLine(TripRequestContext context) {
        PlaceSet places = resolve(context.getDestination());
        String[] hotels = hotelsForBudget(places, context.getBudgetType());
        return "MAP_POINTS: " + String.join(", ",
                hotels[0],
                hotels[1],
                places.currencyExchange()[0],
                places.atms()[0],
                places.shopping()[0],
                places.hiddenGems()[0],
                places.hiddenGems()[1]
        );
    }

    static String dayLine(TripRequestContext context, int zeroBasedDay) {
        PlaceSet places = resolve(context.getDestination());
        String[] hotels = hotelsForBudget(places, context.getBudgetType());
        int index = Math.floorMod(zeroBasedDay, places.landmarks().length);
        String landmark = places.landmarks()[index];
        String hiddenGem = places.hiddenGems()[Math.floorMod(zeroBasedDay, places.hiddenGems().length)];
        String shopping = places.shopping()[Math.floorMod(zeroBasedDay, places.shopping().length)];
        String hotel = hotels[Math.floorMod(zeroBasedDay, hotels.length)];

        return switch (context.getLanguageCode()) {
            case "ru" -> hotel + " check-in, " + landmark + ", " + hiddenGem
                    + ", " + shopping + " and a route planned with official transport stops.";
            case "az" -> hotel + " check-in, " + landmark + ", " + hiddenGem
                    + ", " + shopping + " ve resmi neqliyyat dayanacaqlari ile plan.";
            default -> hotel + " check-in, " + landmark + ", " + hiddenGem
                    + ", " + shopping + " and a route planned with official transport stops.";
        };
    }

    static String promptRules(String destination, String currency) {
        return """
                STRICT CONCRETE PLACE RULES:
                - Do not write generic phrases such as "quiet neighborhood cafe", "central hotel", "local restaurant", "hidden viewpoint", "artisan lane", "main shopping street", or "nearby ATM".
                - Every hotel, cafe, restaurant, museum, bank, exchange point, ATM network, mall, supermarket and viewpoint must be a real official place or brand name operating in or relevant to %s.
                - HOTEL must include exactly 3 real hotel names matched to the user's budget type, with prices in %s where price is mentioned.
                - HACKS must include these concrete clusters: Currency exchange, Fee-friendly ATMs, Shopping and daily essentials, Hidden gems.
                - Hidden gems must be real named cafes, restaurants, museums or viewpoints with a practical local tip.
                - Add a final MAP_POINTS line inside HACKS with 5-7 comma-separated real place names for Google Maps and Earth coverage.
                """.formatted(destination, currency);
    }

    private static String[] hotelsForBudget(PlaceSet places, String budgetType) {
        String normalized = normalize(budgetType);
        if (normalized.contains("luks") || normalized.contains("lux") || normalized.contains("yuks")) {
            return places.luxuryHotels();
        }
        if (normalized.contains("ekonom") || normalized.contains("low") || normalized.contains("asagi")) {
            return places.budgetHotels();
        }
        return places.midHotels();
    }

    private static PlaceSet resolve(String destination) {
        String normalized = normalize(destination);
        if (normalized.contains("paris")) {
            return new PlaceSet(
                    new String[]{"Hotel Darcet", "Hotel Joke Astotel", "Motel One Paris-Porte Doree"},
                    new String[]{"Hotel Le Six", "Hotel Malte Astotel", "Hotel Fabric"},
                    new String[]{"Hotel Ritz Paris", "Le Bristol Paris", "Cheval Blanc Paris"},
                    new String[]{"ChangeGroup Opera", "Travelex Gare du Nord", "BNP Paribas Saint-Germain"},
                    new String[]{"BNP Paribas", "Credit Agricole", "Societe Generale"},
                    new String[]{"Westfield Forum des Halles", "La Grande Epicerie de Paris", "Rue Saint-Honore"},
                    new String[]{"Musee Jacquemart-Andre", "Passage des Panoramas", "Coulée Verte René-Dumont"},
                    new String[]{"Louvre Museum", "Sainte-Chapelle", "Jardin du Luxembourg", "Musee d'Orsay"}
            );
        }
        if (normalized.contains("london")) {
            return new PlaceSet(
                    new String[]{"hub by Premier Inn London Westminster Abbey", "Z Hotel Piccadilly", "Point A Hotel London Kings Cross"},
                    new String[]{"The Hoxton Holborn", "citizenM Tower of London", "The Clermont London Charing Cross"},
                    new String[]{"The Savoy", "Claridge's", "The Connaught"},
                    new String[]{"Post Office Exchange", "Thomas Exchange Global Strand", "Covent Garden FX"},
                    new String[]{"Barclays", "NatWest", "HSBC UK"},
                    new String[]{"Westfield London", "Borough Market", "Regent Street"},
                    new String[]{"Sir John Soane's Museum", "Neal's Yard", "Kyoto Garden"},
                    new String[]{"British Museum", "Tower Bridge", "Tate Modern", "Sky Garden"}
            );
        }
        if (normalized.contains("istanbul")) {
            return new PlaceSet(
                    new String[]{"Cheers Hostel Istanbul", "Hotel Agan Oldcity Istanbul", "Ibis Istanbul Zeytinburnu"},
                    new String[]{"Mest Hotel Istanbul Sirkeci", "Walton Hotels Galata", "The Bank Hotel Istanbul"},
                    new String[]{"Ciragan Palace Kempinski Istanbul", "Four Seasons Hotel Istanbul at Sultanahmet", "Raffles Istanbul"},
                    new String[]{"Kapalicarsi Doviz", "Nuruosmaniye Doviz", "Garanti BBVA Sultanahmet"},
                    new String[]{"Ziraat Bankasi", "Isbank", "Garanti BBVA"},
                    new String[]{"IstinyePark", "Kanyon", "Migros"},
                    new String[]{"Balat Antik Cafe", "Sakirin Mosque", "Kuzguncuk Nail Kitabevi"},
                    new String[]{"Hagia Sophia", "Topkapi Palace", "Galata Tower", "Dolmabahce Palace"}
            );
        }
        if (normalized.contains("baku") || normalized.contains("baki")) {
            return new PlaceSet(
                    new String[]{"Sahil Hostel & Hotel", "ibis Baku City", "Travel Inn Hostel Baku"},
                    new String[]{"Central Park Hotel Baku", "Park Inn by Radisson Baku", "Winter Park Hotel Baku"},
                    new String[]{"Four Seasons Hotel Baku", "The Ritz-Carlton Baku", "JW Marriott Absheron Baku"},
                    new String[]{"Kapital Bank Icherisheher", "Pasha Bank Port Baku", "International Bank of Azerbaijan Sahil"},
                    new String[]{"Kapital Bank", "ABB", "Pasha Bank"},
                    new String[]{"Port Baku Mall", "28 Mall", "Bravo Supermarket"},
                    new String[]{"YARAT Contemporary Art Space", "Miniature Books Museum", "Highland Park"},
                    new String[]{"Maiden Tower", "Heydar Aliyev Center", "Baku Boulevard", "Flame Towers"}
            );
        }
        if (normalized.contains("rome") || normalized.contains("roma")) {
            return new PlaceSet(
                    new String[]{"Hotel Santa Maria", "Hotel Aberdeen Rome", "The Beehive Rome"},
                    new String[]{"Hotel Artemide", "The Guardian Hotel", "Hotel Damaso"},
                    new String[]{"Hotel de Russie", "Hassler Roma", "The St. Regis Rome"},
                    new String[]{"Poste Italiane Roma Centro", "Forexchange Roma Termini", "Intesa Sanpaolo Piazza di Spagna"},
                    new String[]{"UniCredit", "Intesa Sanpaolo", "BNL BNP Paribas"},
                    new String[]{"Galleria Alberto Sordi", "Eataly Roma", "Via del Corso"},
                    new String[]{"Giardino degli Aranci", "Centrale Montemartini", "Quartiere Coppedè"},
                    new String[]{"Colosseum", "Pantheon", "Vatican Museums", "Trevi Fountain"}
            );
        }
        return new PlaceSet(
                new String[]{"ibis Styles City Centre", "Motel One City Centre", "B&B Hotel City Centre"},
                new String[]{"Hilton Garden Inn City Centre", "Radisson Blu City Centre", "Novotel City Centre"},
                new String[]{"Four Seasons Hotel", "The Ritz-Carlton", "Mandarin Oriental"},
                new String[]{"Western Union central branch", "MoneyGram central branch", "Travelex city branch"},
                new String[]{"HSBC", "Santander", "BNP Paribas"},
                new String[]{"Westfield", "Carrefour", "Zara flagship store"},
                new String[]{"Museum of Modern Art", "Old Town viewpoint", "Central Market food hall"},
                new String[]{"National Museum", "Old Town Square", "Main Cathedral", "City Hall"}
        );
    }

    private static String normalize(String value) {
        String cleaned = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        String decomposed = Normalizer.normalize(cleaned, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private record PlaceSet(
            String[] budgetHotels,
            String[] midHotels,
            String[] luxuryHotels,
            String[] currencyExchange,
            String[] atms,
            String[] shopping,
            String[] hiddenGems,
            String[] landmarks
    ) {
    }
}
