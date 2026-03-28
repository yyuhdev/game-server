package club.revived.shared.inventory.config;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ItemConfig(
    @NotNull String material,
    int amount,
    @Nullable String name,
    @Nullable String nameKey,
    @Nullable List<String> lore,
    @Nullable String loreKey,
    @Nullable List<String> loreKeys,
    @Nullable Integer customModelData,
    @Nullable String skullOwner,
    @Nullable String skullTexture,
    boolean glowing,
    @Nullable List<String> flags,
    @NotNull Map<String, String> placeholders
) {

  public static @NotNull Builder builder(final @NotNull String material) {
    return new Builder(material);
  }

  public static final class Builder {

    private final @NotNull String material;
    private int amount = 1;
    private @Nullable String name;
    private @Nullable String nameKey;
    private @Nullable List<String> lore;
    private @Nullable String loreKey;
    private @Nullable List<String> loreKeys;
    private @Nullable Integer customModelData;
    private @Nullable String skullOwner;
    private @Nullable String skullTexture;
    private boolean glowing;
    private @Nullable List<String> flags;
    private @NotNull Map<String, String> placeholders = Map.of();

    private Builder(final @NotNull String material) {
      this.material = material;
    }

    public @NotNull Builder amount(final int amount) {
      this.amount = amount;
      return this;
    }

    public @NotNull Builder name(final @Nullable String name) {
      this.name = name;
      return this;
    }

    public @NotNull Builder nameKey(final @Nullable String nameKey) {
      this.nameKey = nameKey;
      return this;
    }

    public @NotNull Builder lore(final @Nullable List<String> lore) {
      this.lore = lore;
      return this;
    }

    public @NotNull Builder loreKey(final @Nullable String loreKey) {
      this.loreKey = loreKey;
      return this;
    }

    public @NotNull Builder loreKeys(final @Nullable List<String> loreKeys) {
      this.loreKeys = loreKeys;
      return this;
    }

    public @NotNull Builder customModelData(final @Nullable Integer customModelData) {
      this.customModelData = customModelData;
      return this;
    }

    public @NotNull Builder skullOwner(final @Nullable String skullOwner) {
      this.skullOwner = skullOwner;
      return this;
    }

    public @NotNull Builder skullTexture(final @Nullable String skullTexture) {
      this.skullTexture = skullTexture;
      return this;
    }

    public @NotNull Builder glowing(final boolean glowing) {
      this.glowing = glowing;
      return this;
    }

    public @NotNull Builder flags(final @Nullable List<String> flags) {
      this.flags = flags;
      return this;
    }

    public @NotNull Builder placeholders(final @NotNull Map<String, String> placeholders) {
      this.placeholders = placeholders;
      return this;
    }

    public @NotNull ItemConfig build() {
      return new ItemConfig(
          material, amount, name, nameKey, lore, loreKey, loreKeys,
          customModelData, skullOwner, skullTexture, glowing,
          flags, placeholders
      );
    }
  }
}
