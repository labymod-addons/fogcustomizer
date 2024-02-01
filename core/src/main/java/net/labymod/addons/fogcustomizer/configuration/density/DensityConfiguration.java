package net.labymod.addons.fogcustomizer.configuration.density;

import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingRequires;

@SpriteTexture("settings/fogcustomizer")
public class DensityConfiguration extends Config {

  @SpriteSlot(x = 1)
  @SwitchSetting
  private final ConfigProperty<Boolean> surface = new ConfigProperty<>(false);

  @SpriteSlot(x = 1)
  @SettingRequires("surface")
  @SliderSetting(min = 0, max = 100, steps = 5)
  private final ConfigProperty<Integer> surfaceDensity = new ConfigProperty<>(50);

  @SpriteSlot(x = 1)
  @SettingRequires("surface")
  @SliderSetting(min = 10, max = 600, steps = 10)
  private final ConfigProperty<Integer> surfaceDistance = new ConfigProperty<>(100);

  @SpriteSlot(x = 2)
  @SwitchSetting
  private final ConfigProperty<Boolean> hell = new ConfigProperty<>(false);

  @SpriteSlot(x = 2)
  @SettingRequires("hell")
  @SliderSetting(min = 0, max = 100, steps = 5)
  private final ConfigProperty<Integer> hellDensity = new ConfigProperty<>(50);

  @SpriteSlot(x = 2)
  @SettingRequires("hell")
  @SliderSetting(min = 10, max = 400, steps = 10)
  private final ConfigProperty<Integer> hellDistance = new ConfigProperty<>(100);

  @SpriteSlot(x = 3)
  @SwitchSetting
  private final ConfigProperty<Boolean> end = new ConfigProperty<>(false);

  @SpriteSlot(x = 3)
  @SettingRequires("end")
  @SliderSetting(min = 0, max = 100, steps = 5)
  private final ConfigProperty<Integer> endDensity = new ConfigProperty<>(50);

  @SpriteSlot(x = 3)
  @SettingRequires("end")
  @SliderSetting(min = 10, max = 400, steps = 10)
  private final ConfigProperty<Integer> endDistance = new ConfigProperty<>(100);

  public ConfigProperty<Boolean> surface() {
    return this.surface;
  }

  public ConfigProperty<Integer> surfaceDensity() {
    return this.surfaceDensity;
  }

  public ConfigProperty<Integer> surfaceDistance() {
    return this.surfaceDistance;
  }

  public ConfigProperty<Boolean> hell() {
    return this.hell;
  }

  public ConfigProperty<Integer> hellDensity() {
    return this.hellDensity;
  }

  public ConfigProperty<Integer> hellDistance() {
    return this.hellDistance;
  }

  public ConfigProperty<Boolean> end() {
    return this.end;
  }

  public ConfigProperty<Integer> endDensity() {
    return this.endDensity;
  }

  public ConfigProperty<Integer> endDistance() {
    return this.endDistance;
  }
}
