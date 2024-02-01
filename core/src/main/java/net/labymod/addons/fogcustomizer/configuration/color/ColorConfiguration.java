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

package net.labymod.addons.fogcustomizer.configuration.color;

import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget.ColorPickerSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingRequires;
import net.labymod.api.util.Color;

@SpriteTexture("settings/fogcustomizer")
public class ColorConfiguration extends Config {

  @SpriteSlot(x = 1)
  @SwitchSetting
  private final ConfigProperty<Boolean> surface = new ConfigProperty<>(false);

  @SpriteSlot(x = 1)
  @SettingRequires("surface")
  @ColorPickerSetting(chroma = true)
  private final ConfigProperty<Color> surfaceColor = new ConfigProperty<>(Color.WHITE);

  @SpriteSlot(x = 2)
  @SwitchSetting
  private final ConfigProperty<Boolean> hell = new ConfigProperty<>(false);

  @SpriteSlot(x = 2)
  @SettingRequires("hell")
  @ColorPickerSetting(chroma = true)
  private final ConfigProperty<Color> hellColor = new ConfigProperty<>(Color.WHITE);

  @SpriteSlot(x = 3)
  @SwitchSetting
  private final ConfigProperty<Boolean> end = new ConfigProperty<>(false);

  @SpriteSlot(x = 3)
  @SettingRequires("end")
  @ColorPickerSetting(chroma = true)
  private final ConfigProperty<Color> endColor = new ConfigProperty<>(Color.WHITE);

  @SpriteSlot(x = 1, y = 1)
  @SwitchSetting
  private final ConfigProperty<Boolean> water = new ConfigProperty<>(false);

  @SpriteSlot(x = 1, y = 1)
  @SettingRequires("water")
  @ColorPickerSetting(chroma = true)
  private final ConfigProperty<Color> waterColor = new ConfigProperty<>(Color.WHITE);

  @SpriteSlot(x = 2, y = 1)
  @SwitchSetting
  private final ConfigProperty<Boolean> lava = new ConfigProperty<>(false);

  @SpriteSlot(x = 2, y = 1)
  @SettingRequires("lava")
  @ColorPickerSetting(chroma = true)
  private final ConfigProperty<Color> lavaColor = new ConfigProperty<>(Color.WHITE);

  public ConfigProperty<Boolean> surface() {
    return this.surface;
  }

  public ConfigProperty<Color> surfaceColor() {
    return this.surfaceColor;
  }

  public ConfigProperty<Boolean> hell() {
    return this.hell;
  }

  public ConfigProperty<Color> hellColor() {
    return this.hellColor;
  }

  public ConfigProperty<Boolean> end() {
    return this.end;
  }

  public ConfigProperty<Color> endColor() {
    return this.endColor;
  }

  public ConfigProperty<Boolean> water() {
    return this.water;
  }

  public ConfigProperty<Color> waterColor() {
    return this.waterColor;
  }

  public ConfigProperty<Boolean> lava() {
    return this.lava;
  }

  public ConfigProperty<Color> lavaColor() {
    return this.lavaColor;
  }

}
