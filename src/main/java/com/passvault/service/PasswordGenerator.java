package com.passvault.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+?";

    private final SecureRandom random = new SecureRandom();

    public String generate(int length, boolean includeSpecial) {
        List<String> groups = new ArrayList<>(List.of(UPPER, LOWER, DIGITS));
        if (includeSpecial) {
            groups.add(SPECIAL);
        }
        String all = String.join("", groups);

        List<Character> chars = new ArrayList<>();
        for (String group : groups) {
            chars.add(pick(group));
        }
        while (chars.size() < length) {
            chars.add(pick(all));
        }
        Collections.shuffle(chars, random);

        StringBuilder sb = new StringBuilder(length);
        chars.forEach(sb::append);
        return sb.toString();
    }

    private char pick(String source) {
        return source.charAt(random.nextInt(source.length()));
    }
}
