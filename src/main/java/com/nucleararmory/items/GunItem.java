package com.nucleararmory.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GunItem extends Item {
    private final int damage;
    private final int range;

    public GunItem(Properties properties, int damage, int range) {
        super(properties);
        this.damage = damage;
        this.range = range;
    }

    public int getDamage() {
        return damage;
    }

    public int getRange() {
        return range;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 20;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 获取玩家视线方向
            Vec3 lookVector = player.getLookAngle();

            // 计算射击起点
            Vec3 startPos = new Vec3(
                player.getX() + lookVector.x * 0.5,
                player.getEyeY(),
                player.getZ() + lookVector.z * 0.5
            );

            // 获取附魔等级
            int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
            int infinityLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack);
            int flameLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack);
            int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);

            // 计算附魔后的伤害
            float damageAmount = (float) (damage + powerLevel * 2);

            // 发射子弹
            GunProjectile bullet = new GunProjectile(level, player, damageAmount, flameLevel > 0, punchLevel);
            bullet.setPos(startPos.x, startPos.y, startPos.z);

            // 设置速度（力量附魔增加速度）
            double speed = 3.0 + powerLevel * 0.5;
            bullet.setDeltaMovement(
                lookVector.x * speed,
                lookVector.y * speed,
                lookVector.z * speed
            );

            // 添加到世界
            level.addFreshEntity(bullet);

            // 播放射击声音
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 0.5f, 0.5f);
        }

        // 消耗耐久度（无限附魔不消耗）
        int infinityLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack);
        if (infinityLevel == 0) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }

        return InteractionResultHolder.success(stack);
    }
}
