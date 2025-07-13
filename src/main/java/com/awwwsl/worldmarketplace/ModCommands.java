package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.api.Economy;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Mod.EventBusSubscriber
public class ModCommands {
    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext context = event.getBuildContext();
        register(dispatcher, context, event.getCommandSelection());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("eco")
                .then(eco_balance())
                .then(eco_set())
                .then(eco_decay()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> eco_set() {
        String amountArgumentName = "amount";
        return Commands.literal("set")
                .then(Commands.argument(amountArgumentName, DoubleArgumentType.doubleArg()) .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    double amount = DoubleArgumentType.getDouble(ctx, amountArgumentName);
                    if(amount < 0) {
                        source.sendFailure(Component.literal("Amount cannot be negative."));
                        return 0;
                    }
                    Economy.setBalance(ctx.getSource().getPlayer(), BigDecimal.valueOf(amount));
                    source.sendSuccess(() -> Component.literal("Current economy have been set to " + WorldmarketplaceMod.DECIMAL_FORMAT.format(amount)), true);
                    return 1;
                }));
    }
    private static LiteralArgumentBuilder<CommandSourceStack> eco_balance() {
        return Commands.literal("balance")
                .executes(ctx -> {
                    if(ctx.getSource().getPlayer() == null) {
                        ctx.getSource().sendFailure(Component.literal("This command can only be used by players."));
                        return 0;
                    }
                    CommandSourceStack source = ctx.getSource();
                    WorldmarketplaceMod.DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_EVEN);
                    source.sendSuccess(() -> Component.literal("Current economy balance: " + WorldmarketplaceMod.DECIMAL_FORMAT.format(Economy.getBalance(Objects.requireNonNull(ctx.getSource().getPlayer())))), true);
                    return 1;
                }).then(Commands.argument("player", EntityArgument.player()).executes(ctx -> {
                    var player = EntityArgument.getPlayer(ctx, "player");
                    ctx.getSource().sendSuccess(() -> Component.literal("Current economy balance for " + player.getDisplayName().getString() + ": " + WorldmarketplaceMod.DECIMAL_FORMAT.format(Economy.getBalance(player))), true);
                    return 0;
                }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> eco_decay() {
        return Commands.literal("decay")
                .executes(ctx -> {
//                    WorldmarketplaceMod.MARKET_ITEM.decay();
                    CommandSourceStack source = ctx.getSource();
//                    source.sendSuccess(() -> Component.literal("Current economy decayed, current traded: " + WorldmarketplaceMod.MARKET_ITEM.getTraded()), true);
                    return 1;
                });
    }

}
