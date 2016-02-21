@echo off

:: BatchGotAdmin
:-------------------------------------
REM  --> Check for permissions
IF '%PROCESSOR_ARCHITECTURE%' EQU 'amd64' (
   >nul 2>&1 "%SYSTEMROOT%\SysWOW64\icacls.exe" "%SYSTEMROOT%\SysWOW64\config"
 ) ELSE (
   >nul 2>&1 "%SYSTEMROOT%\system32\icacls.exe" "%SYSTEMROOT%\system32\config"
)

REM --> If error flag set, we do not have admin.
if '%errorlevel%' NEQ '0' (
    echo Requesting administrative privileges...
    goto UACPrompt
) else ( goto gotAdmin )

:UACPrompt
    echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
    set params = %*:"=""
    echo UAC.ShellExecute "cmd.exe", "/c ""%~s0"" %params%", "", "runas", 1 >> "%temp%\getadmin.vbs"

    "%temp%\getadmin.vbs"
    del "%temp%\getadmin.vbs"
    exit /B

:gotAdmin
    pushd "%CD%"
    CD /D "%~dp0"
:-------------------------------------- 

IF EXIST %WINDIR%/SysWOW64 (
	cmd /c copy iomem.dll %WINDIR%\SysWOW64
) ELSE (
	cmd /c copy iomem.dll %WINDIR%\System32
)

REM cmd /c mkdir %WINDIR%\printer

REM cmd /c copy MCASmart.jar %WINDIR%\printer
REM cmd /c copy iomemJNI.dll %WINDIR%\printer
REM cmd /c copy prn_adapter_2.0.dll %WINDIR%\printer
