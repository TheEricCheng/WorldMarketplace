package com.awwwsl.worldmarketplace;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

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
                .then(Commands.argument(amountArgumentName, IntegerArgumentType.integer())
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    int amount = IntegerArgumentType.getInteger(ctx, amountArgumentName);
                    if(amount < 0) {
                        source.sendFailure(Component.literal("Amount cannot be negative."));
                        return 0;
                    }
                    DecimalFormat df = new DecimalFormat("#,##0.00");
                    df.setRoundingMode(RoundingMode.HALF_EVEN);

                    WorldmarketplaceMod.ECONOMY = BigDecimal.valueOf(amount);
                    source.sendSuccess(() -> Component.literal("Current economy have been set to " + df.format(amount)), true);
                    return 1;
                }));
    }
    private static LiteralArgumentBuilder<CommandSourceStack> eco_balance() {
        return Commands.literal("balance")
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    DecimalFormat df = new DecimalFormat("#,##0.00");
                    df.setRoundingMode(RoundingMode.HALF_EVEN);
                    source.sendSuccess(() -> Component.literal("Current economy balance: " + df.format(WorldmarketplaceMod.ECONOMY)), true);
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> eco_decay() {
        return Commands.literal("decay")
                .executes(ctx -> {
                    WorldmarketplaceMod.MARKET_ITEM.decay();
                    CommandSourceStack source = ctx.getSource();
                    source.sendSuccess(() -> Component.literal("Current economy decayed, current traded: " + WorldmarketplaceMod.MARKET_ITEM.getTraded()), true);
                    return 1;
                });
    }

}
