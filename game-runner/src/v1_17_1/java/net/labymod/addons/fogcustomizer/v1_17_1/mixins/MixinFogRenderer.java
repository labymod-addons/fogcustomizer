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

package net.labymod.addons.fogcustomizer.v1_17_1.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import net.labymod.addons.fogcustomizer.FogCustomizer;
import net.labymod.addons.fogcustomizer.configuration.FogCustomizerConfiguration;
import net.labymod.addons.fogcustomizer.configuration.color.ColorConfiguration;
import net.labymod.addons.fogcustomizer.configuration.density.DensityConfiguration;
import net.labymod.api.util.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

  @Unique
  private static FogCustomizerConfiguration fogcustomizer$configuration;

  @Shadow
  private static float fogRed;
  @Shadow
  private static float fogGreen;
  @Shadow
  private static float fogBlue;

  @Dynamic
  @Redirect(
      method = {
          "lambda$setupColor$0", // Vanilla
          "lambda$updateFogColor$0" // OptiFine
      },
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/level/biome/Biome;getFogColor()I"
      ),
      require = 0,
      expect = 0
  )
  private static int getFogColor(Biome biome) {
    FogCustomizerConfiguration config = fogcustomizer$config();
    ClientLevel level = Minecraft.getInstance().level;

    if (config.enabled().get() && level != null) {
      ColorConfiguration colors = config.color();
      ResourceKey<Level> dimensionType = level.dimension();

      // Surface
      if (colors.surface().get() && dimensionType == Level.OVERWORLD) {
        return colors.surfaceColor().get().get();
      }

      // Nether
      if (colors.hell().get() && dimensionType == Level.NETHER) {
        return colors.hellColor().get().get();
      }

      // End
      if (colors.end().get() && dimensionType == Level.END) {
        return colors.endColor().get().get();
      }
    }
    return biome.getFogColor();
  }

  @Inject(
      method = "setupColor",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;getClearColorScale()D",
          shift = At.Shift.AFTER
      ),
      require = 0,
      expect = 0
  )
  private static void fogcustomizer$setupColorWaterAndLava(
      Camera camera,
      float partialTicks,
      ClientLevel world,
      int renderDistance,
      float darkenWorldAmount,
      CallbackInfo ci
  ) {
    FogCustomizerConfiguration config = fogcustomizer$config();
    if (!config.enabled().get()) {
      return;
    }

    FogType type = camera.getFluidInCamera();
    if (type == FogType.WATER) {
      ColorConfiguration colors = config.color();

      // Water
      if (colors.water().get()) {
        Color color = colors.waterColor().get();

        fogRed = color.getRed() / 255.0F;
        fogGreen = color.getGreen() / 255.0F;
        fogBlue = color.getBlue() / 255.0F;
      }
    } else if (type == FogType.LAVA) {
      ColorConfiguration colors = config.color();

      // Lava
      if (colors.lava().get()) {
        Color color = colors.lavaColor().get();

        fogRed = color.getRed() / 255.0F;
        fogGreen = color.getGreen() / 255.0F;
        fogBlue = color.getBlue() / 255.0F;
      }
    }
  }

  @Dynamic
  @Inject(
      method = {
          "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZ)V",
          // Vanilla
          "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V"
          // OptiFine
      },
      at = @At(
          value = "INVOKE",
          target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V",
          shift = At.Shift.AFTER
      ),
      require = 0,
      expect = 0
  )
  private static void fogcustomizer$setupFogDensityVanilla(CallbackInfo ci) {
    FogCustomizerConfiguration config = fogcustomizer$config();
    ClientLevel level = Minecraft.getInstance().level;
    if (!config.enabled().get() || level == null) {
      return;
    }

    Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
    Entity entity = camera.getEntity();
    FogType type = camera.getFluidInCamera();
    if (type != FogType.NONE
        || (entity instanceof LivingEntity
        && ((LivingEntity) entity).hasEffect(MobEffects.BLINDNESS))
    ) {
      return;
    }

    DensityConfiguration density = config.density();
    DimensionType dimensionType = level.dimensionType();

    // Surface
    if (density.surface().get() && dimensionType.hasSkyLight()) {
      fogcustomizer$setFog(density.surfaceDensity().get(), density.surfaceDistance().get());
    }

    // Nether
    if (density.hell().get() && dimensionType.ultraWarm()) {
      fogcustomizer$setFog(density.hellDensity().get(), density.hellDistance().get());
    }

    // End
    if (density.end().get() && dimensionType.createDragonFight()) {
      fogcustomizer$setFog(density.endDensity().get(), density.endDistance().get());
    }
  }

  @Unique
  private static void fogcustomizer$setFog(int density, int distance) {
    int start = (int) (distance * (1 - density / 100.0F));
    RenderSystem.setShaderFogStart(Math.min(start, distance));
    RenderSystem.setShaderFogEnd(distance + 0.01F);
  }

  @Unique
  private static FogCustomizerConfiguration fogcustomizer$config() {
    if (fogcustomizer$configuration == null) {
      fogcustomizer$configuration = FogCustomizer.get().configuration();
    }
    return fogcustomizer$configuration;
  }


}
