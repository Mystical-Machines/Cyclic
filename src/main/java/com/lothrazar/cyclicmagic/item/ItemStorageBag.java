package com.lothrazar.cyclicmagic.item;
import java.util.List;
import com.lothrazar.cyclicmagic.IHasRecipe;
import com.lothrazar.cyclicmagic.ModMain;
import com.lothrazar.cyclicmagic.gui.ModGuiHandler;
import com.lothrazar.cyclicmagic.gui.storage.InventoryStorage;
import com.lothrazar.cyclicmagic.util.UtilChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemStorageBag extends BaseItem implements IHasRecipe {
  public ItemStorageBag() {
    this.setMaxStackSize(1);
  }
  @Override
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
    return false;
  }
  @Override
  public int getMaxItemUseDuration(ItemStack stack) {
    return 1; // Without this method, your inventory will NOT work!!!
  }
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
    int size = InventoryStorage.countNonEmpty(stack);
    tooltip.add(UtilChat.lang("item.storage_bag.tooltip") + size);
    super.addInformation(stack, playerIn, tooltip, advanced);
  }
  public static ItemStack getPlayerItemIfHeld(EntityPlayer player) {
    ItemStack wand = player.getHeldItemMainhand();
    if (wand == null || wand.getItem() instanceof ItemStorageBag == false) {
      wand = player.getHeldItemOffhand();
    }
    if (wand == null || wand.getItem() instanceof ItemStorageBag == false) { return null; }
    return wand;
  }
  @Override
  public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World world, EntityPlayer player, EnumHand hand) {
    if (!world.isRemote) {
      BlockPos pos = player.getPosition();
      int x = pos.getX(), y = pos.getY(), z = pos.getZ();
      player.openGui(ModMain.instance, ModGuiHandler.GUI_INDEX_STORAGE, world, x, y, z);
    }
    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
  }
  @Override
  public void addRecipe() {
    GameRegistry.addRecipe(new ItemStack(this), "lsl", "ldl", "lrl",
        'l', Items.LEATHER,
        's', Items.STRING,
        'r', Items.REDSTONE,
        'd', Items.GOLD_INGOT);
  }
}