package club.revived.shared.translation;

import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record TranslationKey(@NotNull String key) {

  private static volatile TranslationEngine engine;

  public TranslationKey {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Translation key must not be null or blank");
    }
  }

  public static void setEngine(final @NotNull TranslationEngine translationEngine) {
    engine = translationEngine;
  }

  public static @Nullable TranslationEngine engine() {
    return engine;
  }

  public static @NotNull TranslationKey of(final @NotNull String key) {
    return new TranslationKey(key);
  }

  public @NotNull String translate() {
    if (engine == null) {
      return key;
    }
    return engine.translate(engine.registry().defaultLocale(), this);
  }

  public @NotNull String translate(final @NotNull Locale locale) {
    if (engine == null) {
      return key;
    }
    return engine.translate(locale, this);
  }

  public @NotNull String translate(final @NotNull Object... args) {
    if (engine == null) {
      return key;
    }
    return engine.translate(engine.registry().defaultLocale(), this, args);
  }

  public @NotNull String translate(final @NotNull Locale locale, final @NotNull Object... args) {
    if (engine == null) {
      return key;
    }
    return engine.translate(locale, this, args);
  }

  public @NotNull List<String> translateList() {
    if (engine == null) {
      return List.of(key);
    }
    return engine.translateList(engine.registry().defaultLocale(), this);
  }

  public @NotNull List<String> translateList(final @NotNull Locale locale) {
    if (engine == null) {
      return List.of(key);
    }
    return engine.translateList(locale, this);
  }

  public @NotNull List<String> translateList(final @NotNull Object... args) {
    if (engine == null) {
      return List.of(key);
    }
    return engine.translateList(engine.registry().defaultLocale(), this, args);
  }

  public @NotNull List<String> translateList(final @NotNull Locale locale, final @NotNull Object... args) {
    if (engine == null) {
      return List.of(key);
    }
    return engine.translateList(locale, this, args);
  }

  @Override
  public @NotNull String toString() {
    return key;
  }
}
