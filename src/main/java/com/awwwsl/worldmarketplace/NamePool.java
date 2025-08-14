package com.awwwsl.worldmarketplace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class NamePool {
    private static final Gson GSON = new GsonBuilder().create();

    public static NamePool fromFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, NamePool.class);
        }
    }

    public static NamePool fromFile(String path) throws IOException {
        return fromFile(new File(path));
    }

    public static class NameEntry {
        private String id;
        private Map<String, String> names;
        private String meaning;
        private String lang;

        public LocalizedName toLocalizedName() {
            return new LocalizedName(id, names, meaning, lang);
        }

        public boolean hasLanguage(String langCode) {
            return names.containsKey(langCode);
        }
    }

    private List<NameEntry> prefixes;
    private List<NameEntry> suffixes;
    private Map<String, String> languageMapping;

    public List<NameEntry> getPrefixes() { return prefixes; }
    public List<NameEntry> getSuffixes() { return suffixes; }
    public Map<String, String> getLanguageMapping() { return languageMapping; }

    public LocalizedName randomName(ResourceLocation villageType) {
        var typeString = villageType.toString();
        return randomName(languageMapping.getOrDefault(typeString, "latin"));
    }
    public LocalizedName randomName(String languageCode) {
        Random r = WorldmarketplaceMod.RANDOM;

        List<NameEntry> filteredPrefixes = prefixes.stream()
            .filter(e -> e.hasLanguage(languageCode))
            .collect(Collectors.toList());

        List<NameEntry> filteredSuffixes = suffixes.stream()
            .filter(e -> e.hasLanguage(languageCode))
            .collect(Collectors.toList());

        if (filteredPrefixes.isEmpty()) filteredPrefixes = prefixes;
        if (filteredSuffixes.isEmpty()) filteredSuffixes = suffixes;

        NameEntry p = filteredPrefixes.get(r.nextInt(filteredPrefixes.size()));
        NameEntry s = filteredSuffixes.get(r.nextInt(filteredSuffixes.size()));

        String id = p.id + "_" + s.id;
        Map<String, String> combined = new HashMap<>();

        // 组合所有语言，拼接对应字符串
        Set<String> langs = new HashSet<>(p.names.keySet());
        langs.addAll(s.names.keySet());

        for (String lang : langs) {
            String pName = p.names.getOrDefault(lang, p.names.get("latin"));
            String sName = s.names.getOrDefault(lang, s.names.get("latin"));
            combined.put(lang, pName + sName);
        }

        String meaning = p.meaning + " " + s.meaning;
        String lang = p.lang + "+" + s.lang;

        return new LocalizedName(id, combined, meaning, lang);
    }
}
