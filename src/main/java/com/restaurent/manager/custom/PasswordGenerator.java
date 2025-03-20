package com.restaurent.manager.custom;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_+=<>?";

    public static String generateRandomPassword(int n) {
        if (n < 2) {
            throw new IllegalArgumentException("Password length must be at least 2 to include one uppercase and one special character.");
        }

        SecureRandom random = new SecureRandom();

        // List to store all characters in the password
        List<Character> passwordCharacters = new ArrayList<>();

        // Add one random uppercase letter
        passwordCharacters.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));

        // Add one random special character
        passwordCharacters.add(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

        // Fill the remaining length of the password with random characters from all categories
        String allCharacters = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS;
        for (int i = 2; i < n; i++) {
            passwordCharacters.add(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        // Shuffle the list to ensure randomness of the position of each character
        Collections.shuffle(passwordCharacters, random);

        // Convert the list to a string
        StringBuilder password = new StringBuilder();
        for (Character ch : passwordCharacters) {
            password.append(ch);
        }

        return password.toString();
    }
}
