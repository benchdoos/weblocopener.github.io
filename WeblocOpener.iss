; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "WeblocOpener"
#define MyAppVersion "1.2"
#define MyAppPublisher "Eugene Zrazhevsky"
#define MyAppURL "https://github.com/benchdoos/WeblocOpener/"
#define MyAppExeName "WeblocOpener.exe"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{F1300E10-BBB2-4695-AC2F-3D58DC0BC0A6}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={pf}\{#MyAppName}
DefaultGroupName={#MyAppName}
OutputDir=F:\Developer\JAVA\WeblocOpener\out\setup
OutputBaseFilename=WeblocOpenerSetup
SetupIconFile=F:\Developer\JAVA\WeblocOpener\icon.ico
Compression=lzma
SolidCompression=yes
ChangesAssociations=yes
PrivilegesRequired=admin

[Registry]
; File association
Root: HKCR; Subkey: ".webloc"; ValueType: string; ValueName: ""; ValueData: "Webloc"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "Webloc"; ValueType: string; ValueName: ""; ValueData: "Webloc link"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Webloc\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\{#MyAppExeName},0"; Flags: uninsdeletevalue 
Root: HKCR; Subkey: "Webloc\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#MyAppExeName}"" ""%1"""; Flags: uninsdeletevalue

; Add new file .webloc
Root: HKCR; Subkey: ".webloc\ShellNew"; ValueType: string; ValueName: "ItemName"; ValueData: """{app}\{#MyAppExeName}"" ""%1"""; Flags: uninsdeletevalue
Root: HKCR; Subkey: ".webloc\ShellNew"; ValueType: string; ValueName: "FileName"; ValueData: "{app}\Template.webloc"; Flags: uninsdeletevalue
Root: HKCR; Subkey: ".webloc\ShellNew"; ValueType: string; ValueName: "NullFile"; ValueData: ""; Flags: uninsdeletevalue

; Add edit file menu
Root: HKCR; Subkey: "Webloc\shell\edit\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#MyAppExeName}"" ""-edit"" ""%1"" "; Flags: uninsdeletevalue          


; Add updater autorun
Root: HKCU; Subkey: "SOFTWARE\Microsoft\Windows\CurrentVersion\Run"; ValueType: string; ValueName: "WeblocOpenerUpdater"; ValueData: """{app}\Updater.jar"" ""-s"""; Flags: uninsdeletevalue

; Add app info
Root: HKCU; Subkey: "SOFTWARE\WeblocOpener"; ValueType: string; ValueName: ""; ValueData: ""; Flags: uninsdeletevalue  
Root: HKCU; Subkey: "SOFTWARE\WeblocOpener"; ValueType: string; ValueName: "Name"; ValueData: "{#MyAppName}"; Flags: uninsdeletevalue 
Root: HKCU; Subkey: "SOFTWARE\WeblocOpener"; ValueType: string; ValueName: "URLUpdateInfo"; ValueData: "{#MyAppURL}"; Flags: uninsdeletevalue 
Root: HKCU; Subkey: "SOFTWARE\WeblocOpener"; ValueType: string; ValueName: "CurrentVersion"; ValueData: "{#MyAppVersion}"; Flags: uninsdeletevalue 
Root: HKCU; Subkey: "SOFTWARE\WeblocOpener"; ValueType: string; ValueName: "InstallLocation"; ValueData: "{app}\"; Flags: uninsdeletevalue 
;Root: HKCU; Subkey: "SOFTWARE\WeblocOpener"; ValueType: string; ValueName: "ProductId"; ValueData: ""; Flags: uninsdeletevalue 
Root: HKCU; Subkey: "SOFTWARE\WeblocOpener"; ValueType: string; ValueName: "AutoUpdateEnabled"; ValueData: "true"; Flags: uninsdeletevalue 

Root: HKCU; Subkey: "SOFTWARE\WeblocOpener\Capabilities"; ValueType: string; ValueName: "ApplicationDescription"; ValueData: "Open, edit and create .webloc links on Windows"; Flags: uninsdeletevalue 
Root: HKCU; Subkey: "SOFTWARE\WeblocOpener\Capabilities\FileAssociations"; ValueType: string; ValueName: ".webloc"; ValueData: "Webloc link"; Flags: uninsdeletevalue 





[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"
Name: "catalan"; MessagesFile: "compiler:Languages\Catalan.isl"
Name: "corsican"; MessagesFile: "compiler:Languages\Corsican.isl"
Name: "czech"; MessagesFile: "compiler:Languages\Czech.isl"
Name: "danish"; MessagesFile: "compiler:Languages\Danish.isl"
Name: "dutch"; MessagesFile: "compiler:Languages\Dutch.isl"
Name: "finnish"; MessagesFile: "compiler:Languages\Finnish.isl"
Name: "french"; MessagesFile: "compiler:Languages\French.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"
Name: "greek"; MessagesFile: "compiler:Languages\Greek.isl"
Name: "hebrew"; MessagesFile: "compiler:Languages\Hebrew.isl"
Name: "hungarian"; MessagesFile: "compiler:Languages\Hungarian.isl"
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"
Name: "japanese"; MessagesFile: "compiler:Languages\Japanese.isl"
Name: "norwegian"; MessagesFile: "compiler:Languages\Norwegian.isl"
Name: "polish"; MessagesFile: "compiler:Languages\Polish.isl"
Name: "portuguese"; MessagesFile: "compiler:Languages\Portuguese.isl"
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl"
Name: "scottishgaelic"; MessagesFile: "compiler:Languages\ScottishGaelic.isl"
Name: "serbiancyrillic"; MessagesFile: "compiler:Languages\SerbianCyrillic.isl"
Name: "serbianlatin"; MessagesFile: "compiler:Languages\SerbianLatin.isl"
Name: "slovenian"; MessagesFile: "compiler:Languages\Slovenian.isl"
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"
Name: "turkish"; MessagesFile: "compiler:Languages\Turkish.isl"
Name: "ukrainian"; MessagesFile: "compiler:Languages\Ukrainian.isl"

[Files]
Source: "F:\Developer\JAVA\WeblocOpener\out\artifacts\WeblocOpener\WeblocOpener.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "F:\Developer\JAVA\WeblocOpener\out\artifacts\WeblocOpener\Template.webloc"; DestDir: "{app}"; Flags: ignoreversion
Source: "F:\Developer\JAVA\WeblocOpener\out\artifacts\Updater\Updater.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "F:\Developer\JAVA\WeblocOpener\readme.rtf"; DestDir: "{app}"; Flags: ignoreversion
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"