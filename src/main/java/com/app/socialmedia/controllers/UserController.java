package com.app.socialmedia.controllers;

import com.app.socialmedia.dtos.user.LoginDto;
import com.app.socialmedia.dtos.user.RegisterDto;
import com.app.socialmedia.dtos.user.UpdateUsernameDto;
import com.app.socialmedia.models.User;
import com.app.socialmedia.services.AuthenticationService;
import com.app.socialmedia.services.JwtService;
import com.app.socialmedia.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
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

    @GetMapping("auth/authenticate")
    public ResponseEntity<User> authenticate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("auth/signup")
    public ResponseEntity<User> register(@RequestBody RegisterDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        if (registeredUser == null) {
            return ResponseEntity.badRequest().body(null);
        }

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

    @PutMapping("/users/profile/update")
    public ResponseEntity<User> updateProfile(
            @RequestParam MultipartFile image,
            @RequestParam String changeableName,
            @RequestParam String bio,
            @RequestParam String country
    ) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser == null) {
            return ResponseEntity.badRequest().body(null);
        }

        if (!image.isEmpty()) {
            if (!Objects.equals(image.getContentType(), "image/jpeg") || image.getSize() > 100000) {

                return ResponseEntity.badRequest().build();
            }
            else {
                userService.updatePfp(currentUser.getId(), image);
            }
        }

        if (!Objects.equals(changeableName, currentUser.getChangeableName())) {
            userService.updateUsername(currentUser, changeableName);
        }
        if (!Objects.equals(bio, currentUser.getBio())) {
            userService.updateBio(currentUser, bio);
        }
        if (!Objects.equals(country, currentUser.getCountry())) {
            userService.updateCountry(currentUser, country);
        }

        return ResponseEntity.ok(userService.findUserById(currentUser.getId()));
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

    @GetMapping("users/{name}")
    public ResponseEntity<Optional<User>> getUser(@PathVariable String name) {
        Optional<User> user = userService.findUser(name);

        return ResponseEntity.ok(user);
    }
}
