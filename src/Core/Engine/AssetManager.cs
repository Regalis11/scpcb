/*
 * Source: Engine Core
 * Type: AssetManager
 * Purpose: Texture/Model loading with caching and path normalization
 */

using System;
using System.Collections.Generic;
using System.IO;
using Raylib_cs;

namespace SCPContainmentBreach.Core.Engine;

public static class AssetManager
{
    private static readonly Dictionary<string, Texture2D> TextureCache = new();
    private static readonly Dictionary<string, Model> ModelCache = new();
    private static readonly Dictionary<string, Image> ImageCache = new();

    private static string _basePath = "";

    public static void Initialize(string basePath)
    {
        _basePath = NormalizePath(basePath);
        if (!string.IsNullOrEmpty(_basePath) && !_basePath.EndsWith('/'))
        {
            _basePath += '/';
        }
    }

    /// <summary>
    /// Normalize Blitz3D backslash paths to forward slashes
    /// </summary>
    public static string NormalizePath(string path)
    {
        if (string.IsNullOrEmpty(path))
            return path;

        // Blitz3D uses backslashes: GFX\map\door.b3d -> GFX/map/door.b3d
        return path.Replace('\\', '/');
    }

    /// <summary>
    /// Get full path combining base path with asset path
    /// </summary>
    private static string GetFullPath(string relativePath)
    {
        string normalized = NormalizePath(relativePath);

        // Handle absolute paths
        if (Path.IsPathRooted(normalized))
            return normalized;

        return _basePath + normalized;
    }

    /// <summary>
    /// LoadTexture - Blitz3D equivalent
    /// flags: 1=color, 2=alpha, 4=masked, 8=mipmap, 16=clamp_u, 32=clamp_v, 64=sphere_env
    /// </summary>
    public static Texture2D LoadTexture(string path, int flags = 1)
    {
        string fullPath = GetFullPath(path);
        string cacheKey = $"{fullPath}:{flags}";

        if (TextureCache.TryGetValue(cacheKey, out Texture2D cached))
            return cached;

        if (!File.Exists(fullPath))
        {
            Console.WriteLine($"[AssetManager] WARNING: Texture not found: {fullPath}");
            return default;
        }

        try
        {
            Image img = Raylib.LoadImage(fullPath);

            // Blitz3D flag 2 = alpha channel
            // Blitz3D flag 4 = masked (color key transparency)
            if ((flags & 4) != 0)
            {
                // Masked: treat black as transparent
                Raylib.ImageColorReplace(ref img, Color.Black, new Color(0, 0, 0, 0));
            }

            Texture2D texture = Raylib.LoadTextureFromImage(img);

            // Blitz3D flag 8 = mipmaps
            if ((flags & 8) != 0)
            {
                Raylib.GenTextureMipmaps(ref texture);
            }

            // Blitz3D flag 16 = clamp U, flag 32 = clamp V
            if ((flags & 16) != 0 || (flags & 32) != 0)
            {
                Raylib.SetTextureWrap(texture, TextureWrap.Clamp);
            }
            else
            {
                Raylib.SetTextureWrap(texture, TextureWrap.Repeat);
            }

            // Linear filtering by default
            Raylib.SetTextureFilter(texture, TextureFilter.Bilinear);

            Raylib.UnloadImage(img);

            TextureCache[cacheKey] = texture;
            return texture;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[AssetManager] ERROR loading texture {fullPath}: {ex.Message}");
            return default;
        }
    }

    /// <summary>
    /// LoadImage - for UI/2D graphics
    /// </summary>
    public static Image LoadImage(string path)
    {
        string fullPath = GetFullPath(path);

        if (ImageCache.TryGetValue(fullPath, out Image cached))
            return cached;

        if (!File.Exists(fullPath))
        {
            Console.WriteLine($"[AssetManager] WARNING: Image not found: {fullPath}");
            return default;
        }

        try
        {
            Image img = Raylib.LoadImage(fullPath);
            ImageCache[fullPath] = img;
            return img;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[AssetManager] ERROR loading image {fullPath}: {ex.Message}");
            return default;
        }
    }

    /// <summary>
    /// LoadMesh - Blitz3D equivalent (static mesh)
    /// </summary>
    public static Model LoadMesh(string path)
    {
        string fullPath = GetFullPath(path);

        if (ModelCache.TryGetValue(fullPath, out Model cached))
            return cached;

        if (!File.Exists(fullPath))
        {
            Console.WriteLine($"[AssetManager] WARNING: Model not found: {fullPath}");
            return default;
        }

        try
        {
            Model model = Raylib.LoadModel(fullPath);
            ModelCache[fullPath] = model;
            return model;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[AssetManager] ERROR loading model {fullPath}: {ex.Message}");
            return default;
        }
    }

    /// <summary>
    /// LoadAnimMesh - Blitz3D equivalent (animated mesh)
    /// Note: Raylib handles animation differently, model contains animation data
    /// </summary>
    public static Model LoadAnimMesh(string path)
    {
        // In Raylib, LoadModel handles both static and animated meshes
        // Animation data is extracted separately with LoadModelAnimations
        return LoadMesh(path);
    }

    /// <summary>
    /// Load model animations separately
    /// </summary>
    public static unsafe ModelAnimation[] LoadAnimations(string path)
    {
        string fullPath = GetFullPath(path);

        if (!File.Exists(fullPath))
        {
            Console.WriteLine($"[AssetManager] WARNING: Animation file not found: {fullPath}");
            return Array.Empty<ModelAnimation>();
        }

        try
        {
            int animCount = 0;
            ModelAnimation* anims = Raylib.LoadModelAnimations(fullPath, ref animCount);

            if (animCount == 0 || anims == null)
                return Array.Empty<ModelAnimation>();

            ModelAnimation[] result = new ModelAnimation[animCount];
            for (int i = 0; i < animCount; i++)
            {
                result[i] = anims[i];
            }

            return result;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[AssetManager] ERROR loading animations {fullPath}: {ex.Message}");
            return Array.Empty<ModelAnimation>();
        }
    }

    /// <summary>
    /// Check if texture is loaded and valid
    /// </summary>
    public static bool IsTextureValid(Texture2D texture)
    {
        return texture.Id > 0;
    }

    /// <summary>
    /// Check if model is loaded and valid
    /// </summary>
    public static bool IsModelValid(Model model)
    {
        return model.MeshCount > 0;
    }

    /// <summary>
    /// Free specific texture from cache
    /// </summary>
    public static void FreeTexture(string path, int flags = 1)
    {
        string fullPath = GetFullPath(path);
        string cacheKey = $"{fullPath}:{flags}";

        if (TextureCache.TryGetValue(cacheKey, out Texture2D texture))
        {
            Raylib.UnloadTexture(texture);
            TextureCache.Remove(cacheKey);
        }
    }

    /// <summary>
    /// Free specific model from cache
    /// </summary>
    public static void FreeModel(string path)
    {
        string fullPath = GetFullPath(path);

        if (ModelCache.TryGetValue(fullPath, out Model model))
        {
            Raylib.UnloadModel(model);
            ModelCache.Remove(fullPath);
        }
    }

    /// <summary>
    /// Unload all cached assets
    /// </summary>
    public static void UnloadAll()
    {
        foreach (var texture in TextureCache.Values)
        {
            Raylib.UnloadTexture(texture);
        }
        TextureCache.Clear();

        foreach (var model in ModelCache.Values)
        {
            Raylib.UnloadModel(model);
        }
        ModelCache.Clear();

        foreach (var image in ImageCache.Values)
        {
            Raylib.UnloadImage(image);
        }
        ImageCache.Clear();
    }

    /// <summary>
    /// Get cache statistics for debugging
    /// </summary>
    public static (int textures, int models, int images) GetCacheStats()
    {
        return (TextureCache.Count, ModelCache.Count, ImageCache.Count);
    }
}
