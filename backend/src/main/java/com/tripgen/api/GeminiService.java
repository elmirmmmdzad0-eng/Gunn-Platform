package com.tripgen.api;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class GeminiService implements TripPlanProvider {

    @Override
    public String getProviderName() {
        return "Gemini";
    }

    @Override
    public String generate(TripRequestContext context) {
        if (context.getLanguageCode().equals("en")) {
            return generateEnglish(context);
        }

        if (context.getLanguageCode().equals("ru")) {
            return generateRussian(context);
        }

        return generateAzerbaijani(context);
    }

    private void appendHiddenGemsBlock(StringBuilder itinerary, String destination) {
        itinerary.append("\nHIDDEN_GEMS:\n")
                .append("1. ").append(destination).append(" backstreet cafe - A quiet local stop away from the main tourist route. Local tip: ask for the house dessert and sit where locals gather.\n")
                .append("2. ").append(destination).append(" hidden viewpoint - A calm angle for golden-hour photos without the crowded main square. Local tip: arrive before sunset and bring comfortable shoes.\n")
                .append("3. ").append(destination).append(" artisan lane - Small workshops and independent makers with more local character than souvenir streets. Local tip: ask makers which nearby street they personally recommend.\n");
    }

    private String generateAzerbaijani(TripRequestContext context) {
        String destination = context.getDestination();
        String[] istanbulDays = {
                "Sultanahmet meydanı, Aya Sofya, Sultanahmet Camii və Gülhanə parkı. Axşam Eminönü sahilində balıq-ekmek və Bosfor mənzərəsi.",
                "Karaköy kafeləri, Galata qülləsi, İstiqlal prospekti və Kadıköydə Moda sahili. Axşam yerli restoranlarda rahat yemək planı.",
                "Dolmabahçe sarayı, Beşiktaş bazarı, Ortaköy məscidi və Bosfor turu. Gün batımında sahil boyunca gəzinti.",
                "Balat rəngli küçələri, Fener, Pierre Loti və Eyüp. Foto dayanacaqları və sakit kofe fasilələri.",
                "Üsküdar, Qız Qalası mənzərəsi, Çamlıca təpəsi və Kuzguncuk. Axşam ənənəvi şirniyyat dayanacağı.",
                "Kapalı Çarşı, Misir Çarşısı, Nişantaşı və yerli dizayn mağazaları. Büdcəyə uyğun alış-veriş marşrutu.",
                "Adalar və ya rahat spa günü. Səfəri yüngül brunch, son hədiyyələr və hava limanına rahat transferlə bağla."
        };

        String[] genericDays = {
                "Şəhərin tarixi mərkəzi, əsas meydanları və yerli mətbəxi ilə tanışlıq. Axşam rahat gəzinti və foto dayanacaqları.",
                "Muzeylər, məşhur küçələr, yerli bazar və ən yaxşı kafe dayanacaqları. Günün sonunda panoramik mənzərə nöqtəsi.",
                "Təbiət və ya sahil marşrutu, yerli məhəllələr və səyahətçinin tempinə uyğun boş vaxt.",
                "Gizli məkanlar, butik mağazalar, yerli restoranlar və axşam üçün sakit mədəni proqram.",
                "Yaxın ətraf rayon və ya qısa günlük tur. Qayıdışda yüngül axşam yeməyi.",
                "Alış-veriş, suvenir, yerli desertlər və rahat şəhər içi nəqliyyat marşrutu.",
                "Səfərin yekunu: sevimli məkanlara qısa dönüş, brunch, çamadan və hava limanına transfer."
        };

        boolean isIstanbul = context.getNormalizedDestination().contains("istanbul")
                || destination.toLowerCase(Locale.ROOT).contains("i̇stanbul");
        String[] dayPool = isIstanbul ? istanbulDays : genericDays;

        StringBuilder itinerary = new StringBuilder();
        itinerary.append("TripGen AI Plan").append("\n");
        itinerary.append("İstiqamət: ").append(destination).append("\n");
        itinerary.append("Büdcə tipi: ").append(context.getBudgetType()).append("\n");
        itinerary.append("Mənbə: Gemini fallback generator").append("\n\n");

        for (int i = 0; i < context.getDays(); i++) {
            itinerary.append(i + 1)
                    .append("-ci gün: ")
                    .append(dayPool[i])
                    .append("\n");
        }

        appendHiddenGemsBlock(itinerary, destination);

        itinerary.append("\nIMAGE_KEYWORDS: ")
                .append(destination)
                .append(" landmark, ")
                .append(destination)
                .append(" cafe, ")
                .append(destination)
                .append(" old town, ")
                .append(destination)
                .append(" travel view");
        itinerary.append("\nQeyd: Bu cavab hazırda Gemini fallback/mock generatorundan gəlir. Real AI qoşulanda eyni service zənciri saxlanacaq.");
        return itinerary.toString();
    }

    private String generateEnglish(TripRequestContext context) {
        String destination = context.getDestination();
        String[] dayPool = {
                "Hotel check-in, the historic city center, landmark walk and a relaxed local dinner.",
                "Breakfast at a local cafe, museum visit, scenic neighborhood walk and an evening viewpoint.",
                "Market route, seaside or park break, shopping stop and a comfortable transfer plan.",
                "Hidden streets, boutique shops, local food tasting and a calm cultural evening.",
                "Short day trip near the city, flexible rest time and a light dinner after return.",
                "Souvenir shopping, dessert stop, public transport route and final photo locations.",
                "Slow brunch, favorite places revisit, packing time and airport transfer."
        };

        StringBuilder itinerary = new StringBuilder();
        itinerary.append("TripGen AI Plan").append("\n");
        itinerary.append("Destination: ").append(destination).append("\n");
        itinerary.append("Budget type: ").append(context.getBudgetType()).append("\n");
        itinerary.append("Source: Gemini fallback generator").append("\n");
        itinerary.append(context.getLanguageInstruction()).append("\n\n");

        for (int i = 0; i < context.getDays(); i++) {
            itinerary.append("Day ")
                    .append(i + 1)
                    .append(": ")
                    .append(dayPool[i])
                    .append("\n");
        }

        appendHiddenGemsBlock(itinerary, destination);

        itinerary.append("\nIMAGE_KEYWORDS: ")
                .append(destination)
                .append(" landmark, ")
                .append(destination)
                .append(" cafe, ")
                .append(destination)
                .append(" old town, ")
                .append(destination)
                .append(" travel view");
        itinerary.append("\nNote: This response currently comes from the Gemini fallback/mock generator.");
        return itinerary.toString();
    }

    private String generateRussian(TripRequestContext context) {
        String destination = context.getDestination();
        String[] dayPool = {
                "Заселение в отель, исторический центр, прогулка по главным достопримечательностям и спокойный ужин в местном ресторане.",
                "Завтрак в местном кафе, музей, прогулка по атмосферному району и вечерняя смотровая площадка.",
                "Маршрут по рынку, отдых у воды или в парке, покупки и удобный план трансфера.",
                "Скрытые улочки, бутик-магазины, дегустация местной кухни и спокойная культурная программа вечером.",
                "Короткая поездка за город, свободное время для отдыха и легкий ужин после возвращения.",
                "Покупка сувениров, остановка на десерт, маршрут на общественном транспорте и финальные фото-точки.",
                "Поздний завтрак, повтор любимых мест, время на сборы и трансфер в аэропорт."
        };

        StringBuilder itinerary = new StringBuilder();
        itinerary.append("План TripGen AI").append("\n");
        itinerary.append("Направление: ").append(destination).append("\n");
        itinerary.append("Тип бюджета: ").append(context.getBudgetType()).append("\n");
        itinerary.append("Источник: резервный генератор Gemini").append("\n");
        itinerary.append(context.getLanguageInstruction()).append("\n\n");

        for (int i = 0; i < context.getDays(); i++) {
            itinerary.append("День ")
                    .append(i + 1)
                    .append(": ")
                    .append(dayPool[i])
                    .append("\n");
        }

        appendHiddenGemsBlock(itinerary, destination);

        itinerary.append("\nIMAGE_KEYWORDS: ")
                .append(destination)
                .append(" landmark, ")
                .append(destination)
                .append(" cafe, ")
                .append(destination)
                .append(" old town, ")
                .append(destination)
                .append(" travel view");
        itinerary.append("\nПримечание: Сейчас этот ответ создан резервным/mock генератором Gemini.");
        return itinerary.toString();
    }
}
