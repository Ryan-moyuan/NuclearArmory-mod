package com.nucleararmory.items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class GunProjectile extends AbstractArrow {
    private int lifetime = 100;
    private float damage;
    private boolean onFire;
    private int punchLevel;

    public GunProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    public GunProjectile(Level level, LivingEntity shooter, float damage, boolean onFire, int punchLevel) {
        super(EntityType.ARROW, shooter, level);
        this.damage = damage;
        this.onFire = onFire;
        this.punchLevel = punchLevel;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        Entity target = result.getEntity();
        Entity shooter = getOwner();

        if (target != shooter && !target.isSpectator() && target instanceof LivingEntity living) {
            float damageAmount = this.damage;

            // 爆头检测
            Vec3 hitLocation = result.getLocation();
            double headY = living.getY() + living.getBbHeight() - 0.3;
            if (hitLocation.y >= headY) {
                damageAmount *= 2.0f;
            }

            // 创建伤害来源
            var damageSource = level().damageSources().playerAttack((Player) shooter);

            // 施加伤害
            boolean hit = target.hurt(damageSource, damageAmount);

            if (hit) {
                // 击退效果（弹射附魔）
                if (punchLevel > 0) {
                    Vec3 knockback = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(0.5 + punchLevel * 0.5);
                    living.knockback(0.5 + punchLevel, knockback.x, knockback.z);
                }

                // 火焰附魔 - 点燃目标
                if (onFire) {
                    living.setSecondsOnFire(5);
                }

                // 击中粒子
                level().addParticle(
                    net.minecraft.core.particles.ParticleTypes.CRIT,
                    hitLocation.x,
                    hitLocation.y,
                    hitLocation.z,
                    0, 0, 0
                );

                // 火焰粒子
                if (onFire) {
                    level().addParticle(
                        net.minecraft.core.particles.ParticleTypes.FLAME,
                        hitLocation.x,
                        hitLocation.y,
                        hitLocation.z,
                        0, 0, 0
                    );
                }
            }
        }

        discard();
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            level().addParticle(
                net.minecraft.core.particles.ParticleTypes.SMOKE,
                getX(),
                getY(),
                getZ(),
                0, 0.05, 0
            );

            // 火焰粒子效果
            if (onFire) {
                level().addParticle(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    getX(),
                    getY(),
                    getZ(),
                    0, 0.1, 0
                );
            }
        }

        lifetime--;
        if (lifetime <= 0) {
            discard();
        }
    }

    @Override
    public void setSecondsOnFire(int seconds) {
        if (onFire) {
            super.setSecondsOnFire(seconds);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    public net.minecraft.world.item.ItemStack getItem() {
        return ItemStack.EMPTY;
    }
}
