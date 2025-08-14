package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.client.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
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

    public CompoundTag save() {
        return save(new CompoundTag());
    }
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

    public static final class Packet {
        private final LocalizedName name;

        public Packet(@NotNull LocalizedName name) {
            this.name = name;
        }

        public Packet(@NotNull FriendlyByteBuf buf) {
            CompoundTag data = buf.readNbt();
            if (data == null) {
                throw new IllegalArgumentException("Received null NBT data for LocalizedName.Packet");
            }
            this.name = LocalizedName.load(data);
        }

        public void encode(@NotNull FriendlyByteBuf buf) {
            buf.writeNbt(this.name.save());
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                if (ctx.get().getDirection().getReceptionSide().isClient()) {
                    ClientPacketHandler.handleMarketNamePacket(name);
                }
            });
            ctx.get().setPacketHandled(true);
        }

        public @NotNull LocalizedName name() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Packet) obj;
            return Objects.equals(this.name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return MessageFormat.format("Packet[name={0}]", name);
        }
    }
}
