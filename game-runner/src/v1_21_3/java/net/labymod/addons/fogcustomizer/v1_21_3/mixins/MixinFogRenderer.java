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

package net.labymod.addons.fogcustomizer.v1_21_3.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.FogShape;
import net.labymod.addons.fogcustomizer.FogCustomizer;
import net.labymod.addons.fogcustomizer.configuration.FogCustomizerConfiguration;
import net.labymod.addons.fogcustomizer.configuration.color.ColorConfiguration;
import net.labymod.addons.fogcustomizer.configuration.density.DensityConfiguration;
import net.labymod.api.util.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

  @Unique
  private static FogCustomizerConfiguration fogcustomizer$configuration;

  private static Color fogCustomizer$fluidFogColor;

  @Dynamic
  @Redirect(
      method = "lambda$computeFogColor$0",
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
      method = "computeFogColor",
      at = @At("HEAD")
  )
  private static void fogCustomizer$setFluidFogColor(
      Camera camera,
      float partialTicks,
      ClientLevel world,
      int renderDistance,
      float darkenWorldAmount,
      CallbackInfoReturnable<Vector4f> cir
  ) {
    FogCustomizerConfiguration config = fogcustomizer$config();
    if (!config.enabled().get()) {
      MixinFogRenderer.fogCustomizer$fluidFogColor = null;
      return;
    }

    FogType type = camera.getFluidInCamera();
    if (type == FogType.WATER) {
      ColorConfiguration colors = config.color();

      // Water
      if (colors.water().get()) {
        MixinFogRenderer.fogCustomizer$fluidFogColor = colors.waterColor().get();
      }
    } else if (type == FogType.LAVA) {
      ColorConfiguration colors = config.color();

      // Lava
      if (colors.lava().get()) {
        MixinFogRenderer.fogCustomizer$fluidFogColor = colors.lavaColor().get();
      } else {
        MixinFogRenderer.fogCustomizer$fluidFogColor = null;
      }
    }
  }

  @ModifyVariable(
      method = "computeFogColor",
      index = 7,
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;"
              + "getClearColorScale()F",
          shift = At.Shift.AFTER
      )
  )
  private static float fogCustomizer$applyRedFogColor(float value) {
    if (MixinFogRenderer.fogCustomizer$fluidFogColor != null) {
      return MixinFogRenderer.fogCustomizer$fluidFogColor.getRed() / 255.0F;
    }

    return value;
  }

  @ModifyVariable(
      method = "computeFogColor",
      index = 8,
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;"
              + "getClearColorScale()F",
          shift = At.Shift.AFTER
      )
  )
  private static float fogCustomizer$applyGreenFogColor(float value) {
    if (MixinFogRenderer.fogCustomizer$fluidFogColor != null) {
      return MixinFogRenderer.fogCustomizer$fluidFogColor.getGreen() / 255.0F;
    }

    return value;
  }

  @ModifyVariable(
      method = "computeFogColor",
      index = 9,
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;"
              + "getClearColorScale()F",
          shift = At.Shift.AFTER
      )
  )
  private static float fogCustomizer$applyBlueFogColor(float value) {
    if (MixinFogRenderer.fogCustomizer$fluidFogColor != null) {
      return MixinFogRenderer.fogCustomizer$fluidFogColor.getBlue() / 255.0F;
    }

    return value;
  }

  @WrapOperation(
      method = "setupFog",
      at = @At(
          value = "NEW",
          target = "(FFLcom/mojang/blaze3d/shaders/FogShape;FFFF)"
              + "Lnet/minecraft/client/renderer/FogParameters;"
      )
  )
  private static FogParameters fogCustomizer$setupFog(
      float $$0, float $$1, FogShape $$2, float $$3, float $$4, float $$5, float $$6,
      Operation<FogParameters> original, Camera camera
  ) {
    FogCustomizerConfiguration fogCustomizerConfig = fogcustomizer$config();
    ClientLevel level = Minecraft.getInstance().level;
    if (!fogCustomizerConfig.enabled().get() || level == null) {
      return original.call($$0, $$1, $$2, $$3, $$4, $$5, $$6);
    }

    Entity entity = camera.getEntity();
    FogType type = camera.getFluidInCamera();
    if (type != FogType.NONE
        || (entity instanceof LivingEntity
        && ((LivingEntity) entity).hasEffect(MobEffects.BLINDNESS))
    ) {
      return original.call($$0, $$1, $$2, $$3, $$4, $$5, $$6);
    }

    DensityConfiguration config = fogCustomizerConfig.density();
    ResourceKey<Level> dimensionType = level.dimension();

    float density = Float.MIN_VALUE;
    float distance = Float.MIN_VALUE;
    if (dimensionType == Level.OVERWORLD && config.surface().get()) {
      density = config.surfaceDensity().get();
      distance = config.surfaceDistance().get();
    } else if (dimensionType == Level.NETHER && config.hell().get()) {
      density = config.hellDensity().get();
      distance = config.hellDistance().get();
    } else if (dimensionType == Level.END && config.end().get()) {
      density = config.endDensity().get();
      distance = config.endDistance().get();
    }

    if (density == Float.MIN_VALUE || distance == Float.MIN_VALUE) {
      return original.call($$0, $$1, $$2, $$3, $$4, $$5, $$6);
    }

    float start = (float) (int) (distance * (1 - density / 100.0F));
    return original.call(start, distance + 0.01F, $$2, $$3, $$4, $$5, $$6);
  }

  @Unique
  private static FogCustomizerConfiguration fogcustomizer$config() {
    if (fogcustomizer$configuration == null) {
      fogcustomizer$configuration = FogCustomizer.get().configuration();
    }
    return fogcustomizer$configuration;
  }
}
