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

package net.labymod.addons.fogcustomizer.v1_21_10.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.nio.ByteBuffer;
import net.labymod.addons.fogcustomizer.FogCustomizer;
import net.labymod.addons.fogcustomizer.configuration.FogCustomizerConfiguration;
import net.labymod.addons.fogcustomizer.configuration.color.ColorConfiguration;
import net.labymod.addons.fogcustomizer.configuration.density.DensityConfiguration;
import net.labymod.api.util.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Since 1.21.6 the fog is no longer a {@code FogParameters} record: the colour is the return value
 * of computeFogColor and the four bounds only exist together in the private buffer write setupFog
 * ends with, so that write is where the density override belongs.
 */
@Mixin(FogRenderer.class)
public class MixinFogRenderer {

  @Unique
  private static FogCustomizerConfiguration fogcustomizer$configuration;

  @Inject(method = "computeFogColor", at = @At("RETURN"))
  private void fogcustomizer$applyFogColor(
      Camera camera, float partialTicks, ClientLevel level, int renderDistance,
      float darkenWorldAmount, boolean isFoggy,
      CallbackInfoReturnable<Vector4f> callback
  ) {
    Color color = this.fogcustomizer$fogColor(camera, level);
    if (color == null) {
      return;
    }

    Vector4f target = callback.getReturnValue();
    target.set(
        color.getRed() / 255.0F,
        color.getGreen() / 255.0F,
        color.getBlue() / 255.0F,
        target.w
    );
  }

  @WrapOperation(
      method = "setupFog",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer"
              + "(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
      )
  )
  private void fogcustomizer$applyFogDensity(
      FogRenderer instance, ByteBuffer buffer, int offset, Vector4f fogColor,
      float environmentalStart, float environmentalEnd, float renderDistanceStart,
      float renderDistanceEnd, float skyEnd, float cloudEnd, Operation<Void> original, Camera camera
  ) {
    FogCustomizerConfiguration configuration = fogcustomizer$config();
    ClientLevel level = Minecraft.getInstance().level;
    if (!configuration.enabled().get() || level == null) {
      original.call(instance, buffer, offset, fogColor, environmentalStart, environmentalEnd,
          renderDistanceStart, renderDistanceEnd, skyEnd, cloudEnd);
      return;
    }

    // The sliders describe the plain view distance fog, so anything that replaces it with its own
    // fog (a fluid, blindness) keeps the fog the game picked.
    Entity entity = camera.getEntity();
    if (camera.getFluidInCamera() != FogType.NONE
        || (entity instanceof LivingEntity livingEntity
        && livingEntity.hasEffect(MobEffects.BLINDNESS))
    ) {
      original.call(instance, buffer, offset, fogColor, environmentalStart, environmentalEnd,
          renderDistanceStart, renderDistanceEnd, skyEnd, cloudEnd);
      return;
    }

    DensityConfiguration density = configuration.density();
    ResourceKey<Level> dimension = level.dimension();

    int percentage;
    int distance;
    if (dimension == Level.OVERWORLD && density.surface().get()) {
      percentage = density.surfaceDensity().get();
      distance = density.surfaceDistance().get();
    } else if (dimension == Level.NETHER && density.hell().get()) {
      percentage = density.hellDensity().get();
      distance = density.hellDistance().get();
    } else if (dimension == Level.END && density.end().get()) {
      percentage = density.endDensity().get();
      distance = density.endDistance().get();
    } else {
      original.call(instance, buffer, offset, fogColor, environmentalStart, environmentalEnd,
          renderDistanceStart, renderDistanceEnd, skyEnd, cloudEnd);
      return;
    }

    float start = (float) (int) (distance * (1.0F - percentage / 100.0F));
    float end = distance + 0.01F;

    // Both ranges are replaced: before 1.21.6 there was only one, and leaving the environmental one
    // untouched would let the biome fog outweigh a lowered density.
    original.call(instance, buffer, offset, fogColor, start, end, start, end, skyEnd, cloudEnd);
  }

  @Unique
  private Color fogcustomizer$fogColor(Camera camera, ClientLevel level) {
    FogCustomizerConfiguration configuration = fogcustomizer$config();
    if (!configuration.enabled().get() || level == null) {
      return null;
    }

    ColorConfiguration colors = configuration.color();
    FogType fogType = camera.getFluidInCamera();
    if (fogType == FogType.WATER) {
      return colors.water().get() ? colors.waterColor().get() : null;
    }

    if (fogType == FogType.LAVA) {
      return colors.lava().get() ? colors.lavaColor().get() : null;
    }

    if (fogType != FogType.NONE) {
      return null;
    }

    ResourceKey<Level> dimension = level.dimension();
    if (dimension == Level.OVERWORLD && colors.surface().get()) {
      return colors.surfaceColor().get();
    }

    if (dimension == Level.NETHER && colors.hell().get()) {
      return colors.hellColor().get();
    }

    if (dimension == Level.END && colors.end().get()) {
      return colors.endColor().get();
    }

    return null;
  }

  @Unique
  private static FogCustomizerConfiguration fogcustomizer$config() {
    if (fogcustomizer$configuration == null) {
      fogcustomizer$configuration = FogCustomizer.get().configuration();
    }

    return fogcustomizer$configuration;
  }
}
