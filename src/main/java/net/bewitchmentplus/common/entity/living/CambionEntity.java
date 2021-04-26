package net.bewitchmentplus.common.entity.living;

import moriyashiine.bewitchment.api.BewitchmentAPI;
import moriyashiine.bewitchment.common.entity.living.util.BWHostileEntity;
import net.bewitchmentplus.BewitchmentPlus;
import net.bewitchmentplus.common.registry.BWPTags;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.List;

public class CambionEntity extends BWHostileEntity {

	int barterTimer = 0;

	protected CambionEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	private static List<ItemStack> getBarteredItem(CambionEntity cambionEntity) {
		LootTable lootTable = cambionEntity.world.getServer().getLootManager().getTable(LootTables.NETHER_BRIDGE_CHEST);
		List<ItemStack> list = lootTable.generateLoot((new LootContext.Builder((ServerWorld) cambionEntity.world)).parameter(LootContextParameters.THIS_ENTITY, cambionEntity).random(cambionEntity.world.random).build(LootContextTypes.BARTER));
		return list;
	}

	public EntityGroup getGroup() {
		return BewitchmentAPI.DEMON;
	}

	protected boolean hasShiny() {
		return true;
	}

	@Override
	public boolean canUsePortals() {
		return true;
	}

	@Override
	public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
		boolean flag = super.canSpawn(world, spawnReason);
		if (flag && (spawnReason == SpawnReason.SPAWNER || spawnReason == SpawnReason.STRUCTURE || spawnReason == SpawnReason.MOB_SUMMONED || spawnReason == SpawnReason.SPAWN_EGG || spawnReason == SpawnReason.COMMAND || spawnReason == SpawnReason.DISPENSER || spawnReason == SpawnReason.NATURAL)) {
			return true;
		}
		if (world instanceof ServerWorld && BewitchmentPlus.config.cambionStructureSpawn) {
			BlockPos nearestVillage = ((ServerWorld) world).locateStructure(StructureFeature.VILLAGE, getBlockPos(), 3, false);
			BlockPos nearestBastion = ((ServerWorld) world).locateStructure(StructureFeature.BASTION_REMNANT, getBlockPos(), 3, false);
			BlockPos nearestFortress = ((ServerWorld) world).locateStructure(StructureFeature.FORTRESS, getBlockPos(), 3, false);
			return (nearestVillage != null && Math.sqrt(nearestVillage.getSquaredDistance(getBlockPos())) < 128);
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (barterTimer > 0) barterTimer--;
	}

	//Todo: Grab from a loot table. Also set up the timer fully.
	//Todo: Add items to the cambion trade tag
	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (barterTimer == 0) {
			if (itemStack.getItem() == BWPTags.CAMBION_TRADE) {
				player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundCategory.NEUTRAL, 1, 1);
				ItemStack itemStack2 = ItemUsage.method_30012(itemStack, player, getBarteredItem()); //Fixme: Pick up on a resource table, and give a player a random item, or a random amount of one specific item at a time.
				player.setStackInHand(hand, itemStack2);
				barterTimer = 1200; //Timer exists to avoid cheese
				return ActionResult.success(this.world.isClient);
			} else {
				return super.interactMob(player, hand);
			}
		}
		return null;
	}

	@Override
	public int getVariants() {
		return 6;
	}
}
