import 'dart:convert';

import 'package:http/http.dart' as http;

class ApiService {
  ApiService({
    http.Client? client,
    String? baseUrl,
  })  : _client = client ?? http.Client(),
        _baseUrl = baseUrl ?? 'https://tripgen-platform.onrender.com/api';

  final http.Client _client;
  final String _baseUrl;

  Future<TripResult> analyzeDestination({
    required String destination,
    required int days,
    List<String> selectedTypes = const [],
    String budgetType = 'AZN',
    String lang = 'az',
  }) async {
    final tripPayload = <String, Object?>{
      'destination': destination,
      'days': days,
      'budgetType': budgetType,
      'selectedTypes': selectedTypes.join(', '),
      'lang': lang,
    };

    Map<String, dynamic>? tripJson;
    Map<String, dynamic>? destinationJson;
    Object? firstError;

    try {
      tripJson = await _postJson(
        '$_baseUrl/trips/generate',
        tripPayload,
      );
    } catch (error) {
      firstError ??= error;
    }

    try {
      destinationJson = await _postJson(
        '$_baseUrl/destinations/analyze',
        <String, Object?>{
          'destination': destination,
          'city': destination,
          'days': days,
          'currency': budgetType,
          'selectedTypes': selectedTypes.join(', '),
        },
      );
    } catch (error) {
      firstError ??= error;
      try {
        destinationJson = await _getJson(
          Uri.parse('$_baseUrl/destinations/analyze').replace(
            queryParameters: <String, String>{
              'city': destination,
              'status': selectedTypes.isEmpty ? 'Premium city break' : selectedTypes.join(', '),
              'currency': budgetType,
              'selectedTypes': selectedTypes.join(', '),
            },
          ),
        );
      } catch (fallbackError) {
        firstError ??= fallbackError;
      }
    }

    if (tripJson == null && destinationJson == null) {
      throw firstError ?? const ApiException('GUNN is temporarily unavailable.');
    }

    return TripResult.fromJson(
      tripJson: tripJson ?? <String, dynamic>{},
      destinationJson: destinationJson ?? <String, dynamic>{},
      fallbackDestination: destination,
      selectedTypes: selectedTypes,
      budgetType: budgetType,
    );
  }

  Future<void> joinWaitlist(String email) async {
    await _postJson(
      '$_baseUrl/waitlist/join',
      <String, String>{'email': email.trim().toLowerCase()},
    );
  }

  Future<Map<String, dynamic>> _postJson(String url, Object body) async {
    final response = await _client.post(
      Uri.parse(url),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(body),
    );
    return _decodeResponse(response);
  }

  Future<Map<String, dynamic>> _getJson(Uri uri) async {
    final response = await _client.get(uri);
    return _decodeResponse(response);
  }

  Map<String, dynamic> _decodeResponse(http.Response response) {
    final rawBody = response.body.trim();
    final Object? decoded = rawBody.isEmpty ? <String, dynamic>{} : jsonDecode(rawBody);
    final data = decoded is Map<String, dynamic> ? decoded : <String, dynamic>{'data': decoded};

    if (response.statusCode < 200 || response.statusCode >= 300) {
      final message = data['message']?.toString() ?? 'Request failed with HTTP ${response.statusCode}.';
      throw ApiException(message, statusCode: response.statusCode);
    }

    return data;
  }
}

class ApiException implements Exception {
  const ApiException(this.message, {this.statusCode});

  final String message;
  final int? statusCode;

  @override
  String toString() => message;
}

class TripResult {
  const TripResult({
    required this.destination,
    required this.days,
    required this.selectedTypes,
    required this.budgetType,
    required this.itineraryRaw,
    required this.hotel,
    required this.visa,
    required this.ticket,
    required this.hacks,
    required this.packingList,
    required this.imageUrls,
    required this.googleMapsUrl,
    required this.googleEarthUrl,
  });

  final String destination;
  final int days;
  final List<String> selectedTypes;
  final String budgetType;
  final String itineraryRaw;
  final String hotel;
  final String visa;
  final String ticket;
  final List<String> hacks;
  final List<String> packingList;
  final List<String> imageUrls;
  final Uri googleMapsUrl;
  final Uri googleEarthUrl;

  factory TripResult.fromJson({
    required Map<String, dynamic> tripJson,
    required Map<String, dynamic> destinationJson,
    required String fallbackDestination,
    required List<String> selectedTypes,
    required String budgetType,
  }) {
    final destination = tripJson['destination']?.toString().trim().isNotEmpty == true
        ? tripJson['destination'].toString()
        : fallbackDestination;

    final imageUrls = (tripJson['imageUrls'] is List)
        ? (tripJson['imageUrls'] as List).map((item) => item.toString()).where((url) => url.isNotEmpty).toList()
        : <String>[];

    return TripResult(
      destination: destination,
      days: _asInt(tripJson['days'], fallback: 3),
      selectedTypes: selectedTypes,
      budgetType: budgetType,
      itineraryRaw: _sanitizeItinerary(tripJson['itineraryRaw']?.toString() ?? ''),
      hotel: destinationJson['hotel']?.toString() ?? '',
      visa: destinationJson['visa']?.toString() ?? '',
      ticket: destinationJson['ticket']?.toString() ?? '',
      hacks: _splitList(destinationJson['hacks']?.toString()),
      packingList: _splitList(destinationJson['packingList']?.toString()),
      imageUrls: imageUrls,
      googleMapsUrl: Uri.https('www.google.com', '/maps/search/', {'api': '1', 'query': destination}),
      googleEarthUrl: Uri.parse('https://earth.google.com/web/search/${Uri.encodeComponent(destination)}'),
    );
  }

  List<DayCluster> get dayClusters {
    final routeText = _routeTextOnly(itineraryRaw);
    final lines = routeText
        .split(RegExp(r'\n+'))
        .map((line) => line.trim())
        .where((line) => line.isNotEmpty)
        .toList();

    final clusters = <DayCluster>[];
    DayCluster? active;

    for (final line in lines) {
      final match = RegExp(r'^(?:day|gün|gun|день)?\s*(\d+)\s*(?:-?ci|-?cı|-?cu|-?cü|st|nd|rd|th)?\s*(?:gün|gun|day|день)?\s*[:.)-]?\s*(.*)$', caseSensitive: false)
          .firstMatch(line);
      if (match != null) {
        active = DayCluster(
          number: int.tryParse(match.group(1) ?? '') ?? clusters.length + 1,
          text: _cleanClusterLine(match.group(2) ?? ''),
        );
        clusters.add(active);
      } else if (active != null) {
        active.text = '${active.text} ${_cleanClusterLine(line)}'.trim();
      }
    }

    if (clusters.isNotEmpty) {
      return clusters.where((cluster) => cluster.text.isNotEmpty).toList();
    }

    return lines.take(7).toList().asMap().entries.map((entry) {
      return DayCluster(number: entry.key + 1, text: _cleanClusterLine(entry.value));
    }).toList();
  }

  List<String> get hiddenGems {
    final match = RegExp(r'(?:^|\n)HIDDEN_GEMS\s*:\s*', caseSensitive: false).firstMatch(itineraryRaw);
    if (match == null) return const [];
    return itineraryRaw
        .substring(match.end)
        .split(RegExp(r'\n+'))
        .map(_cleanClusterLine)
        .where((line) => line.isNotEmpty)
        .take(5)
        .toList();
  }

  static int _asInt(Object? value, {required int fallback}) {
    if (value is int) return value;
    return int.tryParse(value?.toString() ?? '') ?? fallback;
  }

  static List<String> _splitList(String? value) {
    return (value ?? '')
        .replaceAll('*', '')
        .split(RegExp(r'\n|;|,|•|- '))
        .map((item) => item.trim())
        .where((item) => item.isNotEmpty && !item.toLowerCase().contains('məlumat tap'))
        .toList();
  }

  static String _sanitizeItinerary(String value) {
    return value
        .replaceAll('\r', '')
        .split('\n')
        .map((line) => line.trim())
        .where((line) {
          if (line.isEmpty) return false;
          if (RegExp(r'^IMAGE_KEYWORDS\s*:', caseSensitive: false).hasMatch(line)) return false;
          if (RegExp(r'^(source|mənbə|qeyd|note|примечание)\s*:', caseSensitive: false).hasMatch(line) &&
              RegExp(r'(fallback|mock|generator|gemini|static|simulyator|simulator|резерв)', caseSensitive: false).hasMatch(line)) {
            return false;
          }
          return !RegExp(r'(gemini\s+fallback|fallback/mock|mock\s+generator|static\s+search\s+fallback|fallback.*plan)', caseSensitive: false).hasMatch(line);
        })
        .join('\n')
        .trim();
  }

  static String _routeTextOnly(String value) {
    final match = RegExp(r'(?:^|\n)HIDDEN_GEMS\s*:\s*', caseSensitive: false).firstMatch(value);
    return match == null ? value : value.substring(0, match.start).trim();
  }

  static String _cleanClusterLine(String value) {
    return value
        .replaceFirst(RegExp(r'^HIDDEN_GEMS\s*:\s*', caseSensitive: false), '')
        .replaceFirst(RegExp(r'^(?:day|gün|gun|день)\s*\d+\s*[:.)-]?\s*', caseSensitive: false), '')
        .replaceFirst(RegExp(r'^\d+\s*[.)-]\s*'), '')
        .replaceFirst(RegExp(r'^\d+\s*(?:-?ci|-?cı|-?cu|-?cü|st|nd|rd|th)?\s*(?:gün|gun|day|день)\s*[:.)-]?\s*', caseSensitive: false), '')
        .trim();
  }
}

class DayCluster {
  DayCluster({
    required this.number,
    required this.text,
  });

  final int number;
  String text;
}
