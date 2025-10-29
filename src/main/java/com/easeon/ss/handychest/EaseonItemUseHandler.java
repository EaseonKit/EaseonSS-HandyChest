package com.easeon.ss.handychest;

import com.easeon.ss.core.game.EaseonSound;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EaseonItemUseHandler {
    public static ActionResult onUseItem(PlayerEntity player, World world, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (itemStack.getItem() == Items.ENDER_CHEST) {
            var enderChest = player.getEnderChestInventory();
            enderChest.onOpen(player);

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, p) -> {
                GenericContainerScreenHandler handler = GenericContainerScreenHandler.createGeneric9x3(syncId, inv, enderChest);
                return new GenericContainerScreenHandler(handler.getType(), syncId, inv, enderChest, 3) {
                    @Override
                    public void onClosed(PlayerEntity player) {
                        super.onClosed(player);
                        enderChest.onClose(player);
                        EaseonSound.play(player, SoundEvents.BLOCK_ENDER_CHEST_CLOSE);
                    }
                };
            }, Text.translatable("container.enderchest")));

            player.swingHand(hand, true);
            EaseonSound.play(player, SoundEvents.BLOCK_ENDER_CHEST_OPEN);

            return ActionResult.SUCCESS;
        } else if (itemStack.isIn(ItemTags.SHULKER_BOXES)) {
            SimpleInventory inventory = getShulkerBoxInventory(itemStack);

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, p) -> {
                        return new ShulkerBoxScreenHandler(syncId, inv, inventory) {
                            @Override
                            public void onClosed(PlayerEntity player) {
                                EaseonSound.play(player, SoundEvents.BLOCK_SHULKER_BOX_CLOSE);
                                super.onClosed(player);
                                saveShulkerBoxInventory(itemStack, inventory);
                            }
                        };
                    },
                    itemStack.getName()
            ));

            player.incrementStat(Stats.OPEN_SHULKER_BOX);
            player.swingHand(hand, true);
            EaseonSound.play(player, SoundEvents.BLOCK_SHULKER_BOX_OPEN);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private static SimpleInventory getShulkerBoxInventory(ItemStack stack) {
        ContainerComponent container = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        SimpleInventory inventory = new SimpleInventory(27);
        container.copyTo(inventory.getHeldStacks());
        return inventory;
    }

    private static void saveShulkerBoxInventory(ItemStack stack, SimpleInventory inventory) {
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);
            // 빈 슬롯도 포함 (isEmpty 체크 제거)
            items.add(slotStack.copy());
        }
        stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(items));
    }
}