package com.awwwsl.worldmarketplace.items;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.display.MarketTerminalMenu;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MarketTerminalItem extends BlockItem {
    public MarketTerminalItem() {
        super(WorldmarketplaceMod.MARKET_TERMINAL_BLOCK.get(), new Item.Properties()
                .food(new FoodProperties.Builder()
                    .alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 1), 100f)
                    .nutrition(1)
                    .saturationMod(0.5F)
                    .build())
        );
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext useOnContext) {
        if(useOnContext.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        if(useOnContext.getPlayer().isShiftKeyDown()) {
            if(useOnContext.getClickedFace() == Direction.DOWN || useOnContext.getClickedFace() == Direction.UP) {
                return InteractionResult.FAIL;
            }

            return super.useOn(useOnContext);
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider((id, inventory, p) ->
                new MarketTerminalMenu(id, inventory), Component.literal("我的菜单")));
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
