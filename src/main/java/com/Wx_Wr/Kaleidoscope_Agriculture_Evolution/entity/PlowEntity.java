package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.entity;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.block.ModBlocks;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.entity.rope.RopeEndpointCalculator;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.api.RopeAttachable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

import java.util.Optional;
import java.util.UUID;

public class PlowEntity extends AbstractDraggableEntity implements RopeAttachable {

    // 保存所属牛的 UUID
    private static final EntityDataAccessor<Optional<UUID>> OX_UUID =
            SynchedEntityData.defineId(PlowEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public PlowEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OX_UUID, Optional.empty());
    }

    // ==================== 核心牵引行为 ====================

    @Override
    protected void onDraggerMove() {
        BlockPos below = this.blockPosition().below();
        BlockState state = level().getBlockState(below);

        if (isTillable(state)) {
            level().setBlock(below, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0), 3);
            spawnBlockParticles(below, state);
            if (!this.level().isClientSide) {
                this.level().playSound(null, below,
                        net.minecraft.sounds.SoundEvents.HOE_TILL,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.0F);
            }
        }
    }

    @Override
    protected ItemStack getDropItem() {
        return new ItemStack(ModBlocks.PLOW.get().asItem());
    }

    @Override
    protected double getFollowDistance() { return 2.5; }
    @Override
    protected double getPullForce() { return 0.3; }
    @Override
    protected double getMaxSpeed() { return 0.35; }
    @Override
    protected float getTurnSpeed() { return 10.0f; }

    private boolean isTillable(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) ||
                state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT);
    }

    // ==================== 牛的绑定管理 ====================

    public void setOx(PlowOxEntity ox) {
        if (ox != null) {
            this.entityData.set(OX_UUID, Optional.of(ox.getUUID()));
        } else {
            this.entityData.set(OX_UUID, Optional.empty());
        }
    }

    public UUID getOxUUID() {
        return this.entityData.get(OX_UUID).orElse(null);
    }

    /**
     * 获取绑定的牛实体
     */
    public PlowOxEntity getOx() {
        UUID uuid = getOxUUID();
        if (uuid == null) return null;

        if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.entity.Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof PlowOxEntity ox && ox.isAlive()) {
                return ox;
            }
        }
        return null;
    }

    // ==================== 朝向更新 ====================

    @Override
    public void tick() {
        super.tick();

        // 每 tick 更新朝向，面向牛
        PlowOxEntity ox = getOx();
        if (ox != null && !level().isClientSide) {
            updateFacingTowardsOx(ox);
        }
    }

    private void updateFacingTowardsOx(PlowOxEntity ox) {
        double dx = ox.getX() - this.getX();
        double dz = ox.getZ() - this.getZ();

        if (Math.abs(dx) > 0.01 || Math.abs(dz) > 0.01) {
            float targetYaw = (float) (Math.toDegrees(Math.atan2(dx, dz)) + 180f);
            float currentYaw = this.getYRot();
            float deltaYaw = targetYaw - currentYaw;

            while (deltaYaw > 180) deltaYaw -= 360;
            while (deltaYaw < -180) deltaYaw += 360;

            float maxTurn = getTurnSpeed();
            if (deltaYaw > maxTurn) deltaYaw = maxTurn;
            if (deltaYaw < -maxTurn) deltaYaw = -maxTurn;

            this.setYRot(currentYaw + deltaYaw);
        }
    }

    // ==================== 数据持久化 ====================

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID oxId = getOxUUID();
        if (oxId != null) {
            tag.putUUID("OxUUID", oxId);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OxUUID")) {
            this.entityData.set(OX_UUID, Optional.of(tag.getUUID("OxUUID")));
        }
    }

    // ==================== GeckoLib 动画 ====================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<PlowEntity> state) {
        if (isWorking() && this.getDeltaMovement().horizontalDistance() > getMinWorkSpeed()) {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.plow.plowing"));
        } else {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.plow.idle"));
        }
        return PlayState.CONTINUE;
    }

    // ==================== RopeAttachable 接口实现 ====================

    @Override
    public Vec3 getLeftRopeAttachPoint(float partialTick) {
        return RopeEndpointCalculator.getPlowLeftPoint(this, partialTick);
    }

    @Override
    public Vec3 getRightRopeAttachPoint(float partialTick) {
        return RopeEndpointCalculator.getPlowRightPoint(this, partialTick);
    }

    @Override
    public UUID getRopeUUID() {
        return this.getUUID();
    }

    @Override
    public Level getRopeLevel() {
        return this.level();
    }

    @Override
    public boolean isRopeAttachableAlive() {
        return this.isAlive();
    }

    @Override
    public float getRopeYaw(float partialTick) {
        float prevYaw = this.yRotO;
        float currYaw = this.getYRot();
        float delta = currYaw - prevYaw;
        if (delta > 180) delta -= 360;
        if (delta < -180) delta += 360;
        return prevYaw + delta * partialTick;
    }
}