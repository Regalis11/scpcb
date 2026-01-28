/*
 * Source: Main.bb (lines 4748-4790, 3268-3295, 5245-5420)
 * Type: HUD Rendering System
 */

using System;
using System.Numerics;
using Raylib_cs;
using SCPContainmentBreach.Core.Types;
using SCPContainmentBreach.Core.Systems;
using SCPContainmentBreach.Core.Engine;

namespace SCPContainmentBreach.Core.UI;

public static class HUDSystem
{
    private static Texture2D _blinkMeterImg;
    private static Texture2D _blinkIcon;
    private static Texture2D _staminaMeterImg;
    private static Texture2D _sprintIcon;
    private static Texture2D _crouchIcon;
    private static Texture2D _handIcon;
    private static Texture2D _handIcon2;
    private static Texture2D _cursorImg;
    private static Texture2D _invFrameWhite;
    private static Texture2D _invFrameBlack;

    private static Font _font;
    private static bool _initialized;

    private const int MeterWidth = 204;
    private const int MeterHeight = 20;
    private const int MeterSegmentWidth = 10;
    private const int IconSize = 30;
    private const int IconOffset = 50;

    private const int InvSlotWidth = 70;
    private const int InvSlotHeight = 70;
    private const int InvSlotSpacing = 35;

    public static bool HUDEnabled { get; set; } = true;
    public static bool DebugHUD { get; set; }

    public static void Initialize()
    {
        if (_initialized) return;

        _blinkMeterImg = AssetManager.LoadTexture("GFX/blinkmeter.jpg");
        _blinkIcon = AssetManager.LoadTexture("GFX/blinkicon.png");
        _staminaMeterImg = AssetManager.LoadTexture("GFX/staminameter.jpg");
        _sprintIcon = AssetManager.LoadTexture("GFX/sprinticon.png");
        _crouchIcon = AssetManager.LoadTexture("GFX/sneakicon.png");
        _handIcon = AssetManager.LoadTexture("GFX/handsymbol.png");
        _handIcon2 = AssetManager.LoadTexture("GFX/handsymbol2.png");
        _cursorImg = AssetManager.LoadTexture("GFX/cursor.png");
        _invFrameWhite = AssetManager.LoadTexture("GFX/menuwhite.jpg");
        _invFrameBlack = AssetManager.LoadTexture("GFX/menublack.jpg");

        _font = Raylib.GetFontDefault();

        _initialized = true;
    }

    public static void Draw(PlayerState player, MenuState menu)
    {
        if (!_initialized) return;

        int screenWidth = Raylib.GetScreenWidth();
        int screenHeight = Raylib.GetScreenHeight();

        if (HUDEnabled && player.KillTimer >= 0)
        {
            DrawBlinkMeter(player, screenWidth, screenHeight);
            DrawStaminaMeter(player, screenWidth, screenHeight);
        }

        DrawInteractionIcon(screenWidth, screenHeight, menu);
        DrawMessage(menu, screenWidth, screenHeight);

        if (menu.InvOpen != 0)
        {
            DrawInventory(player, menu, screenWidth, screenHeight);
        }

        if (DebugHUD)
        {
            DrawDebugInfo(player, screenWidth, screenHeight);
        }
    }

    private static void DrawBlinkMeter(PlayerState player, int screenWidth, int screenHeight)
    {
        int x = 80;
        int y = screenHeight - 95;

        Raylib.DrawRectangleLines(x, y, MeterWidth, MeterHeight, Color.White);

        float blinkRatio = player.BLINKFREQ > 0 ? player.BlinkTimer / player.BLINKFREQ : 0;
        int segmentCount = (int)(((MeterWidth - 2) * blinkRatio) / MeterSegmentWidth);

        for (int i = 1; i <= segmentCount; i++)
        {
            if (AssetManager.IsTextureValid(_blinkMeterImg))
            {
                Raylib.DrawTexture(_blinkMeterImg, x + 3 + MeterSegmentWidth * (i - 1), y + 3, Color.White);
            }
            else
            {
                Raylib.DrawRectangle(x + 3 + MeterSegmentWidth * (i - 1), y + 3, 8, 14, Color.SkyBlue);
            }
        }

        Raylib.DrawRectangle(x - IconOffset, y, IconSize, IconSize, Color.Black);

        if (player.EyeIrritation > 0)
        {
            Raylib.DrawRectangleLinesEx(
                new Rectangle(x - IconOffset - 3, y - 3, IconSize + 6, IconSize + 6),
                2, new Color(200, 0, 0, 255));
        }

        Raylib.DrawRectangleLines(x - IconOffset - 1, y - 1, IconSize + 2, IconSize + 2, Color.White);

        if (AssetManager.IsTextureValid(_blinkIcon))
        {
            Raylib.DrawTexture(_blinkIcon, x - IconOffset, y, Color.White);
        }
    }

    private static void DrawStaminaMeter(PlayerState player, int screenWidth, int screenHeight)
    {
        int x = 80;
        int y = screenHeight - 55;

        Raylib.DrawRectangleLines(x, y, MeterWidth, MeterHeight, Color.White);

        float staminaRatio = player.Stamina / 100.0f;
        int segmentCount = (int)(((MeterWidth - 2) * staminaRatio) / MeterSegmentWidth);

        for (int i = 1; i <= segmentCount; i++)
        {
            if (AssetManager.IsTextureValid(_staminaMeterImg))
            {
                Raylib.DrawTexture(_staminaMeterImg, x + 3 + MeterSegmentWidth * (i - 1), y + 3, Color.White);
            }
            else
            {
                Raylib.DrawRectangle(x + 3 + MeterSegmentWidth * (i - 1), y + 3, 8, 14, Color.Green);
            }
        }

        Raylib.DrawRectangle(x - IconOffset, y, IconSize, IconSize, Color.Black);
        Raylib.DrawRectangleLines(x - IconOffset - 1, y - 1, IconSize + 2, IconSize + 2, Color.White);

        if (player.Crouch != 0)
        {
            if (AssetManager.IsTextureValid(_crouchIcon))
            {
                Raylib.DrawTexture(_crouchIcon, x - IconOffset, y, Color.White);
            }
        }
        else
        {
            if (AssetManager.IsTextureValid(_sprintIcon))
            {
                Raylib.DrawTexture(_sprintIcon, x - IconOffset, y, Color.White);
            }
        }
    }

    private static void DrawInteractionIcon(int screenWidth, int screenHeight, MenuState menu)
    {
        if (!InteractionSystem.HasTarget)
        {
            if (menu.DrawHandIcon != 0)
            {
                int iconX = screenWidth / 2 - 32;
                int iconY = screenHeight / 2 - 32;
                if (AssetManager.IsTextureValid(_handIcon))
                {
                    Raylib.DrawTexture(_handIcon, iconX, iconY, Color.White);
                }
            }
            return;
        }

        var target = InteractionSystem.CurrentTarget;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        Texture2D iconToUse = target.Type == InteractableType.Item ? _handIcon2 : _handIcon;

        if (AssetManager.IsTextureValid(iconToUse))
        {
            Raylib.DrawTexture(iconToUse, centerX - 32, centerY - 32, Color.White);
        }
    }

    private static void DrawMessage(MenuState menu, int screenWidth, int screenHeight)
    {
        if (menu.MsgTimer <= 0 || string.IsNullOrEmpty(menu.Msg))
            return;

        float alpha = MathF.Min(menu.MsgTimer / 2.0f, 255.0f) / 255.0f;
        byte alphaByte = (byte)(alpha * 255);

        int x = screenWidth / 2;
        int y = screenHeight / 2 + 200;

        int fontSize = 20;
        int textWidth = Raylib.MeasureText(menu.Msg, fontSize);

        Raylib.DrawText(menu.Msg, x - textWidth / 2 + 1, y + 1,
            fontSize, new Color(0, 0, 0, alphaByte));

        Color textColor = new Color(255, 255, 255, alphaByte);
        if (menu.Msg.StartsWith("Blitz3D Error!"))
        {
            textColor = new Color(255, 0, 0, alphaByte);
        }

        Raylib.DrawText(menu.Msg, x - textWidth / 2, y, fontSize, textColor);
    }

    public static void DrawInventory(PlayerState player, MenuState menu, int screenWidth, int screenHeight)
    {
        int startX = screenWidth / 2 - (InvSlotWidth * InventorySystem.MaxItemAmount / 2 + InvSlotSpacing * (InventorySystem.MaxItemAmount / 2 - 1)) / 2;
        int startY = screenHeight / 2 - InvSlotHeight;

        int x = startX;
        int y = startY;

        Vector2 mousePos = Raylib.GetMousePosition();
        int hoveredSlot = -1;

        for (int n = 0; n < InventorySystem.MaxItemAmount; n++)
        {
            bool isMouseOn = mousePos.X > x && mousePos.X < x + InvSlotWidth &&
                             mousePos.Y > y && mousePos.Y < y + InvSlotHeight;

            if (isMouseOn)
                hoveredSlot = n;

            var item = InventorySystem.GetItem(n);

            if (item != null)
            {
                DrawEquippedHighlight(player, item, x, y);
            }

            if (isMouseOn)
            {
                Raylib.DrawRectangleLinesEx(
                    new Rectangle(x - 1, y - 1, InvSlotWidth + 2, InvSlotHeight + 2),
                    1, Color.Red);
            }

            DrawSlotFrame(x, y, InvSlotWidth, InvSlotHeight);

            if (item != null)
            {
                if (InventorySystem.SelectedItem != item || isMouseOn)
                {
                    DrawItemIcon(item, x + InvSlotWidth / 2 - 32, y + InvSlotHeight / 2 - 32);
                }

                if (isMouseOn && InventorySystem.SelectedItem == null)
                {
                    string itemName = item.name;
                    int textWidth = Raylib.MeasureText(itemName, 16);
                    int textX = x + InvSlotWidth / 2 - textWidth / 2;
                    int textY = y + InvSlotHeight + InvSlotSpacing - 15;

                    Raylib.DrawText(itemName, textX + 1, textY + 1, 16, Color.Black);
                    Raylib.DrawText(itemName, textX, textY, 16, Color.White);
                }
            }

            x += InvSlotWidth + InvSlotSpacing;
            if (n == 4)
            {
                y += InvSlotHeight * 2;
                x = startX;
            }
        }

        if (InventorySystem.SelectedItem != null && Raylib.IsMouseButtonDown(MouseButton.Left))
        {
            DrawItemIcon(InventorySystem.SelectedItem,
                (int)mousePos.X - 32, (int)mousePos.Y - 32);
        }

        if (AssetManager.IsTextureValid(_cursorImg))
        {
            Raylib.DrawTexture(_cursorImg, (int)mousePos.X, (int)mousePos.Y, Color.White);
        }
    }

    private static void DrawEquippedHighlight(PlayerState player, Items item, int x, int y)
    {
        string tempName = item.itemtemplate?.tempname ?? "";

        bool isEquipped = tempName switch
        {
            "gasmask" => player.WearingGasMask == 1,
            "supergasmask" => player.WearingGasMask == 2,
            "gasmask3" => player.WearingGasMask == 3,
            "hazmatsuit" => player.WearingHazmat == 1,
            "hazmatsuit2" => player.WearingHazmat == 2,
            "hazmatsuit3" => player.WearingHazmat == 3,
            "vest" => player.WearingVest == 1,
            "finevest" => player.WearingVest == 2,
            "scp714" => player.Wearing714 == 1,
            "nvgoggles" => player.WearingNightVision == 1,
            "supernv" => player.WearingNightVision == 2,
            "finenvgoggles" => player.WearingNightVision == 3,
            _ => false
        };

        if (isEquipped)
        {
            Raylib.DrawRectangle(x - 3, y - 3, InvSlotWidth + 6, InvSlotHeight + 6,
                new Color(200, 200, 200, 255));
        }
    }

    private static void DrawSlotFrame(int x, int y, int width, int height)
    {
        if (AssetManager.IsTextureValid(_invFrameWhite))
        {
            Rectangle srcRect = new Rectangle(0, 0, width, height);
            Rectangle destRect = new Rectangle(x, y, width, height);
            Raylib.DrawTexturePro(_invFrameWhite, srcRect, destRect, Vector2.Zero, 0, Color.White);

            srcRect = new Rectangle(0, 0, width - 6, height - 6);
            destRect = new Rectangle(x + 3, y + 3, width - 6, height - 6);
            Raylib.DrawTexturePro(_invFrameBlack, srcRect, destRect, Vector2.Zero, 0, Color.White);
        }
        else
        {
            Raylib.DrawRectangleLines(x, y, width, height, Color.White);
            Raylib.DrawRectangle(x + 3, y + 3, width - 6, height - 6, Color.Black);
        }
    }

    private static void DrawItemIcon(Items item, int x, int y)
    {
        if (item.invimg != 0)
        {
            // TODO: Draw actual item texture when item system has textures loaded
        }

        Raylib.DrawRectangle(x, y, 64, 64, Color.DarkGray);
        Raylib.DrawRectangleLines(x, y, 64, 64, Color.Gray);

        string shortName = item.name.Length > 8 ? item.name.Substring(0, 8) : item.name;
        int textWidth = Raylib.MeasureText(shortName, 10);
        Raylib.DrawText(shortName, x + 32 - textWidth / 2, y + 27, 10, Color.White);
    }

    private static void DrawDebugInfo(PlayerState player, int screenWidth, int screenHeight)
    {
        int x = 30;
        int y = 50;
        int lineHeight = 20;
        int fontSize = 16;

        Raylib.DrawText($"Player Position: ({player.X:F3}, {player.Y:F3}, {player.Z:F3})",
            x, y, fontSize, Color.White);

        Raylib.DrawText($"Player Rotation: (Pitch: {player.Pitch:F3}, Yaw: {player.Yaw:F3})",
            x, y + lineHeight, fontSize, Color.White);

        Raylib.DrawText($"Stamina: {player.Stamina:F1} | Blink: {player.BlinkTimer:F1}/{player.BLINKFREQ:F1}",
            x, y + lineHeight * 2, fontSize, Color.White);

        Raylib.DrawText($"Injuries: {player.Injuries:F1} | Bloodloss: {player.Bloodloss:F1}",
            x, y + lineHeight * 3, fontSize, Color.White);

        if (player.PlayerRoom != null)
        {
            Raylib.DrawText($"Room: {player.PlayerRoom.RoomTemplate?.Name ?? "Unknown"}",
                x, y + lineHeight * 4, fontSize, Color.White);
        }
    }

    public static void UpdateMessage(MenuState menu, float fpsFactor)
    {
        if (menu.MsgTimer > 0)
        {
            menu.MsgTimer -= fpsFactor;
        }
    }

    public static void ShowMessage(MenuState menu, string msg, float duration = 350.0f)
    {
        menu.Msg = msg;
        menu.MsgTimer = duration;
    }

    public static void HandleInventoryInput(PlayerState player, MenuState menu)
    {
        if (menu.InvOpen == 0) return;

        int screenWidth = Raylib.GetScreenWidth();
        int screenHeight = Raylib.GetScreenHeight();

        int startX = screenWidth / 2 - (InvSlotWidth * InventorySystem.MaxItemAmount / 2 + InvSlotSpacing * (InventorySystem.MaxItemAmount / 2 - 1)) / 2;
        int startY = screenHeight / 2 - InvSlotHeight;

        Vector2 mousePos = Raylib.GetMousePosition();

        int x = startX;
        int y = startY;
        int mouseSlot = -1;

        for (int n = 0; n < InventorySystem.MaxItemAmount; n++)
        {
            bool isMouseOn = mousePos.X > x && mousePos.X < x + InvSlotWidth &&
                             mousePos.Y > y && mousePos.Y < y + InvSlotHeight;

            if (isMouseOn)
            {
                mouseSlot = n;
                break;
            }

            x += InvSlotWidth + InvSlotSpacing;
            if (n == 4)
            {
                y += InvSlotHeight * 2;
                x = startX;
            }
        }

        if (Raylib.IsMouseButtonPressed(MouseButton.Left))
        {
            if (mouseSlot >= 0)
            {
                var clickedItem = InventorySystem.GetItem(mouseSlot);
                if (clickedItem != null && InventorySystem.SelectedItem == null)
                {
                    InventorySystem.SelectedItem = clickedItem;
                }
            }
        }

        if (Raylib.IsMouseButtonReleased(MouseButton.Left))
        {
            if (InventorySystem.SelectedItem != null)
            {
                if (mouseSlot == -1)
                {
                    string tempName = InventorySystem.SelectedItem.itemtemplate?.tempname ?? "";
                    if (tempName != "vest" && tempName != "finevest" &&
                        tempName != "hazmatsuit" && tempName != "hazmatsuit2" && tempName != "hazmatsuit3")
                    {
                        Vector3 dropPos = new Vector3(player.X, player.Y, player.Z);
                        InventorySystem.DropItem(player, InventorySystem.SelectedItem, dropPos, player.Yaw);
                        InventorySystem.SelectedItem = null;
                        menu.InvOpen = 0;
                    }
                    else
                    {
                        ShowMessage(menu, "Double click on this item to take it off.", 350.0f);
                    }
                }
                else
                {
                    var targetItem = InventorySystem.GetItem(mouseSlot);
                    if (targetItem == null)
                    {
                        SwapItemSlot(InventorySystem.SelectedItem, mouseSlot);
                    }
                }
                InventorySystem.SelectedItem = null;
            }
        }

        if (Raylib.IsMouseButtonPressed(MouseButton.Right))
        {
            menu.InvOpen = 0;
        }
    }

    private static void SwapItemSlot(Items item, int targetSlot)
    {
        int sourceSlot = InventorySystem.FindItemSlot(item);
        if (sourceSlot < 0) return;

        // Direct array manipulation would require exposing internals
        // For now, this is a placeholder for slot swapping logic
    }

    public static void Unload()
    {
        if (!_initialized) return;

        if (AssetManager.IsTextureValid(_blinkMeterImg)) Raylib.UnloadTexture(_blinkMeterImg);
        if (AssetManager.IsTextureValid(_blinkIcon)) Raylib.UnloadTexture(_blinkIcon);
        if (AssetManager.IsTextureValid(_staminaMeterImg)) Raylib.UnloadTexture(_staminaMeterImg);
        if (AssetManager.IsTextureValid(_sprintIcon)) Raylib.UnloadTexture(_sprintIcon);
        if (AssetManager.IsTextureValid(_crouchIcon)) Raylib.UnloadTexture(_crouchIcon);
        if (AssetManager.IsTextureValid(_handIcon)) Raylib.UnloadTexture(_handIcon);
        if (AssetManager.IsTextureValid(_handIcon2)) Raylib.UnloadTexture(_handIcon2);
        if (AssetManager.IsTextureValid(_cursorImg)) Raylib.UnloadTexture(_cursorImg);
        if (AssetManager.IsTextureValid(_invFrameWhite)) Raylib.UnloadTexture(_invFrameWhite);
        if (AssetManager.IsTextureValid(_invFrameBlack)) Raylib.UnloadTexture(_invFrameBlack);

        _initialized = false;
    }
}
