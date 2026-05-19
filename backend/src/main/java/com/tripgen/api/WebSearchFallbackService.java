package com.tripgen.api;

import org.springframework.stereotype.Service;

@Service
public class WebSearchFallbackService {

    private void appendHiddenGemsBlock(StringBuilder itinerary, TripRequestContext context) {
        String destination = context.getDestination();
        itinerary.append("\nHIDDEN_GEMS:\n");

        if (context.getLanguageCode().equals("ru")) {
            itinerary.append("1. ").append(destination).append(" тихое кафе в старом квартале - Место вдали от туристического потока, куда чаще заходят местные жители. Местный совет: спросите домашний десерт и фирменный чай.\n")
                    .append("2. ").append(destination).append(" скрытая смотровая точка - Спокойное место для закатных фото без толпы на главной площади. Местный совет: приходите ближе к вечеру и наденьте удобную обувь.\n")
                    .append("3. ").append(destination).append(" переулок ремесленников - Маленькие мастерские и локальные дизайнерские лавки с настоящим характером города. Местный совет: спросите у мастеров, какую соседнюю улицу они советуют увидеть.\n");
            return;
        }

        if (context.getLanguageCode().equals("en")) {
            itinerary.append("1. ").append(destination).append(" backstreet cafe - A quiet local stop away from the main tourist route. Local tip: ask for the house dessert and sit where locals gather.\n")
                    .append("2. ").append(destination).append(" hidden viewpoint - A calm angle for golden-hour photos without the crowded main square. Local tip: arrive before sunset and bring comfortable shoes.\n")
                    .append("3. ").append(destination).append(" artisan lane - Small workshops and independent makers with more local character than souvenir streets. Local tip: ask makers which nearby street they personally recommend.\n");
            return;
        }

        itinerary.append("1. ").append(destination).append(" sakit məhəllə kafesi - Turist axınından uzaq, yerli sakinlərin seçdiyi isti və rahat dayanacaq. Yerli məsləhət: ev şirniyyatını və yerli çayı soruşun.\n")
                .append("2. ").append(destination).append(" gizli mənzərə nöqtəsi - Gün batımında şəhəri izdihamsız görmək üçün az tanınan panorama nöqtəsi. Yerli məsləhət: axşamüstü gedin və rahat ayaqqabı geyinin.\n")
                .append("3. ").append(destination).append(" sənətkarlar keçidi - Kiçik emalatxanalar və yerli dizayn dükanları ilə daha orijinal küçə. Yerli məsləhət: ustalardan yaxınlıqdakı sakit küçə tövsiyəsini istəyin.\n");
    }

    private void appendTourismStyleMeta(StringBuilder itinerary, TripRequestContext context) {
        if (!context.hasSelectedTypes()) {
            return;
        }

        if (context.getLanguageCode().equals("ru")) {
            itinerary.append("Выбранные стили путешествия: ").append(context.getSelectedTypes()).append("\n");
            return;
        }

        if (context.getLanguageCode().equals("en")) {
            itinerary.append("Selected tourism styles: ").append(context.getSelectedTypes()).append("\n");
            return;
        }

        itinerary.append("Seçilən səyahət stilləri: ").append(context.getSelectedTypes()).append("\n");
    }

    private String tourismStyleNote(TripRequestContext context) {
        if (!context.hasSelectedTypes()) {
            return "";
        }

        if (context.getLanguageCode().equals("ru")) {
            return " Маршрут адаптирован под выбранные стили: " + context.getSelectedTypes() + ".";
        }

        if (context.getLanguageCode().equals("en")) {
            return " The route is tailored around the selected tourism styles: " + context.getSelectedTypes() + ".";
        }

        return " Marşrut seçilən turizm stillərinə uyğunlaşdırılıb: " + context.getSelectedTypes() + ".";
    }

    public String generateFromStaticSearch(TripRequestContext context) {
        if (context.getLanguageCode().equals("en")) {
            return generateEnglishFallback(context);
        }

        if (context.getLanguageCode().equals("ru")) {
            return generateRussianFallback(context);
        }

        StringBuilder itinerary = new StringBuilder();
        itinerary.append("GUNN Web Search Fallback").append("\n");
        itinerary.append("İstiqamət: ").append(context.getDestination()).append("\n");
        itinerary.append("Büdcə tipi: ").append(context.getBudgetType()).append("\n");
        appendTourismStyleMeta(itinerary, context);
        itinerary.append("Mənbə: Statik axtarış simulyatoru").append("\n\n");

        for (int i = 1; i <= context.getDays(); i++) {
            itinerary.append(i)
                    .append("-ci gün: ")
                    .append(context.getDestination())
                    .append(" üçün mərkəzi görməli yerlər, yerli mətbəx dayanacağı, ictimai nəqliyyatla rahat marşrut və axşam üçün sakit gəzinti planı.")
                    .append(tourismStyleNote(context))
                    .append("\n");
        }

        appendHiddenGemsBlock(itinerary, context);

        itinerary.append("\nIMAGE_KEYWORDS: ")
                .append(context.getDestination())
                .append(" landmark, ")
                .append(context.getDestination())
                .append(" street, ")
                .append(context.getDestination())
                .append(" city view");
        itinerary.append("\nQeyd: AI provayderləri müvəqqəti əlçatan olmadı. Sistem xəta verməmək üçün statik axtarış fallback planı qaytardı.");
        return itinerary.toString();
    }

    private String generateEnglishFallback(TripRequestContext context) {
        StringBuilder itinerary = new StringBuilder();
        itinerary.append("GUNN Web Search Fallback").append("\n");
        itinerary.append("Destination: ").append(context.getDestination()).append("\n");
        itinerary.append("Budget type: ").append(context.getBudgetType()).append("\n");
        appendTourismStyleMeta(itinerary, context);
        itinerary.append(context.getLanguageInstruction()).append("\n\n");

        for (int i = 1; i <= context.getDays(); i++) {
            itinerary.append("Day ")
                    .append(i)
                    .append(": Central sights in ")
                    .append(context.getDestination())
                    .append(", local food stop, easy public transport route and a calm evening walk.")
                    .append(tourismStyleNote(context))
                    .append("\n");
        }

        appendHiddenGemsBlock(itinerary, context);

        itinerary.append("\nIMAGE_KEYWORDS: ")
                .append(context.getDestination())
                .append(" landmark, ")
                .append(context.getDestination())
                .append(" street, ")
                .append(context.getDestination())
                .append(" city view");
        itinerary.append("\nNote: AI providers were temporarily unavailable, so GUNN returned a static search fallback plan.");
        return itinerary.toString();
    }

    private String generateRussianFallback(TripRequestContext context) {
        StringBuilder itinerary = new StringBuilder();
        itinerary.append("Резервный поиск GUNN").append("\n");
        itinerary.append("Направление: ").append(context.getDestination()).append("\n");
        itinerary.append("Тип бюджета: ").append(context.getBudgetType()).append("\n");
        appendTourismStyleMeta(itinerary, context);
        itinerary.append(context.getLanguageInstruction()).append("\n\n");

        for (int i = 1; i <= context.getDays(); i++) {
            itinerary.append("День ")
                    .append(i)
                    .append(": Главные места в ")
                    .append(context.getDestination())
                    .append(", остановка для местной кухни, удобный маршрут на транспорте и спокойная вечерняя прогулка.")
                    .append(tourismStyleNote(context))
                    .append("\n");
        }

        appendHiddenGemsBlock(itinerary, context);

        itinerary.append("\nIMAGE_KEYWORDS: ")
                .append(context.getDestination())
                .append(" landmark, ")
                .append(context.getDestination())
                .append(" street, ")
                .append(context.getDestination())
                .append(" city view");
        itinerary.append("\nПримечание: AI-провайдеры временно недоступны, поэтому GUNN вернул статический резервный план.");
        return itinerary.toString();
    }
}
