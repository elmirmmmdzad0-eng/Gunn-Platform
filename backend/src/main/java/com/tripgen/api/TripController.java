package com.tripgen.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    @PostMapping("/generate")
    public ResponseEntity<TripResponse> generateTrip(@RequestBody TripRequest request) {
        String destination = cleanDestination(request.getDestination());
        int days = normalizeDays(request.getDays());
        String budgetType = cleanBudgetType(request.getBudgetType());
        String itinerary = buildMockItinerary(destination, days, budgetType);

        TripResponse response = new TripResponse(
                UUID.randomUUID().toString(),
                destination,
                days,
                itinerary
        );

        return ResponseEntity.ok(response);
    }

    private String buildMockItinerary(String destination, int days, String budgetType) {
        String[] istanbulDays = {
                "Sultanahmet meydani, Aya Sofya, Sultanahmet Camii ve Gulhane parki. Axsam Eminonu sahilinde balik-ekmek ve Bosfor menzeresi.",
                "Karakoy kafeleri, Galata qüllesi, Istiklal prospekti ve Kadikoyde Moda sahili. Axsam yerli restoranlarda rahat yemək planı.",
                "Dolmabahce sarayi, Besiktas bazari, Ortakoy mescidi ve Bosfor turu. Gun batiminda sahil boyunca gəzinti.",
                "Balat rengli kuceleri, Fener, Pierre Loti ve Eyup. Foto dayanacaqlari ve sakit kofe fasilələri.",
                "Uskudar, Kiz Qalasi menzeresi, Camlica tepesi ve Kuzguncuk. Axsam geleneksel şirniyyat dayanacağı.",
                "Kapali Carsi, Misir Carsisi, Nisantasi ve yerli dizayn mağazaları. Budceye uygun alış-veriş marşrutu.",
                "Adalar və ya rahat spa günü. Səfəri yüngül brunch, son hədiyyələr və hava limanına rahat transferlə bağla."
        };

        String[] genericDays = {
                "Şəhərin tarixi mərkəzi, əsas meydanlar və yerli mətbəxlə tanışlıq. Axsam rahat gəzinti və foto dayanacaqları.",
                "Muzeylər, məşhur küçələr, yerli bazar və ən yaxşı kafe dayanacaqları. Günün sonunda panoramik mənzərə nöqtəsi.",
                "Təbiət və ya sahil marşrutu, yerli məhəllələr və səyahətçinin tempinə uyğun boş vaxt.",
                "Gizli məkanlar, butik mağazalar, yerli restoranlar və axşam üçün sakit mədəni proqram.",
                "Yaxın ətraf rayon və ya qısa günlük tur. Qayıdışda yüngül axşam yeməyi.",
                "Alış-veriş, suvenir, yerli desertlər və rahat şəhər içi nəqliyyat marşrutu.",
                "Səfərin yekunu: sevimli məkanlara qısa dönüş, brunch, çamadan və hava limanına transfer."
        };

        boolean isIstanbul = destination.toLowerCase(Locale.ROOT).contains("istanbul")
                || destination.toLowerCase(Locale.ROOT).contains("i̇stanbul");
        String[] dayPool = isIstanbul ? istanbulDays : genericDays;

        StringBuilder itinerary = new StringBuilder();
        itinerary.append("TripGen AI Mock Plan").append("\n");
        itinerary.append("Istiqamet: ").append(destination).append("\n");
        itinerary.append("Budce tipi: ").append(budgetType).append("\n\n");

        for (int i = 0; i < days; i++) {
            itinerary.append(i + 1)
                    .append("-ci gun: ")
                    .append(dayPool[i])
                    .append("\n");
        }

        itinerary.append("\nQeyd: Bu test ucun mock AI cavabidir. Gemini/OpenAI qosulanda eyni response strukturu saxlanacaq.");
        return itinerary.toString();
    }

    private String cleanDestination(String destination) {
        if (destination == null || destination.trim().isBlank()) {
            return "Istanbul";
        }

        return destination.trim().replaceAll("\\s+", " ");
    }

    private int normalizeDays(int days) {
        if (days < 1) {
            return 1;
        }

        return Math.min(days, 7);
    }

    private String cleanBudgetType(String budgetType) {
        if (budgetType == null || budgetType.trim().isBlank()) {
            return "Orta";
        }

        return budgetType.trim();
    }
}
