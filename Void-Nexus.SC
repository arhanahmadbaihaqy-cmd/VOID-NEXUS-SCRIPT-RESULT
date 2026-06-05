-- ======================================================================================================== --
--                               V.O.I.D // NEXUS - NEON FUTURISTIC EDITION                                 --
--                                     PATCH V4: ULTIMATE DEPLOYMENT [FULL]                                 --
-- ======================================================================================================== --

local Players = game:GetService("Players")
local RunService = game:GetService("RunService")
local UserInputService = game:GetService("UserInputService")
local TweenService = game:GetService("TweenService")
local Lighting = game:GetService("Lighting")
local StarterGui = game:GetService("StarterGui")
local LocalPlayer = Players.LocalPlayer
local Camera = workspace.CurrentCamera

-- ============================================================================
-- SMART DAILY KEY SYSTEM WITH SESSION SAVING
-- ============================================================================
local function generateDailyKey()
    local days = math.floor(os.time() / 86400)
    local seed = days
    local function rng()
        seed = (seed * 9301 + 49297) % 233280
        return seed / 233280
    end
    local r1 = math.floor(rng() * 9000 + 1000)
    local r2 = math.floor(rng() * 9000 + 1000)
    return "VOIDNX-" .. r1 .. "-" .. r2
end

local CorrectKeyToday = generateDailyKey()
local SaveFileName = "VoidNexus_Session_" .. math.floor(os.time() / 86400) .. ".txt"

local function CheckSavedSession()
    if readfile and isfile then
        if isfile(SaveFileName) then
            local content = readfile(SaveFileName)
            if content == CorrectKeyToday then
                return true
            end
        end
    end
    return false
end

local function SaveSession()
    if writefile then
        pcall(function()
            writefile(SaveFileName, CorrectKeyToday)
        end)
    end
end

-- ============================================================================
-- GLOBAL SCRIPT CONFIGURATION STRUCTURE (AUTOMATED DEFAULT SETUP)
-- ============================================================================
local Config = {
    KeySystem = {
        Enabled = true,
        Verified = CheckSavedSession(),
        MonetizedLink = "https://sfl.gl/S130"
    },
    DiscordLink = "https://discord.gg/voidnexus",
    
    -- Movement Exploits
    SpeedActive = false, SpeedValue = 100,
    JumpActive = false, JumpValue = 100,
    FlyActive = false, FlyValue = 100,
    NoclipActive = false,
    SpinBotActive = false, SpinSpeed = 25,
    
    -- Visual / ESP Systems
    BoxEspActive = false,
    TracerActive = false,
    AuraEspActive = false,
    HitboxActive = false, HitboxSize = 15,
    FullBrightActive = false,
    NoFogActive = false,
    
    -- Combat / Aimbot Mechanics (FIXED INSTANT READY)
    AimActive = false,
    ShowFov = false,
    FovRadius = 150,
    AimWallActive = false, 
    AimPart = "Head",      
    
    -- Target & Teleport Systems
    StalkerActive = false,
    TargetPlayerName = "",
    
    -- Game Optimization & Booster
    PotatoModeActive = false,
    FpsUnlockerActive = false,
    
    -- Custom Theme & Visual Coloring (UPGRADED TO HIGH-TECH NEON BLUE)
    BoxColor = Color3.fromRGB(0, 102, 255),
    TracerColor = Color3.fromRGB(0, 102, 255),
    AuraColor = Color3.fromRGB(0, 180, 255),
    FovColor = Color3.fromRGB(0, 102, 255)
}

local OriginalLighting = {
    Ambient = Lighting.Ambient,
    OutdoorAmbient = Lighting.OutdoorAmbient,
    Brightness = Lighting.Brightness,
    FogEnd = Lighting.FogEnd,
    GlobalShadows = Lighting.GlobalShadows,
}

local savedMaterials = {}
local ScriptRunning = true
local Esp = {}

-- ============================================================================
-- UTILITY CORE FUNCTIONS
-- ============================================================================
local function SendNotification(title, text, duration)
    pcall(function()
        StarterGui:SetCore("SendNotification", {
            Title = title,
            Text = text,
            Icon = "rbxassetid://9714151241",
            Duration = duration or 5
        })
    end)
end

local function GetChar()
    local char = LocalPlayer.Character
    if not char then return nil, nil end
    return char:FindFirstChildOfClass("Humanoid"), char:FindFirstChild("HumanoidRootPart")
end

local function GetPlayerByName(name)
    if not name or name == "" then return nil end
    name = string.lower(name)
    for _, p in pairs(Players:GetPlayers()) do
        if string.sub(string.lower(p.Name), 1, #name) == name or string.sub(string.lower(p.DisplayName), 1, #name) == name then
            return p
        end
    end
    return nil
end

local function GetAimPartInstance(character, partName)
    if partName == "Torso" then
        return character:FindFirstChild("Torso") or character:FindFirstChild("UpperTorso") or character:FindFirstChild("LowerTorso")
    end
    return character:FindFirstChild(partName)
end

local function ApplyPotatoMode(state)
    pcall(function()
        if state then
            Lighting.GlobalShadows = false
            Lighting.FogEnd = 100
            Lighting.Brightness = 1.5
            for _, obj in pairs(workspace:GetDescendants()) do
                if obj:IsA("BasePart") and not obj:IsA("Terrain") then
                    if not savedMaterials[obj] then savedMaterials[obj] = {Material = obj.Material, Color = obj.Color} end
                    obj.Material = Enum.Material.SmoothPlastic
                    obj.Reflectance = 0
                elseif obj:IsA("Texture") or obj:IsA("Decal") then obj.Transparency = 1
                elseif obj:IsA("ParticleEmitter") or obj:IsA("Fire") or obj:IsA("Smoke") or obj:IsA("Sparkles") then obj.Enabled = false
                elseif obj:IsA("PostEffect") or obj:IsA("Atmosphere") or obj:IsA("Sky") then obj.Enabled = false
                end
            end
        else
            Lighting.GlobalShadows = OriginalLighting.GlobalShadows
            Lighting.FogEnd = OriginalLighting.FogEnd
            Lighting.Brightness = OriginalLighting.Brightness
            for obj, data in pairs(savedMaterials) do
                if obj and obj.Parent then obj.Material = data.Material; obj.Color = data.Color end
            end
            savedMaterials = {}
        end
    end)
end

-- ============================================================================
-- DRAWING API FOR TARGET LOCK & VISUAL ESP
-- ============================================================================
local FovCircle
pcall(function()
    FovCircle = Drawing.new("Circle")
    FovCircle.Color = Config.FovColor
    FovCircle.Thickness = 2
    FovCircle.Radius = Config.FovRadius
    FovCircle.Visible = false
end)

local function CreateEsp(p)
    if p == LocalPlayer then return end
    if Esp[p] then pcall(function() Esp[p].Box:Destroy(); Esp[p].Tracer:Destroy() end) end
    Esp[p] = {Box = Drawing.new("Square"), Tracer = Drawing.new("Line"), Highlight = nil}
    Esp[p].Box.Color = Config.BoxColor
    Esp[p].Box.Thickness = 1.5
    Esp[p].Box.Visible = false
    Esp[p].Tracer.Color = Config.TracerColor
    Esp[p].Tracer.Thickness = 1.2
    Esp[p].Tracer.Visible = false
end
for _, p in pairs(Players:GetPlayers()) do CreateEsp(p) end
Players.PlayerAdded:Connect(CreateEsp)

Players.PlayerRemoving:Connect(function(p)
    if Esp[p] then
        pcall(function()
            Esp[p].Box.Visible = false
            Esp[p].Tracer.Visible = false
            Esp[p].Box:Destroy()
            Esp[p].Tracer:Destroy()
            if Esp[p].Highlight then Esp[p].Highlight:Destroy() end
        end)
        Esp[p] = nil
    end
end)

-- ============================================================================
-- MAIN INTERFACE GRAPHICS SETUP (HIGH-TECH NEON BLUE THEME)
-- ============================================================================
local ScreenGui = Instance.new("ScreenGui")
ScreenGui.Name = "VoidNexus_NeonEdition"
ScreenGui.ResetOnSpawn = false
if gethui then ScreenGui.Parent = gethui() else pcall(function() ScreenGui.Parent = LocalPlayer:WaitForChild("PlayerGui") end) end

local Main = Instance.new("Frame")
Main.Size = UDim2.new(0, 580, 0, 620)
Main.Position = UDim2.new(0.5, -290, 0.5, -310)
Main.BackgroundColor3 = Color3.fromRGB(6, 7, 14)
Main.BackgroundTransparency = 0.1
Main.ClipsDescendants = true
Main.Visible = false
Main.Parent = ScreenGui
Instance.new("UICorner", Main).CornerRadius = UDim.new(0, 14)

local MainStroke = Instance.new("UIStroke", Main)
MainStroke.Color = Color3.fromRGB(0, 102, 255) 
MainStroke.Thickness = 2
MainStroke.ApplyStrokeMode = Enum.ApplyStrokeMode.Border

local MainBgImage = Instance.new("ImageLabel", Main)
MainBgImage.Size = UDim2.new(1, 0, 1, 0)
MainBgImage.BackgroundTransparency = 1
MainBgImage.Image = "" 
MainBgImage.ScaleType = Enum.ScaleType.Crop
MainBgImage.ZIndex = 0

-- ============================================================================
-- FUTURISTIC KEY VERIFICATION WINDOW INTERFACE
-- ============================================================================
local KeyFrame = Instance.new("Frame")
KeyFrame.Size = UDim2.new(0, 420, 0, 260)
KeyFrame.Position = UDim2.new(0.5, -210, 0.5, -130)
KeyFrame.BackgroundColor3 = Color3.fromRGB(6, 7, 14)
KeyFrame.Parent = ScreenGui
Instance.new("UICorner", KeyFrame).CornerRadius = UDim.new(0, 14)

local KeyStroke = Instance.new("UIStroke", KeyFrame)
KeyStroke.Color = Color3.fromRGB(0, 102, 255)
KeyStroke.Thickness = 2

local KeyTitle = Instance.new("TextLabel", KeyFrame)
KeyTitle.Size = UDim2.new(1, 0, 0, 45)
KeyTitle.Text = "// CYBER GATEKEEPER SYSTEM //"
KeyTitle.TextColor3 = Color3.fromRGB(0, 180, 255)
KeyTitle.Font = Enum.Font.GothamBold
KeyTitle.TextSize = 14
KeyTitle.BackgroundTransparency = 1

local KeyNote = Instance.new("TextLabel", KeyFrame)
KeyNote.Size = UDim2.new(1, -40, 0, 40)
KeyNote.Position = UDim2.new(0, 20, 0, 50)
KeyNote.Text = "Click 'GET KEY LINK' to get the key.\nBypass."
KeyNote.TextColor3 = Color3.fromRGB(140, 160, 180)
KeyNote.Font = Enum.Font.Gotham
KeyNote.TextSize = 11
KeyNote.TextWrapped = true
KeyNote.BackgroundTransparency = 1

local KeyInput = Instance.new("TextBox", KeyFrame)
KeyInput.Size = UDim2.new(1, -60, 0, 42)
KeyInput.Position = UDim2.new(0, 30, 0, 105)
KeyInput.BackgroundColor3 = Color3.fromRGB(12, 14, 26)
KeyInput.Text = ""
KeyInput.PlaceholderText = "Input key here..."
KeyInput.TextColor3 = Color3.fromRGB(255, 255, 255)
KeyInput.Font = Enum.Font.Gotham
KeyInput.TextSize = 12
Instance.new("UICorner", KeyInput).CornerRadius = UDim.new(0, 6)
local InputStroke = Instance.new("UIStroke", KeyInput)
InputStroke.Color = Color3.fromRGB(0, 102, 255)
InputStroke.Thickness = 1

local GetKeyBtn = Instance.new("TextButton", KeyFrame)
GetKeyBtn.Size = UDim2.new(0, 170, 0, 40)
GetKeyBtn.Position = UDim2.new(0, 30, 0, 170)
GetKeyBtn.BackgroundColor3 = Color3.fromRGB(16, 20, 38)
GetKeyBtn.Text = "GET KEY LINK"
GetKeyBtn.TextColor3 = Color3.fromRGB(0, 180, 255)
GetKeyBtn.Font = Enum.Font.GothamBold
GetKeyBtn.TextSize = 11
Instance.new("UICorner", GetKeyBtn).CornerRadius = UDim.new(0, 6)
Instance.new("UIStroke", GetKeyBtn).Color = Color3.fromRGB(0, 102, 255)

local VerifyBtn = Instance.new("TextButton", KeyFrame)
VerifyBtn.Size = UDim2.new(0, 170, 0, 40)
VerifyBtn.Position = UDim2.new(1, -200, 0, 170)
VerifyBtn.BackgroundColor3 = Color3.fromRGB(0, 102, 255)
VerifyBtn.Text = "ACCESS VERIFY"
VerifyBtn.TextColor3 = Color3.fromRGB(255, 255, 255)
VerifyBtn.Font = Enum.Font.GothamBold
VerifyBtn.TextSize = 11
Instance.new("UICorner", VerifyBtn).CornerRadius = UDim.new(0, 6)

local kDrag, kDragPos, kDragMouse = false, nil, nil
KeyFrame.InputBegan:Connect(function(i)
    if i.UserInputType == Enum.UserInputType.MouseButton1 or i.UserInputType == Enum.UserInputType.Touch then
        kDrag = true; kDragPos = KeyFrame.Position; kDragMouse = i.Position
        i.Changed:Connect(function() if i.UserInputState == Enum.UserInputState.End then kDrag = false end end)
    end
end)
UserInputService.InputChanged:Connect(function(i)
    if kDrag and (i.UserInputType == Enum.UserInputType.MouseMovement or i.UserInputType == Enum.UserInputType.Touch) then
        local delta = i.Position - kDragMouse
        KeyFrame.Position = UDim2.new(kDragPos.X.Scale, kDragPos.X.Offset + delta.X, kDragPos.Y.Scale, kDragPos.Y.Offset + delta.Y)
    end
end)

local tabs = {}

local function UnveilMainGui()
    if KeyFrame then KeyFrame:Destroy() end
    Main.Visible = true
    SendNotification("V.O.I.D HUB", "Granted! Open/closed use [RCTRL] or You can click the button", 6)
    
    if tabs["🏃 MOVEMENT"] then
        tabs["🏃 MOVEMENT"].Button.BackgroundColor3 = Color3.fromRGB(0, 102, 255)
        tabs["🏃 MOVEMENT"].Button.TextColor3 = Color3.fromRGB(255, 255, 255)
        tabs["🏃 MOVEMENT"].Content.Visible = true
        tabs["🏃 MOVEMENT"].Content.Position = UDim2.new(0, 0, 0, 0)
    end
end

GetKeyBtn.MouseButton1Click:Connect(function()
    if setclipboard then
        setclipboard(Config.KeySystem.MonetizedLink)
        SendNotification("KEY SYSTEM", "Link Copied!.", 5)
    else
        KeyInput.Text = Config.KeySystem.MonetizedLink
        SendNotification("KEY SYSTEM", "Your executor doesn't support clipboard copy. Link is displayed in the text box!", 6)
    end
end)

if Config.KeySystem.Verified then
    task.spawn(function()
        task.wait(0.5)
        UnveilMainGui()
    end)
end

VerifyBtn.MouseButton1Click:Connect(function()
    local UserInputKey = KeyInput.Text
    if UserInputKey == CorrectKeyToday and CorrectKeyToday ~= "" then
        Config.KeySystem.Verified = true
        SaveSession()
        UnveilMainGui()
    else
        KeyInput.Text = ""
        KeyInput.PlaceholderText = "WRONG / EXPIRED!!"
        SendNotification("KEY SYSTEM", "WRONG / EXPIRED!", 5)
    end
end)

-- ============================================================================
-- MAIN GUI HEADER BAR & WINDOW OPERATIONS
-- ============================================================================
local TitleBar = Instance.new("Frame", Main)
TitleBar.Size = UDim2.new(1, 0, 0, 45)
TitleBar.BackgroundTransparency = 1
TitleBar.ZIndex = 3

local Title = Instance.new("TextLabel", TitleBar)
Title.Size = UDim2.new(1, -60, 1, 0)
Title.Position = UDim2.new(0, 15, 0, 0)
Title.Text = " V.O.I.D // NEXUS "
Title.TextColor3 = Color3.fromRGB(0, 180, 255)
Title.Font = Enum.Font.GothamBold
Title.TextSize = 13
Title.TextXAlignment = Enum.TextXAlignment.Left
Title.BackgroundTransparency = 1
Title.ZIndex = 3

local MinBtn = Instance.new("TextButton", TitleBar)
MinBtn.Size = UDim2.new(0, 40, 0, 40)
MinBtn.Position = UDim2.new(1, -50, 0, 3)
MinBtn.BackgroundTransparency = 1
MinBtn.Text = "—"
MinBtn.TextColor3 = Color3.fromRGB(0, 180, 255)
MinBtn.Font = Enum.Font.GothamBold
MinBtn.TextSize = 24
MinBtn.ZIndex = 3

local RestoreBtn = Instance.new("TextButton")
RestoreBtn.Size = UDim2.new(0, 95, 0, 35)
RestoreBtn.Position = UDim2.new(0, 15, 0, 15)
RestoreBtn.BackgroundColor3 = Color3.fromRGB(6, 7, 14)
RestoreBtn.Text = "V.O.I.D"
RestoreBtn.TextColor3 = Color3.fromRGB(0, 180, 255)
RestoreBtn.Font = Enum.Font.GothamBold
RestoreBtn.TextSize = 10
RestoreBtn.Visible = false
RestoreBtn.Parent = ScreenGui
Instance.new("UICorner", RestoreBtn).CornerRadius = UDim.new(0, 6)
local RestorStroke = Instance.new("UIStroke", RestoreBtn)
RestorStroke.Color = Color3.fromRGB(0, 102, 255)
RestorStroke.Thickness = 1.5

local drag, dragPos, dragMouse = false, nil, nil
TitleBar.InputBegan:Connect(function(i)
    if i.UserInputType == Enum.UserInputType.MouseButton1 or i.UserInputType == Enum.UserInputType.Touch then
        drag = true; dragPos = Main.Position; dragMouse = i.Position
        i.Changed:Connect(function() if i.UserInputState == Enum.UserInputState.End then drag = false end end)
    end
end)
UserInputService.InputChanged:Connect(function(i)
    if drag and (i.UserInputType == Enum.UserInputType.MouseMovement or i.UserInputType == Enum.UserInputType.Touch) then
        local delta = i.Position - dragMouse
        Main.Position = UDim2.new(dragPos.X.Scale, dragPos.X.Offset + delta.X, dragPos.Y.Scale, dragPos.Y.Offset + delta.Y)
    end
end)

local function MinimizeUI()
    TweenService:Create(Main, TweenInfo.new(0.2, Enum.EasingStyle.Quad, Enum.EasingDirection.In), {Size = UDim2.new(0, 0, 0, 0), BackgroundTransparency = 1}):Play()
    task.wait(0.2); Main.Visible = false; RestoreBtn.Visible = true
end

local function RestoreUI()
    if not Config.KeySystem.Verified then return end
    RestoreBtn.Visible = false; Main.Visible = true
    TweenService:Create(Main, TweenInfo.new(0.25, Enum.EasingStyle.Quad, Enum.EasingDirection.Out), {Size = UDim2.new(0, 580, 0, 620), BackgroundTransparency = 0.1}):Play()
end

MinBtn.MouseButton1Click:Connect(MinimizeUI)
RestoreBtn.MouseButton1Click:Connect(RestoreUI)

-- ============================================================================
-- STRUCTURAL TAB BINDINGS EXECUTIONS
-- ============================================================================
local TabBar = Instance.new("Frame", Main)
TabBar.Size = UDim2.new(0, 130, 1, -60)
TabBar.Position = UDim2.new(0, 10, 0, 55)
TabBar.BackgroundTransparency = 1
TabBar.ZIndex = 3

local TabList = Instance.new("UIListLayout", TabBar)
TabList.Padding = UDim.new(0, 6)
TabList.HorizontalAlignment = Enum.HorizontalAlignment.Center

local TabContent = Instance.new("Frame", Main)
TabContent.Size = UDim2.new(1, -155, 1, -60)
TabContent.Position = UDim2.new(0, 145, 0, 55)
TabContent.BackgroundTransparency = 1
TabContent.ClipsDescendants = true
TabContent.ZIndex = 2

local function CreateTab(name)
    local btn = Instance.new("TextButton", TabBar)
    btn.Size = UDim2.new(0, 115, 0, 35)
    btn.Text = name
    btn.BackgroundColor3 = Color3.fromRGB(15, 18, 32)
    btn.BackgroundTransparency = 0.3
    btn.TextColor3 = Color3.fromRGB(180, 210, 240)
    btn.Font = Enum.Font.GothamBold
    btn.TextSize = 10
    btn.ZIndex = 4
    Instance.new("UICorner", btn).CornerRadius = UDim.new(0, 8)
    Instance.new("UIStroke", btn).Color = Color3.fromRGB(0, 102, 255)

    local content = Instance.new("ScrollingFrame", TabContent)
    content.Size = UDim2.new(1, 0, 1, 0)
    content.BackgroundTransparency = 1
    content.CanvasSize = UDim2.new(0, 0, 0, 0)
    content.ScrollBarThickness = 4
    content.AutomaticCanvasSize = Enum.AutomaticSize.Y
    content.Visible = false
    content.ZIndex = 3

    local layout = Instance.new("UIListLayout", content)
    layout.Padding = UDim.new(0, 6)
    layout.HorizontalAlignment = Enum.HorizontalAlignment.Center

    layout:GetPropertyChangedSignal("AbsoluteContentSize"):Connect(function()
        content.CanvasSize = UDim2.new(0, 0, 0, layout.AbsoluteContentSize.Y + 20)
    end)

    tabs[name] = {Button = btn, Content = content}

    btn.MouseButton1Click:Connect(function()
        for _, t in pairs(tabs) do
            t.Button.BackgroundColor3 = Color3.fromRGB(15, 18, 32)
            t.Button.TextColor3 = Color3.fromRGB(180, 210, 240)
            t.Content.Visible = false
        end
        btn.BackgroundColor3 = Color3.fromRGB(0, 102, 255)
        btn.TextColor3 = Color3.fromRGB(255, 255, 255)
        content.Position = UDim2.new(0, 30, 0, 0)
        content.Visible = true
        TweenService:Create(content, TweenInfo.new(0.25, Enum.EasingStyle.Cubic, Enum.EasingDirection.Out), {Position = UDim2.new(0, 0, 0, 0)}):Play()
    end)
    return content
end

-- ============================================================================
-- INTERFACE COMPONENT BUILDER FUNCTIONS
-- ============================================================================
local function AddHeader(container, text)
    local f = Instance.new("Frame", container)
    f.Size = UDim2.new(1, 0, 0, 30)
    f.BackgroundTransparency = 1
    f.ZIndex = 3
    local l = Instance.new("TextLabel", f)
    l.Size = UDim2.new(1, 0, 1, 0)
    l.Position = UDim2.new(0, 10, 0, 0)
    l.Text = "» " .. text
    l.TextColor3 = Color3.fromRGB(0, 180, 255)
    l.Font = Enum.Font.GothamBold
    l.TextSize = 11
    l.BackgroundTransparency = 1
    l.TextXAlignment = Enum.TextXAlignment.Left
    l.ZIndex = 3
end

local function AddToggle(container, text, key)
    local f = Instance.new("Frame", container)
    f.Size = UDim2.new(1, 0, 0, 48)
    f.BackgroundColor3 = Color3.fromRGB(12, 14, 26)
    f.BackgroundTransparency = 0.2
    f.ZIndex = 3
    Instance.new("UICorner", f).CornerRadius = UDim.new(0, 8)
    local toggleStroke = Instance.new("UIStroke", f)
    toggleStroke.Color = Config[key] and Color3.fromRGB(0, 102, 255) or Color3.fromRGB(30, 42, 60)

    local label = Instance.new("TextLabel", f)
    label.Size = UDim2.new(0.6, 0, 1, 0)
    label.Position = UDim2.new(0, 12, 0, 0)
    label.Text = text .. " : " .. (Config[key] and "ENABLED" or "DISABLED")
    label.TextColor3 = Color3.fromRGB(225, 245, 255)
    label.Font = Enum.Font.Gotham
    label.TextSize = 11
    label.BackgroundTransparency = 1
    label.TextXAlignment = Enum.TextXAlignment.Left
    label.ZIndex = 3

    local btn = Instance.new("TextButton", f)
    btn.Size = UDim2.new(0, 80, 0, 32)
    btn.Position = UDim2.new(1, -95, 0.5, -16)
    btn.Text = Config[key] and "ON" or "OFF"
    btn.BackgroundColor3 = Config[key] and Color3.fromRGB(0, 102, 255) or Color3.fromRGB(40, 47, 65)
    btn.TextColor3 = Color3.fromRGB(255, 255, 255)
    btn.Font = Enum.Font.GothamBold
    btn.TextSize = 11
    btn.ZIndex = 3
    Instance.new("UICorner", btn).CornerRadius = UDim.new(0, 6)

    btn.MouseButton1Click:Connect(function()
        if not Config.KeySystem.Verified then return end
        Config[key] = not Config[key]
        label.Text = text .. " : " .. (Config[key] and "ENABLED" or "DISABLED")
        btn.Text = Config[key] and "ON" or "OFF"
        btn.BackgroundColor3 = Config[key] and Color3.fromRGB(0, 102, 255) or Color3.fromRGB(40, 47, 65)
        toggleStroke.Color = Config[key] and Color3.fromRGB(0, 102, 255) or Color3.fromRGB(30, 42, 60)

        local h, _ = GetChar()
        if h then
            if key == "SpeedActive" and not Config.SpeedActive then h.WalkSpeed = 16 end
        end
        if key == "PotatoModeActive" then ApplyPotatoMode(Config.PotatoModeActive) end
        if key == "FpsUnlockerActive" then
            pcall(function() if setfpscap then setfpscap(Config.FpsUnlockerActive and 999 or 60) end end)
        end
    end)
end

local function AddSlider(container, text, activeKey, valKey, minV, maxV)
    local f = Instance.new("Frame", container)
    f.Size = UDim2.new(1, 0, 0, 85)
    f.BackgroundColor3 = Color3.fromRGB(12, 14, 26)
    f.BackgroundTransparency = 0.2
    Instance.new("UICorner", f).CornerRadius = UDim.new(0, 8)
    local sliderStroke = Instance.new("UIStroke", f)
    sliderStroke.Color = Config[activeKey] and Color3.fromRGB(0, 102, 255) or Color3.fromRGB(30, 42, 60)

    local label = Instance.new("TextLabel", f)
    label.Size = UDim2.new(1, -20, 0, 25)
    label.Position = UDim2.new(0, 12, 0, 5)
    label.Text = text .. " [" .. (Config[activeKey] and "ENABLED" or "DISABLED") .. "] | " .. Config[valKey]
    label.TextColor3 = Color3.fromRGB(225, 245, 255)
    label.Font = Enum.Font.Gotham
    label.TextSize = 10
    label.BackgroundTransparency = 1
    label.TextXAlignment = Enum.TextXAlignment.Left

    local toggle = Instance.new("TextButton", f)
    toggle.Size = UDim2.new(0, 75, 0, 30)
    toggle.Position = UDim2.new(0, 12, 0, 32)
    toggle.Text = Config[activeKey] and "ON" or "OFF"
    toggle.BackgroundColor3 = Config[activeKey] and Color3.fromRGB(0, 102, 255) or Color3.fromRGB(40, 47, 65)
    toggle.TextColor3 = Color3.fromRGB(255, 255, 255)
    toggle.Font = Enum.Font.GothamBold
    toggle.TextSize = 11
    Instance.new("UICorner", toggle).CornerRadius = UDim.new(0, 6)

    local numBox = Instance.new("TextBox", f)
    numBox.Size = UDim2.new(0, 100, 0, 30)
    numBox.Position = UDim2.new(0.5, -50, 0, 32)
    numBox.BackgroundColor3 = Color3.fromRGB(6, 7, 14)
    numBox.Text = tostring(Config[valKey])
    numBox.PlaceholderText = "ketik angka"
    numBox.TextColor3 = Color3.fromRGB(255, 255, 255)
    numBox.Font = Enum.Font.Gotham
    numBox.TextSize = 11
    Instance.new("UICorner", numBox).CornerRadius = UDim.new(0, 6)
    Instance.new("UIStroke", numBox).Color = Color3.fromRGB(0, 102, 255)

    local plus = Instance.new("TextButton", f)
    plus.Size = UDim2.new(0, 45, 0, 30)
    plus.Position = UDim2.new(1, -105, 0, 32)
    plus.Text = "+"
    plus.BackgroundColor3 = Color3.fromRGB(30, 37, 55)
    plus.TextColor3 = Color3.fromRGB(0, 180, 255)
    plus.Font = Enum.Font.GothamBold
    plus.TextSize = 16
    Instance.new("UICorner", plus).CornerRadius = UDim.new(0, 6)

    local minus = Instance.new("TextButton", f)
    minus.Size = UDim2.new(0, 45, 0, 30)
    minus.Position = UDim2.new(1, -55, 0, 32)
    minus.Text = "-"
    minus.BackgroundColor3 = Color3.fromRGB(30, 37, 55)
    minus.TextColor3 = Color3.fromRGB(0, 180, 255)
    minus.Font = Enum.Font.GothamBold
    minus.TextSize = 16
    Instance.new("UICorner", minus).CornerRadius = UDim.new(0, 6)

    local function updateDisplay()
        label.Text = text .. " [" .. (Config[activeKey] and "ENABLED" or "DISABLED") .. "] | " .. Config[valKey]
        numBox.Text = tostring(Config[valKey])
    end

    toggle.MouseButton1Click:Connect(function()
        Config[activeKey] = not Config[activeKey]
        toggle.Text = Config[activeKey] and "ON" or "OFF"
        toggle.BackgroundColor3 = Config[activeKey] and Color3.fromRGB(0, 102, 255) or Color3.fromRGB(40, 47, 65)
        sliderStroke.Color = Config[activeKey] and Color3.fromRGB(0, 102, 255) or Color3.fromRGB(30, 42, 60)
        updateDisplay()
        if activeKey == "SpeedActive" and not Config.SpeedActive then
            local h, _ = GetChar()
            if h then h.WalkSpeed = 16 end
        end
    end)

    numBox.FocusLost:Connect(function(enterPressed)
        if enterPressed then
            local newVal = tonumber(numBox.Text)
            if newVal and newVal >= minV and newVal <= maxV then
                Config[valKey] = newVal
                updateDisplay()
            else
                updateDisplay()
            end
        end
    end)

    minus.MouseButton1Click:Connect(function()
        local newVal = Config[valKey] - 5
        if newVal >= minV then
            Config[valKey] = newVal
            updateDisplay()
        end
    end)

    plus.MouseButton1Click:Connect(function()
        local newVal = Config[valKey] + 5
        if newVal <= maxV then
            Config[valKey] = newVal
            updateDisplay()
        end
    end)
end

local function AddColorPicker(container, titleText, configKey)
    local f = Instance.new("Frame", container)
    f.Size = UDim2.new(1, 0, 0, 65)
    f.BackgroundColor3 = Color3.fromRGB(12, 14, 26)
    f.BackgroundTransparency = 0.2
    f.ZIndex = 3
    Instance.new("UICorner", f).CornerRadius = UDim.new(0, 8)
    Instance.new("UIStroke", f).Color = Color3.fromRGB(0, 102, 255)

    local label = Instance.new("TextLabel", f)
    label.Size = UDim2.new(0.4, 0, 1, 0)
    label.Position = UDim2.new(0, 12, 0, 0)
    label.Text = titleText
    label.TextColor3 = Color3.fromRGB(255, 255, 255)
    label.Font = Enum.Font.GothamBold
    label.TextSize = 11
    label.BackgroundTransparency = 1
    label.TextXAlignment = Enum.TextXAlignment.Left
    label.ZIndex = 3

    local colors = {
        {Color3.fromRGB(0, 102, 255), "Blue"},
        {Color3.fromRGB(0, 255, 255), "Cyan"},
        {Color3.fromRGB(0, 255, 100), "Green"},
        {Color3.fromRGB(255, 255, 0), "Yellow"},
        {Color3.fromRGB(255, 255, 255), "White"}
    }

    local paletteContainer = Instance.new("Frame", f)
    paletteContainer.Size = UDim2.new(0.55, 0, 0, 35)
    paletteContainer.Position = UDim2.new(0.42, 0, 0.5, -17.5)
    paletteContainer.BackgroundTransparency = 1
    paletteContainer.ZIndex = 3

    local list = Instance.new("UIListLayout", paletteContainer)
    list.FillDirection = Enum.FillDirection.Horizontal; list.Padding = UDim.new(0, 5); list.VerticalAlignment = Enum.VerticalAlignment.Center

    for _, colorData in pairs(colors) do
        local cBtn = Instance.new("TextButton", paletteContainer)
        cBtn.Size = UDim2.new(0, 22, 0, 22)
        cBtn.BackgroundColor3 = colorData[1]
        cBtn.Text = ""
        cBtn.ZIndex = 3
        Instance.new("UICorner", cBtn).CornerRadius = UDim.new(0, 4)

        cBtn.MouseButton1Click:Connect(function()
            if not Config.KeySystem.Verified then return end
            Config[configKey] = colorData[1]
            if FovCircle and configKey == "FovColor" then FovCircle.Color = Config.FovColor end
        end)
    end
end

local function AddSelector(container, text, configKey, options)
    local f = Instance.new("Frame", container)
    f.Size = UDim2.new(1, 0, 0, 50)
    f.BackgroundColor3 = Color3.fromRGB(12, 14, 26)
    f.BackgroundTransparency = 0.2
    f.ZIndex = 3
    Instance.new("UICorner", f).CornerRadius = UDim.new(0, 8)
    Instance.new("UIStroke", f).Color = Color3.fromRGB(0, 102, 255)

    local lbl = Instance.new("TextLabel", f)
    lbl.Size = UDim2.new(0.5, 0, 1, 0)
    lbl.Position = UDim2.new(0, 12, 0, 0)
    local displayVal = tostring(Config[configKey])
    if Config[configKey] == false then displayVal = "OFF" elseif Config[configKey] == true then displayVal = "ON" end
    lbl.Text = text .. " : " .. displayVal
    lbl.TextColor3 = Color3.fromRGB(230, 230, 230)
    lbl.Font = Enum.Font.Gotham
    lbl.TextSize = 11
    lbl.BackgroundTransparency = 1
    lbl.TextXAlignment = Enum.TextXAlignment.Left
    lbl.ZIndex = 3

    local btn = Instance.new("TextButton", f)
    btn.Size = UDim2.new(0, 100, 0, 32)
    btn.Position = UDim2.new(1, -115, 0.5, -16)
    btn.Text = "TOGGLE"
    btn.BackgroundColor3 = Color3.fromRGB(30, 35, 55)
    btn.TextColor3 = Color3.fromRGB(0, 180, 255)
    btn.Font = Enum.Font.GothamBold
    btn.TextSize = 11
    btn.ZIndex = 3
    Instance.new("UICorner", btn).CornerRadius = UDim.new(0, 6)
    Instance.new("UIStroke", btn).Color = Color3.fromRGB(0, 102, 255)

    btn.MouseButton1Click:Connect(function()
        local curIndex = 1
        for i, v in ipairs(options) do
            if v == Config[configKey] then curIndex = i break end
        end
        local nextIndex = curIndex + 1
        if nextIndex > #options then nextIndex = 1 end
        Config[configKey] = options[nextIndex]
        
        local newValStr = tostring(Config[configKey])
        if Config[configKey] == false then newValStr = "OFF" elseif Config[configKey] == true then newValStr = "ON" end
        lbl.Text = text .. " : " .. newValStr
    end)
end

-- ============================================================================
-- GENERATING ALL SUB-TAB OBJECTS
-- ============================================================================
local moveTab = CreateTab("🏃 MOVEMENT")
local visualTab = CreateTab("👁 VISUAL")
local coloringTab = CreateTab("🎨 COLORING")
local combatTab = CreateTab("🎯 COMBAT")
local boosterTab = CreateTab("🚀 BOOSTER")
local powerTab = CreateTab("🔌 SYSTEM CONTROL")
local infoTab = CreateTab("ℹ️ INFO")

-- MOVEMENT CONFIGS
AddHeader(moveTab, "MOVEMENT SETTINGS")
AddSlider(moveTab, "SPEED HACK", "SpeedActive", "SpeedValue", 16, 250)
AddSlider(moveTab, "HIGH & INF JUMP", "JumpActive", "JumpValue", 30, 250)
AddSlider(moveTab, "FLY MODE", "FlyActive", "FlyValue", 50, 400)
AddSlider(moveTab, "SPIN BOT SPEED", "SpinBotActive", "SpinSpeed", 5, 150)
AddToggle(moveTab, "NOCLIP", "NoclipActive")

-- VISUAL CONFIGS
AddHeader(visualTab, "VISUAL ESP SETTINGS")
AddToggle(visualTab, "AURA ESP (GLOW)", "AuraEspActive")
AddToggle(visualTab, "BOX ESP", "BoxEspActive")
AddToggle(visualTab, "TRACER ESP", "TracerActive")
AddSlider(visualTab, "BIG HITBOX ENGINE", "HitboxActive", "HitboxSize", 2, 60)
AddToggle(visualTab, "FULL BRIGHT", "FullBrightActive")
AddToggle(visualTab, "NO FOG", "NoFogActive")

-- COMBAT CONFIGS
AddHeader(combatTab, "AIMBOT CRITICAL MECHANICS")
AddToggle(combatTab, "AIMBOT MASTER SWITCH", "AimActive")
AddSlider(combatTab, "AIMBOT FOV SIZE", "ShowFov", "FovRadius", 50, 400) 
AddSelector(combatTab, "WALL CHECK LOCK", "AimWallActive", {false, true}) 
AddSelector(combatTab, "AIMBOT TARGET PART", "AimPart", {"Head", "Torso"}) 

-- COLORING CONFIGS
AddHeader(coloringTab, "THEME & ESP COLOR SETTINGS")
AddColorPicker(coloringTab, "AURA ESP GLOW", "AuraColor")
AddColorPicker(coloringTab, "BOX ESP COLOR", "BoxColor")
AddColorPicker(coloringTab, "TRACER COLOR", "TracerColor")
AddColorPicker(coloringTab, "FOV CIRCLE COLOR", "FovColor")

-- BOOSTER CONFIGS
AddHeader(boosterTab, "PERFORMANCE BOOSTER")
AddToggle(boosterTab, "POTATO MODE", "PotatoModeActive")
AddToggle(boosterTab, "FPS UNLOCKER", "FpsUnlockerActive")

-- ============================================================================
-- SYSTEM CONTROL TAB - ALL IN ONE (STALK + TELEPORT + BRING)
-- ============================================================================
AddHeader(powerTab, "TARGET SYSTEM (SAME NAME FOR ALL)")

-- Input box untuk nama target (SATU UNTUK SEMUA)
local TargetInputFrame = Instance.new("Frame", powerTab)
TargetInputFrame.Size = UDim2.new(1, 0, 0, 45)
TargetInputFrame.BackgroundTransparency = 1

local TargetNameBox = Instance.new("TextBox", TargetInputFrame)
TargetNameBox.Size = UDim2.new(1, 0, 1, 0)
TargetNameBox.BackgroundColor3 = Color3.fromRGB(12, 14, 26)
TargetNameBox.PlaceholderText = "Player target here..."
TargetNameBox.PlaceholderColor3 = Color3.fromRGB(0, 180, 255)
TargetNameBox.Text = ""
TargetNameBox.TextColor3 = Color3.fromRGB(255, 255, 255)
TargetNameBox.Font = Enum.Font.Gotham
TargetNameBox.TextSize = 11
Instance.new("UICorner", TargetNameBox).CornerRadius = UDim.new(0, 6)
Instance.new("UIStroke", TargetNameBox).Color = Color3.fromRGB(0, 102, 255)

-- ========== STALKER SYSTEM (FIX + TOMBOL GA ILANG) ==========
-- ========== STALKER KAMERA DI LU (BUKAN NEMPEL) + GERAK LAGI PAS OFF ==========
local StalkConnection = nil
local StalkActive = false
local StalkTargetPlayer = nil

local function StopStalking()
    if StalkConnection then
        StalkConnection:Disconnect()
        StalkConnection = nil
    end
    StalkActive = false
    StalkTargetPlayer = nil
    
    -- YANG PENTING: LEPAS ANCHORED BIAR BISA GERAK LAGI!
    if LocalPlayer.Character and LocalPlayer.Character:FindFirstChild("HumanoidRootPart") then
        LocalPlayer.Character.HumanoidRootPart.Anchored = false
    end
    
    -- Reset speed biar normal lagi
    local hum = LocalPlayer.Character and LocalPlayer.Character:FindFirstChildOfClass("Humanoid")
    if hum and Config.SpeedActive then
        hum.WalkSpeed = Config.SpeedValue
    elseif hum then
        hum.WalkSpeed = 16
    end
    
    print("Stalk OFF - Lu bisa gerak lagi!")
end

local function StartStalking(targetPlayer)
    StopStalking()
    if not targetPlayer or not targetPlayer.Character then 
        SendNotification("STALK ERROR", "ERROR!", 2)
        return false 
    end
    
    StalkActive = true
    StalkTargetPlayer = targetPlayer
    
    -- Kunci karakter lu biar tetep di belakang target (opsional, biar presisi)
    if LocalPlayer.Character and LocalPlayer.Character:FindFirstChild("HumanoidRootPart") then
        LocalPlayer.Character.HumanoidRootPart.Anchored = true
        local hum = LocalPlayer.Character:FindFirstChildOfClass("Humanoid")
        if hum then hum.WalkSpeed = 0 end
    end
    
    -- LOOP NGE-STALK: POSISI LU NGIKUTIN TARGET, TAPI KAMERA TETEP DI LU!
    StalkConnection = RunService.RenderStepped:Connect(function()
        if StalkTargetPlayer and StalkTargetPlayer.Character and StalkTargetPlayer.Character:FindFirstChild("HumanoidRootPart") then
            local targetHrp = StalkTargetPlayer.Character.HumanoidRootPart
            local myHrp = LocalPlayer.Character and LocalPlayer.Character:FindFirstChild("HumanoidRootPart")
            
            if myHrp then
                -- Lu ngikutin target dari belakang (offset 5 stud di belakang)
                local behindOffset = targetHrp.CFrame.LookVector * -5
                myHrp.CFrame = CFrame.new(targetHrp.Position + behindOffset, targetHrp.Position)
            end
        else
            -- Target ilang/mati -> matiin stalk
            StopStalking()
            if StalkBtn then 
                StalkBtn.Text = "👁️ STALK MODE: OFF"
                StalkBtn.BackgroundColor3 = Color3.fromRGB(40, 47, 65)
            end
            SendNotification("STALK", "Target gone, stalk off!", 2)
        end
    end)
    
    return true
end

-- Tombol Stalk
local StalkBtn = Instance.new("TextButton", powerTab)
StalkBtn.Size = UDim2.new(1, 0, 0, 45)
StalkBtn.Text = "👁️ STALK MODE: OFF"
StalkBtn.BackgroundColor3 = Color3.fromRGB(40, 47, 65)
StalkBtn.TextColor3 = Color3.fromRGB(255, 255, 255)
StalkBtn.Font = Enum.Font.GothamBold
StalkBtn.TextSize = 11
Instance.new("UICorner", StalkBtn).CornerRadius = UDim.new(0, 6)
Instance.new("UIStroke", StalkBtn).Color = Color3.fromRGB(0, 102, 255)

StalkBtn.MouseButton1Click:Connect(function()
    if not Config.KeySystem.Verified then return end
    if StalkActive then
        StopStalking()
        StalkBtn.Text = "👁️ STALK MODE: OFF"
        StalkBtn.BackgroundColor3 = Color3.fromRGB(40, 47, 65)
        SendNotification("STALK", "OFF - Lu bisa gerak lagi!", 2)
    else
        local target = GetPlayerByName(TargetNameBox.Text)
        if target then
            if StartStalking(target) then
                StalkBtn.Text = "👁️ STALK MODE: ON (" .. target.Name .. ")"
                StalkBtn.BackgroundColor3 = Color3.fromRGB(0, 102, 255)
                SendNotification("STALK", "Ngikutin " .. target.Name .. " dari belakang!", 2)
            end
        else
            SendNotification("STALK ERROR", "Target not found!", 2)
        end
    end
end)

-- Tombol Teleport
local TeleportBtn = Instance.new("TextButton", powerTab)
TeleportBtn.Size = UDim2.new(1, 0, 0, 45)
TeleportBtn.BackgroundColor3 = Color3.fromRGB(0, 51, 102)
TeleportBtn.Text = "⚡ TELEPORT TO TARGET"
TeleportBtn.TextColor3 = Color3.fromRGB(255, 255, 255)
TeleportBtn.Font = Enum.Font.GothamBold
TeleportBtn.TextSize = 11
Instance.new("UICorner", TeleportBtn).CornerRadius = UDim.new(0, 6)
Instance.new("UIStroke", TeleportBtn).Color = Color3.fromRGB(0, 102, 255)

TeleportBtn.MouseButton1Click:Connect(function()
    if not Config.KeySystem.Verified then return end
    local target = GetPlayerByName(TargetNameBox.Text)
    local _, myHrp = GetChar()
    if target and target.Character and target.Character:FindFirstChild("HumanoidRootPart") and myHrp then
        myHrp.CFrame = target.Character.HumanoidRootPart.CFrame
        SendNotification("TELEPORT", "STALK TO " .. target.Name .. "SUCCESFUL!", 2)
    else
        SendNotification("ERROR", "Target not found!", 2)
    end
end)

-- Tombol Bring (Bawa target ke lu)
local BringBtn = Instance.new("TextButton", powerTab)
BringBtn.Size = UDim2.new(1, 0, 0, 45)
BringBtn.BackgroundColor3 = Color3.fromRGB(0, 30, 70)
BringBtn.Text = "🌀 BRING TARGET TO YOU"
BringBtn.TextColor3 = Color3.fromRGB(255, 255, 255)
BringBtn.Font = Enum.Font.GothamBold
BringBtn.TextSize = 11
Instance.new("UICorner", BringBtn).CornerRadius = UDim.new(0, 6)
Instance.new("UIStroke", BringBtn).Color = Color3.fromRGB(0, 102, 255)

BringBtn.MouseButton1Click:Connect(function()
    if not Config.KeySystem.Verified then return end
    local target = GetPlayerByName(TargetNameBox.Text)
    local _, myHrp = GetChar()
    if target and target.Character and target.Character:FindFirstChild("HumanoidRootPart") and myHrp then
        target.Character.HumanoidRootPart.CFrame = myHrp.CFrame + Vector3.new(0, 0, 2)
        SendNotification("BRING", "You bring " .. target.Name .. "SUCCESFUL!", 2)
    else
        SendNotification("ERROR", "Target not found!", 2)
    end
end)

AddHeader(powerTab, "DANGER ZONE")

local exitBtn = Instance.new("TextButton", powerTab)
exitBtn.Size = UDim2.new(1, 0, 0, 45)
exitBtn.BackgroundColor3 = Color3.fromRGB(130, 0, 0)
exitBtn.Text = "⛔ DESTROY & EXIT SCRIPT"
exitBtn.TextColor3 = Color3.fromRGB(255, 255, 255)
exitBtn.Font = Enum.Font.GothamBold
exitBtn.TextSize = 11
Instance.new("UICorner", exitBtn).CornerRadius = UDim.new(0, 8)

exitBtn.MouseButton1Click:Connect(function()
    ScriptRunning = false
    StopStalking()
    ApplyPotatoMode(false)
    if FovCircle then FovCircle:Destroy() end
    local _, hrp = GetChar()
    if hrp and hrp:FindFirstChild("SpinBot_Velocity") then hrp.SpinBot_Velocity:Destroy() end
    if hrp and hrp:FindFirstChild("Fly") then hrp.Fly:Destroy() end
    if hrp then hrp.Anchored = false end
    for _, espObj in pairs(Esp) do
        pcall(function()
            espObj.Box.Visible = false
            espObj.Tracer.Visible = false
            espObj.Box:Destroy()
            espObj.Tracer:Destroy()
            if espObj.Highlight then espObj.Highlight:Destroy() end
        end)
    end
    ScreenGui:Destroy()
end)

-- ============================================================================
-- ℹ️ INFO TAB
-- ============================================================================
AddHeader(infoTab, "CREDITS CHRONICLES")

local AuthorLabel = Instance.new("TextLabel", infoTab)
AuthorLabel.Size = UDim2.new(1, -20, 0, 30)
AuthorLabel.BackgroundTransparency = 1
AuthorLabel.Text = "This script is made by: someone"
AuthorLabel.TextColor3 = Color3.fromRGB(255, 255, 255)
AuthorLabel.Font = Enum.Font.GothamBold
AuthorLabel.TextSize = 12
AuthorLabel.TextXAlignment = Enum.TextXAlignment.Center

local DiscordPromo = Instance.new("TextLabel", infoTab)
DiscordPromo.Size = UDim2.new(1, -20, 0, 30)
DiscordPromo.BackgroundTransparency = 1
DiscordPromo.Text = "Join our discord link below 👆"
DiscordPromo.TextColor3 = Color3.fromRGB(0, 180, 255)
DiscordPromo.Font = Enum.Font.Gotham
DiscordPromo.TextSize = 11
DiscordPromo.TextXAlignment = Enum.TextXAlignment.Center

local CopyDcBtn = Instance.new("TextButton", infoTab)
CopyDcBtn.Size = UDim2.new(1, -40, 0, 38)
CopyDcBtn.BackgroundColor3 = Color3.fromRGB(30, 45, 90)
CopyDcBtn.Text = "🔗 COPY DISCORD INVITE LINK"
CopyDcBtn.TextColor3 = Color3.fromRGB(255, 255, 255)
CopyDcBtn.Font = Enum.Font.GothamBold
CopyDcBtn.TextSize = 10
Instance.new("UICorner", CopyDcBtn).CornerRadius = UDim.new(0, 6)
Instance.new("UIStroke", CopyDcBtn).Color = Color3.fromRGB(0, 102, 255)

CopyDcBtn.MouseButton1Click:Connect(function()
    if setclipboard then
        setclipboard(Config.DiscordLink)
        SendNotification("DISCORD", "Link Discord Copied!", 2)
    else
        SendNotification("ERROR", "Failed!", 2)
    end
end)

-- ============================================================================
-- SYSTEM LOOPS & RUNENGINE THREADS
-- ============================================================================
UserInputService.JumpRequest:Connect(function()
    if not ScriptRunning or not Config.JumpActive or not Config.KeySystem.Verified then return end
    local _, hrp = GetChar()
    if hrp then hrp.Velocity = Vector3.new(hrp.Velocity.X, Config.JumpValue, hrp.Velocity.Z) end
end)

UserInputService.InputBegan:Connect(function(i)
    if not ScriptRunning or not Config.KeySystem.Verified then return end
    if i.KeyCode == Enum.KeyCode.RightControl then
        if Main.Visible then MinimizeUI() else RestoreUI() end
    end
end)

-- LOOP 1: HEARTBEAT CORE ENGINE
RunService.Heartbeat:Connect(function()
    if not ScriptRunning or not Config.KeySystem.Verified then return end
    local h, hrp = GetChar()
    if not h or not hrp then return end

    if Config.SpeedActive and not StalkActive then
        h.WalkSpeed = Config.SpeedValue
    elseif not StalkActive then
        if h.WalkSpeed == Config.SpeedValue then h.WalkSpeed = 16 end
    end

    if Config.SpinBotActive and not StalkActive then
        local spinVel = hrp:FindFirstChild("SpinBot_Velocity")
        if not spinVel then
            spinVel = Instance.new("BodyAngularVelocity")
            spinVel.Name = "SpinBot_Velocity"
            spinVel.MaxTorque = Vector3.new(0, math.huge, 0)
            spinVel.Parent = hrp
        end
        spinVel.AngularVelocity = Vector3.new(0, Config.SpinSpeed, 0)
    else
        if hrp:FindFirstChild("SpinBot_Velocity") then hrp.SpinBot_Velocity:Destroy() end
    end

    if Config.FlyActive and not StalkActive then
        local bv = hrp:FindFirstChild("Fly") or Instance.new("BodyVelocity", hrp)
        bv.Name = "Fly"
        bv.MaxForce = Vector3.new(1e6, 1e6, 1e6)
        local dir = Vector3.zero
        if UserInputService:IsKeyDown(Enum.KeyCode.W) then dir = dir + Camera.CFrame.LookVector end
        if UserInputService:IsKeyDown(Enum.KeyCode.S) then dir = dir - Camera.CFrame.LookVector end
        if UserInputService:IsKeyDown(Enum.KeyCode.A) then dir = dir - Camera.CFrame.RightVector end
        if UserInputService:IsKeyDown(Enum.KeyCode.D) then dir = dir + Camera.CFrame.RightVector end
        bv.Velocity = dir.Magnitude > 0 and dir.Unit * Config.FlyValue or Vector3.zero
    elseif hrp:FindFirstChild("Fly") then
        hrp.Fly:Destroy()
    end
end)

-- LOOP 2: RENDERSTEPPED (VISUAL SYSTEMS)
RunService.RenderStepped:Connect(function()
    if not ScriptRunning or not Config.KeySystem.Verified then return end

    if FovCircle then
        FovCircle.Position = Camera.ViewportSize / 2
        FovCircle.Radius = Config.FovRadius
        FovCircle.Color = Config.FovColor
        FovCircle.Visible = Config.ShowFov
    end

    if Config.FullBrightActive then
        Lighting.Ambient = Color3.fromRGB(255, 255, 255)
        Lighting.OutdoorAmbient = Color3.fromRGB(255, 255, 255)
        Lighting.Brightness = 2
    else
        Lighting.Ambient = OriginalLighting.Ambient
        Lighting.OutdoorAmbient = OriginalLighting.OutdoorAmbient
        Lighting.Brightness = OriginalLighting.Brightness
    end
    Lighting.FogEnd = Config.NoFogActive and 999999 or OriginalLighting.FogEnd

    local ClosestTarget = nil
    local MaxDistance = Config.FovRadius

    if Config.AimActive then
        for _, p in pairs(Players:GetPlayers()) do
            if p ~= LocalPlayer and p.Character and p.Character:FindFirstChildOfClass("Humanoid") and p.Character:FindFirstChildOfClass("Humanoid").Health > 0 then
                local targetPart = GetAimPartInstance(p.Character, Config.AimPart)
                
                if targetPart then
                    local pos, onScreen = Camera:WorldToViewportPoint(targetPart.Position)
                    
                    if onScreen then
                        local isVisible = true
                        if Config.AimWallActive then
                            local parts = Camera:GetPartsObscuringTarget({Camera.CFrame.Position, targetPart.Position}, {LocalPlayer.Character, p.Character})
                            if #parts > 0 then isVisible = false end
                        end
                        
                        if isVisible or not Config.AimWallActive then
                            local distance = (Vector2.new(pos.X, pos.Y) - (Camera.ViewportSize / 2)).Magnitude
                            if distance < MaxDistance then
                                ClosestTarget = targetPart
                                MaxDistance = distance
                            end
                        end
                    end
                end
            end
        end
        if ClosestTarget and UserInputService:IsMouseButtonPressed(Enum.UserInputType.MouseButton2) then
            Camera.CFrame = CFrame.new(Camera.CFrame.Position, ClosestTarget.Position)
        end
    end

    for _, p in pairs(Players:GetPlayers()) do
        if p ~= LocalPlayer and Esp[p] then
            local char = p.Character
            if char and char.Parent == workspace and char:FindFirstChild("HumanoidRootPart") and char:FindFirstChildOfClass("Humanoid") and char:FindFirstChildOfClass("Humanoid").Health > 0 then
                local hrp = char.HumanoidRootPart
                local pos, on = Camera:WorldToViewportPoint(hrp.Position)
                
                if Config.HitboxActive then
                    hrp.Size = Vector3.new(Config.HitboxSize, Config.HitboxSize, Config.HitboxSize)
                    hrp.Transparency = 0.7
                    hrp.Color = Config.BoxColor
                    hrp.CanCollide = false
                else
                    hrp.Size = Vector3.new(2, 2, 1)
                    hrp.Transparency = 1
                end

                if Config.AuraEspActive then
                    if not Esp[p].Highlight then
                        local hl = Instance.new("Highlight", char)
                        hl.Name = "AuraESP"; hl.DepthMode = Enum.HighlightDepthMode.AlwaysOnTop
                        Esp[p].Highlight = hl
                    end
                    Esp[p].Highlight.OutlineColor = Config.AuraColor
                    Esp[p].Highlight.FillColor = Config.AuraColor
                    Esp[p].Highlight.FillTransparency = 0.65
                elseif Esp[p].Highlight then
                    Esp[p].Highlight:Destroy(); Esp[p].Highlight = nil
                end

                if on then
                    local top = Camera:WorldToViewportPoint((hrp.CFrame * CFrame.new(0, 3, 0)).Position)
                    local bot = Camera:WorldToViewportPoint((hrp.CFrame * CFrame.new(0, -3.5, 0)).Position)
                    local h = math.abs(top.Y - bot.Y)
                    local w = h * 0.6

                    if Config.BoxEspActive then
                        Esp[p].Box.Size = Vector2.new(w, h)
                        Esp[p].Box.Position = Vector2.new(pos.X - w / 2, pos.Y - h / 2)
                        Esp[p].Box.Color = Config.BoxColor
                        Esp[p].Box.Visible = true
                    else Esp[p].Box.Visible = false end

                    if Config.TracerActive then
                        Esp[p].Tracer.From = Vector2.new(Camera.ViewportSize.X / 2, Camera.ViewportSize.Y)
                        Esp[p].Tracer.To = Vector2.new(pos.X, pos.Y)
                        Esp[p].Tracer.Color = Config.TracerColor
                        Esp[p].Tracer.Visible = true
                    else Esp[p].Tracer.Visible = false end
                else
                    Esp[p].Box.Visible = false
                    Esp[p].Tracer.Visible = false
                end
            else
                if Esp[p].Highlight then Esp[p].Highlight:Destroy(); Esp[p].Highlight = nil end
                Esp[p].Box.Visible = false
                Esp[p].Tracer.Visible = false
            end
        end
    end
end)

-- ============================================================================
-- NOCLIP ENGINE
-- ============================================================================
RunService.PreSimulation:Connect(function()
    if not ScriptRunning or not Config.KeySystem.Verified then return end
    
    if Config.NoclipActive or StalkActive then
        local char = LocalPlayer.Character
        if char then
            local charParts = char:GetChildren()
            for i = 1, #charParts do
                local part = charParts[i]
                if part:IsA("BasePart") then
                    part.CanCollide = false
                elseif part:IsA("Accessory") and part:FindFirstChild("Handle") then
                    part.Handle.CanCollide = false
                end
            end
        end
    end
end) 

print("⚡ V.O.I.D HUB [PATCH V4 - FINAL STABLE] DEPLOYED SUCCESSFULLY!")
