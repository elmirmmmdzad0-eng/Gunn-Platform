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

        itinerary.append("\nQeyd: Bu cavab hazırda Gemini fallback/mock generatorundan gəlir. Real AI qoşulanda eyni service zənciri saxlanacaq.");
        return itinerary.toString();
    }
}
