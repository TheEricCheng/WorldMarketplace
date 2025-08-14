package com.awwwsl.worldmarketplace;

import net.minecraft.nbt.CompoundTag;

import java.text.MessageFormat;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalizedName {
    private final String id;
    private final Map<String, String> names; // langCode -> text
    private final String meaning;
    private final String lang;

    public LocalizedName(String id, Map<String, String> names, String meaning, String lang) {
        this.id = id;
        this.names = names;
        this.meaning = meaning;
        this.lang = lang;
    }

    public String get(String langCode) {
        return names.getOrDefault(langCode, names.getOrDefault("latin", id));
    }

    public String getMeaning() { return meaning; }
    public String getLang() { return lang; }
    public Map<String, String> getAllNames() { return names; }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("id", id);
        tag.putString("lang", lang);
        tag.putString("meaning", meaning);
        CompoundTag namesTag = new CompoundTag();
        for (Map.Entry<String, String> entry : names.entrySet()) {
            namesTag.putString(entry.getKey(), entry.getValue());
        }
        tag.put("names", namesTag);
        return tag;
    }

    public static LocalizedName load(CompoundTag tag) {
        String id = tag.getString("id");
        String lang = tag.getString("lang");
        String meaning = tag.getString("meaning");
        CompoundTag namesTag = tag.getCompound("names");

        Map<String, String> names = namesTag.getAllKeys().stream()
            .collect(Collectors.toMap(key -> key, namesTag::getString));

        return new LocalizedName(id, names, meaning, lang);
    }

    @Override
    public String toString() {
        return MessageFormat.format("LocalizedName'{'id=''{0}'', names={1}, meaning=''{2}'', lang=''{3}'''}'", id, names, meaning, lang);
    }
}
