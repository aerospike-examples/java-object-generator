package com.aerospike.generator.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StringUtils {
    /**
     * Returns if the word in the @{code listOfWords} at the passed index is contained in the @{code wordsToMatch} set.
     * Note that the index can be negative, so -1 would be the last word in the sequence </p>
     * For example, 
     * @param index
     * @param listOfWords
     * @param wordsToMatch
     * @return
     */
    public static boolean isWordOneOf(int index, List<String> listOfWords, Set<String> wordsToMatch) {
        if (index < 0) {
            index = index + listOfWords.size();
        }
        index %= listOfWords.size();
        String refWord = listOfWords.get(index);
        return wordsToMatch.contains(refWord);
    }

    public static boolean isLastWordOneOf(List<String> listOfWords, Set<String> wordsToMatch) {
        return isWordOneOf(-1, listOfWords, wordsToMatch);
    }

    public static boolean isFirstOrLastWordOneOf(List<String> listOfWords, Set<String> wordsToMatch) {
        if (listOfWords.size() == 1) {
            return isFirstWordOneOf(listOfWords, wordsToMatch);
        }
        return isWordOneOf(-1, listOfWords, wordsToMatch) ||
                isWordOneOf(0, listOfWords, wordsToMatch);
    }

    public static boolean isFirstWordOneOf(List<String> listOfWords, Set<String> wordsToMatch) {
        return isWordOneOf(0, listOfWords, wordsToMatch);
    }

    public static boolean areLastTwoWordsOneOf(List<String> listOfWords, 
            Set<String> secondLastWordsToMatch, Set<String> lastWordsToMatch) {
        return listOfWords.size() >= 2 && 
                isWordOneOf(-2, listOfWords, secondLastWordsToMatch) &&
                isWordOneOf(-1, listOfWords, lastWordsToMatch);
    }

    /**
     * return the passes string with the first letter converted to upper case.
     * @param s
     * @return
     */
    public static String capitalise(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    @SafeVarargs
    public static boolean matches(List<String> words, Set<String> ... wordSets) {
        if (words.size() != wordSets.length) {
            return false;
        }
        for (int i = 0; i < words.size(); i++) {
            if (wordSets[i] == null) {
                // Null is a "match anything" prefix
                continue;
            }
            if (!isWordOneOf(i, words, wordSets[i])) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Takes a valid Java identifier like "firstName", "some_long_set_of_words" and returns a list of strings with
     * separate words, all in lower cases. Words can be separated by _ or capitalization. So "firstName" would 
     * become ["first", "name"], and "some_long_set_of_words" would be ["some", "long", "set", "of", "words"]
     * @param word
     * @return
     */
    public static List<String> breakIntoWords(String word) {
        List<String> words = new ArrayList<>();
        return breakIntoWordsByUnderscore(word, words);
    }
    

    private static List<String> breakIntoWordsByUnderscore(String word, List<String> currentWordList) {
        // If there are underscores in the name like "first_name", then separate by underscore
        String[] words = word.split("_");
        for (String thisWord : words) {
            // Make sure the word is valid, eg __address__ should just become address
            if (thisWord.length() > 0) {
                breakIntoWordsByCase(thisWord, currentWordList);
            }
        }
        return currentWordList;
    }
    

    private static List<String> breakIntoWordsByCase(String part, List<String> currentWordList) {
        // Want to handle a couple of different cases:
        // bankOfMars => [bank, of, mars]
        // HECSDebt => [hecs, debt]
        StringBuilder currentWord = new StringBuilder();

        int i = 0;
        while (i < part.length()) {
            char c = part.charAt(i);

            if (Character.isUpperCase(c)) {
                int start = i;

                // Count the run of uppercase letters
                while (i < part.length() && Character.isUpperCase(part.charAt(i))) {
                    i++;
                }

                int capLen = i - start;

                // If the run is at the end or followed by uppercase/lowercase
                if (i == part.length()) {
                    // The whole rest is uppercase: treat as one word
                    if (currentWord.length() > 0) {
                        currentWordList.add(currentWord.toString().toLowerCase());
                        currentWord.setLength(0);
                    }
                    currentWordList.add(part.substring(start).toLowerCase());
                } else if (capLen > 1 && Character.isLowerCase(part.charAt(i))) {
                    // More than one capital letter, followed by lowercase
                    if (currentWord.length() > 0) {
                        currentWordList.add(currentWord.toString().toLowerCase());
                        currentWord.setLength(0);
                    }
                    currentWordList.add(part.substring(start, i - 1).toLowerCase());
                    currentWord.append(part.charAt(i - 1));
                } else {
                    // Single capital letter or capital followed by capital
                    if (currentWord.length() > 0) {
                        currentWordList.add(currentWord.toString().toLowerCase());
                        currentWord.setLength(0);
                    }
                    currentWord.append(c);
                    i = start + 1;
                }
            } else {
                currentWord.append(c);
                i++;
            }
        }

        if (currentWord.length() > 0) {
            currentWordList.add(currentWord.toString().toLowerCase());
        }
        return currentWordList;
    }
    
    public static String makePossessive(String word) {
        if (word.endsWith("s")) {
            return word + "'";
        }
        else {
            return word + "'s";
        }
    }

    public static void main(String[] args) throws Exception {
        String[] testInputs = {
            "home_addr",          // ["home", "addr"]
            "ALLCAPS",
            "__default__",        // ["default"]
            "homeAddress",        // ["home", "address"]
            "CustomerFullName",   // ["customer", "full", "name"]
            "customerHECSDebt"    // ["customer", "hecs", "debt"]
        };

        for (String input : testInputs) {
            System.out.println(input + " -> " + breakIntoWords(input));
        }
    }

}
