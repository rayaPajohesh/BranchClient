@echo off

rem Windows has no built-in wget or curl, so generate a VBS script to do it:
rem -------------------------------------------------------------------------
set DLOAD_SCRIPT=download.vbs
echo Option Explicit                                                    >  %DLOAD_SCRIPT%
echo Dim args, http, fileSystem, adoStream, url, target, status         >> %DLOAD_SCRIPT%
echo.                                                                   >> %DLOAD_SCRIPT%
echo Set args = Wscript.Arguments                                       >> %DLOAD_SCRIPT%
echo Set http = CreateObject("WinHttp.WinHttpRequest.5.1")              >> %DLOAD_SCRIPT%
echo url = args(0)                                                      >> %DLOAD_SCRIPT%
echo target = args(1)                                                   >> %DLOAD_SCRIPT%
echo WScript.Echo "Getting '" ^& target ^& "' from '" ^& url ^& "'..."  >> %DLOAD_SCRIPT%
echo.                                                                   >> %DLOAD_SCRIPT%
echo http.Open "GET", url, False                                        >> %DLOAD_SCRIPT%
echo http.Send                                                          >> %DLOAD_SCRIPT%
echo status = http.Status                                               >> %DLOAD_SCRIPT%
echo.                                                                   >> %DLOAD_SCRIPT%
echo If status ^<^> 200 Then                                            >> %DLOAD_SCRIPT%
echo    WScript.Echo "FAILED to download: HTTP Status " ^& status       >> %DLOAD_SCRIPT%
echo    WScript.Quit 1                                                  >> %DLOAD_SCRIPT%
echo End If                                                             >> %DLOAD_SCRIPT%
echo.                                                                   >> %DLOAD_SCRIPT%
echo Set adoStream = CreateObject("ADODB.Stream")                       >> %DLOAD_SCRIPT%
echo adoStream.Open                                                     >> %DLOAD_SCRIPT%
echo adoStream.Type = 1                                                 >> %DLOAD_SCRIPT%
echo adoStream.Write http.ResponseBody                                  >> %DLOAD_SCRIPT%
echo adoStream.Position = 0                                             >> %DLOAD_SCRIPT%
echo.                                                                   >> %DLOAD_SCRIPT%
echo Set fileSystem = CreateObject("Scripting.FileSystemObject")        >> %DLOAD_SCRIPT%
echo If fileSystem.FileExists(target) Then fileSystem.DeleteFile target >> %DLOAD_SCRIPT%
echo adoStream.SaveToFile target                                        >> %DLOAD_SCRIPT%
echo adoStream.Close                                                    >> %DLOAD_SCRIPT%
echo.                                                                   >> %DLOAD_SCRIPT%
rem -------------------------------------------------------------------------

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

mkdir c:\BranchClient

cscript //Nologo %DLOAD_SCRIPT% http://172.17.18.102:4040/ali/BranchClient/BranchClient.jar c:\BranchClient\BranchClient.jar
cscript //Nologo %DLOAD_SCRIPT% http://172.17.18.102:4040/ali/BranchClient/log4j-1.2.17.jar c:\BranchClient\log4j-1.2.17.jar
cscript //Nologo %DLOAD_SCRIPT% http://172.17.18.102:4040/ali/BranchClient/config.properties c:\BranchClient\config.properties
cscript //Nologo %DLOAD_SCRIPT% http://172.17.18.102:4040/ali/BranchClient/log4j.properties c:\BranchClient\log4j.properties

IF EXIST "%ProgramData%\Microsoft\Windows\Start Menu\Programs\Startup" (
	set STARTUP_DIR="%ProgramData%\Microsoft\Windows\Start Menu\Programs\Startup\BranchClient.bat"
) ELSE (
	set STARTUP_DIR="%ALLUSERSPROFILE%\Start Menu\Programs\Startup\BranchClient.bat"	
)
cscript //Nologo %DLOAD_SCRIPT% http://172.17.18.102:4040/ali/BranchClient/BranchClient.bat %STARTUP_DIR%

cd c:\BranchClient
start javaw -cp c:\BranchClient;c:\BranchClient\BranchClient.jar;c:\BranchClient\log4j-1.2.17.jar com.iac.branchClient.Updater

exit