package com.awwwsl.worldmarketplace.api;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.NotImplementedException;

import java.math.BigDecimal;

public class Economy {
    public static BigDecimal getBalance(ServerPlayer player) {
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

    public static void setBalance(ServerPlayer player, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
        var compound = player.getPersistentData().getCompound("worldmarketplace");
        compound.putString("balance", amount.toString());
        player.getPersistentData().put("worldmarketplace", compound);
    }

    public static void buy(ServerPlayer player, MarketItem item, int amount) {
        throw new NotImplementedException();
    }

    public static void sell(ServerPlayer player, MarketItem item, int amount) {
        var eco = getBalance(player);
        var price = item.basePrice.multiply(BigDecimal.valueOf(amount));
        // TODO: this is biz so item price should be integral calculated
        setBalance(player, eco.add(price));
    }
}
