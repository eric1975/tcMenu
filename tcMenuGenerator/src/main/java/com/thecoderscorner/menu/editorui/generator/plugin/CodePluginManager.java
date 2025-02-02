/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import javafx.scene.image.Image;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * The code plugin manager is responsible for loading all code related plugins at runtime.
 * These plugins contribute to the generation of code during code generation. Each plugin
 * has a creator and descriptions associated with it.
 */
public interface CodePluginManager {
    /**
     * Load all available plugins in the core plugin directory and any others that have been configured.
     * @throws Exception if the plugins could not be loaded.
     */
    void loadPlugins() throws Exception;

    /**
     * Gets a list of all loaded plugins.
     * @return the list of plugins
     */
    List<CodePluginConfig> getLoadedPlugins();

    /**
     * Gets an image object from an image name
     * @param imageName the image name to load
     * @return an image object.
     */
    Optional<Image> getImageForName(CodePluginItem item, String imageName);

    /**
     * Gets a list of plugins filtered by the platform and the subsytem.
     * @param platform the platform to filter for
     * @param subSystem the subsystem to filter for
     * @return the list of filtered plugins
     */
    List<CodePluginItem> getPluginsThatMatch(EmbeddedPlatform platform, SubSystem subSystem);

    /**
     * Completely reload the plugins from disk using the last settings
     */
    void reload();

    /**
     * Get a specific plugin item by ID if it is available.
     * @param id the ID to find
     * @return the plugin if available, otherwise empty
     */
    Optional<CodePluginItem> getPluginById(String id);

    /**
     * @return any errors that occurred during the last load from disk
     */
    List<String> getLoadErrors();

    /**
     * Gets a list of all loaded top level plugin config names
     * @return a list of plugin names
     */
    List<String> getLoadedTopLevelPluginNames();
}
