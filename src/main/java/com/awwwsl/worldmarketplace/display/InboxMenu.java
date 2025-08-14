package com.awwwsl.worldmarketplace.display;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.api.EconomyRepo;
import com.awwwsl.worldmarketplace.api.Market;
import com.awwwsl.worldmarketplace.api.MarketItemType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class InboxMenu extends AbstractContainerMenu {
    private final Market market;

    public InboxMenu(int containerId, Inventory inv, Market market) {
        super(WorldmarketplaceMod.INBOX_MENU_TYPE.get(), containerId);
        this.market = market;
        Container container = new SimpleContainer(54);
        var items = market.items().stream()
                .filter(e -> e.type == MarketItemType.BUY || e.type == MarketItemType.BOTH)
                .toList();
        for(int i = 0; i < items.size(); i++) {
            var marketItem = items.get(i);
            var displayItem = ForgeRegistries.ITEMS.getValue(marketItem.id);
            if(displayItem == null || displayItem == Items.AIR) {
                displayItem = Items.BARRIER;
                var displayStack = new ItemStack(displayItem);
                var loreLines = new ArrayList<Component>();
                loreLines.add(Component.literal("不存在的物品: " + marketItem.id).withStyle(ChatFormatting.GOLD));
                ListTag loreTagList = new ListTag();
                for (Component line : loreLines) {
                    loreTagList.add(StringTag.valueOf(Component.Serializer.toJson(line)));
                }
                CompoundTag displayTag = displayStack.getOrCreateTagElement("display");
                displayTag.put("Lore", loreTagList);
                container.setItem(i, displayStack);
            } else {
                var displayStack = new ItemStack(displayItem);
                var loreLines = new ArrayList<Component>();
                loreLines.add(Component.literal("可交易物品: ").withStyle(ChatFormatting.GOLD).append(displayItem.getDescription()));
                loreLines.add(Component.literal("价格: ").withStyle(ChatFormatting.GOLD).append(Component.literal("$" + marketItem.basePrice.toString()).withStyle(ChatFormatting.GREEN)));
                loreLines.add(Component.literal(""));
                loreLines.add(Component.literal("点击以购买一组").withStyle(ChatFormatting.AQUA));
                loreLines.add(Component.literal("Shift + 点击以最大化购买").withStyle(ChatFormatting.AQUA));
                ListTag loreTagList = new ListTag();
                for (Component line : loreLines) {
                    loreTagList.add(StringTag.valueOf(Component.Serializer.toJson(line)));
                }
                CompoundTag displayTag = displayStack.getOrCreateTagElement("display");
                displayTag.put("Lore", loreTagList);
                container.setItem(i, displayStack);
            }
        }
        // 上面 6 行：54 格
        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new DisplayOnlySlot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        // 玩家物品栏：3 行
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 198));
        }
    }
    public InboxMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, Market.load(Objects.requireNonNull(data.readNbt())));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getItem();
        ItemStack copyStack = originalStack.copy();

        if (index >= 81) {
            // hotbar to inv
            if (!moveItemStackTo(originalStack, 54, 54 + 3 * 9, false)) {
                return ItemStack.EMPTY;
            }
        } else if(!moveItemStackTo(originalStack, 54 + 3 * 9, this.slots.size(), true)) {
            // inv to hotbar
            return ItemStack.EMPTY;
        }

        if (originalStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copyStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clicked(int index, int button, @NotNull ClickType type, @NotNull Player player) {
        if(index >= 54) {
            super.clicked(index, button, type, player);
            return;
        }
        if(player instanceof ServerPlayer serverPlayer) {
            var slot = getSlot(index);
            var item = slot.getItem().getItem();
            if(item == Items.AIR || item == Items.BARRIER) {
                return;
            }
            var marketItem = market.items().stream().filter(e -> e.id.equals(ForgeRegistries.ITEMS.getKey(item))).findFirst().orElse(null);
            if(marketItem == null) {
                WorldmarketplaceMod.LOGGER.warn("Item {} not found in market but in menu", item);
                return;
            }
            int howMany;
            if(type == ClickType.PICKUP) {
                howMany = 64;
            } else if(type == ClickType.QUICK_MOVE) {
                howMany = Integer.MAX_VALUE;
            } else {
                return;
            }
            var canBuy = new AtomicInteger(0);
            player.getInventory().items.forEach(itemStack -> {
                if(itemStack.isEmpty()) {
                    canBuy.addAndGet(item.getMaxStackSize(itemStack));
                } else if(itemStack.getItem() == item) {
                    canBuy.addAndGet(Math.max(item.getMaxStackSize(itemStack) - itemStack.getCount(), 0));
                }
            });
            var needBuy = Math.min(howMany, canBuy.get());
            var total = needBuy;
            if(EconomyRepo.buy(serverPlayer, marketItem, needBuy)) {
                while(needBuy > 0) {
                    var gave = new ItemStack(item);
                    gave.setCount(Math.min(item.getMaxStackSize(gave), needBuy));
                    needBuy -= gave.getCount();
                    if(!serverPlayer.addItem(gave)) {
                        var drop = serverPlayer.drop(gave, false);
                        if (drop != null) {
                            drop.setNoPickUpDelay();
                        }
                    }
                }
                serverPlayer.sendSystemMessage(Component.literal("已购买 " + total + " 件物品: ").append(item.getDescription()).withStyle(ChatFormatting.GREEN));
            } else {
                serverPlayer.sendSystemMessage(Component.literal("Expected: ").append(WorldmarketplaceMod.DECIMAL_FORMAT.format(EconomyRepo.getPrice(marketItem, needBuy))).append(". Actual: ").append(WorldmarketplaceMod.DECIMAL_FORMAT.format(EconomyRepo.getBalance(serverPlayer))).withStyle(ChatFormatting.RED), false);
            }
        }
    }
}

