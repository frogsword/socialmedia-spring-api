package com.app.socialmedia.services;

import com.app.socialmedia.dtos.user.LoginDto;
import com.app.socialmedia.dtos.user.RegisterDto;
import com.app.socialmedia.models.User;
import com.app.socialmedia.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterDto input) {

        if (!Objects.equals(input.getPassword(), input.getConfirmPassword())) {
            return null;
        }

        String changeableName = input.getName();
        
        //make sure 'name' is unique
        //add numbers to end of name until name is unique
        boolean isUnique = false;
        Random rand = new Random();
        while (!isUnique) {
            Optional<User> user = userRepository.findByName(input.getName());
            if (user.isEmpty()) {
                isUnique = true;
            }
            else {
                input.setName(input.getName() + rand.nextInt(100));
            }
        }

        //check for unique email
        Optional<User> emailDuplicate = userRepository.findByEmail(input.getEmail());
        if (emailDuplicate.isPresent()) {
            return null;
        }

        List<String> arr = new ArrayList<>();
        Date now = new Date();

        User user = new User()
                .setName(input.getName())
                .setEmail(input.getEmail())
                .setPassword(passwordEncoder.encode(input.getPassword()));

        user.setBio("No Bio :(");
        user.setChangeableName(changeableName);
        user.setCountry("N/A");
        user.setProfilePicture(new byte[0]);
        user.setLikedTweets(arr);
        user.setFollowing(arr);
        user.setFollowers(arr);
        user.setRegistrationDate(now);

        return userRepository.save(user);
    }

    public User authenticate(LoginDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return userRepository.findByEmail(input.getEmail())
                .orElseThrow();
    }
}
