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
package net.labymod.addons.fogcustomizer.configuration;

import net.labymod.addons.fogcustomizer.configuration.color.ColorConfiguration;
import net.labymod.addons.fogcustomizer.configuration.density.DensityConfiguration;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@SpriteTexture("settings/fogcustomizer")
@SuppressWarnings("FieldMayBeFinal")
public class FogCustomizerConfiguration extends AddonConfig {

  @SpriteSlot(x = 0)
  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @SpriteSlot(x = 0, y = 1)
  private ColorConfiguration color = new ColorConfiguration();

  @SpriteSlot(x = 0, y = 2)
  private DensityConfiguration density = new DensityConfiguration();

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ColorConfiguration color() {
    return this.color;
  }

  public DensityConfiguration density() {
    return this.density;
  }

}
