import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../services/api_service.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  static const Color _background = Color(0xFF050505);
  static const Color _surface = Color(0xFF111111);
  static const Color _platinum = Color(0xFFF4F4F0);
  static const Color _silver = Color(0xFFD9D9D9);

  final ApiService _apiService = ApiService();
  final TextEditingController _destinationController = TextEditingController();
  final TextEditingController _waitlistController = TextEditingController();

  Future<TripResult>? _tripFuture;
  bool _joiningWaitlist = false;
  String? _waitlistMessage;

  final List<String> _selectedTypes = <String>['Premium city break'];

  @override
  void dispose() {
    _destinationController.dispose();
    _waitlistController.dispose();
    super.dispose();
  }

  void _generateTrip() {
    final destination = _destinationController.text.trim();
    if (destination.isEmpty) return;

    setState(() {
      _tripFuture = _apiService.analyzeDestination(
        destination: destination,
        days: 3,
        selectedTypes: _selectedTypes,
      );
    });
  }

  Future<void> _joinWaitlist() async {
    final email = _waitlistController.text.trim();
    if (email.isEmpty) return;

    setState(() {
      _joiningWaitlist = true;
      _waitlistMessage = null;
    });

    try {
      await _apiService.joinWaitlist(email);
      _waitlistController.clear();
      setState(() => _waitlistMessage = 'You are on the GUNN waitlist.');
    } catch (error) {
      setState(() => _waitlistMessage = 'Could not join waitlist. Try again soon.');
    } finally {
      if (mounted) {
        setState(() => _joiningWaitlist = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _background,
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 40),
          children: <Widget>[
            _Header(),
            const SizedBox(height: 40),
            Text(
              'GUNN',
              style: GoogleFonts.sora(
                color: _platinum,
                fontSize: 58,
                height: 0.95,
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 14),
            Text(
              'Navigate the Future of Travel',
              style: GoogleFonts.sora(
                color: _silver,
                fontSize: 17,
                height: 1.55,
                fontWeight: FontWeight.w300,
              ),
            ),
            const SizedBox(height: 28),
            GlassPanel(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    'Destination',
                    style: _labelStyle,
                  ),
                  TextField(
                    controller: _destinationController,
                    textInputAction: TextInputAction.search,
                    onSubmitted: (_) => _generateTrip(),
                    style: GoogleFonts.sora(
                      color: _platinum,
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                    ),
                    decoration: const InputDecoration(
                      hintText: 'Paris, Rome, Tokyo...',
                    ),
                  ),
                  const SizedBox(height: 20),
                  SizedBox(
                    width: double.infinity,
                    height: 56,
                    child: FilledButton(
                      onPressed: _generateTrip,
                      style: FilledButton.styleFrom(
                        backgroundColor: _platinum,
                        foregroundColor: _background,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      child: Text(
                        'Generate',
                        style: GoogleFonts.sora(
                          fontSize: 15,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: <String>[
                'Premium city break',
                'Romantik turizm',
                'Qastronomik turizm',
                'Hidden gems',
              ].map(_styleChip).toList(),
            ),
            const SizedBox(height: 26),
            if (_tripFuture != null)
              FutureBuilder<TripResult>(
                future: _tripFuture,
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return const _LoadingPanel();
                  }
                  if (snapshot.hasError) {
                    return _ErrorPanel(message: snapshot.error.toString());
                  }
                  final result = snapshot.data;
                  if (result == null) return const SizedBox.shrink();
                  return _TripResults(result: result);
                },
              ),
            const SizedBox(height: 30),
            GlassPanel(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text('Private beta', style: _labelStyle),
                  const SizedBox(height: 8),
                  Text(
                    'Join the GUNN waitlist.',
                    style: GoogleFonts.sora(
                      color: _platinum,
                      fontSize: 22,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: _waitlistController,
                    keyboardType: TextInputType.emailAddress,
                    style: GoogleFonts.sora(color: _platinum),
                    decoration: const InputDecoration(hintText: 'you@example.com'),
                  ),
                  const SizedBox(height: 14),
                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: OutlinedButton(
                      onPressed: _joiningWaitlist ? null : _joinWaitlist,
                      style: OutlinedButton.styleFrom(
                        foregroundColor: _platinum,
                        side: BorderSide(color: _platinum.withOpacity(0.32)),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      child: Text(_joiningWaitlist ? 'Joining...' : 'Join waitlist'),
                    ),
                  ),
                  if (_waitlistMessage != null) ...<Widget>[
                    const SizedBox(height: 12),
                    Text(
                      _waitlistMessage!,
                      style: GoogleFonts.sora(color: _silver, fontSize: 13),
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _styleChip(String label) {
    final selected = _selectedTypes.contains(label);
    return ChoiceChip(
      selected: selected,
      label: Text(label),
      labelStyle: GoogleFonts.sora(
        color: selected ? _background : _silver,
        fontSize: 12,
        fontWeight: FontWeight.w700,
      ),
      selectedColor: _platinum,
      backgroundColor: _surface.withOpacity(0.68),
      side: BorderSide(color: _platinum.withOpacity(selected ? 0 : 0.14)),
      onSelected: (value) {
        setState(() {
          if (value) {
            _selectedTypes.add(label);
          } else {
            _selectedTypes.remove(label);
          }
        });
      },
    );
  }

  TextStyle get _labelStyle => GoogleFonts.sora(
        color: _silver.withOpacity(0.72),
        fontSize: 11,
        fontWeight: FontWeight.w800,
      );
}

class _Header extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Row(
      children: <Widget>[
        Container(
          width: 36,
          height: 36,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: Colors.white.withOpacity(0.14)),
            color: Colors.white.withOpacity(0.055),
          ),
          child: const Icon(Icons.explore, size: 18, color: Color(0xFFF4F4F0)),
        ),
        const SizedBox(width: 12),
        Text(
          'GUNN',
          style: GoogleFonts.sora(
            color: const Color(0xFFF4F4F0),
            fontSize: 13,
            fontWeight: FontWeight.w800,
          ),
        ),
      ],
    );
  }
}

class _TripResults extends StatelessWidget {
  const _TripResults({required this.result});

  final TripResult result;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        GlassPanel(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              _ResultRow(label: 'Destination', value: result.destination),
              _ResultRow(label: 'Budget', value: result.budgetType),
              _ResultRow(
                label: 'Styles',
                value: result.selectedTypes.isEmpty ? 'Balanced premium route' : result.selectedTypes.join(', '),
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        ...result.dayClusters.map((cluster) {
          return Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: GlassPanel(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  _ClusterTitle(icon: Icons.route, title: 'Day ${cluster.number}'),
                  const SizedBox(height: 10),
                  Text(cluster.text, style: _bodyStyle),
                ],
              ),
            ),
          );
        }),
        GlassPanel(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              const _ClusterTitle(icon: Icons.public, title: 'Route map'),
              const SizedBox(height: 10),
              Text(
                result.googleMapsUrl.toString(),
                style: _bodyStyle,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 8),
              Text(
                result.googleEarthUrl.toString(),
                style: _bodyStyle,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
        if (result.hiddenGems.isNotEmpty) ...<Widget>[
          const SizedBox(height: 14),
          GlassPanel(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                const _ClusterTitle(icon: Icons.diamond, title: 'Hidden Gems'),
                const SizedBox(height: 10),
                ...result.hiddenGems.map((gem) {
                  return Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: Text(gem, style: _bodyStyle),
                  );
                }),
              ],
            ),
          ),
        ],
      ],
    );
  }

  static TextStyle get _bodyStyle => GoogleFonts.sora(
        color: const Color(0xFFD9D9D9),
        height: 1.65,
        fontSize: 14,
      );
}

class _ResultRow extends StatelessWidget {
  const _ResultRow({
    required this.label,
    required this.value,
  });

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          SizedBox(
            width: 90,
            child: Text(
              label,
              style: GoogleFonts.sora(
                color: const Color(0xFF9D9D9D),
                fontSize: 11,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: GoogleFonts.sora(
                color: const Color(0xFFF4F4F0),
                fontSize: 14,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ClusterTitle extends StatelessWidget {
  const _ClusterTitle({
    required this.icon,
    required this.title,
  });

  final IconData icon;
  final String title;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: <Widget>[
        Icon(icon, color: const Color(0xFFF4F4F0), size: 18),
        const SizedBox(width: 10),
        Text(
          title,
          style: GoogleFonts.sora(
            color: const Color(0xFFF4F4F0),
            fontSize: 16,
            fontWeight: FontWeight.w700,
          ),
        ),
      ],
    );
  }
}

class _LoadingPanel extends StatelessWidget {
  const _LoadingPanel();

  @override
  Widget build(BuildContext context) {
    return const GlassPanel(
      child: Center(
        child: Padding(
          padding: EdgeInsets.all(18),
          child: CircularProgressIndicator(color: Color(0xFFF4F4F0)),
        ),
      ),
    );
  }
}

class _ErrorPanel extends StatelessWidget {
  const _ErrorPanel({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return GlassPanel(
      child: Text(
        message,
        style: GoogleFonts.sora(
          color: const Color(0xFFF2A7A7),
          height: 1.5,
        ),
      ),
    );
  }
}

class GlassPanel extends StatelessWidget {
  const GlassPanel({
    required this.child,
    super.key,
  });

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(14),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 22, sigmaY: 22),
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: Colors.white.withOpacity(0.12)),
            color: Colors.white.withOpacity(0.055),
            boxShadow: <BoxShadow>[
              BoxShadow(
                color: Colors.black.withOpacity(0.35),
                blurRadius: 42,
                offset: const Offset(0, 24),
              ),
            ],
          ),
          child: child,
        ),
      ),
    );
  }
}
