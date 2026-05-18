package com.tripgen.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
        if (request == null || !userService.isActiveSession(request.getUserId(), request.getAuthToken())) {
            System.out.println("[TRIP_GENERATE_BLOCKED] Guest or inactive user tried to use AI planner.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Bu özəlliyi işlətmək üçün qeydiyyatdan keçməlisiniz."));
        }

        return ResponseEntity.ok(tripService.generateTrip(request));
    }
}
