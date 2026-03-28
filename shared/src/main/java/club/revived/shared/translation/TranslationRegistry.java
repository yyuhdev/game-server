package club.revived.shared.translation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

public final class TranslationRegistry {

  private final Map<Locale, Translation> translations;
  private final Locale defaultLocale;

  private TranslationRegistry(final @NotNull Locale defaultLocale) {
    this.translations = new ConcurrentHashMap<>();
    this.defaultLocale = defaultLocale;
  }

  public static @NotNull TranslationRegistry create(final @NotNull Locale defaultLocale) {
    return new TranslationRegistry(defaultLocale);
  }

  public static @NotNull TranslationRegistry create() {
    return create(Locale.ENGLISH);
  }

  public void register(final @NotNull Locale locale, final @NotNull Translation translation) {
    translations.put(locale, translation);
  }

  public @NotNull Optional<Translation> get(final @NotNull Locale locale) {
    return Optional.ofNullable(translations.get(locale));
  }

  public @NotNull Translation getOrDefault(final @NotNull Locale locale) {
    return translations.getOrDefault(locale, translations.get(defaultLocale));
  }

  public @NotNull String translate(final @NotNull Locale locale, final @NotNull TranslationKey key) {
    final var translation = getOrDefault(locale);
    if (translation == null) {
      return key.key();
    }
    return translation.get(key).orElse(key.key());
  }

  public @NotNull String translate(
      final @NotNull Locale locale,
      final @NotNull TranslationKey key,
      final @NotNull Object... args
  ) {
    final var translation = getOrDefault(locale);
    if (translation == null) {
      return key.key();
    }
    return translation.get(key, args).orElse(key.key());
  }

  public @NotNull List<String> translateList(final @NotNull Locale locale, final @NotNull TranslationKey key) {
    final var translation = getOrDefault(locale);
    if (translation == null) {
      return List.of(key.key());
    }
    return translation.getList(key).orElse(List.of(key.key()));
  }

  public @NotNull List<String> translateList(
      final @NotNull Locale locale,
      final @NotNull TranslationKey key,
      final @NotNull Object... args
  ) {
    final var translation = getOrDefault(locale);
    if (translation == null) {
      return List.of(key.key());
    }
    return translation.getList(key, args).orElse(List.of(key.key()));
  }

  public @NotNull Locale defaultLocale() {
    return defaultLocale;
  }

  public boolean hasLocale(final @NotNull Locale locale) {
    return translations.containsKey(locale);
  }
}
