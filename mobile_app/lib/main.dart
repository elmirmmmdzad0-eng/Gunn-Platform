import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import 'screens/home_screen.dart';

void main() {
  runApp(const GunnApp());
}

class GunnApp extends StatelessWidget {
  const GunnApp({super.key});

  static const Color _background = Color(0xFF050505);
  static const Color _platinum = Color(0xFFF4F4F0);
  static const Color _silver = Color(0xFFD9D9D9);

  @override
  Widget build(BuildContext context) {
    final baseTheme = ThemeData.dark(useMaterial3: true);
    final soraTextTheme = GoogleFonts.soraTextTheme(baseTheme.textTheme);

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'GUNN',
      theme: baseTheme.copyWith(
        scaffoldBackgroundColor: _background,
        colorScheme: const ColorScheme.dark(
          primary: _platinum,
          secondary: _silver,
          surface: Color(0xFF111111),
          error: Color(0xFFF2A7A7),
        ),
        textTheme: soraTextTheme.apply(
          bodyColor: _silver,
          displayColor: _platinum,
        ),
        appBarTheme: AppBarTheme(
          backgroundColor: _background,
          elevation: 0,
          centerTitle: false,
          titleTextStyle: GoogleFonts.sora(
            color: _platinum,
            fontSize: 18,
            fontWeight: FontWeight.w700,
          ),
        ),
        inputDecorationTheme: InputDecorationTheme(
          border: InputBorder.none,
          hintStyle: GoogleFonts.sora(
            color: _silver.withOpacity(0.48),
            fontSize: 15,
          ),
        ),
      ),
      home: const HomeScreen(),
    );
  }
}
