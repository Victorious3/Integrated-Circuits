package moe.nightfall.vic.integratedcircuits.client;

import moe.nightfall.vic.integratedcircuits.Constants;
import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Resources {
	// Parts
	public static IIcon ICON_IC;
	public static IIcon ICON_IC_SOCKET;
	public static IIcon ICON_IC_SEGMENT;
	public static IIcon ICON_IC_BASE;
	public static IIcon ICON_IC_BASE_FMP;
	public static IIcon ICON_IC_WIRE;
	public static IIcon ICON_IC_WIRE_FLIPPED;
	public static IIcon ICON_IC_RSWIRE_OFF;
	public static IIcon ICON_IC_RSWIRE_ON;

	// Items
	public static IIcon ICON_PCB;
	public static IIcon ICON_PCB_RAW;

	// Blocks
	public static IIcon ICON_ASSEMBLER_FRONT_OFF;
	public static IIcon ICON_ASSEMBLER_FRONT_ON;
	public static IIcon ICON_ASSEMBLER_BACK;
	public static IIcon ICON_ASSEMBLER_SIDE;
	public static IIcon ICON_ASSEMBLER_BOTTOM;
	public static IIcon ICON_ASSEMBLER_TOP;

	public static IIcon ICON_CAD_FRONT_OFF;
	public static IIcon ICON_CAD_FRONT_ON;
	public static IIcon ICON_CAD_BACK_OFF;
	public static IIcon ICON_CAD_BACK_ON;
	public static IIcon ICON_CAD_SIDE;

	// Resources
	public static final ResourceLocation RESOURCE_ASSEMBLER_BOTTOM = new ResourceLocation(Constants.MOD_ID,
			"textures/blocks/assembler_bottom.png");
	public static final ResourceLocation RESOURCE_ASSEMBLER_SAFETY = new ResourceLocation(Constants.MOD_ID,
			"textures/blocks/assembler_safety.png");

	public static final ResourceLocation RESOURCE_GUI_ASSEMBLER_BACKGROUND = new ResourceLocation(Constants.MOD_ID,
			"textures/gui/assembler.png");
	public static final ResourceLocation RESOURCE_GUI_CONTROLS = new ResourceLocation(Constants.MOD_ID,
			"textures/gui/controls.png");
	public static final ResourceLocation RESOURCE_GUI_CAD_BACKGROUND = new ResourceLocation(Constants.MOD_ID,
			"textures/gui/cad.png");
	public static final ResourceLocation RESOURCE_GUI_7SEGMENT_BACKGROUND = new ResourceLocation(Constants.MOD_ID,
			"textures/gui/7segment.png");

	public static final ResourceLocation RESOURCE_PCB = new ResourceLocation(Constants.MOD_ID,
			"textures/gui/sublogicpart.png");
	public static final ResourceLocation RESOURCE_PCB_PERF1 = new ResourceLocation(Constants.MOD_ID,
			"textures/gui/bg1.png");
	public static final ResourceLocation RESOURCE_PCB_PERF2 = new ResourceLocation(Constants.MOD_ID,
			"textures/gui/bg2.png");

	public static final ResourceLocation RESOURCE_MISC_CROWN = new ResourceLocation(Constants.MOD_ID,
			"textures/crown.png");
	public static final ResourceLocation RESOURCE_MISC_HALO = new ResourceLocation(Constants.MOD_ID,
			"textures/halo.png");
	public static final ResourceLocation RESOURCE_MISC_EARS = new ResourceLocation(Constants.MOD_ID,
			"textures/ears.png");
	public static final ResourceLocation RESOURCE_MISC_FLOWER = new ResourceLocation(Constants.MOD_ID,
			"textures/mami_flower.png");
	public static final ResourceLocation RESOURCE_MISC_FLUFF = new ResourceLocation(Constants.MOD_ID,
			"textures/mami_fluff.png");
	public static final ResourceLocation RESOURCE_MISC_NANO = new ResourceLocation(Constants.MOD_ID,
			"textures/nanokey.png");

	public Resources() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchEvent(TextureStitchEvent event) {
		TextureMap map = event.map;

		switch (map.getTextureType()) {
			case 0:

				ICON_IC = map.registerIcon(Constants.MOD_ID + ":ic");
				ICON_IC_SOCKET = map.registerIcon(Constants.MOD_ID + ":ic_uniform");
				ICON_IC_SEGMENT = map.registerIcon(Constants.MOD_ID + ":ic_segment");
				ICON_IC_BASE = map.registerIcon(Constants.MOD_ID + ":ic_base");
				ICON_IC_BASE_FMP = map.registerIcon(Constants.MOD_ID + ":ic_base_fmp");
				ICON_IC_WIRE = map.registerIcon(Constants.MOD_ID + ":ic_wire");
				ICON_IC_WIRE_FLIPPED = new IconFlipped(ICON_IC_WIRE, true, false);
				ICON_IC_RSWIRE_OFF = map.registerIcon(Constants.MOD_ID + ":ic_rswire_off");
				ICON_IC_RSWIRE_ON = map.registerIcon(Constants.MOD_ID + ":ic_rswire_on");

				ICON_ASSEMBLER_FRONT_OFF = map.registerIcon(Constants.MOD_ID + ":assembler_front_off");
				ICON_ASSEMBLER_FRONT_ON = map.registerIcon(Constants.MOD_ID + ":assembler_front_on");
				ICON_ASSEMBLER_BACK = map.registerIcon(Constants.MOD_ID + ":assembler_back");
				ICON_ASSEMBLER_BOTTOM = map.registerIcon(Constants.MOD_ID + ":assembler_bottom");
				ICON_ASSEMBLER_TOP = map.registerIcon(Constants.MOD_ID + ":assembler_top");
				ICON_ASSEMBLER_SIDE = map.registerIcon(Constants.MOD_ID + ":assembler_side");

				ICON_CAD_FRONT_OFF = map.registerIcon(Constants.MOD_ID + ":cad_front_off");
				ICON_CAD_FRONT_ON = map.registerIcon(Constants.MOD_ID + ":cad_front_on");
				ICON_CAD_BACK_OFF = map.registerIcon(Constants.MOD_ID + ":cad_back_off");
				ICON_CAD_BACK_ON = map.registerIcon(Constants.MOD_ID + ":cad_back_on");
				ICON_CAD_SIDE = map.registerIcon(Constants.MOD_ID + ":cad_side");

				break;

			case 1:

				ICON_PCB = map.registerIcon(Constants.MOD_ID + ":pcb");
				ICON_PCB_RAW = map.registerIcon(Constants.MOD_ID + ":pcb_raw");

				break;
		}
		;
	}
}
