package club.revived.shared.translation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import club.revived.shared.result.Result;

public final class TranslationEngine {

  private static final Gson GSON = new Gson();

  private final TranslationRegistry registry;

  private TranslationEngine(final @NotNull TranslationRegistry registry) {
    this.registry = registry;
  }

  public static @NotNull TranslationEngine create(final @NotNull TranslationRegistry registry) {
    return new TranslationEngine(registry);
  }

  public static @NotNull TranslationEngine create(final @NotNull Locale defaultLocale) {
    return new TranslationEngine(TranslationRegistry.create(defaultLocale));
  }

  public static @NotNull TranslationEngine create() {
    return create(Locale.ENGLISH);
  }

  public @NotNull Result<Translation, TranslationLoadError> loadFromClasspath(
      final @NotNull String resourcePath,
      final @NotNull Locale locale) {
    try (final var stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {

      if (stream == null) {
        return Result.err(new TranslationLoadError.ResourceNotFound(resourcePath));
      }

      return this.loadFromStream(stream, locale);
    } catch (final IOException e) {
      return Result.err(new TranslationLoadError.IoError(e));
    }
  }

  public @NotNull Result<Translation, TranslationLoadError> loadFromFile(
      final @NotNull Path path,
      final @NotNull Locale locale) {
    if (!Files.exists(path)) {
      return Result.err(new TranslationLoadError.ResourceNotFound(path.toString()));
    }

    try (final var stream = Files.newInputStream(path)) {
      return loadFromStream(stream, locale);
    } catch (final IOException e) {
      return Result.err(new TranslationLoadError.IoError(e));
    }
  }

  public @NotNull CompletableFuture<Result<Translation, TranslationLoadError>> loadFromClasspathAsync(
      final @NotNull String resourcePath,
      final @NotNull Locale locale) {
    return CompletableFuture.supplyAsync(() -> loadFromClasspath(resourcePath, locale));
  }

  public @NotNull CompletableFuture<Result<Translation, TranslationLoadError>> loadFromFileAsync(
      final @NotNull Path path,
      final @NotNull Locale locale) {
    return CompletableFuture.supplyAsync(() -> loadFromFile(path, locale));
  }

  public @NotNull Result<Translation, TranslationLoadError> loadAndRegisterFromClasspath(
      final @NotNull String resourcePath,
      final @NotNull Locale locale) {
    final var result = loadFromClasspath(resourcePath, locale);
    result.ifOk(translation -> registry.register(locale, translation));
    return result;
  }

  public @NotNull Result<Translation, TranslationLoadError> loadAndRegisterFromFile(
      final @NotNull Path path,
      final @NotNull Locale locale) {
    final var result = loadFromFile(path, locale);
    result.ifOk(translation -> registry.register(locale, translation));
    return result;
  }

  public @NotNull TranslationRegistry registry() {
    return registry;
  }

  public @NotNull String translate(final @NotNull Locale locale, final @NotNull TranslationKey key) {
    return registry.translate(locale, key);
  }

  public @NotNull String translate(
      final @NotNull Locale locale,
      final @NotNull TranslationKey key,
      final @NotNull Object... args) {
    return registry.translate(locale, key, args);
  }

  public @NotNull List<String> translateList(final @NotNull Locale locale, final @NotNull TranslationKey key) {
    return registry.translateList(locale, key);
  }

  public @NotNull List<String> translateList(
      final @NotNull Locale locale,
      final @NotNull TranslationKey key,
      final @NotNull Object... args) {
    return registry.translateList(locale, key, args);
  }

  private @NotNull Result<Translation, TranslationLoadError> loadFromStream(
      final @NotNull InputStream stream,
      final @NotNull Locale locale) {
    try (final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      final var json = GSON.fromJson(reader, JsonObject.class);
      if (json == null) {
        return Result.err(new TranslationLoadError.ParseError("Invalid JSON structure"));
      }

      final var entries = new HashMap<String, String>();
      final var listEntries = new HashMap<String, List<String>>();
      flattenJson(json, "", entries, listEntries);
      final var translation = Translation.of(entries, listEntries);
      return Result.ok(translation);
    } catch (final Exception e) {
      return Result.err(new TranslationLoadError.ParseError(e.getMessage()));
    }
  }

  private void flattenJson(
      final @NotNull JsonObject json,
      final @NotNull String prefix,
      final @NotNull Map<String, String> entries,
      final @NotNull Map<String, List<String>> listEntries
  ) {
    for (final var entry : json.entrySet()) {
      final var key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
      final var value = entry.getValue();

      if (value.isJsonObject()) {
        flattenJson(value.getAsJsonObject(), key, entries, listEntries);
      } else if (value.isJsonArray()) {
        final var list = new ArrayList<String>();
        for (final var element : value.getAsJsonArray()) {
          if (element.isJsonPrimitive()) {
            list.add(element.getAsString());
          }
        }
        listEntries.put(key, list);
      } else if (value.isJsonPrimitive()) {
        entries.put(key, value.getAsString());
      }
    }
  }

  public sealed interface TranslationLoadError {

    record ResourceNotFound(@NotNull String path) implements TranslationLoadError {
    }

    record IoError(@NotNull IOException cause) implements TranslationLoadError {
    }

    record ParseError(@NotNull String message) implements TranslationLoadError {
    }
  }
}
