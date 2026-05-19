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
            itinerary.append(buildTourismStylePromptDirective(context)).append("\n");
            return;
        }

        if (context.getLanguageCode().equals("en")) {
            itinerary.append("Selected tourism styles: ").append(context.getSelectedTypes()).append("\n");
            itinerary.append(buildTourismStylePromptDirective(context)).append("\n");
            return;
        }

        itinerary.append("Seçilən səyahət stilləri: ").append(context.getSelectedTypes()).append("\n");
        itinerary.append(buildTourismStylePromptDirective(context)).append("\n");
    }

    private String buildTourismStylePromptDirective(TripRequestContext context) {
        if (!context.hasSelectedTypes()) {
            return "";
        }

        String selectedTypes = context.getSelectedTypes();
        if (context.getLanguageCode().equals("ru")) {
            return "ВНИМАНИЕ: пользователь хочет провести это путешествие именно в этих туристических стилях: "
                    + selectedTypes
                    + ". Если в списке есть концертный туризм, обязательно выдели известные концертные залы и места с живой музыкой в городе. Если выбран гастрономический туризм, отдай приоритет местным вкусам, рынкам и ресторанам. Полностью персонализируй план под выбранные типы.";
        }

        if (context.getLanguageCode().equals("en")) {
            return "CRITICAL INSTRUCTION: The user has strictly customized this trip for the following travel styles: "
                    + selectedTypes
                    + ". You MUST transform the entire itinerary configuration based on these styles. For example, if \"Romantik turizm\" is selected, the daily schedule must strictly prioritize romantic viewpoints, elegant dining, scenic walks, and couples' activities in "
                    + context.getDestination()
                    + ", completely shifting the tone away from standard mass tourism. If \"Konsert turizmi\" is selected, the plan must foreground concert halls, live music venues, evening performances, and music neighborhoods. If \"Qastronomik turizm\" is selected, the plan must prioritize local flavors, markets, food halls, tasting menus, and restaurants. Do not treat these styles as optional metadata; make them the main itinerary logic.";
        }

        return "DİQQƏT: İstifadəçi bu səyahəti xüsusi olaraq bu turizm üslublarında keçirmək istəyir: "
                + selectedTypes
                + ". Əgər siyahıda Konsert turizmi varsa, günlük planda mütləq o şəhərdəki məşhur konsert zallarını və canlı musiqi məkanlarını önə çıxar. Əgər Qastronomik turizm seçilibsə, yerli dadlara, bazarlara və restoranlara üstünlük ver. Planı tamamilə bu seçilən növlərə uyğun fərdiləşdir.";
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

    private boolean selectedTypesContain(TripRequestContext context, String... needles) {
        if (!context.hasSelectedTypes()) {
            return false;
        }

        String selectedTypes = context.getSelectedTypes().toLowerCase(Locale.ROOT);
        for (String needle : needles) {
            if (selectedTypes.contains(needle.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }

    private String[] customizeDayPool(String[] defaultPool, TripRequestContext context) {
        String destination = context.getDestination();

        if (selectedTypesContain(context, "Romantik", "Romantic", "Романтичес")) {
            return switch (context.getLanguageCode()) {
                case "en" -> new String[] {
                        "Begin with a slow breakfast in a charming cafe, then visit a romantic viewpoint over " + destination + ". Reserve an elegant dinner table with a landmark or river view.",
                        "Plan a couples' museum or garden walk, a boutique chocolate or pastry stop, and an evening scenic walk through softly lit streets.",
                        "Choose a private photo spot, a quiet riverside promenade or boat ride, and finish with live piano, jazz or a candlelit dining experience.",
                        "Spend the day around hidden courtyards, flower markets and intimate wine bars, keeping the pace slow and elegant.",
                        "Take a short romantic escape outside the center, then return for sunset views and a refined local restaurant.",
                        "Build a memory-focused route: handwritten postcard stop, artisan gift shop, dessert tasting and night city lights.",
                        "Close with brunch, a favorite-viewpoint revisit, relaxed shopping for a meaningful gift and smooth airport transfer."
                };
                case "ru" -> new String[] {
                        "Начните с неспешного завтрака в уютном кафе, затем выберите романтическую смотровую точку в " + destination + " и завершите день элегантным ужином с красивым видом.",
                        "Запланируйте прогулку для пары по саду или музею, остановку за десертом и вечерний маршрут по мягко освещенным улицам.",
                        "Добавьте приватную фотолокацию, прогулку у воды или вечернюю поездку на лодке, а затем ужин с живой музыкой.",
                        "Проведите день в скрытых двориках, цветочных лавках и камерных винных барах с медленным романтическим темпом.",
                        "Сделайте короткий романтический выезд за пределы центра, вернитесь к закату и выберите изысканный местный ресторан.",
                        "Маршрут для воспоминаний: открытка, мастерская подарков, дегустация десертов и ночные огни города.",
                        "Завершите поездку бранчем, повторным визитом к любимой панораме, покупкой символичного подарка и спокойным трансфером."
                };
                default -> new String[] {
                        destination + " üçün günə zərif kafedə sakit səhər yeməyi ilə başlayın, sonra romantik mənzərə nöqtəsinə gedin və axşamı şəhər və ya çay mənzərəli eleqant restoranda tamamlayın.",
                        "Cütlüklər üçün bağ, muzey və ya sakit qalereya gəzintisi planlayın, ardınca desert dayanacağı və yumşaq işıqlı küçələrdə axşam gəzintisi edin.",
                        "Gizli foto nöqtəsi, su kənarında promenade və ya axşam qayıq gəzintisi seçin, günü canlı piano, caz və ya şam işığında yeməklə bitirin.",
                        "Günü gizli həyətlər, çiçək dükanları və butik şərab barları ətrafında yavaş və romantik tempdə qurun.",
                        "Mərkəzdən qısa romantik qaçış edin, gün batımı mənzərəsinə qayıdın və zərif yerli restoran seçin.",
                        "Xatirə yönümlü marşrut qurun: açıqca dayanacağı, sənətkar hədiyyə dükanı, desert dadımı və gecə şəhər işıqları.",
                        "Səyahəti brunch, sevimli mənzərə nöqtəsinə qayıdış, mənalı hədiyyə alış-verişi və rahat transferlə tamamlayın."
                };
            };
        }

        if (selectedTypesContain(context, "Qastronomik", "Gastronomic", "Гастроном")) {
            return switch (context.getLanguageCode()) {
                case "en" -> new String[] {
                        "Start with a local market and bakery route, then choose a classic neighborhood restaurant for signature dishes.",
                        "Build the day around food halls, specialty coffee, a cooking class or tasting menu, and a relaxed dinner.",
                        "Visit an artisan cheese, chocolate or spice stop, then add a street-food walk and a chef-led restaurant.",
                        "Explore hidden local eateries, family-run cafes and a late dessert bar.",
                        "Take a short food-focused day trip near " + destination + " and return for a seasonal dinner.",
                        "Shop for edible souvenirs, visit gourmet stores and compare two local dessert spots.",
                        "Finish with brunch, a favorite market revisit and a light farewell meal."
                };
                case "ru" -> new String[] {
                        "Начните с местного рынка и пекарни, затем выберите ресторан района с фирменными блюдами.",
                        "Постройте день вокруг фуд-холлов, specialty coffee, кулинарного мастер-класса или дегустационного меню.",
                        "Добавьте ремесленный сыр, шоколад или специи, затем прогулку по стрит-фуду и ресторан от шефа.",
                        "Исследуйте скрытые локальные закусочные, семейные кафе и поздний десерт-бар.",
                        "Сделайте короткую гастрономическую поездку рядом с " + destination + " и вернитесь на сезонный ужин.",
                        "Купите съедобные сувениры, зайдите в гастролавки и сравните два местных десерта.",
                        "Завершите бранчем, повторным визитом на любимый рынок и легким прощальным ужином."
                };
                default -> new String[] {
                        "Günə yerli bazar və çörəkçi marşrutu ilə başlayın, sonra imza yeməkləri olan məhəllə restoranı seçin.",
                        "Günü food hall, specialty coffee, kulinariya master-klası və ya dadım menyusu ətrafında qurun.",
                        "Sənətkar pendir, şokolad və ya ədviyyat dayanacağı əlavə edin, ardınca street-food gəzintisi və chef restoranı seçin.",
                        "Gizli yerli yeməkxanalar, ailə kafeləri və gec desert barı kəşf edin.",
                        destination + " yaxınlığında qısa qastronomik günlük tur edin və mövsümi axşam yeməyinə qayıdın.",
                        "Yeməli suvenirlər alın, gourmet mağazalara baş çəkin və iki yerli desert məkanını müqayisə edin.",
                        "Səyahəti brunch, sevimli bazara qayıdış və yüngül vida yeməyi ilə tamamlayın."
                };
            };
        }

        if (selectedTypesContain(context, "Konsert", "Concert", "Концерт")) {
            return switch (context.getLanguageCode()) {
                case "en" -> new String[] {
                        "Plan the day around the city's best-known concert hall, nearby dinner and an evening live performance.",
                        "Visit music-related streets or record shops, then choose a jazz, classical or indie live venue.",
                        "Keep daytime sightseeing light and reserve energy for a headline concert, opera house or acoustic night.",
                        "Explore cultural neighborhoods with small stages, late cafes and local musicians.",
                        "Add a relaxed morning, venue tour or music museum, then an evening show.",
                        "Shop for music souvenirs and plan a second live-music stop.",
                        "Close with brunch near a favorite venue and a smooth transfer."
                };
                case "ru" -> new String[] {
                        "Постройте день вокруг известного концертного зала города, ужина рядом и вечернего выступления.",
                        "Посетите музыкальные улицы или магазины пластинок, затем выберите джазовую, классическую или indie-сцену.",
                        "Днем оставьте легкие достопримечательности, а вечер посвятите концерту, опере или акустическому вечеру.",
                        "Исследуйте культурные районы с камерными сценами, поздними кафе и местными музыкантами.",
                        "Добавьте спокойное утро, тур по площадке или музыкальный музей, затем вечернее шоу.",
                        "Купите музыкальные сувениры и запланируйте вторую live-music остановку.",
                        "Завершите бранчем рядом с любимой площадкой и спокойным трансфером."
                };
                default -> new String[] {
                        "Günü şəhərin məşhur konsert zalı, yaxınlıqda axşam yeməyi və canlı performans ətrafında qurun.",
                        "Musiqi ilə bağlı küçələrə və ya vinil mağazalarına baş çəkin, sonra caz, klassik və ya indie canlı səhnə seçin.",
                        "Gündüz proqramını yüngül saxlayın və axşamı əsas konsert, opera zalı və ya akustik gecəyə ayırın.",
                        "Kiçik səhnələr, gec kafelər və yerli musiqiçilərlə tanınan mədəni məhəllələri kəşf edin.",
                        "Sakit səhər, konsert məkanı turu və ya musiqi muzeyi əlavə edin, sonra axşam şousuna gedin.",
                        "Musiqi suvenirləri alın və ikinci canlı musiqi dayanacağı planlayın.",
                        "Səyahəti sevimli səhnəyə yaxın brunch və rahat transferlə tamamlayın."
                };
            };
        }

        return defaultPool;
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
        String[] dayPool = customizeDayPool(isIstanbul ? istanbulDays : genericDays, context);
        String tourismStyleNote = tourismStyleNote(context);

        StringBuilder itinerary = new StringBuilder();
        itinerary.append("TripGen AI Plan").append("\n");
        itinerary.append("İstiqamət: ").append(destination).append("\n");
        itinerary.append("Büdcə tipi: ").append(context.getBudgetType()).append("\n");
        appendTourismStyleMeta(itinerary, context);
        itinerary.append("Mənbə: Gemini fallback generator").append("\n\n");

        for (int i = 0; i < context.getDays(); i++) {
            itinerary.append(i + 1)
                    .append("-ci gün: ")
                    .append(dayPool[i])
                    .append(tourismStyleNote)
                    .append("\n");
        }

        appendHiddenGemsBlock(itinerary, context);

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
        String tourismStyleNote = tourismStyleNote(context);
        String[] dayPool = {
                "Hotel check-in, the historic city center, landmark walk and a relaxed local dinner.",
                "Breakfast at a local cafe, museum visit, scenic neighborhood walk and an evening viewpoint.",
                "Market route, seaside or park break, shopping stop and a comfortable transfer plan.",
                "Hidden streets, boutique shops, local food tasting and a calm cultural evening.",
                "Short day trip near the city, flexible rest time and a light dinner after return.",
                "Souvenir shopping, dessert stop, public transport route and final photo locations.",
                "Slow brunch, favorite places revisit, packing time and airport transfer."
        };
        dayPool = customizeDayPool(dayPool, context);

        StringBuilder itinerary = new StringBuilder();
        itinerary.append("TripGen AI Plan").append("\n");
        itinerary.append("Destination: ").append(destination).append("\n");
        itinerary.append("Budget type: ").append(context.getBudgetType()).append("\n");
        appendTourismStyleMeta(itinerary, context);
        itinerary.append("Source: Gemini fallback generator").append("\n");
        itinerary.append(context.getLanguageInstruction()).append("\n\n");

        for (int i = 0; i < context.getDays(); i++) {
            itinerary.append("Day ")
                    .append(i + 1)
                    .append(": ")
                    .append(dayPool[i])
                    .append(tourismStyleNote)
                    .append("\n");
        }

        appendHiddenGemsBlock(itinerary, context);

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
        String tourismStyleNote = tourismStyleNote(context);
        String[] dayPool = {
                "Заселение в отель, исторический центр, прогулка по главным достопримечательностям и спокойный ужин в местном ресторане.",
                "Завтрак в местном кафе, музей, прогулка по атмосферному району и вечерняя смотровая площадка.",
                "Маршрут по рынку, отдых у воды или в парке, покупки и удобный план трансфера.",
                "Скрытые улочки, бутик-магазины, дегустация местной кухни и спокойная культурная программа вечером.",
                "Короткая поездка за город, свободное время для отдыха и легкий ужин после возвращения.",
                "Покупка сувениров, остановка на десерт, маршрут на общественном транспорте и финальные фото-точки.",
                "Поздний завтрак, повтор любимых мест, время на сборы и трансфер в аэропорт."
        };

        dayPool = customizeDayPool(dayPool, context);

        StringBuilder itinerary = new StringBuilder();
        itinerary.append("План TripGen AI").append("\n");
        itinerary.append("Направление: ").append(destination).append("\n");
        itinerary.append("Тип бюджета: ").append(context.getBudgetType()).append("\n");
        appendTourismStyleMeta(itinerary, context);
        itinerary.append("Источник: резервный генератор Gemini").append("\n");
        itinerary.append(context.getLanguageInstruction()).append("\n\n");

        for (int i = 0; i < context.getDays(); i++) {
            itinerary.append("День ")
                    .append(i + 1)
                    .append(": ")
                    .append(dayPool[i])
                    .append(tourismStyleNote)
                    .append("\n");
        }

        appendHiddenGemsBlock(itinerary, context);

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
