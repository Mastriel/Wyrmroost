package WolfShotz.Wyrmroost.content.entities.dragonegg;

import WolfShotz.Wyrmroost.Wyrmroost;
import WolfShotz.Wyrmroost.content.entities.dragon.AbstractDragonEntity;
import WolfShotz.Wyrmroost.event.SetupEntities;
import WolfShotz.Wyrmroost.event.SetupItems;
import WolfShotz.Wyrmroost.util.network.EggHatchMessage;
import WolfShotz.Wyrmroost.util.utils.ModUtils;
import WolfShotz.Wyrmroost.util.utils.NetworkUtils;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnMobPacket;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

import static net.minecraft.entity.LivingEntity.ENTITY_GRAVITY;

public class DragonEggEntity extends Entity implements IAnimatedEntity, IEntityAdditionalSpawnData
{
    private int animationTick;
    private Animation animation = NO_ANIMATION;
    public AbstractDragonEntity containedDragon;
    public int hatchTime;
    @OnlyIn(Dist.CLIENT) public boolean wiggleInvert, wiggleInvert2;
    
    public static final Animation WIGGLE_ANIMATION = Animation.create(10);
    
    public DragonEggEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(SetupEntities.dragonEgg, world);
        containedDragon = ModUtils.<AbstractDragonEntity>getTypeByString(packet.getAdditionalData().readString()).create(world);
    }
    
    public DragonEggEntity(EntityType<? extends DragonEggEntity> dragonEgg, World world) {
        super(dragonEgg, world);
    }
    
    // ================================
    //           Entity NBT
    // ================================
    @Override
    protected void registerData() {}
    
    @Override
    public void readAdditional(CompoundNBT compound) {
        containedDragon = ModUtils.<AbstractDragonEntity>getTypeByString(compound.getString("dragonType")).create(world);
        hatchTime = compound.getInt("hatchTime");
    }
    
    @Override
    public void writeAdditional(CompoundNBT compound) {
        compound.putString("dragonType", getDragonKey());
        compound.putInt("hatchTime", hatchTime);
    }
    
    public String getDragonKey() { return EntityType.getKey(containedDragon.getType()).toString(); }
    
    // ================================
    
    @Override
    public void tick() {
        if (containedDragon == null) {
            safeError();
            return;
        }
        
        super.tick();
        
        updateMotion();
        
        if (getProperties().CONDITIONS.test(this)) {
            if (world.isRemote) {
                if (ticksExisted % 3 == 0) {
                    double x = posX + rand.nextGaussian() * 0.2d;
                    double y = posY + rand.nextDouble() + getHeight() / 2;
                    double z = posZ + rand.nextGaussian() * 0.2d;
                    world.addParticle(new RedstoneParticleData(1f, 1f, 0, 0.5f), x, y, z, 0, 0, 0);
                }
            } else {
                if (hatchTime > 0 && getProperties().CONDITIONS.test(this)) --hatchTime;
                else {
                    hatch();
                    Wyrmroost.network.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new EggHatchMessage(this));
                    return;
                }
    
                int bounds = Math.max(hatchTime / 2, 3);
                
                if (hatchTime < getProperties().HATCH_TIME / 2 && rand.nextInt(bounds) == 0 && getAnimation() != WIGGLE_ANIMATION)
                    crack(true);
            }
        }
    
        EntitySize size = getProperties().getSize();
        if (getWidth() != size.width || getHeight() != size.height) recalculateSize();
        
        if (getAnimation() != NO_ANIMATION) {
            ++animationTick;
            if (animationTick >= animation.getDuration()) setAnimation(NO_ANIMATION);
        }
    }
    
    @Override
    protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        super.updateFallState(y, onGroundIn, state, pos);
        
        if (onGroundIn) {
            setAnimation(WIGGLE_ANIMATION);
            crack(false);
        }
    }
    
    private void updateMotion() {
        boolean flag = getMotion().y <= 0.0D;
        double d1 = posY;
        double d0 = 0.5d;
    
        move(MoverType.SELF, getMotion());
        if (!hasNoGravity() && !isSprinting()) {
            Vec3d vec3d2 = getMotion();
            double d2;
            if (flag && Math.abs(vec3d2.y - 0.005D) >= 0.003D && Math.abs(vec3d2.y - d0 / 16.0D) < 0.003D) d2 = -0.003D;
            else d2 = vec3d2.y - d0 / 16.0D;
        
            setMotion(vec3d2.x, d2, vec3d2.z);
        }
    
        Vec3d vec3d6 = getMotion();
        if (collidedHorizontally && isOffsetPositionInLiquid(vec3d6.x, vec3d6.y + (double)0.6F - posY + d1, vec3d6.z)) {
            setMotion(vec3d6.x, (double)0.3F, vec3d6.z);
        }
    }
    
    /**
     * Called to hatch the dragon egg
     * Usage: <P>
     *  - Get the dragon EntityType (If its something it shouldnt, safely fail) <P>
     *  - Set the dragons growing age to a baby <P>
     *  - Set the position of that dragon to the position of this egg <P>
     *  - Remove this entity (the egg) and play any effects
     */
    public void hatch() {
        AbstractDragonEntity newDragon = (AbstractDragonEntity) containedDragon.getType().create(world); // May not be necessary, not sure, do it anyway tho.
        if (!world.isRemote) {
            newDragon.setPosition(posX, posY, posZ);
            newDragon.setGrowingAge(-(newDragon.getEggProperties().HATCH_TIME * 2));
            newDragon.onInitialSpawn(world, world.getDifficultyForLocation(getPosition()), SpawnReason.BREEDING, null, null);
            world.addEntity(newDragon);
        } else {
            for (int i = 0; i < getWidth() * 25; ++i) {
                double x = rand.nextGaussian() * 0.2f;
                double y = rand.nextDouble() * 0.45f;
                double z = rand.nextGaussian() * 0.2f;
                world.addParticle(new ItemParticleData(ParticleTypes.ITEM, new ItemStack(SetupItems.dragonEgg)), posX, posY, posZ, x, y, z);
            }
        }
        world.playSound(posX, posY, posZ, SoundEvents.ENTITY_TURTLE_EGG_HATCH, SoundCategory.BLOCKS, 1, 1, false);
        remove();
    }
    
    public void crack(boolean sendPacket) {
        playSound(SoundEvents.ENTITY_TURTLE_EGG_CRACK, 1f, 1f);
        if (sendPacket) NetworkUtils.sendAnimationPacket(this, WIGGLE_ANIMATION);
        else setAnimation(WIGGLE_ANIMATION);
    }
    
    /**
     * Called When the dragon type of the egg is not what it should be.
     */
    private void safeError() {
        ModUtils.L.error("THIS ISNT A DRAGON WTF KIND OF ABOMINATION IS THIS HATCHING?!?! Unknown Entity Type for Dragon Egg @ {}", getPosition());
        remove();
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        CompoundNBT tag = new CompoundNBT();
        
        tag.putInt("hatchTime", hatchTime);
        tag.putString("dragonType", getDragonKey());
        ItemStack itemStack = new ItemStack(SetupItems.dragonEgg);
        itemStack.setTag(tag);
        InventoryHelper.spawnItemStack(world, posX, posY, posZ, itemStack);
        remove();
        
        return true;
    }
    
    public DragonEggProperties getProperties() {
        try { return containedDragon.getEggProperties(); }
        catch (NullPointerException e) { return new DragonEggProperties(0.65f, 1f, 12000); }
    }
    
    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        ItemStack stack = new ItemStack(SetupItems.dragonEgg);
        CompoundNBT tag = new CompoundNBT();
        tag.putString("dragonType", getDragonKey());
        tag.putInt("hatchTime", getProperties().HATCH_TIME);
        stack.setTag(tag);
        return stack;
    }
    
    @Override
    public EntitySize getSize(Pose poseIn) { return getProperties().getSize(); }
    
    @Override
    public boolean canBePushed() { return false; }
    
    @Override
    public boolean canBeCollidedWith() { return true; }
    
    @Override
    public void fall(float distance, float damageMultiplier) {
        super.fall(distance, damageMultiplier);
    }
    
    // This is needed because it seems to be ignored on server world...
    @Override
    public void onKillCommand() { remove(); }
    
    @Override
    public IPacket<?> createSpawnPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
    
    /**
     * Called by the server when constructing the spawn packet.
     * Data should be added to the provided stream.
     *
     * @param buffer The packet data stream
     */
    @Override
    public void writeSpawnData(PacketBuffer buf) { buf.writeString(getDragonKey()); }
    
    /**
     * Called by the client when it receives a Entity spawn packet.
     * Data should be read out of the stream in the same way as it was written.
     *
     * @param additionalData The packet data stream
     */
    @Override
    public void readSpawnData(PacketBuffer additionalData) { }
    
    // === Animation ===
    @Override
    public int getAnimationTick() { return animationTick; }
    
    @Override
    public void setAnimationTick(int i) { animationTick = i; }
    
    @Override
    public Animation getAnimation() { return animation; }
    
    @Override
    public void setAnimation(Animation animation) {
        this.animation = animation;
        setAnimationTick(0);
        
        
        if (world.isRemote && animation == WIGGLE_ANIMATION) {
            wiggleInvert = rand.nextBoolean();
            wiggleInvert2 = rand.nextBoolean();
        }
    }
    
    @Override
    public Animation[] getAnimations() { return new Animation[] {NO_ANIMATION, WIGGLE_ANIMATION}; }
    
    // ================
}
