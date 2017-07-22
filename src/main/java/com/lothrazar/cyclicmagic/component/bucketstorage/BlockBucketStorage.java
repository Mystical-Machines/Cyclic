package com.lothrazar.cyclicmagic.component.bucketstorage;
import java.util.ArrayList;
import java.util.List;
import com.lothrazar.cyclicmagic.IHasRecipe;
import com.lothrazar.cyclicmagic.ModCyclic;
import com.lothrazar.cyclicmagic.block.base.BlockBase;
import com.lothrazar.cyclicmagic.component.hydrator.TileEntityHydrator;
import com.lothrazar.cyclicmagic.component.hydrator.TileEntityHydrator.Fields;
import com.lothrazar.cyclicmagic.registry.BlockRegistry;
import com.lothrazar.cyclicmagic.registry.RecipeRegistry;
import com.lothrazar.cyclicmagic.util.UtilChat;
import com.lothrazar.cyclicmagic.util.UtilNBT;
import com.lothrazar.cyclicmagic.util.UtilParticle;
import com.lothrazar.cyclicmagic.util.UtilSound;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBucketStorage extends BlockBase implements ITileEntityProvider, IHasRecipe {
  public BlockBucketStorage() {
    super(Material.IRON);
    this.setHardness(7F);
    this.setResistance(7F);
    this.setSoundType(SoundType.GLASS);
    this.setHarvestLevel("pickaxe", 1);
  }
  public static final String NBTBUCKETS = "buckets";
  public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    //?? TE null? http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/modification-development/2677315-solved-tileentity-returning-null
    //http://www.minecraftforge.net/forum/index.php?/topic/38048-19-solved-blockgetdrops-and-tileentity/
    List<ItemStack> ret = new ArrayList<ItemStack>();
    Item item = Item.getItemFromBlock(this);//this.getItemDropped(state, rand, fortune);
    TileEntity ent = world.getTileEntity(pos);
    ItemStack stack = new ItemStack(item);
    if (ent != null && ent instanceof TileEntityBucketStorage) {
      TileEntityBucketStorage te = (TileEntityBucketStorage) ent;
      FluidStack fs = te.getCurrentFluid();
      if (fs != null) {
        UtilNBT.setItemStackNBTVal(stack, BlockBucketStorage.NBTBUCKETS, fs.amount);
        String resourceStr = fs.getFluid().getStill().toString();
        UtilNBT.setItemStackNBTVal(stack, BlockBucketStorage.NBTBUCKETS + "fluid", resourceStr);
        ModCyclic.logger.info("block pickup save has  " + resourceStr + fs.amount);
      }
    }
    ret.add(stack);
    return ret;
  }
  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    if (stack.getTagCompound() != null) {
      NBTTagCompound tags = stack.getTagCompound();
      int b = tags.getInteger(NBTBUCKETS);
      String resourceStr = tags.getString(NBTBUCKETS + "fluid");
      TileEntityBucketStorage container = (TileEntityBucketStorage) worldIn.getTileEntity(pos);
      //          container.setBuckets(b);
      Fluid f = FluidRegistry.getFluid(resourceStr);//TODO: why null
      ModCyclic.logger.info("TODO: placed block has " + resourceStr + b + "??" + f);
    }
  }
  @SideOnly(Side.CLIENT)
  @Override
  public BlockRenderLayer getBlockLayer() {
    return BlockRenderLayer.TRANSLUCENT; // http://www.minecraftforge.net/forum/index.php?topic=18754.0
  }
  @Override
  public boolean isOpaqueCube(IBlockState state) { // http://greyminecraftcoder.blogspot.ca/2014/12/transparent-blocks-18.html
    return false;
  }
  @Override
  public boolean hasComparatorInputOverride(IBlockState state) {
    return true;
  }
  //  @Override
  //  public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
  //    TileEntityBucketStorage container = (TileEntityBucketStorage) world.getTileEntity(pos);
  //    return container.getBuckets();
  //  }
  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileEntityBucketStorage();
  }
  //start of 'fixing getDrops to not have null tile entity', using pattern from forge BlockFlowerPot patch
  @Override
  public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
    if (willHarvest) return true; //If it will harvest, delay deletion of the block until after getDrops
    return super.removedByPlayer(state, world, pos, player, willHarvest);
  }
  @Override
  public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack tool) {
    super.harvestBlock(world, player, pos, state, te, tool);
    world.setBlockToAir(pos);
  }
  @Override
  public IRecipe addRecipe() {
    return RecipeRegistry.addShapedRecipe(new ItemStack(this),
        "i i",
        " o ",
        "i i",
        'o', "obsidian", 'i', "ingotIron");
  }
  @Override
  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    // check the TE
    TileEntityBucketStorage te = (TileEntityBucketStorage) world.getTileEntity(pos);
    boolean success = FluidUtil.interactWithFluidHandler(player, hand, world, pos, side);
    if (te != null) {
      if (!world.isRemote) {
        FluidStack fs = te.getCurrentFluid();
        if (fs != null) {
          String amtStr = fs.amount + " / " + TileEntityBucketStorage.TANK_FULL + " ";
          UtilChat.sendStatusMessage(player, UtilChat.lang("cyclic.fluid.amount") + amtStr + fs.getLocalizedName());
        }
        else {
          UtilChat.sendStatusMessage(player, UtilChat.lang("cyclic.fluid.empty"));
        }
      }
    }
    // otherwise return true if it is a fluid handler to prevent in world placement
    return success || FluidUtil.getFluidHandler(player.getHeldItem(hand)) != null || super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
  }
}
