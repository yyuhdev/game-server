package club.revived.shared.translation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

public final class Translation {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d+)}");

  private final Map<String, String> entries;
  private final Map<String, List<String>> listEntries;

  private Translation(
      final @NotNull Map<String, String> entries,
      final @NotNull Map<String, List<String>> listEntries
  ) {
    this.entries = new ConcurrentHashMap<>(entries);
    this.listEntries = new ConcurrentHashMap<>(listEntries);
  }

  public static @NotNull Translation of(final @NotNull Map<String, String> entries) {
    return new Translation(entries, Map.of());
  }

  public static @NotNull Translation of(
      final @NotNull Map<String, String> entries,
      final @NotNull Map<String, List<String>> listEntries
  ) {
    return new Translation(entries, listEntries);
  }

  public static @NotNull Translation empty() {
    return new Translation(Map.of(), Map.of());
  }

  public @NotNull Optional<String> get(final @NotNull TranslationKey key) {
    return Optional.ofNullable(entries.get(key.key()));
  }

  public @NotNull Optional<String> get(final @NotNull TranslationKey key, final @NotNull Object... args) {
    final var value = entries.get(key.key());
    if (value == null) {
      return Optional.empty();
    }
    return Optional.of(replacePlaceholders(value, args));
  }

  public @NotNull String getOrDefault(final @NotNull TranslationKey key, final @NotNull String defaultValue) {
    return entries.getOrDefault(key.key(), defaultValue);
  }

  public @NotNull String getOrDefault(
      final @NotNull TranslationKey key,
      final @NotNull String defaultValue,
      final @NotNull Object... args
  ) {
    final var value = entries.getOrDefault(key.key(), defaultValue);
    return replacePlaceholders(value, args);
  }

  public boolean has(final @NotNull TranslationKey key) {
    return entries.containsKey(key.key());
  }

  public boolean hasList(final @NotNull TranslationKey key) {
    return listEntries.containsKey(key.key());
  }

  public @NotNull Optional<List<String>> getList(final @NotNull TranslationKey key) {
    return Optional.ofNullable(listEntries.get(key.key()));
  }

  public @NotNull Optional<List<String>> getList(final @NotNull TranslationKey key, final @NotNull Object... args) {
    final var list = listEntries.get(key.key());
    if (list == null) {
      return Optional.empty();
    }
    return Optional.of(list.stream()
        .map(line -> replacePlaceholders(line, args))
        .toList());
  }

  public @NotNull List<String> getListOrDefault(
      final @NotNull TranslationKey key,
      final @NotNull List<String> defaultValue
  ) {
    return listEntries.getOrDefault(key.key(), defaultValue);
  }

  public int size() {
    return entries.size();
  }

  public int listSize() {
    return listEntries.size();
  }

  private @NotNull String replacePlaceholders(final @NotNull String template, final @NotNull Object... args) {
    if (args.length == 0) {
      return template;
    }

    final var matcher = PLACEHOLDER_PATTERN.matcher(template);
    final var result = new StringBuilder();

    while (matcher.find()) {
      final var index = Integer.parseInt(matcher.group(1));
      final var replacement = index < args.length ? String.valueOf(args[index]) : matcher.group();
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);

    return result.toString();
  }
}
