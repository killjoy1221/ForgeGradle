/*
 * A Gradle plugin for the creation of Minecraft mods and MinecraftForge plugins.
 * Copyright (C) 2013 Minecraft Forge
 *
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package net.minecraftforge.gradle.util.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.minecraftforge.gradle.util.json.LiteLoaderJson.VersionObject;
import net.minecraftforge.gradle.util.json.fgversion.FGVersionDeserializer;
import net.minecraftforge.gradle.util.json.fgversion.FGVersionWrapper;
import net.minecraftforge.gradle.util.json.forgeversion.ForgeArtifact;
import net.minecraftforge.gradle.util.json.forgeversion.ForgeArtifactAdapter;
import net.minecraftforge.gradle.util.json.version.AssetIndex;
import net.minecraftforge.gradle.util.json.version.Version;

public class JsonFactory
{
    public static final Gson GSON;

    static
    {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new EnumAdaptorFactory());
        builder.registerTypeAdapter(Date.class, new DateAdapter());
        builder.registerTypeAdapter(File.class, new FileAdapter());
        builder.registerTypeAdapter(VersionObject.class, new LiteLoaderJson.VersionAdapter());
        builder.registerTypeAdapter(ForgeArtifact.class, new ForgeArtifactAdapter());
        builder.registerTypeAdapter(FGVersionWrapper.class, new FGVersionDeserializer());
        builder.enableComplexMapKeySerialization();
        builder.setPrettyPrinting();
        GSON = builder.create();
    }

    public static Version loadVersion(File json, File... inheritanceDirs) throws JsonSyntaxException, JsonIOException, IOException
    {
        FileReader reader = new FileReader(json);
        Version v = GSON.fromJson(reader, Version.class);
        reader.close();

        if (!Strings.isNullOrEmpty(v.inheritsFrom))
        {
            boolean found = false;

            for (File inheritDir : inheritanceDirs)
            {
                File parentFile = new File(inheritDir, v.inheritsFrom + ".json");

                if (parentFile.exists())
                {
                    Version parent = loadVersion(new File(inheritDir, v.inheritsFrom + ".json"), inheritanceDirs);
                    v.extendFrom(parent);
                    break;
                }
            }

            // still didnt find the inherited
            if (!found)
            {
                throw new FileNotFoundException("Inherited json file (" + v.inheritsFrom + ") not found! Maybe you are running in offline mode?");
            }
        }

        return v;
    }

    public static AssetIndex loadAssetsIndex(File json) throws JsonSyntaxException, JsonIOException, IOException
    {
        FileReader reader = new FileReader(json);
        AssetIndex a = GSON.fromJson(reader, AssetIndex.class);
        reader.close();
        return a;
    }

    public static LiteLoaderJson loadLiteLoaderJson(File json) throws JsonSyntaxException, JsonIOException, IOException
    {
        FileReader reader = new FileReader(json);
        LiteLoaderJson a = GSON.fromJson(reader, LiteLoaderJson.class);
        reader.close();
        return a;
    }

    public static Map<String, MCInjectorStruct> loadMCIJson(File json) throws IOException
    {
        FileReader reader = new FileReader(json);
        Map<String, MCInjectorStruct> ret = new LinkedHashMap<String, MCInjectorStruct>();

        JsonObject object = (JsonObject) new JsonParser().parse(reader);
        reader.close();

        for (Entry<String, JsonElement> entry : object.entrySet())
        {
            ret.put(entry.getKey(), GSON.fromJson(entry.getValue(), MCInjectorStruct.class));
        }
        return ret;
    }
}
