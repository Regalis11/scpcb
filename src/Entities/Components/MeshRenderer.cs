using Raylib_cs;
using System.Numerics;

namespace SCPContainmentBreach.Entities.Components;

/// <summary>
/// Компонент для рендеринга 3D моделей.
/// Аналог загруженного Mesh в Blitz3D (LoadMesh, LoadAnimMesh).
/// </summary>
public class MeshRenderer : ComponentBase, IDisposable
{
    private Model _model;
    private bool _modelLoaded;

    // Материал и цвет
    public Color Tint { get; set; } = Color.White;
    public bool CastShadows { get; set; } = true;
    public bool ReceiveShadows { get; set; } = true;

    // Видимость (как ShowEntity/HideEntity в Blitz3D)
    public bool Visible { get; set; } = true;

    // Альфа прозрачность (EntityAlpha в Blitz3D)
    public float Alpha { get; set; } = 1.0f;

    /// <summary>
    /// Загружает модель из файла
    /// </summary>
    public void LoadModel(string path)
    {
        if (_modelLoaded)
        {
            Raylib.UnloadModel(_model);
        }

        _model = Raylib.LoadModel(path);
        _modelLoaded = true;

        Console.WriteLine($"[MeshRenderer] Loaded model: {path}");
    }

    /// <summary>
    /// Устанавливает модель напрямую
    /// </summary>
    public void SetModel(Model model)
    {
        if (_modelLoaded)
        {
            Raylib.UnloadModel(_model);
        }

        _model = model;
        _modelLoaded = true;
    }

    /// <summary>
    /// Устанавливает текстуру для материала (EntityTexture в Blitz3D)
    /// </summary>
    public unsafe void SetTexture(Texture2D texture, int materialIndex = 0)
    {
        if (!_modelLoaded) return;

        if (materialIndex < _model.MaterialCount)
        {
            Raylib.SetMaterialTexture(ref _model.Materials[materialIndex],
                MaterialMapIndex.Albedo, texture);
        }
    }

    public override void Draw()
    {
        if (!_modelLoaded || !Visible) return;

        // Применяем альфа к цвету
        var color = new Color(
            Tint.R,
            Tint.G,
            Tint.B,
            (byte)(Tint.A * Alpha)
        );

        // Получаем трансформацию из Transform компонента
        Vector3 position = Transform.WorldPosition;
        Vector3 scale = Transform.WorldScale;

        // Извлекаем Euler углы для Raylib
        Vector3 rotationAxis = Vector3.UnitY;
        float rotationAngle = 0;

        // Упрощённый вариант - используем матрицу напрямую
        // Для более сложных случаев нужно извлекать из кватерниона
        var euler = Transform.LocalEulerAngles;

        Raylib.DrawModelEx(_model, position, rotationAxis, euler.Y, scale, color);
    }

    public void Dispose()
    {
        if (_modelLoaded)
        {
            Raylib.UnloadModel(_model);
            _modelLoaded = false;
        }

        GC.SuppressFinalize(this);
    }
}
