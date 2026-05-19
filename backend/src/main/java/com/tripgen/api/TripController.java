package com.tripgen.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    private final TripService tripService;
    private final UserService userService;

    public TripController(TripService tripService, UserService userService) {
        this.tripService = tripService;
        this.userService = userService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateTrip(@RequestBody TripRequest request) {
        boolean authenticated = request != null
                && userService.isActiveSession(request.getUserId(), request.getAuthToken());

        if (!authenticated) {
            System.out.println("[TRIP_GENERATE_GUEST] Guest generation enabled for open tourism-style planner.");
        }

        return ResponseEntity.ok(tripService.generateTrip(request));
    }
}
