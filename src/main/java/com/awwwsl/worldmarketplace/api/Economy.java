package com.awwwsl.worldmarketplace.api;

import com.awwwsl.worldmarketplace.ModNetwork;
import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = WorldmarketplaceMod.MOD_ID)
public class Economy {
    public static BigDecimal getBalance(Player player) {
        var compound = player.getPersistentData().getCompound("worldmarketplace");
        var balance = compound.getString("balance");
        if(balance.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            try {
                return new BigDecimal(balance);
            } catch (NumberFormatException e) {
                WorldmarketplaceMod.LOGGER.error("Error parsing economy balance, resetting to zero", e);
                setBalance(player, BigDecimal.ZERO);
                return BigDecimal.ZERO;
            }
        }
    }

    public static void setBalance(Player player, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
        var compound = player.getPersistentData().getCompound("worldmarketplace");
        compound.putString("balance", amount.toString());
        player.getPersistentData().put("worldmarketplace", compound);
        sync(player);
    }

    public static boolean buy(Player player, MarketItem item, int amount) {
        var eco = getBalance(player);
        var price = item.basePrice.multiply(new BigDecimal(amount));
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative: " + price);
        }
        if(eco.compareTo(price) < 0) {
            return false;
        }
        setBalance(player, eco.subtract(price));
        return true;
    }

    public static BigDecimal getPrice(MarketItem item, int amount) {
        return item.basePrice.multiply(new BigDecimal(amount));
    }

    public static void sell(Player player, MarketItem item, int amount) {
        var eco = getBalance(player);
        var price = item.basePrice.multiply(BigDecimal.valueOf(amount));
        // TODO: this is biz so item price should be integral calculated
        setBalance(player, eco.add(price));
    }

    public static boolean withdraw(Player player, BigDecimal amount, boolean simulate) {
        var balance  = getBalance(player);
        if(balance.compareTo(amount) < 0) {
            return false;
        }
        if(!simulate) {
            setBalance(player, balance.subtract(amount));
        }
        return true;
    }

    private static void sync(@NotNull Player player) {
        if(player instanceof ServerPlayer serverPlayer) {
            sync(serverPlayer);
        }
    }
    private static void sync(@NotNull ServerPlayer serverPlayer) {
        var packet = new Packet(getBalance(serverPlayer));
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
            packet);
    }

    public static class Packet {
        @NotNull
        private final BigDecimal balance;

        public Packet(@NotNull BigDecimal balance) {
            this.balance = balance;
        }

        public Packet(@NotNull FriendlyByteBuf buf) {
            CompoundTag data = buf.readNbt();
            if (data == null) throw new IllegalArgumentException("Data cannot be null");
            this.balance = new BigDecimal(data.getString("balance"));
        }

        public void encode(@NotNull FriendlyByteBuf buf) {
            CompoundTag data = new CompoundTag();
            data.putString("balance", balance.toString());
            buf.writeNbt(data);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                // 这里不要直接处理客户端逻辑
                if (ctx.get().getDirection().getReceptionSide().isClient()) {
                    // 只调用客户端处理器
                    com.awwwsl.worldmarketplace.client.ClientEconomyPacketHandler.handle(balance);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        sync(serverPlayer);
    }
}
