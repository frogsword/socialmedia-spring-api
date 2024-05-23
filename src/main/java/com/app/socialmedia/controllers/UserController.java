package com.app.socialmedia.controllers;

import com.app.socialmedia.dtos.user.LoginDto;
import com.app.socialmedia.dtos.user.RegisterDto;
import com.app.socialmedia.dtos.user.UpdateUsernameDto;
import com.app.socialmedia.models.User;
import com.app.socialmedia.services.AuthenticationService;
import com.app.socialmedia.services.JwtService;
import com.app.socialmedia.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final UserService userService;

    public UserController(JwtService jwtService, AuthenticationService authenticationService, UserService userService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @PostMapping("auth/signup")
    public ResponseEntity<User> register(@RequestBody RegisterDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("auth/login")
    public ResponseEntity<String> authenticate(@RequestBody LoginDto loginUserDto, HttpServletResponse response) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwt = jwtService.generateToken(authenticatedUser);

        Cookie jwtCookie = new Cookie("JSON_WEB_TOKEN", jwt);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        //jwtCookie.setSecure(true);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(jwt);
    }

    @GetMapping("users/me")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(currentUser);
    }

    @PatchMapping("users/username/update")
    public ResponseEntity<User> updateUsername(@RequestBody UpdateUsernameDto updateUsernameDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        User user = userService.updateUsername(currentUser.getId(), updateUsernameDto.getName());
        return ResponseEntity.ok(user);
    }

    @PutMapping("users/{followingId}/follow")
    public ResponseEntity<Boolean> toggleFollow(@PathVariable String followingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        boolean successful;
        if (!currentUser.getId().equals(followingId)) {
            successful = userService.toggleFollow(currentUser.getId(), followingId);
        }
        else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(successful);
    }


    @GetMapping("users")
    public ResponseEntity<List<User>> allUsers() {
        List<User> users = userService.allUsers();

        return ResponseEntity.ok(users);
    }
}
