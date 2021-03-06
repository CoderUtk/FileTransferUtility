  ; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "FileTransfer"
#define MyAppVersion "1.0"
#define MyAppPublisher "Utkarsh"
#define MyAppExeName "FileTransfer.bat"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{4KKLK398-7706-434B-YU3D-K20FDAX8125X}
AppName={#MyAppName}
AppVersion={#MyAppVersion}

;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName=C:\\{#MyAppName}
DisableProgramGroupPage=yes
OutputDir=Executables\
OutputBaseFilename=FileTransferUtility
;SetupIconFile=logo.ico
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}";
 

[Files]
Source: "FileTransferUtility.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "build\*"; DestDir: "{app}\build"; Flags: ignoreversion recursesubdirs
Source: "lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs
Source: "manifest.mf"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs
Source: "build.xml"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs
Source: "Connections.json"; DestDir: "{app}"; Flags: ignoreversion
Source: "Keys\*"; DestDir: "{app}\Keys"; Flags: ignoreversion recursesubdirs
Source: "Scripts\*"; DestDir: "{app}\Scripts"; Flags: ignoreversion recursesubdirs
Source: "temp\*"; DestDir: "{app}\temp"; Flags: ignoreversion recursesubdirs
Source: "puttygen.exe"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs
Source: "FileTransfer.bat"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs


; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{commonprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: shellexec postinstall skipifsilent

