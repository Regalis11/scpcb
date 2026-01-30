using System.Numerics;
using System.Text.RegularExpressions;

namespace SCPContainmentBreach.Legacy;

/// <summary>
/// Парсер для чтения данных из оригинальных файлов SCP:CB.
/// Поддерживает:
/// - .bb файлы (Blitz3D исходный код) - для извлечения констант и конфигураций
/// - .ini файлы (конфигурация комнат, настройки)
/// - .b3d файлы (3D модели Blitz3D формата)
/// - Карты из Map Creator
///
/// ПРИМЕЧАНИЕ: Это заглушка. Полная реализация будет добавлена позже.
/// </summary>
public class LegacyParser
{
    private readonly string _basePath;

    public LegacyParser(string basePath)
    {
        _basePath = basePath;
    }

    #region INI Parser (rooms.ini, options.ini)

    /// <summary>
    /// Парсит .ini файл в словарь секций
    /// </summary>
    public Dictionary<string, Dictionary<string, string>> ParseIniFile(string relativePath)
    {
        var result = new Dictionary<string, Dictionary<string, string>>();
        var fullPath = Path.Combine(_basePath, relativePath);

        if (!File.Exists(fullPath))
        {
            Console.WriteLine($"[LegacyParser] INI file not found: {fullPath}");
            return result;
        }

        string currentSection = "default";
        result[currentSection] = new Dictionary<string, string>();

        foreach (var line in File.ReadAllLines(fullPath))
        {
            var trimmed = line.Trim();

            // Пропускаем пустые строки и комментарии
            if (string.IsNullOrEmpty(trimmed) || trimmed.StartsWith(';'))
                continue;

            // Секция [SectionName]
            if (trimmed.StartsWith('[') && trimmed.EndsWith(']'))
            {
                currentSection = trimmed[1..^1];
                if (!result.ContainsKey(currentSection))
                {
                    result[currentSection] = new Dictionary<string, string>();
                }
                continue;
            }

            // Ключ = Значение
            var equalsIndex = trimmed.IndexOf('=');
            if (equalsIndex > 0)
            {
                var key = trimmed[..equalsIndex].Trim();
                var value = trimmed[(equalsIndex + 1)..].Trim();
                result[currentSection][key] = value;
            }
        }

        Console.WriteLine($"[LegacyParser] Parsed INI: {relativePath} ({result.Count} sections)");
        return result;
    }

    #endregion

    #region Room Definition Parser

    /// <summary>
    /// Структура описания комнаты из rooms.ini
    /// </summary>
    public record RoomDefinition(
        string Name,
        string MeshPath,
        int Shape,           // 0=эндкап, 1=коридор, 2=угол, 3=T-перекрёсток, 4=крест
        bool Commonness,
        bool Large,
        string? Zone,        // LCZ, HCZ, EZ
        string? DisableDecals
    );

    /// <summary>
    /// Парсит rooms.ini и возвращает список комнат
    /// </summary>
    public List<RoomDefinition> ParseRoomsConfig()
    {
        var rooms = new List<RoomDefinition>();
        var ini = ParseIniFile("Data/rooms.ini");

        foreach (var section in ini)
        {
            if (section.Key == "default") continue;

            var props = section.Value;
            var room = new RoomDefinition(
                Name: section.Key,
                MeshPath: props.GetValueOrDefault("mesh path", ""),
                Shape: int.Parse(props.GetValueOrDefault("shape", "0")),
                Commonness: props.GetValueOrDefault("commonness", "0") != "0",
                Large: props.GetValueOrDefault("large", "0") != "0",
                Zone: props.GetValueOrDefault("zone"),
                DisableDecals: props.GetValueOrDefault("disabledecals")
            );
            rooms.Add(room);
        }

        Console.WriteLine($"[LegacyParser] Loaded {rooms.Count} room definitions");
        return rooms;
    }

    #endregion

    #region Blitz3D Source Parser (для констант)

    /// <summary>
    /// Извлекает константы (Const) из .bb файла
    /// </summary>
    public Dictionary<string, object> ExtractConstants(string relativePath)
    {
        var constants = new Dictionary<string, object>();
        var fullPath = Path.Combine(_basePath, relativePath);

        if (!File.Exists(fullPath))
        {
            Console.WriteLine($"[LegacyParser] BB file not found: {fullPath}");
            return constants;
        }

        var content = File.ReadAllText(fullPath);

        // Паттерн для Const NAME = VALUE или Const NAME# = VALUE
        var constRegex = new Regex(@"Const\s+(\w+)([#%$]?)\s*=\s*(.+?)(?:;|$)", RegexOptions.Multiline);

        foreach (Match match in constRegex.Matches(content))
        {
            var name = match.Groups[1].Value;
            var typeHint = match.Groups[2].Value;
            var valueStr = match.Groups[3].Value.Trim();

            object value;
            try
            {
                value = typeHint switch
                {
                    "#" => float.Parse(valueStr),
                    "%" => int.Parse(valueStr),
                    "$" => valueStr.Trim('"'),
                    _ => ParseAutoType(valueStr)
                };
                constants[name] = value;
            }
            catch
            {
                // Пропускаем сложные выражения
            }
        }

        Console.WriteLine($"[LegacyParser] Extracted {constants.Count} constants from {relativePath}");
        return constants;
    }

    private static object ParseAutoType(string value)
    {
        if (int.TryParse(value, out var intVal)) return intVal;
        if (float.TryParse(value, out var floatVal)) return floatVal;
        return value.Trim('"');
    }

    /// <summary>
    /// Извлекает глобальные переменные (Global) из .bb файла
    /// </summary>
    public Dictionary<string, string> ExtractGlobals(string relativePath)
    {
        var globals = new Dictionary<string, string>();
        var fullPath = Path.Combine(_basePath, relativePath);

        if (!File.Exists(fullPath))
        {
            return globals;
        }

        var content = File.ReadAllText(fullPath);

        // Паттерн для Global NAME = VALUE или Global NAME
        var globalRegex = new Regex(@"Global\s+(\w+)([#%$]?)(?:\s*=\s*(.+?))?(?:;|$)", RegexOptions.Multiline);

        foreach (Match match in globalRegex.Matches(content))
        {
            var name = match.Groups[1].Value;
            var value = match.Groups[3].Success ? match.Groups[3].Value.Trim() : "";
            globals[name] = value;
        }

        return globals;
    }

    #endregion

    #region B3D Model Parser (Placeholder)

    /// <summary>
    /// Структура для хранения данных B3D модели.
    /// B3D - бинарный формат Blitz3D для 3D моделей.
    /// </summary>
    public class B3DModel
    {
        public List<Vector3> Vertices { get; } = new();
        public List<Vector3> Normals { get; } = new();
        public List<Vector2> UVs { get; } = new();
        public List<int> Indices { get; } = new();
        public List<string> Textures { get; } = new();

        // Анимация (если есть)
        public List<B3DBone> Bones { get; } = new();
        public List<B3DAnimation> Animations { get; } = new();
    }

    public class B3DBone
    {
        public string Name { get; set; } = "";
        public int ParentIndex { get; set; } = -1;
        public Vector3 Position { get; set; }
        public Quaternion Rotation { get; set; }
        public Vector3 Scale { get; set; } = Vector3.One;
    }

    public class B3DAnimation
    {
        public string Name { get; set; } = "";
        public float Length { get; set; }
        public List<B3DKeyframe> Keyframes { get; } = new();
    }

    public class B3DKeyframe
    {
        public float Time { get; set; }
        public int BoneIndex { get; set; }
        public Vector3 Position { get; set; }
        public Quaternion Rotation { get; set; }
        public Vector3 Scale { get; set; }
    }

    /// <summary>
    /// Парсит .b3d модель (PLACEHOLDER - требует полной реализации)
    /// </summary>
    public B3DModel? ParseB3DModel(string relativePath)
    {
        var fullPath = Path.Combine(_basePath, relativePath);

        if (!File.Exists(fullPath))
        {
            Console.WriteLine($"[LegacyParser] B3D file not found: {fullPath}");
            return null;
        }

        // TODO: Реализовать полный парсинг B3D формата
        // B3D формат: https://moddb.fandom.com/wiki/B3D
        //
        // Структура файла:
        // - BB3D chunk (header)
        //   - TEXS chunk (textures)
        //   - BRUS chunk (brushes/materials)
        //   - NODE chunk (scene graph)
        //     - MESH chunk (geometry)
        //     - BONE chunk (skeleton)
        //     - KEYS chunk (animation)
        //     - ANIM chunk (animation sequences)

        Console.WriteLine($"[LegacyParser] B3D parsing not implemented yet: {relativePath}");
        return new B3DModel();
    }

    #endregion

    #region Map Parser (Placeholder)

    /// <summary>
    /// Структура карты
    /// </summary>
    public class MapData
    {
        public int Width { get; set; }
        public int Height { get; set; }
        public MapCell[,]? Cells { get; set; }
        public List<MapEvent> Events { get; } = new();
    }

    public class MapCell
    {
        public string? RoomName { get; set; }
        public int Rotation { get; set; } // 0, 90, 180, 270
        public bool IsForestGrid { get; set; }
    }

    public class MapEvent
    {
        public string Name { get; set; } = "";
        public Vector3 Position { get; set; }
        public Dictionary<string, string> Properties { get; } = new();
    }

    /// <summary>
    /// Парсит карту из Map Creator (PLACEHOLDER)
    /// </summary>
    public MapData? ParseMap(string mapPath)
    {
        var fullPath = Path.Combine(_basePath, mapPath);

        if (!File.Exists(fullPath))
        {
            Console.WriteLine($"[LegacyParser] Map file not found: {fullPath}");
            return null;
        }

        // TODO: Реализовать парсинг формата карт Map Creator
        Console.WriteLine($"[LegacyParser] Map parsing not implemented yet: {mapPath}");
        return new MapData { Width = 0, Height = 0 };
    }

    #endregion

    #region Utility

    /// <summary>
    /// Получает список всех .b3d файлов в директории
    /// </summary>
    public IEnumerable<string> FindB3DFiles(string relativePath = "GFX")
    {
        var fullPath = Path.Combine(_basePath, relativePath);

        if (!Directory.Exists(fullPath))
        {
            yield break;
        }

        foreach (var file in Directory.EnumerateFiles(fullPath, "*.b3d", SearchOption.AllDirectories))
        {
            yield return Path.GetRelativePath(_basePath, file);
        }
    }

    /// <summary>
    /// Получает список всех звуковых файлов
    /// </summary>
    public IEnumerable<string> FindSoundFiles(string relativePath = "SFX")
    {
        var fullPath = Path.Combine(_basePath, relativePath);

        if (!Directory.Exists(fullPath))
        {
            yield break;
        }

        var extensions = new[] { "*.ogg", "*.wav", "*.mp3" };

        foreach (var ext in extensions)
        {
            foreach (var file in Directory.EnumerateFiles(fullPath, ext, SearchOption.AllDirectories))
            {
                yield return Path.GetRelativePath(_basePath, file);
            }
        }
    }

    #endregion
}
