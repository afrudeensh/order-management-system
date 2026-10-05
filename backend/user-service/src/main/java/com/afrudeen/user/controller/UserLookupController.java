package com.afrudeen.user.controller;


import com.afrudeen.user.dto.response.UserSummary;
import com.afrudeen.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
public class UserLookupController {

    private final AuthService service;

    public UserLookupController(AuthService service) {
        this.service = service;
    }

    @GetMapping("/lookup")
    @Operation(summary = "Names and emails for a list of user ids (admin only)")
    public List<UserSummary> lookup(@RequestHeader("X-User-Role") String role,
                                    @RequestParam List<Long> ids) {
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Admin only");
        }
        return service.lookup(ids);
    }
}