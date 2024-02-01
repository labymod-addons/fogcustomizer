/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.labymod.addons.fogcustomizer.v1_12_2.mixins;

import net.labymod.addons.fogcustomizer.FogCustomizer;
import net.labymod.addons.fogcustomizer.configuration.FogCustomizerConfiguration;
import net.labymod.addons.fogcustomizer.configuration.color.ColorConfiguration;
import net.labymod.addons.fogcustomizer.configuration.density.DensityConfiguration;
import net.labymod.api.util.Color;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

  @Unique
  private FogCustomizerConfiguration fogcustomizer$configuration;

  @Shadow
  private float fogColorRed;
  @Shadow
  private float fogColorGreen;
  @Shadow
  private float fogColorBlue;

  @Redirect(
      method = "updateFogColor(F)V",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/World;getFogColor(F)Lnet/minecraft/util/math/Vec3d;"
      )
  )
  private Vec3d fogcustomizer$getFogColor(World world, float partialTicks) {
    FogCustomizerConfiguration config = this.fogcustomizer$config();

    if (config.enabled().get()) {
      ColorConfiguration colors = config.color();
      WorldProvider provider = world.provider;

      // Overworld
      if (provider instanceof WorldProviderSurface && colors.surface().get()) {
        Color color = colors.surfaceColor().get();
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;

        float strength = this.fogcustomizer$getStrength(world, partialTicks);
        red = red * (strength * 0.94F + 0.06F);
        green = green * (strength * 0.94F + 0.06F);
        blue = blue * (strength * 0.91F + 0.09F);
        return new Vec3d(red, green, blue);
      }

      // Nether
      if (provider instanceof WorldProviderHell && colors.hell().get()) {
        Color color = colors.hellColor().get();
        return new Vec3d(
            color.getRed() / 255.0F,
            color.getGreen() / 255.0F,
            color.getBlue() / 255.0F
        );
      }

      // End
      if (provider instanceof WorldProviderEnd && colors.end().get()) {
        Color color = colors.surfaceColor().get();
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;

        float strength = this.fogcustomizer$getStrength(world, partialTicks);
        red = red * (strength * 0.0F + 0.15F);
        green = green * (strength * 0.0F + 0.15F);
        blue = blue * (strength * 0.0F + 0.15F);

        return new Vec3d(red, green, blue);
      }
    }

    // Vanilla
    return world.getFogColor(partialTicks);
  }

  @Inject(
      method = "updateFogColor",
      at = @At(
          value = "FIELD",
          target = "Lnet/minecraft/client/renderer/EntityRenderer;fogColor1:F",
          shift = Shift.BEFORE
      )
  )
  private void fogcustomizer$updateFogColor(float partialTicks, CallbackInfo ci) {
    FogCustomizerConfiguration config = this.fogcustomizer$config();
    if (!config.enabled().get()) {
      return;
    }

    Minecraft minecraft = Minecraft.getMinecraft();
    Entity entity = minecraft.getRenderViewEntity();
    if (entity == null) {
      return;
    }

    WorldClient world = minecraft.world;
    IBlockState block = ActiveRenderInfo.getBlockStateAtEntityViewpoint(
        world,
        entity,
        partialTicks
    );

    ColorConfiguration colors = config.color();
    if (block.getMaterial() == Material.WATER && colors.water().get()) {
      // Get respiration
      float respiration = (float) EnchantmentHelper.getRespirationModifier(
          (EntityLivingBase) entity
      ) * 0.2F;
      if (entity instanceof EntityLivingBase
          && ((EntityLivingBase) entity).isPotionActive(MobEffects.WATER_BREATHING)) {
        respiration = respiration * 0.3F + 0.6F;
      }

      Color color = colors.waterColor().get();

      this.fogColorRed = color.getRed() / 255.0F + respiration;
      this.fogColorGreen = color.getGreen() / 255.0F + respiration;
      this.fogColorBlue = color.getBlue() / 255.0F + respiration;
    } else if (block.getMaterial() == Material.LAVA && colors.lava().get()) {
      Color color = colors.lavaColor().get();

      this.fogColorRed = color.getRed() / 255.0F;
      this.fogColorGreen = color.getGreen() / 255.0F;
      this.fogColorBlue = color.getBlue() / 255.0F;
    }
  }

  @Inject(
      method = "setupFog",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/WorldProvider;doesXZShowFog(II)Z",
          shift = Shift.BEFORE
      )
  )
  private void fogcustomizer$setupFogSurface(int startCoords, float partialTicks, CallbackInfo ci) {
    FogCustomizerConfiguration config = this.fogcustomizer$config();
    if (!config.enabled().get()) {
      return;
    }

    DensityConfiguration density = config.density();
    if (!density.surface().get()) {
      return;
    }

    this.fogcustomizer$setFog(density.surfaceDensity().get(), density.surfaceDistance().get());
  }

  @Inject(
      method = "setupFog",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/renderer/GlStateManager;setFogEnd(F)V",
          shift = Shift.AFTER
      ),
      slice = @Slice(
          from = @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/WorldProvider;doesXZShowFog(II)Z"
          ),
          to = @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/renderer/GlStateManager;enableColorMaterial()V"
          )
      )
  )
  private void fogcustomizer$setupFogHellAndEnd(int startCoords, float partialTicks,
      CallbackInfo ci) {
    FogCustomizerConfiguration config = this.fogcustomizer$config();
    if (!config.enabled().get()) {
      return;
    }

    WorldProvider provider = Minecraft.getMinecraft().world.provider;

    DensityConfiguration density = config.density();
    if (density.hell().get() && provider instanceof WorldProviderHell) {
      this.fogcustomizer$setFog(density.hellDensity().get(), density.hellDistance().get());
    }

    if (density.end().get() && provider instanceof WorldProviderEnd) {
      this.fogcustomizer$setFog(density.endDensity().get(), density.endDistance().get());
    }
  }

  @Unique
  private float fogcustomizer$getStrength(World world, float partialTicks) {
    float angle = world.getCelestialAngle(partialTicks);
    float strength = MathHelper.cos(angle * (float) Math.PI * 2.0F) * 2.0F + 0.5F;
    return MathHelper.clamp(strength, 0.0F, 1.0F);
  }

  @Unique
  private void fogcustomizer$setFog(int density, int distance) {
    int start = (int) (distance * (1 - density / 100.0F));
    GlStateManager.setFogStart(Math.min(start, distance));
    GlStateManager.setFogEnd(distance + 0.01F);
  }

  @Unique
  private FogCustomizerConfiguration fogcustomizer$config() {
    if (this.fogcustomizer$configuration == null) {
      this.fogcustomizer$configuration = FogCustomizer.get().configuration();
    }
    return this.fogcustomizer$configuration;
  }


}
