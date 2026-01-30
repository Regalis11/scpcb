using SCPContainmentBreach.Core;

namespace SCPContainmentBreach;

/// <summary>
/// Точка входа в приложение.
/// SCP: Containment Breach - C# Port using Raylib
/// </summary>
internal static class Program
{
    public static void Main(string[] args)
    {
        Console.WriteLine("╔══════════════════════════════════════════════════════════╗");
        Console.WriteLine("║     SCP: Containment Breach - C# Raylib Port             ║");
        Console.WriteLine("║     Version: 0.1.0-alpha                                  ║");
        Console.WriteLine("╚══════════════════════════════════════════════════════════╝");
        Console.WriteLine();

        // Парсинг аргументов командной строки
        ParseArguments(args);

        try
        {
            using var game = new Game();
            game.Run();
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[FATAL] Unhandled exception: {ex}");

            // Записываем в лог-файл
            var logPath = Path.Combine(AppContext.BaseDirectory, "crash.log");
            File.WriteAllText(logPath, $"[{DateTime.Now}]\n{ex}");

            Console.WriteLine($"[FATAL] Crash log saved to: {logPath}");
            Environment.Exit(1);
        }

        Console.WriteLine("[Program] Exited normally");
    }

    private static void ParseArguments(string[] args)
    {
        for (int i = 0; i < args.Length; i++)
        {
            switch (args[i].ToLower())
            {
                case "-debug":
                case "--debug":
                    Console.WriteLine("[Program] Debug mode enabled");
                    break;

                case "-fullscreen":
                case "--fullscreen":
                    Console.WriteLine("[Program] Fullscreen mode requested");
                    // TODO: Передать в Game
                    break;

                case "-help":
                case "--help":
                case "-h":
                    PrintHelp();
                    Environment.Exit(0);
                    break;

                default:
                    if (!args[i].StartsWith('-'))
                    {
                        Console.WriteLine($"[Program] Unknown argument: {args[i]}");
                    }
                    break;
            }
        }
    }

    private static void PrintHelp()
    {
        Console.WriteLine("SCP: Containment Breach - C# Port");
        Console.WriteLine();
        Console.WriteLine("Usage: SCPContainmentBreach [options]");
        Console.WriteLine();
        Console.WriteLine("Options:");
        Console.WriteLine("  -debug, --debug       Enable debug mode");
        Console.WriteLine("  -fullscreen           Start in fullscreen mode");
        Console.WriteLine("  -help, -h             Show this help message");
        Console.WriteLine();
        Console.WriteLine("Controls:");
        Console.WriteLine("  WASD          - Move");
        Console.WriteLine("  Mouse         - Look around");
        Console.WriteLine("  Shift         - Sprint");
        Console.WriteLine("  Tab           - Toggle debug grid");
        Console.WriteLine("  F11           - Toggle fullscreen");
        Console.WriteLine("  Escape        - Menu / Exit");
    }
}
